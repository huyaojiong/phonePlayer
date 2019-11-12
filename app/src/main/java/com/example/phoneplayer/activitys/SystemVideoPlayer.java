package com.example.phoneplayer.activitys;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.phoneplayer.R;
import com.example.phoneplayer.beans.MediaItem;
import com.example.phoneplayer.utils.CacheUtils;
import com.example.phoneplayer.utils.Utils;

import java.util.ArrayList;


public class SystemVideoPlayer extends Activity implements View.OnClickListener {
    /**
     * 进度更新
     */
    private static final int PROGRESS = 0;
    /**
     * 隐藏控制面板
     */
    private static final int HIDE_MEDIACONTROLLER = 2;
    /**
     * 默认播放
     */
    private static final int DEFAULT_SCREEN = 3;
    /**
     * 全屏播放
     */
    private static final int FULL_SCREEN = 4;
    /**
     * 显示网速
     */
    private static final int SHOW_SPEED = 5;
    private VideoView videoview;
    private Uri uri;
    private Utils utils;
    private BatteryReceiver receiver;
    private ArrayList<MediaItem> mediaItems;
    private int position;
    //定义手势识别器
    private GestureDetector detector;
    //是否隐藏控制面板
    private boolean isShowMediaController = false;
    private int mScreenWidth;
    private int mScreenHeight;
    private boolean isFullScreen = false;
    private int VideoWidth;
    private int VideoHeight;
    private int preCurrentPositon;//上一秒视频播放的位置
    private float mStarY = 0;//手指滑动的初始距离
    private float mStarX=0;
    int totalDistance = 0;
    private int mDownVoice;
    private TextView tv_name;
    private ImageView iv_battery;
    private TextView tv_time;
    private Button btn_voice;
    private SeekBar seekbar_voice;
    private Button btn_switch_player;
    private LinearLayout ll_top;
    private TextView tv_current_time;
    private SeekBar seekbar_video;
    private TextView tv_duration;
    private Button btn_video_exit;
    private Button btn_video_pre;
    private Button btn_video_start_pause;
    private Button btn_video_next;
    private Button btn_video_switch_screen;
    private LinearLayout ll_bottom;

    private AudioManager am;
    /**
     * 当前的音量：0~15
     */
    private int currentVolume;
    /**
     * 最大音量
     */
    private int maxVolume;
    /**
     * 是否是静音
     */
    private boolean isMute = false;
    /**
     * 是否是网络资源
     */
    private boolean isNetUri = false;

    private RelativeLayout rl_loading;
    private LinearLayout ll_buffer;
    private TextView tv_buffer_netspeed;
    private TextView tv_loading_netspeed;

    private int prePosition = 0;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case PROGRESS:
                    //得到当前的播放进度
                    int currentPosition = videoview.getCurrentPosition();
                    seekbar_video.setProgress(currentPosition);
                    tv_current_time.setText(utils.stringForTime(currentPosition));
                    //更新系统时间
                    tv_time.setText(utils.getSysteTime());
                    //设置缓冲效果
                    if (isNetUri) {

                        int buffer = videoview.getBufferPercentage();//0~100;

                        int totalBuffer = seekbar_video.getMax() * buffer;

                        int secondaryProgress = totalBuffer / 100;

                        seekbar_video.setSecondaryProgress(secondaryProgress);

                    } else {
                        seekbar_video.setSecondaryProgress(0);
                    }

                    int buffer = currentPosition - prePosition;

                    if (videoview.isPlaying()) {
                        if (buffer < 500) {
                            ll_buffer.setVisibility(View.VISIBLE);
                        } else {
                            ll_buffer.setVisibility(View.GONE);
                        }
                    }

