package org.plus.apps.business.ui.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.plus.apps.business.ShopUtils;
import org.plus.apps.business.data.ShopDataSerializer;
import org.plus.apps.business.ui.cells.ProductImageCell;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class HorizontalFilterLayout extends FrameLayout {

    public static class Filter{
        public String title;
        public int res;
        public String key;
    }

    private ArrayList<Filter> filters = new ArrayList<>();

    public interface FilterDelegate{

        void onItemClick(View view, int position,Filter imageInput,float x, float y);
        void onItemLonClick(View view, int position);
    }





    private RecyclerListView listView;
    private LinearLayoutManager layoutManager;
    private FilterAdapter adapter;
    private FilterDelegate delegate;
    private boolean divider;


    public HorizontalFilterLayout(Context context,FilterDelegate layoutImageDelegate) {
        super(context);

        delegate = layoutImageDelegate;
        listView = new RecyclerListView(context);
        listView.setPadding(AndroidUtilities.dp(4),0,AndroidUtilities.dp(4),AndroidUtilities.dp(4));
        listView.setClipToPadding(false);
        listView.setLayoutManager(layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        listView.setHorizontalScrollBarEnabled(false);
        listView.setGlowColor(ShopUtils.getIntAlphaColor(Theme.getColor(Theme.key_chats_actionBackground),0.1f));
        listView.setAdapter(adapter = new FilterAdapter(context));
        addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT));
        listView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(android.graphics.Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.left = AndroidUtilities.dp(4);
                outRect.bottom = AndroidUtilities.dp(4);
                outRect.top = AndroidUtilities.dp(4);
                outRect.right = AndroidUtilities.dp(4);
            }
        });
        listView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        listView.setOnItemClickListener((view, position, x, y) -> {
            if(delegate != null){
                Filter input = filters.get(position);
                delegate.onItemClick(view,position,input,x,y);
            }
        });

        listView.setOnItemLongClickListener((view, position) -> {
            if(delegate != null){
                delegate.onItemLonClick( view,position);
            }
            return false;
        });
    }

    public RecyclerListView getListView() {
        return listView;
    }


    public void setDelegate(FilterDelegate delegate) {
        this.delegate = delegate;
    }
    public void setFilters(){

        Filter filter = new Filter();
        filter.key =  "filter";
        filter.title = "Filter";
        filter.res = R.drawable.menu_newfilter;
        filters.add(0,filter);


        filter = new Filter();
        filter.key =  "sort";
        filter.title = "Sort";
        filter.res = R.drawable.ic_sort;
        filters.add(1,filter);

    }
    public void setFilters(ArrayList<ShopDataSerializer.Field> fields){
        if(fields == null){
            return;
        }
        filters.clear();
      //  setFilters();

//        for(int a = 0; a < fields.size(); a++){
//            ShopDataSerializer.Field field = fields.get(a);
//            if(field == null){
//                continue;
//            }
//            Filter filter = new Filter();
//            filter.key = field.key;
//            filter.title = field.label;
//            filter.res = R.drawable.menu_add;
//            filters.add(filter);
//        }


        Filter filter = new Filter();
        filter.key =  "filter";
        filter.title = "Filter";
        filter.res = R.drawable.menu_newfilter;
        filters.add(0,filter);


        filter = new Filter();
        filter.key =  "sort";
        filter.title = "Sort";
        filter.res = R.drawable.ic_sort;
        filters.add(1,filter);

        Collections.sort(fields, (o1, o2) -> Integer.compare(o1.order,o2.order));

        if(adapter != null){
            adapter.notifyDataSetChanged();
        }
    }




    private class FilterAdapter extends RecyclerListView.SelectionAdapter{

        private Context mContext;

        public FilterAdapter(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return true;
        }


        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new RecyclerListView.Holder(new FilterItemView(mContext));
        }


        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Filter filter = filters.get(position);
            FilterItemView itemView = (FilterItemView)holder.itemView;
            itemView.setFilter(filter);
        }

        @Override
        public int getItemCount() {
            return filters.size();
        }

    }

    private class FilterItemView extends FrameLayout{

        private TextView filterTextView;

        public FilterItemView(@NonNull Context context) {
            super(context);

            filterTextView = new TextView(context);
            filterTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
            filterTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            filterTextView.setTypeface(AndroidUtilities.getTypeface("fonts/Roboto-Black.ttf"));
            filterTextView.setLines(1);
            filterTextView.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(4), AndroidUtilities.dp(8), AndroidUtilities.dp(4));
            filterTextView.setMaxLines(1);
            filterTextView.setText("Sort");
            filterTextView.setSingleLine(true);
            filterTextView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            filterTextView.setCompoundDrawablePadding(AndroidUtilities.dp(8));
            addView(filterTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 4, 16, 8, 16));
            filterTextView.setBackground(ShopUtils.createRoundStrokeDrwable(8, 3, Theme.key_windowBackgroundGray, Theme.key_windowBackgroundWhite));
        }

        public void setFilter(Filter filter){
            if (filter == null) {
                return;
            }
            filterTextView.setText(filter.title);

            Drawable drawable = getResources().getDrawable(filter.res).mutate();
            if (drawable != null) {
                drawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextBlack), PorterDuff.Mode.MULTIPLY));
            }

            if(filter.key.equals("sort")){
                filterTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
            }else{
                filterTextView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);

            }
        }

    }




}
