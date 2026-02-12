package greendream.ait.tarot.activity;

import greendream.ait.tarot.R;
import greendream.ait.tarot.data.MapData;
import greendream.ait.tarot.data.ConfigData;
import greendream.ait.tarot.data.SpreadCardJasonHelper;
import greendream.ait.tarot.data.TarotDatabaseHelper;
import greendream.ait.tarot.util.Utils;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class TarotSpreadGuideActivity extends Activity implements
		OnClickListener {

	private int spreadId;
	private boolean isCustom;
	private int customCardCount;
	private Button btn_shuffle_cards;
	private Button btn_go_directly_to_spread;
	private TextView tvTarotSpreadName;
	private ImageView ivTarotSpread;
	private TextView tvTarotSpreadGuide;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tarot_spread_guide);

		// Reload screen size and background
		ConfigData.reloadScreen(this);
		

		// Load background
		((ImageView) findViewById(R.id.background)).setBackgroundDrawable(ConfigData.rbdBackground);
		
		btn_shuffle_cards = (Button) findViewById(R.id.btn_shuffle_cards);
		btn_shuffle_cards.setTypeface(ConfigData.UVNCatBien_R);
		btn_shuffle_cards.setOnClickListener(this);

		btn_go_directly_to_spread = (Button) findViewById(R.id.btn_go_directly_to_spread);
		btn_go_directly_to_spread.setTypeface(ConfigData.UVNCatBien_R);
		btn_go_directly_to_spread.setOnClickListener(this);

		tvTarotSpreadName = (TextView) findViewById(R.id.tvTarotSpreadName);
		tvTarotSpreadName.setTypeface(ConfigData.UVNCatBien_R);

		ivTarotSpread = (ImageView) findViewById(R.id.ivTarotSpread);
		ivTarotSpread.setOnClickListener(this);

		tvTarotSpreadGuide = (TextView) findViewById(R.id.tvTarotSpreadGuide);
		tvTarotSpreadGuide.setTextSize(ConfigData.FONT_SIZE);

		spreadId = getIntent().getExtras().getInt("spreadId", 0);
		isCustom = getIntent().getExtras().getBoolean("isCustom", false);

		if (isCustom || TarotDatabaseHelper.isCustomSpread(spreadId)) {
			isCustom = true;
			// Load from database
			TarotDatabaseHelper db = TarotDatabaseHelper.getInstance(this);
			String name = db.getCustomSpreadName(spreadId);
			String desc = db.getCustomSpreadDescription(spreadId);
			customCardCount = db.getCustomSpreadCardCount(spreadId);
			String steps = db.getCustomSpreadStepDescriptions(spreadId);

			tvTarotSpreadName.setText(name != null ? name : "Trải bài tùy chỉnh");
			ivTarotSpread.setBackgroundResource(R.drawable.btn_spreadcard);

			// Build guide text from description + step descriptions
			StringBuilder guideText = new StringBuilder();
			if (desc != null && !desc.isEmpty()) {
				guideText.append(desc).append("\n\n");
			}
			guideText.append("Số lá bài: ").append(customCardCount).append("\n\n");
			
			if (steps != null && !steps.isEmpty()) {
				try {
					org.json.JSONArray stepsArray = new org.json.JSONArray(steps);
					guideText.append("Các vị trí:\n");
					for (int i = 0; i < stepsArray.length(); i++) {
						guideText.append((i + 1)).append(". ").append(stepsArray.getString(i)).append("\n");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			tvTarotSpreadGuide.setText(guideText.toString());
		} else {
			// Built-in spread - original behavior
			tvTarotSpreadName.setText(SpreadCardJasonHelper.getSpreadName(spreadId));
			ivTarotSpread.setBackgroundResource(MapData.arrSpreadCardIcon_R_Id[spreadId]);
			tvTarotSpreadGuide.setText(SpreadCardJasonHelper.getSpreadInfo(spreadId));
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ivTarotSpread:
		case R.id.btn_shuffle_cards:
			Intent intentShuffleCutActivity = new Intent(this,
					ShuffleCutActivity.class);
			intentShuffleCutActivity.putExtra("spread_id", spreadId);
			if (isCustom) {
				intentShuffleCutActivity.putExtra("card_count", customCardCount);
			} else {
				intentShuffleCutActivity.putExtra("card_count",
						SpreadCardJasonHelper.getStepArray(spreadId).length);
			}
			this.startActivity(intentShuffleCutActivity);
			break;

		case R.id.btn_go_directly_to_spread:
			Intent intentSpreadCardsActivity = new Intent(this,
					SpreadCardsActivity.class);
			intentSpreadCardsActivity.putExtra("spreadId", spreadId);
			if (isCustom) {
				ConfigData.randomMultipleCards(customCardCount);
			} else {
				ConfigData.randomMultipleCards(SpreadCardJasonHelper.getStepArray(spreadId).length);
			}
			this.startActivity(intentSpreadCardsActivity);
			break;
		}
	}

	@Override
	protected void onResume() {
		// Load background
		((ImageView) findViewById(R.id.background))
				.setBackgroundDrawable(ConfigData.rbdBackground);
		super.onResume();
	}
}
