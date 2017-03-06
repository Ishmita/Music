package com.example.android.music;

import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    public EditText notesEditText;
    public ImageButton play, pause;
    String notesString, notes[], dots[];
    boolean isReleased;
    public MediaPlayer mediaPlayer;
    public MusicThread musicThread;
    public HashMap noteSound = new HashMap();
    public ArrayList<String> notesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        notesEditText = (EditText) findViewById(R.id.notes_edit_text);
        play = (ImageButton) findViewById(R.id.play_button);
        pause = (ImageButton) findViewById(R.id.pause_button);
        play.setEnabled(false);
        pause.setEnabled(false);
        noteSound.put("a1", R.raw.a1);
        noteSound.put("a1s", R.raw.a1s);
        noteSound.put("b1", R.raw.b1);
        noteSound.put("c1", R.raw.c1);
        noteSound.put("c1s", R.raw.c1s);
        noteSound.put("c2", R.raw.c2);
        noteSound.put("d1", R.raw.d1);
        noteSound.put("d1s", R.raw.d1s);
        noteSound.put("e1", R.raw.e1);
        noteSound.put("f1", R.raw.f1);
        noteSound.put("f1s", R.raw.f1s);
        noteSound.put("g1", R.raw.g1);
        noteSound.put("g1s", R.raw.g1s);

        notesEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length()>0) {
                    play.setEnabled(true);
                    play.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    play.setEnabled(false);
                    play.setBackgroundColor(getResources().getColor(R.color.disabled));
                    pause.setEnabled(false);
                    pause.setBackgroundColor(getResources().getColor(R.color.disabled));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isValid = true;
                pause.setEnabled(true);
                pause.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                notesString = notesEditText.getText().toString().toLowerCase();
                checkNotes();
                dots = notesString.split("[^.]+");
                for (String dot:dots) {
                    if(dot.length()>5) {
                        Toast.makeText(MainActivity.this, "Invalid Input", Toast.LENGTH_SHORT).show();
                        isValid = false;
                        break;
                    }
                }
                if(isValid) {
                    musicThread = new MusicThread();
                    musicThread.start();
                }
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer!=null && !isReleased) {
                    mediaPlayer.stop();
                }
                    musicThread.interrupt();
            }
        });
    }

    public void checkNotes() {
        notes = notesString.split("[.]+");
        for (String note: notes) {
            if(noteSound.containsKey(note)) {
                notesList.add(note);
            }else {
                String s = separateNotes(note);
                if(s!= null) {
                    String str[] = s.split(" ");
                    for (String string: str) {
                        notesList.add(string);
                    }
                }else {
                    Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public String separateNotes(String seq) {

        HashMap<String, String> map = new HashMap<>();
        if (noteSound.containsKey(seq)) {
            return seq;
        }
        if (map.containsKey(seq)) {
            return map.get(seq);
        }
        int len = seq.length();
        for (int i = 1; i <= len; i++) {
            String prefix = seq.substring(0 , i);

            if (noteSound.containsKey(prefix)) {
                String suffix = seq.substring(i , len);
                String sepSuffix = separateNotes(suffix);
                if(sepSuffix!= null) {
                    map.put(seq, prefix + " " + sepSuffix);
                    return prefix + " " + sepSuffix;
                }
            }
        }
        return null;
    }
    public class MusicThread extends Thread {

        public void run() {
            try {
                isReleased = false;
                for (int i = 0; i < notesList.size() ; i++) {

                    Log.d(TAG, "note at " + (i+1) + ": " + notesList.get(i));
                    mediaPlayer = MediaPlayer.create(MainActivity.this,
                            (int)noteSound.get(notesList.get(i)));

                    mediaPlayer.start();

                    if(dots.length > 0 && i < (dots.length-1)) {
                        Log.d(TAG, "dots at " + i +": " + dots[i + 1].length());
                        Thread.sleep(dots[i + 1].length() * 50);
                    }

                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            if(mediaPlayer.isLooping()) {
                                mediaPlayer.stop();
                            }
                            mediaPlayer.release();
                        }
                    });

                }
            }catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            } finally {
                if(mediaPlayer!= null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    isReleased = true;
                }
            }
        }
    }

    @Override
    protected void onStop() {
        if(mediaPlayer!= null) {
            mediaPlayer.release();
        }
        super.onStop();
    }
}
