package greendream.ait.tarot.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import greendream.ait.tarot.R;
import greendream.ait.tarot.data.ConfigData;

public class CutCardLayout extends LinearLayout {

	private OnCutListener listener;
	private ImageView pile1, pile2, pile3;
	private int cardWidth, cardHeight;

	public interface OnCutListener {
		void onCut();
	}

	public CutCardLayout(Context context) {
		super(context);
		init(context);
	}

	public CutCardLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context) {
		setOrientation(LinearLayout.HORIZONTAL);
		setGravity(Gravity.CENTER);
		
		int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
		cardWidth = screenWidth / 4; // Smaller piles
		cardHeight = (int) (cardWidth * 1.5);
		
		pile1 = createPile(context, 1);
		pile2 = createPile(context, 2);
		pile3 = createPile(context, 3);
		
		addView(pile1);
		addView(pile2);
		addView(pile3);
	}
	
	private ImageView createPile(Context context, final int id) {
		ImageView pile = new ImageView(context);
		pile.setImageResource(R.drawable.back); // Use card back
		// Add padding or stacked look if possible
		
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(cardWidth, cardHeight);
		params.setMargins(10, 0, 10, 0);
		pile.setLayoutParams(params);
		
		pile.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleCut((ImageView) v);
			}
		});
		
		return pile;
	}
	
	private void handleCut(final ImageView selectedPile) {
		// Animate scale up
		ScaleAnimation scale = new ScaleAnimation(1.0f, 1.2f, 1.0f, 1.2f, 
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		scale.setDuration(300);
		scale.setFillAfter(true);
		scale.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {}
			@Override
			public void onAnimationRepeat(Animation animation) {}
			@Override
			public void onAnimationEnd(Animation animation) {
				if (listener != null) {
					listener.onCut();
				}
			}
		});
		
		selectedPile.startAnimation(scale);
		
		// Hide others or fade out? Keep simple for now
		if (selectedPile != pile1) pile1.setAlpha(0.5f);
		if (selectedPile != pile2) pile2.setAlpha(0.5f);
		if (selectedPile != pile3) pile3.setAlpha(0.5f);
	}

	public void setOnCutListener(OnCutListener listener) {
		this.listener = listener;
	}
	
	public void reset() {
		pile1.clearAnimation();
		pile2.clearAnimation();
		pile3.clearAnimation();
		pile1.setAlpha(1.0f);
		pile2.setAlpha(1.0f);
		pile3.setAlpha(1.0f);
	}
}
