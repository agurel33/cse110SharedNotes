package edu.ucsd.cse110.sharednotes.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.time.Instant;

class TimestampAdapter extends TypeAdapter<Long> {
    @Override
    public void write(JsonWriter out, Long value) throws java.io.IOException {
        var instant = Instant.ofEpochSecond(value);
        System.out.println(value);
        out.value(instant.toString());
    }

    @Override
    public Long read(JsonReader in) throws java.io.IOException {
        var instant = Instant.parse(in.nextString());
        return instant.getEpochSecond();
    }
}

@Entity(tableName = "notes")
public class Note {
    /** The title of the note. Used as the primary key for shared notes (even on the cloud). */
    @PrimaryKey
    @SerializedName("title")
    @NonNull
    public String title;

    /** The content of the note. */
    @SerializedName("content")
    @NonNull
    public String content;

    /**
     * When the note was last modified. Used for resolving local (db) vs remote (api) conflicts.
     * Defaults to 0 (Jan 1, 1970), so that if a note already exists remotely, its content is
     * always preferred to a new empty note.
     */
    @JsonAdapter(TimestampAdapter.class)
    @SerializedName(value = "updated_at", alternate = "updatedAt")
    public long updatedAt = 0;

    /** General constructor for a note. */
    public Note(@NonNull String title, @NonNull String content) {
        this.title = title;
        this.content = content;
        this.updatedAt = 0;
    }

    @Ignore
    public Note(@NonNull String title, @NonNull String content, long updatedAt) {
        this.title = title;
        this.content = content;
        this.updatedAt = updatedAt;
    }

    public static Note fromJSON(String json) {
        return new Gson().fromJson(json, Note.class);
    }
}