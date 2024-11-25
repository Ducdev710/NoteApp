package com.example.noteapp_xml.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.noteapp_xml.entities.Note;

import java.util.List;

@Dao
public interface NoteDao {
    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY isPinned DESC, id DESC")
    List<Note> getAllNotes();
    @Query("SELECT * FROM notes WHERE isDeleted = 1 ORDER BY id DESC")
    List<Note> getAllDeletedNotes();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNote(Note note);
    @Delete
    void deleteNote(Note note);
    @Update
    void updateNote(Note note);
    @Query("UPDATE notes SET isDeleted = 1 WHERE id = :noteId")
    void markNoteAsDeleted(int noteId);
    @Query("DELETE FROM notes WHERE isDeleted = 1")
    void deleteAllDeletedNotes();
    @Query("UPDATE notes SET isDeleted = 0 WHERE isDeleted = 1")
    void restoreAllDeletedNotes();
}
