package greendream.ait.tarot.activity;

import greendream.ait.tarot.BuildConfig;
import greendream.ait.tarot.R;
import greendream.ait.tarot.custom.BuyTarotCustomDialog;
import greendream.ait.tarot.custom.CardDetailForBrowserCardFragment;
import greendream.ait.tarot.data.CardsDetailJasonHelper;
import greendream.ait.tarot.data.MapData;
import greendream.ait.tarot.data.ConfigData;
import greendream.ait.tarot.util.ImageLoaderAsynch;
import greendream.ait.tarot.util.Utils;
import greendream.ait.tarot.util.ImageCache.ImageCacheParams;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class CardDetailViewPagerForBrowserCardActivity extends
		FragmentActivity implements OnClickListener, OnPageChangeListener {
	
	private static final String VIEW_PAGER_IMAGE_CACHE_DIR = "extra_image_cache";
	private ImageLoaderAsynch mImageLoader;
	private ImagePagerAdapter mAdapter;
	private ViewPager mPager;
	
	// 1 : Card info show
	// 2 : Association show
	public int visibleMode = 1;
	public boolean isShowDetail = false;

	private TextView tvCardName;
	private RelativeLayout top_bar;
	private RelativeLayout bottom_bar;
	private Button btn_home;
	private Button btn_card_detail;
	private Button btn_associations;
	private Button btn_note;
	private int position;

	@TargetApi(11)
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_card_view_pager_browser_card);

		// Reload screen size and background
		ConfigData.reloadScreen(this);
				
		/**
		 * Setup for ImageLoader
		 */
		ImageCacheParams cacheParams = new ImageCacheParams(this,
				VIEW_PAGER_IMAGE_CACHE_DIR);
		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to
													// 25% of current available
													// memory

		// The ImageLoader takes care of loading images into our ImageView
		// children asynchronously
		mImageLoader = new ImageLoaderAsynch(this);
		mImageLoader.setLoadingImage(null);
		mImageLoader.addImageCache(getSupportFragmentManager(), cacheParams);
		mImageLoader.setImageFadeIn(false);

		// Load background
		((ImageView) findViewById(R.id.background))
				.setBackgroundDrawable(ConfigData.rbdBackground);

		/**
		 *  Get component for control
		 */
		position = getIntent().getIntExtra("position", 0);

		tvCardName = (TextView) findViewById(R.id.tvCardName);
		tvCardName.setTypeface(ConfigData.UVNCatBien_R);
		tvCardName.setText(CardsDetailJasonHelper.getEnglishCardName(position));

		top_bar = (RelativeLayout) findViewById(R.id.top_bar);
		
		btn_home = (Button) findViewById(R.id.btn_home);
		btn_home.setHeight(tvCardName.getHeight());
		btn_home.setWidth(tvCardName.getHeight());
		btn_home.setOnClickListener(this);

		bottom_bar = (RelativeLayout) findViewById(R.id.bottom_bar);

		btn_card_detail = (Button) findViewById(R.id.btn_card_detail);
		btn_card_detail.setOnClickListener(this);

		btn_associations = (Button) findViewById(R.id.btn_associations);
		btn_associations.setOnClickListener(this);

		btn_note = (Button) findViewById(R.id.btn_note);
		btn_note.setOnClickListener(this);
		btn_note.setTypeface(ConfigData.UVNCatBien_R);

		
		// Set up ViewPager and backing adapter
		mAdapter = new ImagePagerAdapter(getSupportFragmentManager(),
				MapData.arrCardImage_R_Id.length);
		mPager = (ViewPager) findViewById(R.id.Pager);
		mPager.setOnPageChangeListener(this);
		mPager.setAdapter(mAdapter);
		mPager.setPageMargin((int) getResources().getDimension(
				R.dimen.thin_padding));
		mPager.setOffscreenPageLimit(1);

		// Set the current item based on the extra passed in to this activity
		if (position != -1) {
			mPager.setCurrentItem(position);
		}		
		
	}

	@Override
	public void onResume() {
		super.onResume();
		mImageLoader.setExitTasksEarly(false);
		// Load background
		((ImageView) findViewById(R.id.background))
		.setBackgroundDrawable(ConfigData.rbdBackground);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mImageLoader.setExitTasksEarly(true);
		mImageLoader.flushCache();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mImageLoader.closeCache();
	}

	/**
	 * Called by the ViewPager child fragments to load images via the one
	 * ImageLoader
	 */
	public ImageLoaderAsynch getImageLoader() {
		return mImageLoader;
	}

	/**
	 * The main adapter that backs the ViewPager. A subclass of
	 * FragmentStatePagerAdapter as there could be a large number of items in
	 * the ViewPager and we don't want to retain them all in memory at once but
	 * create/destroy them on the fly.
	 */
	private class ImagePagerAdapter extends FragmentStatePagerAdapter {
		private final int mSize;

		public ImagePagerAdapter(FragmentManager fm, int size) {
			super(fm);
			mSize = size;
		}

		@Override
		public int getCount() {
			return mSize;
		}

		@Override
		public Fragment getItem(int position) {
			return CardDetailForBrowserCardFragment.newInstance(position);
		}
	}

	public void showTopAndBottomBar() {
		// TODO Auto-generated method stub
		top_bar.setVisibility(View.VISIBLE);
		bottom_bar.setVisibility(View.VISIBLE);
	}

	public void hideTopAndBottomBar() {
		// TODO Auto-generated method stub
		top_bar.setVisibility(View.INVISIBLE);
		bottom_bar.setVisibility(View.INVISIBLE);
	}

	public void updateShowHideDetailForViewPager() {
		for (int i = 0; i < mAdapter.getCount(); i++) {
			try {
				View view = mPager.getChildAt(i);
				
				ScrollView svCardDetail = (ScrollView) view
						.findViewById(R.id.svCardDetail);
				ScrollView svCardAssociations = (ScrollView) view
						.findViewById(R.id.svCardAssociations);
				// Show ViewMode
				if (isShowDetail) {
					showTopAndBottomBar();
					if (visibleMode == 1) {
						svCardDetail.setVisibility(View.VISIBLE);
						svCardAssociations.setVisibility(View.INVISIBLE);
					}
					if (visibleMode == 2) {
						svCardDetail.setVisibility(View.INVISIBLE);
						svCardAssociations.setVisibility(View.VISIBLE);
					}
				} else {
					hideTopAndBottomBar();
					svCardDetail.setVisibility(View.INVISIBLE);
					svCardAssociations.setVisibility(View.INVISIBLE);
				}
			} catch (Exception e) {

			}
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.Pager:
			break;
		case R.id.btn_home:
			this.finish();
			ConfigData.IS_USER_DESTROY_BY_BACK_BUTTON = true;
			this.startActivity(new Intent(this, HomeActivity.class));
			break;

		case R.id.btn_card_detail:
			btn_card_detail
					.setBackgroundResource(R.drawable.btn_card_interpretation_selected);
			btn_associations.setBackgroundResource(R.drawable.btn_associations);
			visibleMode = 1;
			updateShowHideDetailForViewPager();
			// other process below here

			break;

		case R.id.btn_associations:
			btn_card_detail
					.setBackgroundResource(R.drawable.btn_card_interpretation);
			btn_associations
					.setBackgroundResource(R.drawable.btn_associations_selected);
			visibleMode = 2;
			updateShowHideDetailForViewPager();
			// other process below here

			break;

			break;

		case R.id.btn_note:
			Intent intentNote = new Intent(this, EditCardNoteActivity.class);
			intentNote.putExtra(EditCardNoteActivity.EXTRA_CARD_ID, mPager.getCurrentItem());
			startActivity(intentNote);
			break;
		}
	}

	public void btnCardAssociationClicked() {
		btn_card_detail
				.setBackgroundResource(R.drawable.btn_card_interpretation);
		btn_associations
				.setBackgroundResource(R.drawable.btn_associations_selected);
		visibleMode = 2;
		updateShowHideDetailForViewPager();
	}
	
	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int new_position, float arg1, int arg2) {
		mImageLoader.clearMemCache();
	}

	@Override
	public void onPageSelected(int arg0) {		
		tvCardName.setText(CardsDetailJasonHelper.getEnglishCardName(mPager.getCurrentItem()));
		mImageLoader.clearMemCache();
		updateShowHideDetailForViewPager();
		System.gc();
	}

}
