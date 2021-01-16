
package org.plus.apps.business.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.exoplayer2.util.Log;

import org.plus.apps.business.ProductCategoryActivity;
import org.plus.apps.business.GlobalSearchActivity;
import org.plus.apps.business.ProductCell;
import org.plus.apps.business.ShopUtils;
import org.plus.apps.business.data.ShopDataController;
import org.plus.apps.business.data.ShopDataSerializer;
import org.plus.apps.business.ui.cells.BusinessCell;
import org.plus.apps.business.ui.components.LoadingView;
import org.plus.experment.HuluChatScanFragment;
import org.plus.net.APIError;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MrzRecognizer;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.EmptyCell;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.BottomPagesView;
import org.telegram.ui.Components.ClippingImageView;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.FlickerLoadingView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RadialProgressView;
import org.telegram.ui.Components.RecyclerAnimationScrollHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SearchField;

import java.util.ArrayList;

import static org.plus.apps.business.ProductCell.getItemSize;

public class StoreActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate{

    public interface  StoreActivityDelegate{

        default  void didPressFeatureShop(long channel){

        }
        default void didBusSelected(ShopDataSerializer.ProductType business){

        }

        default void didFeaturedProductSelected(ShopDataSerializer.FeaturedProduct featuredProduct){

        }

        default void didPressedMore(String remote,String sec){}

    }


    public static final String SEC_GRANDSHOP_FEATURED_INSTASHOPS = "grandshop-featured-instashops";
    public static final String SEC_GRANDSHOP_PRODUCT_MORE = "grandshop-featured-instashops";

    public static final String UI_BANNER = "ui_collection_banner";
    public static final String UI_SHOP_HORIZONTAL = "ui_shop_horizontal";
    public static final String UI_BUSINESS = "ui_business";
    public static final String UI_PRODUCT_VERTICAL = "ui_product_vertical";
    public static final String UI_PRODUCT_HORIZONTAL= "ui_product_horizontal";

    public static final String UI_SHOP_INSTA_VIEW= "ui_shop_insta_view";

    private BannerContainer bannerContainer;
    private ShopContainer shopContainer;
    private BusinessContainer businessContainer;
    private ProductContainer productContainer;
    private ProductHorizontalContainer  productHorizontalContainer;
    private SwipeRefreshLayout refreshLayout;

    private ImageView pagedownButtonImage;
    private FrameLayout pagedownButton;
    private AnimatorSet pagedownButtonAnimation;
    private boolean canShowPagedownButton;


    private boolean storeFieldLoaded;

    private ArrayList<ShopDataSerializer.FieldSet> storeFiledArrayList = new ArrayList<>();

    private RecyclerListView listView;
    private LinearLayoutManager layoutManager;
    private ListAdapter adapter;
    private EmptyTextProgressView progressView;

    public static String section = "grandshop-front";

    @Override
    public boolean onFragmentCreate() {
        getNotificationCenter().addObserver(this, NotificationCenter.didConfigrationLoaded);
        getNotificationCenter().addObserver(this,NotificationCenter.didRemoteDataLoaded);

        getShopDataController().loadSectionConfiguration(section,true,classGuid);

        return super.onFragmentCreate();
    }


    @Override
    public void onFragmentDestroy() {

        getNotificationCenter().removeObserver(this, NotificationCenter.didConfigrationLoaded);
        getNotificationCenter().removeObserver(this,NotificationCenter.didRemoteDataLoaded);

        super.onFragmentDestroy();
    }

    private final static int fav = 1;
    private final static int search = 2;
    private final static int qr = 3;

    @Override
    public View createView(Context context) {

       actionBar.setBackButtonImage(R.drawable.ic_ab_back);
       actionBar.setAddToContainer(true);
       actionBar.setCastShadows(false);
       actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick(){
           @Override
           public void onItemClick(int id) {
               if(id == -1){
                   finishFragment();
               }else if(id == qr){
                   HuluChatScanFragment.showAsSheet(StoreActivity.this, true, new HuluChatScanFragment.CameraScanActivityDelegate() {
                       @Override
                       public void didFindQr(String text) {
                           ShopUtils.openFragment(StoreActivity.this,text);
                       }
                   });
               }else if(id == search) {
                  presentFragment(new GlobalSearchActivity());
               }else if(id == fav){
                   presentFragment(new LikeProductActivity());

               }
           }
       });

       int top_margin  = actionBar.getOccupyStatusBar()?(int)(AndroidUtilities.statusBarHeight/AndroidUtilities.density):0;

