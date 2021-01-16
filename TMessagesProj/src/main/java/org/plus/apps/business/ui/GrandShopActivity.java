//package org.plus.apps.business.ui;
//
//import android.content.Context;
//import android.graphics.Canvas;
//import android.graphics.PorterDuff;
//import android.graphics.PorterDuffColorFilter;
//import android.graphics.Rect;
//import android.os.Bundle;
//import android.text.Editable;
//import android.text.TextUtils;
//import android.text.TextWatcher;
//import android.util.TypedValue;
//import android.view.Gravity;
//import android.view.KeyEvent;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.animation.AnimationUtils;
//import android.view.animation.GridLayoutAnimationController;
//import android.view.animation.LayoutAnimationController;
//import android.view.inputmethod.EditorInfo;
//import android.widget.FrameLayout;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.GridLayoutManager;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import org.plus.apps.business.ShopUtils;
//import org.plus.apps.business.data.ShopDataController;
//import org.plus.apps.business.data.ShopDataSerializer;
//import org.plus.apps.business.ui.cells.BusinessCell;
//import org.plus.apps.business.ui.cells.ProductCell;
//import org.plus.apps.business.data.ShopDataModels;
//import org.plus.net.APIError;
//import org.telegram.messenger.AndroidUtilities;
//import org.telegram.messenger.NotificationCenter;
//import org.telegram.messenger.R;
//import org.telegram.ui.ActionBar.ActionBar;
//import org.telegram.ui.ActionBar.ActionBarMenu;
//import org.telegram.ui.ActionBar.ActionBarMenuItem;
//import org.telegram.ui.ActionBar.BaseFragment;
//import org.telegram.ui.ActionBar.Theme;
//import org.telegram.ui.Cells.HintDialogCell;
//import org.telegram.ui.Cells.LoadingCell;
//import org.telegram.ui.Components.CloseProgressDrawable2;
//import org.telegram.ui.Components.EditTextBoldCursor;
//import org.telegram.ui.Components.EmptyTextProgressView;
//import org.telegram.ui.Components.LayoutHelper;
//import org.telegram.ui.Components.RecyclerListView;
//
//import java.util.ArrayList;
//
//public class GrandShopActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate{
//
//    private RecyclerListView listView;
//    private RecyclerListView innerListView;
//    private RecyclerListView gridInnerListView;
//    private ListAdapter adapter;
//    private GridLayoutManager gridLayoutManager;
//
//    private ArrayList<ShopDataSerializer.FeaturedProduct> featuredProducts = new ArrayList<>();
//    private ArrayList<ShopDataSerializer.ProductType> businesses =new ArrayList<>();
//    private ArrayList<ShopDataSerializer.ShopSnip> featuredShops = new ArrayList<>();
//
//
//
//    private int rowCount;
//
//
//    private int searchRow;
//    private int featuredShopHeaderRow;
//    private int featuredShopRow;
//    private int categoriesHeaderRow;
//    private int categoriesRow;
//    private int featuredProductHeaderRow;
//    private int featuredProductStartRow;
//    private int featuredProductEndRow;
//    private int loadingRow;
//
//
//    private boolean showLoadMore;
//
//    private void updateRow(){
//      rowCount = 0;
//      int previousProductStartRow = featuredProductStartRow;
//      int previousFeaturedShopRow = featuredShopHeaderRow;
//      int previousCategoryRow = categoriesHeaderRow;
//
////      if(!featuredProducts.isEmpty() || !businesses.isEmpty() || !featuredShops.isEmpty()){
////          searchRow = rowCount++;
////      }else{
////          searchRow = -1;
////      }
//
//      if(!featuredShops.isEmpty()){
//          featuredShopHeaderRow = rowCount++;
//          featuredShopRow = rowCount++;
//          if(previousFeaturedShopRow == -1){
//              if(adapter != null){
//                  adapter.notifyItemRangeInserted(featuredShopHeaderRow,2);
//              }
//          }
//      }else{
//          featuredShopHeaderRow = -1;
//          featuredShopRow = -1;
//      }
//
//      if(!businesses.isEmpty()){
//          categoriesHeaderRow = rowCount++;
//          categoriesRow = rowCount++;
//
//          if(previousCategoryRow == -1){
//              if(adapter != null){
//                  adapter.notifyItemRangeInserted(categoriesHeaderRow,2);
//              }
//          }
//      }else{
//          categoriesHeaderRow = -1;
//          categoriesRow = -1;
//      }
//
//      if(!featuredProducts.isEmpty()){
//          featuredProductHeaderRow = rowCount++;
//          featuredProductStartRow = rowCount;
//          rowCount += featuredProducts.size();
//          featuredProductEndRow = rowCount;
//
//          if(previousProductStartRow == -1){
//              if(adapter != null){
//                  adapter.notifyItemRangeInserted(featuredProductStartRow,featuredProducts.size());
//              }
//          }
//
//      }else{
//          featuredProductHeaderRow = -1;
//          featuredProductStartRow =-1;
//          featuredProductEndRow = -1;
//      }
//
////      if(showLoadMore){
////          loadingRow = rowCount++;
////      }else{
////          loadingRow = -1;
////      }
//
////      if(adapter != null){
////          adapter.notifyDataSetChanged();
////      }
//
//    }
//
//    private ActionBarMenuItem searchItem;
//    private ActionBarMenuItem likeItem;
//    private ActionBarMenuItem scanItem;
//
//    private EmptyTextProgressView emptyTextProgressView;
//
//
//    @Override
//    public boolean onFragmentCreate() {
//        NotificationCenter.getInstance(currentAccount).addObserver(this,NotificationCenter.didFeaturedProductLoaded);
//        NotificationCenter.getInstance(currentAccount).addObserver(this,NotificationCenter.didFeaturedShopLoaded);
//        NotificationCenter.getInstance(currentAccount).addObserver(this,NotificationCenter.didBusinessLoaded);
//
//        ShopDataController.getInstance(currentAccount).loadFeaturedProduct(classGuid);
//        ShopDataController.getInstance(currentAccount).loadFeaturedShop(classGuid);
//        ShopDataController.getInstance(currentAccount).loadSectionConfiguration(true);
//
//        return super.onFragmentCreate();
//    }
//
//
//
//    @Override
//    public void onFragmentDestroy() {
//        NotificationCenter.getInstance(currentAccount).removeObserver(this,NotificationCenter.didFeaturedProductLoaded);
//        NotificationCenter.getInstance(currentAccount).removeObserver(this,NotificationCenter.didFeaturedShopLoaded);
//        NotificationCenter.getInstance(currentAccount).removeObserver(this,NotificationCenter.didBusinessLoaded);
//        super.onFragmentDestroy();
//    }
//
//
//    @Override
//    protected ActionBar createActionBar(Context context) {
//        ActionBar actionBar = new ActionBar(context);
//        actionBar.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefault));
//        actionBar.setItemsBackgroundColor(Theme.getColor(Theme.key_actionBarDefaultSelector), false);
//        actionBar.setItemsBackgroundColor(Theme.getColor(Theme.key_actionBarActionModeDefaultSelector), true);
//        actionBar.setItemsColor(Theme.getColor(Theme.key_actionBarDefaultIcon), false);
//        actionBar.setItemsColor(Theme.getColor(Theme.key_actionBarActionModeDefaultIcon), true);
//
//        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
//        actionBar.setAllowOverlayTitle(false);
//        actionBar.setCastShadows(true);
//        actionBar.setTitle("Shop");
//
//
//        return actionBar;
//    }
//
//    @Override
//    public View createView(Context context) {
//
//        ActionBarMenu menu = actionBar.createMenu();
//
//        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
//            @Override
//            public void onItemClick(int id) {
//                if (id == -1) {
//                    finishFragment();
//                }else if(id == 1){
//                    LikeProductActivity likeProductActivity = new LikeProductActivity();
//                    presentFragment(likeProductActivity);
//                }
//            }
//        });
//
//        likeItem =  menu.addItem(1, R.drawable.msg_fave);
//        searchItem =  menu.addItem(2, R.drawable.ic_ab_search);
//        ActionBarMenuItem otherItem = menu.addItem(3,R.drawable.ic_ab_other);
//        otherItem.addSubItem(4,R.drawable.wallet_qr,"Scan");
//
//
//        fragmentView = new FrameLayout(context);
//        FrameLayout frameLayout = (FrameLayout) fragmentView;
//        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
//
//        listView = new RecyclerListView(context) {
//
//            @Override
//            public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
//                return false;
//            }
//
//            @Override
//            protected void attachLayoutAnimationParameters(View child, ViewGroup.LayoutParams params, int index, int count) {
//                final LayoutManager layoutManager = getLayoutManager();
//                if (getAdapter() != null && layoutManager instanceof GridLayoutManager){
//
//                    GridLayoutAnimationController.AnimationParameters animationParams =
//                            (GridLayoutAnimationController.AnimationParameters) params.layoutAnimationParameters;
//
//                    if (animationParams == null) {
//                        // If there are no animation parameters, create new once and attach them to
//                        // the LayoutParams.
//                        animationParams = new GridLayoutAnimationController.AnimationParameters();
//                        params.layoutAnimationParameters = animationParams;
//                    }
//
//                    // Next we are updating the parameters
//
//                    // Set the number of items in the RecyclerView and the index of this item
//                    animationParams.count = count;
//                    animationParams.index = index;
//
//                    // Calculate the number of columns and rows in the grid
//                    final int columns = ((GridLayoutManager) layoutManager).getSpanCount();
//                    animationParams.columnsCount = columns;
//                    animationParams.rowsCount = count / columns;
//
//                    // Calculate the column/row position in the grid
//                    final int invertedIndex = count - 1 - index;
//                    animationParams.column = columns - 1 - (invertedIndex % columns);
//                    animationParams.row = animationParams.rowsCount - 1 - invertedIndex / columns;
//
//                } else {
//                    // Proceed as normal if using another type of LayoutManager
//                    super.attachLayoutAnimationParameters(child, params, index, count);
//                }
//            }
//
//        };
//        gridLayoutManager = new GridLayoutManager(context,2){
//            @Override
//            public boolean supportsPredictiveItemAnimations() {
//                return true;
//            }
//        };
//        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
//            @Override
//            public int getSpanSize(int position) {
//                ListAdapter listAdapter = (ListAdapter) listView.getAdapter();
//                if(listAdapter != null){
//                   int type =  listAdapter.getItemViewType(position);
//                   if(type == 6){
//                       return 1;
//                   }
//                }
//                return gridLayoutManager.getSpanCount();
//            }
//        });
//
//        listView.setLayoutManager(gridLayoutManager);
//        listView.setVerticalScrollBarEnabled(false);
//        listView.setClipToPadding(false);
//        listView.setPadding(AndroidUtilities.dp(8),0,AndroidUtilities.dp(8),0);
//        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
//        listView.setAdapter(adapter = new ListAdapter(context));
//
//
//        int resId = R.anim.grid_layout_animation_from_bottom;
//        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(context, resId);
//        listView.setLayoutAnimation(animation);
//
//        listView.addItemDecoration(new RecyclerView.ItemDecoration() {
//            @Override
//            public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
//                super.onDraw(c, parent, state);
//            }
//
//            @Override
//            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
//                   outRect.left = 8;
//                   outRect.right = 8;
//                   outRect.bottom = 8;
//                   outRect.top = 8;
//            }
//        });
//
//        adapter.setFeatureShopDelegate(new FeatureShopDelegate() {
//            @Override
//            public void didPressFeatureShop(ShopDataSerializer.ShopSnip shop) {
//                if(shop != null){
//                    Bundle bundle = new Bundle();
//                    bundle.putInt("chat_id", ShopUtils.toClientChannelId(shop.channel));
//                    BusinessProfileActivity businessProfileActivity = new BusinessProfileActivity(bundle);
//                    presentFragment(businessProfileActivity);
//                }
//            }
//        });
//
//        adapter.setBusinessDelegate(new BusinessDelegate() {
//            @Override
//            public void didBusSelected(ShopDataSerializer.ProductType business) {
//
//                if(business != null){
//                    Bundle bundle = new Bundle();
//                    bundle.putString("title",business.display_name);
//                    bundle.putString("bus_type",business.key);
//                    ProductBusinessFragment productBusinessFragment = new ProductBusinessFragment(bundle);
//                    presentFragment(productBusinessFragment);
//                }
//            }
//        });
//
//        emptyTextProgressView = new EmptyTextProgressView(context);
//        emptyTextProgressView.showProgress();
//        emptyTextProgressView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
//        frameLayout.addView(emptyTextProgressView,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.MATCH_PARENT));
//
//
//        return fragmentView;
//    }
//
//    private class ListAdapter extends RecyclerListView.SelectionAdapter{
//
//
//        private FeatureShopDelegate featureShopDelegate;
//        private BusinessDelegate businessDelegate;
//
//        public void setBusinessDelegate(BusinessDelegate businessDelegate) {
//            this.businessDelegate = businessDelegate;
//        }
//
//        public void setFeatureShopDelegate(FeatureShopDelegate featureShopDelegate) {
//            this.featureShopDelegate = featureShopDelegate;
//        }
//
//
//        private Context context;
//
//        public ListAdapter(Context context) {
//
//            this.context = context;
//        }
//
//        @Override
//        public boolean isEnabled(RecyclerView.ViewHolder holder) {
//            return true;
//        }
//
//        @NonNull
//        @Override
//        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//
//
//            View view = null;
//            switch (viewType){
//                case 1:
//                    view = new TitleCell(context);
//                    break;
//                case 2:
//                    view = new SearchField(context);
//                    break;
//                case 3:
//                    RecyclerListView horizontalListView = new RecyclerListView(context) {
//                        @Override
//                        public boolean onInterceptTouchEvent(MotionEvent e) {
//                            if (getParent() != null && getParent().getParent() != null) {
//                                getParent().getParent().requestDisallowInterceptTouchEvent(canScrollHorizontally(-1));
//                            }
//                            return super.onInterceptTouchEvent(e);
//                        }
//
//                        @Override
//                        protected void attachLayoutAnimationParameters(View child, ViewGroup.LayoutParams params, int index, int count) {
//                            final LayoutManager layoutManager = getLayoutManager();
//                            if (getAdapter() != null && layoutManager instanceof GridLayoutManager){
//
//                                GridLayoutAnimationController.AnimationParameters animationParams =
//                                        (GridLayoutAnimationController.AnimationParameters) params.layoutAnimationParameters;
//
//                                if (animationParams == null) {
//                                    // If there are no animation parameters, create new once and attach them to
//                                    // the LayoutParams.
//                                    animationParams = new GridLayoutAnimationController.AnimationParameters();
//                                    params.layoutAnimationParameters = animationParams;
//                                }
//
//                                // Next we are updating the parameters
//
//                                // Set the number of items in the RecyclerView and the index of this item
//                                animationParams.count = count;
//                                animationParams.index = index;
//
//                                // Calculate the number of columns and rows in the grid
//                                final int columns = ((GridLayoutManager) layoutManager).getSpanCount();
//                                animationParams.columnsCount = columns;
//                                animationParams.rowsCount = count / columns;
//
//                                // Calculate the column/row position in the grid
//                                final int invertedIndex = count - 1 - index;
//                                animationParams.column = columns - 1 - (invertedIndex % columns);
//                                animationParams.row = animationParams.rowsCount - 1 - invertedIndex / columns;
//
//                            } else {
//                                // Proceed as normal if using another type of LayoutManager
//                                super.attachLayoutAnimationParameters(child, params, index, count);
//                            }
//                        }
//
//                    };
//                    horizontalListView.setTag(9);
//                    int resId = R.anim.layotu_anim_from_bottom;
//                    LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(context, resId);
//                    horizontalListView.setLayoutAnimation(animation);
//                    LinearLayoutManager layoutManager = new LinearLayoutManager(context) {
//                        @Override
//                        public boolean supportsPredictiveItemAnimations() {
//                            return true;
//                        }
//                    };
//                    layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
//                    horizontalListView.setLayoutManager(layoutManager);
//                    //horizontalListView.setDisallowInterceptTouchEvents(true);
//                    horizontalListView.setAdapter(new CategoryAdapterRecycler());
//                    horizontalListView.setOnItemClickListener((view1, position) -> {
//                        if (featureShopDelegate != null) {
//                            featureShopDelegate.didPressFeatureShop(featuredShops.get(position));
//                        }
//                    });
//                    view = horizontalListView;
//                    innerListView = horizontalListView;
//                    break;
//                case 4:
//                    RecyclerListView gridListView = new RecyclerListView(context) {
//                        @Override
//                        public boolean onInterceptTouchEvent(MotionEvent e) {
//                            if (getParent() != null && getParent().getParent() != null) {
//                                getParent().getParent().requestDisallowInterceptTouchEvent(canScrollHorizontally(-1));
//                            }
//                            return super.onInterceptTouchEvent(e);
//                        }
//
//                        @Override
//                        protected void attachLayoutAnimationParameters(View child, ViewGroup.LayoutParams params, int index, int count) {
//                            final LayoutManager layoutManager = getLayoutManager();
//                            if (getAdapter() != null && layoutManager instanceof GridLayoutManager){
//
//                                GridLayoutAnimationController.AnimationParameters animationParams =
//                                        (GridLayoutAnimationController.AnimationParameters) params.layoutAnimationParameters;
//
//                                if (animationParams == null) {
//                                    // If there are no animation parameters, create new once and attach them to
//                                    // the LayoutParams.
//                                    animationParams = new GridLayoutAnimationController.AnimationParameters();
//                                    params.layoutAnimationParameters = animationParams;
//                                }
//
//                                // Next we are updating the parameters
//
//                                // Set the number of items in the RecyclerView and the index of this item
//                                animationParams.count = count;
//                                animationParams.index = index;
//
//                                // Calculate the number of columns and rows in the grid
//                                final int columns = ((GridLayoutManager) layoutManager).getSpanCount();
//                                animationParams.columnsCount = columns;
//                                animationParams.rowsCount = count / columns;
//
//                                // Calculate the column/row position in the grid
//                                final int invertedIndex = count - 1 - index;
//                                animationParams.column = columns - 1 - (invertedIndex % columns);
//                                animationParams.row = animationParams.rowsCount - 1 - invertedIndex / columns;
//
//                            } else {
//                                // Proceed as normal if using another type of LayoutManager
//                                super.attachLayoutAnimationParameters(child, params, index, count);
//                            }
//                        }
//
//                    };
//                    gridListView.setTag(9);
//                   // gridListView.setItemAnimator(null);
//                   //// gridListView.setLayoutAnimation(null);
//                    LayoutAnimationController anim = AnimationUtils.loadLayoutAnimation(context, R.anim.grid_layout_animation_from_bottom);
//                    gridListView.setLayoutAnimation(anim);
//                    GridLayoutManager gridLayoutManager = new GridLayoutManager(context,4) {
//                        @Override
//                        public boolean supportsPredictiveItemAnimations() {
//                            return true;
//                        }
//                    };
//                    gridListView.setLayoutManager(gridLayoutManager);
//                    //horizontalListView.setDisallowInterceptTouchEvents(true);
//                    gridListView.setAdapter(new ProductCategoryListAdapter());
//                    gridListView.setOnItemClickListener((view1, position) -> {
//                        if (businessDelegate != null) {
//                            businessDelegate.didBusSelected(businesses.get(position));
//                        }
//                    });
//                    view = gridListView;
//                    gridInnerListView = gridListView;
//                    break;
//                case 6:
//                    ProductCell ProductCell = new ProductCell(context);
////                    ProductCell.setDelegate(new ProductCell.ProductCellDelegate() {
////                        @Override
////                        public void onProductSelect(ShopDataSerializer.FeaturedProduct product, ProductCell ProductCell) {
////                            Bundle bundle = new Bundle();
////                            bundle.putInt("chat_id",ShopUtils.toClientChannelId(product.shopSnip.channel));
////                            bundle.putInt("item_id",product.id);
////                            ProductDetailFragment productDetailFragment = new ProductDetailFragment(bundle);
////                            presentFragment(productDetailFragment);
////
////                        }
////
////                        @Override
////                        public void onShopPressed(ShopDataSerializer.ShopSnip snip) {
////                            Bundle bundle = new Bundle();
////                            bundle.putInt("chat_id",ShopUtils.toClientChannelId(snip.channel));
////                            BusinessProfileActivity businessProfileActivity = new BusinessProfileActivity(bundle);
////                            presentFragment(businessProfileActivity);
////
////                        }
////                    });
//                    view = ProductCell;
//                    break;
//                case 5:
//                default:
//                    view = new LoadingCell(context);
//                    break;
//
//            }
//
//            view.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//            return new RecyclerListView.Holder(view);
//        }
//
//
//
//        @Override
//        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//
//            if(holder.getItemViewType() == 1){
//                TitleCell titleCell = (TitleCell)holder.itemView;
//                if(position == categoriesHeaderRow){
//                    titleCell.setText("Explore by categories");
//                }else if(position == featuredProductHeaderRow){
//                    titleCell.setText("Featured Products");
//                }else if(position == featuredShopHeaderRow){
//                    titleCell.setText("Featured Shops");
//                }
//            } else if(holder.getItemViewType() == 6){
//                ProductCell cell = (ProductCell) holder.itemView;
//                ShopDataSerializer.FeaturedProduct messageObject = featuredProducts.get(position - featuredProductStartRow);
//                cell.setData(messageObject);
//            }
//        }
//
//
//        @Override
//        public int getItemViewType(int position) {
//
//            if(position == categoriesHeaderRow || position == featuredProductHeaderRow || position == featuredShopHeaderRow){
//                return 1;
//            }else if(position == searchRow){
//                return 2;
//            }else if(position == featuredShopRow){
//                return 3;
//            }else if(position == categoriesRow){
//                return 4;
//            }else if(position == loadingRow){
//                return 5;
//            }else if(position >= featuredProductStartRow && position < featuredProductEndRow ){
//                return 6;
//            }
//
//            return super.getItemViewType(position);
//        }
//
//        @Override
//        public int getItemCount() {
//            return rowCount;
//        }
//
//
//        private class CategoryAdapterRecycler extends RecyclerListView.SelectionAdapter {
//
//            public void setIndex(int value) {
//                notifyDataSetChanged();
//            }
//
//
//            @Override
//            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//                View view = new HintDialogCell(context);
//                view.setLayoutParams(new RecyclerView.LayoutParams(AndroidUtilities.dp(80), AndroidUtilities.dp(86)));
//                return new RecyclerListView.Holder(view);
//            }
//
//
//            @Override
//            public boolean isEnabled(RecyclerView.ViewHolder holder) {
//                return true;
//            }
//
//            @Override
//            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
//                HintDialogCell cell = (HintDialogCell) holder.itemView;
//                ShopDataSerializer.ShopSnip shop = featuredShops.get(position);
//               // cell.setShop(shop);
//            }
//
//            @Override
//            public int getItemCount() {
//                int count = 0;
//                if(featuredShops != null){
//                    count = featuredShops.size();
//                }
//                return count;
//            }
//        }
//
//        private class ProductCategoryListAdapter extends RecyclerListView.SelectionAdapter{
//
//            @Override
//            public int getItemCount() {
//                return businesses.size();
//            }
//
//            @Override
//            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//                View  view = new BusinessCell(context);
//                view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, AndroidUtilities.dp(100)));
//                return new RecyclerListView.Holder(view);
//            }
//
//            @Override
//            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//                BusinessCell shareDialogCell = (BusinessCell)holder.itemView;
//                shareDialogCell.setBusiness(businesses.get(position));
//
//
//            }
//
//            @Override
//            public boolean isEnabled(RecyclerView.ViewHolder holder) {
//                return true;
//            }
//
//        }
//
//    }
//
//
//    @Override
//    public void didReceivedNotification(int id, int account, Object... args) {
//
//        if(id == NotificationCenter.didFeaturedShopLoaded){
//            boolean loaded = (boolean)args[0];
//            if(loaded){
//                featuredShops = (ArrayList<ShopDataSerializer.ShopSnip>)args[1];
//                updateRow();
//            }
//            if(emptyTextProgressView != null && emptyTextProgressView.getVisibility() == View.VISIBLE){
//                emptyTextProgressView.setVisibility(View.GONE);
//                listView.setVisibility(View.VISIBLE);
//            }
//        }else if(id == NotificationCenter.didFeaturedProductLoaded){
//            boolean loaded = (boolean)args[0];
//            int guid = (int)args[2];
//            if(guid == classGuid){
//                if(loaded){
//                    ArrayList<ShopDataSerializer.FeaturedProduct> productArrayList = (ArrayList<ShopDataSerializer.FeaturedProduct>)args[1];
//                    featuredProducts.addAll(productArrayList);
//                    updateRow();
//                }else{
//                    APIError apiError = (APIError)args[1];
//                }
//            }
//            if(emptyTextProgressView != null && emptyTextProgressView.getVisibility() == View.VISIBLE){
//                emptyTextProgressView.setVisibility(View.GONE);
//                listView.setVisibility(View.VISIBLE);
//            }
//
//        }else if(id == NotificationCenter.didBusinessLoaded){
//            boolean loaded = (boolean)args[0];
//            if(loaded){
//                businesses = (ArrayList<ShopDataSerializer.ProductType>)args[1];
//                updateRow();
//            }
//            if(emptyTextProgressView != null && emptyTextProgressView.getVisibility() == View.VISIBLE){
//                emptyTextProgressView.setVisibility(View.GONE);
//                listView.setVisibility(View.VISIBLE);
//            }
//        }
//    }
//
//
//    private static class TitleCell extends FrameLayout {
//
//        private TextView titleView;
//
//        public TitleCell(Context context) {
//            super(context);
//
//            titleView = new TextView(getContext());
//            titleView.setLines(1);
//            titleView.setSingleLine(true);
//            titleView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
//            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
//            titleView.setTypeface(AndroidUtilities.getTypeface("fonts/Roboto-Black.ttf"));
//            titleView.setPadding(AndroidUtilities.dp(4), AndroidUtilities.dp(15), AndroidUtilities.dp(22), AndroidUtilities.dp(8));
//            titleView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
//            titleView.setGravity(Gravity.CENTER_VERTICAL);
//            addView(titleView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 60));
//        }
//
//        @Override
//        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(60), MeasureSpec.EXACTLY));
//        }
//
//        public void setText(String text) {
//            titleView.setText(text);
//        }
//    }
//
//    private  static class SearchField extends FrameLayout {
//
//        private View searchBackground;
//        private ImageView searchIconImageView;
//        private ImageView clearSearchImageView;
//        private CloseProgressDrawable2 progressDrawable;
//        private EditTextBoldCursor searchEditText;
//        private View backgroundView;
//
//
//
//        public SearchField(Context context) {
//            super(context);
//
//            searchBackground = new View(context);
//            searchBackground.setBackgroundDrawable(Theme.createRoundRectDrawable(AndroidUtilities.dp(4), Theme.getColor(Theme.key_dialogSearchBackground)));
//            addView(searchBackground, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 42, Gravity.LEFT | Gravity.TOP, 4, 0, 4, 0));
//
//            searchIconImageView = new ImageView(context);
//            searchIconImageView.setScaleType(ImageView.ScaleType.CENTER);
//            searchIconImageView.setImageResource(R.drawable.smiles_inputsearch);
//            searchIconImageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogSearchIcon), PorterDuff.Mode.MULTIPLY));
//            addView(searchIconImageView, LayoutHelper.createFrame(42, 42, Gravity.LEFT | Gravity.TOP, 10, 0, 0, 0));
//
//            clearSearchImageView = new ImageView(context);
//            clearSearchImageView.setScaleType(ImageView.ScaleType.CENTER);
//            clearSearchImageView.setImageDrawable(progressDrawable = new CloseProgressDrawable2());
//            progressDrawable.setSide(AndroidUtilities.dp(7));
//            clearSearchImageView.setScaleX(0.1f);
//            clearSearchImageView.setScaleY(0.1f);
//            clearSearchImageView.setAlpha(0.0f);
//            clearSearchImageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogSearchIcon), PorterDuff.Mode.MULTIPLY));
//            addView(clearSearchImageView, LayoutHelper.createFrame(42, 42, Gravity.RIGHT | Gravity.TOP, 14, 0, 14, 0));
//            clearSearchImageView.setOnClickListener(v -> {
//                searchEditText.setText("");
//                AndroidUtilities.showKeyboard(searchEditText);
//            });
//
//            searchEditText = new EditTextBoldCursor(context);
//            searchEditText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
//            searchEditText.setHintTextColor(Theme.getColor(Theme.key_dialogSearchHint));
//            searchEditText.setTextColor(Theme.getColor(Theme.key_dialogSearchText));
//            searchEditText.setBackgroundDrawable(null);
//            searchEditText.setPadding(0, 0, 0, 0);
//            searchEditText.setMaxLines(1);
//            searchEditText.setLines(1);
//            searchEditText.setSingleLine(true);
//
//
//
//            searchEditText.setImeOptions(EditorInfo.IME_ACTION_SEARCH | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
//
//            searchEditText.setHint("Search listing");
//            searchEditText.setCursorColor(Theme.getColor(Theme.key_featuredStickers_addedIcon));
//            searchEditText.setCursorSize(AndroidUtilities.dp(20));
//            searchEditText.setCursorWidth(1.5f);
//
//
//            addView(searchEditText, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 42, Gravity.LEFT | Gravity.TOP, 16 + 38, 0, 16 + 30, 0));
//            searchEditText.addTextChangedListener(new TextWatcher() {
//                @Override
//                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//                }
//
//                @Override
//                public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//                }
//
//                @Override
//                public void afterTextChanged(Editable s) {
//                    boolean show = searchEditText.length() > 0;
//                    boolean showed = clearSearchImageView.getAlpha() != 0;
//                    if (show != showed) {
//                        clearSearchImageView.animate()
//                                .alpha(show ? 1.0f : 0.0f)
//                                .setDuration(150)
//                                .scaleX(show ? 1.0f : 0.1f)
//                                .scaleY(show ? 1.0f : 0.1f)
//                                .start();
//                    }
//                    String text = searchEditText.getText().toString();
//                    if (text.length() != 0) {
//
////                        for(int a = 0; a < mediaPages.length; a++) {
////                            if(mediaPages[a].selectedType == 0) {
////                                mediaPages[a].emptyView.
////                                mediaPages[a].di.setVisibility(View.VISIBLE);
////                                mediaPages[a].listView.setEmptyView(null);
////                                mediaPages[a].emptyView.setVisibility(View.VISIBLE);
////                            }
////                        }
//
////                        if (searchEmptyView != null) {
////                            searchEmptyView.setText(LocaleController.getString("NoResult", R.string.NoResult));
////                        }
//                    } else {
////                        if (gridView.getAdapter() != listAdapter) {
////                            int top = getCurrentTop();
////                            searchEmptyView.setText(LocaleController.getString("NoChats", R.string.NoChats));
////                            searchEmptyView.showTextView();
////                            gridView.setAdapter(listAdapter);
////                            listAdapter.notifyDataSetChanged();
////                            if (top > 0) {
////                                layoutManager.scrollToPositionWithOffset(0, -top);
////                            }
////                        }
//                    }
////                    if (searchAdapter != null) {
////                        searchAdapter.searchDialogs(text);
////                    }te
//                }
//            });
//
//            searchEditText.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                }
//            });
//
//
//            setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                }
//            });
//            searchEditText.setOnEditorActionListener((v, actionId, event) -> {
//                if (event != null && (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_SEARCH || event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
//     //               searchProduct(searchEditText.getText().toString());
//                    AndroidUtilities.hideKeyboard(searchEditText);
//
//                    View view = v.focusSearch(FOCUS_DOWN);
//                    if (view != null) {
//                        if (!view.requestFocus(FOCUS_DOWN)) {
//                            return true;
//                        }
//                    }
//                    return false;
//
//
//                }
//                return false;
//            });
//
//
//        }
//
//        public void hideKeyboard() {
//            AndroidUtilities.hideKeyboard(searchEditText);
//        }
//    }
//
//    public interface  FeatureShopDelegate{
//
//        void didPressFeatureShop(ShopDataSerializer.ShopSnip shop);
//    }
//
//    public interface BusinessDelegate{
//        void didBusSelected(ShopDataSerializer.ProductType business);
//    }
//
//
//
//}
