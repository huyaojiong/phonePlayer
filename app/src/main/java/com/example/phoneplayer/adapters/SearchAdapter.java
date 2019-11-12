package com.example.phoneplayer.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.phoneplayer.R;
import com.example.phoneplayer.beans.SearchBean;

import java.util.List;

/**
 * 作者：杨光福 on 2016/5/28 15:51
 * 微信：yangguangfu520
 * QQ号：541433511
 * 作用：xxxx
 */
public class SearchAdapter extends BaseAdapter {

    private final Context context;
    private final List<SearchBean.ItemsEntity> mediaItems;

    public SearchAdapter(Context context, List<SearchBean.ItemsEntity>  mediaItems){
        this.context = context;
        this.mediaItems = mediaItems;
    }

    @Override
    public int getCount() {
        return mediaItems.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_netvideo_pager, null);
            viewHolder = new ViewHolder();
            viewHolder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
            viewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            viewHolder.tv_desc = (TextView) convertView.findViewById(R.id.tv_desc);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //根据位置得到对应的数据
        SearchBean.ItemsEntity mediaItem = mediaItems.get(position);
        viewHolder.tv_name.setText(mediaItem.getItemTitle());
        viewHolder.tv_desc.setText(mediaItem.getKeywords());

        //请求图片：XUtils3或者Glide
//            x.image().bind(viewHolder.iv_icon,mediaItem.getImageUrl());
        //使用Glide请求图片
        SearchBean.ItemsEntity.ItemImageEntity itemImageEntity = mediaItem.getItemImage();
        Glide.with(context).load(itemImageEntity.getImgUrl1())
                .diskCacheStrategy(DiskCacheStrategy.ALL)//图片的缓存
                .placeholder(R.drawable.vedio_default)//加载过程中的图片
                .error(R.drawable.vedio_default)//加载失败的时候显示的图片
                .into(viewHolder.iv_icon);//请求成功后把图片设置到的控件


        return convertView;
    }


    static class ViewHolder {
        ImageView iv_icon;
        TextView tv_name;
        TextView tv_desc;
    }
}