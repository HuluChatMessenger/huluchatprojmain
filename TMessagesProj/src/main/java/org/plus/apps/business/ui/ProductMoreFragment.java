package org.plus.apps.business.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.plus.apps.business.ProductCell;
import org.plus.apps.business.ShopUtils;
import org.plus.apps.business.data.ShopDataController;
import org.plus.apps.business.data.ShopDataSerializer;
import org.plus.apps.business.ui.components.ShopsEmptyCell;
import org.plus.net.APIError;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.FlickerLoadingView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;


public class ProductMoreFragment extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {


    private boolean productEndReached;
    private boolean isLoading;
    private String moreLink;

    private RecyclerListView listView;
    private ListAdapter listAdapter;
    private LinearLayoutManager layoutManager;
    private ShopsEmptyCell dialogsEmptyCell;

    private Paint backgroundPaint = new Paint();

    private ArrayList<ShopDataSerializer.FeaturedProduct> products = new ArrayList<>();

    private FlickerLoadingView progressView;

    public String link;
    public String section;
    public String title;
    private String more;

    public ProductMoreFragment(String remote,String sec,String title) {
        this.link = remote;
        this.title = title;
        this.section = sec;
    }


    @Override
    public boolean onFragmentCreate() {
        getNotificationCenter().addObserver(this,NotificationCenter.didConfigrationLoaded);
        getNotificationCenter().addObserver(this,NotificationCenter.didRemoteDataLoaded);

        getShopDataController().loadRemoteData(link,"ui_product_vertical",classGuid);

        return super.onFragmentCreate();
    }

    private SwipeRefreshLayout refreshLayout;

