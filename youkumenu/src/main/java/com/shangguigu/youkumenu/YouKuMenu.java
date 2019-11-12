package com.shangguigu.youkumenu;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class YouKuMenu extends RelativeLayout implements View.OnClickListener {
    private ImageView icon_home;
    private ImageView icon_menu;
    private RelativeLayout level1;
    private RelativeLayout level2;
    private RelativeLayout level3;
    private Context context;
    /**
     * 是否显示第三圆环
     * true:显示
     * false隐藏
     */
    private boolean isShowLevel3 = true;

    /**
     * 是否显示第二圆环
     * true:显示
     * false隐藏
     */
    private boolean isShowLevel2 = true;


    /**
     * 是否显示第一圆环
     * true:显示
     * false隐藏
     */
    private boolean isShowLevel1 = true;

    public YouKuMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        //setFocusableInTouchMode(true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initView();
    }

    private void initView() {
        icon_home = (ImageView) findViewById(R.id.icon_home);
        icon_menu = (ImageView) findViewById(R.id.icon_menu);
        level1 = (RelativeLayout) findViewById(R.id.level1);
        level2 = (RelativeLayout) findViewById(R.id.level2);
        level3 = (RelativeLayout) findViewById(R.id.level3);


        //设置点击事件
        icon_home.setOnClickListener(this);
        icon_menu.setOnClickListener(this);
        level1.setOnClickListener(this);
        level2.setOnClickListener(this);
        level3.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.icon_home) {
            if (isShowLevel2) {
                isShowLevel2 = false;
                Tools.hideView(level2);
                if (isShowLevel3) {
                    isShowLevel3 = false;
                    Tools.hideView(level3, 200);
                }
            } else {
                isShowLevel2 = true;
                Tools.showView(level2);
            }
        } else if (id == R.id.icon_menu) {
            if (isShowLevel3) {
                //隐藏
                isShowLevel3 = false;
                Tools.hideView(level3);
            } else {
                //显示
                isShowLevel3 = true;
                Tools.showView(level3);
            }
        } else if (id == R.id.level1) {
            Toast.makeText(context, "level1", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.level2) {
            Toast.makeText(context, "level2", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.level3) {
            Toast.makeText(context, "level3", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_MENU) {
            //如果一级，二级，三级菜单是显示的就全部隐藏
            if (isShowLevel1) {
                isShowLevel1 = false;
                Tools.hideView(level1);
                if (isShowLevel2) {
                    //隐藏二级菜单
                    isShowLevel2 = false;
                    Tools.hideView(level2, 200);
                    if (isShowLevel3) {
                        //隐藏三级菜单
                        isShowLevel3 = false;
                        Tools.hideView(level3, 400);
                    }
                }
            } else {
                //如果一级，二级菜单隐藏，就显示
                //显示一级菜单
                isShowLevel1 = true;
                Tools.showView(level1);

                //显示二级菜单
                isShowLevel2 = true;
                Tools.showView(level2, 200);
            }
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }


}
