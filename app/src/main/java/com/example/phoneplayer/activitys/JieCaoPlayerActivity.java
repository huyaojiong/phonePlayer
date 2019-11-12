package com.example.phoneplayer.activitys;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.example.phoneplayer.R;
import com.example.phoneplayer.beans.MediaItem;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;



@ContentView(R.layout.activity_jie_cao_player)
public class JieCaoPlayerActivity extends AppCompatActivity {

    @ViewInject(R.id.jz_Player)
    private JzvdStd jzvdStd;
    private Uri uri;
    private ArrayList<MediaItem> mediaItems;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        x.view().inject(this);
        initData();
    }

    private void initData() {

        uri = getIntent().getData();//得到一个地址：文件浏览器，浏览器，相册
        mediaItems = (ArrayList<MediaItem>) getIntent().getSerializableExtra("videolist");
        position = getIntent().getIntExtra("position", 0);//列表中的位置
        MediaItem bean = mediaItems.get(position);
        String path = bean.getData(); // 视频地址
        String name = bean.getName(); // 视频名称
        jzvdStd.setUp(path, name);
        jzvdStd.startVideo();  // 开始自动播放

        // 视频的回退按钮设置点击事件
        jzvdStd.backButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                closePalyer();

            }
        });


    }

    private void closePalyer() {
        //Jzvd.releaseAllVideos(); // 释放视频
        startActivity(new Intent(JieCaoPlayerActivity.this, MainActivity.class)); // 跳转到上层界面
        JieCaoPlayerActivity.this.finish(); // 结束当前界面
    }


    @Override
    public void onBackPressed() { //"全屏竖屏切换的时候继续播放"
        if (Jzvd.backPress()) {
            return;
        }
        super.onBackPressed();
    }


    @Override
    protected void onPause() {
        super.onPause();
        Jzvd.releaseAllVideos();
    }

    //"activity销毁的时候释放资源，播放器停止播放"
    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            jzvdStd.releaseAllVideos();
        } catch (Exception e) {
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            closePalyer();
        }
        return super.onKeyUp(keyCode, event);
    }


}
