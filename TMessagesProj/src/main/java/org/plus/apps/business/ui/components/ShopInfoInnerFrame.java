package org.plus.apps.business.ui.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;

@SuppressLint("ViewConstructor")
public class ShopInfoInnerFrame extends FrameLayout {

    public interface  ShopInfoInnerFrameDelegate{
        void onItemClicked(ShopInfo shopInfo);
    }

    private ArrayList<ShopInfo> shopInfoArrayList;

    private ShopInfoInnerFrameDelegate delegate;
    private RecyclerListView listView;
    private ListAdapter adapter;
    private LinearLayoutManager layoutManager;

    public static class ShopInfo{
        public String name;
        public String value;
        public String type;
    }

    public static class InfoInnerCell extends LinearLayout {

        private TextView nameTextView;
        private TextView valueTextView;


        public InfoInnerCell(Context context) {
            super(context);

            setOrientation(VERTICAL);
            setPadding(AndroidUtilities.dp(8),AndroidUtilities.dp(8),AndroidUtilities.dp(8),AndroidUtilities.dp(16));

            nameTextView = new TextView(context);
            nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            nameTextView.setLines(1);
            nameTextView.setMaxLines(1);
            nameTextView.setSingleLine(true);
            nameTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
            nameTextView.setGravity(Gravity.CENTER);
            nameTextView.setPadding(AndroidUtilities.dp(4),AndroidUtilities.dp(4),AndroidUtilities.dp(4),AndroidUtilities.dp(4));
            nameTextView.setEllipsize(TextUtils.TruncateAt.END);
            addView(nameTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 8, 0, 0));

            valueTextView = new TextView(context);
            valueTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            valueTextView.setLines(1);
            valueTextView.setMaxLines(1);
            valueTextView.setSingleLine(true);
            valueTextView.setTextColor(Theme.getColor(Theme.key_actionBarTabLine));
            valueTextView.setGravity(Gravity.CENTER);
            valueTextView.setPadding(AndroidUtilities.dp(4),AndroidUtilities.dp(4),AndroidUtilities.dp(4),AndroidUtilities.dp(4));
            addView(valueTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT,  Gravity.CENTER_HORIZONTAL, 0, 8, 0, 0));

            //setWillNotDraw(false);
        }
//
//        @Override
//        protected void onDraw(Canvas canvas) {
//            canvas.drawLine(getMeasuredWidth(),0,getMeasuredWidth() ,getMeasuredHeight(),Theme.dividerPaint);
//            super.onDraw(canvas);
//        }

        public void setData(ShopInfo info){
            nameTextView.setText(info.value);
            valueTextView.setText(info.name);
        }
    }
    public ShopInfoInnerFrame(Context context,ArrayList<ShopInfo> info) {
        super(context);
        shopInfoArrayList = info;
        int paddingLeft = 16;

        listView = new RecyclerListView(context){

            @Override
            public void onScrollStateChanged(int state) {
                super.onScrollStateChanged(state);

            }
        };
        listView.setVerticalScrollBarEnabled(false);
        listView.setHorizontalScrollBarEnabled(false);
        listView.setItemAnimator( null);
        listView.setLayoutAnimation(null);
        listView.setClipToPadding(false);
        listView.setGlowColor(Theme.getColor(Theme.key_dialogScrollGlow));
        listView.setPadding(AndroidUtilities.dp(paddingLeft), 0, AndroidUtilities.dp(paddingLeft), 0);
        listView.setLayoutManager(layoutManager = new LinearLayoutManager(context,RecyclerView.HORIZONTAL,false){
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        });
        listView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int childCount = parent.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View child = parent.getChildAt(i);
                    int right = child.getRight();
                    if(i == childCount - 1){
                        continue;
                    }
                    canvas.drawLine(right,4,right + 1,child.getBottom() - child.getBottom() * 0.2f ,Theme.dividerPaint);
                }
            }
        });
        adapter = new ListAdapter(context);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((view, position) -> {
            if(position < 0 || position >= shopInfoArrayList.size()){
                return;
            }
            ShopInfo shopInfo = shopInfoArrayList.get(position);
            if(delegate != null && shopInfo != null){
                delegate.onItemClicked(shopInfo);
            }
        });

        addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.MATCH_PARENT,Gravity.CENTER));
    }

    public void setData(ArrayList<ShopInfo> infos){
        if(infos == null || infos.isEmpty()){
            return;
        }
        shopInfoArrayList = infos;
        if(adapter != null){
            adapter.notifyDataSetChanged();
        }
    }

    public void setDelegate(ShopInfoInnerFrameDelegate delegate) {
        this.delegate = delegate;
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter{

        private Context mContext;

        public ListAdapter(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return true;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return   new RecyclerListView.Holder(new InfoInnerCell(mContext));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            InfoInnerCell infoInnerCell = (InfoInnerCell)holder.itemView;
            infoInnerCell.setData(shopInfoArrayList.get(position));
        }

        @Override
        public int getItemCount() {
            return shopInfoArrayList != null?shopInfoArrayList.size():0;
        }
    }
}
