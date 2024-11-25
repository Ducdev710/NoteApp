package com.example.noteapp_xml.actitvities;

import static java.util.Locale.filter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.noteapp_xml.R;
import com.example.noteapp_xml.adpater.NoteAdapter;
import com.example.noteapp_xml.database.NoteDatabase;
import com.example.noteapp_xml.entities.Note;
import com.example.noteapp_xml.listener.NoteListener;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NoteListener {

    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    public static final int REQUEST_CODE_SHOW_NOTES = 3;
    public static final int REQUEST_CODE_SELECT_IMAGE = 4;
    private RecyclerView notesRecyclerView;
    private List<Note> noteList;
    private NoteAdapter noteAdapter;
    private int noteClickedPosition = -1;
    private AlertDialog dialogAddURL;
    private DrawerLayout drawerLayout;
    private boolean isToggleOn = false;
    private boolean isDarkMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*SharedPreferences sharedPreferences = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        isDarkMode = sharedPreferences.getBoolean("isDarkMode", false);
        setAppTheme(isDarkMode);*/

        setContentView(R.layout.activity_main);
        ImageView imageAddNoteMain = findViewById(R.id.imageAddNoteMain);
        imageAddNoteMain.setOnClickListener(view ->
                startActivityForResult(
                new Intent(getApplicationContext(), CreateNoteActivity.class),
                REQUEST_CODE_ADD_NOTE));
        notesRecyclerView = findViewById(R.id.notesRecyclerView);
        notesRecyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        noteList = new ArrayList<>();
        noteAdapter = new NoteAdapter(noteList, this);
        notesRecyclerView.setAdapter(noteAdapter);
        notesRecyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        /*registerForContextMenu(notesRecyclerView);*/
        getNotes(REQUEST_CODE_SHOW_NOTES, false);
        EditText inputSearch = findViewById(R.id.inputsearch);
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after2) {
                noteAdapter.cancelTimer();
            }
            @Override
            public void afterTextChanged(Editable editable) {
                if (!noteList.isEmpty()) {
                    noteAdapter.searchNotes(editable.toString());
                }
            }
        });
        findViewById(R.id.imageAddImage).setOnClickListener(view -> {
            selectImage();
        });
        findViewById(R.id.imageAddWebLink).setOnClickListener(view -> {
            showAddURLDialog();
        });
        TextView textNoteCount = findViewById(R.id.textNoteCount);
        updateNoteCount(textNoteCount);
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_about_us) {
                Intent intent = new Intent(MainActivity.this, AboutUsActitity.class);
                startActivity(intent);
                drawerLayout.closeDrawers();
                return true;
            /*}else if (id == R.id.nav_color_changer) {
                isToggleOn = !isToggleOn;
                item.setIcon(isToggleOn ? R.drawable.baseline_toggle_on_24 : R.drawable.baseline_toggle_off_24);
                return true;*/
            }else if (id == R.id.nav_delete_list) {
                Intent intent = new Intent(MainActivity.this, DeletedList.class);
                startActivity(intent);
                drawerLayout.closeDrawers();
                return true;
            }
            return true;
        });
    }
    /*@Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_color_changer) {
            isToggleOn = !isToggleOn;
            item.setIcon(isToggleOn ? R.drawable.baseline_toggle_on_24 : R.drawable.baseline_toggle_off_24);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }*/
    /*private void toggleTheme() {
        isDarkMode = !isDarkMode;
        setAppTheme(isDarkMode);
        SharedPreferences.Editor editor = getSharedPreferences("ThemePrefs", MODE_PRIVATE).edit();
        editor.putBoolean("isDarkMode", isDarkMode);
        editor.apply();
    }

    private void setAppTheme(boolean isDarkMode) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }*/

    public void openMenu(View view) {
        if (!drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.openDrawer(GravityCompat.END);
        } else {
            drawerLayout.closeDrawer(GravityCompat.END);
        }
    }
    private void updateNoteCount(TextView textNoteCount) {
        int noteCount = noteList.size();
        String noteText = noteCount == 1 ? "You have 1 note" : "You have " + noteCount + " notes";
        textNoteCount.setText(noteText);
    }
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
    }
    private String getPathFromUri(Uri contentUri){
        String filePath;
        Cursor cursor = getContentResolver()
                .query(contentUri, null, null, null, null);
        if(cursor == null){
            filePath = contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }
    private void getNotes(final int requestCode, final boolean isNoteDeleted) {
        @SuppressLint("StaticFieldLeak")
        class GetNotesTask extends AsyncTask<Void, Void, List<Note>> {
            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NoteDatabase.getDatabaseInstance(getApplicationContext())
                        .noteDao().getAllNotes();
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                if (requestCode == REQUEST_CODE_SHOW_NOTES) {
                    noteList.clear();
                    noteList.addAll(notes);
                    noteAdapter.notifyDataSetChanged();
                } else if (requestCode == REQUEST_CODE_ADD_NOTE) {
                    noteList.add(0, notes.get(0));
                    noteAdapter.notifyItemInserted(0);
                    notesRecyclerView.smoothScrollToPosition(0);
                } else if (requestCode == REQUEST_CODE_UPDATE_NOTE) {
                    noteList.remove(noteClickedPosition);
                    if (isNoteDeleted) {
                        noteAdapter.notifyItemRemoved(noteClickedPosition);
                    } else {
                        noteList.add(noteClickedPosition, notes.get(noteClickedPosition));
                        noteAdapter.notifyItemInserted(noteClickedPosition);
                    }
                }
                updateNoteCount(findViewById(R.id.textNoteCount));
            }
        }
        new GetNotesTask().execute();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK) {
            getNotes(REQUEST_CODE_SHOW_NOTES, false);
        }
        else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK) {
            if (data != null) {
                Note updatedNote = (Note) data.getSerializableExtra("note");
                getNotes(REQUEST_CODE_SHOW_NOTES, data.getBooleanExtra("isNoteDeleted", false));
            }
        }else if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    try {
                        String selectedImagePath = getPathFromUri(selectedImageUri);
                        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                        intent.putExtra("isFromQuickActions", true);
                        intent.putExtra("quickActionType", "image");
                        intent.putExtra("imagePath", selectedImagePath);
                        startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
                    } catch (Exception exception) {
                        Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
    private void showAddURLDialog() {
        if (dialogAddURL == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_add_url,
                    findViewById(R.id.layoutAddUrlContainer));
            builder.setView(view);
            dialogAddURL = builder.create();
            if (dialogAddURL.getWindow() != null) {
                dialogAddURL.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            final EditText inputURL = view.findViewById(R.id.inputURL);
            inputURL.requestFocus();
            view.findViewById(R.id.textAdd).setOnClickListener(view1 -> {
                if (inputURL.getText().toString().trim().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Enter URL", Toast.LENGTH_SHORT).show();
                } else if (!Patterns.WEB_URL.matcher(inputURL.getText().toString()).matches()) {
                    Toast.makeText(MainActivity.this, "Enter valid URL", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                    intent.putExtra("isFromQuickActions", true);
                    intent.putExtra("quickActionType", "URL");
                    intent.putExtra("URL", inputURL.getText().toString());
                    startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
                    dialogAddURL.dismiss();
                }
            });
            view.findViewById(R.id.textCancel).setOnClickListener(view1 -> dialogAddURL.dismiss());
        }
        dialogAddURL.show();
    }

    @Override
    public void onNoteClicked(Note note, int position) {
        noteClickedPosition = position;
        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("note", note);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.context_menu, menu);
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
        } else if (item.getItemId() == R.id.action_pin) {
            pinNoteAtPosition(position);
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }
    private void deleteNoteAtPosition(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View view = LayoutInflater.from(this).inflate(
                R.layout.layout_delete_note,
                (ViewGroup) findViewById(R.id.layoutDeleteAllNoteContainer)
        );
        builder.setView(view);
        AlertDialog dialogDeleteNote = builder.create();
        if (dialogDeleteNote.getWindow() != null) {
            dialogDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        view.findViewById(R.id.textDeleteNote).setOnClickListener(v -> {
            Note noteToDelete = noteList.get(position);
            noteToDelete.setDeleted(true);
            new UpdateNoteTask().execute(noteToDelete);
            noteList.remove(position);
            noteAdapter.notifyItemRemoved(position);
            updateNoteCount(findViewById(R.id.textNoteCount));
            dialogDeleteNote.dismiss();
        });
        view.findViewById(R.id.textCancel).setOnClickListener(v -> dialogDeleteNote.dismiss());
        dialogDeleteNote.show();
    }

    private void pinNoteAtPosition(int position) {
        Note noteToPin = noteList.get(position);
        if (noteToPin.isPinned()) { // Unpin note
            noteToPin.setPinned(false);
            noteList.remove(position);
            noteList.add(noteToPin);
            noteAdapter.notifyItemMoved(position, noteList.size() - 1);
            noteAdapter.notifyItemChanged(noteList.size() - 1);
        } else {  // Pin note
            noteToPin.setPinned(true);
            noteList.remove(position);
            noteList.add(0, noteToPin);
            noteAdapter.notifyItemMoved(position, 0);
            noteAdapter.notifyItemChanged(0);
            notesRecyclerView.smoothScrollToPosition(0);
        }
        new UpdateNoteTask().execute(noteToPin);
    }
    private class UpdateNoteTask extends AsyncTask<Note, Void, Void> {
        @Override
        protected Void doInBackground(Note... notes) {
            NoteDatabase.getDatabaseInstance(getApplicationContext()).noteDao().updateNote(notes[0]);
            return null;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        getNotes(REQUEST_CODE_SHOW_NOTES, false);
    }
    /*private class DeleteNoteTask extends AsyncTask<Note, Void, Void> {
        @Override
        protected Void doInBackground(Note... notes) {
            NoteDatabase.getDatabaseInstance(getApplicationContext()).noteDao().deleteNote(notes[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            noteList.remove(noteAdapter.getSelectedNotePosition());
            noteAdapter.notifyItemRemoved(noteAdapter.getSelectedNotePosition());
            updateNoteCount(findViewById(R.id.textNoteCount));
        }
    }*/
}