package edu.ucsd.cse110.sharednotes.model;

import android.util.Log;

import androidx.annotation.WorkerThread;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NoteAPI {
    // TODO: Implement the API using OkHttp!
    // TODO: - getNote (maybe getNoteAsync)
    // TODO: - putNote (don't need putNotAsync, probably)
    // TODO: Read the docs: https://square.github.io/okhttp/
    // TODO: Read the docs: https://sharednotes.goto.ucsd.edu/docs

    private volatile static NoteAPI instance = null;
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    private OkHttpClient client;

    public NoteAPI() {
        this.client = new OkHttpClient();
    }

    public static NoteAPI provide() {
        if (instance == null) {
            instance = new NoteAPI();
        }
        return instance;
    }
    @WorkerThread
    public void addNote(Note note) {
        RequestBody req_body = RequestBody.create(note.toJSON(), JSON);
        String title = note.title.replace(" ", "%20");

        Request request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/notes/" + title)
                .put(req_body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @WorkerThread
    public Note getNote(String title)  {
        String title2 = title.replace(" ", "%20");
        String body = null;
        var request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/notes/" + title2)
                .build();
        //try isn't running, operating on background thread, need completion block? or stop main thread and run on separate thread.
        try (var response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            body = response.body().string();
            Log.d("GET", body);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Note.fromJSON(body);
    }

    /**
     * An example of sending a GET request to the server.
     *
     * The /echo/{msg} endpoint always just returns {"message": msg}.
     *
     * This method should can be called on a background thread (Android
     * disallows network requests on the main thread).
     */
//    @WorkerThread
//    public String echo(String msg) {
//        // URLs cannot contain spaces, so we replace them with %20.
//        String encodedMsg = msg.replace(" ", "%20");
//
//        var request = new Request.Builder()
//                .url("https://sharednotes.goto.ucsd.edu/echo/" + encodedMsg)
//                .method("GET", null)
//                .build();
//
//        try (var response = client.newCall(request).execute()) {
//            assert response.body() != null;
//            var body = response.body().string();
//            Log.i("ECHO", body);
//            return body;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//    @AnyThread
//    public Future<String> echoAsync(String msg) {
//        var executor = Executors.newSingleThreadExecutor();
//        var future = executor.submit(() -> echo(msg));
//
//        // We can use future.get(1, SECONDS) to wait for the result.
//        return future;
//    }
}
