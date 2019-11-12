package com.example.phoneplayer.pagers;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;

import android.widget.Toast;

import com.example.phoneplayer.R;
import com.example.phoneplayer.adapters.NetAudioPagerAdapter;
import com.example.phoneplayer.base.BasePager;
import com.example.phoneplayer.beans.NetAudioBean;
import com.example.phoneplayer.utils.CacheUtils;
import com.example.phoneplayer.utils.Content;
import com.google.gson.Gson;

import org.xutils.common.Callback;
import org.xutils.common.util.LogUtil;
import org.xutils.http.RequestParams;
import org.xutils.x;


import java.util.List;

public class NetAudioPager extends BasePager {

    private ListView listview;
    /**
     * 数据集合
     */
    private List<NetAudioBean.ListEntity> lists;

    public NetAudioPager(Context context) {
        super(context);
    }

    @Override
    public View initView() {
        View view = View.inflate(context, R.layout.net_audio_pager, null);
        listview = (ListView) view.findViewById(R.id.listView);
        return view;
    }

    @Override
    public void initData() {
        super.initData();
        System.out.println("网络音频数据初始化了。。。");
        String saveJson = CacheUtils.getString(context, Content.ALL_RES_URL);
        if (!TextUtils.isEmpty(saveJson)) {
            processData(saveJson);
        }
        getDataFromNet();
    }

    private void getDataFromNet() {
        RequestParams params = new RequestParams(Content.ALL_RES_URL);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                LogUtil.e("result===" + result);
                processData(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                LogUtil.e("onError===" + ex.getMessage());
            }

            @Override
            public void onCancelled(CancelledException cex) {
                LogUtil.e("onCancelled===" + cex.getMessage());
            }

            @Override
            public void onFinished() {
                LogUtil.e("onFinished===");
            }
        });
    }

    /**
     * 解析和显示数据
     *
     * @param json
     */
    private void processData(String json) {

        NetAudioBean netAudioBean = parseJson(json);
        lists = netAudioBean.getList();

        if(lists != null && lists.size()>0){

            listview.setAdapter(new NetAudioPagerAdapter(context,lists));

        }else{
            Toast.makeText(context, "没有得到数据", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * 解析数据
     *
     * @param json
     * @return
     */
    private NetAudioBean parseJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, NetAudioBean.class);
    }
}
