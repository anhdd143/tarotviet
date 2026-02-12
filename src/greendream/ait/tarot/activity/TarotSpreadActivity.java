package greendream.ait.tarot.activity;

import greendream.ait.tarot.R;
import greendream.ait.tarot.data.ConfigData;
import greendream.ait.tarot.data.MapData;
import greendream.ait.tarot.data.SpreadCardJasonHelper;
import greendream.ait.tarot.data.TarotDatabaseHelper;
import greendream.ait.tarot.util.Utils;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import java.util.ArrayList;

/**
 * Shows list of spread types (built-in + custom) and the "Create custom" option.
 * Replaces original TarotSpreadActivity with extended functionality.
 */
public class TarotSpreadActivity extends Activity implements
		OnItemClickListener, OnItemLongClickListener {

	private static final int REQUEST_CREATE_SPREAD = 100;
	
	private TextView tvTarotSpreadTitle;
	private ListView lvTarotSpread;
	private CombinedSpreadAdapter adapter;

	// Data model for list items
	private static final int TYPE_BUILTIN = 0;
	private static final int TYPE_CUSTOM = 1;
	private static final int TYPE_CREATE_NEW = 2;

	private ArrayList<SpreadItem> spreadItems = new ArrayList<SpreadItem>();

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tarot_spread);

		// Reload screen size and background
		ConfigData.reloadScreen(this);

		// Load background
		((ImageView) findViewById(R.id.background)).setBackgroundDrawable(ConfigData.rbdBackground);

		tvTarotSpreadTitle = (TextView) findViewById(R.id.tvTarotSpreadTitle);
		tvTarotSpreadTitle.setTypeface(ConfigData.UVNCatBien_R);

		lvTarotSpread = (ListView) findViewById(R.id.lvTarotSpread);
		lvTarotSpread.setOnItemClickListener(this);
		lvTarotSpread.setOnItemLongClickListener(this);

		buildSpreadList();
		adapter = new CombinedSpreadAdapter();
		lvTarotSpread.setAdapter(adapter);
	}

	private void buildSpreadList() {
		spreadItems.clear();

		// 1. Built-in spreads
		int builtInCount = MapData.arrSpreadCardIcon_R_Id.length;
		for (int i = 0; i < builtInCount; i++) {
			SpreadItem item = new SpreadItem();
			item.type = TYPE_BUILTIN;
			item.spreadId = i;
			item.name = SpreadCardJasonHelper.getSpreadName(i);
			item.iconResId = MapData.arrSpreadCardIcon_R_Id[i];
			spreadItems.add(item);
		}

		// 2. Custom spreads from DB
		TarotDatabaseHelper db = TarotDatabaseHelper.getInstance(this);
		Cursor cursor = db.getAllCustomSpreads();
		if (cursor != null) {
			while (cursor.moveToNext()) {
				SpreadItem item = new SpreadItem();
				item.type = TYPE_CUSTOM;
				long dbId = cursor.getLong(cursor.getColumnIndex(TarotDatabaseHelper.COL_CS_ID));
				item.spreadId = (int)(TarotDatabaseHelper.CUSTOM_SPREAD_ID_OFFSET + dbId);
				item.dbId = dbId;
				item.name = cursor.getString(cursor.getColumnIndex(TarotDatabaseHelper.COL_CS_NAME));
				item.cardCount = cursor.getInt(cursor.getColumnIndex(TarotDatabaseHelper.COL_CS_CARD_COUNT));
				item.iconResId = -1; // no built-in icon
				spreadItems.add(item);
			}
			cursor.close();
		}

		// 3. "Create new" button at the end
		SpreadItem createItem = new SpreadItem();
		createItem.type = TYPE_CREATE_NEW;
		createItem.name = "+ Tự thiết kế trải bài";
		spreadItems.add(createItem);
	}

	@Override
	public void onItemClick(AdapterView<?> listAdapter, View arg1,
			int position, long arg3) {
		SpreadItem item = spreadItems.get(position);
		
		switch (item.type) {
		case TYPE_BUILTIN:
			Intent intent = new Intent(TarotSpreadActivity.this,
					TarotSpreadGuideActivity.class);
			intent.putExtra("spreadId", item.spreadId);
			this.startActivity(intent);
			break;

		case TYPE_CUSTOM:
			Intent customIntent = new Intent(TarotSpreadActivity.this,
					TarotSpreadGuideActivity.class);
			customIntent.putExtra("spreadId", item.spreadId);
			customIntent.putExtra("isCustom", true);
			this.startActivity(customIntent);
			break;

		case TYPE_CREATE_NEW:
			Intent createIntent = new Intent(TarotSpreadActivity.this,
					CreateCustomSpreadActivity.class);
			this.startActivityForResult(createIntent, REQUEST_CREATE_SPREAD);
			break;
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		final SpreadItem item = spreadItems.get(position);
		if (item.type != TYPE_CUSTOM) return false;

		new AlertDialog.Builder(this)
			.setTitle(item.name)
			.setItems(new String[]{"Xóa kiểu trải bài này"}, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (which == 0) {
						confirmDeleteCustomSpread(item);
					}
				}
			})
			.create().show();
		return true;
	}

	private void confirmDeleteCustomSpread(final SpreadItem item) {
		new AlertDialog.Builder(this)
			.setTitle("Xóa trải bài")
			.setMessage("Bạn có chắc muốn xóa \"" + item.name + "\"?")
			.setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					TarotDatabaseHelper db = TarotDatabaseHelper.getInstance(TarotSpreadActivity.this);
					db.deleteCustomSpread(item.dbId);
					buildSpreadList();
					adapter.notifyDataSetChanged();
					Toast.makeText(TarotSpreadActivity.this, "Đã xóa", Toast.LENGTH_SHORT).show();
				}
			})
			.setNegativeButton("Hủy", null)
			.create().show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CREATE_SPREAD && resultCode == RESULT_OK) {
			buildSpreadList();
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	protected void onResume() {
		// Load background
		((ImageView) findViewById(R.id.background))
				.setBackgroundDrawable(ConfigData.rbdBackground);
		super.onResume();
	}

	// ==================== Inner Classes ====================

	private static class SpreadItem {
		int type;        // TYPE_BUILTIN, TYPE_CUSTOM, TYPE_CREATE_NEW
		int spreadId;    // for built-in (0-9) or custom (1000+)
		long dbId;       // DB _id for custom spreads
		String name;
		int iconResId;
		int cardCount;
	}

	private class CombinedSpreadAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return spreadItems.size();
		}

		@Override
		public Object getItem(int position) {
			return spreadItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = getLayoutInflater();
			SpreadItem item = spreadItems.get(position);

			if (item.type == TYPE_CREATE_NEW) {
				// Special "create" row
				View row = convertView;
				if (row == null || row.getTag() == null || !row.getTag().equals("create")) {
					row = inflater.inflate(R.layout.row_profile_image_with_text, parent, false);
					row.setTag("create");
				}
				ImageView iv = (ImageView) row.findViewById(R.id.ivListViewItemIcon);
				iv.setBackgroundResource(R.drawable.btn_encyclopedia);
				
				TextView tv = (TextView) row.findViewById(R.id.tvListViewItemText);
				tv.setText(item.name);
				tv.setTypeface(ConfigData.UVNCatBien_R);
				tv.setTextColor(Color.parseColor("#FFD700")); // gold color
				return row;
			}

			// Built-in or Custom spread row
			View row = convertView;
			if (row == null || (row.getTag() != null && row.getTag().equals("create"))) {
				row = inflater.inflate(R.layout.row_profile_image_with_text, parent, false);
				row.setTag("spread");
			}

			ImageView iv = (ImageView) row.findViewById(R.id.ivListViewItemIcon);
			if (item.type == TYPE_BUILTIN && item.iconResId > 0) {
				iv.setBackgroundResource(item.iconResId);
			} else {
				// Custom spread - use a default icon
				iv.setBackgroundResource(R.drawable.btn_spreadcard);
			}

			TextView tv = (TextView) row.findViewById(R.id.tvListViewItemText);
			if (item.type == TYPE_CUSTOM) {
				tv.setText(item.name + " (" + item.cardCount + " lá)");
			} else {
				tv.setText(item.name);
			}
			tv.setTypeface(ConfigData.UVNCatBien_R);
			tv.setTextColor(Color.WHITE);

			return row;
		}
	}
}
