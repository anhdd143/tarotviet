package greendream.ait.tarot.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import greendream.ait.tarot.R;
import greendream.ait.tarot.data.ConfigData;

public class ShuffleCardLayout extends FrameLayout {

	private static final int CARD_COUNT = 25;
	private List<ImageView> cards = new ArrayList<ImageView>();
	private Random random = new Random();
	private int cardWidth, cardHeight;

	public ShuffleCardLayout(Context context) {
		super(context);
		init(context);
	}

	public ShuffleCardLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public ShuffleCardLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		// Calculate card size based on screen width
		// Use ConfigData if initialized, otherwise fallback
		int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
		int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
		
		cardWidth = screenWidth / 5;
		cardHeight = (int) (cardWidth * 1.5); // Aspect ratio ~ 2:3

		// Add cards
		for (int i = 0; i < CARD_COUNT; i++) {
			ImageView card = new ImageView(context);
			card.setImageResource(R.drawable.back); // Use card back drawable
			
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(cardWidth, cardHeight);
			card.setLayoutParams(params);
			
			// Initial random position
			randomizePosition(card, screenWidth, screenHeight);
			
			addView(card);
			cards.add(card);
		}
	}

	private void randomizePosition(ImageView card, int w, int h) {
		// Random position within bounds, centered somewhat
		int centerX = w / 2 - cardWidth / 2;
		int centerY = h / 2 - cardHeight / 2;
		
		int offsetX = random.nextInt(w / 2) - w / 4;
		int offsetY = random.nextInt(h / 3) - h / 6;
		
		card.setTranslationX(centerX + offsetX);
		card.setTranslationY(centerY + offsetY);
		card.setRotation(random.nextInt(360)); // Random rotation
	}

	public void shuffle() {
		int w = getWidth();
		int h = getHeight();
		
		for (ImageView card : cards) {
			// Animate to new random position
			float startX = card.getTranslationX();
			float startY = card.getTranslationY();
			float startRot = card.getRotation();
			
			int centerX = w / 2 - cardWidth / 2;
			int centerY = h / 2 - cardHeight / 2;
			
			int offsetX = random.nextInt(w / 2) - w / 4;
			int offsetY = random.nextInt(h / 3) - h / 6;
			
			float endX = centerX + offsetX;
			float endY = centerY + offsetY;
			float endRot = startRot + random.nextInt(180) - 90;

			// Animation logic could be improved with ObjectAnimator, 
			// but keeping it simple with View properties for now as it's efficient
			
			card.animate()
				.translationX(endX)
				.translationY(endY)
				.rotation(endRot)
				.setDuration(300 + random.nextInt(200))
				.start();
		}
	}
	
	/**
	 * Gather cards to center (before cut)
	 */
	public void gather() {
		int w = getWidth();
		int h = getHeight();
		int centerX = w / 2 - cardWidth / 2;
		int centerY = h / 2 - cardHeight / 2;
		
		for (ImageView card : cards) {
			card.animate()
				.translationX(centerX)
				.translationY(centerY)
				.rotation(0)
				.setDuration(500)
				.start();
		}
	}
}
