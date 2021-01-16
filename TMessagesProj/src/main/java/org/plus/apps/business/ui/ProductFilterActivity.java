package org.plus.apps.business.ui;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScrollerCustom;
import androidx.recyclerview.widget.RecyclerView;

import org.plus.apps.business.ShopUtils;
import org.plus.apps.business.data.ShopDataController;
import org.plus.apps.business.data.ShopDataSerializer;
import org.plus.apps.business.ui.components.TagTextLayout;
import org.plus.apps.business.ui.components.ValueSelectBottomSheet;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.PollEditTextCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.http.POST;

import static org.plus.apps.business.ui.ProductInputActivity.UI_INPUT_NUM;
import static org.plus.apps.business.ui.ProductInputActivity.UI_INPUT_STRING;

public class ProductFilterActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private TextView acceptTextView;
    private HashMap<String,Object> filterMap = new HashMap<>();
    private HashMap<String,Object> valueMap = new HashMap<>();

    public interface  ProductFilterDelegate{
        void onFilterSelected(HashMap<String,Object> filterMap);
    }

    private Map<String,Integer> keyToPos = new HashMap<>();


    private ProductFilterDelegate filterDelegate;

    public void setFilterDelegate(ProductFilterDelegate filterDelegate) {
        this.filterDelegate = filterDelegate;
    }

    private RecyclerListView listView;
    private ListAdapter listAdapter;
    private LinearLayoutManager layoutManager;
    private EmptyTextProgressView emptyTextProgressView;

    private int rowCount;
    private int firstSecRow;

    private String  bus_type;
    ArrayList<ShopDataSerializer.Field> fields  = new ArrayList<>();

    private String scrollToKey;

    public ProductFilterActivity(String type) {
        bus_type = type;
        scrollToKey = "";
    }

    public ProductFilterActivity(String type,String scrollKey) {
        bus_type = type;
        scrollToKey = scrollKey;
    }


    private String section;

    @Override
    public boolean onFragmentCreate() {
        section = bus_type + "-filter";
        ShopDataController.getInstance(currentAccount).loadSectionConfiguration(section,true,classGuid);
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.didConfigrationLoaded);


        return super.onFragmentCreate();
    }


    @Override
    public void onFragmentDestroy() {
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.didConfigrationLoaded);
        super.onFragmentDestroy();
    }

    private static final int reset = 1;

    @Override
    public View createView(Context context) {
        actionBar.setTitle("Categories");
        actionBar.setBackButtonImage(R.drawable.edit_cancel);

        ActionBarMenu menu = actionBar.createMenu();
        menu.addItem(reset, "reset".toUpperCase());

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }else if(id == reset){
                    filterMap.clear();
                    valueMap.clear();
                    if(listAdapter != null){
                        listAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = (FrameLayout) fragmentView;
        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        listView = new RecyclerListView(context);
        listView.setLayoutManager(layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false){
            @Override
            public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {

            }
        });
        listView.setVerticalScrollBarEnabled(false);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT,Gravity.LEFT|Gravity.TOP,0,0,0,48));
        listView.setAdapter(listAdapter = new ListAdapter(context));

        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        listView.setOnItemClickListener((view, position) -> {


        });

        emptyTextProgressView = new EmptyTextProgressView(context);
        emptyTextProgressView.setShowAtCenter(true);
        emptyTextProgressView.showProgress();
        emptyTextProgressView.setText("No filter found!");
        listView.setEmptyView(emptyTextProgressView);
        frameLayout.addView(emptyTextProgressView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));




        FrameLayout bottomLayout = new FrameLayout(context);
        bottomLayout.setBackgroundDrawable(Theme.createSelectorWithBackgroundDrawable(Theme.getColor(Theme.key_passport_authorizeBackground), Theme.getColor(Theme.key_passport_authorizeBackgroundSelected)));
        frameLayout.addView(bottomLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.BOTTOM));


        acceptTextView = new TextView(context);
        acceptTextView.setTextColor(Theme.getColor(Theme.key_passport_authorizeText));
        acceptTextView.setText("Apply");
        acceptTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        acceptTextView.setGravity(Gravity.CENTER);
        acceptTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        bottomLayout.addView(acceptTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        acceptTextView.setOnClickListener(v -> {
            if(filterDelegate != null){
                filterDelegate.onFilterSelected(filterMap);
                finishFragment();
            }
        });


        return fragmentView;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
        AndroidUtilities.requestAdjustResize(getParentActivity(), classGuid);
    }


    private void scrollToKeyView(int pos){
        if(pos != -1){
            RecyclerView.ViewHolder holder = listView.findViewHolderForAdapterPosition(pos);
            if(holder != null){
                View view =  holder.itemView;
                if(view != null){
                    if(view instanceof HeaderEditTextCell){
                        HeaderEditTextCell pollEditTextCell = (HeaderEditTextCell)view;
                        pollEditTextCell.getPollEditTextCell().getTextView().requestFocus();
                        AndroidUtilities.showKeyboard(pollEditTextCell.getPollEditTextCell().getTextView());
                    }else if(view instanceof TextSettingsCell){
                        TextSettingsCell textSettingsCell = (TextSettingsCell)holder.itemView;
                        textSettingsCell.performClick();
                    }else{
                        AndroidUtilities.shakeView(view, 2, 0);
                    }
                }
            }
        }

    }


    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.didConfigrationLoaded) {
            boolean loaded=(boolean)args[0];
            String sec = (String) args[1];
            if(loaded && sec.equals(section)){
               ArrayList<ShopDataSerializer.FieldSet> filterModels = (ArrayList<ShopDataSerializer.FieldSet> )args[2];
               if(filterModels != null && filterModels.size() > 0){
                   fields = filterModels.get(0).fields;
                   if(listAdapter != null){
                       listAdapter.notifyDataSetChanged();
                       if(!scrollToKey.isEmpty()){
                           if(scrollToKey == null || keyToPos == null){
                               return;
                           }
                           Integer pos;
                           if((pos =  keyToPos.get(scrollToKey)) == null){
                               return;
                           }
                           AndroidUtilities.runOnUIThread(new Runnable() {
                               @Override
                               public void run() {
                                   layoutManager.scrollToPosition(pos);
                                   scrollToKeyView(pos);
                               }
                           },300);


                       }
                   }

               }
            }
        }
    }



    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        private Context mContext;


        public ListAdapter(Context context) {
            mContext = context;
        }


        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return holder.getItemViewType() == 1;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 1:
                    view = new TextSettingsCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 2:
                    HeaderTagTextLayout tagTextLayout = new HeaderTagTextLayout(mContext,12);
                    view = tagTextLayout;
                    view.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    tagTextLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 4:
                    view = new HeaderEditTextCell(mContext,12);
                    view.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    break;
                case 3:
                    PollEditTextCell pollEditTextCell = new PollEditTextCell(mContext,null);
                    view = pollEditTextCell;
                    pollEditTextCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 10:
                default:
                    view = new ShadowSectionCell(mContext);
                    view.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    break;
            }
            return new RecyclerListView.Holder(view);
        }




        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ShopDataSerializer.Field filterModel = fields.get(position);

            if(!TextUtils.isEmpty(scrollToKey)){
                keyToPos.put(filterModel.key,position);
            }

            switch (holder.getItemViewType()) {
                case 1: {
                    TextSettingsCell textSettingsCell = (TextSettingsCell) holder.itemView;
                    textSettingsCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
                    textSettingsCell.setTextAndIcon(filterModel.label, R.drawable.message_arrow, true);
                    textSettingsCell.setOnClickListener(v -> {
                        ValueSelectBottomSheet.createShareAlert(mContext, filterModel.choices, false, new ValueSelectBottomSheet.ProductTagValueDelegate() {
                            @Override
                            public void didProductTagSelected(ArrayList<TagTextLayout.TagText> tags) {
                                filterMap.put(filterModel.key, tags.get(0).key);
                                textSettingsCell.setTextAndIcon(tags.get(0).value, R.drawable.message_arrow, true);
                            }
                        }).show();
                    });
                }
                break;
                case 2:{
                    HeaderTagTextLayout headerTagTextLayout = (HeaderTagTextLayout)holder.itemView;
                    headerTagTextLayout.setHeader(filterModel.label);
                    headerTagTextLayout.showHeader(!ShopUtils.isEmpty(filterModel.label));
                    TagTextLayout tagTextLayout = headerTagTextLayout.getTagTextLayout();
                    ArrayList<TagTextLayout.TagText> tagTexts = new ArrayList<>();
                    for (String[] li : filterModel.choices) {
                        tagTexts.add(new TagTextLayout.TagText(li[0], li[1]));
                    }
                    tagTextLayout.setDelegate((view, pos, selected) -> {
                        tagTextLayout.setSelectedTag(selected);
                        filterMap.put(filterModel.key,selected.key);
                        valueMap.put(filterModel.key,selected);
                    });
                    tagTextLayout.setTagTexts(tagTexts);
                    if(valueMap.get(filterModel.key) != null){
                        TagTextLayout.TagText value = (TagTextLayout.TagText) valueMap.get(filterModel.key);
                        tagTextLayout.setSelectedTag(value);
                    }else {
                        tagTextLayout.setSelectedTag(null);
                    }
                }
                break;
                case 4:{
                    HeaderEditTextCell headerEditTextCell = (HeaderEditTextCell)holder.itemView;
                    headerEditTextCell.setHint(filterModel.placeholder,filterModel.label);
                    headerEditTextCell.setField(filterModel);
                    headerEditTextCell.setInputType(filterModel);
                    break;
                }
            }
        }


        @Override
        public int getItemViewType(int position) {
            ShopDataSerializer.Field model = fields.get(position);
             if(model != null){
                if(model.ui_type.equals("ui_choose")){
                    return 1;
                }else if(model.ui_type.equals("ui_choose_hor")){
                    return 2;
                }else if(model.ui_type.startsWith("ui_input")){
                    return 4;
                }
            }
            return super.getItemViewType(position);
        }


        @Override
        public int getItemCount() {
            int count = 0;
            if(fields != null){
                count = fields.size();
            }
            return count;
        }
    }


    private  class HeaderEditTextCell extends LinearLayout {

        public HeaderCell header;
        public PollEditTextCell pollEditTextCell;

        public PollEditTextCell getPollEditTextCell() {
            return pollEditTextCell;
        }

        public HeaderEditTextCell(@NonNull Context context, int padding) {
            super(context);

            setOrientation(VERTICAL);

            setPadding(0,AndroidUtilities.dp(padding),0,AndroidUtilities.dp(padding));

            pollEditTextCell = new PollEditTextCell(context,null);
            pollEditTextCell.addTextWatcher(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(field != null){
                        filterMap.put(field.key,s.toString());
                        valueMap.put(field.key,s.toString());
                    }


                }
            });
            pollEditTextCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

            header = new HeaderCell(context,Theme.key_dialogTextBlack,21);
            header.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

            addView(header, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT));
            addView(pollEditTextCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT));

        }

        private ShopDataSerializer.Field field;
        public void setField(ShopDataSerializer.Field field1){
            field = field1;
        }


        public void setInputType(ShopDataSerializer.Field filterModel){
            if(filterModel == null){
                return;
            }
            if(filterModel.ui_type.equals(UI_INPUT_NUM)){
                pollEditTextCell.getTextView().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            }else if(filterModel.ui_type.equals(UI_INPUT_STRING)){
                pollEditTextCell.getTextView().setInputType(InputType.TYPE_CLASS_TEXT);
            }

        }


        public void setHint(String hint,String headerTExt){
            header.setText(headerTExt);
            pollEditTextCell.setTextAndHint("",hint,false);

        }

    }

    private static class HeaderTagTextLayout extends LinearLayout{

        private TagTextLayout tagTextLayout;
        public HeaderCell header;


        public HeaderTagTextLayout(Context context,int padding) {
            super(context);

            setOrientation(VERTICAL);

            setPadding(0,AndroidUtilities.dp(padding),0,AndroidUtilities.dp(padding));

            header = new HeaderCell(context,Theme.key_dialogTextBlack,21);
            header.setVisibility(GONE);
            header.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

            addView(header, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT));

            tagTextLayout = new TagTextLayout(context);
            addView(tagTextLayout,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT));

        }

        public void  setHeader(String text){
            header.setText(text);
        }

        public void showHeader(boolean show){
            if(show){
                header.setVisibility(VISIBLE);
            }else{
                header.setVisibility(GONE);
            }
        }

        public TagTextLayout getTagTextLayout() {
            return tagTextLayout;
        }
    }



}
