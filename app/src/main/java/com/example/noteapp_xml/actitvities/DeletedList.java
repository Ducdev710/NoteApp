package com.example.noteapp_xml.actitvities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.noteapp_xml.R;
import com.example.noteapp_xml.adpater.NoteAdapter;
import com.example.noteapp_xml.database.NoteDatabase;
import com.example.noteapp_xml.entities.Note;
import com.example.noteapp_xml.listener.NoteListener;

import java.util.ArrayList;
import java.util.List;

public class DeletedList extends AppCompatActivity implements NoteListener {

    private static List<Note> deletedNoteList;
    private static NoteAdapter noteAdapter;
    private TextView deleteAll;
    private TextView restoreAll;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deleted_list);
        ImageView backBtn = findViewById(R.id.backToMainFromDeletedList);
        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(DeletedList.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
        TextView editDeletedList = findViewById(R.id.editDeletedList);
        LinearLayout layoutDeleteAndRestore = findViewById(R.id.layoutDeleteAndRestore);
        editDeletedList.setOnClickListener(v -> {
            if (layoutDeleteAndRestore.getVisibility() == View.VISIBLE) {
                layoutDeleteAndRestore.setVisibility(View.GONE);
            } else {
                layoutDeleteAndRestore.setVisibility(View.VISIBLE);
            }
        });
        RecyclerView deletedNotesRecyclerView = findViewById(R.id.recyclerView);
        deletedNotesRecyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        deletedNoteList = new ArrayList<>();
        noteAdapter = new NoteAdapter(deletedNoteList, this);
        deletedNotesRecyclerView.setAdapter(noteAdapter);
        deleteAll = findViewById(R.id.deleteAll);
        deleteAll.setOnClickListener(v -> {
            deleteAllNotes();
        });
        restoreAll = findViewById(R.id.restoreAll);
        restoreAll.setOnClickListener(v -> {
            restoreAllNotes();
        });

        loadDeletedNotes();
    }
    private void loadDeletedNotes() {
        @SuppressLint("StaticFieldLeak")
        class GetDeletedNotesTask extends AsyncTask<Void, Void, List<Note>> {
            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NoteDatabase.getDatabaseInstance(getApplicationContext())
                        .noteDao().getAllDeletedNotes();
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                deletedNoteList.clear();
                deletedNoteList.addAll(notes);
                noteAdapter.notifyDataSetChanged();
            }
        }
        new GetDeletedNotesTask().execute();
    }

    @Override
    public void onNoteClicked(Note note, int position) {

    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.context_menu_deleted_list, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int position = noteAdapter.getSelectedNotePosition();
        if (position == -1) {
            return super.onContextItemSelected(item);
        }

        if (item.getItemId() == R.id.action_delete) {
            deleteNoteAtPosition(position);
            return true;
        } else if (item.getItemId() == R.id.action_restore) {
            restoreNoteAtPosition(position);
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }
    private void deleteNoteAtPosition(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(DeletedList.this);
        View view = LayoutInflater.from(this).inflate(
                R.layout.layout_delete_note,
                findViewById(R.id.layoutDeleteNoteContainer)
        );
        builder.setView(view);
        AlertDialog dialogDeleteNote = builder.create();
        if (dialogDeleteNote.getWindow() != null) {
            dialogDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        view.findViewById(R.id.textDeleteNote).setOnClickListener(v -> {
            Note noteToDelete = deletedNoteList.get(position);
            new DeleteNoteTask().execute(noteToDelete);
            deletedNoteList.remove(position);
            noteAdapter.notifyItemRemoved(position);
            dialogDeleteNote.dismiss();
        });
        view.findViewById(R.id.textCancel).setOnClickListener(v -> dialogDeleteNote.dismiss());
        dialogDeleteNote.show();
    }

    private void restoreNoteAtPosition(int position) {
        Note noteToRestore = deletedNoteList.get(position);
        noteToRestore.setDeleted(false);
        new UpdateNoteTask().execute(noteToRestore);
        deletedNoteList.remove(position);
        noteAdapter.notifyItemRemoved(position);
    }

    private static class DeleteNoteTask extends AsyncTask<Note, Void, Void> {
        @Override
        protected Void doInBackground(Note... notes) {
            NoteDatabase.getDatabaseInstance(null).noteDao().deleteNote(notes[0]);
            return null;
        }
    }

    private static class UpdateNoteTask extends AsyncTask<Note, Void, Void> {
        @Override
        protected Void doInBackground(Note... notes) {
            NoteDatabase.getDatabaseInstance(null).noteDao().updateNote(notes[0]);
            return null;
        }
    }

    private void deleteAllNotes() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DeletedList.this);
        View view = LayoutInflater.from(this).inflate(
                R.layout.layout_delete_all_notes,
                findViewById(R.id.layoutDeleteAllNoteContainer)
        );
        builder.setView(view);
        AlertDialog dialogDeleteAllNotes = builder.create();
        if (dialogDeleteAllNotes.getWindow() != null) {
            dialogDeleteAllNotes.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        view.findViewById(R.id.textDeleteAllNote).setOnClickListener(v -> {
            new DeleteAllNotesTask().execute();
            dialogDeleteAllNotes.dismiss();
        });
        view.findViewById(R.id.textAllCancel).setOnClickListener(v -> dialogDeleteAllNotes.dismiss());
        dialogDeleteAllNotes.show();
    }


    private static class DeleteAllNotesTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            NoteDatabase.getDatabaseInstance(null).noteDao().deleteAllDeletedNotes();
            return null;
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            deletedNoteList.clear();
            noteAdapter.notifyDataSetChanged();
        }
    }
    private void restoreAllNotes() {
        new RestoreAllNotesTask().execute();
    }
    private static class RestoreAllNotesTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            NoteDatabase.getDatabaseInstance(null).noteDao().restoreAllDeletedNotes();
            return null;
        }
        @SuppressLint("NotifyDataSetChanged")
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            deletedNoteList.clear();
            noteAdapter.notifyDataSetChanged();
        }
    }
}
