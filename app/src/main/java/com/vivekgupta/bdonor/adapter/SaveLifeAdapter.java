package com.vivekgupta.bdonor.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.vivekgupta.bdonor.R;
import com.vivekgupta.bdonor.activities.SearchResultActivity;
import com.vivekgupta.bdonor.utils.SaveLifeUtils;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by vivek on 26-12-2016.
 */

public class SaveLifeAdapter extends RecyclerView.Adapter<SaveLifeAdapter.ViewHolder> {

    private ArrayList<Map<String, Object>> userList;
    private SearchResultActivity searchActivity;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView donorName, distance;
        public Button requestButton;
        public ViewHolder(View itemView) {
            super(itemView);
            donorName = itemView.findViewById(R.id.donor_name);
            distance = itemView.findViewById(R.id.donor_distance);
            requestButton = itemView.findViewById(R.id.button_send_request);
        }
    }
    public SaveLifeAdapter(ArrayList<Map<String, Object>> list, SearchResultActivity searchResultActivity){
        this.userList = list;
        this.searchActivity = searchResultActivity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Map<String, Object> user = userList.get(position);
        holder.donorName.setText(String.valueOf(user.get("name")));
        holder.distance.setText(String.valueOf(user.get("donor_distance"))+" km away");

        holder.requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //call the donor
                //String number = String.valueOf(user.get("num"));
                //if(!TextUtils.isEmpty(number))
                Map<String, Object> user = userList.get(holder.getAdapterPosition());
                String number = String.valueOf(user.get(SaveLifeUtils.TAG_NUM));
                searchActivity.sendDonorRequest(number);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}
