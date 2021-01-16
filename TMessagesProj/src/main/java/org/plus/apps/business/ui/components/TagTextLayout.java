package org.plus.apps.business.ui.components;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.plus.apps.business.ShopUtils;
import org.plus.apps.business.ui.cells.Ui_text_tag_cell;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TagTextLayout extends FrameLayout {

    public static class TagText{
      public String value;
      public String key;

        public TagText(String key, String value) {
            this.value = value;
            this.key = key;
        }

        public Map<String,Object> toMap(){
            Map<String,Object> map = new HashMap<>();
            map.put(key,value);
            return map;
        }
    }
    private TagText currentSelectedTag;
    private ArrayList<TagText> tagTexts = new ArrayList<>();

    public interface TagTextDelegate{
        void onItemClick(View view, int position,TagText selected);
    }

    private RecyclerListView listView;
    private LinearLayoutManager layoutManager;
    private TagTextAdapter adapter;
    private TagTextDelegate delegate;

    public TagTextLayout(Context context) {
        super(context);

        listView = new RecyclerListView(context);
        ((DefaultItemAnimator) listView.getItemAnimator()).setDelayAnimations(false);
        listView.setLayoutManager(layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        listView.setHorizontalScrollBarEnabled(false);
        listView.setGlowColor(ShopUtils.getIntAlphaColor(Theme.getColor(Theme.key_chats_actionBackground),0.1f));
        listView.setAdapter(adapter = new TagTextAdapter(context));
        addView(listView);
        listView.setOnItemClickListener((view, position, x, y) -> {
            if(delegate != null){
                currentSelectedTag = tagTexts.get(position);
                adapter.notifyDataSetChanged();
                delegate.onItemClick(view,position,tagTexts.get(position));
            }
        });

    }

    public void setDelegate(TagTextDelegate delegate) {
        this.delegate = delegate;
    }

    public void setTagTexts(ArrayList<TagText> tagTexts) {
        showCancel = false;
        this.tagTexts = tagTexts;
        if(adapter != null){
            adapter.notifyDataSetChanged();
        }
    }


    private boolean showCancel;
    private boolean isFilterMode;
    public void showOnlySelected(){
        isFilterMode = true;
        showCancel = true;
        tagTexts.clear();
        adapter.notifyItemRangeRemoved(0,tagTexts.size());
        tagTexts.add(0,currentSelectedTag);
        adapter.notifyItemInserted(0);
        tagTexts.add(1,new TagText("filter","Filter"));
        adapter.notifyItemInserted(1);

    }

    public boolean isFilterMode() {
        return isFilterMode;
    }

    public void setSelectedTag(TagText selected) {
        currentSelectedTag = selected;
        if(adapter != null){
            adapter.notifyDataSetChanged();
        }
    }

    private class TagTextAdapter extends RecyclerListView.SelectionAdapter{

        private Context mContext;

        public TagTextAdapter(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return false;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Ui_text_tag_cell groupCreateUserCell  =  new Ui_text_tag_cell(mContext,8);
            return new RecyclerListView.Holder(groupCreateUserCell);
        }

        
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Ui_text_tag_cell groupCreateUserCell = (Ui_text_tag_cell)holder.itemView;
            TagText tagText = tagTexts.get(position);
            if(tagText != null){
                groupCreateUserCell.setText(tagText.value);
                if(currentSelectedTag != null){
                    boolean sel = false;
                    if(currentSelectedTag.key.equals(tagText.key)){
                        sel = true;
                    }
                    groupCreateUserCell.setTagSelected(sel);
                }else{
                    groupCreateUserCell.setTagSelected(false);
                }
            }
        }


        @Override
        public int getItemCount() {
            return tagTexts != null?tagTexts.size():0;
        }
    }


}
