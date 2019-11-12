package com.example.phoneplayer.base;

import android.content.Context;
import android.view.View;

public abstract class BasePager {

    public boolean isInitData = false;
    public Context context;
    public View rootView;

    public BasePager(Context context) {
        this.context = context;
        rootView = initView();
        isInitData = false;

    }

    public abstract View initView();

    public void initData() {

    }


}
