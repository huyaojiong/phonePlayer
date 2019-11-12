package com.example.phoneplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;


/**
 * 作者：杨光福 on 2016/5/25 11:27
 * 微信：yangguangfu520
 * QQ号：541433511
 * 作用：缓存工具
 */
public class CacheUtils {
    /**
     * 保持播放历史记录
     * @param context
     * @param key
     * @param values
     */
    public static void putInt(Context context, String key, int values) {
        SharedPreferences sp = context.getSharedPreferences("atguigu",Context.MODE_PRIVATE);
        sp.edit().putInt(key,values).commit();
    }


    /**
     * 得到播放记录
     */
    public static int getInt(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences("atguigu",Context.MODE_PRIVATE);
        return sp.getInt(key,0);
    }


    /**
     * 保持软件参数
     * @param context
     * @param key
     * @param values
     */
    public static void putString(Context context, String key, String values) {
        SharedPreferences sp = context.getSharedPreferences("atguigu",Context.MODE_PRIVATE);
        sp.edit().putString(key,values).commit();
    }

    /**
     * 获取软件缓存的文本信息
     * @param context
     * @param key
     * @return
     */
    public static String getString(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences("atguigu",Context.MODE_PRIVATE);
        return sp.getString(key,"");
    }
}
