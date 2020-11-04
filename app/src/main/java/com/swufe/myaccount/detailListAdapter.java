package com.swufe.myaccount;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class detailListAdapter extends ArrayAdapter {
    private static final String TAG = "detailListAdapter";

    public detailListAdapter(Context context,
                     int resource,
                     ArrayList<HashMap<String,String>> list) {
        super(context, resource, list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView = convertView;
        if(itemView == null){
            itemView = LayoutInflater.from(getContext()).inflate(R.layout.detail_item_list,
                    parent,
                    false);
        }

        Map<String,String> map = (Map<String, String>) getItem(position);
        TextView detailType = (TextView) itemView.findViewById(R.id.detailTypeName);
        TextView detailDate = (TextView) itemView.findViewById(R.id.detailTime);
        TextView detailRemark = (TextView) itemView.findViewById(R.id.detailRemark);
        TextView detailMoney = (TextView) itemView.findViewById(R.id.detailMoney);

        detailType.setText(map.get("itemType"));
        detailDate.setText(map.get("itemDate"));
        detailRemark.setText(map.get("itemRemark"));
        detailMoney.setText(map.get("itemMoney"));

        return itemView;
    }
}
