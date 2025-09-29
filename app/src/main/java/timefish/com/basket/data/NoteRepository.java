package timefish.com.basket.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import timefish.com.basket.R;

public class NoteRepository {

    private static NoteRepository instance;

    private final NoteDatabaseHelper databaseHelper;
    private final String[] defaultTags;

    private NoteRepository(Context context) {
        databaseHelper = new NoteDatabaseHelper(context.getApplicationContext());
        defaultTags = context.getResources().getStringArray(R.array.note_tags);
    }

    public static synchronized NoteRepository getInstance(Context context) {
        if (instance == null) {
            instance = new NoteRepository(context);
        }
        return instance;
    }

    public long insertNote(String text, String tag) {
        if (TextUtils.isEmpty(text)) {
            return -1;
        }

        long createdAt = System.currentTimeMillis();

        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(NoteDatabaseHelper.COLUMN_TEXT, text);
        values.put(NoteDatabaseHelper.COLUMN_TAG, tag);
        values.put(NoteDatabaseHelper.COLUMN_CREATED_AT, createdAt);

        return database.insert(NoteDatabaseHelper.TABLE_NOTES, null, values);
    }

    public List<Note> getNotes() {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase database = databaseHelper.getReadableDatabase();

        Cursor cursor = database.query(
                NoteDatabaseHelper.TABLE_NOTES,
                null,
                null,
                null,
                null,
                null,
                NoteDatabaseHelper.COLUMN_CREATED_AT + " DESC");

        try {
            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(NoteDatabaseHelper.COLUMN_ID));
                    String text = cursor.getString(cursor.getColumnIndexOrThrow(NoteDatabaseHelper.COLUMN_TEXT));
                    String tag = cursor.getString(cursor.getColumnIndexOrThrow(NoteDatabaseHelper.COLUMN_TAG));
                    long createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(NoteDatabaseHelper.COLUMN_CREATED_AT));
                    notes.add(new Note(id, text, tag, createdAt));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        return notes;
    }

    public String[] getDefaultTags() {
        return Arrays.copyOf(defaultTags, defaultTags.length);
    }

    public Map<String, List<Note>> groupByTag(List<Note> notes) {
        Map<String, List<Note>> grouped = new LinkedHashMap<>();
        for (Note note : notes) {
            String tag = TextUtils.isEmpty(note.getTag()) ? "Untagged" : note.getTag();
            if (!grouped.containsKey(tag)) {
                grouped.put(tag, new ArrayList<Note>());
            }
            grouped.get(tag).add(note);
        }
        return grouped;
    }

    public Map<String, List<Note>> groupByDay(List<Note> notes) {
        Map<String, List<Note>> grouped = new LinkedHashMap<>();
        SimpleDateFormatHolder holder = SimpleDateFormatHolder.get();

        for (Note note : notes) {
            String day = holder.format(note.getCreatedAt());
            if (!grouped.containsKey(day)) {
                grouped.put(day, new ArrayList<Note>());
            }
            grouped.get(day).add(note);
        }

        return grouped;
    }

    private static class SimpleDateFormatHolder {
        private static final ThreadLocal<SimpleDateFormatHolder> HOLDER = new ThreadLocal<>();
        private final java.text.SimpleDateFormat formatter =
                new java.text.SimpleDateFormat("EEE, MMM d", java.util.Locale.getDefault());

        static SimpleDateFormatHolder get() {
            SimpleDateFormatHolder holder = HOLDER.get();
            if (holder == null) {
                holder = new SimpleDateFormatHolder();
                HOLDER.set(holder);
            }
            return holder;
        }

        String format(long millis) {
            return formatter.format(new java.util.Date(millis));
        }
    }
}
