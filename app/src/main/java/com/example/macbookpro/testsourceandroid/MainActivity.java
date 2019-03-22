package com.example.macbookpro.testsourceandroid;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

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

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private HashMap<String, String> mediaInfo;

    public MediaPlayer mediaPlayer;
    private SeekBar main_seekBar;

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
    private void createMediaPlayer(final String mediaURL,final String mediaId){
        if (mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
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
                Log.e("audio","=========onPrepared==========");
                mediaPlayer.start();
                int lengthOfTime = mediaPlayer.getDuration();
                main_seekBar.setProgress(0);
                main_seekBar.setMax(lengthOfTime);
                if (lengthOfTime > 2000){
                    mediaPlayer.seekTo(lengthOfTime - 2000);
                    main_seekBar.setProgress((lengthOfTime - 2000)/lengthOfTime);
                }
                updateProgressBar();
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                uploadTestReportToParseServer(mediaId,mediaURL,true,"");
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //创建视频播放器
        initExoPlayer();

        main_seekBar = (SeekBar) findViewById(R.id.main_seekBar);


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
            createMediaPlayer(mediaURL,mediaId);
            try {
                //"https://cms-1255803335.cos.ap-beijing.myqcloud.com/f39752c09f1d6428531c66df4a14bee9_X8UUzn21Pi.mp4"
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
                if (e == null) {
                    //开始播放
                    if (timer != null) {
                        main_seekBar.setProgress(0);
                        timer.cancel();
                        timer = null;
                    }
                    startPlayMediaWithURL(response);

                } else {
                    Log.e("tag==fetchTestJob=err=:",e.toString());
                }
            }
        });
    }
    // 开启线程，更新播放进度
    private void updateProgressBar() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 100;
                message.obj = System.currentTimeMillis();
                myHandler.sendMessage(message);
            }
        };
        timer = new Timer();
        // 参数：
        // 1000，延时1秒后执行。
        // 2000，每隔2秒执行1次task。
        timer.schedule(task, 0, 1000);
    }
    Timer timer;
    MyHandler myHandler = new MyHandler(MainActivity.this);

    private static class MyHandler extends Handler {
        private WeakReference<Activity> mActivity;

        MyHandler(Activity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity currentActivity = (MainActivity) mActivity.get();

            switch (msg.what) {
                case 100:
                    if (currentActivity.mediaPlayer != null)
                        currentActivity.main_seekBar.setProgress(currentActivity.mediaPlayer.getCurrentPosition() * 1000 / currentActivity.mediaPlayer.getDuration());
            }
        }
    }

}
