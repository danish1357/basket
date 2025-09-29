package timefish.com.basket.notifications;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.widget.Toast;

import timefish.com.basket.R;
import timefish.com.basket.data.NoteRepository;

public class NotificationReceiver extends BroadcastReceiver {

    public static final String ACTION_SAVE_NOTE = "timefish.com.basket.action.SAVE_NOTE";
    public static final String EXTRA_NOTIFICATION_ID = "extra_notification_id";
    public static final String KEY_NOTE_TEXT = "key_note_text";
    public static final String KEY_NOTE_TAG = "key_note_tag";
    public static final String ACTION_NOTES_UPDATED = "timefish.com.basket.NOTES_UPDATED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }

        Bundle bundle = RemoteInput.getResultsFromIntent(intent);
        if (bundle == null) {
            return;
        }

        String noteText = bundle.getString(KEY_NOTE_TEXT);
        String tag = bundle.getString(KEY_NOTE_TAG);

        if (TextUtils.isEmpty(noteText)) {
            Toast.makeText(context, R.string.quick_note_empty_message, Toast.LENGTH_SHORT).show();
            return;
        }

        NoteRepository repository = NoteRepository.getInstance(context);
        repository.insertNote(noteText, tag);

        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ACTION_NOTES_UPDATED));

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, QuickNoteService.CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_menu_edit)
                    .setContentTitle(context.getString(R.string.quick_note_saved_title))
                    .setContentText(context.getString(R.string.quick_note_saved_message))
                    .setAutoCancel(true);
            notificationManager.notify(QuickNoteNotificationIds.SAVE_CONFIRMATION_ID, builder.build());
        }
    }
}
