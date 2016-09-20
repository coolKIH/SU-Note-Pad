package com.product.hao.newnotepad;


import android.app.Activity;
import android.app.ListFragment;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class SoundFragment extends ListFragment {
    public static String mAudioFilePostfix = ".wav";

    AudioItemClickListener audioItemListener;
    ArrayAdapter<String> arrayAdapter;
    List<String> audioItems;

    public SoundFragment() {
        // Required empty public constructor
    }

    public interface AudioItemClickListener {
        public void playAudio(long id);
        public void playHDAudio(long id);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayListOfAudio();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mDealWithClickEvent();
    }
    void mDealWithClickEvent(){
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                File deleteFile = new File(MainActivity_hbg.audioFileDir,getFileNamesStrArr()[((int) l)]);
                if(deleteFile!=null){
                    deleteFile.delete();
                }
                if(!deleteFile.exists()){
                    new AlertDialog.Builder(getListView().getContext()).setTitle("Hey").setMessage("Goodbye").show();
                }
                audioItems.clear();;
                Collections.addAll(audioItems,getFileNamesStrArr());
                arrayAdapter.notifyDataSetChanged();
                return true;
            }
        });
    }

    void DisplayListOfAudio() {
        audioItems = new ArrayList<String>();
        Collections.addAll(audioItems,getFileNamesStrArr());
        Log.w("DBP","Size of audio items is "+String.valueOf(audioItems.size()));
        arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, audioItems);
        setListAdapter(arrayAdapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if(audioItemListener!=null) {
            audioItemListener.playAudio(id);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            audioItemListener = (AudioItemClickListener) activity;
        }catch (ClassCastException e){
            Log.w("DBP",e.getMessage());
        }
    }
    public static String[] getFileNamesStrArr(){
        return MainActivity_hbg.audioFileDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith(mAudioFilePostfix);
            }
        });
    }
}