        SearchField searchField = new SearchField(context){
            @Override
            protected void onFieldTouchUp(EditTextBoldCursor editText) {
                presentFragment(new GlobalSearchActivity());
            }
        };
        searchField.setHint(LocaleController.getString("SearchFor",R.string.SearchFor));
        actionBar.addView(searchField, 0, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, Gravity.BOTTOM | Gravity.LEFT,40, top_margin, 40 + 40, 0));

        ActionBarMenu menu = actionBar.createMenu();
        menu.addItem(qr,R.drawable.wallet_qr);
        menu.addItem(fav,R.drawable.ic_like_line);

        fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = (FrameLayout)fragmentView;
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

        refreshLayout = new SwipeRefreshLayout(context);
        frameLayout.addView(refreshLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));

        adapter = new ListAdapter(context);

        listView = new RecyclerListView(context){
            @Override
            protected boolean allowSelectChildAtPosition(View child) {
                return false;
            }

            @Override
            public boolean hasOverlappingRendering() {
                return false;
            }

            @Override
            public void invalidate() {
                super.invalidate();
                if (fragmentView != null) {
                    fragmentView.invalidate();
                }
            }

        };
        listView.setVerticalScrollBarEnabled(false);
        listView.setLayoutAnimation(null);
        listView.setItemAnimator(null);
        listView.setPadding(0, 0, 0, 0);
        listView.setClipToPadding(false);
        layoutManager = new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false){
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return true;
            }
        };
        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {

            private float totalDy = 0;
            private boolean scrollUp;
            private final int scrollValue = AndroidUtilities.dp(100);


            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                scrollUp = dy < 0;
                int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                if (firstVisibleItem != RecyclerView.NO_POSITION) {
                    int totalItemCount = adapter.getItemCount();
                    if (firstVisibleItem == 0) {
                        if (dy >= 0) {
                            canShowPagedownButton = false;
                            updatePagedownButtonVisibility(true);
                        }
                    } else {
                        if (dy > 0) {
                            if (pagedownButton.getTag() == null) {
                                totalDy += dy;
                                if (totalDy > scrollValue) {
                                    totalDy = 0;
                                    canShowPagedownButton = true;
                                    updatePagedownButtonVisibility(true);
                                }
                            }
                        } else {
                            if (pagedownButton.getTag() != null) {
                                totalDy += dy;
                                if (totalDy < -scrollValue) {
                                    canShowPagedownButton = false;
                                    updatePagedownButtonVisibility(true);
                                    totalDy = 0;
                                }
                            }
                        }
                    }
                }

            }
        });

        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        listView.setLayoutManager(layoutManager);
        listView.setGlowColor(0);
        listView.setAdapter(adapter);
        refreshLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));


        listView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.top = 16;
                outRect.bottom = 16;
                outRect.left = 16;
                outRect.right = 16;
            }
        });

        progressView = new EmptyTextProgressView(context);
        progressView.showProgress();
        frameLayout.addView(progressView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
        listView.setEmptyView(progressView);

        refreshLayout.setOnRefreshListener(() -> refresh());

        pagedownButton = new FrameLayout(context);
        pagedownButton.setVisibility(View.INVISIBLE);
        frameLayout.addView(pagedownButton, LayoutHelper.createFrame(66, 59, Gravity.RIGHT | Gravity.BOTTOM, 0, 0, -3, 5));
        pagedownButton.setOnClickListener(view -> {
            if(layoutManager != null){
                layoutManager.scrollToPosition(0);
            }
        });

        pagedownButtonImage = new ImageView(context);
        pagedownButtonImage.setImageResource(R.drawable.msg_go_up);
        pagedownButtonImage.setScaleType(ImageView.ScaleType.CENTER);
        pagedownButtonImage.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chat_goDownButtonIcon), PorterDuff.Mode.MULTIPLY));
        pagedownButtonImage.setPadding(0, AndroidUtilities.dp(2), 0, 0);
        Drawable drawable;
        if (Build.VERSION.SDK_INT >= 21) {
            pagedownButtonImage.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, AndroidUtilities.dp(42), AndroidUtilities.dp(42));
                }
            });
            drawable = Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(42), Theme.getColor(Theme.key_chat_goDownButton), Theme.getColor(Theme.key_listSelector));
        } else {
            drawable = Theme.createCircleDrawable(AndroidUtilities.dp(42), Theme.getColor(Theme.key_chat_goDownButton));
        }
        Drawable shadowDrawable = context.getResources().getDrawable(R.drawable.pagedown_shadow).mutate();
        shadowDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chat_goDownButtonShadow), PorterDuff.Mode.MULTIPLY));
        CombinedDrawable combinedDrawable = new CombinedDrawable(shadowDrawable, drawable, 0, 0);
        combinedDrawable.setIconSize(AndroidUtilities.dp(42), AndroidUtilities.dp(42));
        drawable = combinedDrawable;
        pagedownButtonImage.setBackgroundDrawable(drawable);

        pagedownButton.addView(pagedownButtonImage, LayoutHelper.createFrame(46, 46, Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM));


        return fragmentView;
    }

    private void refresh(){
        storeFieldLoaded  = false;
        progressView.setVisibility(View.VISIBLE);
        getShopDataController().loadSectionConfiguration(section,true,classGuid);
    }

    private void parseStoreFieldUi(ArrayList<ShopDataSerializer.FieldSet> storeFields){
        if(storeFields == null || storeFields.isEmpty() || storeFieldLoaded || getParentActivity() == null){
            return;
        }
        Context context = getParentActivity();
        storeFieldLoaded = true;
        storeFiledArrayList.clear();
        for(int a = 0 ; a < storeFields.size(); a++)
        {
            ShopDataSerializer.FieldSet filed = storeFields.get(a);
            if(filed == null)
            {
                continue;
            }
            if(ShopUtils.isEmpty(filed.type)){
                continue;
            }
            switch (filed.type) {
                case UI_BANNER:
                    if(bannerContainer == null){
                        bannerContainer = new BannerContainer(context){
                            @Override
                            protected void onCollPressed(ShopDataSerializer.Collection collection) {
                                if (collection != null) {
                                    presentFragment(new CollectionFragment(collection));
                                }
                            }
                        };
                        bannerContainer.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    }
                    break;
                case UI_BUSINESS:
                    if(businessContainer == null){
                        businessContainer = new BusinessContainer(context);
                        businessContainer.setHeader(filed.header);
                        businessContainer.setDelegate(new StoreActivityDelegate() {
                            @Override
                            public void didBusSelected(ShopDataSerializer.ProductType business) {
                                Bundle bundle = new Bundle();
                                bundle.putString("title",business.display_name);
                                bundle.putString("bus_type",business.key);
                                ProductCategoryActivity productBusinessFragment = new ProductCategoryActivity(bundle);
                                presentFragment(productBusinessFragment);
                            }
                        });
                        businessContainer.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    }
                    break;
                case UI_PRODUCT_HORIZONTAL:
                    if(productHorizontalContainer == null){
                        productHorizontalContainer = new ProductHorizontalContainer(context);
                        productHorizontalContainer.setMoreAndSec(filed.remote,filed.key);
                        productHorizontalContainer.setHeader(filed.header);
                        productHorizontalContainer.setDelegate(new StoreActivityDelegate() {
                            @Override
                            public void didFeaturedProductSelected(ShopDataSerializer.FeaturedProduct product) {
                                Bundle bundle = new Bundle();
                                bundle.putInt("chat_id",ShopUtils.toClientChannelId(product.shopSnip.channel));
                                bundle.putInt("item_id",product.id);
                                ProductDetailFragment productDetailFragment = new ProductDetailFragment(bundle);
                                presentFragment(productDetailFragment);
                            }

                            @Override
                            public void didPressedMore(String remote, String sec) {
                                ProductMoreFragment instaShopFragment = new ProductMoreFragment(remote,sec,filed.header);
                                presentFragment(instaShopFragment);
                            }
                        });
                        productHorizontalContainer.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    }
                    break;
                case UI_PRODUCT_VERTICAL:

                    if(productContainer == null){
                       productContainer = new ProductContainer(context);
                       productContainer.setHeader(filed.header);
                       productContainer.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    }
                    break;
                case UI_SHOP_HORIZONTAL:
                    if(shopContainer == null){
                        shopContainer = new ShopContainer(context);
                        shopContainer.setHeader(filed.header);
                        shopContainer.setMoreAndSec(filed.remote,filed.more);
                        shopContainer.setDelegate(new StoreActivityDelegate() {
                            @Override
                            public void didPressFeatureShop(long channel) {
                                Bundle bundle = new Bundle();
                                bundle.putInt("chat_id", ShopUtils.toClientChannelId(channel));
                                BusinessProfileActivity businessProfileActivity = new BusinessProfileActivity(bundle);
                                presentFragment(businessProfileActivity);
                            }

                            @Override
                            public void didPressedMore(String remote, String sec) {
                                if(sec.equals(SEC_GRANDSHOP_FEATURED_INSTASHOPS)){
                                    InstaShopFragment instaShopFragment = new InstaShopFragment(remote,sec);
                                    presentFragment(instaShopFragment);
                                }
                            }
                        });
                        shopContainer.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    }
                    break;
            }
            storeFiledArrayList.add(filed);
            if(!filed.type.equals(UI_BUSINESS) && !ShopUtils.isEmpty(filed.remote)){
                getShopDataController().loadRemoteData(filed.remote,filed.type,classGuid);
            }else{
                getShopDataController().loadSectionConfiguration("product_types",true,classGuid);
            }
        }
        if(adapter != null){
            adapter.notifyDataSetChanged();
        }

    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if(id == NotificationCenter.didConfigrationLoaded){
            boolean loaded = (boolean)args[0];
            String sec = (String)args[1];
            if(loaded){
                if(sec.equals("product_types")){
                    ArrayList<ShopDataSerializer.ProductType>  productTypes = (ArrayList<ShopDataSerializer.ProductType>)args[2];
                    if(businessContainer != null){
                        businessContainer.setBusinesses(productTypes);
                    }
                }else if(sec.equals(section)){
                    ArrayList<ShopDataSerializer.FieldSet> storeFields = (ArrayList<ShopDataSerializer.FieldSet>) args[2];
                    parseStoreFieldUi(storeFields);

                }
                progressView.setVisibility(View.GONE);
                if(adapter != null){
                    adapter.notifyDataSetChanged();
                }
            }else{
                //handle
            }
        }else if(id == NotificationCenter.didRemoteDataLoaded){
            int guid = (int)args[3];
            if(guid == classGuid){
                boolean loaded = (boolean)args[0];
                if(loaded){
                    String type = (String) args[1];
                    String next =(String) args[4];
                    switch (type) {
                        case UI_SHOP_HORIZONTAL: {
                            ArrayList<ShopDataSerializer.ShopSnip> shopSnips = (ArrayList<ShopDataSerializer.ShopSnip>) args[2];
                            if (shopContainer != null) {

                                shopContainer.setData(shopSnips);
                            }
                            break;
                        }
                        case UI_PRODUCT_VERTICAL: {
                            ArrayList<ShopDataSerializer.FeaturedProduct> shopSnips = (ArrayList<ShopDataSerializer.FeaturedProduct>) args[2];
                            if (productContainer != null) {
                                productContainer.clearProduct();
                                productContainer.setNext(next);
                                productContainer.setProductEndReached(ShopUtils.isEmpty(next));
                                productContainer.setProductData(shopSnips);

                            }
                            break;
                        }
                        case UI_PRODUCT_HORIZONTAL:
                        ArrayList<ShopDataSerializer.FeaturedProduct> shopSnips = (ArrayList<ShopDataSerializer.FeaturedProduct>)args[2];
                        if(productHorizontalContainer != null){
                            productHorizontalContainer.setProductData(shopSnips);

                        }

                        break;
                        case UI_BANNER:
                        ArrayList<ShopDataSerializer.Collection> collections= (ArrayList<ShopDataSerializer.Collection>)args[2];
                        if(bannerContainer != null){
                            bannerContainer.setCollections(collections);
                        }

                        break;
                    }
                }else{
                    APIError apiError  = (APIError)args[1];
                }
                if(refreshLayout != null){
                    refreshLayout.setRefreshing(false);
                }

            }
        }
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter{

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
            View view;
            switch (viewType){
                case 1:
                    if (bannerContainer.getParent() != null) {
                        ((ViewGroup) bannerContainer.getParent()).removeView(bannerContainer);
                    }
                    view = bannerContainer;
                    break;
                case 2:
                    if (businessContainer.getParent() != null) {
                        ((ViewGroup) businessContainer.getParent()).removeView(businessContainer);
                    }
                    view = businessContainer;
                    break;
                case 3:
                    if (productContainer.getParent() != null) {
                        ((ViewGroup) productContainer.getParent()).removeView(productContainer);
                    }
                    view = productContainer;
                    break;
                case 4:
                    if (shopContainer.getParent() != null) {
                        ((ViewGroup) shopContainer.getParent()).removeView(shopContainer);
                    }
                    view = shopContainer;
                    break;
                case 5:
                    if (productHorizontalContainer.getParent() != null) {
                        ((ViewGroup) productHorizontalContainer.getParent()).removeView(productHorizontalContainer);
                    }
                    view = productHorizontalContainer;
                    break;
                case 6:
                default:
                    view = new EmptyCell(mContext);
                    break;
            }
            return new RecyclerListView.Holder(view);
        }


        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        }

        @Override
        public int getItemViewType(int position) {
            ShopDataSerializer.FieldSet storeFiled = storeFiledArrayList.get(position);
            switch (storeFiled.type){
                case UI_BANNER:
                    return 1;
                case UI_BUSINESS:
                    return 2;
                case UI_PRODUCT_VERTICAL:
                    return 3;
                case UI_SHOP_HORIZONTAL:
                    return 4;
                case UI_PRODUCT_HORIZONTAL:
                    return 5;
            }
            return super.getItemViewType(position);
        }

        @Override
        public int getItemCount() {
            int count = 0;
            if(storeFiledArrayList != null){
                count = storeFiledArrayList.size();
            }
            return count;
        }
    }

    @Override
    public boolean isSwipeBackEnabled(MotionEvent event) {
        return false;
    }


    private  static class ShopContainer extends FrameLayout{

        private  ArrayList<ShopDataSerializer.ShopSnip> shopSnips = new ArrayList<>();
        private ShopAdapter adapter;
        private TitleCell titleCell;
        private TextView textView;
        private StoreActivityDelegate delegate;
        private RecyclerListView listView;
        private  FrameLayout loadingLayout;

        private String more_sec;
        private String remote;


        public void setMoreAndSec(String remote,String moreSec){
            this.remote = remote;
            this.more_sec = moreSec;
        }

        public ShopContainer(Context context) {
            super(context);

            titleCell = new TitleCell(context);
            addView(titleCell,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,60,Gravity.LEFT|Gravity.TOP,21,0,100,0));

            textView = new TextView(context);
            textView.setTextColor(Theme.getColor(Theme.key_dialogTextBlue));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            textView.setLines(1);
            textView.setMaxLines(1);

            textView.setSingleLine(true);
            textView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            textView.setCompoundDrawablePadding(AndroidUtilities.dp(4));
            addView(textView,LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT,60,Gravity.RIGHT|Gravity.TOP,0,0,16,0));
            setHeaderTextAndIcon("See All",R.drawable.message_arrow);
            textView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(delegate != null){
                        delegate.didPressedMore(remote,more_sec);
                    }
                }
            });

            listView = new RecyclerListView(context);
            listView.setTag(9);
            listView.setLayoutManager(new LinearLayoutManager(context,RecyclerView.HORIZONTAL, false){
                @Override
                public boolean supportsPredictiveItemAnimations() {
                    return false;
                }
            });
            listView.setAdapter(adapter = new ShopAdapter());
            listView.setItemAnimator(null);
            listView.setVisibility(GONE);
            listView.setLayoutAnimation(null);
            addView(listView,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,Gravity.LEFT|Gravity.TOP,0,60,0,0));

            listView.setOnItemClickListener((view, position) -> {
                if(delegate != null){
                    delegate.didPressFeatureShop(shopSnips.get(position).channel);
                }
            });

            loadingLayout = new FrameLayout(context);
            loadingLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            LoadingView loadingView = new LoadingView(context){

                @Override
                public int getColumnsCount() {
                    return 4;
                }
            };
            loadingView.setIsSingleCell(false);
            loadingView.setViewType(LoadingView.FEATURED_SHOP);
            loadingLayout.addView(loadingView,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.MATCH_PARENT,Gravity.LEFT|Gravity.TOP,0,16,0,16));

            addView(loadingLayout,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,210));

        }



    public void setHeaderTextAndIcon(String text, int resId) {
        try {
            textView.setText(text);
            Drawable drawable = getResources().getDrawable(resId).mutate();
            if (drawable != null) {
                drawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextBlue), PorterDuff.Mode.MULTIPLY));
            }
            textView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
        } catch (Throwable e) {
            FileLog.e(e);
        }
    }

      public void setDelegate(StoreActivityDelegate delegate) {
            this.delegate = delegate;
        }

        public void setOnMoreClikListner(OnClickListener onMoreClikListner){
           // moreImage.setVisibility(VISIBLE);
            //moreImage.setOnClickListener(onMoreClikListner);
        }

        public void setHeader(String header){
            titleCell.setText(header);
        }

        public void setData(ArrayList<ShopDataSerializer.ShopSnip> snips){
            shopSnips = snips;
            if(adapter != null){
                adapter.notifyDataSetChanged();
            }
            if(listView.getVisibility() != VISIBLE){
                listView.setVisibility(VISIBLE);
            }


            if(loadingLayout.getVisibility() == VISIBLE){
                loadingLayout.setVisibility(GONE);
            }
        }

        private class ShopAdapter extends RecyclerListView.SelectionAdapter{

            @Override
            public boolean isEnabled(RecyclerView.ViewHolder holder) {
                return true;
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = new ShopCell(getContext());
                view.setLayoutParams(new RecyclerView.LayoutParams(AndroidUtilities.dp(90), AndroidUtilities.dp(90)));
                return new RecyclerListView.Holder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                ShopCell cell = (ShopCell) holder.itemView;
                ShopDataSerializer.ShopSnip shop = shopSnips.get(position);
                 cell.setShop(shop);
            }

            @Override
            public int getItemCount() {
                int count = 0;
                count =  shopSnips.size();
                return count;
            }
        }

        private static class ShopCell extends FrameLayout {

            private BackupImageView imageView;
            private TextView nameTextView;

            private Paint paint = new Paint();

            public ShopCell(Context context) {
                super(context);

                setWillNotDraw(false);

                imageView = new BackupImageView(context){
                    @Override
                    protected void onDraw(Canvas canvas) {
                        canvas.drawCircle(getMeasuredWidth()/2,getMeasuredHeight()/2,AndroidUtilities.dp(30),Theme.avatar_backgroundPaint);
                        super.onDraw(canvas);
                    }
                };
                imageView.setRoundRadius(AndroidUtilities.dp(27));
                imageView.setSize(AndroidUtilities.dp(54),AndroidUtilities.dp(54));
                addView(imageView, LayoutHelper.createFrame(60, 60, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 7, 0, 0));

                nameTextView = new TextView(context);
                nameTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
                nameTextView.setMaxLines(1);
                nameTextView.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
                nameTextView.setLines(1);
                nameTextView.setEllipsize(TextUtils.TruncateAt.END);
                addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 6, 70, 6, 0));
            }


            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(86 ), MeasureSpec.EXACTLY));
            }

            public void setShop(ShopDataSerializer.ShopSnip shopSnip){
                if(shopSnip == null){
                    return;
                }
                nameTextView.setText(shopSnip.title);
                AvatarDrawable avatarDrawable = new AvatarDrawable();
                avatarDrawable.setInfo(5,shopSnip.title,null);
                if(shopSnip.profile_picture != null){
                    imageView.setImage(shopSnip.profile_picture.photo,"50_50",avatarDrawable);
                }else{
                    imageView.setImage(null,null,avatarDrawable);
                }
            }

        }

    }

    public static class BannerContainer extends FrameLayout{

        private BottomPagesView bottomPages;
        private ViewPager viewPager;
        private Adapter adapter;
        private ArrayList<ShopDataSerializer.Collection> collections = new ArrayList<>();
        private LoadingView loadingView;


        private Runnable runnable  = new Runnable() {
            @Override
            public void run() {
                if(viewPager != null && adapter != null){

                   int current =  viewPager.getCurrentItem();//0,1,2
                   int total = adapter.getCount();//3
                   if(current < total){
                       current = current + 1;
                   }
                   if(current == total){
                       current = 0;
                   }
                   viewPager.setCurrentItem(current);
                }
                AndroidUtilities.runOnUIThread(runnable,3000);

            }
        };


        public BannerContainer(Context context) {
            super(context);

            viewPager = new ViewPager(context) {
                @Override
                public boolean onInterceptTouchEvent(MotionEvent ev) {
                    if (getParent() != null) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    return super.onInterceptTouchEvent(ev);
                }

                @Override
                protected void onAttachedToWindow() {
                    super.onAttachedToWindow();
                    requestLayout();
                }
            };
            AndroidUtilities.setViewPagerEdgeEffectColor(viewPager, Theme.getColor(Theme.key_actionBarDefaultArchived));
            viewPager.setAdapter(adapter = new Adapter());
            viewPager.setPageMargin(0);
            viewPager.setOffscreenPageLimit(1);
            addView(viewPager, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    bottomPages.setPageOffset(position, positionOffset);
                }

                @Override
                public void onPageSelected(int i) {
                    FileLog.d("test1");
                }

                @Override
                public void onPageScrollStateChanged(int i) {
                    FileLog.d("test1");
                }
            });

            bottomPages = new BottomPagesView(getContext(), viewPager, 0);
            bottomPages.setColor(Theme.key_chats_unreadCounterMuted, Theme.key_chats_actionBackground);
            addView(bottomPages, LayoutHelper.createFrame(33, 5, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0, 0, 19));

            loadingView = new LoadingView(context){
                @Override
                public int getColumnsCount() {
                    return 1;
                }
            };
            loadingView.setViewType(LoadingView.BANNER_TYPE);
            addView(loadingView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT,Gravity.CENTER_HORIZONTAL));




        }

        protected  void onCollPressed(ShopDataSerializer.Collection collection){

        }

        public void onDestroy(){
            if(runnable != null){
                AndroidUtilities.cancelRunOnUIThread(runnable);
            }
        }

        public void setCollections(ArrayList<ShopDataSerializer.Collection> collections) {
            this.collections = collections;
            if(collections.size() <= 1){
                bottomPages.setVisibility(GONE);
            }else{
                bottomPages.setVisibility(VISIBLE);
                bottomPages.setPagesCount(collections.size());
                bottomPages.invalidate();
            }
            if(adapter != null){
                adapter.notifyDataSetChanged();
            }
            if(loadingView.getVisibility() == VISIBLE){
                loadingView.setVisibility(GONE);
            }
            AndroidUtilities.runOnUIThread(runnable,3000);

        }

        @Override
        public void invalidate() {
            super.invalidate();
            bottomPages.invalidate();
        }
        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(216), MeasureSpec.EXACTLY));
        }

        private class Adapter extends PagerAdapter {
            @Override
            public int getCount() {
                return collections != null?collections.size():0;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                BannerInnerCell innerCell = new BannerInnerCell(container.getContext(), collections.get(position));

                innerCell.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onCollPressed(collections.get(position));
                    }
                });
                if (innerCell.getParent() != null) {
                    ViewGroup parent = (ViewGroup) innerCell.getParent();
                    parent.removeView(innerCell);
                }
                container.addView(innerCell, 0);
                return innerCell;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
            }

            @Override
            public void setPrimaryItem(ViewGroup container, int position, Object object) {
                super.setPrimaryItem(container, position, object);
                bottomPages.setCurrentPage(position);
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view.equals(object);
            }

            @Override
            public void restoreState(Parcelable arg0, ClassLoader arg1) {
            }

            @Override
            public Parcelable saveState() {
                return null;
            }

            @Override
            public void unregisterDataSetObserver(DataSetObserver observer) {
                if (observer != null) {
                    super.unregisterDataSetObserver(observer);
                }
            }
        }

        private static class BannerInnerCell extends FrameLayout{

            private BackupImageView backupImageView;
            private ShopDataSerializer.Collection collection;
            private FrameLayout container;
            private TextView textView;

            public BannerInnerCell(Context context, ShopDataSerializer.Collection col) {
                super(context);

                collection = col;
                int width =(int) ( getItemSize(1) / AndroidUtilities.density);

                backupImageView = new BackupImageView(context);
                backupImageView.setRoundRadius(AndroidUtilities.dp(16));
//                backupImageView.getImageReceiver().setRoundRadius(AndroidUtilities.dp(16),AndroidUtilities.dp(16),0,0);
                addView(backupImageView,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.MATCH_PARENT,Gravity.CENTER_HORIZONTAL|Gravity.TOP,8,8,8,8));

                if(collection != null && collection.photo != null){
                   String link =   collection.photo.photo;
                   backupImageView.setImage(link,null,null);
                }

                container = new FrameLayout(context);
                container.setBackground(ShopUtils.createBottomRoundRectDrawable(32,Theme.getColor(Theme.key_chat_mediaTimeBackground)));
                container.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16));
                addView(container, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.LEFT, 8, 8, 8, 8));

                textView = new TextView(context);
                textView.setTextColor(0xffffffff);
                textView.setTypeface(AndroidUtilities.getTypeface("fonts/Roboto-Black.ttf"));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                textView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
                container.addView(textView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.BOTTOM, 13, 0, 0, 16));
                textView.setText(col.title);
            }
        }
    }

    private  static class BusinessContainer extends FrameLayout{

        private StoreActivityDelegate delegate;

        private ArrayList<ShopDataSerializer.ProductType> businesses =new ArrayList<>();
        private ProductCategoryListAdapter adapter;
        private TitleCell titleCell;
        private ImageView moreImage;

        public BusinessContainer(Context context) {
            super(context);


            titleCell = new TitleCell(context);
            addView(titleCell,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,60,Gravity.LEFT|Gravity.TOP,21,0,60,0));

            moreImage = new ImageView(context);
            moreImage.setImageResource(R.drawable.message_arrow);
            moreImage.setScaleType(ImageView.ScaleType.CENTER);
            moreImage.setVisibility(GONE);
            moreImage.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextBlack), PorterDuff.Mode.MULTIPLY));
            addView(moreImage,LayoutHelper.createFrame(60,60,Gravity.RIGHT|Gravity.TOP,0,0,0,0));

            RecyclerListView gridListView = new RecyclerListView(context);
            gridListView.setTag(9);
            gridListView.setItemAnimator(null);
            gridListView.setLayoutAnimation(null);
            GridLayoutManager gridLayoutManager = new GridLayoutManager(context,4) {
                @Override
                public boolean supportsPredictiveItemAnimations() {
                    return false;
                }
            };
            gridListView.setLayoutManager(gridLayoutManager);
            gridListView.setAdapter(adapter = new ProductCategoryListAdapter());
            addView(gridListView,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,Gravity.LEFT|Gravity.TOP,0,60,0,0));

            gridListView.setOnItemClickListener(new RecyclerListView.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    ShopDataSerializer.ProductType productType = businesses.get(position);
                    if( delegate != null){
                        delegate.didBusSelected(productType);
                    }
                }
            });

        }

        public void setDelegate(StoreActivityDelegate delegate) {
            this.delegate = delegate;
        }

        public void setOnMoreClikListner(OnClickListener onMoreClikListner){
            moreImage.setVisibility(VISIBLE);
            moreImage.setOnClickListener(onMoreClikListner);
        }

        public void setHeader(String header){
            titleCell.setText(header);
        }

        public void setBusinesses(ArrayList<ShopDataSerializer.ProductType> businesses) {
            this.businesses = businesses;
            if(adapter != null){
                adapter.notifyDataSetChanged();
            }
        }

        private class ProductCategoryListAdapter extends RecyclerListView.SelectionAdapter{

            @Override
            public int getItemCount() {
                return businesses.size();
            }

            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View  view = new BusinessCell(getContext());
                view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, AndroidUtilities.dp(100)));
                return new RecyclerListView.Holder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                BusinessCell shareDialogCell = (BusinessCell)holder.itemView;
                shareDialogCell.setBusiness(businesses.get(position));
            }

            @Override
            public boolean isEnabled(RecyclerView.ViewHolder holder) {
                return true;
            }

        }

    }

    private  class ProductContainer extends FrameLayout{

        private TitleCell titleCell;
        private ImageView moreImage;
        private Paint backgroundPaint = new Paint();

        private RecyclerListView listView;
        private GridLayoutManager layoutManager;
        private ImageView emptyImageView;
        private LinearLayout emptyView;
        private TextView emptyTextView;
        private LinearLayout progressView;
        private RadialProgressView progressBar;
        private ClippingImageView animatingImageView;
        private RecyclerAnimationScrollHelper scrollHelper;
        private int selectedType;
        private ArrayList<ProductCell> cellCache = new ArrayList<>(10);
        private ArrayList<ProductCell> cache = new ArrayList<>(10);

        private boolean isLoadingProduct;
        private boolean productEndReached;
        private String nextLink;

        public void setNext(String next) {
            this.nextLink = next;
        }

        public void clearProduct(){
            products.clear();

        }

        public void setLoading(boolean val){
            isLoadingProduct = val;
        }

        public void setProductEndReached(boolean endreached){
            productEndReached = endreached;
        }

        private ProductAdapter productAdapter;

        private FrameLayout loadingLayout;


        private ArrayList<ShopDataSerializer.FeaturedProduct> products = new ArrayList<>();

        public ProductContainer(@NonNull Context context) {
            super(context);

            productAdapter = new ProductAdapter();
            listView = new RecyclerListView(context) {
                @Override
                protected void onLayout(boolean changed, int l, int t, int r, int b) {
                    super.onLayout(changed, l, t, r, b);
                    checkLoadMoreScroll();
                }
            };
            listView.setFocusable(true);
            listView.setFocusableInTouchMode(true);
            listView.setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING);
            listView.setPadding(0, AndroidUtilities.dp(2), 0, 0);
            listView.setItemAnimator(null);
            listView.setAdapter(productAdapter);
            listView.setClipToPadding(false);
            listView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

            layoutManager = new GridLayoutManager(context,2);
            listView.setLayoutManager(layoutManager);
            listView.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                    outRect.left = AndroidUtilities.dp(4);
                    outRect.right = AndroidUtilities.dp(4);
                    outRect.bottom = AndroidUtilities.dp (4);
                    outRect.top = AndroidUtilities.dp(4);
                }
            });
           listView.setPadding(AndroidUtilities.dp(8),0,AndroidUtilities.dp(8),0);
           addView(listView,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,Gravity.LEFT|Gravity.TOP,0,60,0,0));

           listView.setOnItemClickListener((view, position) -> {

               if(position < 0 || position >= products.size()){
                   return;
               }
               ShopDataSerializer.FeaturedProduct pro = products.get(position);
               if(pro == null){
                   return;
               }
               Bundle bundle = new Bundle();
               bundle.putInt("chat_id",ShopUtils.toClientChannelId(pro.shopSnip.channel));
               bundle.putInt("item_id",pro.id);
               ProductDetailFragment productDetailFragment = new ProductDetailFragment(bundle);
               StoreActivity.this.presentFragment(productDetailFragment);
           });

