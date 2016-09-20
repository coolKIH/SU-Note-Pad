package com.product.hao.newnotepad;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.skd.androidrecording.audio.AudioRecordingHandler;
import com.skd.androidrecording.audio.AudioRecordingThread;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MainActivity_hbg extends AppCompatActivity implements ScanningCompleteListener,CropPicListener,GeneratingQRListener{
    EditText editTextTitle;
    EditText editTextContent;
    Button BSaveNote, BNewNote;
    SQLiteDatabase db;
    private MenuItem recorderItem= null;
    private static String idNoteInProcess;
    private static boolean returnFromList;
    private static String editTextTitleStr;
    private static String editTextContentStr;
    private static final int ID_REQUEST = 1;
    private static final int REQUEST_IMAGE_CAPURE = 2;
    private static final int REQUEST_IMAGE_CROP = 3;
    private static final int REQUEST_IMAGE_PICK = 4;
    private boolean recording = false;
    private MediaRecorder recorder = null;
    public static File audioFileDir = null;
    DrawerLayout drawerLayout;
    private ListView mDrawerList;
    private static final String SD_PATH= Environment.getExternalStorageDirectory().getPath()+File.separator+"haoOcr";
    ProgressDialog progressDialog = null;
    static String mlang;
    final String mPRES_NAME = "appSettings";
    final String mPRES_LANG = "setLang";
    final String DEFAULT_SCAN_LANG = "eng";
    AudioRecordingThread audioRecordingThread = null;
    private Boolean isReturnFromScanningQR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_with_hbg);

        drawerLayout = (DrawerLayout)findViewById(R.id.drawerlayout);
        Log.w("DBP",String.valueOf(getSupportActionBar()==null));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_drawer);

        editTextTitle = (EditText)findViewById(R.id.editTextTitle);
        editTextContent = (EditText)findViewById(R.id.editTextContent);
        db = new NotePadDbHelper(this).getWritableDatabase();
        returnFromList = false;

        audioFileDir = new File(getExternalFilesDir(null).getAbsolutePath()+"/recordedFiles/");
        if(!audioFileDir.exists()) {
            audioFileDir.mkdir();
        }

        mDrawerList = (ListView)findViewById(R.id.leftdrawer);
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,R.layout.drawer_list_item,getResources().getStringArray(R.array.array_functions_in_hbg)));
        initialDrawerReadyClick();
        BNewNote = (Button)findViewById(R.id.bNew);
        BNewNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTextContent.setText("");
                editTextTitle.setText("");
                returnFromList = false;
            }
        });
        BSaveNote = (Button)findViewById(R.id.bThatsit);
        BSaveNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateEditTextStr();
                if(!checkSavable()){
                    Toast.makeText(MainActivity_hbg.this, "Say something XD", Toast.LENGTH_SHORT).show();
                }else{
                    ContentValues cv = new ContentValues();
                    cv.put("title",editTextTitleStr);
                    cv.put("content",editTextContentStr);
                    Log.w("DBP","cv");
                    if(returnFromList){
                        db.update(NotePadDbHelper.getTableName(),cv,"id="+idNoteInProcess,null);
                    }else{
                        try {
                            db.insert(NotePadDbHelper.getTableName(), null, cv);
                        }catch (Exception e){
                            Log.w("DBP",e.getMessage());
                        }
                    }
                }
            }
        });
        File file = new File(SD_PATH);
        if(!file.exists()){
            file.mkdirs();
        }
        SharedPreferences sharedPreferences = getSharedPreferences(mPRES_NAME,0);
        mlang = sharedPreferences.getString(mPRES_LANG,DEFAULT_SCAN_LANG);
        isReturnFromScanningQR = false;
    }

    @Override
    public void setTextResult(String strng) {
        editTextContent.setText(strng);
    }

    void initialDrawerReadyClick(){
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(i==0){
                    goToList(view);
                }else if(i==1){
                    removeAll(view);
                }else if(i==2){
                    SharedPreferences sharedPreferences = getSharedPreferences(mPRES_NAME,0);
                    final SharedPreferences.Editor editor = sharedPreferences.edit();
                    AlertDialog.Builder builder = new AlertDialog.Builder(mDrawerList.getContext());
                    builder.setTitle("Select a language to scan...")
                            .setItems(R.array.array_scan_lang, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Log.w("DBP click lang ",String.valueOf(i));
                                    switch (i){
                                        case 0:
                                            mlang = "eng";
                                            break;
                                        case 1:
                                            mlang = "chi_sim";
                                            break;
                                        case 2:
                                            mlang = "chi_tra";
                                            default:
                                                mlang = DEFAULT_SCAN_LANG;
                                    }
                                    editor.putString(mPRES_LANG,mlang).commit();
                                }
                            }).create().show();
                }else if(i==3){
                    startActivity(new Intent(MainActivity_hbg.this,QRScreen.class));
                }
                ((DrawerLayout)findViewById(R.id.drawerlayout)).closeDrawer(mDrawerList);
            }
        });
    }

    void goToList(View v){
        Log.w("DBP","goTolist");
        Intent intent = new Intent(this,DisplayHistory.class);
        startActivityForResult(intent,ID_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==ID_REQUEST){
            if(resultCode == RESULT_OK){
                returnFromList = true;
                logCatDis(data.getStringExtra(DisplayHistory.TRANSFERED_ID));
                idNoteInProcess = data.getStringExtra(DisplayHistory.TRANSFERED_ID);
                try {
                    Cursor cv = db.query(NotePadDbHelper.getTableName(), new String[]{"title", "content"}, "id=?", new String[]{idNoteInProcess}, null, null, null);
                    cv.moveToFirst();
                    editTextTitle.setText(cv.getString(0));
                    editTextContent.setText(cv.getString(1));
                }catch (Exception e){
                    logCatDis(e.getMessage());
                }
            }
        }else if(requestCode==REQUEST_IMAGE_CAPURE||requestCode==REQUEST_IMAGE_PICK){
            if(resultCode==RESULT_OK){
                Log.w("DBP image rec"," start progress");
                Log.w("data is null? ",String.valueOf(data==null));
                PromptCropDialog promptCropDialog = new PromptCropDialog(this,data);
                promptCropDialog.cropPicListener = this;
                promptCropDialog.show();
            }
        }else if(requestCode==REQUEST_IMAGE_CROP){
            if(resultCode==RESULT_OK){
                Bundle extras = data.getExtras();
                Bitmap bitmap = extras.getParcelable("data");
                Log.w("bitmap is null? ",String.valueOf(bitmap==null));
                onImageTaken(bitmap);
            }
        }
        else{
                super.onActivityResult(requestCode, resultCode, data);
        }
        if(isReturnFromScanningQR) {
            IntentResult scannedResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (scannedResult != null) {
                String scannedContent = scannedResult.getContents();
                String scannedFormat = scannedResult.getFormatName();
                editTextContent.setText(scannedContent);
                editTextTitle.setText(scannedFormat);
            } else {
                Toast.makeText(this, "Failure scanning QR code", Toast.LENGTH_SHORT).show();
            }
            isReturnFromScanningQR = false;
        }
    }

    @Override
    public void getBitmapAndCropAndDone(Intent data) {
        Toast.makeText(this,"will crop",Toast.LENGTH_SHORT).show();
        Uri picUri = data.getData();
        try{
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.setDataAndType(picUri,"image/*");
            cropIntent.putExtra("crop","true");
            cropIntent.putExtra("return-data",true);
            startActivityForResult(cropIntent,REQUEST_IMAGE_CROP);
        }catch (ActivityNotFoundException e){
            String errorMessage = "Whoops - your device doesn't support the crop";
            Toast.makeText(this,errorMessage,Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void getBitmapAndDone(Intent data) {
        Toast.makeText(this,"will not crop",Toast.LENGTH_SHORT).show();
        Bitmap bitmap = null;
        if(data.getData()==null){
            bitmap = (Bitmap)data.getExtras().get("data");
        }else{
            try {
                Log.d("DBP capure result ","data.getData()!=null" + "but extras is null? "+String.valueOf(data.getExtras()==null));
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
            }catch (Exception e){
                Log.d("DBP","REQUEST Error");
            }
        }
        Log.w("bitmap is null? ",String.valueOf(bitmap==null));
        onImageTaken(bitmap);
    }

    private void onImageTaken(Bitmap bitmap){
        Log.w("DBP image taken"," on image taken");
        Log.w("bitmap is  ",String.valueOf(bitmap==null));
        if(bitmap==null)return;
        ScanningWork scanningWork = new ScanningWork(this,bitmap);
        scanningWork.scanningCompleteListener = this;
        scanningWork.execute();
    }

    void updateEditTextStr(){
        editTextTitleStr = editTextTitle.getText().toString();
        editTextContentStr = editTextContent.getText().toString();
    }
    boolean checkSavable(){
        return !(editTextTitleStr.isEmpty() && editTextContentStr.isEmpty());
    }
    void logCatDis(String s){
        Log.w("DBP",s);
    }
    void removeAll(View v){
        new NotePadDbHelper(this).onUpgrade(db,1,2);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_action_bar,menu);
        recorderItem = menu.findItem(R.id.recordAudio);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.recordAudio:
                if(recording){
                    stopAudioRecorder();
                    recorderItem.setIcon(R.drawable.recorder);
                }else{
                    startAudioRecorder();
                    recorderItem.setIcon(R.drawable.recorderon);
                }{
                }
                recording = !recording;
                return true;
            case android.R.id.home:
                if(drawerLayout.isDrawerOpen(mDrawerList))
                drawerLayout.closeDrawer(mDrawerList);
                else
                drawerLayout.openDrawer(mDrawerList);
                return true;
            case R.id.camera:
                startOcr();
                return true;
            case R.id.album:
                pickImgFromAlbum();
                return true;
            case R.id.scanQR:
                IntentIntegrator scanIntent = new IntentIntegrator(this);
                scanIntent.initiateScan();
                isReturnFromScanningQR=true;
                return true;
            case R.id.disQR:
                GenQRFromNote genQRFromNote = new GenQRFromNote(this);
                genQRFromNote.genQRHelper = this;
                genQRFromNote.execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void displayQRForNote(Bitmap bitmap) {
        if(bitmap == null){
            Toast.makeText(this,"Failure fetching qr code",Toast.LENGTH_SHORT);
        }else{
            ImageView imageView = new ImageView(this);
            imageView.setImageBitmap(bitmap);
            AlertDialog.Builder buider=
                    new AlertDialog.Builder(this).
                            setMessage("Scanned to get the content!")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).setView(imageView);
            buider.create().show();
        }
    }


    @Override
    public Bitmap getQRForNote() {
        updateEditTextStr();
        QRCodeWriter writer = new QRCodeWriter();
        Bitmap bmp = null;
        try {
            String eol=System.getProperty("line.separator");
            BitMatrix bitMatrix = writer.encode(editTextTitleStr+eol+editTextContentStr, BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return bmp;
    }


    void pickImgFromAlbum(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,REQUEST_IMAGE_PICK);
    }
    private  void startOcr(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager())!=null){
            startActivityForResult(intent,REQUEST_IMAGE_CAPURE);
        }
    }

    private void startAudioRecorder(){
        audioRecordingThread = new AudioRecordingThread(generateStrNewFileName(), new AudioRecordingHandler() {
            @Override
            public void onFftDataCapture(byte[] bytes) {

            }

            @Override
            public void onRecordSuccess() {

            }

            @Override
            public void onRecordingError() {

            }

            @Override
            public void onRecordSaveError() {

            }
        });
        audioRecordingThread.start();
    }

    private void stopAudioRecorder(){
        if(audioRecordingThread!=null){
            audioRecordingThread.stopRecording();
            audioRecordingThread = null;
        }
    }

    private void startRecorder(){
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setOutputFile(generateStrNewFileName());
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setAudioEncodingBitRate(128);
        recorder.setAudioSamplingRate(44100);
        try{
            recorder.prepare();
        }catch (Exception e){
            Log.w("DBO",e.getMessage());
        }
        recorder.start();
    }
    private void stopRecorder(){
        recorder.stop();
        recorder.release();
        recorder = null;
    }
    private String generateStrNewFileName(){
        Calendar calendar = Calendar.getInstance();
        String string = audioFileDir.getAbsolutePath();
        string+="/";
        string+=calendar.get(calendar.YEAR);
        if(calendar.get(calendar.MONTH)<9) string+="0";
        string+=String.valueOf(calendar.get(calendar.MONTH)+1);
        if(calendar.get(calendar.DATE)<10) string+="0";
        string+=String.valueOf(calendar.get(calendar.DATE));
        if(calendar.get(calendar.HOUR_OF_DAY)<10)string+="0";
        string+=String.valueOf(calendar.get(calendar.HOUR_OF_DAY));
        if(calendar.get(calendar.MINUTE)<10)string+="0";
        string+=String.valueOf(calendar.get(calendar.MINUTE));
        if(calendar.get(calendar.SECOND)<10)string+="0";
        string+=calendar.get(calendar.SECOND);
        string+=".wav";
        return string;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(recorder!=null){
            recorder.release();
            recorder = null;
            recording = false;
            recorderItem.setIcon(R.drawable.recorder);
        }
    }

    public static String getSdPath(){
        return SD_PATH;
    }
    public static String getLang(){return mlang;}
}