    @Override
    public void onFragmentDestroy() {
        getNotificationCenter().removeObserver(this,NotificationCenter.didRemoteDataLoaded);
        getNotificationCenter().removeObserver(this,NotificationCenter.didConfigrationLoaded);
        super.onFragmentDestroy();
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(title);
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

        refreshLayout = new SwipeRefreshLayout(context);


        listAdapter = new ListAdapter(context);
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
        listView.setAdapter(listAdapter);
        listView.setClipToPadding(false);
        listView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                checkLoadMoreScroll();
            }
        });

        listView.setOnItemClickListener((view, position) -> {

            if(position < 0 || position > products.size()){
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
            presentFragment(productDetailFragment);
        });



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

        refreshLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

            }
        });
        frameLayout.addView(refreshLayout,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,Gravity.LEFT|Gravity.TOP,0,0,0,0));
        int resId = R.anim.layotu_anim_from_bottom;
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(context, resId);
        listView.setLayoutAnimation(animation);



        dialogsEmptyCell = new ShopsEmptyCell(context);
        dialogsEmptyCell.setType(4);
        frameLayout.addView(dialogsEmptyCell,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,Gravity.LEFT|Gravity.TOP,0,0,0,0));

        listView.setEmptyView(dialogsEmptyCell);


        progressView = new FlickerLoadingView(context) {

            @Override
            public int getColumnsCount() {
                return 2;
            }

            @Override
            public int getViewType() {
                return FlickerLoadingView.PHOTOS_TYPE;
            }

            @Override
            protected void onDraw(Canvas canvas) {
                backgroundPaint.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), backgroundPaint);
                super.onDraw(canvas);
            }
        };
        progressView.showDate(false);
        progressView.setVisibility(View.VISIBLE);
        frameLayout.addView(progressView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        return fragmentView;
    }

    private void checkLoadMoreScroll() {
        if (listView.getAdapter() == null || ShopUtils.isEmpty(moreLink)) {
            return;
        }
        int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
        int visibleItemCount = firstVisibleItem == RecyclerView.NO_POSITION ? 0 : Math.abs(layoutManager.findLastVisibleItemPosition() - firstVisibleItem) + 1;
        int totalItemCount = listView.getAdapter().getItemCount();
        final int threshold = 3;
        if (firstVisibleItem + visibleItemCount > totalItemCount - threshold && !isLoading) {
             isLoading = true;
             getShopDataController().loadMoreFeatureProduct(moreLink, new ShopDataController.ProductSearchCallBack() {
                 @Override
                 public void run(Object response, APIError error, String next, int count) {
                     AndroidUtilities.runOnUIThread(new Runnable() {
                         @Override
                         public void run() {
                             isLoading = false;
                             if(error == null){
                                 moreLink = next;
                                 productEndReached = ShopUtils.isEmpty(moreLink);
                                 ArrayList<ShopDataSerializer.FeaturedProduct>  featuredProducts = (ArrayList<ShopDataSerializer.FeaturedProduct>)response;
                                 products.addAll(featuredProducts);
                                 if(listAdapter != null){
                                     int oldCount = listAdapter.getItemCount();
                                     listAdapter.notifyItemRangeInserted(oldCount-1,featuredProducts.size());
                                 }
                             }
                         }
                     });
                 }
             });
        }
    }



    @Override
    public void didReceivedNotification(int id, int account, Object... args) {

        if(id == NotificationCenter.didConfigrationLoaded) {
            boolean loaded = (boolean) args[0];
            String sec = (String) args[1];
            if (loaded && sec.equals(section)) {
                ArrayList<ShopDataSerializer.FieldSet> storeFields = (ArrayList<ShopDataSerializer.FieldSet>) args[2];
                getShopDataController().loadRemoteData(storeFields.get(0).remote,storeFields.get(0).type,classGuid);
            }
        }else if(id == NotificationCenter.didRemoteDataLoaded){
            if(refreshLayout != null && refreshLayout.isRefreshing()){
                refreshLayout.setRefreshing(false);
            }
            isLoading = false;
            int guid = (int)args[3];
            if(guid == classGuid){
                boolean loaded = (boolean)args[0];
                if(loaded){
                    moreLink = (String)args[4];
                    if(ShopUtils.isEmpty(moreLink)){
                        productEndReached = true;
                    }
                    ArrayList<ShopDataSerializer.FeaturedProduct>  featuredProducts = (ArrayList<ShopDataSerializer.FeaturedProduct>) args[2];
                    products.addAll(featuredProducts);
                    if(listAdapter != null){
                        int oldCount = listAdapter.getItemCount();
                        if(oldCount > 0){
                            listAdapter.notifyItemRangeInserted(oldCount-1,featuredProducts.size());
                        }else {
                            listAdapter.notifyDataSetChanged();
                        }

                    }

                    if(progressView != null){
                        progressView.setVisibility(View.GONE);
                    }
                }else{
                    APIError apiError  = (APIError)args[1];
                }

            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if(listAdapter != null){
            listAdapter.notifyDataSetChanged();
        }
    }


    private class ListAdapter extends RecyclerListView.SelectionAdapter{

        private Context mContext;


        public ListAdapter(Context context){
            mContext = context;
        }


        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return true;
        }


        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view;
            switch (viewType){
                case 1:
                    view = new ProductCell(mContext);
                    view.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    break;
                case 2:
                default:
                    FlickerLoadingView flickerLoadingView = new FlickerLoadingView(mContext){
                        @Override
                        public int getColumnsCount() {
                            return 2;
                        }
                    };
                    flickerLoadingView.setIsSingleCell(true);
                    flickerLoadingView.showDate(false);
                    flickerLoadingView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    view = flickerLoadingView;
                    break;
            }

            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if(holder.getItemViewType() == 1){
                ProductCell faveListCell = (ProductCell)holder.itemView;
                faveListCell.setFeatureProduct(products.get(position));
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
            int count = 0;
            if(products != null){
                count = products.size();
                if(isLoading && !productEndReached){
                    count = count+ 1;
                }
            }
            return count;
        }
    }


    public static class ProductSniptItemCell extends FrameLayout {


        private ShopDataSerializer.FeaturedProduct featuredProduct;

        private SimpleTextView dateTextView;
        private TextView nameTextView;
        private TextView priceTextView;
        private BackupImageView imageView;

        private FrameLayout topContainer;

        private int itemSize;
        private boolean isFav;

        public ProductSniptItemCell(Context context) {
            super(context);

            int rad = 16;

            itemSize =(int) ( getItemSize(2) / AndroidUtilities.density);
            itemSize = (int) (itemSize - itemSize*0.1);

            imageView = new BackupImageView(context);
            imageView.getImageReceiver().setRoundRadius(    rad,rad,0,0);
            addView(imageView, LayoutHelper.createFrame(itemSize,itemSize,Gravity.TOP|Gravity.LEFT));


            topContainer = new FrameLayout(context);
            int color = ShopUtils.getIntAlphaColor(Color.BLACK,0.2f);
            topContainer.setBackground(ShopUtils.createTopRoundRectDrawable(rad,color));
            addView(topContainer, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,itemSize,Gravity.TOP|Gravity.LEFT));

            dateTextView = new SimpleTextView(context);
            dateTextView.setTextSize(12);
            dateTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));

            dateTextView.setTextColor(Theme.getColor(Theme.key_avatar_text));
            addView(dateTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT|Gravity.TOP, 8,8 , 8, 8));


            nameTextView = new TextView(context);
            nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            nameTextView.setMaxLines(1);
            nameTextView.setSingleLine();
            nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            nameTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
            nameTextView.setGravity(Gravity.LEFT);
            nameTextView.setPadding(AndroidUtilities.dp(4),AndroidUtilities.dp(4),AndroidUtilities.dp(4),AndroidUtilities.dp(4));
            nameTextView.setEllipsize(TextUtils.TruncateAt.END);
            addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT|Gravity.TOP, 0,itemSize + 4  , 48, 0));

            priceTextView = new TextView(context);
            priceTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            priceTextView.setLines(1);
            priceTextView.setMaxLines(1);
            dateTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            priceTextView.setSingleLine(true);
            priceTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
            priceTextView.setGravity(Gravity.LEFT);
            priceTextView.setPadding(AndroidUtilities.dp(4),AndroidUtilities.dp(8),AndroidUtilities.dp(8),AndroidUtilities.dp(8));
            addView(priceTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT|Gravity.TOP, 0, itemSize + 16 + 16 + 4, 0, 0));
            //setBackground(ShopUtils.createRoundStrokeDrwable(rad,3,Theme.key_windowBackgroundGray,Theme.key_windowBackgroundWhite));

        }



        public void setProduct(ShopDataSerializer.FeaturedProduct product) {
            if(product == null|| ShopUtils.isEmpty(product.title)){
                return;
            }
            featuredProduct = product;
            if(featuredProduct.pictureSnip != null && (featuredProduct.pictureSnip .photo != null)){
                imageView.setImage(featuredProduct.pictureSnip.photo,"100_100",Theme.chat_attachEmptyDrawable);
            }else{
                imageView.setImage(null,null,Theme.chat_attachEmptyDrawable);
            }
            String title = product.title.substring(0, 1).toUpperCase() + featuredProduct.title.substring(1).toLowerCase();
            nameTextView.setText(title);
            priceTextView.setText(ShopUtils.formatCurrency(featuredProduct.price));

        }

        public  int getItemSize(int itemsCount) {
            final int itemWidth;
            if (AndroidUtilities.isTablet()) {
                itemWidth = (AndroidUtilities.dp(490) - (itemsCount - 1) * AndroidUtilities.dp(2)) / itemsCount;
            } else {
                itemWidth = (AndroidUtilities.displaySize.x - (itemsCount - 1) * AndroidUtilities.dp(2)) / itemsCount;
            }
            return itemWidth;
        }

    }

}
