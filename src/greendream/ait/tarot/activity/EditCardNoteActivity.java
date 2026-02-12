package greendream.ait.tarot.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import greendream.ait.tarot.R;
import greendream.ait.tarot.data.CardsDetailJasonHelper;
import greendream.ait.tarot.data.ConfigData;
import greendream.ait.tarot.data.MapData;
import greendream.ait.tarot.data.TarotDatabaseHelper;
import greendream.ait.tarot.util.ImageCache.ImageCacheParams;
import greendream.ait.tarot.util.ImageLoaderAsynch;

public class EditCardNoteActivity extends FragmentActivity implements OnClickListener {

	public static final String EXTRA_CARD_ID = "card_id";
	private static final String IMAGE_CACHE_DIR = "card_note_images";

	private int cardId;
	private EditText etNote;
	private ImageView ivCardImage;
	private TextView tvCardName;
	private Button btnSave, btnCancel, btnDelete;
	
	private ImageLoaderAsynch mImageLoader;
	private TarotDatabaseHelper dbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_card_note);

		ConfigData.reloadScreen(this);
		dbHelper = TarotDatabaseHelper.getInstance(this);

		cardId = getIntent().getIntExtra(EXTRA_CARD_ID, -1);
		if (cardId == -1) {
			finish();
			return;
		}

		initViews();
		loadData();
	}

	private void initViews() {
		etNote = (EditText) findViewById(R.id.etNote);
		ivCardImage = (ImageView) findViewById(R.id.ivCardImage);
		tvCardName = (TextView) findViewById(R.id.tvCardName);
		btnSave = (Button) findViewById(R.id.btnSave);
		btnCancel = (Button) findViewById(R.id.btnCancel);
		btnDelete = (Button) findViewById(R.id.btnDelete);

		btnSave.setOnClickListener(this);
		btnCancel.setOnClickListener(this);
		btnDelete.setOnClickListener(this);
		
		// Image Loader setup
		ImageCacheParams cacheParams = new ImageCacheParams(this, IMAGE_CACHE_DIR);
		cacheParams.setMemCacheSizePercent(0.25f);
		mImageLoader = new ImageLoaderAsynch(this);
		mImageLoader.addImageCache(getSupportFragmentManager(), cacheParams);
	}

	private void loadData() {
		// Load card info
		String name = CardsDetailJasonHelper.getEnglishCardName(cardId);
		tvCardName.setText(name);

		// Load image
		// Assuming card images are 120xSomething or similar small size for preview
		// Helper usually needs width/height in key like "id_w_h"
		int w = 150; 
		int h = 250;
		mImageLoader.loadImage(MapData.arrCardImage_R_Id[cardId] + "_" + w + "_" + h, ivCardImage);

		// Load existing note
		String note = dbHelper.getCardNote(cardId);
		if (note != null) {
			etNote.setText(note);
			etNote.setSelection(note.length());
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.btnSave) {
			saveNote();
		} else if (id == R.id.btnCancel) {
			finish();
		} else if (id == R.id.btnDelete) {
			confirmDelete();
		}
	}

	private void saveNote() {
		String text = etNote.getText().toString().trim();
		if (text.isEmpty()) {
			// If empty, treat as delete? Or just warn?
			// Galaxy Tarot warns "You did not add any personal text"
			Toast.makeText(this, "Vui lòng nhập nội dung ghi chú.", Toast.LENGTH_SHORT).show();
			return;
		}

		dbHelper.saveCardNote(cardId, text);
		Toast.makeText(this, "Đã lưu ghi chú.", Toast.LENGTH_SHORT).show();
		setResult(RESULT_OK);
		finish();
	}

	private void confirmDelete() {
		new AlertDialog.Builder(this)
			.setTitle("Xóa ghi chú")
			.setMessage("Bạn có chắc muốn xóa ghi chú cho lá bài này không?")
			.setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dbHelper.deleteCardNote(cardId);
					Toast.makeText(EditCardNoteActivity.this, "Đã xóa ghi chú.", Toast.LENGTH_SHORT).show();
					setResult(RESULT_OK);
					finish();
				}
			})
			.setNegativeButton("Hủy", null)
			.show();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mImageLoader.setExitTasksEarly(false);
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
}
