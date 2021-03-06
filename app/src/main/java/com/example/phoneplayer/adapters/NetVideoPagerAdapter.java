package com.example.phoneplayer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.phoneplayer.R;
import com.example.phoneplayer.beans.MediaItem;
import com.example.phoneplayer.utils.Utils;

import java.util.List;

public class NetVideoPagerAdapter extends BaseAdapter {
    private List<MediaItem> list;
    private LayoutInflater inflater;
    private Context context;


    public NetVideoPagerAdapter(Context context, List<MediaItem> list) {
        this.inflater = LayoutInflater.from(context);
        this.list = list;
        this.context = context;
    }
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView==null)
        {
            viewHolder=new ViewHolder();
            convertView=inflater.inflate(R.layout.item_netvideo_pager,null);
            viewHolder.iv_icon=convertView.findViewById(R.id.iv_icon);
            viewHolder.tv_name=convertView.findViewById(R.id.tv_name);
            viewHolder.tv_desc=convertView.findViewById(R.id.tv_desc);
            convertView.setTag(viewHolder);

        }
        else {
            viewHolder= (ViewHolder) convertView.getTag();
        }
        //根据位置得到对应的数据
        MediaItem mediaItem = list.get(position);
        viewHolder.tv_name.setText(mediaItem.getName());
        viewHolder.tv_desc.setText(mediaItem.getDesc());

        //使用Glide请求图片
        Glide.with(context).load(mediaItem.getImageUrl())
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
