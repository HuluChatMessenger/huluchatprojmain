package org.plus.apps.business.ui;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.util.Log;

import org.plus.apps.business.ShopUtils;
import org.plus.apps.business.data.ShopDataController;
import org.plus.apps.business.data.ShopDataSerializer;
import org.plus.net.APIError;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;

public class InstaShopFragment extends BaseFragment  implements NotificationCenter.NotificationCenterDelegate {

    public interface Delegate{
        void onMoreClick(ShopDataSerializer.InstaShop instaShop);
    }

    private ArrayList<ShopDataSerializer.InstaShop> instaShops = new ArrayList<>();

    private RecyclerListView listView;
    private LinearLayoutManager layoutManager;
    private ListAdapter listAdapter;

    public String link;
    public String section;

    public InstaShopFragment(String remote,String sec) {
        this.link = remote;
        this.section = sec;
    }

    @Override
    public boolean onFragmentCreate() {

        getNotificationCenter().addObserver(this,NotificationCenter.didConfigrationLoaded);
        getNotificationCenter().addObserver(this,NotificationCenter.didRemoteDataLoaded);


        ShopDataController.getInstance(currentAccount).loadSectionConfiguration(section,true,classGuid);
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        getNotificationCenter().removeObserver(this,NotificationCenter.didConfigrationLoaded);
        getNotificationCenter().removeObserver(this,NotificationCenter.didRemoteDataLoaded);

        super.onFragmentDestroy();
    }


    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle("Shop  Directories");
        actionBar.setCastShadows(false);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick(){
            @Override
            public void onItemClick(int id) {
                if(id == -1){
                    finishFragment();
                }
            }
        });

        fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = (FrameLayout)fragmentView;
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

        listAdapter = new ListAdapter(context);

        listView = new RecyclerListView(context);
        listView.setVerticalScrollBarEnabled(false);
        DefaultItemAnimator itemAnimator = (DefaultItemAnimator) listView.getItemAnimator();
        itemAnimator.setSupportsChangeAnimations(false);
        itemAnimator.setDelayAnimations(false);
        listView.setPadding(0, AndroidUtilities.dp(0), 0, 0);
        listView.setClipToPadding(false);
        layoutManager = new LinearLayoutManager(context){
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        };

        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        listView.setLayoutManager(layoutManager);
        listView.setGlowColor(0);
        listView.setAdapter(listAdapter);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));

        EmptyTextProgressView emptyTextProgressView = new EmptyTextProgressView(context);
        emptyTextProgressView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        emptyTextProgressView.showProgress();
        frameLayout.addView(emptyTextProgressView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
        listView.setEmptyView(emptyTextProgressView);

        return fragmentView;
    }


    @Override
    public void onResume() {
        super.onResume();
        if(listAdapter != null){
            listAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void didReceivedNotification(int id, int account, Object... args) {

        if(id == NotificationCenter.didConfigrationLoaded) {
            boolean loaded = (boolean) args[0];
            String sec = (String) args[1];
            if (loaded && sec.equals(section)) {
                ArrayList<ShopDataSerializer.FieldSet> storeFields = (ArrayList<ShopDataSerializer.FieldSet>) args[2];
                 ShopDataController.getInstance(currentAccount).loadRemoteData(storeFields.get(0).remote,storeFields.get(0).type,classGuid);
            }
        }else if(id == NotificationCenter.didRemoteDataLoaded){
            Log.i("didRemoteDataLoaded","NotificationCenter.didRemoteDataLoaded");
            int guid = (int)args[3];
            if(guid == classGuid){
                Log.i("didRemoteDataLoaded","NotificationCenter.classGuid");

                boolean loaded = (boolean)args[0];
                if(loaded){
                    Log.i("didRemoteDataLoaded","loaded true");

                    String type = (String) args[1];
                    Log.i("didRemoteDataLoaded",type + " is type");

                    if (StoreActivity.UI_SHOP_INSTA_VIEW.equals(type)) {
                        Log.i("didRemoteDataLoaded","StoreActivity.UI_SHOP_INSTA_VIEW.equals(type)");

                        instaShops = (ArrayList<ShopDataSerializer.InstaShop>) args[2];
                        if (listAdapter != null) {
                            listAdapter.notifyDataSetChanged();
                        }
                    }
                }else{
                    APIError apiError  = (APIError)args[1];
                }
            }
        }
    }

    private  class ListAdapter extends RecyclerListView.SelectionAdapter{

        private Context mContext;

        public ListAdapter(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return false;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            InstaInnerLayout instaInnerLayout = new InstaInnerLayout(mContext);
            instaInnerLayout.setDelegate(new Delegate() {
                @Override
                public void onMoreClick(ShopDataSerializer.InstaShop instaShop) {

                    Bundle bundle = new Bundle();
                    bundle.putInt("chat_id",ShopUtils.toClientChannelId(instaShop.channel));
                    BusinessProfileActivity profileActivity = new BusinessProfileActivity(bundle);
                    presentFragment(profileActivity);

                }
            });
            return new RecyclerListView.Holder(instaInnerLayout);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ShopDataSerializer.InstaShop instaShop  = instaShops.get(position);
            InstaInnerLayout instaInnerLayout = (InstaInnerLayout)holder.itemView;
            instaInnerLayout.setInstaShop(instaShop);

        }

        @Override
        public int getItemCount() {
            return instaShops != null?instaShops.size():0;
        }
    }

    public static  class InstaInnerLayout extends FrameLayout {

        private Delegate delegate;

        public void setDelegate(Delegate delegate) {
            this.delegate = delegate;
        }

        private ShopDataSerializer.InstaShop instaShop;

        private BackupImageView shopImageView;
        private TextView nameTextView;
        private TextView onlineTextView;
        private SimpleTextView moreTextView;

        private ArrayList<BackupImageView> imageViews = new ArrayList<>();

        public InstaInnerLayout(Context context) {
            super(context);

            shopImageView = new BackupImageView(context);
            shopImageView.setRoundRadius(AndroidUtilities.dp(21));
            addView(shopImageView, LayoutHelper.createFrame(42,42, Gravity.LEFT|Gravity.TOP,16,16,0,0));

            nameTextView = new TextView(context);
            nameTextView.setTextColor(Theme.getColor(Theme.key_profile_title));
            nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
            nameTextView.setLines(1);
            nameTextView.setMaxLines(1);
            nameTextView.setSingleLine(true);
            nameTextView.setEllipsize(TextUtils.TruncateAt.END);
            nameTextView.setGravity(Gravity.LEFT);
            nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 74, 16, 96, 0));

            onlineTextView = new TextView(context);
            onlineTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            onlineTextView.setLines(1);
            onlineTextView.setMaxLines(1);
            onlineTextView.setSingleLine(true);
            onlineTextView.setTextColor(Theme.getColor(Theme.key_wallet_grayText));

            onlineTextView.setEllipsize(TextUtils.TruncateAt.END);
            onlineTextView.setGravity(Gravity.LEFT);
            addView(onlineTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 74, 42, 96, 0));

            moreTextView = new SimpleTextView(getContext());
            moreTextView.setTextSize(15);
            moreTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            moreTextView.setTextColor(Theme.getColor(Theme.key_profile_status));

            moreTextView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(delegate != null){
                        delegate.onMoreClick(instaShop);
                    }
                }
            });

            moreTextView.setGravity(Gravity.RIGHT);
            addView(moreTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.RIGHT | Gravity.TOP, 0, 58/2, 16, 0));

            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.setWeightSum(3);
            for(int a = 0; a < 3; a++){
                FrameLayout frameLayout = new FrameLayout(context);
                BackupImageView backupImageView = new BackupImageView(context);
                backupImageView.setRoundRadius(8);
                frameLayout.addView(backupImageView,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.MATCH_PARENT,Gravity.CENTER,16,16,16,16));
                imageViews.add(backupImageView);
                frameLayout.setBackground(ShopUtils.createRoundStrokeDrwable(8,3,Theme.key_windowBackgroundGray,Theme.key_windowBackgroundWhite));
                linearLayout.addView(frameLayout, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1f,Gravity.CENTER,3,3,3,3));
            }
            addView(linearLayout,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,100,Gravity.LEFT|Gravity.TOP,32,42 + 13 + 16,32,16));

        }

        public void setInstaShop(ShopDataSerializer.InstaShop instaShop) {
            this.instaShop = instaShop;
            nameTextView.setText(instaShop.title);
            moreTextView.setText("More");
            onlineTextView.setText(instaShop.count + " Listing");

            AvatarDrawable avatarDrawable = new AvatarDrawable();
            avatarDrawable.setInfo(5,instaShop.title,null);
            if(instaShop.profilePicture != null){
                shopImageView.setImage(instaShop.profilePicture.photo,null,avatarDrawable);
            }else{
                shopImageView.setImage(null,null,avatarDrawable);

            }

            for(int a = 0 ; a < imageViews.size(); a++){
                if(instaShop.products.size() > a){
                    ShopDataSerializer.InstaShop.Product product = instaShop.products.get(a);
                    if(product != null){
                        imageViews.get(a).setImage(product.picture.photo,null,null);
                    }
                }
            }
        }
    }

}
