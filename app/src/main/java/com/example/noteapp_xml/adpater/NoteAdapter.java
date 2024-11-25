package com.example.noteapp_xml.adpater;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.noteapp_xml.R;
import com.example.noteapp_xml.entities.Note;
import com.example.noteapp_xml.listener.NoteListener;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.LogRecord;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private List<Note> notes;
    private NoteListener noteListener;
    private Timer timer;
    private List<Note> noteSource;
    public NoteAdapter(List<Note> notes, NoteListener noteListener) {
        this.notes = notes;
        this.noteListener = noteListener;
        noteSource = notes;
    }
    private int selectedNotePosition = -1;

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_container_note,
                        parent,
                        false
                ));
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        holder.setNote(notes.get(position));
        holder.layoutNote.setOnClickListener(view -> {
            noteListener.onNoteClicked(notes.get(position), position);
        });
        holder.layoutNote.setOnLongClickListener(view -> {
            selectedNotePosition = holder.getAdapterPosition();
            view.showContextMenu();
            return true;
        });

    }
    public int getSelectedNotePosition() {
        return selectedNotePosition;
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }
    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle, textSubtitle, textDateTime;
        LinearLayout layoutNote;
        RoundedImageView imageNote;
        ImageView imagePinned;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            textSubtitle = itemView.findViewById(R.id.textSubtitle);
            textDateTime = itemView.findViewById(R.id.textDateTime);
            layoutNote = itemView.findViewById(R.id.layoutNote);
            imageNote = itemView.findViewById(R.id.imageNote);
            imagePinned = itemView.findViewById(R.id.imagePin);

            itemView.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                MenuInflater inflater = ((Activity) itemView.getContext()).getMenuInflater();
                if (itemView.getContext() instanceof com.example.noteapp_xml.actitvities.DeletedList) {
                    inflater.inflate(R.menu.context_menu_deleted_list, menu);
                } else {
                    inflater.inflate(R.menu.context_menu, menu);
                }
            });
        }

        void setNote(Note note) {
            textTitle.setText(note.getTitle());
            if (note.getSubtitle().trim().isEmpty()) {
                textSubtitle.setVisibility(View.GONE);
            } else {
                textSubtitle.setText(note.getSubtitle());
            }
            textDateTime.setText(note.getDateTime());

            GradientDrawable gradientDrawable = (GradientDrawable) layoutNote.getBackground();
            if (note.getColor() != null) {
                gradientDrawable.setColor(Color.parseColor(note.getColor()));
            } else {
                gradientDrawable.setColor(Color.parseColor("#333333"));
            }
            if (note.getImagePath() != null) {
                /*imageNote.setImageBitmap(BitmapFactory.decodeFile(note.getImagePath()));*/
                Glide.with(itemView.getContext())
                        .load(note.getImagePath())
                        .into(imageNote);
                imageNote.setVisibility(View.VISIBLE);
            } else {
                imageNote.setVisibility(View.GONE);
            }
            imagePinned.setVisibility(note.isPinned() ? View.VISIBLE : View.GONE);
        }
    }
    public void searchNotes(String searchQuery) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (searchQuery.trim().isEmpty()) {
                    notes = noteSource;
                } else {
                    ArrayList<Note> temp = new ArrayList<>();
                    for (Note note : noteSource) {
                        if (note.getTitle().toLowerCase().contains(searchQuery.toLowerCase())
                                || note.getSubtitle().toLowerCase().contains(searchQuery.toLowerCase())
                                || note.getNoteText().toLowerCase().contains(searchQuery.toLowerCase())) {
                            temp.add(note);
                        }
                    }
                    notes = temp;
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        }, 500);
    }
    public void cancelTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }
}
