package com.example.phoneplayer.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.phoneplayer.IMediaService;
import com.example.phoneplayer.R;
import com.example.phoneplayer.activitys.AudioPlayer;
import com.example.phoneplayer.beans.MediaItem;
import com.example.phoneplayer.pagers.AudioPager;
import com.example.phoneplayer.utils.CacheUtils;
import com.example.phoneplayer.utils.Content;
import com.example.phoneplayer.utils.Utils;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;


public class MediaService extends Service {


    /**
     * 音频列表
     */
    private ArrayList<MediaItem> mediaItems;
    /**
     * 当前列表的播放位置
     */
    private int position;
    /**
     * 一首音乐的信息
     */
    private MediaItem mediaItem;
    /**
     * 播放器
     */
    private MediaPlayer mediaplayer;
    //三种播放模式
    public static final int REPEAT_ORDER = 1;
    public static final int REPEAT_SINGLE = 2;
    public static final int REPEAT_ALL = 3;
    private int playmode = REPEAT_ORDER;

    //通知管理
    private NotificationManager manager;




    IMediaService.Stub stub = new IMediaService.Stub() {
        MediaService service = MediaService.this;

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public void openAudio(int position) throws RemoteException {
            service.openAudio(position);
        }

        @Override
        public void start() throws RemoteException {
            service.start();

        }

        @Override
        public void pause() throws RemoteException {
            service.pause();

        }

        @Override
        public void next() throws RemoteException {
            service.next();
        }

        @Override
        public void pre() throws RemoteException {
            service.pre();
        }

        @Override
        public int getPlaymode() throws RemoteException {
            return service.getPlaymode();
        }

        @Override
        public void setPlaymode(int playmode) throws RemoteException {
            service.setPlaymode(playmode);
        }

        @Override
        public int getCurrentPosition() throws RemoteException {
            return service.getCurrentPosition();
        }

        @Override
        public int getDuration() throws RemoteException {
            return service.getDuration();
        }

        @Override
        public String getName() throws RemoteException {
            return service.getName();
        }

        @Override
        public String getArtist() throws RemoteException {
            return service.getArtist();
        }

        @Override
        public void seekTo(int seekto) throws RemoteException {
            service.seekTo(seekto);
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            return service.isPlaying();
        }

        @Override
        public void notifyChange(String action) throws RemoteException {
            service.notifyChange(action);
        }

        @Override
        public String getAudioPath() throws RemoteException {
            return service.getAudioPath();
        }

        @Override
        public int getAudioSessionId() throws RemoteException {
            return service.getAudioSessionId();
        }
    };

    private int getAudioSessionId() {
        return mediaplayer.getAudioSessionId();
    }

    /**
     * 音频播放的绝对路径
     *
     * @return
     */
    private String getAudioPath() {
        return mediaItem.getData();
    }





    public MediaService() {

    }


    @Override
    public void onCreate() {
        super.onCreate();

        //拿取本地数据
        getDataFromLocal();
        //拿到储存模式
        playmode = CacheUtils.getInt(this, "musicmode");
    }

