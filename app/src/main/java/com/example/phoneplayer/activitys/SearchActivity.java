package com.example.phoneplayer.activitys;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.phoneplayer.R;
import com.example.phoneplayer.adapters.SearchAdapter;
import com.example.phoneplayer.beans.SearchBean;
import com.example.phoneplayer.utils.Content;
import com.example.phoneplayer.utils.JsonParser;
import com.example.phoneplayer.utils.JsonUitl;



import org.xutils.common.Callback;
import org.xutils.common.util.LogUtil;
import org.xutils.http.RequestParams;
import org.xutils.x;


import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class SearchActivity extends Activity {

    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    /**
     * 搜索的集合数据
     */
    private List<SearchBean.ItemsEntity> items;
    private EditText etSearch;
    private ImageView ivInput;
    private TextView tvSearch;
    private ListView lvResult;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViews();
    }

    private void findViews() {
        setContentView(R.layout.activity_search);
        etSearch = (EditText) findViewById(R.id.et_search);
        ivInput = (ImageView) findViewById(R.id.iv_input);
        tvSearch = (TextView) findViewById(R.id.tv_search);
        lvResult = (ListView) findViewById(R.id.lv_result);

        //设置点击事件
        MyOnClickListener myOnClickListener = new MyOnClickListener();
        ivInput.setOnClickListener(myOnClickListener);
        tvSearch.setOnClickListener(myOnClickListener);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Toast.makeText(SearchActivity.this,s.toString(),Toast.LENGTH_SHORT).show();

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    class MyOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_search://搜索
//                    Toast.makeText(SearchActivity.this, "搜索", Toast.LENGTH_SHORT).show();
                    startSearch();
                    break;
                case R.id.iv_input://语音输入
                   Toast.makeText(SearchActivity.this, "语音输入", Toast.LENGTH_SHORT).show();
                    //startVoice();
                    break;
            }
        }
    }




    private void startSearch() {

        String text = etSearch.getText().toString().trim();      //阿福->
        if (!TextUtils.isEmpty(text)) {
            try {
                text = URLEncoder.encode(text, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            String url = Content.SEARCH_URL + text;
            getDataFromNet(url);


        } else {
            Toast.makeText(this, "你没有输入您要搜索的内容", Toast.LENGTH_SHORT).show();
        }
    }

    private void getDataFromNet(String url) {
        RequestParams params = new RequestParams(url);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.e("",result);
                processData(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.e("tag","联网失败=="+ex.getMessage());

            }

            @Override
            public void onCancelled(CancelledException cex) {
                LogUtil.e("onCancelled=="+cex.getMessage());
            }

            @Override
            public void onFinished() {
                LogUtil.e("onFinished==");
            }
        });
    }

    /**
     * 解析和显示数据
     * @param json
     */
    private void processData(String json) {
        SearchBean searchBean = (SearchBean) JsonUitl.stringToObject(json,SearchBean.class);
        //有数据了
        items   = searchBean.getItems();

        if(items != null && items.size()>0){

            lvResult.setAdapter(new SearchAdapter(this,items));

        }else{
            Toast.makeText(SearchActivity.this, "没有搜索到数据", Toast.LENGTH_SHORT).show();
        }

    }
}
