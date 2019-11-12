package com.example.phoneplayer.pagers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.phoneplayer.R;
import com.example.phoneplayer.activitys.SystemVideoPlayer;
import com.example.phoneplayer.adapters.NetVideoPagerAdapter;

import com.example.phoneplayer.base.BasePager;
import com.example.phoneplayer.beans.MediaItem;
import com.example.phoneplayer.beans.NetMediaItem;
import com.example.phoneplayer.utils.CacheUtils;
import com.example.phoneplayer.utils.Content;
import com.example.phoneplayer.utils.JsonUitl;
import com.example.phoneplayer.utils.Utils;
import com.example.phoneplayer.views.XListView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.common.util.LogUtil;
import org.xutils.http.RequestParams;
import org.xutils.x;


import java.util.ArrayList;
import java.util.List;

public class NetVideoPager extends BasePager {
    private XListView lv_netvideo_pager;
    private TextView tv_nomedia;
    private ProgressBar pb_loading;
    private ArrayList<MediaItem> mediaItems;
    private List<NetMediaItem> netMediaItems;
    private NetVideoPagerAdapter adapter;
    private Utils utils;

    public NetVideoPager(Context context) {
        super(context);
        utils = new Utils();
    }

    @Override
    public View initView() {

        View view = View.inflate(context, R.layout.netvideo_pager, null);
        lv_netvideo_pager = (XListView) view.findViewById(R.id.lv_netvideo_pager);
        tv_nomedia = (TextView) view.findViewById(R.id.tv_nomedia);
        pb_loading = (ProgressBar) view.findViewById(R.id.pb_loading);
        //设置点击事件
        lv_netvideo_pager.setOnItemClickListener(new MyOnItemClickListener());

        lv_netvideo_pager.setPullLoadEnable(true);
        lv_netvideo_pager.setPullRefreshEnable(true);

        lv_netvideo_pager.setXListViewListener(new MyIXListViewListener());
        return view;
    }

    class MyOnItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //传视频列表
            Intent intent = new Intent(context, SystemVideoPlayer.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("videolist", mediaItems);
            intent.putExtras(bundle);
            intent.putExtra("position", position - 1);
            context.startActivity(intent);
        }
    }

    class MyIXListViewListener implements XListView.IXListViewListener {
        @Override
        public void onRefresh() {
            getDataFromNet();
            onLoad();
        }

        @Override
        public void onLoadMore() {

            getMoreDataFromNet();
        }
    }


    @Override
    public void initData() {

        super.initData();
        //textView.setText("网络视频");

        getDataFromNet();

    }

    /**
     * 获取联网数据
     */
    private void getDataFromNet() {
        RequestParams params = new RequestParams(Content.NET_VIDEO_URL);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {

                CacheUtils.putString(context, Content.NET_VIDEO_URL,result);
                processData(result);

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {

            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });

    }


    private void getMoreDataFromNet() {
        RequestParams params = new RequestParams(Content.NET_VIDEO_URL);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                LogUtil.e("联网请求成功==" + result);
                CacheUtils.putString(context, Content.NET_VIDEO_URL, result);
                parseMoreData(result);
                adapter.notifyDataSetChanged();
                onLoad();
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                LogUtil.e("联网请求失败==");
            }

            @Override
            public void onCancelled(CancelledException cex) {
                LogUtil.e("onCancelled==");
            }

            @Override
            public void onFinished() {
                LogUtil.e("onFinished==");
            }
        });

    }

    private void processData(String json) {
        mediaItems = new ArrayList<>();
        try {
            String jsonArray = new JSONObject(json).optJSONArray("trailers").toString();
            netMediaItems = JsonUitl.stringToList(jsonArray, NetMediaItem.class);
            for (int i = 0; i < netMediaItems.size(); i++) {
                MediaItem item = new MediaItem();
                item.setName(netMediaItems.get(i).getMovieName());
                item.setData(netMediaItems.get(i).getUrl());
                item.setDesc(netMediaItems.get(i).getVideoTitle());
                item.setImageUrl(netMediaItems.get(i).getCoverImg());

                mediaItems.add(item);
            }
        } catch (Exception e) {

        }

        //主线程
        if (mediaItems != null && mediaItems.size() > 0) {
            tv_nomedia.setVisibility(View.GONE);
            adapter=new NetVideoPagerAdapter(context, mediaItems);
            //设置适配器
            lv_netvideo_pager.setAdapter(adapter);
        } else {
            tv_nomedia.setVisibility(View.VISIBLE);

        }
        pb_loading.setVisibility(View.GONE);
    }

    private void onLoad() {
        lv_netvideo_pager.stopRefresh();
        lv_netvideo_pager.stopLoadMore();
        lv_netvideo_pager.setRefreshTime(utils.getSysteTime());
    }

    private void parseMoreData(String json) {
        try {
            JSONObject object = new JSONObject(json);
            JSONArray jsonArray = object.optJSONArray("trailers");
//            object.getJSONArray("trailers");//不好-
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jsonObject = (JSONObject) jsonArray.get(i);

                if (jsonObject != null) {

                    MediaItem mediaItem = new MediaItem();
                    mediaItems.add(mediaItem);//添加到集合中--可以

                    String coverImg = jsonObject.getString("coverImg");
                    mediaItem.setImageUrl(coverImg);

                    String url = jsonObject.optString("url");
                    mediaItem.setData(url);

                    String movieName = jsonObject.optString("movieName");
                    mediaItem.setName(movieName);

                    String videoTitle = jsonObject.optString("videoTitle");
                    mediaItem.setDesc(videoTitle);


                }


            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
