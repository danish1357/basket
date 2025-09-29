package timefish.com.basket.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import timefish.com.basket.R;
import timefish.com.basket.data.Note;

public class NoteListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_NOTE = 1;

    private final List<ListItem> items = new ArrayList<>();

    public void setItems(Map<String, List<Note>> groupedNotes) {
        items.clear();
        if (groupedNotes != null) {
            for (Map.Entry<String, List<Note>> entry : groupedNotes.entrySet()) {
                items.add(new HeaderItem(entry.getKey()));
                for (Note note : entry.getValue()) {
                    items.add(new NoteItem(note));
                }
            }
        }
        notifyDataSetChanged();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View view = inflater.inflate(R.layout.item_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_note, parent, false);
            return new NoteViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ListItem item = items.get(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind(((HeaderItem) item).title);
        } else if (holder instanceof NoteViewHolder) {
            ((NoteViewHolder) holder).bind(((NoteItem) item).note);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private abstract static class ListItem {
        abstract int getType();
    }

    private static class HeaderItem extends ListItem {
        private final String title;

        HeaderItem(String title) {
            this.title = title;
        }

        @Override
        int getType() {
            return TYPE_HEADER;
        }
    }

    private static class NoteItem extends ListItem {
        private final Note note;

        NoteItem(Note note) {
            this.note = note;
        }

        @Override
        int getType() {
            return TYPE_NOTE;
        }
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final TextView titleView;

        HeaderViewHolder(View itemView) {
            super(itemView);
            titleView = (TextView) itemView.findViewById(R.id.header_title);
        }

        void bind(String title) {
            titleView.setText(title);
        }
    }

    private static class NoteViewHolder extends RecyclerView.ViewHolder {

        private final TextView noteText;
        private final TextView noteTag;
        private final TextView noteTime;

        NoteViewHolder(View itemView) {
            super(itemView);
            noteText = (TextView) itemView.findViewById(R.id.note_text);
            noteTag = (TextView) itemView.findViewById(R.id.note_tag);
            noteTime = (TextView) itemView.findViewById(R.id.note_time);
        }

        void bind(Note note) {
            noteText.setText(note.getText());
            noteTag.setText(note.getDisplayTag());
            noteTime.setText(note.getDisplayTime());
        }
    }
}
