package com.vivekgupta.bdonor.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.vivekgupta.bdonor.MessageClass;
import com.vivekgupta.bdonor.R;
import com.vivekgupta.bdonor.activities.RequestListActivity;

import java.util.ArrayList;

/**
 * Created by vivek on 08-01-2017.
 */

public class RequestorListAdapter extends RecyclerView.Adapter<RequestorListAdapter.ViewHolder> {
    private ArrayList<MessageClass> requestorList;
    private RequestListActivity requestListActivity;
    private int currPosition = 0;
    private Double currLatitude, currLongitude;
    //public static boolean selfTriggered = false;



    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView requestorName, requestorBType, pintsRequired, reqMsg, reqAddress, reqContact;
        Button acceptButton, rejectButton;
        ViewHolder(View itemView) {
            super(itemView);
            requestorName = itemView.findViewById(R.id.requestor_name);
            requestorBType = itemView.findViewById(R.id.requestor_blood_type);
            pintsRequired = itemView.findViewById(R.id.requestor_pints_needed);
            reqMsg = itemView.findViewById(R.id.requestor_msg);
            reqAddress = itemView.findViewById(R.id.requestor_address);
            reqContact = itemView.findViewById(R.id.requestor_contact);
            acceptButton = itemView.findViewById(R.id.accept_button);
            rejectButton = itemView.findViewById(R.id.reject_button);
        }
    }
    public RequestorListAdapter(ArrayList<MessageClass> list, RequestListActivity requestListActivity){
        this.requestorList = list;
        this.requestListActivity = requestListActivity;
        currLatitude = this.requestListActivity.getLatitude();
        currLongitude = this.requestListActivity.getLongitude();

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.request_list_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        MessageClass user = requestorList.get(position);
        //set name
        String name = String.valueOf(user.req_name);
        holder.requestorName.setText(name);
        //set btype
        String btype = "Blood Type: "+ String.valueOf(user.btype);
        holder.requestorBType.setText(btype);
        //set amount
        String pints = "Amount: "+String.valueOf(user.pints)+" pint";
        holder.pintsRequired.setText(pints);
        //set message
        String msg = "Reason: "+String.valueOf(user.req_msg);
        holder.reqMsg.setText(msg);
        //set address
        String address = "Address: "+String.valueOf(user.req_address);
        holder.reqAddress.setText(address);
        //set contact
        String number = "Contact: "+String.valueOf(user.req_num);
        holder.reqContact.setText(number);
        //set distance
//        Double desLat = (Double) user.get("latitude");
//        Double desLon = (Double) user.get("longitude");
//        String strDistance = "error locating";
//        if(currLongitude != null && currLatitude != null) {
//            int distance = distanceBetween(desLat, desLon);
//            strDistance = "~"+String.valueOf(distance)+ "km away";
//            Log.e(SaveLifeUtils.TAG, "desLat:" + desLat + ",desLon:" + desLon + ",distance:" + distance);
//        }
//        holder.req_dis.setText(strDistance);

        holder.acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currPosition = holder.getAdapterPosition();
                MessageClass user = requestorList.get(currPosition);
                sendAcceptMessage(user.req_num);
            }
        });

        holder.rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currPosition = holder.getAdapterPosition();
                //MessageClass user = requestorList.get(currPosition);
                declineRequest();
            }
        });
    }

    @Override
    public int getItemCount() {

        return requestorList.size();
    }
    private void sendAcceptMessage(String req_num) {
        //removeMessageNode(req_key);
        //send message
        //get user info
        //fragmentRequestorList.sendMessage(req_num);
        //removeListItem();
        requestListActivity.callDonorIntent(req_num);

    }
    private void declineRequest(){
        //removeMessageNode(req_key);
        removeListItem();
    }

//    public void removeMessageNode(String req_key){
//        selfTriggered = true;
//        //Log.e("vivek", "removeMessageNode self triggred is "+selfTriggered);
//        DatabaseReference mFirebaseDatabase = fragmentRequestorList.getFireBaseReference();
//        mFirebaseDatabase.child(req_key).removeValue(new DatabaseReference.CompletionListener() {
//            @Override
//            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//                //remove the item from list
//                RequestorListAdapter.selfTriggered = false;
//                removeListItem();
//            }
//        });
//    }

    private void removeListItem() {
        requestorList.remove(currPosition);
        notifyItemRemoved(currPosition);
        notifyItemRangeChanged(currPosition, requestorList.size());
        requestListActivity.updateUserNotificationList(requestorList);
    }
}

