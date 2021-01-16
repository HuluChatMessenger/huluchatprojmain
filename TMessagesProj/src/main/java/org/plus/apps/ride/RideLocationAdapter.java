package org.plus.apps.ride;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.plus.apps.ride.SuperLocationAdapter;
import org.plus.apps.ride.data.RideObjects;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Cells.LocationCell;
import org.telegram.ui.Components.RecyclerListView;

public class RideLocationAdapter extends SuperLocationAdapter {

    private Context mContext;

    private int currentAccount = UserConfig.selectedAccount;

    public RideLocationAdapter(Context context){
       mContext = context;
    }

    @Override
    public boolean isEnabled(RecyclerView.ViewHolder holder) {
        return true;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecyclerListView.Holder(new LocationCell(mContext, false));
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((LocationCell) holder.itemView).setLocation(places.get(position), position, position != places.size() - 1);
    }

    public RideObjects.SearchLocation getItem(int i) {
        if (i >= 0 && i < places.size()) {
            return places.get(i);
        }
        return null;
    }


    @Override
    public int getItemCount() {
        return places.size();
    }
}
