package com.example.phoneplayer.activitys;


import android.os.Bundle;

import android.widget.RadioGroup;
import android.widget.Toast;


import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.phoneplayer.R;
import com.example.phoneplayer.base.BasePager;
import com.example.phoneplayer.base.ReplaceFragment;
import com.example.phoneplayer.pagers.AudioPager;
import com.example.phoneplayer.pagers.NetAudioPager;
import com.example.phoneplayer.pagers.NetVideoPager;
import com.example.phoneplayer.pagers.VideoPager;

import org.xutils.view.annotation.ContentView;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import fm.jiecao.jcvideoplayer_lib.JCVideoPlayerStandard;

import static android.widget.RadioGroup.*;

@ContentView(R.layout.activity_main)
public class MainActivity extends FragmentActivity implements OnCheckedChangeListener {
    @ViewInject(R.id.rg_main)
    private RadioGroup rg_main;

    private List<BasePager> basePagers;
    private int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);
        initData();
    }

    private void initData() {
        basePagers = new ArrayList<>();
        basePagers.add(new VideoPager(this));
        basePagers.add(new AudioPager(this));
        basePagers.add(new NetVideoPager(this));
        basePagers.add(new NetAudioPager(this));

        rg_main.setOnCheckedChangeListener(this);

        rg_main.check(R.id.rb_video);
    }


    private BasePager getBasePager() {
        BasePager basePager = basePagers.get(position);
        if (basePager != null && !basePager.isInitData) {
            basePager.isInitData = true;
            basePager.initData();
        }
        return basePager;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            default:
                position = 0;
                break;
            case R.id.rb_video:
                position = 0;
                break;
            case R.id.rb_audio:
                position = 1;
                break;
            case R.id.rb_netvideo:
                position = 2;
                break;
            case R.id.rb_netaudio:
                position = 3;
                break;

        }
        setFragment();
    }

    private void setFragment() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fl_main, new ReplaceFragment(getBasePager()));
        ft.commit();
    }

    private long startTime;

    @Override
    public void onBackPressed() {
        if (position != 0) {
            rg_main.check(R.id.rb_video);
            return;
        }
        if (System.currentTimeMillis() - startTime > 2000) {
            startTime = System.currentTimeMillis();
            Toast.makeText(MainActivity.this, "再点一次退出", Toast.LENGTH_SHORT).show();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        JCVideoPlayerStandard.releaseAllVideos();
    }

    @Override
    public void onDestroy() { //"activity销毁的时候释放资源，播放器停止播放"
        super.onDestroy();
        try {
            JCVideoPlayerStandard.releaseAllVideos();
        } catch (Exception e) {
        }
    }
}
