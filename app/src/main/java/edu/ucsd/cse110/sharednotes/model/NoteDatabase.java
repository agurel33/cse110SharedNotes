package edu.ucsd.cse110.sharednotes.model;

import android.content.Context;

import androidx.annotation.VisibleForTesting;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Note.class}, version = 2, exportSchema = false)
public abstract class NoteDatabase extends RoomDatabase {
    private volatile static NoteDatabase instance = null;

    public abstract NoteDao getDao();

    public synchronized static NoteDatabase provide(Context context) {
        if (instance == null) {
            instance = NoteDatabase.make(context);
        }
        return instance;
    }

    private static NoteDatabase make(Context context) {
        return Room.databaseBuilder(context, NoteDatabase.class, "note_app.db")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();
    }

    @VisibleForTesting
    public static void inject(NoteDatabase testDatabase) {
        if (instance != null ) {
            instance.close();
        }
        instance = testDatabase;
    }
}