                    prePosition = currentPosition;
                    handler.removeMessages(PROGRESS);
                    handler.sendEmptyMessageDelayed(PROGRESS, 1000);
                    break;
                case HIDE_MEDIACONTROLLER://隐藏控制面板
                    hideMediaController();
                    break;
                case SHOW_SPEED: {
                    String netSpeed = utils.getNetSpeed(SystemVideoPlayer.this);
                    tv_buffer_netspeed.setText("缓存中.." + netSpeed);
                    tv_loading_netspeed.setText("正在玩命加载中..." + netSpeed);
                    handler.sendEmptyMessageDelayed(SHOW_SPEED, 2000);
                }
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_system_video_player);




        initView();
        initData();
        getData();
        setData();
        setListener();
    }

    private void getData() {
        uri = getIntent().getData();
        mediaItems = (ArrayList<MediaItem>) getIntent().getSerializableExtra("videolist");
        position = getIntent().getIntExtra("position", 0);//列表中的位置
    }

    private void setData() {
        if (mediaItems != null && mediaItems.size() > 0) {
            MediaItem mediaItem = mediaItems.get(position);
            videoview.setVideoPath(mediaItem.getData());
            tv_name.setText(mediaItem.getName());
            isNetUri = utils.isNetUri(mediaItem.getData());
        } else if (uri != null) {
            videoview.setVideoURI(uri);
            tv_name.setText(uri.toString());
            isNetUri = utils.isNetUri(uri.toString());
        }

        setButtonState();

        //设置不锁屏
        videoview.setKeepScreenOn(true);

    }

    private void setListener() {
        videoview.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                VideoWidth = mp.getVideoWidth();
                VideoHeight = mp.getVideoHeight();

                //得到当前的播放进度
                int duration = videoview.getDuration();
                seekbar_video.setMax(duration);
                tv_duration.setText(utils.stringForTime(duration));
                //自定义控件后默认全屏，所以要先保持小屏
                handler.sendEmptyMessage(PROGRESS);
                videoview.start();//开始播放
                setVideoType(FULL_SCREEN);
                //默认隐藏控制面板
                hideMediaController();
                //隐藏加载页面
                rl_loading.setVisibility(View.GONE);
            }
        });

        videoview.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                // Toast.makeText(SystemVideoPlayer.this, "播放出错了", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        videoview.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //Toast.makeText(SystemVideoPlayer.this, "播放完成", Toast.LENGTH_SHORT).show();
                //finish();
                setPlayNext();

            }
        });

        seekbar_video.setOnSeekBarChangeListener(new VideoOnSeekBarChangeListener());

        seekbar_voice.setOnSeekBarChangeListener(new VoiceOnSeekBarChangeListener());
        //设置控制面板
        //videoview.setMediaController(new MediaController(this));


    }

    private void initData() {

        utils = new Utils();
        //注册监听电量广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        receiver = new BatteryReceiver();
        registerReceiver(receiver, intentFilter);

        detector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
                startAndPause();
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                Toast.makeText(SystemVideoPlayer.this, "我被双击了", Toast.LENGTH_SHORT).show();
                if (isFullScreen) {
                    setVideoType(DEFAULT_SCREEN);
                } else {
                    setVideoType(FULL_SCREEN);
                }
                return super.onDoubleTap(e);
            }


            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (isShowMediaController) {
                    //隐藏
                    hideMediaController();
                    handler.removeMessages(HIDE_MEDIACONTROLLER);
                } else {
                    //显示
                    showMediaController();
                    handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 5000);
                }
                return super.onSingleTapConfirmed(e);
            }
        });

        //实例化AudioManager
        am = (AudioManager) getSystemService(AUDIO_SERVICE);
        currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        seekbar_voice.setMax(maxVolume); //设置最大值
        seekbar_voice.setProgress(currentVolume);  //设置默认值


        //得到屏幕宽高,旧方法过时，推荐新技术
        //        getWindowManager().getDefaultDisplay().getWidth();
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;


        handler.sendEmptyMessage(SHOW_SPEED);
    }


    private void initView() {
        videoview = findViewById(R.id.videoview);
        tv_name = findViewById(R.id.tv_name);
        iv_battery = findViewById(R.id.iv_battery);
        tv_time = findViewById(R.id.tv_time);
        btn_voice = findViewById(R.id.btn_voice);
        seekbar_voice = findViewById(R.id.seekbar_voice);
        btn_switch_player = findViewById(R.id.btn_switch_player);
        ll_top = findViewById(R.id.ll_top);
        tv_current_time = findViewById(R.id.tv_current_time);
        seekbar_video = findViewById(R.id.seekbar_video);
        tv_duration = findViewById(R.id.tv_duration);
        btn_video_exit = findViewById(R.id.btn_video_exit);
        btn_video_pre = findViewById(R.id.btn_video_pre);
        btn_video_start_pause = findViewById(R.id.btn_video_start_pause);
        btn_video_next = findViewById(R.id.btn_video_next);
        btn_video_switch_screen = findViewById(R.id.btn_video_switch_screen);
        ll_bottom = findViewById(R.id.ll_bottom);

        rl_loading = (RelativeLayout) findViewById(R.id.rl_loading);
        ll_buffer = (LinearLayout) findViewById(R.id.ll_buffer);
        tv_buffer_netspeed = (TextView) findViewById(R.id.tv_buffer_netspeed);
        tv_loading_netspeed = (TextView) findViewById(R.id.tv_loading_netspeed);
        btn_voice.setOnClickListener(this);
        btn_switch_player.setOnClickListener(this);
        btn_video_exit.setOnClickListener(this);
        btn_video_pre.setOnClickListener(this);
        btn_video_start_pause.setOnClickListener(this);
        btn_video_next.setOnClickListener(this);
        btn_video_switch_screen.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_voice:
                isMute = !isMute;
                updateVolume(currentVolume);
                break;
            case R.id.btn_switch_player:
                showSwichPlayerDialog();
                break;
            case R.id.btn_video_exit:
                finish();
                break;
            case R.id.btn_video_pre:
                setPlayPre();
                break;
            case R.id.btn_video_start_pause:
                startAndPause();
                break;
            case R.id.btn_video_next:
                setPlayNext();
                break;
            case R.id.btn_video_switch_screen:
                break;
            default:
                break;
        }
        handler.removeMessages(HIDE_MEDIACONTROLLER);
        handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 5000);
    }






    class VoiceOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                updateVolumeProgress(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            handler.removeMessages(HIDE_MEDIACONTROLLER);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 5000);

        }
    }

    private void updateVolumeProgress(int volume) {
        am.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
        seekbar_voice.setProgress(volume);//设置seekBar进度
        currentVolume = volume;
        if (volume <= 0) {
            isMute = true;
        } else {
            isMute = false;
        }
    }

    /**
     * 根据传入的值修改音量
     *
     * @param volume
     */
    private void updateVolume(int volume) {
        if (isMute) {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
            seekbar_voice.setProgress(0);//设置seekBar进度
        } else {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
            seekbar_voice.setProgress(volume);//设置seekBar进度
            currentVolume = volume;
        }

    }

    class VideoOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                videoview.seekTo(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            handler.removeMessages(HIDE_MEDIACONTROLLER);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 5000);
        }
    }

    @Override
    protected void onDestroy() {
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        super.onDestroy();
    }

    /**
     * 电量监控
     */
    private class BatteryReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra("level", 0);//电量：0~100
            //主线程
            setBattery(level);
        }
    }

    private void setBattery(int level) {
        if (level <= 0) {
            iv_battery.setImageResource(R.drawable.ic_battery_0);
        } else if (level <= 10) {
            iv_battery.setImageResource(R.drawable.ic_battery_10);
        } else if (level <= 20) {
            iv_battery.setImageResource(R.drawable.ic_battery_20);
        } else if (level <= 40) {
            iv_battery.setImageResource(R.drawable.ic_battery_40);
        } else if (level <= 60) {
            iv_battery.setImageResource(R.drawable.ic_battery_60);
        } else if (level <= 80) {
            iv_battery.setImageResource(R.drawable.ic_battery_80);
        } else if (level <= 100) {
            iv_battery.setImageResource(R.drawable.ic_battery_100);
        } else {
            iv_battery.setImageResource(R.drawable.ic_battery_100);
        }
    }

    private void setPlayPre() {
        if (mediaItems != null && mediaItems.size() > 0) {
            //播放上一个
            position--;
            if (position >= 0) {

                MediaItem mediaItem = mediaItems.get(position);
                videoview.setVideoPath(mediaItem.getData());//设置播放地址-
                tv_name.setText(mediaItem.getName());
                isNetUri = utils.isNetUri(mediaItem.getData());

                setButtonState();
                rl_loading.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setPlayNext() {
        if (mediaItems != null && mediaItems.size() > 0) {
            //播放下一个
            position++;
            if (position < mediaItems.size()) {

                MediaItem mediaItem = mediaItems.get(position);
                videoview.setVideoPath(mediaItem.getData());//设置播放地址-
                tv_name.setText(mediaItem.getName());
                isNetUri = utils.isNetUri(mediaItem.getData());

                setButtonState();

                if (position == mediaItems.size() - 1) {
                    Toast.makeText(SystemVideoPlayer.this, "已经是最后一个视频了", Toast.LENGTH_SHORT).show();
                }
                rl_loading.setVisibility(View.VISIBLE);
            } else {
                finish();
            }
        } else if (uri != null) {
            //退出播放器
            finish();
        }
    }

    private void startAndPause() {
        if (videoview.isPlaying()) {
            //暂停
            videoview.pause();
            //按钮设置播放状态
            btn_video_start_pause.setBackgroundResource(R.drawable.btn_video_play_selector);
        } else {
            //播放
            videoview.start();
            //按钮设置暂停状态
            btn_video_start_pause.setBackgroundResource(R.drawable.btn_video_pause_selector);
        }

    }

    /**
     * 设置上一个和下一个按钮的状态
     */
    private void setButtonState() {

        //播放列表
        if (mediaItems != null && mediaItems.size() > 0) {

            if (position == 0) {//第一个视频
                btn_video_pre.setEnabled(false);
                btn_video_pre.setBackgroundResource(R.drawable.btn_pre_gray);
            } else if (position == mediaItems.size() - 1) {//最后一个视频
                btn_video_next.setEnabled(false);
                btn_video_next.setBackgroundResource(R.drawable.btn_next_gray);
            } else {
                btn_video_next.setEnabled(true);
                btn_video_next.setBackgroundResource(R.drawable.btn_video_next_selector);
                btn_video_pre.setEnabled(true);
                btn_video_pre.setBackgroundResource(R.drawable.btn_video_pre_selector);
            }

        } else if (uri != null) {

            btn_video_next.setEnabled(false);
            btn_video_next.setBackgroundResource(R.drawable.btn_next_gray);
            btn_video_pre.setEnabled(false);
            btn_video_pre.setBackgroundResource(R.drawable.btn_pre_gray);
        } else {
            Toast.makeText(this, "没有播放地址", Toast.LENGTH_SHORT).show();
        }
    }

    private void hideMediaController() {
        ll_bottom.setVisibility(View.GONE);
        ll_top.setVisibility(View.GONE);
        isShowMediaController = false;
    }

    private void showMediaController() {
        ll_bottom.setVisibility(View.VISIBLE);
        ll_top.setVisibility(View.VISIBLE);
        isShowMediaController = true;
    }

    private void showSwichPlayerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage("当前使用系统播放器播放，是否切换到万能播放器播放视频！");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mediaItems != null && mediaItems.size() > 0) {
                    MediaItem mediaItem = mediaItems.get(position);
                   CacheUtils.putInt(SystemVideoPlayer.this, mediaItem.getData(), videoview.getCurrentPosition());
                } else if (uri != null) {
                    CacheUtils.putInt(SystemVideoPlayer.this, uri.toString(),  videoview.getCurrentPosition());
                }

                startJieCaoPlayer();
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void startJieCaoPlayer() {

        if(videoview != null){
            videoview.stopPlayback();
        }


        Intent intent = new Intent(this, JieCaoPlayerActivity.class);
        if(mediaItems != null && mediaItems.size() >0){

            Bundle bundle = new Bundle();
            bundle.putSerializable("videolist", mediaItems);
            intent.putExtras(bundle);
            intent.putExtra("position", position);

        }else if(uri != null){
            intent.setData(uri);//文件
        }

        startActivity(intent);

        finish();

    }

    private void setVideoType(int defaultScreen) {

        if (defaultScreen == DEFAULT_SCREEN) {
            //1.设置视频画面的大小
            //视频真实的宽和高
            //1.设置视频画面的大小
            //视频真实的宽和高
            int mVideoWidth = VideoWidth;
            int mVideoHeight = VideoHeight;

            //屏幕的宽和高
            int width = mScreenWidth;
            int height = mScreenHeight;

            // for compatibility, we adjust size based on aspect ratio
            if (mVideoWidth * height < width * mVideoHeight) {
                //Log.i("@@@", "image too wide, correcting");
                width = height * mVideoWidth / mVideoHeight;
            } else if (mVideoWidth * height > width * mVideoHeight) {
                //Log.i("@@@", "image too tall, correcting");
                height = width * mVideoHeight / mVideoWidth;
            }
            isFullScreen = false;
            btn_video_switch_screen.setBackgroundResource(R.drawable.btn_switch_screen_full_selector);

        } else if (defaultScreen == FULL_SCREEN) {

            isFullScreen = true;
            btn_video_switch_screen.setBackgroundResource(R.drawable.btn_switch_screen_default_selector);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //3.把事件传递给手势识别器,非常重要，不写就不会有效果
        detector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStarY = event.getY();
                mStarX = event.getX();
                totalDistance = Math.min(mScreenHeight, mScreenWidth);
                //单独再拿取一次就是为了使得原有音量独立，如果用原先求得那个音量在move里用，因为move会不断执行
                //所以随着音量的增加会成倍增加，例如y=y+5；
                mDownVoice = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                handler.removeMessages(HIDE_MEDIACONTROLLER);
                break;
            case MotionEvent.ACTION_MOVE:

                float endY = event.getY();
                float endX = event.getX();
                //如何在屏幕右半部滑动改变音量，左半部改变亮度
                float distance = mStarY - endY;
                if (endX>mScreenWidth/2){
                    float delVoice = (distance / totalDistance) * maxVolume;

                    currentVolume = (int) Math.min(maxVolume, Math.max(0, delVoice + mDownVoice));//非常巧妙
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
                    seekbar_voice.setProgress(currentVolume);
                }else {
                    //左边屏幕-调节亮度
                    final double FLING_MIN_DISTANCE = 0.5;
                    final double FLING_MIN_VELOCITY = 0.5;
                    if (distance > FLING_MIN_DISTANCE
                            && Math.abs(distance) > FLING_MIN_VELOCITY) {
                        setBrightness(20);
                    }
                    if (distance < FLING_MIN_DISTANCE
                            && Math.abs(distance) > FLING_MIN_VELOCITY) {
                        setBrightness(-20);
                    }
                }

                break;
            case MotionEvent.ACTION_UP:
                handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
                break;

        }
        return super.onTouchEvent(event);
    }

    //监听物理音量键的改变
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            currentVolume--;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            currentVolume++;
        }
        am.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
        seekbar_voice.setProgress(currentVolume);
        return super.onKeyDown(keyCode, event);
    }

    /*
     *
     * 设置屏幕亮度 lp = 0 全暗 ，lp= -1,根据系统设置， lp = 1; 最亮
     */
    public void setBrightness(float brightness) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();

        lp.screenBrightness = lp.screenBrightness + brightness / 255.0f;
        //如果超过指定界限范围就开始在振动
        if (lp.screenBrightness > 1) {
            lp.screenBrightness = 1;

        } else if (lp.screenBrightness < 0.2) {
            lp.screenBrightness = (float) 0.2;

        }
        //设置亮暗度
        getWindow().setAttributes(lp);
    }


}
