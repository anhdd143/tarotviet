package greendream.ait.tarot.activity;

import greendream.ait.tarot.R;
import greendream.ait.tarot.data.CardsDetailJasonHelper;
import greendream.ait.tarot.data.ConfigData;
import greendream.ait.tarot.data.SpreadCardJasonHelper;
import greendream.ait.tarot.data.TarotDatabaseHelper;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * JournalActivity - Nhật ký Tarot
 * Hiển thị danh sách bài đọc đã lưu (Spreads) và lá bài hàng ngày (Daily Cards).
 * Lấy cảm hứng từ Galaxy Tarot JournalActivity.
 */
public class JournalActivity extends Activity implements View.OnClickListener {

	private static final int REQUEST_EDIT = 100;

	private Button btnTabReadings;
	private Button btnTabDaily;
	private Button btnDeleteAll;
	private Button btnStartReading;
	private ListView listReadings;
	private LinearLayout emptyState;
	private TextView tvEmptyTitle;
	private TextView tvEmptyDescription;
	private TextView tvJournalTitle;

	private TarotDatabaseHelper dbHelper;
	private JournalAdapter adapter;

	// true = showing daily cards tab, false = showing spread readings tab
	private boolean showingDailyCards = false;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_journal);

		// Load background
		((ImageView) findViewById(R.id.background))
				.setBackgroundDrawable(ConfigData.rbdBackground);

		dbHelper = TarotDatabaseHelper.getInstance(this);

		// Title
		tvJournalTitle = (TextView) findViewById(R.id.tvJournalTitle);
		if (ConfigData.UVNCatBien_R != null) {
			tvJournalTitle.setTypeface(ConfigData.UVNCatBien_R);
		}

		// Tab buttons
		btnTabReadings = (Button) findViewById(R.id.btn_tab_readings);
		btnTabReadings.setOnClickListener(this);
		btnTabDaily = (Button) findViewById(R.id.btn_tab_daily);
		btnTabDaily.setOnClickListener(this);

		// Empty state
		emptyState = (LinearLayout) findViewById(R.id.emptyState);
		tvEmptyTitle = (TextView) findViewById(R.id.tvEmptyTitle);
		tvEmptyDescription = (TextView) findViewById(R.id.tvEmptyDescription);
		btnStartReading = (Button) findViewById(R.id.btn_start_reading);
		btnStartReading.setOnClickListener(this);

		// Delete all button 
		btnDeleteAll = (Button) findViewById(R.id.btn_delete_all);
		btnDeleteAll.setOnClickListener(this);

		// ListView
		listReadings = (ListView) findViewById(R.id.listReadings);
		registerForContextMenu(listReadings);

		listReadings.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				openEditReading(id);
			}
		});

		// Show readings tab by default
		showReadingsTab();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Refresh data when returning from edit
		refreshList();
		// Reload background
		((ImageView) findViewById(R.id.background))
				.setBackgroundDrawable(ConfigData.rbdBackground);
	}

	private void showReadingsTab() {
		showingDailyCards = false;
		btnTabReadings.setBackgroundColor(0xCC8B4513);
		btnTabReadings.setTextColor(0xFFFFFFFF);
		btnTabDaily.setBackgroundColor(0x66555555);
		btnTabDaily.setTextColor(0xFFCCCCCC);
		tvEmptyTitle.setText("Chưa có bài đọc nào");
		tvEmptyDescription.setText("Hãy trải bài, sau đó nhấn nút Lưu để ghi vào nhật ký.");
		btnStartReading.setText("Bắt Đầu Trải Bài");
		refreshList();
	}

	private void showDailyTab() {
		showingDailyCards = true;
		btnTabDaily.setBackgroundColor(0xCC8B4513);
		btnTabDaily.setTextColor(0xFFFFFFFF);
		btnTabReadings.setBackgroundColor(0x66555555);
		btnTabReadings.setTextColor(0xFFCCCCCC);
		tvEmptyTitle.setText("Chưa có lá bài ngày nào");
		tvEmptyDescription.setText("Hãy rút 1 lá bài và lưu lại để theo dõi lá bài mỗi ngày.");
		btnStartReading.setText("Rút Lá Bài Ngày");
		refreshList();
	}

	private void refreshList() {
		Cursor cursor;
		if (showingDailyCards) {
			cursor = dbHelper.getReadingsByType(2); // single card
		} else {
			cursor = dbHelper.getReadingsByType(1); // spread
		}

		if (cursor != null && cursor.getCount() > 0) {
			emptyState.setVisibility(View.GONE);
			listReadings.setVisibility(View.VISIBLE);
			btnDeleteAll.setVisibility(View.VISIBLE);
			adapter = new JournalAdapter(this, cursor);
			listReadings.setAdapter(adapter);
		} else {
			emptyState.setVisibility(View.VISIBLE);
			listReadings.setVisibility(View.GONE);
			btnDeleteAll.setVisibility(View.GONE);
		}
	}

	private void openEditReading(long readingId) {
		Intent intent = new Intent(this, EditSavedSpreadActivity.class);
		intent.putExtra("reading_id", readingId);
		startActivityForResult(intent, REQUEST_EDIT);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_EDIT) {
			refreshList();
		}
	}

	private void confirmDeleteAll() {
		String message = showingDailyCards
			? "Bạn có chắc muốn xóa tất cả lá bài ngày đã lưu?"
			: "Bạn có chắc muốn xóa tất cả bài đọc đã lưu?";

		new AlertDialog.Builder(this)
			.setTitle("Xác nhận")
			.setMessage(message)
			.setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dbHelper.deleteAllReadings();
					refreshList();
				}
			})
			.setNegativeButton("Hủy", null)
			.create()
			.show();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_tab_readings:
			showReadingsTab();
			break;
		case R.id.btn_tab_daily:
			showDailyTab();
			break;
		case R.id.btn_start_reading:
			if (showingDailyCards) {
				// Go to draw single card
				Intent intentDraw = new Intent(this, ShuffleCutActivity.class);
				intentDraw.putExtra("cardSelectInNeed", 1);
				startActivity(intentDraw);
			} else {
				// Go to spreads
				Intent intentSpread = new Intent(this, TarotSpreadActivity.class);
				startActivity(intentSpread);
			}
			finish();
			break;
		case R.id.btn_delete_all:
			confirmDeleteAll();
			break;
		}
	}

	// ==================== Context Menu ====================

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, 1, 0, "Xem / Chỉnh sửa");
		menu.add(0, 2, 0, "Xóa");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = 
			(AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case 1: // View/Edit
			openEditReading(info.id);
			return true;
		case 2: // Delete
			confirmDeleteSingle(info.id);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	private void confirmDeleteSingle(final long readingId) {
		new AlertDialog.Builder(this)
			.setTitle("Xác nhận")
			.setMessage("Bạn có chắc muốn xóa bài đọc này?")
			.setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dbHelper.deleteReading(readingId);
					refreshList();
				}
			})
			.setNegativeButton("Hủy", null)
			.create()
			.show();
	}

	// ==================== Adapter ====================

	private class JournalAdapter extends BaseAdapter {
		private Context context;
		private Cursor cursor;

		public JournalAdapter(Context context, Cursor cursor) {
			this.context = context;
			this.cursor = cursor;
		}

		@Override
		public int getCount() {
			return cursor != null ? cursor.getCount() : 0;
		}

		@Override
		public Object getItem(int position) {
			if (cursor != null && cursor.moveToPosition(position)) {
				return cursor;
			}
			return null;
		}

		@Override
		public long getItemId(int position) {
			if (cursor != null && cursor.moveToPosition(position)) {
				return cursor.getLong(cursor.getColumnIndex(TarotDatabaseHelper.COL_ID));
			}
			return -1;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater inflater = LayoutInflater.from(context);
				convertView = inflater.inflate(R.layout.item_journal_reading, parent, false);
			}

			if (cursor != null && cursor.moveToPosition(position)) {
				TextView tvTitle = (TextView) convertView.findViewById(R.id.tvReadingTitle);
				TextView tvDate = (TextView) convertView.findViewById(R.id.tvReadingDate);
				TextView tvType = (TextView) convertView.findViewById(R.id.tvReadingType);
				TextView tvQuestion = (TextView) convertView.findViewById(R.id.tvReadingQuestion);
				TextView tvCardCount = (TextView) convertView.findViewById(R.id.tvCardCount);

				// Title
				String title = cursor.getString(cursor.getColumnIndex(TarotDatabaseHelper.COL_TITLE));
				if (title == null || title.isEmpty()) {
					title = "Bài đọc không có tiêu đề";
				}
				tvTitle.setText(title);

				// Date
				String date = cursor.getString(cursor.getColumnIndex(TarotDatabaseHelper.COL_CREATED_AT));
				if (date != null && date.length() >= 10) {
					// Format: yyyy-MM-dd -> dd/MM/yyyy
					tvDate.setText(date.substring(8, 10) + "/" + date.substring(5, 7) + "/" + date.substring(0, 4));
				} else {
					tvDate.setText("");
				}

				// Spread type
				int spreadId = cursor.getInt(cursor.getColumnIndex(TarotDatabaseHelper.COL_SPREAD_ID));
				int readingType = cursor.getInt(cursor.getColumnIndex(TarotDatabaseHelper.COL_READING_TYPE));
				if (readingType == 2) {
					tvType.setText("🃏 Lá bài ngày");
				} else {
					String spreadName = SpreadCardJasonHelper.getSpreadName(spreadId);
					tvType.setText("🔮 " + (spreadName != null ? spreadName : "Trải bài #" + spreadId));
				}

				// Question
				String question = cursor.getString(cursor.getColumnIndex(TarotDatabaseHelper.COL_QUESTION));
				if (question != null && !question.isEmpty()) {
					tvQuestion.setVisibility(View.VISIBLE);
					tvQuestion.setText("❓ " + question);
				} else {
					tvQuestion.setVisibility(View.GONE);
				}

				// Card count
				String cardIds = cursor.getString(cursor.getColumnIndex(TarotDatabaseHelper.COL_CARD_IDS));
				if (cardIds != null && !cardIds.isEmpty()) {
					int count = cardIds.split(",").length;
					tvCardCount.setText("🃏 " + count + " lá bài");
				} else {
					tvCardCount.setText("");
				}
			}

			return convertView;
		}
	}
}
