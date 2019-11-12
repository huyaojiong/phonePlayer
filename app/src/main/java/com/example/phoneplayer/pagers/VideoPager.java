package com.example.phoneplayer.pagers;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.phoneplayer.R;
import com.example.phoneplayer.activitys.JieCaoPlayerActivity;
import com.example.phoneplayer.activitys.SystemVideoPlayer;
import com.example.phoneplayer.adapters.VideoPagerAdapter;
import com.example.phoneplayer.base.BasePager;
import com.example.phoneplayer.beans.MediaItem;


import java.util.ArrayList;



public class VideoPager extends BasePager {

    private ListView lv_video_pager;
    private TextView tv_nomedia;
    private ProgressBar pb_loading;
    private ArrayList<MediaItem> mediaItems;

    public VideoPager(Context context) {
        super(context);
    }

    @Override
    public View initView() {

        View view = View.inflate(context, R.layout.video_pager, null);
        lv_video_pager = (ListView) view.findViewById(R.id.lv_video_pager);
        tv_nomedia = (TextView) view.findViewById(R.id.tv_nomedia);
        pb_loading = (ProgressBar) view.findViewById(R.id.pb_loading);
        //设置点击事件
        lv_video_pager.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MediaItem mediaItem = mediaItems.get(position);
                //Intent intent = new Intent(context, JieCaoPlayerActivity.class);
                //intent.putExtra("media", mediaItem);
                Intent intent = new Intent(context, SystemVideoPlayer.class);
                //intent.setDataAndType(Uri.parse(mediaItem.getData()),"video/*");

                //传视频列表
                Bundle bundle = new Bundle();
                bundle.putSerializable("videolist", mediaItems);
                intent.putExtras(bundle);
                intent.putExtra("position", position);
                context.startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void initData() {

        super.initData();
        System.out.println("本地视频数据初始化了。。。");
        getData();
    }

    private void getData() {
        new Thread() {
            @Override
            public void run() {
                super.run();

                mediaItems = new ArrayList<>();
                ContentResolver contentResolver = context.getContentResolver();
                Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                String[] objects = {
                        MediaStore.Video.Media.DISPLAY_NAME,//在Sdcard显示的名称
                        MediaStore.Video.Media.DURATION,//视频的长度
                        MediaStore.Video.Media.SIZE,//视频文件大小
                        MediaStore.Video.Media.DATA,//视频的绝对地址

                };
                Cursor cursor = contentResolver.query(uri, objects, null, null, MediaStore.Video.Media.DISPLAY_NAME + " asc");
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


                        //把视频添加到列表中
                        mediaItems.add(mediaItem);
                    }
                    cursor.close();
                }
                handler.sendEmptyMessage(0);
            }
        }.start();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //主线程
            if (mediaItems != null && mediaItems.size() > 0) {
                tv_nomedia.setVisibility(View.GONE);
                pb_loading.setVisibility(View.GONE);

                //设置适配器
                lv_video_pager.setAdapter(new VideoPagerAdapter(context, mediaItems));
            } else {
                tv_nomedia.setVisibility(View.VISIBLE);
                pb_loading.setVisibility(View.GONE);
            }

        }
    };


}
