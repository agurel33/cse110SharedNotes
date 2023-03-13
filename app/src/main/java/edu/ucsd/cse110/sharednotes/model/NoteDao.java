package edu.ucsd.cse110.sharednotes.model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Query;
import androidx.room.Upsert;

import java.util.List;

/** Data access object for the {@link Note} class. */
@Dao
public abstract class NoteDao {
    /**
     * In the TodoList app, our DAO used the @Insert, @Update to define methods that insert and
     * update items from the database.
     * <p>
     * Here we replace both @Insert and @Update with @Upsert. An @Upsert method will insert a new
     * item into the database if one with the title doesn't already exist, or update an existing
     * item if it does.
     */
    @Upsert
    public abstract long upsert(Note note);

    @Query("SELECT EXISTS(SELECT 1 FROM notes WHERE title = :title)")
    public abstract boolean exists(String title);

    @Query("SELECT * FROM notes WHERE title = :title")
    public abstract LiveData<Note> get(String title);

    @Query("SELECT * FROM notes ORDER BY title")
    public abstract LiveData<List<Note>> getAll();

    @Delete
    public abstract int delete(Note note);
}
