package timefish.com.basket;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import timefish.com.basket.data.Note;
import timefish.com.basket.data.NoteRepository;
import timefish.com.basket.notifications.NotificationReceiver;
import timefish.com.basket.notifications.QuickNoteService;
import timefish.com.basket.ui.NoteListAdapter;

public class MainActivity extends AppCompatActivity {

    private static final String STATE_GROUPING = "state_grouping";
    private static final int GROUP_BY_DAY = 0;
    private static final int GROUP_BY_TAG = 1;

    private Spinner groupSelector;
    private RecyclerView noteList;
    private TextView emptyView;
    private NoteListAdapter adapter;
    private NoteRepository repository;
    private int groupingMode = GROUP_BY_DAY;

    private final BroadcastReceiver notesUpdatedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (NotificationReceiver.ACTION_NOTES_UPDATED.equals(intent.getAction())) {
                loadNotes();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        groupSelector = (Spinner) findViewById(R.id.group_selector);
        noteList = (RecyclerView) findViewById(R.id.note_list);
        emptyView = (TextView) findViewById(R.id.empty_view);

        adapter = new NoteListAdapter();
        noteList.setLayoutManager(new LinearLayoutManager(this));
        noteList.setAdapter(adapter);

        repository = NoteRepository.getInstance(this);

        if (savedInstanceState != null) {
            groupingMode = savedInstanceState.getInt(STATE_GROUPING, GROUP_BY_DAY);
        }

        groupSelector.setSelection(groupingMode);
        groupSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (groupingMode != position) {
                    groupingMode = position;
                    loadNotes();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Ignore
            }
        });

        startQuickNoteService();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                notesUpdatedReceiver,
                new IntentFilter(NotificationReceiver.ACTION_NOTES_UPDATED));
        loadNotes();
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(notesUpdatedReceiver);
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_GROUPING, groupingMode);
    }

    private void loadNotes() {
        List<Note> notes = repository.getNotes();
        Map<String, List<Note>> grouped;
        if (groupingMode == GROUP_BY_TAG) {
            grouped = repository.groupByTag(notes);
        } else {
            grouped = repository.groupByDay(notes);
        }
        adapter.setItems(grouped);
        updateEmptyView();
    }

    private void updateEmptyView() {
        if (adapter.isEmpty()) {
            noteList.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            noteList.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    private void startQuickNoteService() {
        Intent serviceIntent = new Intent(this, QuickNoteService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }
}
