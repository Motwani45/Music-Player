package com.example.i_music;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.InterruptedIOException;
import java.util.ArrayList;

public class PlaySong extends AppCompatActivity {
    MediaPlayer mediaPlayer;
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        mediaPlayer.release();
        updateSeek.interrupt();
    }
    AudioManager audioManager;
    AudioAttributes audioAttributes;
    AudioFocusRequest audioFocusRequest;
    AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener=new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if(focusChange==AudioManager.AUDIOFOCUS_GAIN){
                mediaPlayer.start();
            }
            else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                mediaPlayer.pause();
                mediaPlayer.seekTo(0);
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
            }
    };
    TextView textView;
    ImageView play,previous,next,pause;
    ArrayList<File> songs;
    /*MediaPlayer mediaPlayer;*/
    int position;
    SeekBar seekBar;
    String textContent;
    Thread updateSeek;
    TextView startTime;
    TextView endTime;
    Utilities utils=new Utilities();
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_song);
        textView=findViewById(R.id.textView);
        startTime=findViewById(R.id.startTime);
        endTime=findViewById(R.id.endTime);
        play=findViewById(R.id.play);
        previous=findViewById(R.id.previous);
        next=findViewById(R.id.next);
        seekBar=findViewById(R.id.seekBar);
        audioManager= (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioAttributes=new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();
        audioFocusRequest=new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).setAudioAttributes(audioAttributes).setAcceptsDelayedFocusGain(true).setOnAudioFocusChangeListener(onAudioFocusChangeListener).build();
        Intent in=getIntent();
        Bundle bundle=in.getExtras();
        songs=(ArrayList) bundle.getParcelableArrayList("songList");
        textContent=in.getStringExtra("currentSong");
        textView.setText(textContent);
        textView.setSelected(true);
        position=in.getIntExtra("position",0);
        Uri uri= Uri.parse(songs.get(position).toString());
        mediaPlayer=MediaPlayer.create(this,uri);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                next.performClick();
            }
        });
        seekBar.setMax(mediaPlayer.getDuration());


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });
        updateSeek=new Thread(){
            @Override
            public void run() {
                endTime.setText(utils.milliSecondsToTimer(mediaPlayer.getDuration()));
                int currentPos=0;
                try{
                    while(currentPos< mediaPlayer.getDuration()){
                        startTime.setText(utils.milliSecondsToTimer(mediaPlayer.getCurrentPosition()));
                        currentPos= mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(currentPos);
                        sleep(1000);
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

        };
        updateSeek.start();
        play.setImageResource(R.drawable.pause);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final int focusrequest=audioManager.requestAudioFocus(audioFocusRequest);
                if(mediaPlayer.isPlaying()){
                    play.setImageResource(R.drawable.play);
                    mediaPlayer.pause();
                    audioManager.abandonAudioFocusRequest(audioFocusRequest);
                }
                else{
                    if(focusrequest==AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        play.setImageResource(R.drawable.pause);
//                        Toast.makeText(getApplicationContext(), "AudioFocus Request Granted Playing Media...", Toast.LENGTH_SHORT).show();
                        mediaPlayer.start();
                    }
                    else if(focusrequest==AudioManager.AUDIOFOCUS_REQUEST_DELAYED){
//                        Toast.makeText(getApplicationContext(), "AudioFocus Request Delayed Please Wait For A While...", Toast.LENGTH_SHORT).show();
                    }
                    else if(focusrequest==AudioManager.AUDIOFOCUS_REQUEST_FAILED){
//                        Toast.makeText(getApplicationContext(), "AudioFocus Request Failed Can't Play The Media...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                if(position!=0){
                    position-=1;
                }
                else{
                    position=songs.size()-1;
                }
                Uri uri= Uri.parse(songs.get(position).toString());
                mediaPlayer=new MediaPlayer().create(getApplicationContext(),uri);
                seekBar.setMax(mediaPlayer.getDuration());
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        next.performClick();
                    }
                });
                new Thread(updateSeek).start();
                textContent=songs.get(position).getName().toString();
                textView.setText(textContent);
                play.setImageResource(R.drawable.pause);
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                if(position!=(songs.size()-1)){
                    position+=1;
                }
                else{
                    position=0;
                }
                Uri uri= Uri.parse(songs.get(position).toString());
                mediaPlayer=new MediaPlayer().create(getApplicationContext(),uri);
                seekBar.setMax(mediaPlayer.getDuration());
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        next.performClick();
                    }
                });
                new Thread(updateSeek).start();
                textContent=songs.get(position).getName().toString();
                textView.setText(textContent);
                play.setImageResource(R.drawable.pause);
            }
        });

    }
}