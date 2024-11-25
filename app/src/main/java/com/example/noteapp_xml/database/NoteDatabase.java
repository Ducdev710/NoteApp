package com.example.noteapp_xml.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;

import com.example.noteapp_xml.dao.NoteDao;
import com.example.noteapp_xml.entities.Note;

@Database(entities = {Note.class}, version = 3,exportSchema = false)
public abstract class NoteDatabase extends RoomDatabase {
    public static NoteDatabase noteDatabase;
    private static final Migration MIGRATION_1_2 = new Migration(1,2) {
        @Override
        public void migrate(androidx.sqlite.db.SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE notes ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0");
        }
    };
    public static final Migration MIGRATION_2_3 = new Migration(2,3) {
        @Override
        public void migrate(androidx.sqlite.db.SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE notes ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0");
        }
    };
    public static synchronized NoteDatabase getDatabaseInstance(Context context){
        if (noteDatabase == null){
            noteDatabase = Room.databaseBuilder(context.getApplicationContext(),
                    NoteDatabase.class,"note_database")
                    .addMigrations(MIGRATION_1_2,MIGRATION_2_3)
                    .build();
        }
        return noteDatabase;
    }
    public abstract NoteDao noteDao();
}
