package greendream.ait.tarot.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * SpreadDesignerView - Drag-and-drop canvas for designing custom spreads.
 * Inspired by Galaxy Tarot's SpreadLayout.
 * 
 * Features:
 * - Drag cards to position them
 * - Double-tap to toggle portrait/landscape
 * - Long-press to edit label or remove card
 * - Snap-to-grid alignment
 * - Auto-arrange cards in grid
 */
public class SpreadDesignerView extends FrameLayout {

	// Card dimensions (relative to canvas - will be scaled)
	private static final int BASE_CARD_WIDTH = 70;
	private static final int BASE_CARD_HEIGHT = 110;
	private static final int SNAP_THRESHOLD = 20; // pixels
	private static final int GRID_SPACING = 40; // pixels

	private List<CardPlaceholder> cards = new ArrayList<CardPlaceholder>();
	private CardPlaceholder selectedCard = null;
	private int dragOffsetX, dragOffsetY;
	private boolean isDragging = false;
	private boolean showGrid = true;

	private Paint gridPaint;
	private Paint cardPaint;
	private Paint cardStrokePaint;
	private Paint selectedPaint;
	private Paint numberPaint;
	private Paint labelPaint;

	private GestureDetector gestureDetector;
	private OnDesignerChangeListener changeListener;

	/**
	 * Card placeholder data
	 */
	public static class CardPlaceholder {
		public int x; // pixel position
		public int y;
		public int rotation; // 0 = portrait, 180 = landscape
		public String label;
		public int index; // card number (1-based)
		public View view; // the visual view

		public CardPlaceholder(int index, int x, int y, int rotation, String label) {
			this.index = index;
			this.x = x;
			this.y = y;
			this.rotation = rotation;
			this.label = label != null ? label : "Vị trí " + index;
		}

		public int getWidth() {
			return rotation == 180 ? BASE_CARD_HEIGHT : BASE_CARD_WIDTH;
		}

		public int getHeight() {
			return rotation == 180 ? BASE_CARD_WIDTH : BASE_CARD_HEIGHT;
		}
	}

	public interface OnDesignerChangeListener {
		void onCardCountChanged(int count);
		void onCardSelected(CardPlaceholder card);
	}

	public SpreadDesignerView(Context context) {
		super(context);
		init(context);
	}

	public SpreadDesignerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SpreadDesignerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		setWillNotDraw(false); // Enable onDraw for grid

		// Grid paint (dashed lines)
		gridPaint = new Paint();
		gridPaint.setColor(Color.argb(40, 255, 255, 255));
		gridPaint.setStrokeWidth(1);
		gridPaint.setStyle(Paint.Style.STROKE);
		gridPaint.setPathEffect(new DashPathEffect(new float[]{5, 5}, 0));

		// Card fill paint
		cardPaint = new Paint();
		cardPaint.setColor(Color.argb(180, 60, 50, 120));
		cardPaint.setStyle(Paint.Style.FILL);
		cardPaint.setAntiAlias(true);

		// Card border paint
		cardStrokePaint = new Paint();
		cardStrokePaint.setColor(Color.argb(200, 180, 160, 220));
		cardStrokePaint.setStrokeWidth(2);
		cardStrokePaint.setStyle(Paint.Style.STROKE);
		cardStrokePaint.setAntiAlias(true);

		// Selected card highlight
		selectedPaint = new Paint();
		selectedPaint.setColor(Color.argb(220, 255, 215, 0));
		selectedPaint.setStrokeWidth(3);
		selectedPaint.setStyle(Paint.Style.STROKE);
		selectedPaint.setAntiAlias(true);

		// Number text paint
		numberPaint = new Paint();
		numberPaint.setColor(Color.WHITE);
		numberPaint.setTextSize(28);
		numberPaint.setTextAlign(Paint.Align.CENTER);
		numberPaint.setAntiAlias(true);
		numberPaint.setFakeBoldText(true);

		// Label text paint
		labelPaint = new Paint();
		labelPaint.setColor(Color.argb(200, 200, 200, 200));
		labelPaint.setTextSize(12);
		labelPaint.setTextAlign(Paint.Align.CENTER);
		labelPaint.setAntiAlias(true);

