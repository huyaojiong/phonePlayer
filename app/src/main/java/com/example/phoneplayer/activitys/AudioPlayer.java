package com.example.phoneplayer.activitys;


import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.phoneplayer.IMediaService;
import com.example.phoneplayer.R;
import com.example.phoneplayer.beans.MediaItem;
import com.example.phoneplayer.pagers.AudioPager;
import com.example.phoneplayer.services.MediaService;
import com.example.phoneplayer.utils.Content;
import com.example.phoneplayer.utils.LyricUtils;
import com.example.phoneplayer.utils.Utils;
import com.example.phoneplayer.views.BaseVisualizerView;
import com.example.phoneplayer.views.LyricShowView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

public class AudioPlayer extends Activity implements View.OnClickListener {
    private static final int PROGRESS = 1;
    /**
     * 显示歌词-歌词缓缓往上推移
     */
    private static final int SHOW_LYRIC = 2;
    private ImageView iv_icon;
    private ImageView ivIcon;
    private TextView tvName;
    private TextView tvArtist;
    private TextView tvTime;
    private SeekBar seekbarAudio;
    private Button btnAudioPlaymode;
    private Button btnAudioPre;
    private Button btnAudioStartPause;
    private Button btnAudioNext;
    private Button btnLyrc;
    private LyricShowView lyric_showview;
    private BaseVisualizerView baseVisualizerView;
    private Utils utils;
    private Visualizer mVisualizer;

    /**
     * 音频播放的列表位置
     */
    private int position;
    /**
     * MediaService的代理类
     */
    private IMediaService mediaService;
    private MyReceiver receiver;
    private ServiceConnection con = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mediaService = IMediaService.Stub.asInterface(service);
            try {
                if (!notification) {
                    mediaService.openAudio(position);
                } else {
                    mediaService.notifyChange(Content.OPEN_AUDIO);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mediaService = null;
        }
    };
    private boolean notification;

