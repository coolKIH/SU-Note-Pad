package com.product.hao.newnotepad;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.skd.androidrecording.audio.AudioPlaybackManager;
import com.skd.androidrecording.video.PlaybackHandler;
import com.skd.androidrecording.visualizer.VisualizerView;
import com.skd.androidrecording.visualizer.renderer.BarGraphRenderer;

import java.io.File;
import java.io.IOException;

public class DisplayHistory extends AppCompatActivity implements WordsFragment.WordsItemClickListener, SoundFragment.AudioItemClickListener{
    public static final String TRANSFERED_ID = "com.product.hao.newnotepad.DisplayHistory.ID";
    Button bWords, bAudio;
    MediaPlayer mediaPlayer;
    int isPlayingAudioID;
    String[] names;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_history);
        names = SoundFragment.getFileNamesStrArr();

        Log.w("DBP whether null",String.valueOf(getActionBar()==null));

        bWords = (Button)findViewById(R.id.button_choose_text);
        bAudio = (Button)findViewById(R.id.button_choose_audio);
        bWords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().beginTransaction().replace(R.id.fragment_container,new WordsFragment()).commit();
                bWords.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,2));
                bAudio.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,1));
            }
        });
        bAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().beginTransaction().replace(R.id.fragment_container,new SoundFragment()).commit();
                bWords.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,1));
                bAudio.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,2));
            }
        });
        if(savedInstanceState==null){
            bWords.callOnClick();
        }
    }

    @Override
    public void playHDAudio(long id) {
        String filename = MainActivity_hbg.audioFileDir.getAbsolutePath() + File.separator + names[(int)id];
        Log.w("DBP file name is ", filename);
    }


    @Override
    public void playAudio(long id) {
        if(mediaPlayer==null) mediaPlayer = new MediaPlayer();
        if(!mediaPlayer.isPlaying()) {
            try {
                mediaPlayer.reset();
                isPlayingAudioID = (int)id;
                mediaPlayer.setDataSource(MainActivity_hbg.audioFileDir.getAbsolutePath() + "/" + names[((int) id)]);
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                Log.d("DBP", e.getMessage());
            }
        }
        else{
            if(isPlayingAudioID == (int)id){
                mediaPlayer.release();
                mediaPlayer = null;
            }else{
                mediaPlayer.stop();
                mediaPlayer.reset();
                try {
                    isPlayingAudioID = (int)id;
                    mediaPlayer.setDataSource(MainActivity_hbg.audioFileDir.getAbsolutePath() + "/" + names[((int) id)]);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                }catch (IOException e){
                    Log.d("DBP", e.getMessage());
                }
            }
        }
    }

    @Override
    public void backToEditNote(long id) {
        Intent intent = new Intent(this,MainActivity_hbg.class);
        intent.putExtra(TRANSFERED_ID,String.valueOf(id));
        setResult(RESULT_OK,intent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.w("DBP","DisplayHistory onStop!");
        if(mediaPlayer!=null){
            if(mediaPlayer.isPlaying()){
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }
    }
}
