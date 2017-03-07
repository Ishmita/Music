package com.example.android.music;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
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
    public ArrayList<Integer> positions = new ArrayList<>();
    boolean isReleased = true;
    public MediaPlayer mediaPlayer;
    public MusicThread musicThread;
    public HashMap noteSound = new HashMap();
    public ArrayList<String> notesList = new ArrayList<>();
    public Handler handler;

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

        handler = new Handler() {
            public void handleMessage (Message msg) {
                //disable stop button after sound sequence finishes.
                pause.setEnabled(false);
                pause.setBackgroundColor(getResources().getColor(R.color.disabled));

                //enable play button after sound sequence finishes.
                play.setEnabled(true);
                play.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            }
        };

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String temp = notesEditText.getText().toString().toLowerCase();
                notesString = temp.replace(" ", "");
                dots = notesString.split("[^.]+");
                boolean validInput = checkNotes();

                if(isReleased && validInput) {
                    Log.d(TAG, "just before playing");
                    //disable play button till current sound sequence finishes.
                    play.setEnabled(false);
                    play.setBackgroundColor(getResources().getColor(R.color.disabled));

                    pause.setEnabled(true);
                    pause.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    musicThread = new MusicThread();
                    musicThread.start();
                }
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                musicThread.interrupt();
            }
        });


    }

    public boolean checkNotes() {
        notes = notesString.split("[.]+");
        int i = 0, j = 0, lenDots = dots.length;
        notesList.clear();
        positions.clear();

        for (String note: notes) {
            Log.d(TAG, "current note: " + note);

            if(noteSound.containsKey(note)) {
                Log.d(TAG, "straight added note: " + note);
                notesList.add(note);
                if (j < lenDots - 1) {
                    Log.d(TAG, "dots after " + (i+1) + "are: " + dots[j+1]);
                    positions.add(i);
                    i++;                //increment note number
                    j++;                //increment dot number
                }
            }else {
                Log.d(TAG, "needs splitting: " + note);
                String s = separateNotes(note);
                if(s!= null) {
                    String str[] = s.split(" ");
                    for (String string: str) {
                        Log.d(TAG, "after splitting each node: " +string);
                        notesList.add(string);
                    }
                    if (j < lenDots - 1) {
                        Log.d(TAG, "dots after " + (i+str.length) + " are: " + dots[j+1]);
                        positions.add(i + str.length - 1);
                        i = i + str.length;
                        j++;
                    }
                }else {
                    Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }

        /*for (int k =0 ;k < positions.size(); k++) {
            Log.d(TAG, "size of dotes after " + (positions.get(k) + 1) + ": " + dots[k+1].length());
        }*/
        return true;
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
                int j = 0;

                for (int i = 0; i < notesList.size() ; i++) {

                    Log.d(TAG, "note at " + (i+1) + ": " + notesList.get(i));
                    mediaPlayer = MediaPlayer.create(MainActivity.this,
                            (int)noteSound.get(notesList.get(i)));

                    mediaPlayer.start();

                    if(positions.size()>0 && j < positions.size() && positions.get(j) == i) {

                        Log.d(TAG, "DOTS after " + (positions.get(j)+1) +": " + dots[j+1].length());
                        Thread.sleep(dots[j+1].length() * 50);
                        j++;
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
                    //sendEmptyMessage to signify sound sequence finished or stopped
                    handler.sendEmptyMessage(0);
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
