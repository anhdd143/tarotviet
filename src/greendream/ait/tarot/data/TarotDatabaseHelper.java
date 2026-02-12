package greendream.ait.tarot.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * SQLite Database Helper for Tarot Viet
 * Manages saved readings (spreads), saved daily cards, and personal card notes.
 * Inspired by Galaxy Tarot's Journal feature.
 */
public class TarotDatabaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "tarot_viet.db";
	private static final int DATABASE_VERSION = 3;

	/**
	 * Custom spread IDs use offset: actual spreadId = CUSTOM_SPREAD_ID_OFFSET + db _id
	 * Built-in spreads use IDs 0-9. Custom spreads use 1000+.
	 */
	public static final int CUSTOM_SPREAD_ID_OFFSET = 1000;

	// Table: saved_readings (lưu bài đọc - spread readings)
	public static final String TABLE_READINGS = "saved_readings";
	public static final String COL_ID = "_id";
	public static final String COL_SPREAD_ID = "spread_id";
	public static final String COL_TITLE = "title";
	public static final String COL_QUESTION = "question";
	public static final String COL_NOTES = "notes";
	public static final String COL_CARD_IDS = "card_ids"; // comma-separated
	public static final String COL_CARD_DIMENSIONS = "card_dimensions"; // comma-separated
	public static final String COL_READING_TYPE = "reading_type"; // 1=spread, 2=single card
	public static final String COL_CREATED_AT = "created_at";

	// Table: card_notes (ghi chú cá nhân cho từng lá bài)
	public static final String TABLE_CARD_NOTES = "card_notes";
	public static final String COL_NOTE_ID = "_id";
	public static final String COL_CARD_ID = "card_id";
	public static final String COL_NOTE_TEXT = "note_text";
	public static final String COL_NOTE_CREATED_AT = "created_at";
	public static final String COL_NOTE_UPDATED_AT = "updated_at";

	private static TarotDatabaseHelper sInstance;

	// Table: custom_spreads (kiểu trải bài tự thiết kế)
	public static final String TABLE_CUSTOM_SPREADS = "custom_spreads";
	public static final String COL_CS_ID = "_id";
	public static final String COL_CS_NAME = "name";
	public static final String COL_CS_DESCRIPTION = "description";
	public static final String COL_CS_CARD_COUNT = "card_count";
	public static final String COL_CS_STEP_DESCRIPTIONS = "step_descriptions"; // JSON array
	public static final String COL_CS_CARD_POSITIONS = "card_positions"; // JSON array of {x,y,rotation,label}
	public static final String COL_CS_CREATED_AT = "created_at";

	private static final String CREATE_TABLE_READINGS = 
		"CREATE TABLE " + TABLE_READINGS + " ("
		+ COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
		+ COL_SPREAD_ID + " INTEGER DEFAULT -1, "
		+ COL_TITLE + " TEXT, "
		+ COL_QUESTION + " TEXT, "
		+ COL_NOTES + " TEXT, "
		+ COL_CARD_IDS + " TEXT, "
		+ COL_CARD_DIMENSIONS + " TEXT, "
		+ COL_READING_TYPE + " INTEGER DEFAULT 1, "
		+ COL_CREATED_AT + " TEXT"
		+ ");";

	private static final String CREATE_TABLE_CARD_NOTES =
		"CREATE TABLE " + TABLE_CARD_NOTES + " ("
		+ COL_NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
		+ COL_CARD_ID + " INTEGER UNIQUE, "
		+ COL_NOTE_TEXT + " TEXT, "
		+ COL_NOTE_CREATED_AT + " TEXT, "
		+ COL_NOTE_UPDATED_AT + " TEXT"
		+ ");";

	private static final String CREATE_TABLE_CUSTOM_SPREADS =
		"CREATE TABLE " + TABLE_CUSTOM_SPREADS + " ("
		+ COL_CS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
		+ COL_CS_NAME + " TEXT NOT NULL, "
		+ COL_CS_DESCRIPTION + " TEXT, "
		+ COL_CS_CARD_COUNT + " INTEGER NOT NULL, "
		+ COL_CS_STEP_DESCRIPTIONS + " TEXT, "
		+ COL_CS_CARD_POSITIONS + " TEXT, "
		+ COL_CS_CREATED_AT + " TEXT"
		+ ");";


	/**
	 * Singleton pattern to prevent multiple database connections
	 */
	public static synchronized TarotDatabaseHelper getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new TarotDatabaseHelper(context.getApplicationContext());
		}
		return sInstance;
	}

	private TarotDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_READINGS);
		db.execSQL(CREATE_TABLE_CARD_NOTES);
		db.execSQL(CREATE_TABLE_CUSTOM_SPREADS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion < 2) {
			db.execSQL(CREATE_TABLE_CUSTOM_SPREADS);
		}
		if (oldVersion < 3) {
			try {
				db.execSQL("ALTER TABLE " + TABLE_CUSTOM_SPREADS 
					+ " ADD COLUMN " + COL_CS_CARD_POSITIONS + " TEXT");
			} catch (Exception e) {
				// Column may already exist
			}
		}
	}

	// ==================== READINGS ====================

	/**
	 * Save a spread reading to the journal
	 * @param spreadId the spread type id
	 * @param title user-given title
	 * @param question the question asked
	 * @param notes user notes
	 * @param cardIds array of card IDs
	 * @param cardDimensions array of card dimensions (0=upright, 1=reversed)
	 * @param readingType 1=spread, 2=single card
	 * @return the row ID of the new entry
	 */
	public long saveReading(int spreadId, String title, String question, 
			String notes, int[] cardIds, int[] cardDimensions, int readingType) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(COL_SPREAD_ID, spreadId);
		values.put(COL_TITLE, title);
		values.put(COL_QUESTION, question);
		values.put(COL_NOTES, notes);
		values.put(COL_CARD_IDS, intArrayToString(cardIds));
		values.put(COL_CARD_DIMENSIONS, intArrayToString(cardDimensions));
		values.put(COL_READING_TYPE, readingType);
		values.put(COL_CREATED_AT, getCurrentDateTime());
		return db.insert(TABLE_READINGS, null, values);
	}

	/**
	 * Update an existing reading
	 */
	public int updateReading(long id, String title, String question, String notes) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(COL_TITLE, title);
		values.put(COL_QUESTION, question);
		values.put(COL_NOTES, notes);
		return db.update(TABLE_READINGS, values, COL_ID + " = ?",
			new String[]{String.valueOf(id)});
	}

	/**
	 * Delete a reading by ID
	 */
	public int deleteReading(long id) {
		SQLiteDatabase db = getWritableDatabase();
		return db.delete(TABLE_READINGS, COL_ID + " = ?",
			new String[]{String.valueOf(id)});
	}

	/**
	 * Delete all readings
	 */
	public int deleteAllReadings() {
		SQLiteDatabase db = getWritableDatabase();
		return db.delete(TABLE_READINGS, null, null);
	}

	/**
	 * Get all saved readings, ordered by newest first
	 */
	public Cursor getAllReadings() {
		SQLiteDatabase db = getReadableDatabase();
		return db.query(TABLE_READINGS, null, null, null, null, null,
			COL_CREATED_AT + " DESC");
	}

	/**
	 * Get readings by type (1=spread, 2=single card)
	 */
	public Cursor getReadingsByType(int readingType) {
		SQLiteDatabase db = getReadableDatabase();
		return db.query(TABLE_READINGS, null, 
			COL_READING_TYPE + " = ?",
			new String[]{String.valueOf(readingType)}, 
			null, null, COL_CREATED_AT + " DESC");
	}

	/**
	 * Get a single reading by ID
	 */
	public Cursor getReading(long id) {
		SQLiteDatabase db = getReadableDatabase();
		return db.query(TABLE_READINGS, null, 
			COL_ID + " = ?",
			new String[]{String.valueOf(id)},
			null, null, null);
	}

	/**
	 * Get the count of saved readings
	 */
	public int getReadingsCount() {
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_READINGS, null);
		int count = 0;
		if (cursor.moveToFirst()) {
			count = cursor.getInt(0);
		}
		cursor.close();
		return count;
	}

	// ==================== CARD NOTES ====================

	/**
	 * Save or update a personal note for a card
	 */
	public long saveCardNote(int cardId, String noteText) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(COL_CARD_ID, cardId);
		values.put(COL_NOTE_TEXT, noteText);
		String now = getCurrentDateTime();
		
		// Check if note exists
		Cursor cursor = db.query(TABLE_CARD_NOTES, null,
			COL_CARD_ID + " = ?", new String[]{String.valueOf(cardId)},
			null, null, null);
		
		long result;
		if (cursor != null && cursor.moveToFirst()) {
			values.put(COL_NOTE_UPDATED_AT, now);
			result = db.update(TABLE_CARD_NOTES, values, 
				COL_CARD_ID + " = ?", new String[]{String.valueOf(cardId)});
		} else {
			values.put(COL_NOTE_CREATED_AT, now);
			values.put(COL_NOTE_UPDATED_AT, now);
			result = db.insert(TABLE_CARD_NOTES, null, values);
		}
		if (cursor != null) cursor.close();
		return result;
	}

	/**
	 * Get note for a specific card
	 */
	public String getCardNote(int cardId) {
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query(TABLE_CARD_NOTES, 
			new String[]{COL_NOTE_TEXT},
			COL_CARD_ID + " = ?", new String[]{String.valueOf(cardId)},
			null, null, null);
		String note = null;
		if (cursor != null && cursor.moveToFirst()) {
			note = cursor.getString(0);
		}
		if (cursor != null) cursor.close();
		return note;
	}

	/**
	 * Delete note for a specific card
	 */
	public int deleteCardNote(int cardId) {
		SQLiteDatabase db = getWritableDatabase();
		return db.delete(TABLE_CARD_NOTES, 
			COL_CARD_ID + " = ?", new String[]{String.valueOf(cardId)});
	}

	// ==================== CUSTOM SPREADS ====================

	/**
	 * Save a custom spread design
	 * @param name spread name
	 * @param description general description
	 * @param cardCount number of cards (2-15)
	 * @param stepDescriptions JSON array string of step descriptions
	 * @return the row ID
	 */
	public long saveCustomSpread(String name, String description, 
			int cardCount, String stepDescriptions) {
		return saveCustomSpread(name, description, cardCount, stepDescriptions, null);
	}

	/**
	 * Save a custom spread with card positions
	 * @param cardPositions JSON array of {x, y, rotation, label}
	 */
	public long saveCustomSpread(String name, String description, 
			int cardCount, String stepDescriptions, String cardPositions) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(COL_CS_NAME, name);
		values.put(COL_CS_DESCRIPTION, description);
		values.put(COL_CS_CARD_COUNT, cardCount);
		values.put(COL_CS_STEP_DESCRIPTIONS, stepDescriptions);
		values.put(COL_CS_CARD_POSITIONS, cardPositions);
		values.put(COL_CS_CREATED_AT, getCurrentDateTime());
		return db.insert(TABLE_CUSTOM_SPREADS, null, values);
	}

	/**
	 * Update an existing custom spread
	 */
	public int updateCustomSpread(long id, String name, String description,
			int cardCount, String stepDescriptions, String cardPositions) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(COL_CS_NAME, name);
		values.put(COL_CS_DESCRIPTION, description);
		values.put(COL_CS_CARD_COUNT, cardCount);
		values.put(COL_CS_STEP_DESCRIPTIONS, stepDescriptions);
		values.put(COL_CS_CARD_POSITIONS, cardPositions);
		return db.update(TABLE_CUSTOM_SPREADS, values,
			COL_CS_ID + " = ?", new String[]{String.valueOf(id)});
	}

	/**
	 * Get all custom spreads
	 */
	public Cursor getAllCustomSpreads() {
		SQLiteDatabase db = getReadableDatabase();
		return db.query(TABLE_CUSTOM_SPREADS, null, null, null, null, null,
			COL_CS_CREATED_AT + " ASC");
	}

	/**
	 * Get a custom spread by its DB _id
	 */
	public Cursor getCustomSpread(long id) {
		SQLiteDatabase db = getReadableDatabase();
		return db.query(TABLE_CUSTOM_SPREADS, null, 
			COL_CS_ID + " = ?", new String[]{String.valueOf(id)},
			null, null, null);
	}

	/**
	 * Get custom spread count
	 */
	public int getCustomSpreadCount() {
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_CUSTOM_SPREADS, null);
		int count = 0;
		if (cursor.moveToFirst()) {
			count = cursor.getInt(0);
		}
		cursor.close();
		return count;
	}

	/**
	 * Delete a custom spread
	 */
	public int deleteCustomSpread(long id) {
		SQLiteDatabase db = getWritableDatabase();
		return db.delete(TABLE_CUSTOM_SPREADS, COL_CS_ID + " = ?",
			new String[]{String.valueOf(id)});
	}

	/**
	 * Get custom spread name by composite spreadId (1000+_id)
	 */
	public String getCustomSpreadName(int spreadId) {
		long dbId = spreadId - CUSTOM_SPREAD_ID_OFFSET;
		Cursor cursor = getCustomSpread(dbId);
		String name = null;
		if (cursor != null && cursor.moveToFirst()) {
			name = cursor.getString(cursor.getColumnIndex(COL_CS_NAME));
		}
		if (cursor != null) cursor.close();
		return name;
	}

	/**
	 * Get custom spread card count by composite spreadId (1000+_id)
	 */
	public int getCustomSpreadCardCount(int spreadId) {
		long dbId = spreadId - CUSTOM_SPREAD_ID_OFFSET;
		Cursor cursor = getCustomSpread(dbId);
		int count = 0;
		if (cursor != null && cursor.moveToFirst()) {
			count = cursor.getInt(cursor.getColumnIndex(COL_CS_CARD_COUNT));
		}
		if (cursor != null) cursor.close();
		return count;
	}

	/**
	 * Get step descriptions for a custom spread by composite spreadId
	 * @return JSON array string of step descriptions
	 */
	public String getCustomSpreadStepDescriptions(int spreadId) {
		long dbId = spreadId - CUSTOM_SPREAD_ID_OFFSET;
		Cursor cursor = getCustomSpread(dbId);
		String steps = null;
		if (cursor != null && cursor.moveToFirst()) {
			steps = cursor.getString(cursor.getColumnIndex(COL_CS_STEP_DESCRIPTIONS));
		}
		if (cursor != null) cursor.close();
		return steps;
	}

	/**
	 * Get custom spread description by composite spreadId
	 */
	public String getCustomSpreadDescription(int spreadId) {
		long dbId = spreadId - CUSTOM_SPREAD_ID_OFFSET;
		Cursor cursor = getCustomSpread(dbId);
		String desc = null;
		if (cursor != null && cursor.moveToFirst()) {
			desc = cursor.getString(cursor.getColumnIndex(COL_CS_DESCRIPTION));
		}
		if (cursor != null) cursor.close();
		return desc;
	}

	/**
	 * Check if a spreadId refers to a custom spread
	 */
	public static boolean isCustomSpread(int spreadId) {
		return spreadId >= CUSTOM_SPREAD_ID_OFFSET;
	}

	/**
	 * Get card positions JSON for a custom spread by composite spreadId
	 * @return JSON array string of card positions, or null
	 */
	public String getCustomSpreadPositions(int spreadId) {
		long dbId = spreadId - CUSTOM_SPREAD_ID_OFFSET;
		Cursor cursor = getCustomSpread(dbId);
		String positions = null;
		if (cursor != null && cursor.moveToFirst()) {
			int colIdx = cursor.getColumnIndex(COL_CS_CARD_POSITIONS);
			if (colIdx >= 0) {
				positions = cursor.getString(colIdx);
			}
		}
		if (cursor != null) cursor.close();
		return positions;
	}

	// ==================== UTILITIES ====================

	private String getCurrentDateTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		return sdf.format(new Date());
	}

	/**
	 * Convert int array to comma-separated string for storage
	 */
	public static String intArrayToString(int[] arr) {
		if (arr == null || arr.length == 0) return "";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < arr.length; i++) {
			if (i > 0) sb.append(",");
			sb.append(arr[i]);
		}
		return sb.toString();
	}

	/**
	 * Convert comma-separated string back to int array
	 */
	public static int[] stringToIntArray(String str) {
		if (str == null || str.isEmpty()) return new int[0];
		String[] parts = str.split(",");
		int[] result = new int[parts.length];
		for (int i = 0; i < parts.length; i++) {
			result[i] = Integer.parseInt(parts[i].trim());
		}
		return result;
	}
}
