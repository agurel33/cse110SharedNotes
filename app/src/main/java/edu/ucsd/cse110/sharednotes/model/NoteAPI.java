package edu.ucsd.cse110.sharednotes.model;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NoteAPI {
    // TODO: Implement the API using OkHttp!
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

    public void addNote(Note note) throws InterruptedException {
        Gson gson = new Gson();

        String title2 = note.title.replace(" ", "%20");
        RequestBody body = RequestBody.create(gson.toJson(note), JSON);

        Request request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/notes/" + title2)
                .put(body)
                .build();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }});

        t.start();
        t.join();
    }

    public Note getNote(String title) throws IOException, InterruptedException {
        final String[] fullBody = new String[1];
        String title2 = title.replace(" ", "%20");
        Request request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/notes/" + title2)
                .build();
        //try isn't running, operating on background thread, need completion block? or stop main thread and run on separate thread.
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                    fullBody[0] = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }});

        t.start(); // spawn thread
        t.join();  // wait for thread to finish
        var note =  Note.fromJSON(fullBody[0]);
        return note;
    }

    /**
     * An example of sending a GET request to the server.
     *
     * The /echo/{msg} endpoint always just returns {"message": msg}.
     */
    public void echo(String msg) {
        // URLs cannot contain spaces, so we replace them with %20.
        msg = msg.replace(" ", "%20");

        var request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/echo/" + msg)
                .method("GET", null)
                .build();

        try (var response = client.newCall(request).execute()) {
            assert response.body() != null;
            var body = response.body().string();
            Log.i("ECHO", body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
