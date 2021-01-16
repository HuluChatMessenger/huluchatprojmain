package org.plus.apps.business.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.plus.apps.business.ProductCell;
import org.plus.apps.business.ShopUtils;
import org.plus.apps.business.data.ShopDataController;
import org.plus.apps.business.data.ShopDataSerializer;
import org.plus.apps.business.ui.components.ShopsEmptyCell;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.FlickerLoadingView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;

public class CollectionFragment extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private FlickerLoadingView progressView;
    private SwipeRefreshLayout refreshLayout;

    private ArrayList<ProductCell> cellCache = new ArrayList<>(10);
    private ArrayList<ProductCell> cache = new ArrayList<>(10);

    private ListAdapter listAdapter;
    private RecyclerListView listView;
    private GridLayoutManager layoutManager;

    private ArrayList<ShopDataSerializer.Product> products = new ArrayList<>();
    private ShopDataSerializer.Collection collection;

    private int rowCount;
    private int collectionRow;
    private int productStartRow;
    private int productEndRow;
    private int loadingRow;
    private int emptyLayoutRow;

    private boolean isLoadingProduct;
    private String nextLoadProduct;
    private boolean productEndReached;

    public CollectionFragment(ShopDataSerializer.Collection col){
        collection = col;
    }

    private void updateRow(){
        rowCount = 0;

        int count = products.size();

        if(count > 0){
            collectionRow = rowCount++;
            productStartRow = rowCount;
            rowCount += count;
            productEndRow = rowCount;
        }else{
            collectionRow = -1;
            productStartRow = -1;
            productEndRow = -1;
        }

        if(showEmpty && productStartRow == -1){
            emptyLayoutRow = rowCount++;
        }else{
            emptyLayoutRow = -1;

        }



        if(isLoadingProduct || emptyLayoutRow != -1 || productStartRow !=-1){
            loadingRow = -1;
        }else{
            loadingRow = rowCount++;
        }

//        if(emptyLayoutRow != -1 || productStartRow !=-1){
//            loadingRow = -1;
//        }else{
//            loadingRow = rowCount++;
//        }

        if(showError && productStartRow == -1){
            errorLayoutRow = rowCount++;
        }else{
            errorLayoutRow = -1;
        }

        if(listAdapter != null){
            listAdapter.notifyDataSetChanged();
            listView.scheduleLayoutAnimation();
        }

    }

    @Override
    public boolean onFragmentCreate() {
        getNotificationCenter().addObserver(this,NotificationCenter.collectionLoaded);
        if(collection != null){
            ShopDataController.getInstance(currentAccount).loadProductCollection(collection.id,classGuid);
        }else{
            return false;
        }
        updateRow();
        return super.onFragmentCreate();
    }


    @Override
    public void onFragmentDestroy() {
        getNotificationCenter().removeObserver(this,NotificationCenter.collectionLoaded);
        super.onFragmentDestroy();
    }
    private Paint backgroundPaint = new Paint();

    private boolean fromRefresh;
    private void refresh(){
        if(isLoadingProduct){
            refreshLayout.setRefreshing(false);
            return;
        }
        fromRefresh = true;
        nextLoadProduct = null;
        productEndReached = false;
        isLoadingProduct = true;
        ShopDataController.getInstance(currentAccount).loadProductCollection(collection.id,classGuid);
    }


    @Override
    public View createView(Context context) {

        actionBar.setTitle(collection.title);
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setCastShadows(false);

        for (int a = 0; a < 10; a++) {
            cellCache.add(new ProductCell(context));
        }

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick(){
            @Override
            public void onItemClick(int id) {
                if(id == -1){
                    finishFragment();
                }
            }
        });

        fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = (FrameLayout) fragmentView;
        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

        refreshLayout = new SwipeRefreshLayout(context);
        frameLayout.addView(refreshLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));


        listView = new RecyclerListView(context);
        listView.setItemAnimator(null);
        listView.setClipToPadding(false);
        listView.setPadding(AndroidUtilities.dp(8),0,AndroidUtilities.dp(8),0);
        listView.setLayoutManager(layoutManager = new GridLayoutManager(context, 2));
        listView.setAdapter(listAdapter = new ListAdapter(context));
        listView.setGlowColor(Theme.getColor(Theme.key_wallet_blackBackground));
        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                checkLoadMoreScroll();
            }
        });
        refreshLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));


        listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                ShopDataSerializer.Product product = listAdapter.getItem(position);
                if(product == null || product.shop == null){
                    return;
                }
                Bundle bundle = new Bundle();
                bundle.putInt("chat_id",ShopUtils.toClientChannelId(product.shop.chanel));
                bundle.putInt("item_id",product.id);
                ProductDetailFragment productDetailFragment = new ProductDetailFragment(bundle);
                presentFragment(productDetailFragment);
            }
        });

        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                ListAdapter listAdapter = (ListAdapter) listView.getAdapter();
                if(listAdapter != null){
                    int type =  listAdapter.getItemViewType(position);
                    if(type == 1){
                        return 1;
                    }else{
                        return 2;
                    }
                }
                return layoutManager.getSpanCount();
            }
        });

        listView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.onDraw(c, parent, state);
            }

            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.left = 4;
                outRect.right = 4;
                outRect.bottom = 4;
                outRect.top = 4;
            }
        });

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

        int resId = R.anim.layotu_anim_from_bottom;
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(context, resId);
        listView.setLayoutAnimation(animation);
        listView.setLayoutAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if(layoutManager != null){
                    View filterView = layoutManager.findViewByPosition(collectionRow);
                    if(filterView != null){
                        filterView.clearAnimation();
                    }
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


        refreshLayout.setOnRefreshListener(this::refresh);

        return fragmentView;
    }
    private boolean showEmpty;

    private void checkLoadMoreScroll() {
        if (listView.getAdapter() == null) {
            return;
        }
        int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
        int visibleItemCount = firstVisibleItem == RecyclerView.NO_POSITION ? 0 : Math.abs(layoutManager.findLastVisibleItemPosition() - firstVisibleItem) + 1;
        int totalItemCount = listView.getAdapter().getItemCount();
        final int threshold = 3;
        if (firstVisibleItem + visibleItemCount > totalItemCount - threshold && !isLoadingProduct) {
            if(!productEndReached){
                isLoadingProduct = true;
                ShopDataController.getInstance(UserConfig.selectedAccount).loadMoreProductForShop(nextLoadProduct,getClassGuid());
            }
        }
    }

    private boolean showError;
    private int errorLayoutRow;


    @Override
    public void didReceivedNotification(int id, int account, Object... args) {

        if(id == NotificationCenter.collectionLoaded){
            boolean loaded = (boolean)args[0];
            int guid = (int)args[2];
            if(classGuid == guid){
                isLoadingProduct = false;
                showError = false;
                if(refreshLayout != null && refreshLayout.isRefreshing()){
                    refreshLayout.setRefreshing(false);
                }
                if(loaded){
                    ArrayList<ShopDataSerializer.Product> productArrayList = (ArrayList<ShopDataSerializer.Product>)args[1];
                    nextLoadProduct = (String) args[3];
                    if(ShopUtils.isEmpty(nextLoadProduct)){
                        productEndReached = true;
                    }else{
                        productEndReached = false;
                    }
                    if(fromRefresh){
                        products.clear();
                        fromRefresh = false;
                    }
                    products.addAll(productArrayList);
                    if(products.isEmpty() && productEndReached){
                        showEmpty = true;
                    }else{
                        showEmpty = false;
                    }
                    products.addAll(productArrayList);

                    if(listView != null){
                        listView.stopScroll();
                    }
                }else{
                    showError = products.isEmpty();
                }
                updateRow();
                if(progressView.getVisibility() == View.VISIBLE){
                    progressView.setVisibility(View.GONE);
                }

        }
    }else  if(id == NotificationCenter.didProductLoaded){
            boolean loaded = (boolean)args[0];
            int guid = (int)args[2];
            if(classGuid == guid){
                if(refreshLayout != null && refreshLayout.isRefreshing()){
                    refreshLayout.setRefreshing(false);
                }
                showError = false;
                isLoadingProduct = false;
                if(loaded){
                    ArrayList<ShopDataSerializer.Product> productArrayList = (ArrayList<ShopDataSerializer.Product>)args[1];
                    nextLoadProduct = (String) args[3];
                    if(ShopUtils.isEmpty(nextLoadProduct)){
                        productEndReached = true;
                    }else{
                        productEndReached = false;
                    }
                    if(fromRefresh){
                        products.clear();
                        fromRefresh = false;
                    }
                    products.addAll(productArrayList);
                    if(products.isEmpty() && productEndReached){
                        showEmpty = true;
                    }else{
                        showEmpty = false;
                    }
                    products.addAll(productArrayList);

                    if(listView != null){
                        listView.stopScroll();
                    }
                }else{
                    showError = products.isEmpty();
                }
                updateRow();
                if(progressView.getVisibility() == View.VISIBLE){
                    progressView.setVisibility(View.GONE);
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
            int type = holder.getItemViewType();
            return type == 1 || type == 3;
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
                        view = new ProductCell(mContext,true);
                    }
                    ProductCell productCell = (ProductCell)view;
                    cache.add(productCell);
                    break;
                case 3:
                    StoreActivity.BannerContainer bannerContainer = new StoreActivity.BannerContainer(mContext);
                    ArrayList<ShopDataSerializer.Collection> collections = new ArrayList<>();
                    collections.add(collection);
                    bannerContainer.setCollections(new ArrayList<>(collections));
                    view = bannerContainer;
                    break;
                case 2:
                    ShopsEmptyCell dialogsEmptyCell = new ShopsEmptyCell(mContext);
                    dialogsEmptyCell.setType(4);
                    view = dialogsEmptyCell;
                    break;
                case 6:
                    EmptyTextProgressView emptyTextProgressView = new EmptyTextProgressView(mContext);
                    emptyTextProgressView.setTopImage(R.drawable.ic_connection);
                    emptyTextProgressView.setText("Connection issues!");
                    emptyTextProgressView.showTextView();
                    view = emptyTextProgressView;
                    break;
                case 5:
                default:
                    FlickerLoadingView flickerLoadingView = new FlickerLoadingView(mContext){
                        @Override
                        public int getColumnsCount() {
                            return 2;
                        }
                    };
                    flickerLoadingView.setIsSingleCell(true);
                    flickerLoadingView.showDate(false);
                    flickerLoadingView.setViewType(FlickerLoadingView.PHOTOS_TYPE);
                    view = flickerLoadingView;
                    break;
            }
            return new RecyclerListView.Holder(view);
        }

        public ShopDataSerializer.Product getItem(int position) {
            int newPos = position - productStartRow;
            if(newPos < 0 || newPos >= products.size()){
                return null;
            }
            return products.get(newPos);
        }


        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if(holder.getItemViewType() == 1){
                ProductCell cell = (ProductCell) holder.itemView;
                ShopDataSerializer.Product messageObject = getItem(position);
                cell.setProduct(messageObject);
                cell.setDelegate(new ProductCell.ProductDelegate() {

                    @Override
                    public void onFavSelected(long chat_id, int product_id, boolean favorite) {
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                ShopDataController.getInstance(currentAccount).checkFav(favorite, chat_id, product_id, new ShopDataController.BooleanCallBack() {
                                    @Override
                                    public void onResponse(boolean susscess) {
                                        if(susscess){
                                            cell.setFavorite(!messageObject.is_favorite);
                                        }
                                    }
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
//                cell.setDelegate(new ProductCell.ProductDelegate() {
//                    @Override
//                    public void onFavSelected(ShopDataSerializer.Product product) {
////                        Log.i("berhanza","onFavSelected" + product.is_favorite);
////
////                        if(product.is_favorite == cell.isFavorite()){
////                            Log.i("berhanza","onFavSelected" + "no change");
////                            return;
////                        }
////                        Log.i("berhanza","onFavSelected" + product.is_favorite);
////                        ShopDataController.getInstance(currentAccount).addProductToFav(product,cell.isFavorite());
//                    }
//                });

            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position >= productStartRow && position < productEndRow) {
                return 1;
            }else if(position == emptyLayoutRow){
                return 2;
            }else if(position == loadingRow){
                return 5;
            }else if(position == errorLayoutRow){
                return 6;
            } else if (position == collectionRow) {
                return 3;
            }
            return super.getItemViewType(position);
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }
    }


//    private class ListAdapter extends RecyclerListView.SelectionAdapter {
//
//        private Context context;
//
//        public ListAdapter(Context context) {
//            this.context = context;
//        }
//
//        @Override
//        public boolean isEnabled(RecyclerView.ViewHolder holder) {
//            return false;
//        }
//
//        @NonNull
//        @Override
//        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//
//            View view;
//            switch (viewType) {
//                case 1:
//                    if (!cellCache.isEmpty()) {
//                        view = cellCache.get(0);
//                        cellCache.remove(0);
//                        ViewGroup p = (ViewGroup) view.getParent();
//                        if (p != null) {
//                            p.removeView(view);
//                        }
//                    } else {
//                        view = new ProductCell(context);
//                    }
//                    cache.add((ProductCell) view);
//                    break;
//
//                case 2:
//                    StoreActivity.BannerContainer bannerContainer = new StoreActivity.BannerContainer(context);
//                    ArrayList<ShopDataSerializer.Collection> collections = new ArrayList<>();
//                    collections.add(collection);
//                    bannerContainer.setCollections(new ArrayList<>(collections));
//                    view = bannerContainer;
//                    break;
//                case 3:
//                default:
//                    LoadingView loadingView = new LoadingView(context) {
//                        @Override
//                        public int getColumnsCount() {
//                            return 2;
//                        }
//                    };
//                    loadingView.setIsSingleCell(false);
//                    loadingView.setViewType(LoadingView.PHOTOS_TYPE);
//                    view = loadingView;
//                    break;
//            }
//
//            return new RecyclerListView.Holder(view);
//        }
//
//        public ShopDataSerializer.Product getItem(int position) {
//            int newPos = position - productStartRow;
//            if(newPos < 0 || newPos >= products.size()){
//                return null;
//            }
//            return products.get(position - productStartRow);
//        }
//
//
//        @Override
//        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//
//            if(holder.getItemViewType() == 1){
//                ProductCell cell = (ProductCell) holder.itemView;
//                ShopDataSerializer.Product messageObject = getItem(position);
//                cell.setProduct(messageObject);
//            }
//        }
//
//
//        @Override
//        public int getItemViewType(int position) {
//            if (position == collectionRow) {
//                return 2;
//            } else if (position >= productStartRow && position < productEndRow) {
//                return 1;
//            } else if (position == loadingRow) {
//                return 3;
//            }
//            return super.getItemViewType(position);
//        }
//
//        @Override
//        public int getItemCount() {
//            return rowCount;
//        }
//    }


}

