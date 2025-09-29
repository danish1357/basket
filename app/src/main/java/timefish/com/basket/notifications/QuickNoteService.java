package timefish.com.basket.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;

import timefish.com.basket.MainActivity;
import timefish.com.basket.R;
import timefish.com.basket.data.NoteRepository;

public class QuickNoteService extends Service {

    public static final String CHANNEL_ID = "quick_note_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(QuickNoteNotificationIds.FOREGROUND_ID, buildNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = buildNotification();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(QuickNoteNotificationIds.FOREGROUND_ID, notification);
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification buildNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int activityFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 23) {
            activityFlags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, activityFlags);

        RemoteInput noteInput = new RemoteInput.Builder(NotificationReceiver.KEY_NOTE_TEXT)
                .setLabel(getString(R.string.quick_note_input_hint))
                .build();

        String[] tags = NoteRepository.getInstance(this).getDefaultTags();
        RemoteInput tagInput = new RemoteInput.Builder(NotificationReceiver.KEY_NOTE_TAG)
                .setLabel(getString(R.string.quick_note_tag_hint))
                .setChoices(tags)
                .build();

        Intent saveIntent = new Intent(NotificationReceiver.ACTION_SAVE_NOTE);
        saveIntent.setPackage(getPackageName());
        int broadcastFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 23) {
            broadcastFlags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent savePendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                saveIntent,
                broadcastFlags);

        NotificationCompat.Action addAction = new NotificationCompat.Action.Builder(
                android.R.drawable.ic_menu_edit,
                getString(R.string.quick_note_add_action),
                savePendingIntent)
                .addRemoteInput(noteInput)
                .addRemoteInput(tagInput)
                .build();

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_edit)
                .setContentTitle(getString(R.string.quick_note_title))
                .setContentText(getString(R.string.quick_note_description))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .addAction(addAction)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.quick_note_channel_name),
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(getString(R.string.quick_note_channel_description));
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
