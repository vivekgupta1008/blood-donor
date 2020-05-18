package com.vivekgupta.bdonor.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vivekgupta.bdonor.BTypeList;
import com.vivekgupta.bdonor.R;
import com.vivekgupta.bdonor.interfaces.ItemClickListener;

import java.util.ArrayList;

/**
 * Created by vivek on 05-02-2017.
 */

public class BtypeListAdapter extends RecyclerView.Adapter<BtypeListAdapter.ViewHolder>{
    private ArrayList<BTypeList> blist;
    private ItemClickListener clickListener;
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView bType, bTypeNum;
        ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            bType = itemView.findViewById(R.id.item_blood_type);
            bTypeNum = itemView.findViewById(R.id.item_blood_type_req_num);
        }

        @Override
        public void onClick(View v) {
            clickListener.onClick(v, getAdapterPosition());
        }
    }

    public BtypeListAdapter(ArrayList<BTypeList> list){
        this.blist = list;
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_blood_list, parent, false);
        return new ViewHolder(v);
    }



    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BTypeList bitem = blist.get(position);
        holder.bType.setText(bitem.bType);
        if(bitem.bTypeNum == 0)
            holder.bTypeNum.setVisibility(View.GONE);
        else {
            holder.bTypeNum.setVisibility(View.VISIBLE);
            holder.bTypeNum.setText(String.valueOf(bitem.bTypeNum));
        }
    }

    @Override
    public int getItemCount() {
        return blist.size();
    }


}