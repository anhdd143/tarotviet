package greendream.ait.tarot.activity;

import greendream.ait.tarot.R;
import greendream.ait.tarot.data.CardsDetailJasonHelper;
import greendream.ait.tarot.data.SpreadCardJasonHelper;
import greendream.ait.tarot.data.TarotDatabaseHelper;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * EditSavedSpreadActivity - Chỉnh sửa bài đọc đã lưu
 * Cho phép người dùng xem và chỉnh sửa tiêu đề, câu hỏi, ghi chú cho bài đọc.
 * Lấy cảm hứng từ Galaxy Tarot EditSavedSpreadActivity.
 */
public class EditSavedSpreadActivity extends Activity implements View.OnClickListener {

	private long readingId = -1;
	private EditText etTitle;
	private EditText etQuestion;
	private EditText etNotes;
	private TextView tvReadingInfo;
	private Button btnSave;
	private Button btnCancel;
	private Button btnDelete;

	private TarotDatabaseHelper dbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_saved_spread);

		dbHelper = TarotDatabaseHelper.getInstance(this);

		// Get reading ID from intent
		readingId = getIntent().getLongExtra("reading_id", -1);

		// Find views
		tvReadingInfo = (TextView) findViewById(R.id.tvReadingInfo);
		etTitle = (EditText) findViewById(R.id.etTitle);
		etQuestion = (EditText) findViewById(R.id.etQuestion);
		etNotes = (EditText) findViewById(R.id.etNotes);
		btnSave = (Button) findViewById(R.id.btn_save);
		btnCancel = (Button) findViewById(R.id.btn_cancel);
		btnDelete = (Button) findViewById(R.id.btn_delete);

		btnSave.setOnClickListener(this);
		btnCancel.setOnClickListener(this);
		btnDelete.setOnClickListener(this);

		if (readingId == -1) {
			Toast.makeText(this, "Không tìm thấy bài đọc", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		loadReading();
	}

	private void loadReading() {
		Cursor cursor = dbHelper.getReading(readingId);
		if (cursor != null && cursor.moveToFirst()) {
			// Fill edit fields
			String title = cursor.getString(cursor.getColumnIndex(TarotDatabaseHelper.COL_TITLE));
			String question = cursor.getString(cursor.getColumnIndex(TarotDatabaseHelper.COL_QUESTION));
			String notes = cursor.getString(cursor.getColumnIndex(TarotDatabaseHelper.COL_NOTES));
			int spreadId = cursor.getInt(cursor.getColumnIndex(TarotDatabaseHelper.COL_SPREAD_ID));
			int readingType = cursor.getInt(cursor.getColumnIndex(TarotDatabaseHelper.COL_READING_TYPE));
			String cardIdsStr = cursor.getString(cursor.getColumnIndex(TarotDatabaseHelper.COL_CARD_IDS));
			String createdAt = cursor.getString(cursor.getColumnIndex(TarotDatabaseHelper.COL_CREATED_AT));

			if (title != null) etTitle.setText(title);
			if (question != null) etQuestion.setText(question);
			if (notes != null) etNotes.setText(notes);

			// Build info text
			StringBuilder info = new StringBuilder();
			if (readingType == 2) {
				info.append("Loại: Rút 1 lá bài\n");
			} else {
				String spreadName = SpreadCardJasonHelper.getSpreadName(spreadId);
				info.append("Loại: " + (spreadName != null ? spreadName : "Trải bài #" + spreadId) + "\n");
			}

			// Show cards
			if (cardIdsStr != null && !cardIdsStr.isEmpty()) {
				int[] cardIds = TarotDatabaseHelper.stringToIntArray(cardIdsStr);
				info.append("Số lá bài: " + cardIds.length + "\n");
				info.append("Các lá bài: ");
				for (int i = 0; i < cardIds.length; i++) {
					if (i > 0) info.append(", ");
					String cardName = CardsDetailJasonHelper.getEnglishCardName(cardIds[i]);
					info.append(cardName != null ? cardName : "Card #" + cardIds[i]);
				}
				info.append("\n");
			}

			// Date
			if (createdAt != null) {
				info.append("Ngày: " + createdAt);
			}

			tvReadingInfo.setText(info.toString());
			cursor.close();
		} else {
			Toast.makeText(this, "Không tìm thấy bài đọc", Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	private void saveReading() {
		String title = etTitle.getText().toString().trim();
		String question = etQuestion.getText().toString().trim();
		String notes = etNotes.getText().toString().trim();

		dbHelper.updateReading(readingId, title, question, notes);
		Toast.makeText(this, "Đã lưu thay đổi", Toast.LENGTH_SHORT).show();
		setResult(RESULT_OK);
		finish();
	}

	private void confirmDelete() {
		new AlertDialog.Builder(this)
			.setTitle("Xác nhận xóa")
			.setMessage("Bạn có chắc muốn xóa bài đọc này? Hành động này không thể hoàn tác.")
			.setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dbHelper.deleteReading(readingId);
					Toast.makeText(EditSavedSpreadActivity.this, "Đã xóa bài đọc", Toast.LENGTH_SHORT).show();
					setResult(RESULT_OK);
					finish();
				}
			})
			.setNegativeButton("Hủy", null)
			.create()
			.show();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_save:
			saveReading();
			break;
		case R.id.btn_cancel:
			finish();
			break;
		case R.id.btn_delete:
			confirmDelete();
			break;
		}
	}
}
