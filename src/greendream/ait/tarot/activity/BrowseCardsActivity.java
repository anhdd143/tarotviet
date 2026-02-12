package greendream.ait.tarot.activity;

import greendream.ait.tarot.BuildConfig;
import greendream.ait.tarot.R;
import greendream.ait.tarot.data.ConfigData;
import greendream.ait.tarot.util.ImageCache.ImageCacheParams;
import greendream.ait.tarot.util.ImageLoaderAsynch;
import greendream.ait.tarot.view.adapter.CardImageGridViewAdapter;
import greendream.ait.tarot.view.adapter.CardImageSectionListViewAdapter;
import greendream.ait.tarot.view.adapter.GroupCardImageGridViewAdapter;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;

public class BrowseCardsActivity extends FragmentActivity implements
		OnClickListener {

	public static Context mContext;
	private static final String BROWSE_CARD_IMAGE_CACHE_DIR = "browse_card_image_cache";

	public static BrowseCardsActivity instance;
	private TextView tvTitle;
	private Button btn_grid;
	private Button btn_list;
	private Button btn_associations;
	private Button btn_minus;
	private Button btn_plus;
	private android.widget.ImageButton btn_search; // Added search button

	// Fragment to show Browse mode
	GirdCardFragment mGridCardFragment;
	ListViewCardFragment mListViewCardFragment;
	GoupCardFragment mGoupCardFragment;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_browse_cards);

		// Reload screen size and background
		ConfigData.reloadScreen(this);
		
		// Load background
		((ImageView) findViewById(R.id.background))
				.setBackgroundDrawable(ConfigData.rbdBackground);

		mContext = this.getApplicationContext();

		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setTypeface(ConfigData.UVNCatBien_R);

		btn_grid = (Button) findViewById(R.id.btn_grid);
		btn_grid.setOnClickListener(this);

		btn_list = (Button) findViewById(R.id.btn_list);
		btn_list.setOnClickListener(this);

		btn_associations = (Button) findViewById(R.id.btn_associations);
		btn_associations.setOnClickListener(this);

		btn_minus = (Button) findViewById(R.id.btn_minus);
		btn_minus.setOnClickListener(this);

		btn_plus = (Button) findViewById(R.id.btn_plus);
		btn_plus.setOnClickListener(this);

		btn_search = (android.widget.ImageButton) findViewById(R.id.btn_search);
		btn_search.setOnClickListener(this);

		// Replace container by mGridCardFragment
		selectBrowserMode(ConfigData.BROWSER_MODE);

		instance = this;
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		ConfigData.IS_USER_DESTROY_BY_BACK_BUTTON = true;
		super.onBackPressed();
	}

	@Override
	protected void onStart() {
		super.onStart();

		EasyTracker.getInstance(this).activityStart(this); // Google Analyze method.

	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		EasyTracker.getInstance(this).activityStop(this); // Google Analyze method.
	}
	
	@Override
	protected void onResume() {
		// Load background
		((ImageView) findViewById(R.id.background))
				.setBackgroundDrawable(ConfigData.rbdBackground);
		super.onResume();
	}

	/**
	 * select browser view mode
	 * 
	 * @param mode
	 */
	public void selectBrowserMode(int mode) {
		ConfigData.BROWSER_MODE = mode;
		FragmentManager fragmentManager = getSupportFragmentManager();

		switch (ConfigData.BROWSER_MODE) {
		case 0: // Grid view mode
			btn_grid.setBackgroundResource(R.drawable.grid_selected);
			btn_list.setBackgroundResource(R.drawable.btn_list);
			btn_associations.setBackgroundResource(R.drawable.btn_associations);
			btn_plus.setVisibility(View.VISIBLE);
			btn_minus.setVisibility(View.VISIBLE);

			if (ConfigData.ZOOM_LEVEL == 3) {
				btn_plus.setBackgroundResource(R.drawable.plus_disable);
				btn_plus.setEnabled(false);
				btn_minus.setBackgroundResource(R.drawable.btn_minus);
				btn_minus.setEnabled(true);
			}

			if (ConfigData.ZOOM_LEVEL == 0) {
				btn_minus.setBackgroundResource(R.drawable.minus_disable);
				btn_minus.setEnabled(false);
				btn_plus.setBackgroundResource(R.drawable.btn_plus);
				btn_plus.setEnabled(true);
			}

			if (mGridCardFragment == null) {
				mGridCardFragment = new GirdCardFragment();
			}
			fragmentManager.beginTransaction()
					.replace(R.id.browse_container, mGridCardFragment).commit();
			break;
		case 1: // List view mode
			btn_grid.setBackgroundResource(R.drawable.btn_grid);
			btn_list.setBackgroundResource(R.drawable.list_selected);
			btn_associations.setBackgroundResource(R.drawable.btn_associations);
			btn_plus.setVisibility(View.INVISIBLE);
			btn_minus.setVisibility(View.INVISIBLE);
			if (mListViewCardFragment == null) {
				mListViewCardFragment = new ListViewCardFragment();
			}
			fragmentManager.beginTransaction()
					.replace(R.id.browse_container, mListViewCardFragment)
					.commit();
			break;
		case 2: // Group card mode
			btn_grid.setBackgroundResource(R.drawable.btn_grid);
			btn_list.setBackgroundResource(R.drawable.btn_list);
			btn_associations
					.setBackgroundResource(R.drawable.btn_associations_selected);
			btn_plus.setVisibility(View.INVISIBLE);
			btn_minus.setVisibility(View.INVISIBLE);
			if (mGoupCardFragment == null) {
				mGoupCardFragment = new GoupCardFragment();
			}
			fragmentManager.beginTransaction()
					.replace(R.id.browse_container, mGoupCardFragment).commit();
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.encyclopedia_menu, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_home:
			this.finish();
			break;

		case R.id.btn_grid:
			// other process below here
			selectBrowserMode(0);
			break;

		case R.id.btn_list:
			// other process below here
			selectBrowserMode(1);
			break;

		case R.id.btn_associations:
			// other process below here
			selectBrowserMode(2);
			break;

		case R.id.btn_minus:
			if (mGridCardFragment.zoomGridView('-') == false) {
				btn_minus.setBackgroundResource(R.drawable.minus_disable);
				btn_minus.setEnabled(false);
				btn_plus.setBackgroundResource(R.drawable.btn_plus);
				btn_plus.setEnabled(true);
			} else {
				btn_plus.setBackgroundResource(R.drawable.btn_plus);
				btn_plus.setEnabled(true);
				btn_minus.setBackgroundResource(R.drawable.btn_minus);
				btn_minus.setEnabled(true);
			}
			break;

		case R.id.btn_plus:
			if (mGridCardFragment.zoomGridView('+') == false) {
				btn_plus.setBackgroundResource(R.drawable.plus_disable);
				btn_plus.setEnabled(false);
				btn_minus.setBackgroundResource(R.drawable.btn_minus);
				btn_minus.setEnabled(true);
			} else {

				btn_plus.setBackgroundResource(R.drawable.btn_plus);
				btn_plus.setEnabled(true);
				btn_minus.setBackgroundResource(R.drawable.btn_minus);
				btn_minus.setEnabled(true);
			}
			break;

		case R.id.btn_search:
			showSearchDialog();
			break;
		}
	}

	private void showSearchDialog() {
		android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
		builder.setTitle("Tìm kiếm lá bài");

		final android.widget.AutoCompleteTextView input = new android.widget.AutoCompleteTextView(this);
		input.setThreshold(1);
		
		int cardCount = MapData.arrCardImage_R_Id.length;
		final String[] cardNames = new String[cardCount];
		
		for (int i = 0; i < cardCount; i++) {
			String name = greendream.ait.tarot.data.CardsDetailJasonHelper.getEnglishCardName(i);
			cardNames[i] = name != null ? name : "Card " + i;
		}

		android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, cardNames);
		input.setAdapter(adapter);
		
		input.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(android.widget.AdapterView<?> parent, View view, int position, long id) {
				String selection = (String) parent.getItemAtPosition(position);
				int foundId = -1;
				for(int i=0; i<cardNames.length; i++) {
					if(cardNames[i].equals(selection)) {
						foundId = i;
						break;
					}
				}
				
				if (foundId != -1) {
					android.content.Intent intent = new android.content.Intent(BrowseCardsActivity.this, 
						CardDetailViewPagerForBrowserCardActivity.class);
					intent.putExtra("position", foundId);
					startActivity(intent);
				}
			}
		});

		builder.setView(input);
		builder.setNegativeButton("Hủy", new android.content.DialogInterface.OnClickListener() {
			@Override
			public void onClick(android.content.DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.show();
	}

	public static class GirdCardFragment extends
			android.support.v4.app.Fragment {

		private int cell_width = 0;
		private int mNumColumns;
		private GridView mGridView;
		private CardImageGridViewAdapter mCardImageGridViewAdapter;
		public static ImageLoaderAsynch mImageLoader;
		public static GirdCardFragment mInstance;
		public static int imageWidth;
		public static int imageHeight;
		

		/**
		 * Empty constructor as per the Fragment documentation
		 */
		public GirdCardFragment() {
			mInstance = this;
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			createImageLoader();

			mCardImageGridViewAdapter = new CardImageGridViewAdapter(
					getActivity());

			super.onCreate(savedInstanceState);
		}

		private void createImageLoader() {
			//
			// TODO INIT FOR IMAGE CACHE
			ImageCacheParams cacheParams = new ImageCacheParams(getActivity(),
					BROWSE_CARD_IMAGE_CACHE_DIR);

			cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to
														// 25% of app memory

			// The ImageFetcher takes care of loading images into our ImageView
			// children asynchronously
			mImageLoader = new ImageLoaderAsynch(getActivity());
			mImageLoader.addImageCache(getActivity()
					.getSupportFragmentManager(), cacheParams);
			mImageLoader.setImageFadeIn(false);

			// Set Image size
			mInstance.calculateCellWidth();
			imageWidth = cell_width - 5;
			imageHeight = imageWidth * 1232 / 710;
		}

		/**
		 * Reclaim memory and cancel all background task
		 */
		public void restartCacheToClaimMemory() {
			// TODO Auto-generated method stub
			try {
				mImageLoader.setExitTasksEarly(true);
				mImageLoader.clearMemCache();
				mImageLoader.flushCache();
				mImageLoader.closeCache();
			} catch (Exception e) {
			}
			
			createImageLoader();
			
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			final View view = inflater.inflate(R.layout.fragment_gridcard,
					container, false);

			mGridView = (GridView) view.findViewById(R.id.gridview);
			mGridView.setAdapter(mCardImageGridViewAdapter);
			mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
				@Override
				public void onScrollStateChanged(AbsListView absListView,
						int scrollState) {
					// Pause fetcher to ensure smoother scrolling when flinging
					// if (scrollState ==
					// AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
					// mImageLoader.setPauseWork(true);
					// } else {
					// mImageLoader.setPauseWork(false);
					// }
				}

				@Override
				public void onScroll(AbsListView absListView,
						int firstVisibleItem, int visibleItemCount,
						int totalItemCount) {
				}
			});

			// This listener is used to get the final width of the GridView and
			// then calculate the
			// number of columns and the width of each column. The width of each
			// column is variable
			// as the GridView has stretchMode=columnWidth. The column width is
			// used to set the height
			// of each view so we get nice card.
			mGridView.getViewTreeObserver().addOnGlobalLayoutListener(
					new ViewTreeObserver.OnGlobalLayoutListener() {

						@Override
						public void onGlobalLayout() {
							// TODO Auto-generated method stub
							if (mCardImageGridViewAdapter.getNumColumns() == 0) {
								// compute cell_width base on zoom_level
								calculateCellWidth();

								mNumColumns = (int) Math.floor(mGridView
										.getWidth() / (cell_width + 5));

								if (mNumColumns > 0) {
									mCardImageGridViewAdapter
											.setNumColumns(mNumColumns);

									mCardImageGridViewAdapter
											.setTopBarHeight(BrowseCardsActivity.instance
													.findViewById(R.id.tvTitle)
													.getHeight());

									mCardImageGridViewAdapter
											.setBottomBarHeight(BrowseCardsActivity.instance
													.findViewById(
															R.id.bottom_bar)
													.getHeight());

									mGridView.setNumColumns(mNumColumns);
								}
							}
						}
					});

			return view;
		}

		/**
		 * Get width of grid view
		 * 
		 * @return the integer number
		 */
		public int calculateCellWidth() {
			switch (ConfigData.ZOOM_LEVEL) {
			case 0: // 8 Columns portrait
				cell_width = (ConfigData.SCREEN_WIDTH - 9 * 5) / 8;
				break;
			case 1: // 5 Columns portrait
				cell_width = (ConfigData.SCREEN_WIDTH - 6 * 5) / 5;
				break;
			case 2: // 3 Columns portrait
				cell_width = (ConfigData.SCREEN_WIDTH - 4 * 5) / 3;
				break;
			case 3: // 2 Columns portrait
				cell_width = (ConfigData.SCREEN_WIDTH - 3 * 5) / 2;
				break;
			}

			return cell_width;
		}

		/**
		 * Zoom gridView by ZoomIn: mode == '-' ZoomOut mode == '+'
		 * 
		 * @return false: if need disable button fire zoom event true: otherwise
		 */
		public boolean zoomGridView(char mode) {

			// Clear cache to reclaim memory
			restartCacheToClaimMemory();

			if (BuildConfig.DEBUG) {
				Log.w("CLEAR CACHE", "CLEAR CACHE BY ZOOM GRID BUTTON");
			}

			// Change zoom level after apply new Zoom event
			if (mode == '+') {
				if (ConfigData.ZOOM_LEVEL < 3) {
					ConfigData.ZOOM_LEVEL++;
				}
			} else { // mode == '-'
				if (ConfigData.ZOOM_LEVEL > 0) {
					ConfigData.ZOOM_LEVEL--;
				}
			}

			// Calculate and reset Image size
			cell_width = calculateCellWidth();
			imageWidth = cell_width - 5;
			imageHeight = imageWidth * 1232 / 710;

			// Calculate the number of columns in new zoom mode
			mNumColumns = (int) Math.floor(mGridView.getWidth()
					/ (cell_width + 5));

			// Update the number of Columns in a line of gridView
			mCardImageGridViewAdapter.setNumColumns(mNumColumns);
			mCardImageGridViewAdapter
					.setTopBarHeight(BrowseCardsActivity.instance.findViewById(
							R.id.tvTitle).getHeight());

			// refresh gridView
			mGridView.setNumColumns(mNumColumns);
			mGridView.setColumnWidth(cell_width);
			mGridView.refreshDrawableState();
			
			if (ConfigData.ZOOM_LEVEL <= 0) {
				return false;
			} else if (ConfigData.ZOOM_LEVEL > 0 && ConfigData.ZOOM_LEVEL < 3) {
				return true;
			} else {
				return false;
			}

		}

		@Override
		public void onResume() {
			super.onResume();
			mImageLoader.setExitTasksEarly(false);
			mCardImageGridViewAdapter.notifyDataSetChanged();
		}

		@Override
		public void onPause() {
			super.onPause();
			mImageLoader.setPauseWork(false);
			mImageLoader.setExitTasksEarly(true);
			mImageLoader.flushCache();
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
			mImageLoader.clearMemCache();
			if (ConfigData.IS_USER_DESTROY_BY_BACK_BUTTON) {
				mImageLoader.clearDiskCache();
				ConfigData.IS_USER_DESTROY_BY_BACK_BUTTON = false;
			}
			mImageLoader.closeCache();
		}

	}

	public static class ListViewCardFragment extends
			android.support.v4.app.Fragment implements OnItemClickListener {

		private ListView mListView;
		private CardImageSectionListViewAdapter mCardImageSectionListViewAdapter;
		public static ImageLoaderAsynch mImageLoader;
		public static ListViewCardFragment mInstance;

		/**
		 * Empty constructor as per the Fragment documentation
		 */
		public ListViewCardFragment() {
			mInstance = this;
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			createImageLoader();

			mCardImageSectionListViewAdapter = new CardImageSectionListViewAdapter(
					getActivity());

			super.onCreate(savedInstanceState);
		}

		private void createImageLoader() {
			// TODO INIT FOR IMAGE CACHE
			ImageCacheParams cacheParams = new ImageCacheParams(getActivity(),
					BROWSE_CARD_IMAGE_CACHE_DIR);

			cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to
														// 25% of app memory

			// The ImageFetcher takes care of loading images into our ImageView
			// children asynchronously
			mImageLoader = new ImageLoaderAsynch(getActivity());
			mImageLoader.addImageCache(getActivity()
					.getSupportFragmentManager(), cacheParams);
			mImageLoader.setImageFadeIn(false);

			// Set Image size
			mImageLoader.setImageSize(ConfigData.SCREEN_WIDTH / 6,
					ConfigData.SCREEN_WIDTH / 6 * 1232 / 710);

		}

		/**
		 * Reclaim memory and cancel all background task
		 */
		public void restartCacheToClaimMemory() {
			// TODO Auto-generated method stub
			mImageLoader.clearMemCache();
			mImageLoader.setPauseWork(false);
			mImageLoader.setExitTasksEarly(true);
			mImageLoader.flushCache();
			mImageLoader.closeCache();
			createImageLoader();
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			View view = inflater.inflate(R.layout.fragment_listcard, container,
					false);

			mListView = (ListView) view.findViewById(R.id.lvTarotSpread);
			mListView.setOnItemClickListener(this);
			mListView.setAdapter(mCardImageSectionListViewAdapter);
			mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
				@Override
				public void onScrollStateChanged(AbsListView absListView,
						int scrollState) {
					// // Pause fetcher to ensure smoother scrolling when
					// flinging
					// if (scrollState ==
					// AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
					// mImageLoader.setPauseWork(true);
					// } else {
					// mImageLoader.setPauseWork(false);
					// }
				}

				@Override
				public void onScroll(AbsListView absListView,
						int firstVisibleItem, int visibleItemCount,
						int totalItemCount) {
				}
			});

			// This listener is used to get the final width of the GridView and
			// then calculate the
			// number of columns and the width of each column. The width of each
			// column is variable
			// as the GridView has stretchMode=columnWidth. The column width is
			// used to set the height
			// of each view so we get nice card.
			mListView.getViewTreeObserver().addOnGlobalLayoutListener(
					new ViewTreeObserver.OnGlobalLayoutListener() {

						@Override
						public void onGlobalLayout() {
							// TODO Auto-generated method stub
							mCardImageSectionListViewAdapter
									.setTopBarHeight(BrowseCardsActivity.instance
											.findViewById(R.id.tvTitle)
											.getHeight());

							mCardImageSectionListViewAdapter
									.setBottomBarHeight(BrowseCardsActivity.instance
											.findViewById(R.id.bottom_bar)
											.getHeight());

						}
					});

			return view;
		}

		@Override
		public void onItemClick(AdapterView<?> arg0, View v, int position,
				long arg3) {
			mCardImageSectionListViewAdapter.showDetailViewPager(position);
		}

		@Override
		public void onResume() {
			super.onResume();
			mImageLoader.setExitTasksEarly(false);
			mCardImageSectionListViewAdapter.notifyDataSetChanged();
		}

		@Override
		public void onPause() {
			super.onPause();
			mImageLoader.setPauseWork(false);
			mImageLoader.setExitTasksEarly(true);
			mImageLoader.flushCache();
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
			mImageLoader.clearMemCache();
			if (ConfigData.IS_USER_DESTROY_BY_BACK_BUTTON) {
				mImageLoader.clearDiskCache();
				ConfigData.IS_USER_DESTROY_BY_BACK_BUTTON = false;
			}
			mImageLoader.closeCache();
		}
	}

	public static class GoupCardFragment extends
			android.support.v4.app.Fragment {

		private int cell_width = 0;
		private GridView mGridView;
		private GroupCardImageGridViewAdapter mGroupCardImageGridViewAdapter;
		public static ImageLoaderAsynch mImageLoader;
		public static GoupCardFragment mInstance;

		/**
		 * Empty constructor as per the Fragment documentation
		 */
		public GoupCardFragment() {
			mInstance = this;
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			createImageLoader();

			mGroupCardImageGridViewAdapter = new GroupCardImageGridViewAdapter(
					getActivity());

			super.onCreate(savedInstanceState);
		}

		private void createImageLoader() {
			// TODO INIT FOR IMAGE CACHE
			ImageCacheParams cacheParams = new ImageCacheParams(getActivity(),
					BROWSE_CARD_IMAGE_CACHE_DIR);

			cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to
														// 25% of app memory

			// The ImageFetcher takes care of loading images into our ImageView
			// children asynchronously
			mImageLoader = new ImageLoaderAsynch(getActivity());
			mImageLoader.addImageCache(getActivity()
					.getSupportFragmentManager(), cacheParams);
			mImageLoader.setImageFadeIn(false);
			// Set Image size
			mImageLoader.setImageSize(getCellWidth() - 5);

			mImageLoader.setLoadingImage(null);

		}

		/**
		 * Reclaim memory and cancel all background task
		 */
		public void restartCacheToClaimMemory() {
			// TODO Auto-generated method stub
			mImageLoader.clearMemCache();
			mImageLoader.setPauseWork(false);
			mImageLoader.setExitTasksEarly(true);
			mImageLoader.flushCache();
			mImageLoader.closeCache();
			createImageLoader();
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			final View view = inflater.inflate(R.layout.fragment_gridcard,
					container, false);

			mGridView = (GridView) view.findViewById(R.id.gridview);
			mGridView.setAdapter(mGroupCardImageGridViewAdapter);
			mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
				@Override
				public void onScrollStateChanged(AbsListView absListView,
						int scrollState) {
					// // Pause fetcher to ensure smoother scrolling when
					// flinging
					// if (scrollState ==
					// AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
					// mImageLoader.setPauseWork(true);
					// } else {
					// mImageLoader.setPauseWork(false);
					// }
				}

				@Override
				public void onScroll(AbsListView absListView,
						int firstVisibleItem, int visibleItemCount,
						int totalItemCount) {
				}
			});

			// This listener is used to get the final width of the GridView and
			// then calculate the
			// number of columns and the width of each column. The width of each
			// column is variable
			// as the GridView has stretchMode=columnWidth. The column width is
			// used to set the height
			// of each view so we get nice card.
			mGridView.getViewTreeObserver().addOnGlobalLayoutListener(
					new ViewTreeObserver.OnGlobalLayoutListener() {

						@Override
						public void onGlobalLayout() {
							// TODO Auto-generated method stub
							int mNumColumns = mGridView.getWidth()
									/ getCellWidth();
							mGroupCardImageGridViewAdapter
									.setNumColumns(mNumColumns);

							mGroupCardImageGridViewAdapter
									.setTopBarHeight(BrowseCardsActivity.instance
											.findViewById(R.id.tvTitle)
											.getHeight());

							mGroupCardImageGridViewAdapter
									.setBottomBarHeight(BrowseCardsActivity.instance
											.findViewById(R.id.bottom_bar)
											.getHeight());

							mGridView.setNumColumns(mNumColumns);
						}
					});

			return view;
		}

		/**
		 * Get width of grid view
		 * 
		 * @return the integer number
		 */
		public int getCellWidth() {
			cell_width = (ConfigData.SCREEN_WIDTH - 40) / 3;
			return cell_width;
		}

		@Override
		public void onResume() {
			super.onResume();
			mImageLoader.setExitTasksEarly(false);
			mGroupCardImageGridViewAdapter.notifyDataSetChanged();
		}

		@Override
		public void onPause() {
			super.onPause();
			mImageLoader.setPauseWork(false);
			mImageLoader.setExitTasksEarly(true);
			mImageLoader.flushCache();
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
			mImageLoader.clearMemCache();
			if (ConfigData.IS_USER_DESTROY_BY_BACK_BUTTON) {
				mImageLoader.clearDiskCache();
				ConfigData.IS_USER_DESTROY_BY_BACK_BUTTON = false;
			}
			mImageLoader.closeCache();

		}
	}

}