		// Gesture detector for double-tap and long press
		gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onDoubleTap(MotionEvent e) {
				CardPlaceholder card = findCardAt((int) e.getX(), (int) e.getY());
				if (card != null) {
					// Toggle rotation
					card.rotation = (card.rotation == 0) ? 180 : 0;
					invalidate();
					return true;
				}
				return false;
			}

			@Override
			public void onLongPress(MotionEvent e) {
				CardPlaceholder card = findCardAt((int) e.getX(), (int) e.getY());
				if (card != null) {
					showCardOptionsDialog(card);
				}
			}

			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				CardPlaceholder card = findCardAt((int) e.getX(), (int) e.getY());
				if (card != null) {
					selectedCard = card;
					if (changeListener != null) {
						changeListener.onCardSelected(card);
					}
					invalidate();
					return true;
				}
				// Tap empty area - deselect
				selectedCard = null;
				invalidate();
				return false;
			}
		});
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		gestureDetector.onTouchEvent(event);

		int x = (int) event.getX();
		int y = (int) event.getY();

		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				CardPlaceholder card = findCardAt(x, y);
				if (card != null) {
					selectedCard = card;
					isDragging = true;
					dragOffsetX = x - card.x;
					dragOffsetY = y - card.y;
					// Bring to front
					invalidate();
					return true;
				}
				break;

			case MotionEvent.ACTION_MOVE:
				if (isDragging && selectedCard != null) {
					int newX = x - dragOffsetX;
					int newY = y - dragOffsetY;

					// Constrain to canvas
					newX = Math.max(0, Math.min(newX, getWidth() - selectedCard.getWidth()));
					newY = Math.max(0, Math.min(newY, getHeight() - selectedCard.getHeight()));

					// Snap to grid
					if (showGrid) {
						newX = snapToGrid(newX);
						newY = snapToGrid(newY);
					}

					selectedCard.x = newX;
					selectedCard.y = newY;
					invalidate();
					return true;
				}
				break;

			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				isDragging = false;
				break;
		}
		return true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// Draw grid lines
		if (showGrid) {
			for (int x = 0; x < getWidth(); x += GRID_SPACING) {
				canvas.drawLine(x, 0, x, getHeight(), gridPaint);
			}
			for (int y = 0; y < getHeight(); y += GRID_SPACING) {
				canvas.drawLine(0, y, getWidth(), y, gridPaint);
			}
		}

		// Draw each card placeholder
		for (CardPlaceholder card : cards) {
			drawCard(canvas, card);
		}
	}

	private void drawCard(Canvas canvas, CardPlaceholder card) {
		float left = card.x;
		float top = card.y;
		float right = left + card.getWidth();
		float bottom = top + card.getHeight();

		RectF rect = new RectF(left, top, right, bottom);

		// Fill
		canvas.drawRoundRect(rect, 6, 6, cardPaint);

		// Border
		if (card == selectedCard) {
			canvas.drawRoundRect(rect, 6, 6, selectedPaint);
		} else {
			canvas.drawRoundRect(rect, 6, 6, cardStrokePaint);
		}

		// Card number
		float cx = left + card.getWidth() / 2f;
		float cy = top + card.getHeight() / 2f;
		canvas.drawText(String.valueOf(card.index), cx, cy + 10, numberPaint);

		// Rotation indicator
		if (card.rotation == 180) {
			// Draw a small "L" indicator for landscape
			Paint indicatorPaint = new Paint();
			indicatorPaint.setColor(Color.argb(180, 255, 215, 0));
			indicatorPaint.setTextSize(14);
			indicatorPaint.setTextAlign(Paint.Align.LEFT);
			indicatorPaint.setAntiAlias(true);
			canvas.drawText("↔", left + 4, top + 14, indicatorPaint);
		}

		// Label below card number
		if (card.label != null && card.label.length() > 0) {
			String displayLabel = card.label;
			if (displayLabel.length() > 10) {
				displayLabel = displayLabel.substring(0, 8) + "..";
			}
			canvas.drawText(displayLabel, cx, cy + 28, labelPaint);
		}
	}

	/**
	 * Add a new card at the center of the canvas
	 */
	public void addCard() {
		int index = cards.size() + 1;
		int x = getWidth() / 2 - BASE_CARD_WIDTH / 2;
		int y = getHeight() / 2 - BASE_CARD_HEIGHT / 2;

		// Offset each new card slightly so they don't stack
		x += (index - 1) * 15 % getWidth();
		y += (index - 1) * 15 % getHeight();

		// Snap
		if (showGrid) {
			x = snapToGrid(x);
			y = snapToGrid(y);
		}

		CardPlaceholder card = new CardPlaceholder(index, x, y, 0, "Vị trí " + index);
		cards.add(card);
		selectedCard = card;
		invalidate();

		if (changeListener != null) {
			changeListener.onCardCountChanged(cards.size());
		}
	}

	/**
	 * Remove a specific card
	 */
	public void removeCard(CardPlaceholder card) {
		cards.remove(card);
		// Renumber remaining cards
		for (int i = 0; i < cards.size(); i++) {
			CardPlaceholder c = cards.get(i);
			c.index = i + 1;
			if (c.label != null && c.label.startsWith("Vị trí ")) {
				c.label = "Vị trí " + (i + 1);
			}
		}
		if (selectedCard == card) {
			selectedCard = null;
		}
		invalidate();

		if (changeListener != null) {
			changeListener.onCardCountChanged(cards.size());
		}
	}

	/**
	 * Auto-arrange cards in an evenly spaced grid
	 */
	public void autoArrange() {
		int count = cards.size();
		if (count == 0) return;

		int cols;
		if (count <= 2) cols = 2;
		else if (count <= 4) cols = 2;
		else if (count <= 9) cols = 3;
		else cols = 4;

		int rows = (int) Math.ceil((double) count / cols);

		int canvasW = getWidth();
		int canvasH = getHeight();

		// Calculate spacing
		int totalCardW = cols * BASE_CARD_WIDTH;
		int totalCardH = rows * BASE_CARD_HEIGHT;
		int spacingX = (canvasW - totalCardW) / (cols + 1);
		int spacingY = (canvasH - totalCardH) / (rows + 1);

		spacingX = Math.max(spacingX, 10);
		spacingY = Math.max(spacingY, 10);

		int cardIdx = 0;
		for (int r = 0; r < rows && cardIdx < count; r++) {
			int cardsInRow = Math.min(cols, count - r * cols);
			// Center the row
			int rowWidth = cardsInRow * BASE_CARD_WIDTH + (cardsInRow - 1) * spacingX;
			int startX = (canvasW - rowWidth) / 2;

			for (int c = 0; c < cardsInRow && cardIdx < count; c++) {
				CardPlaceholder card = cards.get(cardIdx);
				card.x = startX + c * (BASE_CARD_WIDTH + spacingX);
				card.y = spacingY + r * (BASE_CARD_HEIGHT + spacingY);
				card.rotation = 0; // Reset to portrait
				cardIdx++;
			}
		}
		invalidate();
	}

	/**
	 * Set card count - adds or removes cards to match
	 */
	public void setCardCount(int count) {
		while (cards.size() < count) {
			int idx = cards.size() + 1;
			cards.add(new CardPlaceholder(idx, 0, 0, 0, "Vị trí " + idx));
		}
		while (cards.size() > count) {
			cards.remove(cards.size() - 1);
		}
		// Auto-arrange after setting count
		post(new Runnable() {
			@Override
			public void run() {
				if (getWidth() > 0 && getHeight() > 0) {
					autoArrange();
				}
			}
		});
	}

	/**
	 * Toggle grid visibility
	 */
	public void toggleGrid() {
		showGrid = !showGrid;
		invalidate();
	}

	public boolean isGridVisible() {
		return showGrid;
	}

	/**
	 * Get card count
	 */
	public int getCardCount() {
		return cards.size();
	}

	/**
	 * Get cards list
	 */
	public List<CardPlaceholder> getCards() {
		return cards;
	}

	/**
	 * Set listener for changes
	 */
	public void setOnDesignerChangeListener(OnDesignerChangeListener listener) {
		this.changeListener = listener;
	}

	/**
	 * Export positions as JSON string
	 * Format: [{"x":150,"y":50,"rotation":0,"label":"Quá khứ"}, ...]
	 */
	public String exportPositionsJSON() {
		try {
			JSONArray arr = new JSONArray();
			int canvasW = getWidth();
			int canvasH = getHeight();

			for (CardPlaceholder card : cards) {
				JSONObject obj = new JSONObject();
				// Store as fraction of canvas for device independence
				obj.put("x", canvasW > 0 ? (double) card.x / canvasW : 0);
				obj.put("y", canvasH > 0 ? (double) card.y / canvasH : 0);
				obj.put("rotation", card.rotation);
				obj.put("label", card.label);
				arr.put(obj);
			}
			return arr.toString();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Export step descriptions as JSON array string
	 */
	public String exportStepDescriptionsJSON() {
		try {
			JSONArray arr = new JSONArray();
			for (CardPlaceholder card : cards) {
				arr.put(card.label != null ? card.label : "");
			}
			return arr.toString();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Import positions from JSON string
	 */
	public void importPositionsJSON(String json) {
		if (json == null || json.isEmpty()) return;
		try {
			final JSONArray arr = new JSONArray(json);
			cards.clear();

			// Need canvas dimensions - post to ensure they're available
			post(new Runnable() {
				@Override
				public void run() {
					try {
						int canvasW = getWidth();
						int canvasH = getHeight();
						if (canvasW <= 0 || canvasH <= 0) {
							canvasW = 400;
							canvasH = 600;
						}

						for (int i = 0; i < arr.length(); i++) {
							JSONObject obj = arr.getJSONObject(i);
							int x = (int) (obj.getDouble("x") * canvasW);
							int y = (int) (obj.getDouble("y") * canvasH);
							int rotation = obj.optInt("rotation", 0);
							String label = obj.optString("label", "Vị trí " + (i + 1));
							cards.add(new CardPlaceholder(i + 1, x, y, rotation, label));
						}
						invalidate();

						if (changeListener != null) {
							changeListener.onCardCountChanged(cards.size());
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ==================== Private helpers ====================

	private CardPlaceholder findCardAt(int x, int y) {
		// Search in reverse order (top-most card first)
		for (int i = cards.size() - 1; i >= 0; i--) {
			CardPlaceholder card = cards.get(i);
			if (x >= card.x && x <= card.x + card.getWidth()
				&& y >= card.y && y <= card.y + card.getHeight()) {
				return card;
			}
		}
		return null;
	}

	private int snapToGrid(int value) {
		int remainder = value % GRID_SPACING;
		if (remainder < SNAP_THRESHOLD) {
			return value - remainder;
		} else if (GRID_SPACING - remainder < SNAP_THRESHOLD) {
			return value + (GRID_SPACING - remainder);
		}
		return value;
	}

	private void showCardOptionsDialog(final CardPlaceholder card) {
		final Context context = getContext();
		String[] options = new String[]{
			"Sửa tiêu đề vị trí",
			"Xoay (Ngang ↔ Dọc)",
			"Xóa vị trí này"
		};

		new AlertDialog.Builder(context)
			.setTitle("Vị trí " + card.index + ": " + card.label)
			.setItems(options, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
						case 0: // Edit label
							showEditLabelDialog(card);
							break;
						case 1: // Toggle rotation
							card.rotation = (card.rotation == 0) ? 180 : 0;
							invalidate();
							break;
						case 2: // Remove
							removeCard(card);
							break;
					}
				}
			})
			.show();
	}

	private void showEditLabelDialog(final CardPlaceholder card) {
		final Context context = getContext();
		final EditText input = new EditText(context);
		input.setText(card.label);
		input.setSelectAllOnFocus(true);

		new AlertDialog.Builder(context)
			.setTitle("Tiêu đề vị trí " + card.index)
			.setView(input)
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String newLabel = input.getText().toString().trim();
					if (newLabel.length() > 0) {
						card.label = newLabel;
						invalidate();
					}
				}
			})
			.setNegativeButton("Hủy", null)
			.show();
	}
}