//           layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
//               @Override
//               public int getSpanSize(int position) {
//                   if(adapter != null){
//                     int type =   productAdapter.getItemViewType(position);
//                     if(type == 2){
//                         return 2;
//                     }
//                   }
//                   return 1;
//               }
//           });


           emptyView = new LinearLayout(context);
           emptyView.setWillNotDraw(false);
           emptyView.setOrientation(LinearLayout.VERTICAL);
           emptyView.setGravity(Gravity.CENTER);
           emptyView.setVisibility(View.GONE);
           addView(emptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

           emptyView.setOnTouchListener((v, event) -> true);

           emptyImageView = new ImageView(context);
           emptyView.addView(emptyImageView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

           emptyTextView = new TextView(context);
           emptyTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
           emptyTextView.setGravity(Gravity.CENTER);
           emptyTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
           emptyTextView.setPadding(AndroidUtilities.dp(40), 0, AndroidUtilities.dp(40), AndroidUtilities.dp(128));
           emptyView.addView(emptyTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 0, 24, 0, 0));

            progressView = new LinearLayout(context) {
                @Override
                protected void onDraw(Canvas canvas) {
                    backgroundPaint.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), backgroundPaint);
                }
            };
            progressView.setWillNotDraw(false);
            progressView.setGravity(Gravity.CENTER);
            progressView.setOrientation(LinearLayout.VERTICAL);
            progressView.setVisibility(View.GONE);
            addView(progressView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

            scrollHelper = new RecyclerAnimationScrollHelper(listView, layoutManager);

            emptyImageView.setImageResource(R.drawable.smiles_info);
            emptyTextView.setText("No  listing found!");

            titleCell = new TitleCell(context);
            addView(titleCell,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,60,Gravity.LEFT|Gravity.TOP,21,0,60,0));

            moreImage = new ImageView(context);
            moreImage.setImageResource(R.drawable.message_arrow);
            moreImage.setScaleType(ImageView.ScaleType.CENTER);
            moreImage.setVisibility(GONE);
            moreImage.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextBlue), PorterDuff.Mode.MULTIPLY));
            addView(moreImage,LayoutHelper.createFrame(60,60,Gravity.RIGHT|Gravity.TOP,0,0,0,0));

            listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    checkLoadMoreScroll();
                }
            });

            loadingLayout = new FrameLayout(context);
            loadingLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            FlickerLoadingView loadingView = new FlickerLoadingView(context){

                @Override
                public int getColumnsCount() {
                    return 2;
                }
            };
            loadingView.setIsSingleCell(false);
            loadingView.setViewType(FlickerLoadingView.PHOTOS_TYPE);
            loadingLayout.addView(loadingView,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.MATCH_PARENT,Gravity.LEFT|Gravity.TOP,0,16,0,16));

            addView(loadingLayout,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.MATCH_PARENT));
        }

        public void setHeader(String header){
            titleCell.setText(header);
        }


        public void setProductData(ArrayList<ShopDataSerializer.FeaturedProduct> featuredProducts){
            if(listView != null){
                listView.stopScroll();
            }
            int oldCount = products.size();
            products.addAll(featuredProducts);
            if(productAdapter != null){
                if(oldCount > 0){
                    productAdapter.notifyItemRangeInserted(oldCount,featuredProducts.size());
                }else{
                    productAdapter.notifyDataSetChanged();
                }
            }

            if(loadingLayout != null){
                loadingLayout.setVisibility(GONE);
            }
        }


        private void checkLoadMoreScroll() {
                if (listView.getAdapter() == null || ShopUtils.isEmpty(nextLink)) {
                    return;
                }
                int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                int visibleItemCount = firstVisibleItem == RecyclerView.NO_POSITION ? 0 : Math.abs(layoutManager.findLastVisibleItemPosition() - firstVisibleItem) + 1;
                int totalItemCount = listView.getAdapter().getItemCount();
                final int threshold = 3;
                if (firstVisibleItem + visibleItemCount > totalItemCount - threshold && !isLoadingProduct) {
                    if(!productEndReached){
                        isLoadingProduct = true;
                        getShopDataController().loadMoreFeatureProduct(nextLink, (response, error, next, count) -> AndroidUtilities.runOnUIThread(() -> {
                           if(error == null){
                               isLoadingProduct = false;
                               nextLink = next;
                               productEndReached = ShopUtils.isEmpty(nextLink);
                               setProductData((ArrayList<ShopDataSerializer.FeaturedProduct>)response);
                           }
                       }));
                    }
                }
            }


        public RecyclerListView getListView() {
            return listView;
        }

        private class ProductAdapter extends RecyclerListView.SelectionAdapter{

            @Override
            public boolean isEnabled(RecyclerView.ViewHolder holder) {
                return holder.getItemViewType() == 1;
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view;
                switch (viewType){
                    case 1:
                        if (!cellCache.isEmpty()) {
                            view = cellCache.get(0);
                            cellCache.remove(0);
                            ViewGroup p = (ViewGroup) view.getParent();
                            if (p != null) {
                                p.removeView(view);
                            }
                        } else {
                            view = new ProductCell(getContext(),true);
                        }
                        cache.add(( ProductCell)view);
                        view.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        break;
                    case 2:
                    default:
                        FlickerLoadingView flickerLoadingView = new FlickerLoadingView(getContext()){
                            @Override
                            public int getColumnsCount() {
                                return 1;
                            }
                        };
                        flickerLoadingView.setIsSingleCell(true);
                        flickerLoadingView.showDate(false);
                        flickerLoadingView.setViewType(FlickerLoadingView.PHOTOS_TYPE);
                        view = flickerLoadingView;
                        view.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                }

                return new RecyclerListView.Holder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

                if(holder.getItemViewType() == 1){
                    ProductCell cell = (ProductCell) holder.itemView;
                    cell.setDelegate(new ProductCell.ProductDelegate() {
                        @Override
                        public void onShopClicked(long chat_id) {
                            Bundle bundle = new Bundle();
                            bundle.putInt("chat_id", ShopUtils.toClientChannelId(chat_id));
                            BusinessProfileActivity businessProfileActivity = new BusinessProfileActivity(bundle);
                            presentFragment(businessProfileActivity);
                        }

                    });


                    cell.setDelegate(new ProductCell.ProductDelegate() {
                        @Override
                        public void onFavSelected(long chat_id, int product_id, boolean favorite) {
                            AndroidUtilities.runOnUIThread(new Runnable() {
                                @Override
                                public void run() {
                                    ShopDataController.getInstance(currentAccount).checkFav(favorite, chat_id, product_id, susscess -> {
//
                                    });
                                }
                            },300);
                        }
                        @Override
                        public void onShopClicked(long chat_id) {
                            Bundle bundle = new Bundle();
                            bundle.putInt("chat_id", ShopUtils.toClientChannelId(chat_id));
                            BusinessProfileActivity businessProfileActivity = new BusinessProfileActivity(bundle);
                            presentFragment(businessProfileActivity);
                        }
                    });
                    ShopDataSerializer.FeaturedProduct messageObject = products.get(position);
                    cell.setFeatureProduct(messageObject);
                }

            }


            @Override
            public int getItemViewType(int position) {
                if(position < products.size()){
                    return 1;
                }
                return 2;
            }


            @Override
            public int getItemCount() {
                int count = products.size();
                if(!productEndReached && isLoadingProduct){
                    count = count + 1;
                }
                return count;
            }
        }
    }

    private  class ProductHorizontalContainer extends FrameLayout{

        private String more_sec;
        private String remote;


        public void setMoreAndSec(String remote,String moreSec){
            this.remote = remote;
            this.more_sec = moreSec;
        }


        private TitleCell titleCell;
        private TextView textView;

        private Paint backgroundPaint = new Paint();

        private RecyclerListView listView;
        private LinearLayoutManager layoutManager;
        private ImageView emptyImageView;
        private LinearLayout emptyView;
        private TextView emptyTextView;
        private LinearLayout progressView;
        private RadialProgressView progressBar;
        private ClippingImageView animatingImageView;
        private RecyclerAnimationScrollHelper scrollHelper;
        private int selectedType;
        private ArrayList<ProductCell> cellCache = new ArrayList<>(10);
        private ArrayList<ProductCell> cache = new ArrayList<>(10);

        private boolean isLoadingProduct;
        private boolean productEndReached;

        private ProductAdapter productAdapter;

        private StoreActivityDelegate delegate;

        private FrameLayout loadingLayout;
        private LoadingView loadingView;




        public void setDelegate(StoreActivityDelegate delegate) {
            this.delegate = delegate;
        }


        private ArrayList<ShopDataSerializer.FeaturedProduct> products = new ArrayList<>();

        public ProductHorizontalContainer(@NonNull Context context) {
            super(context);

            productAdapter = new ProductAdapter();
            listView = new RecyclerListView(context) {
                @Override
                protected void onLayout(boolean changed, int l, int t, int r, int b) {
                    super.onLayout(changed, l, t, r, b);
                    // checkLoadMoreScroll(listView, layoutManager);
                }
            };
            listView.setFocusable(true);
            listView.setFocusableInTouchMode(true);
            listView.setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING);
            listView.setPinnedSectionOffsetY(-AndroidUtilities.dp(2));
            listView.setPadding(0, AndroidUtilities.dp(2), 0, 0);
            listView.setItemAnimator(null);
            listView.setAdapter(productAdapter);
            listView.setClipToPadding(false);
            listView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            //listView.setHorizontalFadingEdgeEnabled(true);
           // listView.setGlowColor(ShopUtils.getIntAlphaColor(Theme.getColor(Theme.key_windowBackgroundWhite),0.4f));

            layoutManager = new LinearLayoutManager(context,RecyclerView.HORIZONTAL,false);
            listView.setLayoutManager(layoutManager);
            listView.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                    outRect.left = AndroidUtilities.dp(4);
                    outRect.right = AndroidUtilities.dp(4);
                    outRect.bottom = AndroidUtilities.dp (4);
                    outRect.top = AndroidUtilities.dp(4);
                }
            });
            listView.setPadding(AndroidUtilities.dp(8),0,AndroidUtilities.dp(8),0);
            addView(listView,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,Gravity.LEFT|Gravity.TOP,0,60,0,0));

            listView.setOnItemClickListener((view, position) -> {
                if(delegate != null){
                    delegate.didFeaturedProductSelected(products.get(position));
                }
            });

            emptyView = new LinearLayout(context);
            emptyView.setWillNotDraw(false);
            emptyView.setOrientation(LinearLayout.VERTICAL);
            emptyView.setGravity(Gravity.CENTER);
            emptyView.setVisibility(View.GONE);
            addView(emptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

            emptyView.setOnTouchListener((v, event) -> true);

            emptyImageView = new ImageView(context);
            emptyView.addView(emptyImageView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

            emptyTextView = new TextView(context);
            emptyTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
            emptyTextView.setGravity(Gravity.CENTER);
            emptyTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
            emptyTextView.setPadding(AndroidUtilities.dp(40), 0, AndroidUtilities.dp(40), AndroidUtilities.dp(128));
            emptyView.addView(emptyTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 0, 24, 0, 0));

            progressView = new LinearLayout(context) {
                @Override
                protected void onDraw(Canvas canvas) {
                    backgroundPaint.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), backgroundPaint);
                }
            };

            progressView.setWillNotDraw(false);
            progressView.setGravity(Gravity.CENTER);
            progressView.setOrientation(LinearLayout.VERTICAL);
            progressView.setVisibility(View.GONE);
            addView(progressView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

            scrollHelper = new RecyclerAnimationScrollHelper(listView, layoutManager);

            emptyImageView.setImageResource(R.drawable.smiles_info);
            emptyTextView.setText("No  listing found!");

            titleCell = new TitleCell(context);
            addView(titleCell,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,60,Gravity.LEFT|Gravity.TOP,21,0,100,0));

            textView = new TextView(context);
            textView.setTextColor(Theme.getColor(Theme.key_dialogTextBlue));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            textView.setLines(1);
            textView.setMaxLines(1);
            textView.setSingleLine(true);
            textView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            textView.setCompoundDrawablePadding(AndroidUtilities.dp(4));
            addView(textView,LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT,60,Gravity.RIGHT|Gravity.TOP,0,0,16,0));
            setHeaderTextAndIcon("See All",R.drawable.message_arrow);

            textView.setOnClickListener(v -> {
                if(delegate != null){
                    delegate.didPressedMore(remote,more_sec);
                }
            });
            loadingLayout = new FrameLayout(context);
            loadingLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            LoadingView loadingView = new LoadingView(context){

                @Override
                public int getColumnsCount() {
                    return 3;
                }
            };
            loadingView.setIsSingleCell(false);
            loadingView.setViewType(LoadingView.PRODUCT_HORIZONTAL);
            loadingLayout.addView(loadingView,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.MATCH_PARENT,Gravity.LEFT|Gravity.TOP,0,16,0,16));
            int itemSize =(int) ( getItemSize(2) / AndroidUtilities.density) ;
            itemSize = (int) (itemSize - itemSize*0.2);
            addView(loadingLayout,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,itemSize + 72 + 80));
        }

        public void setHeaderTextAndIcon(String text, int resId) {
            try {
                textView.setText(text);
                Drawable drawable = getResources().getDrawable(resId).mutate();
                if (drawable != null) {
                    drawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextBlue), PorterDuff.Mode.MULTIPLY));
                }
                textView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
            } catch (Throwable e) {
                FileLog.e(e);
            }
        }

        public void setDelegate(OnClickListener delegate){
            textView.setVisibility(VISIBLE);
            textView.setOnClickListener(delegate);
        }


        public void setHeader(String header){
            titleCell.setText(header);
        }

        public void setProductData(ArrayList<ShopDataSerializer.FeaturedProduct> featuredProducts){
            if(listView != null){
              //  listView.stopScroll();
            }
            int oldCount = products.size();
            products.addAll(featuredProducts);
            if(productAdapter != null){
                if(oldCount > 0){
                    productAdapter.notifyItemRangeInserted(oldCount,products.size());
                }else{
                    productAdapter.notifyDataSetChanged();
                }
            }
            if(loadingLayout.getVisibility() == VISIBLE){
                loadingLayout.setVisibility(GONE);
            }

        }

        private void checkLoadMoreScroll(RecyclerListView listView,GridLayoutManager layoutManager){

        }

        public RecyclerListView getListView() {
            return listView;
        }

        private class ProductAdapter extends RecyclerListView.SelectionAdapter{

            @Override
            public boolean isEnabled(RecyclerView.ViewHolder holder) {
                return true;
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                ProductCell view;
                if (!cellCache.isEmpty()) {
                    view = cellCache.get(0);
                    cellCache.remove(0);
                    ViewGroup p = (ViewGroup) view.getParent();
                    if (p != null) {
                        p.removeView(view);
                    }
                } else {
                    view = new ProductCell(getContext(),true);
                }
                cache.add(view);
                return new RecyclerListView.Holder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                ProductCell cell = (ProductCell) holder.itemView;
                ShopDataSerializer.FeaturedProduct product = products.get(position);
                cell.setFeatureProduct(product);
                cell.setDelegate(new ProductCell.ProductDelegate() {
                    @Override
                    public void onFavSelected(long chat_id, int product_id, boolean favorite) {
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                ShopDataController.getInstance(currentAccount).checkFav(favorite, chat_id, product_id, susscess -> {
//
                                });
                            }
                        },300);
                    }
                    @Override
                    public void onShopClicked(long chat_id) {
                        Bundle bundle = new Bundle();
                        bundle.putInt("chat_id", ShopUtils.toClientChannelId(chat_id));
                        BusinessProfileActivity businessProfileActivity = new BusinessProfileActivity(bundle);
                        presentFragment(businessProfileActivity);
                    }
                });

            }


            @Override
            public int getItemViewType(int position) {
                return super.getItemViewType(position);
            }

            @Override
            public int getItemCount() {
                int count = 0;
                if(products != null){
                    count = products.size();
                }
                return count;
            }
        }
    }

    private static class TitleCell extends FrameLayout {

        private TextView titleView;

        public TitleCell(Context context) {
            super(context);

            titleView = new TextView(getContext());
            titleView.setLines(1);
            titleView.setSingleLine(true);
            titleView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            titleView.setTypeface(AndroidUtilities.getTypeface("fonts/Roboto-Black.ttf"));
            titleView.setPadding(AndroidUtilities.dp(4), AndroidUtilities.dp(15), AndroidUtilities.dp(22), AndroidUtilities.dp(8));
            titleView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
            titleView.setGravity(Gravity.CENTER_VERTICAL);
            addView(titleView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 60));
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(60), MeasureSpec.EXACTLY));
        }

        public void setText(String text) {
            titleView.setText(text);
        }
    }


    private void updatePagedownButtonVisibility(boolean animated) {
        if (pagedownButton == null) {
            return;
        }
        boolean show = canShowPagedownButton;
        if (show) {

            if (pagedownButton.getTag() == null) {
                if (pagedownButtonAnimation != null) {
                    pagedownButtonAnimation.cancel();
                    pagedownButtonAnimation = null;
                }
                pagedownButton.setTag(1);
                if (animated) {
                    if (pagedownButton.getTranslationY() == 0) {
                        pagedownButton.setTranslationY(AndroidUtilities.dp(100));
                    }
                    pagedownButton.setVisibility(View.VISIBLE);
                    pagedownButtonAnimation = new AnimatorSet();
                    pagedownButtonAnimation.playTogether(ObjectAnimator.ofFloat(pagedownButton, View.TRANSLATION_Y, 0));
                    pagedownButtonAnimation.setDuration(200);
                    pagedownButtonAnimation.start();
                } else {
                    pagedownButton.setTranslationY(0);
                    pagedownButton.setVisibility(View.VISIBLE);
                }
            }
        } else {
            if (pagedownButton.getTag() != null) {
                pagedownButton.setTag(null);
                if (pagedownButtonAnimation != null) {
                    pagedownButtonAnimation.cancel();
                    pagedownButtonAnimation = null;
                }
                if (animated) {
                    pagedownButtonAnimation = new AnimatorSet();
                    pagedownButtonAnimation.playTogether(ObjectAnimator.ofFloat(pagedownButton, View.TRANSLATION_Y, AndroidUtilities.dp(100)));

                    pagedownButtonAnimation.setDuration(200);
                    pagedownButtonAnimation.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            pagedownButton.setVisibility(View.INVISIBLE);
                        }
                    });
                    pagedownButtonAnimation.start();
                } else {
                    pagedownButton.setVisibility(View.INVISIBLE);
                }
            }
        }
    }



    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        return super.getThemeDescriptions();
    }
}
