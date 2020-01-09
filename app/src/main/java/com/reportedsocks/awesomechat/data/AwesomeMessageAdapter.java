package com.reportedsocks.awesomechat.data;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.reportedsocks.awesomechat.R;
import com.reportedsocks.awesomechat.model.AwesomeMessage;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AwesomeMessageAdapter extends ArrayAdapter<AwesomeMessage> {

    private List<AwesomeMessage> messages;
    private Activity activity;

    public AwesomeMessageAdapter(@NonNull Activity context, int resource, @NonNull ArrayList<AwesomeMessage> objects) {
        super(context, resource, objects);

        this.messages = objects;
        this.activity = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {



        ViewHolder viewHolder;

        LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        AwesomeMessage message = getItem(position);
        int layoutResource = 0;
        int viewType = getItemViewType(position);
        if(viewType == 0){
            layoutResource = R.layout.my_message_item;
        } else {
            layoutResource = R.layout.your_message_item;
        }

        /*
        if(message.isMine()){
            layoutResource = R.layout.your_message_item;
        } else {
            layoutResource = R.layout.my_message_item;
        }
        */
        if(convertView != null){
            viewHolder = (ViewHolder) convertView.getTag();
        } else {
            convertView = layoutInflater.inflate(layoutResource, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }






        boolean isText = message.getImageUrl() == null;
        if(isText){
            viewHolder.messageTextView.setVisibility(View.VISIBLE);
            viewHolder.photoImageView.setVisibility(View.GONE);
            viewHolder.messageTextView.setText(message.getText());
        } else {
            viewHolder.messageTextView.setVisibility(View.GONE);
            viewHolder.photoImageView.setVisibility(View.VISIBLE);
            Glide.with(viewHolder.photoImageView.getContext())
                    .load(message.getImageUrl()).into(viewHolder.photoImageView);
        }

        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        int flag;
        AwesomeMessage awesomeMessage = messages.get(position);
        if(awesomeMessage.isMine()){
            flag = 0;
        } else {
            flag = 1;
        }
        return flag;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }


    public class ViewHolder{
        public TextView messageTextView;
        public ImageView photoImageView;

        public ViewHolder(View view){
            photoImageView = view.findViewById(R.id.photoImageView);
            messageTextView = view.findViewById(R.id.messageTextView);
        }
    }
}
