package edu.ucsd.cse110.sharednotes.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class NoteRepository {
    private final NoteDao dao;
    private ScheduledFuture<?> poller; // what could this be for... hmm?
    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    public NoteRepository(NoteDao dao) {
        this.dao = dao;
    }

    // Synced Methods
    // ==============

    /**
     * This is where the magic happens. This method will return a LiveData object that will be
     * updated when the note is updated either locally or remotely on the server. Our activities
     * however will only need to observe this one LiveData object, and don't need to care where
     * it comes from!
     * <p>
     * This method will always prefer the newest version of the note.
     *
     * @param title the title of the note
     * @return a LiveData object that will be updated when the note is updated locally or remotely.
     */
    public LiveData<Note> getSynced(String title) throws InterruptedException {
        var note = new MediatorLiveData<Note>();

        Observer<Note> updateFromRemote = theirNote -> {
            var ourNote = note.getValue();
            if (theirNote == null) return; // do nothing
            if (ourNote == null || ourNote.version < theirNote.version) {
                upsertLocal(theirNote, false);
            }
        };

        // If we get a local update, pass it on.
        note.addSource(getLocal(title), note::postValue);
        // If we get a remote update, update the local version (triggering the above observer)
        note.addSource(getRemote(title), updateFromRemote);

        return note;
    }

    public void upsertSynced(Note note) throws InterruptedException {
        upsertLocal(note);
        upsertRemote(note);
    }

    // Local Methods
    // =============

    public LiveData<Note> getLocal(String title) {
        return dao.get(title);
    }

    public LiveData<List<Note>> getAllLocal() {
        return dao.getAll();
    }

    public void upsertLocal(Note note, boolean incrementVersion) {
        // We don't want to increment when we sync from the server, just when we save.
        if (incrementVersion) note.version = note.version + 1;
        note.version = note.version + 1;
        dao.upsert(note);
    }

    public void upsertLocal(Note note) {
        upsertLocal(note, true);
    }

    public void deleteLocal(Note note) {
        dao.delete(note);
    }

    public boolean existsLocal(String title) {
        return dao.exists(title);
    }

    // Remote Methods
    // ==============

    public LiveData<Note> getRemote(String title) throws InterruptedException {
        // Cancel any previous poller if it exists.
//        if (this.poller != null && !this.poller.isCancelled()) {
//            poller.cancel(true);
//        }

        MutableLiveData<Note> noteData = new MutableLiveData<>();
        var localNote = dao.get(title).getValue();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        poller = scheduler.scheduleAtFixedRate(() ->{
            Note remote = new Note("", "");
            try {
                NoteAPI api = new NoteAPI();
                api = api.provide();
                remote = api.getNote(title);
                noteData.postValue(remote);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (noteData.getValue() != null && noteData.getValue().version > localNote.version) {
                upsertLocal(remote, true);
            }
        }, 0, 3, TimeUnit.SECONDS);
        return noteData;
    }

    public void upsertRemote(Note note) throws InterruptedException {
        NoteAPI api = new NoteAPI();
        api = api.provide();
        NoteAPI finalApi = api;
        Thread t = new Thread(() -> finalApi.addNote(note));
        t.start();
        t.join();
    }
}
