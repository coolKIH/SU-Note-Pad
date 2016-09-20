package com.product.hao.newnotepad;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by HallHuang on 2016/8/15.
 */
public class NotePadDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "Hao_Db.db";
    private static final String NOTE_PAD_TB_NAME = "NotePad";
    private static final int DB_VERSION = 1;
    private static final String SQL_TB_CREATED =
            "CREATE TABLE IF NOT EXISTS " + NOTE_PAD_TB_NAME +
                    " (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, content TEXT);";
    private static final String SQL_TB_DROPPED =
            "DROP TABLE IF EXISTS " + NOTE_PAD_TB_NAME;


    public NotePadDbHelper(Context context){
        super(context,DB_NAME,null,DB_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_TB_CREATED);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(SQL_TB_DROPPED);
        onCreate(sqLiteDatabase);
    }
    public static String getTableName(){
        return NOTE_PAD_TB_NAME;
    }
}
