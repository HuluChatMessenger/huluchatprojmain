package org.plus.apps.business.ui.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.plus.apps.business.ShopUtils;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.GroupCreateSpan;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;

public class SpanListLayout extends FrameLayout {


    public static class SpanData{

        public int res;
        public String title;
        public String key;
    }

    public interface Delegate{
        void onSpanSelected(GroupCreateSpan groupCreateSpan,int pos);
    }

    private Delegate delegate;

    public void setDelete(Delegate delete) {
        this.delegate = delete;
    }

    private SpanAdapter adapter;

    private ArrayList<SpanData> businesses;


    public SpanListLayout(Context context) {
        super(context);

        RecyclerListView listView = new RecyclerListView(context);
        ((DefaultItemAnimator) listView.getItemAnimator()).setDelayAnimations(false);
        LinearLayoutManager layoutManager;
        listView.setLayoutManager(layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        listView.setHorizontalScrollBarEnabled(false);
        listView.setGlowColor(ShopUtils.getIntAlphaColor(Theme.getColor(Theme.key_chats_actionBackground),0.1f));
        listView.setAdapter(adapter = new SpanAdapter(context));
        addView(listView);

        listView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.left = AndroidUtilities.dp(12);
                outRect.bottom = AndroidUtilities.dp(12);
                outRect.top = AndroidUtilities.dp(12);
                outRect.right = AndroidUtilities.dp(12);
            }
            @Override
            public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.onDraw(c, parent, state);
            }
        });

        listView.setOnItemClickListener((view, position) -> {
            GroupCreateSpan groupCreateSpan = (GroupCreateSpan)view;
            if(groupCreateSpan.isDeleting()){
                groupCreateSpan.cancelDeleteAnimation();
            }else{
                groupCreateSpan.startDeleteAnimation();
            }
            if(delegate != null){
                delegate.onSpanSelected((GroupCreateSpan)view,position);
            }



        });
    }



    public void setSpans(ArrayList<SpanData> businesses) {
        this.businesses = businesses;
        if(adapter != null){
            if(adapter.getItemCount() == 0){
                adapter.notifyItemRangeInserted(0,businesses.size());
            }else {
                adapter.notifyDataSetChanged();
            }
        }
    }

    private class SpanAdapter extends RecyclerListView.SelectionAdapter{

        private Context mContext;

        public SpanAdapter(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return true;
        }


       public SpanData getItem(int pos){
            return businesses.get(pos);
       }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            GroupCreateSpan groupCreateUserCell  =  new GroupCreateSpan(mContext,true);
            return new RecyclerListView.Holder(groupCreateUserCell);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            GroupCreateSpan groupCreateUserCell  =  (GroupCreateSpan)holder.itemView;
            groupCreateUserCell.setData(businesses.get(position));
        }

        @Override
        public int getItemCount() {
            return  businesses != null?businesses.size():0;
        }
    }

}
