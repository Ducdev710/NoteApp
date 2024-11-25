package com.example.noteapp_xml.listener;

import com.example.noteapp_xml.entities.Note;

public interface NoteListener {
    void onNoteClicked(Note note,int position);
}
