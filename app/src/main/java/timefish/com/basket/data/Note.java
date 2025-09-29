package timefish.com.basket.data;

import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Represents a note entered from the quick notification or in-app UI.
 */
public class Note {

    private final long id;
    private final String text;
    private final String tag;
    private final long createdAt;

    public Note(long id, String text, String tag, long createdAt) {
        this.id = id;
        this.text = text;
        this.tag = tag;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getTag() {
        return tag;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getDisplayTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return formatter.format(new Date(createdAt));
    }

    public String getDisplayTag() {
        if (TextUtils.isEmpty(tag)) {
            return "Untagged";
        }
        return tag;
    }
}
