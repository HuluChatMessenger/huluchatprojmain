package org.plus.apps.business.ui;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

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
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;
import java.util.Collections;

public class LikeProductActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private SwipeRefreshLayout refreshLayout;

    private RecyclerListView listView;
    private ListAdapter listAdapter;
    private LinearLayoutManager layoutManager;
    private ShopsEmptyCell dialogsEmptyCell;

    private ArrayList<ShopDataSerializer.Product> products = new ArrayList<>();

    private EmptyTextProgressView progressView;

    @Override
    public boolean onFragmentCreate() {

        NotificationCenter.getInstance(currentAccount).addObserver(this,NotificationCenter.didFavoriteProductLoaded);
        ShopDataController.getInstance(currentAccount).loadLikedProduct(classGuid);
        return super.onFragmentCreate();
    }



    @Override
    public View createView(Context context) {

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle("Favorite");
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
        frameLayout.addView(refreshLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.MATCH_PARENT));


        listView  = new RecyclerListView(context);
        listView.setPadding(AndroidUtilities.dp(2),0,AndroidUtilities.dp(2),0);
        listView.setLayoutManager(layoutManager = new GridLayoutManager(context,2));
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        listAdapter = new ListAdapter(context);
        listView.setAdapter(listAdapter);
        listView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.right = 4;
                outRect.left = 4;
                outRect.bottom = 4;
                outRect.right = 4;
            }
        });

        listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if(position <0 || position >= products.size()){
                    return;
                }
                ShopDataSerializer.Product product =  products.get(position);
                if(product != null){
                    Bundle bundle = new Bundle();
                    bundle.putInt("chat_id",ShopUtils.toClientChannelId(product.shop.chanel));
                    bundle.putInt("item_id",product.id);
                    ProductDetailFragment detailFragment = new ProductDetailFragment(bundle);
                    presentFragment(detailFragment);
                }
            }
        });
        refreshLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.MATCH_PARENT));


        dialogsEmptyCell = new ShopsEmptyCell(context);
        dialogsEmptyCell.setType(4);
        listView.setEmptyView(dialogsEmptyCell);

        progressView = new EmptyTextProgressView(context);
        progressView.showProgress();
        progressView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        frameLayout.addView(progressView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.MATCH_PARENT));

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        return fragmentView;
    }

    @Override
    public void onFragmentDestroy() {
        getNotificationCenter().removeObserver(this,NotificationCenter.didFavoriteProductLoaded);
        super.onFragmentDestroy();
    }

    private boolean refreshing;
    private void refresh(){
        if(refreshing){
            return;
        }
        refreshing = true;
        nextLoadProduct = null;
        productEndReached = false;
        getShopDataController().loadLikedProduct(classGuid);
    }

    private String nextLoadProduct;
    private boolean productEndReached;
    @Override
    public void didReceivedNotification(int id, int account, Object... args) {

        if(id == NotificationCenter.didFavoriteProductLoaded){
            int guid = (int)args[1];
            if(classGuid == guid){
                boolean loaded =(boolean) args[0];
                if(loaded){
                    if(refreshLayout != null && refreshLayout.isRefreshing()){
                        refreshLayout.setRefreshing(false);
                    }
                    if(refreshing){
                        products.clear();
                    }
                    ArrayList<ShopDataSerializer.Product> productArrayList = (ArrayList<ShopDataSerializer.Product>)args[2];
                    nextLoadProduct = (String) args[3];
                    if(ShopUtils.isEmpty(nextLoadProduct)){
                        productEndReached = true;
                    }else{
                        productEndReached = false;
                    }
                    int oldCount = listAdapter.getItemCount();
                    products.addAll(productArrayList);
                    if(oldCount > 0 && !refreshing){
                        listAdapter.notifyItemRangeInserted(oldCount,productArrayList.size());
                    }else{
                        listAdapter.notifyDataSetChanged();
                    }
                    if(progressView != null){
                        progressView.setVisibility(View.GONE);
                    }
                    refreshing = false;
                }else{
                    refreshing = false;
                    if(refreshLayout != null && refreshLayout.isRefreshing()){
                        refreshLayout.setRefreshing(false);
                    }

                    if(progressView != null){
                        progressView.setVisibility(View.GONE);
                    }

                    if(products.isEmpty()){
                        showDialog(ShopUtils.createConnectionAlert(getParentActivity(), this::refresh).create());
                    }
                }
            }

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
            ProductCell faveListCell = new ProductCell(mContext);
            faveListCell.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(faveListCell);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ProductCell faveListCell = (ProductCell)holder.itemView;
            ShopDataSerializer.Product product = products.get(position);
            faveListCell.setProduct(products.get(position));
            faveListCell.setDelegate(new ProductCell.ProductDelegate() {
                @Override
                public void onFavSelected(long chat_id, int product_id, boolean favorite) {
                    if(product.is_favorite == favorite){
                        return;
                    }
                    products.remove(product);
                    notifyItemRemoved(position);
                    ShopDataController.getInstance(currentAccount).checkFav(favorite, chat_id, product_id, new ShopDataController.BooleanCallBack() {
                        @Override
                        public void onResponse(boolean susscess) {
                            if(susscess){
                                faveListCell.setFavorite(!product.is_favorite);
                            }
                        }
                    });
                }

                @Override
                public void onShopClicked(long chat_id) {

                }
            });


        }

        @Override
        public int getItemCount() {
            return products.size();
        }
    }

}
