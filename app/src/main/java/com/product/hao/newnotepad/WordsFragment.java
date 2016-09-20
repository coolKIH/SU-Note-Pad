package com.product.hao.newnotepad;


import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class WordsFragment extends ListFragment {

    SimpleCursorAdapter cursorAdapter;
    WordsItemClickListener wordsItemClickListener;
    public WordsFragment() {
        // Required empty public constructor
    }
    public interface WordsItemClickListener{
        public void backToEditNote(long id);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayListOfNotes();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mDealWithClickEvent();
    }

    void DisplayListOfNotes(){
        try{
            SQLiteDatabase db = new NotePadDbHelper(getActivity()).getReadableDatabase();
            Log.w("DBP", "Open DB");
            Cursor cursor = db.rawQuery("SELECT id AS _id, * FROM "+NotePadDbHelper.getTableName()+" ORDER BY _id DESC",null);
            Log.w("DBP", "Open cursor");
            if(cursor.moveToFirst()!=false){
                cursorAdapter = new SimpleCursorAdapter(getActivity(),R.layout.audio_list_item_2,cursor,new String[]{"title","content"},new int[]{android.R.id.text1,android.R.id.text2},2){
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view =  super.getView(position, convertView, parent);
                        //TextView textView= (TextView)view.findViewById(android.R.id.text2);
                        //textView.setMinHeight(0);
                        //textView.setMinimumHeight(0);
                        //textView.setHeight(48);
                        Log.w("DBP","getView fun "+String.valueOf(position)+" "+String.valueOf(view.getMinimumHeight())+" "+String.valueOf(view.isPressed()));
                        return view;
                    }

                };
                setListAdapter(cursorAdapter);
            }
            else{
                Log.w("DBP","Cursor move to first.");
            }
        }
        catch(Exception e){
            Log.w("DBP",e.getMessage());
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.w("DBP","OnAttach context");
        try{
            wordsItemClickListener = (WordsItemClickListener)context;
        }catch (Exception e){
            Log.w("DBP",e.getMessage());
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.w("DBP","OnAttach Activity");
        try{
            wordsItemClickListener = (WordsItemClickListener)activity;
            Log.w("DBP","OnAttach Activity named "+activity.toString());

        }catch (ClassCastException e){
            Log.w("DBP",e.getMessage());
        }
    }

    void mDealWithClickEvent(){
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                updateDbRemoveItem(l);
                SQLiteDatabase db = new NotePadDbHelper(getActivity()).getReadableDatabase();
                Cursor cursor = db.rawQuery("SELECT id AS _id, * FROM "+NotePadDbHelper.getTableName()+" ORDER BY _id DESC",null);
                cursor.moveToFirst();
                cursorAdapter.changeCursor(cursor);
                Log.d("DBP","Long Press");
                new AlertDialog.Builder(getListView().getContext()).setTitle("Hey").setMessage("Goodbye").show();
                cursorAdapter.notifyDataSetChanged();
                return true;
            }
        });
    }
    void updateDbRemoveItem(long l){
        SQLiteDatabase db = new NotePadDbHelper(getActivity()).getWritableDatabase();
        db.delete(NotePadDbHelper.getTableName(),"id=?",new String[]{String.valueOf(l)});
        db.close();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if(wordsItemClickListener!=null) {
            wordsItemClickListener.backToEditNote(id);
        }
    }

}
