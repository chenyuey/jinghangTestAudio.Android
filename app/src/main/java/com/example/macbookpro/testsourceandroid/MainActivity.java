package com.example.macbookpro.testsourceandroid;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.macbookpro.testsourceandroid.Util.SystemUtil;
import com.parse.FunctionCallback;
import com.parse.Parse;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.FunctionCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class MainActivity extends AppCompatActivity {

    public MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //创建meidiaPlayer
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                Log.e("audio","=========onError==========");
                return false;
            }
        });
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
                Log.e("audio","=========onPrepared==========");
            }
        });

        Button startBtn = findViewById(R.id.start_btn);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchTestJob();
            }
        });
    }
    public void startPlayMediaWithURL(HashMap<String, String> response){
        String mediaURL = response.get("mediaUrl");
        String mediaType = response.get("mediaType");
        String mediaId = response.get("mediaId");
        if (mediaType.equals("audio")) {//音频
            try {
                mediaPlayer.setDataSource(mediaURL);
                mediaPlayer.prepare();
            } catch (Exception e) {
                Log.e("audio error",e.toString());
                uploadTestReportToParseServer(mediaId,mediaURL,false,e.toString());
            }
        }else if (mediaType.equals("video")){
            try {
                mediaPlayer.setDataSource(mediaURL);
                mediaPlayer.prepare();
            } catch (Exception e) {
                Log.e("video error",e.toString());
            }
        }

    }
    public void uploadTestReportToParseServer(String mediaId,String mediaURL ,boolean success,String errorMsg){
        Map<String, String> dicEquipmentInfo = new HashMap<String, String>();
        dicEquipmentInfo.put("system_version", "Android"+SystemUtil.getSystemVersion());
        dicEquipmentInfo.put("player_name", "MediaPlayer");
        dicEquipmentInfo.put("equipment_name", SystemUtil.getSystemModel());

        Map<String, Object> dicParameters = new HashMap<String, Object>();
        dicParameters.put("equipment", dicEquipmentInfo);
        dicParameters.put("mediaId", mediaId);
        dicParameters.put("mediaUrl", mediaURL);
        dicParameters.put("errorMsg", errorMsg);
        dicParameters.put("success", success);
        ParseCloud.callFunctionInBackground("uploadTestReport", dicParameters, new FunctionCallback<HashMap<String, String>>() {
            public void done(HashMap<String, String> response, ParseException e) {
                Log.e("response==uploadTestReport:",response.toString());
                if (e == null) {
                } else {
                }
            }
        });
    }
    public void fetchTestJob(){
        Map<String, String> dicEquipmentInfo = new HashMap<String, String>();
        dicEquipmentInfo.put("system_version", "Android"+SystemUtil.getSystemVersion());
        dicEquipmentInfo.put("player_name", "MediaPlayer");
        dicEquipmentInfo.put("equipment_name", SystemUtil.getSystemModel());
        Map<String, Map<String, String>> dicParameters = new HashMap<String, Map<String, String>>();
        dicParameters.put("equipment", dicEquipmentInfo);
        ParseCloud.callFunctionInBackground("fetchTestJob", dicParameters, new FunctionCallback<HashMap<String, String>>() {
            public void done(HashMap<String, String> response, ParseException e) {
                if (e == null) {
                    //开始播放
                    startPlayMediaWithURL(response);
                    Log.e("tag=====:",response.toString());
                } else {
                }
            }
        });
    }
}
