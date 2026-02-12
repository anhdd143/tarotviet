package greendream.ait.tarot.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import greendream.ait.tarot.R;
import greendream.ait.tarot.data.TarotDatabaseHelper;
import greendream.ait.tarot.view.SpreadDesignerView;

/**
 * Activity for creating custom spread designs.
 * Two-step flow:
 *   Step 1: Enter name, description, card count
 *   Step 2: Drag-drop designer canvas to position cards
 *
 * Inspired by Galaxy Tarot's spread designer.
 */
public class CreateCustomSpreadActivity extends Activity 
		implements View.OnClickListener, SpreadDesignerView.OnDesignerChangeListener {

	private ViewFlipper viewFlipper;

	// Step 1: Info
	private EditText editSpreadName;
	private EditText editSpreadDescription;
	private SeekBar seekBarCardCount;
	private TextView txtCardCount;
	private Button btnNextToDesigner;

	// Step 2: Designer
	private SpreadDesignerView spreadDesigner;
	private Button btnBackToInfo;
	private Button btnAddCard;
	private Button btnAutoArrange;
	private Button btnToggleGrid;
	private Button btnSaveDesign;
	private TextView txtDesignerTitle;
	private TextView txtDesignerCardCount;
	private TextView txtDesignerHint;

	private TarotDatabaseHelper dbHelper;
	private int currentCardCount = 3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_create_custom_spread);

		dbHelper = TarotDatabaseHelper.getInstance(this);

		initStep1Views();
		initStep2Views();
	}

	private void initStep1Views() {
		viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);

		editSpreadName = (EditText) findViewById(R.id.editSpreadName);
		editSpreadDescription = (EditText) findViewById(R.id.editSpreadDescription);
		seekBarCardCount = (SeekBar) findViewById(R.id.seekBarCardCount);
		txtCardCount = (TextView) findViewById(R.id.txtCardCount);
		btnNextToDesigner = (Button) findViewById(R.id.btnNextToDesigner);

		// SeekBar: min=1, max=14 → card count 2-15
		seekBarCardCount.setProgress(1); // Default = 3 cards (progress 1 + 2 offset)
		currentCardCount = 3;
		txtCardCount.setText(String.valueOf(currentCardCount));

		seekBarCardCount.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				currentCardCount = progress + 2; // offset: 0→2, 14→16 but max=15
				if (currentCardCount > 15) currentCardCount = 15;
				txtCardCount.setText(String.valueOf(currentCardCount));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});

		btnNextToDesigner.setOnClickListener(this);
	}

	private void initStep2Views() {
		spreadDesigner = (SpreadDesignerView) findViewById(R.id.spreadDesigner);
		btnBackToInfo = (Button) findViewById(R.id.btnBackToInfo);
		btnAddCard = (Button) findViewById(R.id.btnAddCard);
		btnAutoArrange = (Button) findViewById(R.id.btnAutoArrange);
		btnToggleGrid = (Button) findViewById(R.id.btnToggleGrid);
		btnSaveDesign = (Button) findViewById(R.id.btnSaveDesign);
		txtDesignerTitle = (TextView) findViewById(R.id.txtDesignerTitle);
		txtDesignerCardCount = (TextView) findViewById(R.id.txtDesignerCardCount);
		txtDesignerHint = (TextView) findViewById(R.id.txtDesignerHint);

		spreadDesigner.setOnDesignerChangeListener(this);

		btnBackToInfo.setOnClickListener(this);
		btnAddCard.setOnClickListener(this);
		btnAutoArrange.setOnClickListener(this);
		btnToggleGrid.setOnClickListener(this);
		btnSaveDesign.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();

		if (id == R.id.btnNextToDesigner) {
			goToDesigner();
		} else if (id == R.id.btnBackToInfo) {
			viewFlipper.setDisplayedChild(0);
		} else if (id == R.id.btnAddCard) {
			spreadDesigner.addCard();
			updateDesignerCardCount();
		} else if (id == R.id.btnAutoArrange) {
			spreadDesigner.autoArrange();
		} else if (id == R.id.btnToggleGrid) {
			spreadDesigner.toggleGrid();
			btnToggleGrid.setText(spreadDesigner.isGridVisible() ? "Lưới ✓" : "Lưới");
		} else if (id == R.id.btnSaveDesign) {
			saveSpread();
		}
	}

	private void goToDesigner() {
		String name = editSpreadName.getText().toString().trim();
		if (name.length() == 0) {
			Toast.makeText(this, "Nhập tên kiểu trải bài", Toast.LENGTH_SHORT).show();
			editSpreadName.requestFocus();
			return;
		}

		// Set card count on designer
		spreadDesigner.setCardCount(currentCardCount);

		// Update title
		txtDesignerTitle.setText(name);
		updateDesignerCardCount();

		// Show hint for first time
		if (spreadDesigner.getCardCount() > 0) {
			txtDesignerHint.setVisibility(View.GONE);
		} else {
			txtDesignerHint.setVisibility(View.VISIBLE);
		}

		viewFlipper.setDisplayedChild(1);
	}

	private void saveSpread() {
		String name = editSpreadName.getText().toString().trim();
		String desc = editSpreadDescription.getText().toString().trim();

		if (name.length() == 0) {
			Toast.makeText(this, "Nhập tên trải bài", Toast.LENGTH_SHORT).show();
			return;
		}

		int cardCount = spreadDesigner.getCardCount();
		if (cardCount < 1) {
			Toast.makeText(this, "Cần ít nhất 1 lá bài", Toast.LENGTH_SHORT).show();
			return;
		}

		String stepDescriptions = spreadDesigner.exportStepDescriptionsJSON();
		String cardPositions = spreadDesigner.exportPositionsJSON();

		long id = dbHelper.saveCustomSpread(name, desc, cardCount, stepDescriptions, cardPositions);

		if (id > 0) {
			Toast.makeText(this, "Đã lưu: " + name, Toast.LENGTH_SHORT).show();
			setResult(RESULT_OK);
			finish();
		} else {
			Toast.makeText(this, "Lỗi khi lưu", Toast.LENGTH_SHORT).show();
		}
	}

	private void updateDesignerCardCount() {
		txtDesignerCardCount.setText(spreadDesigner.getCardCount() + " lá");
	}

	// ==================== SpreadDesignerView callbacks ====================

	@Override
	public void onCardCountChanged(int count) {
		updateDesignerCardCount();
	}

	@Override
	public void onCardSelected(SpreadDesignerView.CardPlaceholder card) {
		// Could show card info in a panel - for now just update UI
	}

	@Override
	public void onBackPressed() {
		if (viewFlipper.getDisplayedChild() == 1) {
			// On designer page - go back to info
			viewFlipper.setDisplayedChild(0);
		} else {
			super.onBackPressed();
		}
	}
}
