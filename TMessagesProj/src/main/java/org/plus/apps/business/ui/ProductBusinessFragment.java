//package org.plus.apps.business.ui;
//
//import android.animation.AnimatorSet;
//import android.content.Context;
//import android.graphics.Canvas;
//import android.graphics.Rect;
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.view.Gravity;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.EditText;
//import android.widget.FrameLayout;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.GridLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import org.plus.apps.business.ProductCell;
//import org.plus.apps.business.ShopUtils;
//import org.plus.apps.business.data.ShopDataController;
//import org.plus.apps.business.data.ShopDataSerializer;
//import org.plus.apps.business.ui.cells.FilterHorizontalLayout;
//import org.plus.apps.business.ui.components.ProductSortAlert;
//import org.plus.apps.business.ui.components.ShopMediaLayout;
//import org.plus.apps.business.ui.components.ShopsEmptyCell;
//import org.plus.net.APIError;
//import org.telegram.messenger.AndroidUtilities;
//import org.telegram.messenger.NotificationCenter;
//import org.telegram.messenger.R;
//import org.telegram.messenger.UserConfig;
//import org.telegram.ui.ActionBar.ActionBar;
//import org.telegram.ui.ActionBar.ActionBarMenu;
//import org.telegram.ui.ActionBar.ActionBarMenuItem;
//import org.telegram.ui.ActionBar.BaseFragment;
//import org.telegram.ui.ActionBar.Theme;
//import org.telegram.ui.Cells.LoadingCell;
//import org.telegram.ui.Components.EmptyTextProgressView;
//import org.telegram.ui.Components.LayoutHelper;
//import org.telegram.ui.Components.RecyclerListView;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//
//
//public class ProductBusinessFragment extends BaseFragment implements NotificationCenter.NotificationCenterDelegate{
//
//
//    private ArrayList<Integer> favProductList = new ArrayList<>();
//
//    public void checkFav(int product_id){
//        if(favProductList.contains(product_id)){
//            favProductList.remove(product_id);
//        }else{
//            favProductList.add(product_id);
//        }
//    }
//
//    public boolean isFav(int product_id){
//       return favProductList.contains(product_id);
//    }
//
//    private HashMap<String, Object> currentFilter = new HashMap<>();
//    private ShopDataSerializer.ProductType.Sort  currentSort;
//
//    private ArrayList<ProductCell> cellCache = new ArrayList<>(10);
//    private ArrayList<ProductCell> cache = new ArrayList<>(10);
//
//    private int rowCount;
//    private int filterRow;
//    private int productStartRow;
//    private int productEndRow;
//    private int progressRow;
//    private int emptyLayoutRow;
//
//    private boolean isLoadingProduct;
//    private boolean productEndReached;
//    private boolean showEmpty;
//    private int productCount;
//
//    private ArrayList<ShopDataSerializer.Product> products = new ArrayList<>();
//
//    public void UpdateRowCount(){
//         rowCount = 0;
//         filterRow = -1;
//         productStartRow = -1;
//         productEndRow  = -1;
//         progressRow  = -1;
//         emptyLayoutRow  = -1;
//
//         filterRow = rowCount++;
//         int count = products.size();
//         if (count != 0) {
//            productStartRow = rowCount;
//            rowCount += count;
//            productEndRow = rowCount;
//        }
//
//        if(showEmpty && productStartRow == -1){
//            emptyLayoutRow = rowCount++;
//        }
//
//        if(emptyLayoutRow == -1 &&  productStartRow != -1 && !isLoadingProduct && !productEndReached){
//            progressRow = rowCount++;
//        }
//
//        if(adapter != null){
//            adapter.notifyDataSetChanged();
//        }
//
//    }
//
//    private RecyclerListView listView;
//    private ListAdapter adapter;
//    private GridLayoutManager gridLayoutManager;
//
//    private RecyclerListView searchListView;
//    private ProductSearchAdapter productSearchAdapter;
//
//    public String bus_type;
//    public String title;
//
//    public ProductBusinessFragment(Bundle args) {
//        super(args);
//    }
//
//    @Override
//    public boolean onFragmentCreate() {
//        bus_type = getArguments().getString("bus_type");
//        title = getArguments().getString("title");
//        NotificationCenter.getInstance(currentAccount).addObserver(this,NotificationCenter.didProductLoaded);
//        NotificationCenter.getInstance(currentAccount).addObserver(this,NotificationCenter.filteredProductListed);
//        getNotificationCenter().addObserver(this,NotificationCenter.didProductSearchLoaded);
//
//        ShopDataController.getInstance(UserConfig.selectedAccount).loadProducts(bus_type,null,getClassGuid());
//        return super.onFragmentCreate();
//    }
//
//
//    @Override
//    public void onFragmentDestroy() {
//        NotificationCenter.getInstance(currentAccount).removeObserver(this,NotificationCenter.didProductLoaded);
//        NotificationCenter.getInstance(currentAccount).addObserver(this,NotificationCenter.filteredProductListed);
//        getNotificationCenter().removeObserver(this,NotificationCenter.didProductSearchLoaded);
//        super.onFragmentDestroy();
//    }
//
//    private EmptyTextProgressView searcheEmptyView;
//    private AnimatorSet searchAnimator;
//    private float searchAnimationProgress;
//    private  boolean searchIsShowed;
//
//
//    private void setSearchAnimationProgress(float progress) {
//        searchAnimationProgress = progress;
//        if (fragmentView != null) {
//            fragmentView.invalidate();
//        }
//    }
//
//     private boolean searching;
//     private boolean searchWas;
//
//
//    @Override
//    public View createView(Context context) {
//
//        for (int a = 0; a < 10; a++) {
//            cellCache.add(new ProductCell(context));
//        }
//        ActionBarMenu menu = actionBar.createMenu();
//        actionBar.setCastShadows(false);
//        actionBar.setTitle(title.substring(0,1).toUpperCase() + title.substring(1).toLowerCase());
//        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
//
//        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
//            @Override
//            public void onItemClick(int id) {
//                if (id == -1) {
//                    finishFragment();
//                }
//            }
//        });
//
//        productSearchAdapter = new ProductSearchAdapter(context);
//        ActionBarMenuItem searchField =   menu.addItem(1, R.drawable.ic_ab_search).setIsSearchField(true).setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener(){
//            @Override
//            public void onSearchExpand() {
//                searching = true;
//
//            }
//
//
//            @Override
//            public void onSearchCollapse() {
//                productSearchAdapter.search(null);
//                searching = false;
//                searchWas = false;
//                listView.setAdapter(adapter);
//                showSearch(false, true);
//
//            }
//
//
//
//            @Override
//            public void onSearchPressed(EditText editText) {
//                showSearch(true, true);
//                String text = editText.getText().toString();
//                productSearchAdapter.search(text);
//                if (text.length() != 0) {
//                    searchWas = true;
//                }
//
//            }
//        });
//        searchField.setSearchFieldHint("Search in " + title);
//        searchField.setClearsTextOnSearchCollapse(true);
//        searching = false;
//        searchWas = false;
//
//        fragmentView = new FrameLayout(context);
//        FrameLayout frameLayout = (FrameLayout) fragmentView;
//        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
//
//        listView = new RecyclerListView(context);
//        gridLayoutManager = new GridLayoutManager(context,2){
//            @Override
//            public boolean supportsPredictiveItemAnimations() {
//                return false;
//            }
//        };
//        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
//            @Override
//            public int getSpanSize(int position) {
//                ListAdapter listAdapter = (ListAdapter) listView.getAdapter();
//                if(listAdapter != null){
//                    int type =  listAdapter.getItemViewType(position);
//                    if(type == 1){
//                        return 1;
//                    }else{
//                        return 2;
//                    }
//                }
//                return gridLayoutManager.getSpanCount();
//            }
//        });
//
//
//        listView.setLayoutManager(gridLayoutManager);
//        listView.setVerticalScrollBarEnabled(false);
//        listView.setHorizontalScrollBarEnabled(false);
//        listView.setClipToPadding(false);
//        listView.setPadding(AndroidUtilities.dp(8),0,AndroidUtilities.dp(8),0);
//        listView.setAdapter(adapter = new ListAdapter(context));
//        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
//
//        listView.addItemDecoration(new RecyclerView.ItemDecoration() {
//            @Override
//            public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
//                super.onDraw(c, parent, state);
//            }
//
//            @Override
//            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
//                outRect.left = 8;
//                outRect.right = 8;
//                outRect.bottom = 8;
//                outRect.top = 8;
//            }
//        });
//
//        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                checkLoadMoreScroll();
//            }
//        });
//        listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener() {
//            @Override
//            public void onItemClick(View view, int position) {
//                 ShopDataSerializer.Product product =  adapter.getItem(position);
//                if(product != null){
//                    Bundle bundle = new Bundle();
//                    bundle.putInt("chat_id", ShopUtils.toClientChannelId(product.shop.chanel));
//                    bundle.putInt("item_id",product.id);
//
//                    ProductDetailFragment detailFragment = new ProductDetailFragment(bundle);
//                    presentFragment(detailFragment);
//                }
//            }
//        });
//
//        searchListView = new RecyclerListView(context);
//        searchListView.setVerticalScrollBarEnabled(false);
//        searchListView.setGlowColor(Theme.getColor(Theme.key_avatar_backgroundActionBarBlue));
//        searchListView.setAdapter(productSearchAdapter);
//        searchListView.setItemAnimator(null);
//        GridLayoutManager gridLayoutManager = new GridLayoutManager(context,2){
//            @Override
//            public boolean supportsPredictiveItemAnimations() {
//                return false;
//            }
//        };
//         gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
//            @Override
//            public int getSpanSize(int position) {
//                ProductSearchAdapter listAdapter = (ProductSearchAdapter) searchListView.getAdapter();
//                if(listAdapter != null){
//                    int type =  listAdapter.getItemViewType(position);
//                    if(type == 1){
//                        return 1;
//                    }else{
//                        return 2;
//                    }
//                }
//                return gridLayoutManager.getSpanCount();
//            }
//        });
//
//
//        searchListView.addItemDecoration(new RecyclerView.ItemDecoration() {
//            @Override
//            public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
//                super.onDraw(c, parent, state);
//            }
//
//            @Override
//            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
//                outRect.left = 8;
//                outRect.right = 8;
//                outRect.bottom = 8;
//                outRect.top = 8;
//            }
//        });
//
//
//        searchListView.setLayoutManager(gridLayoutManager);
//        searchListView.setVisibility(View.GONE);
//        searchListView.setLayoutAnimation(null);
//        searchListView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
//        frameLayout.addView(searchListView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
//        searchListView.setOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
//                    AndroidUtilities.hideKeyboard(getParentActivity().getCurrentFocus());
//                }
//            }
//        });
//
////        searcheEmptyView = new EmptyTextProgressView(context);
////        searcheEmptyView.showProgress();
////        searcheEmptyView.setVisibility(View.GONE);
////        searchListView.setEmptyView(searcheEmptyView);
////        frameLayout.addView(searcheEmptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
//
//
//        return fragmentView;
//    }
//
//    private void showSearch(boolean show, boolean animated) {
//        if(show){
//            searchListView.setVisibility(View.VISIBLE);
//        }else{
//            searchListView.setVisibility(View.GONE);
//
//        }
//    }
//
//    private String nextLoadProduct;
//
//    private void checkLoadMoreScroll() {
//        if(listView.getAdapter() == null){
//            return;
//        }
//        int firstVisibleItem = gridLayoutManager.findFirstVisibleItemPosition();
//        int visibleItemCount = firstVisibleItem == RecyclerView.NO_POSITION ? 0 : Math.abs(gridLayoutManager.findLastVisibleItemPosition() - firstVisibleItem) + 1;
//        int totalItemCount = listView.getAdapter().getItemCount();
//
//        final int threshold = 3;
//
//        if(listView.getAdapter() instanceof ProductSearchAdapter){
//            if (firstVisibleItem + visibleItemCount > totalItemCount - threshold && !productSearchAdapter.loadingSearch) {
//
//                if(!productSearchAdapter.searchProductEndReached){
//                    productSearchAdapter.loadingSearch = true;
//                    productSearchAdapter.loadMoreSearchResult();
//                }
//            }
//            productSearchAdapter.loadMoreSearchResult();
//        }else{
//            if (firstVisibleItem + visibleItemCount > totalItemCount - threshold && !isLoadingProduct) {
//
//                if(!productEndReached){
//                    isLoadingProduct = true;
//                    ShopDataController.getInstance(UserConfig.selectedAccount).loadMoreProductForShop(nextLoadProduct,getClassGuid());
//                }
//            }
//        }
//
//
//    }
//
//
//    @Override
//    public void didReceivedNotification(int id, int account, Object... args) {
//
//        if(id == NotificationCenter.didProductLoaded){
//
//            boolean loaded = (boolean)args[0];
//            int guid = (int)args[2];
//            if(classGuid == guid){
//                isLoadingProduct = false;
//                if(loaded){
//                    ArrayList<ShopDataSerializer.Product> productArrayList = (ArrayList<ShopDataSerializer.Product>)args[1];
//                    nextLoadProduct = (String) args[3];
//                    if(ShopUtils.isEmpty(nextLoadProduct)){
//                        productEndReached = true;
//                    }else{
//                        productEndReached = false;
//                    }
//                    products.addAll(productArrayList);
//
//                    if(products.isEmpty() && productEndReached){
//                        showEmpty = true;
//                    }else{
//                        showEmpty = false;
//                    }
//                    productCount = (int)args[4];
//                    products.addAll(productArrayList);
//                    UpdateRowCount();
//                }
//
//
//            }
//        }else if(id == NotificationCenter.filteredProductListed) {
//            boolean loaded = (boolean) args[0];
//            int guid = (int) args[2];
//            if (guid == classGuid) {
//                if (loaded) {
//                    isLoadingProduct = false;
//                    ArrayList<ShopDataSerializer.Product> productArrayList = (ArrayList<ShopDataSerializer.Product>) args[1];
//                    nextLoadProduct = (String) args[3];
//                    if (ShopUtils.isEmpty(nextLoadProduct)) {
//                        productEndReached = true;
//                    } else {
//                        productEndReached = false;
//                    }
//                    products.addAll(productArrayList);
//
//                    if (products.isEmpty() && productEndReached) {
//                        showEmpty = true;
//                    } else {
//                        showEmpty = false;
//                    }
//                    UpdateRowCount();
//                } else {
//                    APIError apiError = (APIError) args[1];
//                   // isLoadingProduct = true;
////                    if(products.isEmpty()){
////                        ShopDataController.getInstance(UserConfig.selectedAccount).loadProductsForShop(profileActivity.getChat_id(),,profileActivity.getClassGuid());
////                    }
//                }
//
//
//            }
//
//        }else if(id == NotificationCenter.didProductSearchLoaded){
//
////            boolean loaded = (boolean)args[0];
////            int guid = (int)args[2];
////            if (guid == classGuid) {
////                ArrayList<ShopDataSerializer.Product> productArrayList = (ArrayList<ShopDataSerializer.Product>) args[1];
////
////            }
//
//    }
//    }
//
//
//    private class ListAdapter extends RecyclerListView.SelectionAdapter{
//
//        private Context context;
//
//        public ListAdapter(Context context) {
//            this.context = context;
//        }
//
//
//        @Override
//        public boolean isEnabled(RecyclerView.ViewHolder holder) {
//            return holder.getItemViewType() == 1;
//        }
//
//        @NonNull
//        @Override
//        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//
//            View view;
//            switch (viewType){
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
//                case 2:
//                    ShopsEmptyCell dialogsEmptyCell = new ShopsEmptyCell(context);
//                    dialogsEmptyCell.setType(4);
//                    view = dialogsEmptyCell;
//                    break;
//                case 4:
//                    FilterHorizontalLayout filterHorizontalLayout = new FilterHorizontalLayout(context);
//                    filterHorizontalLayout.setText(title);
//                    view  = filterHorizontalLayout;
//                    filterHorizontalLayout.onFilterClick(v -> {
//                        ProductFilterActivity filterActivity = new ProductFilterActivity(bus_type);
//                        filterActivity.setFilterDelegate(filterMap -> {
//                            currentFilter = filterMap;
//                            processSelectedFilter();
//                        });
//                        presentFragment(filterActivity);
//                    });
//
//                    filterHorizontalLayout.onSortClick(v -> {
//                        ProductSortAlert productSortAlert = new ProductSortAlert(context,false,bus_type,false,currentSort);
//                        productSortAlert.setDelegate(new ProductSortAlert.SortAlertDelegate() {
//                            @Override
//                            public void didSortSelected(ShopDataSerializer.ProductType.Sort sort) {
//                                if(sort != null){
//                                    currentSort = sort;
//                                    processSelectedFilter();
//
//                                }
//                            }
//                        });
//                        showDialog(productSortAlert);
//                    });
//                    break;
//                case 3:
//                default:
//                    view = new LoadingCell(context, AndroidUtilities.dp(32), AndroidUtilities.dp(74));
//                    break;
//            }
//
//            return new RecyclerListView.Holder(view);
//        }
//
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
//                cell.setData(messageObject);
//            }
//        }
//
//        @Override
//        public int getItemViewType(int position) {
//
//            if (position >= productStartRow && position < productEndRow) {
//                return 1;
//            }else if(position == emptyLayoutRow){
//                return 2;
//            }else if(position == progressRow){
//                return 3;
//            }else if(position == filterRow){
//                return 4;
//            }
//            return super.getItemViewType(position);
//        }
//
//        @Override
//        public int getItemCount() {
//            return rowCount;
//        }
//    }
//
//
//    private void processSelectedFilter(){
//        products.clear();
//        isLoadingProduct = true;
//        productEndReached = false;
//        showEmpty  = true;
//        listView.setEmptyView(null);
//        ShopDataController.getInstance(UserConfig.selectedAccount).filterProductFromBusiness(bus_type,currentFilter,currentSort,classGuid);
//
//    }
//
//    private  class ProductSearchAdapter extends RecyclerListView.SelectionAdapter{
//
//        private HashMap<String, Object> currentSearchFilter = new HashMap<>();
//        private ShopDataSerializer.ProductType.Sort currentSearchSort;
//
//        private int rowCount;
//        private int filterRow;
//        private int productStartRow;
//        private int productEndRow;
//        private int progressRow;
//        private int emptyLayoutRow;
//
//        private boolean showEmpty;
//
//        public void updateRow(){
//            rowCount = 0;
//            filterRow = -1;
//            productStartRow = -1;
//            productEndRow  = -1;
//            progressRow  = -1;
//            emptyLayoutRow  = -1;
//
//            filterRow = rowCount++;
//            int count = searchResult.size();
//            if (count != 0) {
//                productStartRow = rowCount;
//                rowCount += count;
//                productEndRow = rowCount;
//            }
//
//            if(showEmpty && productStartRow == -1){
//                emptyLayoutRow = rowCount++;
//            }
//
//            if(emptyLayoutRow == -1 &&  productStartRow != -1 && !loadingSearch && !searchProductEndReached){
//                progressRow = rowCount++;
//            }
//
//            if(productSearchAdapter != null){
//                productSearchAdapter.notifyDataSetChanged();
//            }
//
//        }
//
//        private ArrayList<ShopDataSerializer.Product> searchResult = new ArrayList<>();
//
//        private Runnable searchRunnable;
//        private int searchesInProgress = 0;
//        private int reqId = 0;
//        private boolean searchProductEndReached;
//        private String nextLoad;
//        private int searchProductCount;
//        private String currentQuery;
//        private boolean loadingSearch;
//
//        private Context mContext;
//
//        public ProductSearchAdapter(Context context) {
//            mContext = context;
//
//        }
//
//        public void loadMoreSearchResult(){
//            if(searchProductEndReached || ShopUtils.isEmpty(nextLoad)){
//                return;
//            }
//            ShopDataController.getInstance(currentAccount).loadMoreSearchProduct(nextLoad, new ShopDataController.ProductSearchCallBack() {
//                @Override
//                public void run(Object response, APIError error, String next, int count) {
//                    if(response != null && error == null){
//                        searchProductCount = count;
//                        ArrayList<ShopDataSerializer.Product> pros = (ArrayList<ShopDataSerializer.Product>)response;
//                        nextLoad = next;
//                        if(ShopUtils.isEmpty(nextLoad)){
//                            searchProductEndReached = true;
//                        }else{
//                            searchProductEndReached = false;
//                        }
//                        searchResult.addAll(pros);
//                        if(searchResult.isEmpty() && searchProductEndReached){
//                            showEmpty = true;
//                        }else{
//                            showEmpty = false;
//                        }
//                        updateRow();
//                    }
//                }
//            });
//
//        }
//
//        private void processSelectedFilter(){
//            isLoadingProduct = true;
//            productEndReached = false;
//            showEmpty  = true;
//
//            listView.setEmptyView(null);
//            ShopDataController.getInstance(UserConfig.selectedAccount).filterProductFromBusiness(bus_type,currentFilter,currentSort,classGuid);
//
//        }
//
//        public void search(String search){
//            currentQuery = search;
//            if (searchRunnable != null) {
//                AndroidUtilities.cancelRunOnUIThread(searchRunnable);
//                searchRunnable = null;
//            }
//            if (TextUtils.isEmpty(currentQuery)) {
//                if (!searchResult.isEmpty()  || searchesInProgress != 0) {
//                    searchResult.clear();
//                    if (reqId != 0) {
//                        ShopDataController.getInstance(currentAccount).cancelRequest(reqId);
//                        reqId = 0;
//                       searching = false;
//                    }
//                }
//                updateRow();
//                return;
//            }
//            searching = true;
//            searchServer();
//        }
//
//        private void searchServer(){
//            AndroidUtilities.runOnUIThread(searchRunnable = () -> reqId =  ShopDataController.getInstance(currentAccount).searchProductForBusiness(bus_type, currentQuery, currentSearchSort, currentSearchFilter, new ShopDataController.ProductSearchCallBack() {
//                @Override
//                public void run(Object response, APIError error, String next, int count) {
//                    searching = false;
//                    reqId = 0;
//                    if(response != null && error == null){
//                        searchProductCount = count;
//                        ArrayList<ShopDataSerializer.Product> pros = (ArrayList<ShopDataSerializer.Product>)response;
//                        nextLoad = next;
//                        if(ShopUtils.isEmpty(nextLoad)){
//                            searchProductEndReached = true;
//                        }else{
//                            searchProductEndReached = false;
//                        }
//                        searchResult.addAll(pros);
//                        if(searchResult.isEmpty() && searchProductEndReached){
//                            showEmpty = true;
//                        }else{
//                            showEmpty = false;
//                        }
//                        updateRow();
//                    }
//                }
//            }),300);
//        }
//
//        @Override
//        public boolean isEnabled(RecyclerView.ViewHolder holder) {
//            return holder.getItemViewType() == 1;
//        }
//
//        @NonNull
//        @Override
//        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//            View view;
//            switch (viewType){
//                case 1:
//                    if (!cellCache.isEmpty()) {
//                        view = cellCache.get(0);
//                        cellCache.remove(0);
//                        ViewGroup p = (ViewGroup) view.getParent();
//                        if (p != null) {
//                            p.removeView(view);
//                        }
//                    } else {
//                        view = new ProductCell(mContext);
//                    }
//                    cache.add((ProductCell) view);
//                    break;
//                case 2:
//                    ShopsEmptyCell dialogsEmptyCell = new ShopsEmptyCell(mContext);
//                    dialogsEmptyCell.setType(0);
//                    view = dialogsEmptyCell;
//                    break;
//                case 4:
//                    FilterHorizontalLayout filterHorizontalLayout = new FilterHorizontalLayout(mContext);
//                    filterHorizontalLayout.setText(title);
//                    view  = filterHorizontalLayout;
////                    filterHorizontalLayout.onFilterClick(v -> {
////
////                        ProductFilterActivity filterActivity = new ProductFilterActivity(bus_type);
////                        filterActivity.setFilterDelegate(filterMap -> {
////                            currentSearchFilter = filterMap;
////                            processSelectedFilter();
////                        });
////                        presentFragment(filterActivity);
////                    });
//
//
//
//                    filterHorizontalLayout.onSortClick(v -> {
//                        ProductSortAlert productSortAlert = new ProductSortAlert(mContext,false,bus_type,false,currentSearchSort);
//                        productSortAlert.setDelegate(new ProductSortAlert.SortAlertDelegate() {
//                            @Override
//                            public void didSortSelected(ShopDataSerializer.ProductType.Sort sort) {
//                                if(sort != null){
//                                    currentSearchSort = sort;
//                                    processSelectedFilter();
//
//                                }
//                            }
//                        });
//                        showDialog(productSortAlert);
//                    });
//                    break;
//                case 3:
//                default:
//                    view = new LoadingCell(mContext, AndroidUtilities.dp(32), AndroidUtilities.dp(74));
//                    break;
//            }
//
//            return new RecyclerListView.Holder(view);
//        }
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
//        @Override
//        public int getItemViewType(int position) {
//
//            if (position >= productStartRow && position < productEndRow) {
//                return 1;
//            }else if(position == emptyLayoutRow){
//                return 2;
//            }else if(position == progressRow){
//                return 3;
//            }else if(position == filterRow){
//                return 4;
//            }
//            return super.getItemViewType(position);
//        }
//
//        public ShopDataSerializer.Product getItem(int position) {
//            int newPos = position - productStartRow;
//            if(newPos < 0 || newPos >= searchResult.size()){
//                return null;
//            }
//            return searchResult.get(position - productStartRow);
//        }
//
//
//        @Override
//        public int getItemCount() {
//            return rowCount;
//        }
//    }
//}
