package org.plus.apps.business;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.exoplayer2.util.Log;

import org.plus.apps.business.GlobalSearchActivity;
import org.plus.apps.business.ProductCell;
import org.plus.apps.business.ShopUtils;
import org.plus.apps.business.data.ShopDataController;
import org.plus.apps.business.data.ShopDataSerializer;
import org.plus.apps.business.ui.BusinessProfileActivity;
import org.plus.apps.business.ui.ProductDetailFragment;
import org.plus.apps.business.ui.ProductFilterActivity;
import org.plus.apps.business.ui.components.HorizontalFilterLayout;
import org.plus.apps.business.ui.components.ProductSortAlert;
import org.plus.apps.business.ui.components.ShopsEmptyCell;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.FlickerLoadingView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SearchField;

import java.util.ArrayList;
import java.util.HashMap;

public class ProductCategoryActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private int reqId = 0;

    private ArrayList<ProductCell> cellCache = new ArrayList<>(10);
    private ArrayList<ProductCell> cache = new ArrayList<>(10);

    private ArrayList<ShopDataSerializer.Field> fields  = new ArrayList<>();
    private ArrayList<ShopDataSerializer.Product> products = new ArrayList<>();

    private RecyclerListView listView;
    private ListAdapter adapter;
    private GridLayoutManager gridLayoutManager;
    private HorizontalFilterLayout spanListLayout;
    private FlickerLoadingView progressView;

    private String bus_type;
    private String title;
    private String section;

    private String nextLoadProduct;
    private boolean isLoadingProduct;
    private boolean productEndReached;
    private boolean showEmpty;
    private boolean showError;
    private int productCount;

    private int rowCount;
    private int filterRow;
    private int filterInfoRow;
    private int productStartRow;
    private int productEndRow;
    private int loadingRow;
    private int emptyLayoutRow;
    private int errorLayoutRow;

    public ProductCategoryActivity(Bundle args) {
        super(args);
    }

    @Override
    public boolean onFragmentCreate() {
        if(getArguments() != null){
            bus_type = getArguments().getString("bus_type");
            title = getArguments().getString("title");
        }
        getNotificationCenter().addObserver(this, NotificationCenter.didConfigrationLoaded);
        getNotificationCenter().addObserver(this, NotificationCenter.didProductLoaded);
        section = bus_type + "-filter";
        ShopDataController.getInstance(currentAccount).loadSectionConfiguration(section,true,classGuid);

        ShopDataController.getInstance(UserConfig.selectedAccount).loadProducts(bus_type,null,getClassGuid());
        return super.onFragmentCreate();
    }

    @Override
    public boolean isSwipeBackEnabled(MotionEvent event) {
        return false;
    }

    @Override
    public void onFragmentDestroy() {
       getNotificationCenter().removeObserver(this, NotificationCenter.didConfigrationLoaded);
       getNotificationCenter().removeObserver(this, NotificationCenter.didProductLoaded);

       if(reqId != 0){
           ShopDataController.getInstance(currentAccount).cancelRequest(reqId);
       }

       super.onFragmentDestroy();
    }
    private Paint backgroundPaint = new Paint();


    private void updateRow(){

       rowCount = 0;
       filterRow = -1;
       filterInfoRow = -1;
       productStartRow = -1;
       productEndRow = -1;
       loadingRow = -1;
       emptyLayoutRow = -1;
        errorLayoutRow = -1;


        if(fields != null && fields.size() > 0 && products.size() > 0){
            filterRow = rowCount++;
        }


        if(currentFilter.size() > 1){
            filterInfoRow = rowCount++;
        }
        int count = products.size();
        if (count != 0) {
            productStartRow = rowCount;
            rowCount += count;
            productEndRow = rowCount;
        }

        if(showEmpty && productStartRow == -1){
            emptyLayoutRow = rowCount++;
        }

        if(emptyLayoutRow == -1 &&  productStartRow != -1 && !ShopUtils.isEmpty(nextLoadProduct)){
            loadingRow = rowCount++;
        }

        if(showError && productStartRow == -1){
            errorLayoutRow = rowCount++;
        }

        if(adapter != null){
            adapter.notifyDataSetChanged();
            listView.scheduleLayoutAnimation();
        }
    }

    private ShopDataSerializer.ProductType.Sort currentSort;
    private HashMap<String, Object> currentFilter = new HashMap<>();
    private SwipeRefreshLayout refreshLayout;

    private void loadProducts(){
        products.clear();
        isLoadingProduct = true;
        productEndReached = false;
        progressView.setVisibility(View.VISIBLE);
        listView.setEmptyView(null);
        currentFilter.put("ordering",ShopUtils.formatSort(currentSort));
        ShopDataController.getInstance(UserConfig.selectedAccount).loadProducts(bus_type,currentFilter,classGuid);
    }

    @Override
    public View createView(Context context) {
        for (int a = 0; a < 10; a++) {
            cellCache.add(new ProductCell(context));
        }
        ActionBarMenu menu = actionBar.createMenu();
        actionBar.setCastShadows(false);
        actionBar.setTitle(title);

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        int top_margin  = actionBar.getOccupyStatusBar()?(int)(AndroidUtilities.statusBarHeight/AndroidUtilities.density):0;

//        SearchField searchField = new SearchField(context){
//            @Override
//            protected void onFieldTouchUp(EditTextBoldCursor editText) {
//                presentFragment(new GlobalSearchActivity());
//            }
//        };


//        if(title.length() > 1)
//           searchField.setHint("Search in " + title.substring(0,1).toUpperCase() + title.substring(1).toLowerCase());
//
//        actionBar.addView(searchField, 0, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, Gravity.BOTTOM | Gravity.LEFT,40, top_margin, 0, 0));

        menu.addItem(1,R.drawable.ic_ab_search);

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }else if(id == 1){
                    Bundle bundle = new Bundle();
                    bundle.putString("bus_type",title);
                    bundle.putString("bus_type",bus_type);
                    presentFragment(new GlobalSearchActivity(bundle));

                }
            }
        });

        spanListLayout = new HorizontalFilterLayout(context,new HorizontalFilterLayout.FilterDelegate(){

            @Override
            public void onItemClick(View view, int position, HorizontalFilterLayout.Filter imageInput, float x, float y) {

            }

            @Override
            public void onItemLonClick(View view, int position) {

            }
        });
        spanListLayout.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        spanListLayout.setDelegate(new HorizontalFilterLayout.FilterDelegate() {
            @Override
            public void onItemClick(View view, int position, HorizontalFilterLayout.Filter filter, float x, float y) {
                if(filter.key.equals("filter")){
                    ProductFilterActivity  productFilterActivity = new ProductFilterActivity(bus_type);
                    presentFragment(productFilterActivity);
                    productFilterActivity.setFilterDelegate(filterMap -> {
                        currentFilter = filterMap;
                        loadProducts();
                    });
                }else if(filter.key.equals("sort")){
                    ProductSortAlert productSortAlert = new ProductSortAlert(context, false, bus_type, false, currentSort);
                    productSortAlert.setDelegate(sort -> {
                        if (sort != null) {
                            currentSort = sort;
                            loadProducts();
                        }
                    });
                  showDialog(productSortAlert);
                }else{
                    ProductFilterActivity productFilterActivity = new ProductFilterActivity(bus_type,filter.key);
                     productFilterActivity.setFilterDelegate(filterMap -> {
                        currentFilter = filterMap;
                        loadProducts();
                    });
                    presentFragment(productFilterActivity);
                }
            }

            @Override
            public void onItemLonClick(View view, int position) {

            }
        });

        fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = (FrameLayout) fragmentView;
        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

        refreshLayout = new SwipeRefreshLayout(context);
        listView = new RecyclerListView(context){

            @Override
            protected boolean allowSelectChildAtPosition(View child) {
                if(child instanceof HorizontalFilterLayout){
                    return false;
                }
                return super.allowSelectChildAtPosition(child);
            }
        };
        gridLayoutManager = new GridLayoutManager(context,2){
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return true;
            }
        };

        listView.setLayoutManager(gridLayoutManager);
        listView.setVerticalScrollBarEnabled(true);
        listView.setHorizontalScrollBarEnabled(false);
        listView.setClipToPadding(false);
        listView.setPadding(AndroidUtilities.dp(8),0,AndroidUtilities.dp(8),0);
        listView.setAdapter(adapter = new ListAdapter(context));
        refreshLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        listView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.onDraw(c, parent, state);
            }

            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                RecyclerListView.Holder holder = (RecyclerListView.Holder) parent.getChildViewHolder(view);
                if(holder != null){
                    if(holder.getItemViewType() ==  1){
                        outRect.left = 16;
                        outRect.right = 16;
                        outRect.bottom = 16;
                        outRect.top = 16;
                    }
                }
            }
        });

        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
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
                return gridLayoutManager.getSpanCount();
            }
        });

        listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                ShopDataSerializer.Product product = adapter.getItem(position);
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
        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                checkLoadMoreScroll();
            }
        });

        int resId = R.anim.layotu_anim_from_bottom;
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(context, resId);
        listView.setLayoutAnimation(animation);
        listView.setLayoutAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if(gridLayoutManager != null){
                    View filterView = gridLayoutManager.findViewByPosition(filterRow);
                    if(filterView != null){
                        filterView.clearAnimation();
                    }
                    View filterInfoView = gridLayoutManager.findViewByPosition(filterInfoRow);
                    if(filterInfoView != null){
                        filterInfoView.clearAnimation();
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

        frameLayout.addView(refreshLayout,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.MATCH_PARENT));

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

        refreshLayout.setOnRefreshListener(this::refresh);
        return fragmentView;
    }

    private void checkLoadMoreScroll() {
        if (listView.getAdapter() == null) {
            return;
        }
        int firstVisibleItem = gridLayoutManager.findFirstVisibleItemPosition();
        int visibleItemCount = firstVisibleItem == RecyclerView.NO_POSITION ? 0 : Math.abs(gridLayoutManager.findLastVisibleItemPosition() - firstVisibleItem) + 1;
        int totalItemCount = listView.getAdapter().getItemCount();
        final int threshold = 3;
        if (firstVisibleItem + visibleItemCount > totalItemCount - threshold && !isLoadingProduct) {
            if(!productEndReached){
                isLoadingProduct = true;
               getShopDataController().loadMoreProductForShop(nextLoadProduct,getClassGuid());
            }
        }
    }


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
        ShopDataController.getInstance(UserConfig.selectedAccount).loadProducts(bus_type,null,getClassGuid());
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
                    spanListLayout.setFilters(fields);
                    updateRow();
                }
            }
       } else if(id == NotificationCenter.didProductLoaded){
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
                    productCount = (int)args[4];
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
                case 2:
                    ShopsEmptyCell dialogsEmptyCell = new ShopsEmptyCell(mContext);
                    dialogsEmptyCell.setType(4);
                    view = dialogsEmptyCell;
                    break;
                case 3:
                    view = new InfoTextView(mContext);
                    break;
                case 4:
                    if (spanListLayout.getParent() != null) {
                        ((ViewGroup) spanListLayout.getParent()).removeView(spanListLayout);
                    }
                    view = spanListLayout;
                    break;
                case 6:
                    EmptyTextProgressView emptyTextProgressView = new EmptyTextProgressView(mContext);
                    emptyTextProgressView.setTopImage(R.drawable.ic_connection);
                    emptyTextProgressView.setText("Connection issues!");
                    emptyTextProgressView.showTextView();
                    view = emptyTextProgressView;
                    emptyTextProgressView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
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

            }else if(holder.getItemViewType() == 3){
                InfoTextView simpleTextView = (InfoTextView) holder.itemView;
                simpleTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        currentFilter.clear();
                        loadProducts();
                    }
                });
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position >= productStartRow && position < productEndRow) {
                return 1;
            }else if(position == emptyLayoutRow){
                return 2;
            }else if(position == filterInfoRow){
                return 3;
            }else if(position == filterRow){
                return 4;
            }else if(position == loadingRow){
                return 5;
            }else if(position == errorLayoutRow){
                return 6;
            }
            return super.getItemViewType(position);
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }
    }


    public class InfoTextView extends FrameLayout {

        private TextView textView;
        private RectF rect = new RectF();

        public InfoTextView(Context context) {
            super(context);

            textView = new TextView(context);
            textView.setTextColor(Theme.getColor(Theme.key_chats_menuItemText));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            textView.setLines(1);
            textView.setText("");
            textView.setMaxLines(1);
            textView.setSingleLine(true);
            textView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            textView.setCompoundDrawablePadding(AndroidUtilities.dp(8));
            addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.CENTER_VERTICAL, 19, 0, 16, 0));

            setTextAndIcon("Showing result based on applied filters",R.drawable.msg_forward);
        }


        public TextView getTextView() {
            return textView;
        }

                @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(48), MeasureSpec.EXACTLY));
        }

//        @Override
//        protected void onAttachedToWindow() {
//            super.onAttachedToWindow();
//            textView.setTextColor(Theme.getColor(Theme.key_chats_menuItemText));
//        }


        public void setTextAndIcon(String text, int resId) {
            try {
                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
                spannableStringBuilder.append(text);
                int start = spannableStringBuilder.length();
                spannableStringBuilder.append(" Reset");
                int end = spannableStringBuilder.length();
                spannableStringBuilder.setSpan(new ForegroundColorSpan(Theme.getColor(Theme.key_dialogTextBlue)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                textView.setText(spannableStringBuilder);

                Drawable drawable = getResources().getDrawable(resId).mutate();
                if (drawable != null) {
                    drawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chats_menuItemIcon), PorterDuff.Mode.MULTIPLY));
                }
                textView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            } catch (Throwable e) {
                FileLog.e(e);
            }
        }
    }


}