    @Override
    protected void onPause() {
        super.onPause();
        mVisualizer.release();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_audio_player);
        initData();
        findViews();
        getData();
        bindAndStartService();
    }

    private void getData() {

        notification = getIntent().getBooleanExtra("Notification", false);
        if (!notification) {
            position = getIntent().getIntExtra("position", 0);
        }
    }

    private void bindAndStartService() {
        Intent intent = new Intent(this, MediaService.class);
        intent.setAction("com.example.phoneplayer_OPENAUDIO");
        bindService(intent, con, Context.BIND_AUTO_CREATE);
        startService(intent);//避免Service被重新创建
    }

    private void initData() {
        utils = new Utils();
        //注册广播
        receiver = new MyReceiver();
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction(Content.OPEN_AUDIO);//监听打开音乐成功的动作
        registerReceiver(receiver, intentfilter);

        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true, priority = 5)
    public void onMessageEvent(MediaItem mediaItem) {
        setViewData();
        try {
            seekbarAudio.setMax(mediaService.getDuration());
        } catch (RemoteException e) {
            e.printStackTrace();
        }


        handler.sendEmptyMessage(PROGRESS);
        setPlayAndpauseState();


        showLyric();
        setupVisualizerFxAndUi();
    }

    private void setupVisualizerFxAndUi() {

        try {
            int audioSessionid = mediaService.getAudioSessionId();
            System.out.println("audioSessionid==" + audioSessionid);
            mVisualizer = new Visualizer(audioSessionid);
            // 参数内必须是2的位数
            mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
            // 设置允许波形表示，并且捕获它
            baseVisualizerView.setVisualizer(mVisualizer);
            mVisualizer.setEnabled(true);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private void findViews() {
        setContentView(R.layout.activity_audio_player);
        ivIcon = (ImageView) findViewById(R.id.iv_icon);
        tvName = (TextView) findViewById(R.id.tv_name);
        tvArtist = (TextView) findViewById(R.id.tv_artist);
        tvTime = (TextView) findViewById(R.id.tv_time);
        seekbarAudio = (SeekBar) findViewById(R.id.seekbar_audio);
        btnAudioPlaymode = (Button) findViewById(R.id.btn_audio_playmode);
        btnAudioPre = (Button) findViewById(R.id.btn_audio_pre);
        btnAudioStartPause = (Button) findViewById(R.id.btn_audio_start_pause);
        btnAudioNext = (Button) findViewById(R.id.btn_audio_next);
        btnLyrc = (Button) findViewById(R.id.btn_lyrc);
        lyric_showview = (LyricShowView) findViewById(R.id.lyric_showview);
        baseVisualizerView = (BaseVisualizerView) findViewById(R.id.baseVisualizerView);

        iv_icon = (ImageView) findViewById(R.id.iv_icon);
        iv_icon.setBackgroundResource(R.drawable.animation_list);
        AnimationDrawable animationDrawable = (AnimationDrawable) iv_icon.getBackground();
        animationDrawable.start();

        btnAudioPlaymode.setOnClickListener(this);
        btnAudioPre.setOnClickListener(this);
        btnAudioStartPause.setOnClickListener(this);
        btnAudioNext.setOnClickListener(this);
        btnLyrc.setOnClickListener(this);

        seekbarAudio.setOnSeekBarChangeListener(new MyOnSeekBarChangeListener());
    }

    @Override
    public void onClick(View v) {
        if (v == btnAudioPlaymode) {
            // Handle clicks for btnAudioPlaymode
            changePlaymode();
        } else if (v == btnAudioPre) {
            try {
                if (mediaService != null) {
                    mediaService.pre();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            // Handle clicks for btnAudioPre
        } else if (v == btnAudioStartPause) {
            try {
                if (mediaService.isPlaying()) {
                    //暂停
                    mediaService.pause();


                    //按钮设置播放状态
                    btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_start_selector);
                } else {
                    //播放
                    mediaService.start();

                    //按钮设置暂停状态
                    btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_pause_selector);
                }
                // Handle clicks for btnAudioStartPause
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (v == btnAudioNext) {
            // Handle clicks for btnAudioNext
            try {
                if (mediaService != null) {
                    mediaService.next();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (v == btnLyrc) {
            // Handle clicks for btnLyrc
        }
    }

    private void changePlaymode() {

        try {
            int playmode = mediaService.getPlaymode();

            if (playmode == MediaService.REPEAT_ORDER) {
                playmode = MediaService.REPEAT_SINGLE;
            } else if (playmode == MediaService.REPEAT_SINGLE) {
                playmode = MediaService.REPEAT_ALL;
            } else if (playmode == MediaService.REPEAT_ALL) {
                playmode = MediaService.REPEAT_ORDER;
            } else {
                playmode = MediaService.REPEAT_ORDER;
            }
            //保持到Service的实例中
            mediaService.setPlaymode(playmode);

            showPlaymode();


        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void showPlaymode() {
        try {
            int playmode = mediaService.getPlaymode();//从服务里面

            if (playmode == MediaService.REPEAT_ORDER) {
                Toast.makeText(AudioPlayer.this, "顺序播放", Toast.LENGTH_SHORT).show();
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_normal_selector);
            } else if (playmode == MediaService.REPEAT_SINGLE) {
                Toast.makeText(AudioPlayer.this, "单曲播放", Toast.LENGTH_SHORT).show();
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_single_selector);
            } else if (playmode == MediaService.REPEAT_ALL) {
                Toast.makeText(AudioPlayer.this, "全部播放", Toast.LENGTH_SHORT).show();
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_all_selector);
            } else {
                Toast.makeText(AudioPlayer.this, "顺序播放", Toast.LENGTH_SHORT).show();
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_normal_selector);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (con != null) {
            unbindService(con);
            con = null;
        }

        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }

        handler.removeCallbacksAndMessages(null);


        EventBus.getDefault().unregister(this);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case PROGRESS://更新时间

                    //得到当前的进度

                    try {
                        int currentPosition = mediaService.getCurrentPosition();
                        int duration = mediaService.getDuration();

                        tvTime.setText(utils.stringForTime(currentPosition) + "/" + utils.stringForTime(duration));


                        //跟新进度
                        seekbarAudio.setProgress(currentPosition);

                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    handler.removeMessages(PROGRESS);
                    handler.sendEmptyMessageDelayed(PROGRESS, 1000);
                    break;
            }
        }
    };

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            setViewData();
            try {
                seekbarAudio.setMax(mediaService.getDuration());
            } catch (RemoteException e) {
                e.printStackTrace();
            }


            handler.sendEmptyMessage(PROGRESS);
            setPlayAndpauseState();
        }
    }

    private void setPlayAndpauseState() {
        try {
            if (mediaService != null) {
                if (mediaService.isPlaying()) {
                    //暂停状态
                    btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_pause_selector);
                } else {
                    //播放状态
                    btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_start_selector);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void setViewData() {
        try {
            tvName.setText(mediaService.getName());
            tvArtist.setText(mediaService.getArtist());


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class MyOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                try {
                    mediaService.seekTo(progress);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    private void showLyric() {
        LyricUtils lyricUtils = new LyricUtils();
        try {
            String path = mediaService.getAudioPath();//mnt/sdcard/audio/beijing.mp3
            path = path.substring(0, path.indexOf("."));////mnt/sdcard/audio/beijing;

            File file = new File(path + ".lrc");//////mnt/sdcard/audio/beijing.lrc
            if (!file.exists()) {
                file = new File(path + ".txt");//////mnt/sdcard/audio/beijing.txt
            }

            lyricUtils.readLyricFile(file);//传文件进入解析歌词工具类

            //把解析好的歌词传入显示歌词的控件上
            lyric_showview.setLyrics(lyricUtils.getLyrics());


        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (lyricUtils.isExistLyric()) {
            handler.sendEmptyMessage(SHOW_LYRIC);
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode== KeyEvent.KEYCODE_BACK)
        {
            Intent intent=new Intent(this,MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}