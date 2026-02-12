package greendream.ait.tarot.activity;

import greendream.ait.tarot.BuildConfig;
import greendream.ait.tarot.R;
import greendream.ait.tarot.data.ConfigData;
import greendream.ait.tarot.util.ImageCache;
import greendream.ait.tarot.util.TransitionHelper;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View.OnClickListener;

import com.google.ads.*;
import com.google.analytics.tracking.android.EasyTracker;

public class HomeActivity extends Activity implements OnClickListener,
		OnTouchListener {

	private TextView tvAppName;
	private Button btn_drawcard;
	private Button btn_spreadcard;
	private Button btn_encyclopedia;
	private Button btn_profile_animation;
	private Button btn_journal;

	private AdView adView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		// Load all setting in background thread when splash show
		ConfigData.loadSettingData(this);

		// Load background
		((ImageView) findViewById(R.id.background))
				.setBackgroundDrawable(ConfigData.rbdBackground);

		// Look up the AdView as a resource and load a request.
		adView = (AdView) this.findViewById(R.id.adView);
		adView.loadAd(new AdRequest());

		tvAppName = (TextView) findViewById(R.id.tvAppName);
		tvAppName.setTypeface(ConfigData.UVNCatBien_R);

		btn_drawcard = (Button) findViewById(R.id.btn_drawcard);
		btn_drawcard.setTypeface(ConfigData.UVNCatBien_R);
		btn_drawcard.setOnClickListener(this);
		btn_drawcard.setOnTouchListener(this);

		btn_spreadcard = (Button) findViewById(R.id.btn_spreadcard);
		btn_spreadcard.setTypeface(ConfigData.UVNCatBien_R);
		btn_spreadcard.setOnClickListener(this);
		btn_spreadcard.setOnTouchListener(this);

		btn_encyclopedia = (Button) findViewById(R.id.btn_encyclopedia);
		btn_encyclopedia.setTypeface(ConfigData.UVNCatBien_R);
		btn_encyclopedia.setOnClickListener(this);
		btn_encyclopedia.setOnTouchListener(this);

		btn_profile_animation = (Button) findViewById(R.id.btn_profile_animation);
		btn_profile_animation.setTypeface(ConfigData.UVNCatBien_R);
		btn_profile_animation.setOnClickListener(this);
		btn_profile_animation.setOnTouchListener(this);

		btn_journal = (Button) findViewById(R.id.btn_journal);
		btn_journal.setOnClickListener(this);
		btn_journal.setOnTouchListener(this);
	}

	@Override
	protected void onStart() {
		// ShowWarrningMessage();

		// Show active request
		// if (SettingData.checkIsActivated(this) == false) {
		// RequestActivatedApplicationCustomDialog dialog = new
		// RequestActivatedApplicationCustomDialog(this);
		// dialog.showDialog();
		// }

		EasyTracker.getInstance(this).activityStart(this); // Google Analyze
															// method.

		super.onStart();
	}

	@Override
	protected void onResume() {
		// Load background
		((ImageView) findViewById(R.id.background))
				.setBackgroundDrawable(ConfigData.rbdBackground);

		super.onResume();
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this); // Google Analyze method.
	}

	@Override
	protected void onDestroy() {
		if (adView != null) {
			adView.destroy();
		}

		try {
			// Delete Disk cache directory
			ImageCache.deleteDiskCacheDir();
		} catch (Exception e) {
		}

		super.onDestroy();

	}

	/**
	 * Print Available of memory a run time
	 */
	public static void printAvailableMemory() {
		if (BuildConfig.DEBUG) {
			Log.w("Total memory now: ", ""
					+ (Runtime.getRuntime().totalMemory() / 1024) + "KB");
			Log.w("Max memory now: ", ""
					+ (Runtime.getRuntime().maxMemory() / 1024) + "KB");
			Log.w("Free memory now: ", ""
					+ (Runtime.getRuntime().freeMemory() / 1024) + "KB");

			Log.w("WIDTH x HEIGHT", ConfigData.SCREEN_WIDTH + " x "
					+ ConfigData.SCREEN_HEIGHT);
		}
	}

	public void ShowWarrningMessage() {
		(new AlertDialog.Builder(this))
				.setIcon(R.drawable.warning_important)
				.setTitle("Chú ý quan trọng !")
				.setMessage(
						"Đây là chương trình demo chức năng.\n\n\t1. Bạn không được phép dùng cho bất kỳ mục đích kinh tế nào khác !\n\n\t2. Vui lòng liên hệ Lê Ngọc Anh - 01238059792 để thương mại hóa ứng dụng.")
				.setPositiveButton("Đồng ý", null)
				.setNegativeButton("Không",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								HomeActivity.this.finish();
							}
						}).create().show();
	}

	@Override
	protected void onPause() {
		ConfigData.saveSettingData();
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.home_menu, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_drawcard:
			Intent intentDrawCardActivity = new Intent(this,
					ChooseCardActivity.class);
			intentDrawCardActivity.putExtra("cardSelectInNeed", 1);
			TransitionHelper.transitionTo(this, intentDrawCardActivity);
			break;

		case R.id.btn_spreadcard:
			Intent intentTarotSpreadActivity = new Intent(this,
					TarotSpreadActivity.class);
			TransitionHelper.transitionTo(this, intentTarotSpreadActivity);
			break;

		case R.id.btn_encyclopedia:
			Intent intentBrowseCards = new Intent(this,
					BrowseCardsActivity.class);
			TransitionHelper.transitionTo(this, intentBrowseCards);
			break;

		case R.id.btn_profile_animation:
			Intent intentProfileActivity = new Intent(this,
					ProfileActivity.class);
			TransitionHelper.transitionTo(this, intentProfileActivity);
			break;

		case R.id.btn_journal:
			Intent intentJournalActivity = new Intent(this,
					JournalActivity.class);
			TransitionHelper.transitionTo(this, intentJournalActivity);
			break;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_drawcard:
			if (MotionEvent.ACTION_DOWN == event.getAction()) {
				btn_drawcard.startAnimation(ConfigData.animation_button_press);
			}
			break;

		case R.id.btn_spreadcard:
			if (MotionEvent.ACTION_DOWN == event.getAction()) {
				btn_spreadcard
						.startAnimation(ConfigData.animation_button_press);
			}
			break;

		case R.id.btn_encyclopedia:
			if (MotionEvent.ACTION_DOWN == event.getAction()) {
				btn_encyclopedia
						.startAnimation(ConfigData.animation_button_press);
			}
			break;

		case R.id.btn_profile_animation:
			if (MotionEvent.ACTION_DOWN == event.getAction()) {
				btn_profile_animation
						.startAnimation(ConfigData.animation_button_press);
			}
			break;

		case R.id.btn_journal:
			if (MotionEvent.ACTION_DOWN == event.getAction()) {
				btn_journal.startAnimation(ConfigData.animation_button_press);
			}
			break;
		}
		return false;
	}

}
