package com.example.macbookpro.testsourceandroid;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.macbookpro.testsourceandroid.Util.SystemUtil;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
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

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class MainActivity extends AppCompatActivity {

    SimpleExoPlayerView mExoPlayerView;
    private SimpleExoPlayer exoPlayer;
    private MediaSource videoSource;
//    private DefaultBandwidthMeter bandwidthMeter;

    public MediaPlayer mediaPlayer;

    private void initExoPlayer(){
        //1. 创建一个默认的 TrackSelector
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTackSelectionFactory);
        //2.创建ExoPlayer
        exoPlayer = ExoPlayerFactory.newSimpleInstance(MainActivity.this, trackSelector);
        //3.创建SimpleExoPlayerView
        mExoPlayerView = findViewById(R.id.exo_player);
        //4.为SimpleExoPlayer设置播放器
        mExoPlayerView.setPlayer(exoPlayer);
        mExoPlayerView.setUseController(false);
        exoPlayer.setPlayWhenReady(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //创建视频播放器
        initExoPlayer();

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
                Log.e("start","========fetchTestJob before=======");
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
                DataSource.Factory mediaDataSourceFactory = new DefaultDataSourceFactory(MainActivity.this,
                        Util.getUserAgent(MainActivity.this, "TestSourceAndroid"));
                ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
                videoSource=new ExtractorMediaSource(Uri.parse(mediaURL), mediaDataSourceFactory, extractorsFactory, null, null);
                exoPlayer.prepare(videoSource);
                exoPlayer.setPlayWhenReady(true);
            } catch (Exception e) {
                Log.e("video error",e.toString());
                uploadTestReportToParseServer(mediaId,mediaURL,false,e.toString());
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
                Log.e("uploadTestReport:",response.toString());
                if (e == null) {
                    fetchTestJob();
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
//                Log.e("tag====:",response.toString());
                if (e == null) {
                    Log.e("tag==fetchTestJob===:",response.toString());
                    //开始播放
                    startPlayMediaWithURL(response);

                } else {
                    Log.e("tag==fetchTestJob=err=:",e.toString());
                }
            }
        });
    }
}