    private void getDataFromLocal() {
        new Thread() {
            @Override
            public void run() {
                super.run();

                mediaItems = new ArrayList<MediaItem>();
                ContentResolver contentResolver = getContentResolver();
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] objects = {
                        MediaStore.Audio.Media.DISPLAY_NAME,//在Sdcard显示的名称
                        MediaStore.Audio.Media.DURATION,//视频的长度
                        MediaStore.Audio.Media.SIZE,//视频文件大小
                        MediaStore.Audio.Media.DATA,//视频的绝对地址
                        MediaStore.Audio.Media.ARTIST//艺术家
                };
                Cursor cursor = contentResolver.query(uri, objects, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {

                        MediaItem mediaItem = new MediaItem();
                        String name = cursor.getString(0);
                        mediaItem.setName(name);

                        long duration = cursor.getLong(1);
                        mediaItem.setDuration(duration);

                        long size = cursor.getLong(2);
                        mediaItem.setSize(size);

                        String data = cursor.getString(3);
                        mediaItem.setData(data);

                        String artist = cursor.getString(4);
                        mediaItem.setArtist(artist);

                        //把视频添加到列表中
                        mediaItems.add(mediaItem);
                    }


                    cursor.close();

                }


            }
        }.start();
    }

    @Override
    public IBinder onBind(Intent intent) {

        return stub;
    }


    /**
     * 根据位置打开音乐
     *
     * @param position
     */
    private void openAudio(int position) {
        this.position = position;

        if (mediaItems != null && mediaItems.size() > 0) {

            mediaItem = mediaItems.get(position);

            //把上一次或者正在播放的给释放掉
            if (mediaplayer != null) {
                mediaplayer.reset();
                //mediaplayer.release();
                mediaplayer = null;
            }

            try {
                mediaplayer = new MediaPlayer();
                //设置准备好的监听
                mediaplayer.setOnPreparedListener(new MyOnPreparedListener());
                mediaplayer.setOnErrorListener(new MyOnErrorListener());
                mediaplayer.setOnCompletionListener(new MyOnCompletionListener());
                mediaplayer.setDataSource(mediaItem.getData());
                mediaplayer.prepareAsync();//本地资源和网络资源都行
//                mediaplayer.prepare();//本地资源


                if (playmode == MediaService.REPEAT_SINGLE) {
                    mediaplayer.setLooping(true);
                } else {
                    mediaplayer.setLooping(false);
                }


            } catch (IOException e) {
                e.printStackTrace();
            }


        } else {
            //数据还没有加载好
            Toast.makeText(MediaService.this, "数据还没有加载好呢！", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 播放音乐
     */
    private void start() {
        mediaplayer.start();
        setNotification();

    }

    /**
     * 暂停音乐
     */
    private void pause() {
        mediaplayer.pause();
        //通知消失掉
        manager.cancel(8);

    }

    /**
     * 下一首
     */
    private void next() {
        setNextPosition();
        openNextPosition();
    }


    /**
     * 上一首
     */
    private void pre() {
        setPrePosition();
        openPrePosition();
    }


    /**
     * 得到播放模式
     *
     * @return
     */
    private int getPlaymode() {
        return playmode;
    }

    /**
     * 设置播放模式
     *
     * @param playmode
     */
    private void setPlaymode(int playmode) {
        this.playmode = playmode;
        if (playmode == MediaService.REPEAT_SINGLE) {
            mediaplayer.setLooping(true);
        } else {
            mediaplayer.setLooping(false);
        }
        CacheUtils.putInt(this, "musicmode", playmode);
    }

    /**
     * 得到当前播放进度
     * @return
     */
    private int getCurrentPosition() {
        return mediaplayer.getCurrentPosition();
    }

    /**
     * 得到当前的总时长
     * @return
     */
    private int getDuration() {
        if (mediaplayer != null) {
            return mediaplayer.getDuration();
        } else {
            return 0;
        }
    }


    /**
     * 得到歌曲的名称
     * @return
     */
    private String getName() {
        return mediaItem.getName();
    }


    /**
     * 得到演唱者
     * @return
     */
    private String getArtist() {
        return mediaItem.getArtist();
    }

    /**
     * 音频的拖动
     * @param seekto
     */
    private void seekTo(int seekto) {
        mediaplayer.seekTo(seekto);
    }

    private boolean isPlaying() {
        return mediaplayer.isPlaying();
    }


    private class MyOnPreparedListener implements MediaPlayer.OnPreparedListener {

        @Override
        public void onPrepared(MediaPlayer mp) {
            start();
            notifyChange(Content.OPEN_AUDIO);
        }
    }

    private void notifyChange(String Action) {
        // Intent intent=new Intent();
        //intent.setAction(Action);
        // sendBroadcast(intent);

        EventBus.getDefault().post(new MediaItem());
    }

    private class MyOnErrorListener implements MediaPlayer.OnErrorListener {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            next();
            return true;
        }
    }

    private class MyOnCompletionListener implements MediaPlayer.OnCompletionListener {
        @Override
        public void onCompletion(MediaPlayer mp) {
            next();
        }
    }

    private void setNextPosition() {
        int playmode = getPlaymode();

        if (playmode == MediaService.REPEAT_ORDER) {
            position++;

        } else if (playmode == MediaService.REPEAT_SINGLE) {
            position++;
        } else if (playmode == MediaService.REPEAT_ALL) {
            position++;
            if (position > mediaItems.size() - 1) {
                position = 0;
            }
        } else {
            position++;
        }
    }

    private void openNextPosition() {
        int playmode = getPlaymode();

        if (playmode == MediaService.REPEAT_ORDER) {

            if (position < mediaItems.size()) {
                openAudio(position);
            } else {
                position = mediaItems.size() - 1;
            }

        } else if (playmode == MediaService.REPEAT_SINGLE) {

            if (position < mediaItems.size()) {
                openAudio(position);

            }

        } else if (playmode == MediaService.REPEAT_ALL) {
            openAudio(position);
        } else {
            if (position < mediaItems.size()) {
                openAudio(position);
            } else {
                position = mediaItems.size() - 1;
            }
        }
    }

    private void openPrePosition() {
        int playmode = getPlaymode();

        if (playmode == MediaService.REPEAT_ORDER) {

            if (position >= 0) {
                openAudio(position);
            } else {
                position = 0;
            }

        } else if (playmode == MediaService.REPEAT_SINGLE) {
            if (position >= 0) {
                openAudio(position);

            }

        } else if (playmode == MediaService.REPEAT_ALL) {
            openAudio(position);
        } else {
            if (position >= 0) {
                openAudio(position);
            } else {
                position = 0;
            }
        }
    }

    private void setPrePosition() {
        int playmode = getPlaymode();

        if (playmode == MediaService.REPEAT_ORDER) {
            position--;

        } else if (playmode == MediaService.REPEAT_SINGLE) {
            position--;
        } else if (playmode == MediaService.REPEAT_ALL) {
            position--;
            if (position < 0) {
                position = mediaItems.size() - 1;
            }
        } else {
            position--;
        }
    }

    private void setNotification() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            //弹出通知-点击的时候进入音乐播放器页面
            manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            //这个意图表示点击后调回音乐播放
            Intent intent = new Intent(this, AudioPlayer.class);
            //解决bug冲突
            intent.putExtra("Notification", true);//从状态栏进入音乐播放页面
           // Intent appIntent = new Intent(Intent.ACTION_MAIN);
            //appIntent.addCategory(Intent.CATEGORY_LAUNCHER);
           // appIntent.setComponent(new ComponentName(this.getPackageName(), AudioPlayer.class.getPackage().getName() + "." + AudioPlayer.class.getName()));
           // appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            intent.putExtra("Notification", true);//从状态栏进入音乐播放页面


            PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            //pendingintent表示携带数据的意图
            Notification bifurcation = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.notification_music_playing)
                    .setContentTitle("321音乐")
                    .setContentText("正在播放:" + getName())
                    .setContentIntent(pi)
                    .build();
            bifurcation.flags = Notification.FLAG_ONGOING_EVENT;//点击不消失
            manager.notify(8, bifurcation);
        }
    }




}

