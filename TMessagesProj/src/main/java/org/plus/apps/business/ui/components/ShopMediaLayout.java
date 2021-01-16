package org.plus.apps.business.ui.components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import org.plus.apps.business.ui.BusinessProfileActivity;
import org.plus.apps.business.ui.ProductDetailFragment;
import org.plus.apps.business.ui.ProductFilterActivity;
import org.plus.apps.business.ui.StoreReviewFragment;
import org.plus.apps.business.ui.cells.EditUserReviewCell;
import org.plus.apps.business.ui.cells.FilterHorizontalLayout;
import org.plus.apps.business.ui.cells.ShopReviewCell;
import org.plus.apps.business.ui.cells.ShopReviewInputCell;
import org.plus.apps.business.ui.cells.Ui_info_bold_cell;
import org.plus.net.APIError;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.EmptyCell;
import org.telegram.ui.Cells.LoadingCell;
import org.telegram.ui.Cells.SharedPhotoVideoCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Components.ClippingImageView;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RadialProgressView;
import org.telegram.ui.Components.RecyclerAnimationScrollHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.ScrollSlidingTextTabStrip;

import java.util.ArrayList;
import java.util.HashMap;


@SuppressLint("ViewConstructor")
public class ShopMediaLayout extends FrameLayout implements NotificationCenter.NotificationCenterDelegate {


    private static class MediaPage extends FrameLayout {

        private RecyclerListView listView;
        private LinearLayout progressView;
        private TextView emptyTextView;
        private GridLayoutManager layoutManager;
        private ImageView emptyImageView;
        private LinearLayout emptyView;
        private RadialProgressView progressBar;
        private RecyclerAnimationScrollHelper scrollHelper;
        private int selectedType;

        public MediaPage(Context context) {
            super(context);
        }
    }

    private ArrayList<ProductCell> cellCache = new ArrayList<>(10);
    private ArrayList<ProductCell> cache = new ArrayList<>(10);
    
    private ActionBar actionBar;
    private FakeSearchParent searchField;

    private ProductAdapter productAdapter;
    private ReviewAdapter reviewAdapter;
    private AboutAdapter aboutAdapter;


    private SwipeRefreshLayout refreshLayout;
    private MediaPage[] mediaPages = new MediaPage[2];
    private Drawable pinnedHeaderShadowDrawable;
    private ScrollSlidingTextTabStrip scrollSlidingTextTabStrip;
    private View shadowLine;

    private int maximumVelocity;
    private Paint backgroundPaint = new Paint();
    
    private AnimatorSet tabsAnimation;
    private boolean tabsAnimationInProgress;
    private boolean animatingForward;
    private boolean backAnimation;

    private int chat_id;
    private int columnsCount = 2;

    private static final Interpolator interpolator = t -> {
        --t;
        return t * t * t * t * t + 1.0F;
    };

    private int startedTrackingPointerId;
    private boolean startedTracking;
    private boolean maybeStartTracking;
    private int startedTrackingX;
    private int startedTrackingY;
    private VelocityTracker velocityTracker;
    
    private  int productRowCount;
    private  int reviewRowCount;
    private int  aboutRowCount;


    private int listingNumberRow;
    private int searchRow;
    private int filterRow;
    private int productStartRow;
    private int productEndRow;
    private int progressRow;
    private int emptyLayoutRow;



    private int reviewActionRow;
    private int reviewHeaderRow;
    private int reviewSortRow;
    private int reviewStartRow;
    private int reviewEndRow;
    private int reviewLoadingRow;
    private int userReviewRow;

    private int aboutCityRow;
    private int aboutAddressRow;
    private int aboutWebsiteRow;
    private int aboutDescRow;

    private boolean commentEndReached;
    private int commentTotalCount;
    private boolean userCanComment;
    private boolean userHasCommented;

    private ShopDataSerializer.Shop currentShop;
    private ShopDataSerializer.Review currentUserReview;

    private boolean showEmpty;

    private String nextComment;
    private String nextLoadProduct;

    private boolean isLoadingProduct;
    private boolean productEndReached;
    private int productCount;
    private boolean loadingReview;

    private ArrayList<ShopDataSerializer.Review> reviews = new ArrayList<>();
    private ArrayList<ShopDataSerializer.ProductType> businesses = new ArrayList<>();
    private ArrayList<ShopDataSerializer.Product> products = new ArrayList<>();


    private BusinessProfileActivity profileActivity;

    private String selected_business = "general";
    private String current_search;
    private ShopDataSerializer.ProductType.Sort currentReviewSort;
    private ShopDataSerializer.ProductType.Sort currentSort;
    private HashMap<String, Object> currentFilter = new HashMap<>();

    private boolean shopReviewSelfLoaded;

    private void updateRow(int id){
         listingNumberRow = -1;
         searchRow = -1;
         filterRow = -1;
         productStartRow = -1;
         productEndRow = -1;
         progressRow = -1;
         emptyLayoutRow = -1;

         reviewActionRow = -1;
         reviewHeaderRow = -1;
         reviewSortRow = -1;
         reviewStartRow = -1;
         reviewEndRow = -1;
         reviewLoadingRow = -1;
         userReviewRow = -1;

         aboutCityRow = -1;
         aboutAddressRow = -1;
         aboutWebsiteRow = -1;
         aboutDescRow = -1;
        if(id == 0){
            productRowCount = 0;
            listingNumberRow = productRowCount++;
            searchRow = productRowCount++;
            filterRow = productRowCount++;

            int count = products.size();

            if (count != 0) {
                productStartRow = productRowCount;
                productRowCount += count;
                productEndRow = productRowCount;
            }

            if (showEmpty && productStartRow == -1) {
                emptyLayoutRow = productRowCount++;
            }

            if (emptyLayoutRow == -1 && productEndRow != -1 && !isLoadingProduct && !productEndReached) {
                progressRow = productRowCount++;
            }

            if (productAdapter != null) {
                productAdapter.notifyDataSetChanged();
            }
        }else if(id == 1){
            reviewRowCount = 0;
            int count = reviews.size();

            if (userCanComment) {
                reviewActionRow = reviewRowCount++;
            }

            if (userHasCommented) {
                userReviewRow = reviewRowCount++;
            }

            if (count != 0) {

               // reviewSortRow = reviewRowCount++;
                reviewHeaderRow = reviewRowCount++;

                reviewStartRow = reviewRowCount ;
                reviewRowCount += count;
                reviewEndRow = reviewRowCount ;

                if (!commentEndReached && loadingReview) {
                    reviewLoadingRow = reviewRowCount++;
                }
            }

            if (reviewAdapter != null) {
                reviewAdapter.notifyDataSetChanged();
            }
        }else if(id == 2){
            aboutRowCount = 0;

            if (currentShop != null) {
                if (!ShopUtils.isEmpty(currentShop.city)) {
                    aboutCityRow = aboutRowCount++;
                }
                if (!ShopUtils.isEmpty(currentShop.address)) {
                    aboutAddressRow = aboutRowCount++;
                }
                if (!ShopUtils.isEmpty(currentShop.website)) {
                    aboutWebsiteRow = aboutRowCount++;
                }

                if (!ShopUtils.isEmpty(currentShop.description)) {
                    aboutDescRow = aboutRowCount++;
                }

            }
            if (aboutAdapter != null) {
                aboutAdapter.notifyDataSetChanged();
            }
        }
    }

    public void refresh(){
     // switchToCurrentSelectedMode(false);
    }

    public void setCurrentShop(ShopDataSerializer.Shop shop) {
        if (shop != null) {
            chat_id = ShopUtils.toClientChannelId(shop.channel_id);
        }
        currentShop = shop;
        updateRow(2);
    }

    public ShopMediaLayout(Context context, int chatId, BusinessProfileActivity parent) {
        super(context);
        profileActivity = parent;
        this.chat_id = chatId;
        actionBar = profileActivity.getActionBar();

        for (int a = 0; a < 10; a++) {
            cellCache.add(new ProductCell(context));
        }

        profileActivity.getNotificationCenter().addObserver(this, NotificationCenter.didProductLoaded);
        profileActivity.getNotificationCenter().addObserver(this, NotificationCenter.didReviewLoaded);
        profileActivity.getNotificationCenter().addObserver(this, NotificationCenter.didBusinessLoaded);
        profileActivity.getNotificationCenter().addObserver(this, NotificationCenter.didReviewSelfLoaded);
        profileActivity.getNotificationCenter().addObserver(this, NotificationCenter.didNewProductListed);

        profileActivity.getNotificationCenter().addObserver(this,NotificationCenter.didProductUpdated);





        ViewConfiguration configuration = ViewConfiguration.get(context);
        maximumVelocity = configuration.getScaledMaximumFlingVelocity();

        pinnedHeaderShadowDrawable = context.getResources().getDrawable(R.drawable.photos_header_shadow);
        pinnedHeaderShadowDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundGrayShadow), PorterDuff.Mode.MULTIPLY));


        scrollSlidingTextTabStrip = new ScrollSlidingTextTabStrip(context);
        scrollSlidingTextTabStrip.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        scrollSlidingTextTabStrip.setColors(Theme.key_profile_tabSelectedLine, Theme.key_profile_tabSelectedText, Theme.key_profile_tabText, Theme.key_profile_tabSelector);
        scrollSlidingTextTabStrip.setDelegate(new ScrollSlidingTextTabStrip.ScrollSlidingTabStripDelegate() {
            @Override
            public void onPageSelected(int id, boolean forward) {
                if (mediaPages[0].selectedType == id) {
                    return;
                }
                mediaPages[1].selectedType = id;
                mediaPages[1].setVisibility(View.VISIBLE);
                switchToCurrentSelectedMode(true);
                animatingForward = forward;
                onSelectedTabChanged();
            }

            @Override
            public void onSamePageSelected() {
                scrollToTop();
            }


            @Override
            public void onPageScrolled(float progress) {
                if (progress == 1 && mediaPages[1].getVisibility() != View.VISIBLE) {
                    return;
                }
                if (animatingForward) {
                    mediaPages[0].setTranslationX(-progress * mediaPages[0].getMeasuredWidth());
                    mediaPages[1].setTranslationX(mediaPages[0].getMeasuredWidth() - progress * mediaPages[0].getMeasuredWidth());
                } else {
                    mediaPages[0].setTranslationX(progress * mediaPages[0].getMeasuredWidth());
                    mediaPages[1].setTranslationX(progress * mediaPages[0].getMeasuredWidth() - mediaPages[0].getMeasuredWidth());
                }

                if (progress == 1) {
                    MediaPage tempPage = mediaPages[0];
                    mediaPages[0] = mediaPages[1];
                    mediaPages[1] = tempPage;
                    mediaPages[1].setVisibility(View.GONE);
                }

            }
        });

        productAdapter = new ProductAdapter(context);
        reviewAdapter = new ReviewAdapter(context);
        aboutAdapter = new AboutAdapter(context);

        setWillNotDraw(false);

        int scrollToPositionOnRecreate = -1;
        int scrollToOffsetOnRecreate = 0;

        for (int a = 0; a < mediaPages.length; a++) {
            if (a == 0) {
                if (mediaPages[a] != null && mediaPages[a].layoutManager != null) {
                    scrollToPositionOnRecreate = mediaPages[a].layoutManager.findFirstVisibleItemPosition();
                    if (scrollToPositionOnRecreate != mediaPages[a].layoutManager.getItemCount() - 1) {
                        RecyclerListView.Holder holder = (RecyclerListView.Holder) mediaPages[a].listView.findViewHolderForAdapterPosition(scrollToPositionOnRecreate);
                        if (holder != null) {
                            scrollToOffsetOnRecreate = holder.itemView.getTop();
                        } else {
                            scrollToPositionOnRecreate = -1;
                        }
                    } else {
                        scrollToPositionOnRecreate = -1;
                    }
                }
            }
            final MediaPage mediaPage = new MediaPage(context) {
                @Override
                public void setTranslationX(float translationX) {
                    super.setTranslationX(translationX);
                    if (tabsAnimationInProgress) {
                        if (mediaPages[0] == this) {
                            float scrollProgress = Math.abs(mediaPages[0].getTranslationX()) / (float) mediaPages[0].getMeasuredWidth();
                            scrollSlidingTextTabStrip.selectTabWithId(mediaPages[1].selectedType, scrollProgress);
                        }
                    }
                }
            };
            addView(mediaPage, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.TOP, 0, 48, 0, 0));
            mediaPages[a] = mediaPage;


            final GridLayoutManager layoutManager = mediaPages[a].layoutManager = new GridLayoutManager(context, 2);

            mediaPages[a].listView = new RecyclerListView(context) {
                @Override
                protected void onLayout(boolean changed, int l, int t, int r, int b) {
                    super.onLayout(changed, l, t, r, b);
                    checkLoadMoreScroll(mediaPage, mediaPage.listView, layoutManager);
                }

                @Override
                protected boolean allowSelectChildAtPosition(View child) {
                    if (child instanceof TagTextLayout) {
                        return false;
                    }
                    return super.allowSelectChildAtPosition(child);
                }
            };
            mediaPages[a].listView.setFocusable(true);
            mediaPages[a].listView.setFocusableInTouchMode(true);
            mediaPages[a].listView.setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING);
            mediaPages[a].listView.setPinnedSectionOffsetY(-AndroidUtilities.dp(2));
            mediaPages[a].listView.setPadding(0, AndroidUtilities.dp(2), 0, 0);
            mediaPages[a].listView.setItemAnimator(null);
            mediaPages[a].listView.setClipToPadding(false);
            mediaPages[a].listView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

            mediaPages[a].listView.setOnItemClickListener((view, position) -> {

                if (mediaPage.selectedType == 0) {
                    if (mediaPage.listView.getAdapter() instanceof ProductAdapter) {
                        ProductAdapter productAdapter = (ProductAdapter) mediaPage.listView.getAdapter();
                        ShopDataSerializer.Product product = productAdapter.getItem(position);
                        if (product != null) {
                            Bundle bundle = new Bundle();
                            bundle.putInt("chat_id", chat_id);
                            bundle.putInt("item_id", product.id);

                            ProductDetailFragment detailFragment = new ProductDetailFragment(bundle);
                            detailFragment.setCurrentShop(currentShop);
                            profileActivity.presentFragment(detailFragment);
                        }
                    }
                } else if (mediaPage.selectedType == 1) {


                } else if (mediaPage.selectedType == 2) {

                    if (mediaPage.listView.getAdapter() instanceof AboutAdapter) {
                        if (position == aboutWebsiteRow) {
                            Browser.openUrl(context, currentShop.website);
                        }
                    }
                }
            });

            mediaPages[a].listView.setLayoutManager(layoutManager);

            mediaPages[a].layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {

                    RecyclerView.Adapter adapter = mediaPage.listView.getAdapter();
                    if (adapter != null) {
                        if (adapter instanceof ProductAdapter) {
                            ProductAdapter productAdapter = (ProductAdapter) adapter;
                            if (productAdapter.getItemViewType(position) == 4) {
                                return 1;
                            } else {
                                return layoutManager.getSpanCount();
                            }
                        } else if (adapter instanceof ReviewAdapter) {
                            ReviewAdapter reviewAdapter = (ReviewAdapter) adapter;
                            return 2;
                        } else if (adapter instanceof AboutAdapter) {
                            AboutAdapter reviewAdapter = (AboutAdapter) adapter;
                            return 2;
                        }

                    }
                    return 0;
                }
            });


            mediaPages[a].listView.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(android.graphics.Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                    RecyclerListView.Holder holder = (RecyclerListView.Holder) parent.getChildViewHolder(view);
                    RecyclerView.Adapter adapter = mediaPage.listView.getAdapter();
                    if (adapter != null) {
                        if (adapter instanceof ProductAdapter) {
                            if (holder.getItemViewType() == 4) {
                                outRect.left = AndroidUtilities.dp(4);
                                outRect.right = AndroidUtilities.dp(4);
                                outRect.bottom = AndroidUtilities.dp(4);
                                outRect.top = AndroidUtilities.dp(4);
                            }
                        } else {
                            outRect.left = AndroidUtilities.dp(0);
                            outRect.right = AndroidUtilities.dp(0);
                            outRect.bottom = AndroidUtilities.dp(0);
                            outRect.top = AndroidUtilities.dp(0);
                        }
                    }

                }

            });

            mediaPages[a].listView.setPadding(AndroidUtilities.dp(8), 0, AndroidUtilities.dp(8), 0);
            mediaPages[a].addView(mediaPages[a].listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

            mediaPages[a].listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    checkLoadMoreScroll(mediaPage, recyclerView, layoutManager);
                }
            });
            if (a == 0 && scrollToPositionOnRecreate != -1) {
                layoutManager.scrollToPositionWithOffset(scrollToPositionOnRecreate, scrollToOffsetOnRecreate);
            }

            final RecyclerListView listView = mediaPages[a].listView;

            mediaPages[a].emptyView = new LinearLayout(context);
            mediaPages[a].emptyView.setWillNotDraw(false);
            mediaPages[a].emptyView.setOrientation(LinearLayout.VERTICAL);
            mediaPages[a].emptyView.setGravity(Gravity.CENTER);
            mediaPages[a].emptyView.setVisibility(View.GONE);
            mediaPages[a].addView(mediaPages[a].emptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

            mediaPages[a].emptyView.setOnTouchListener((v, event) -> true);

            mediaPages[a].emptyImageView = new ImageView(context);
            mediaPages[a].emptyView.addView(mediaPages[a].emptyImageView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

            mediaPages[a].emptyTextView = new TextView(context);
            mediaPages[a].emptyTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
            mediaPages[a].emptyTextView.setGravity(Gravity.CENTER);
            mediaPages[a].emptyTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
            mediaPages[a].emptyTextView.setPadding(AndroidUtilities.dp(40), 0, AndroidUtilities.dp(40), AndroidUtilities.dp(128));
            mediaPages[a].emptyView.addView(mediaPages[a].emptyTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 0, 24, 0, 0));

            mediaPages[a].progressView = new LinearLayout(context) {
                @Override
                protected void onDraw(Canvas canvas) {
                    backgroundPaint.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), backgroundPaint);
                }
            };
            mediaPages[a].progressView.setWillNotDraw(false);
            mediaPages[a].progressView.setGravity(Gravity.CENTER);
            mediaPages[a].progressView.setOrientation(LinearLayout.VERTICAL);
            mediaPages[a].progressView.setVisibility(View.GONE);
            mediaPages[a].addView(mediaPages[a].progressView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

            mediaPages[a].progressBar = new RadialProgressView(context);
            mediaPages[a].progressView.addView(mediaPages[a].progressBar, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

            if (a != 0) {
                mediaPages[a].setVisibility(View.GONE);
            }

            mediaPages[a].scrollHelper = new RecyclerAnimationScrollHelper(mediaPages[a].listView, mediaPages[a].layoutManager);
        }
        addView(scrollSlidingTextTabStrip, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.LEFT | Gravity.TOP));

        shadowLine = new View(context);
        shadowLine.setBackgroundColor(Theme.getColor(Theme.key_divider));
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
        layoutParams.topMargin = AndroidUtilities.dp(48) - 1;
        addView(shadowLine, layoutParams);

        updateTabs();

        switchToCurrentSelectedMode(false);
        ShopDataController.getInstance(UserConfig.selectedAccount).loadSectionConfiguration("product_types", true, parent.getClassGuid());
    }


    private void switchToCurrentSelectedMode(boolean animated){
        for (int a = 0; a < mediaPages.length; a++) {
            mediaPages[a].listView.stopScroll();
        }
        int a = animated ? 1 : 0;
        RecyclerView.Adapter currentAdapter = mediaPages[a].listView.getAdapter();
        mediaPages[a].emptyTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
        mediaPages[a].emptyImageView.setVisibility(View.VISIBLE);
        mediaPages[a].listView.setPinnedHeaderShadowDrawable(null);


        if (mediaPages[a].selectedType == 0) {

            if (currentAdapter != productAdapter) {
                mediaPages[a].listView.setAdapter(productAdapter);
            }

            mediaPages[a].listView.setPinnedHeaderShadowDrawable(pinnedHeaderShadowDrawable);
            mediaPages[a].emptyImageView.setImageResource(R.drawable.smiles_info);
            mediaPages[a].emptyTextView.setText(LocaleController.getString("ProductListingNotFound",R.string.EmptyProductListing));

            if (!isLoadingProduct && products.isEmpty()) {

                ShopDataController.getInstance(UserConfig.selectedAccount).loadProductsForShop(chat_id, currentSort, profileActivity.getClassGuid());
                mediaPages[a].progressView.setVisibility(View.VISIBLE);
                mediaPages[a].listView.setEmptyView(null);
                mediaPages[a].emptyView.setVisibility(View.GONE);
            } else {

                mediaPages[a].progressView.setVisibility(View.GONE);
                mediaPages[a].listView.setVisibility(VISIBLE);
                if(productAdapter != null){
                    productAdapter.notifyDataSetChanged();
                }
                mediaPages[a].listView.setEmptyView(mediaPages[a].emptyView);
            }

        } else if (mediaPages[a].selectedType == 1) {

            if (currentAdapter != reviewAdapter) {
                 mediaPages[a].listView.setAdapter(reviewAdapter);
            }

            mediaPages[a].emptyImageView.setImageResource(R.drawable.empety_review);
            mediaPages[a].emptyTextView.setText(LocaleController.getString("EmptyShopReview",R.string.EmptyShopReview));

            if (!loadingReview && reviews.isEmpty()) {
                if(userCanComment){
                    ShopDataController.getInstance(UserConfig.selectedAccount).loadShopReviewSelf(chat_id, profileActivity.getClassGuid());
                }
               // ShopDataController.getInstance(UserConfig.selectedAccount).loadShopReview(false, chat_id, currentReviewSort, profileActivity.getClassGuid());
                ShopDataController.getInstance(UserConfig.selectedAccount).loadShopReviews(chat_id, currentReviewSort, nextComment,profileActivity.getClassGuid());

                mediaPages[a].progressView.setVisibility(View.VISIBLE);
                mediaPages[a].listView.setEmptyView(null);
                mediaPages[a].emptyView.setVisibility(View.GONE);
            } else {
                mediaPages[a].progressView.setVisibility(View.GONE);
                mediaPages[a].listView.setEmptyView(mediaPages[a].emptyView);
            }


        } else if (mediaPages[a].selectedType == 2) {

            if (currentAdapter != aboutAdapter) {
                mediaPages[a].listView.setAdapter(aboutAdapter);
            }
            mediaPages[a].progressView.setVisibility(View.GONE);
            mediaPages[a].listView.setEmptyView(mediaPages[a].emptyView);
        }

        mediaPages[a].emptyTextView.setPadding(AndroidUtilities.dp(40), 0, AndroidUtilities.dp(40), AndroidUtilities.dp(128));
        mediaPages[a].listView.setVisibility(View.VISIBLE);
        updateRow(mediaPages[a].selectedType);


    }


    private void updateProductItm(int pos, ShopDataSerializer.Product product){
        //updateRow(0);
    }


    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if(id == NotificationCenter.didNewProductListed){
            if(args[0] instanceof ShopDataSerializer.Product){
                ShopDataSerializer.Product product =(ShopDataSerializer.Product) args[0];
                products.add(0,product);
                updateRow(0);
                if(productAdapter != null){
                    productAdapter.notifyItemInserted(0);
                }
            }
        }else  if(id == NotificationCenter.didProductUpdated){
            boolean updated = (Boolean)args[0];
            if(updated){
                ShopDataSerializer.Product updatedProduct =(ShopDataSerializer.Product) args[1];
                ShopDataSerializer.Product toBeUpdatedProduct = null;
                int index = -1;
                for (int a = 0, N = products.size(); a < N; a++) {
                    if(products.get(a).id == updatedProduct.id){
                        index = a;
                        break;
                    }
                }

                if(toBeUpdatedProduct != null){

                    if(index >= 0 && index < products.size()){

                        updateProductItm(index,updatedProduct);
                        //Log.i("producttobe","set index = " + index);
                        //products.set(index,updatedProduct);
                    }
//                    if(productAdapter != null){
//                            productAdapter.notifyItemChanged(index + productStartRow);
//                                        //  updateRow(0);
//                    }
                }

            }

        }if (id == NotificationCenter.didProductLoaded) {
            boolean loaded = (boolean) args[0];
            int classGuid = (int) args[2];
            if (classGuid == profileActivity.getClassGuid()) {
                isLoadingProduct = false;
                if (loaded) {
                    ArrayList<ShopDataSerializer.Product> productArrayList = (ArrayList<ShopDataSerializer.Product>) args[1];
                    nextLoadProduct = (String) args[3];
                    if (ShopUtils.isEmpty(nextLoadProduct)) {
                        productEndReached = true;
                    } else {
                        productEndReached = false;
                    }
                    products.addAll(productArrayList);
                    if (products.isEmpty() && productEndReached) {
                        showEmpty = true;
                    } else {
                        showEmpty = false;
                    }
                    if (productAdapter != null) {
                        for (int a = 0; a < mediaPages.length; a++) {
                            if (mediaPages[a].listView.getAdapter() == productAdapter) {
                                mediaPages[a].listView.stopScroll();
                            }
                        }
                    }
                    productCount = (int) args[4];
                    updateRow(0);
                } else {
                    APIError apiError = (APIError) args[1];
                    if (products.isEmpty()) {
                        showEmpty = true;
                        productCount = 0;
                        if (productAdapter != null) {
                            for (int a = 0; a < mediaPages.length; a++) {
                                if (mediaPages[a].listView.getAdapter() == productAdapter) {
                                    mediaPages[a].listView.stopScroll();
                                }
                            }
                        }
                    }

                }

                for (int a = 0; a < mediaPages.length; a++) {
                    if (mediaPages[a].selectedType == 0) {
                        if (!isLoadingProduct) {
                            if (mediaPages[a].progressView != null) {
                                mediaPages[a].progressView.setVisibility(View.GONE);
                            }
                            if (mediaPages[a].listView != null) {
                                if (mediaPages[a].listView.getEmptyView() == null) {
                                    mediaPages[a].listView.setEmptyView(mediaPages[a].emptyView);
                                }
                            }
                        }
                    }
                }
            }

        } else if (id == NotificationCenter.didConfigrationLoaded) {
            boolean loaded = (boolean) args[0];
            String sec = (String) args[1];
            if (loaded && sec.equals("product_types")) {
                businesses = (ArrayList<ShopDataSerializer.ProductType>) args[1];
                if(!businesses.isEmpty()){
                    selected_business = businesses.get(0).key;
                }
               updateRow(0);
            }
        } else if (id == NotificationCenter.didReviewLoaded) {
            boolean loaded = (boolean) args[0];
            if (loaded) {
                commentTotalCount = (int) args[3];
                nextComment = (String) args[2];
                loadingReview = false;
                commentEndReached = ShopUtils.isEmpty(nextComment);
                ArrayList<ShopDataSerializer.Review> productArrayList = (ArrayList<ShopDataSerializer.Review>) args[1];
                reviews.addAll(productArrayList);
                if (reviewAdapter != null) {
                    for (int a = 0; a < mediaPages.length; a++) {
                        if (mediaPages[a].listView.getAdapter() == reviewAdapter) {
                            mediaPages[a].listView.stopScroll();
                        }
                    }
                }
                updateRow(1);
            }

            if (!shopReviewSelfLoaded) {
               // ShopDataController.getInstance(UserConfig.selectedAccount).loadShopReviewSelf(chat_id,profileActivity.getClassGuid());
            }

            for (int a = 0; a < mediaPages.length; a++) {
                if (mediaPages[a].selectedType == 1) {
                    if (!loadingReview) {
                        if (mediaPages[a].progressView != null) {
                            mediaPages[a].progressView.setVisibility(View.GONE);
                        }
                        if (mediaPages[a].listView != null) {
                            if (mediaPages[a].listView.getEmptyView() == null) {
                                mediaPages[a].listView.setEmptyView(mediaPages[a].emptyView);
                            }
                        }
                    }
                }
            }
        } else if (id == NotificationCenter.didReviewSelfLoaded) {
            boolean loaded = (boolean) args[0];
            if (loaded) {
                shopReviewSelfLoaded = true;
                ArrayList<ShopDataSerializer.Review> reviewArrayList = (ArrayList<ShopDataSerializer.Review>) args[1];
                //userCanComment = (currentShop.created_by != null && currentShop.created_by.telegramId != profileActivity.getUserConfig().clientUserId) && reviewArrayList.isEmpty();
                userCanComment = reviewArrayList.isEmpty();
                userCanComment = currentShop.can_review;

                if (!reviewArrayList.isEmpty()) {
                    userHasCommented = true;
                    currentUserReview = reviewArrayList.get(0);
                }

                updateRow(1);
            }

        }

    }

    private void loadProducts(){
        products.clear();
        isLoadingProduct = true;
        productEndReached = false;
        for (int a = 0; a < mediaPages.length; a++) {
            if (mediaPages[a].selectedType == 0) {
                mediaPages[a].progressView.setVisibility(View.VISIBLE);
                mediaPages[a].listView.setEmptyView(null);
                mediaPages[a].emptyView.setVisibility(View.VISIBLE);
            }
        }
        currentFilter.put("ordering",ShopUtils.formatSort(currentSort));
        if(!TextUtils.isEmpty(current_search)){
            currentFilter.put("search",current_search);
        }
        ShopDataController.getInstance(UserConfig.selectedAccount).loadProducts(chat_id, selected_business,currentFilter, profileActivity.getClassGuid());
    }


    private class ProductAdapter extends RecyclerListView.SelectionAdapter {

        private Context mContext;

        public ProductAdapter(Context context) {
            mContext = context;
        }


        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int viewType = holder.getItemViewType();
            return viewType == 4;
        }


        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 1:
                    TitleCell ui_info_bold_cell = new TitleCell(mContext);
                    view = ui_info_bold_cell;
                    ui_info_bold_cell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 33:
                    TagTextLayout tagTextLayout = new TagTextLayout(mContext);
                    view = tagTextLayout;
                    tagTextLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    ArrayList<TagTextLayout.TagText> tagTexts = new ArrayList<>();
                    int count = businesses.size();
                    for (int a = 0, N = count; a < N; a++) {
                        ShopDataSerializer.ProductType business = businesses.get(a);
                        if (business == null) {
                            a--;
                            N--;
                            continue;
                        }
                        tagTexts.add(new TagTextLayout.TagText(business.toString(), business.display_name));
                    }
                    tagTextLayout.setDelegate((view1, position, selected) -> {
                        selected_business = selected.key;
                        current_search = "";
                        loadProducts();
                    });
                    tagTextLayout.setTagTexts(tagTexts);
                    break;
                case 4:
                    if (!cellCache.isEmpty()) {
                        view = cellCache.get(0);
                        cellCache.remove(0);
                        ViewGroup p = (ViewGroup) view.getParent();
                        if (p != null) {
                            p.removeView(view);
                        }
                    } else {
                        view = new ProductCell(mContext);
                    }
                    cache.add((ProductCell) view);
                    break;
                case 2:
                    searchField = new FakeSearchParent(mContext);
                    searchField.setDelegate(search ->  {
                        current_search = search;
                        loadProducts();
                    });
                    view = searchField;
                    break;
                case 3:
                    FilterHorizontalLayout filterHorizontalLayout = new FilterHorizontalLayout(mContext);
                    view = filterHorizontalLayout;
                    filterHorizontalLayout.onFilterClick(v -> {
                        searchField.hideKeyboard();
                        BusinessAlert businessAlert = new BusinessAlert(mContext, false);
                        businessAlert.setDelegate(business -> {
                            selected_business = business.key;
                            ProductFilterActivity filterActivity = new ProductFilterActivity(selected_business);
                            filterActivity.setFilterDelegate(new ProductFilterActivity.ProductFilterDelegate() {
                                @Override
                                public void onFilterSelected(HashMap<String, Object> filterMap) {
                                    currentFilter = filterMap;
                                    current_search = "";
                                    loadProducts();
                                }
                            });
                            profileActivity.presentFragment(filterActivity);
                            filterHorizontalLayout.setText(business.display_name);
                        });
                        profileActivity.showDialog(businessAlert);

                    });
                    filterHorizontalLayout.onSortClick(v -> {
                        searchField.hideKeyboard();
                        ProductSortAlert productSortAlert = new ProductSortAlert(getContext(), false, selected_business, false, currentSort);
                        productSortAlert.setDelegate(sort -> {
                            if (sort != null) {
                                currentSort = sort;
                                current_search = "";
                                loadProducts();

                            }
                        });
                        profileActivity.showDialog(productSortAlert);
                    });
                    break;
                case 53:
                    EmptyTextProgressView emptyTextProgressView = new EmptyTextProgressView(mContext);
                    emptyTextProgressView.setTopImage(R.drawable.files_empty);
                    emptyTextProgressView.setText(LocaleController.getString("NoResultForFitler",R.string.NoResultForFitler));
                    view = emptyTextProgressView;
                    emptyTextProgressView.setShowAtTop(true);
                    emptyTextProgressView.showTextView();
                    view.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    break;
                case 5:
                    ShopsEmptyCell dialogsEmptyCell = new ShopsEmptyCell(mContext);
                    dialogsEmptyCell.setType(4);
                    view = dialogsEmptyCell;
                    break;
                case 6:
                    view = new LoadingCell(mContext, AndroidUtilities.dp(32), AndroidUtilities.dp(74));
                    break;
                default:
                    view = new EmptyCell(mContext,0);
                    break;
            }


            if (viewType != 5 && viewType != 7)
                view.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        public ShopDataSerializer.Product getItem(int position) {
            int newPos = position - productStartRow;
            if (newPos < 0 || newPos >= products.size()) {
                return null;
            }
            return products.get(newPos);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder.getItemViewType() == 4) {
                ProductCell cell = (ProductCell) holder.itemView;
                ShopDataSerializer.Product product = getItem(position);
                if(product != null){
                    cell.setProduct(product);
                    cell.setDelegate(new ProductCell.ProductDelegate() {
                        @Override
                        public void onFavSelected(long chat_id, int product_id, boolean favorite) {
                            AndroidUtilities.runOnUIThread(new Runnable() {
                                @Override
                                public void run() {
                                    ShopDataController.getInstance(UserConfig.selectedAccount).checkFav(favorite, chat_id, product_id, new ShopDataController.BooleanCallBack() {
                                        @Override
                                        public void onResponse(boolean susscess) {
                                            if(susscess){
                                                cell.setFavorite(!product.is_favorite);
                                            }
                                        }
                                    });
                                }
                            },300);
                        }

                    });
                }

            } else if (holder.getItemViewType() == 1) {
                TitleCell ui_info_bold_cell = (TitleCell) holder.itemView;
                if (position == listingNumberRow) {
                    ui_info_bold_cell.setText(productCount + " Listing");
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == listingNumberRow) {
                return 1;
            } else if (position == searchRow) {
                return 2;
            } else if (position == filterRow) {
                return 3;
            } else if (position >= productStartRow && position < productEndRow) {
                return 4;
            } else if (position == emptyLayoutRow) {
                return 5;
            } else if (position == progressRow) {
                return 6;
            }
            return super.getItemViewType(position);
        }


        @Override
        public int getItemCount() {
            return productRowCount;
        }
    }
    private class ReviewAdapter extends RecyclerListView.SelectionAdapter {

        private Context mContext;

        public ReviewAdapter(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return holder.getItemViewType() == 4 || holder.getItemViewType() == 3;
        }


        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view;
            switch (viewType) {
                case 5:
                    view = new EditUserReviewCell(mContext);
                    break;
                case 2:
                    view = new Ui_info_bold_cell(mContext);
                    break;
                case 0:
                    view = new ShopReviewCell(mContext);
                    break;
                case 3:
                    ShopReviewInputCell shopReviewInputCell = new ShopReviewInputCell(mContext);
                    view = shopReviewInputCell;
                    shopReviewInputCell.setReviewDelegate(count -> {
                        StoreReviewFragment storeReviewFragment = new StoreReviewFragment(currentShop, count, chat_id, "", new StoreReviewFragment.ShopReviewDelegate() {
                            @Override
                            public void reviewPosted(String comment, int rate) {

                            }
                        });
                        profileActivity.presentFragment(storeReviewFragment);

                    });
                    break;
                case 4:
                    view = new ReviewSortCell(mContext);
                    break;
                case 1:
                default:
                    view = new LoadingCell(mContext);
            }


            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder.getItemViewType() == 0) {
                ShopReviewCell reviewCell = (ShopReviewCell) holder.itemView;
                reviewCell.setReview(reviews.get(position - reviewStartRow), true);
            } else if (holder.getItemViewType() == 2) {
                Ui_info_bold_cell ui_info_bold_cell = (Ui_info_bold_cell) holder.itemView;
                ui_info_bold_cell.setText(LocaleController.formatPluralString("Reviews_many",commentTotalCount));
            } else if (holder.getItemViewType() == 4) {
                ReviewSortCell reviewSortCell = (ReviewSortCell) holder.itemView;
                reviewSortCell.setTextAndIcon(currentReviewSort == null ? LocaleController.getString("SortbyRecent",R.string.SortbyRecent) : currentReviewSort.label, R.drawable.contacts_sort_time);
                reviewSortCell.setONSortClicked(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ProductSortAlert productSortAlert = new ProductSortAlert(getContext(), false, selected_business, true, currentReviewSort);
                        productSortAlert.setDelegate(new ProductSortAlert.SortAlertDelegate() {
                            @Override
                            public void didSortSelected(ShopDataSerializer.ProductType.Sort sort) {
                                if (sort != null) {
                                    currentReviewSort = sort;
                                    reviewSortCell.setTextAndIcon(sort.label, R.drawable.contacts_sort_time);
                                }
                            }
                        });
                        profileActivity.showDialog(productSortAlert);
                    }
                });
            } else if (holder.getItemViewType() == 5) {
                EditUserReviewCell editUserReviewCell = (EditUserReviewCell) holder.itemView;
                editUserReviewCell.setData(currentUserReview);
                editUserReviewCell.setEditUserReviewCellDelegate(new EditUserReviewCell.EditUserReviewCellDelegate() {
                    @Override
                    public void showDeleteOption(ShopDataSerializer.Review review) {


                    }

                    @Override
                    public void showEditReview(ShopDataSerializer.Review review) {

                        StoreReviewFragment storeReviewFragment = new StoreReviewFragment(currentShop, review.rating, chat_id, review.comment, new StoreReviewFragment.ShopReviewDelegate() {
                            @Override
                            public void reviewPosted(String comment, int rate) {

                            }
                        });
                        profileActivity.presentFragment(storeReviewFragment);

                    }
                });
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == reviewHeaderRow) {
                return 2;
            }
            if (position == reviewSortRow) {
                return 4;
            } else if (position == reviewActionRow) {
                return 3;
            } else if (position >= reviewStartRow && position < reviewEndRow) {
                return 0;
            } else if (position == reviewLoadingRow) {
                return 1;
            } else if (position == userReviewRow) {
                return 5;
            }
            return super.getItemViewType(position);
        }


        @Override
        public int getItemCount() {
            return reviewRowCount;
        }

    }
    private class AboutAdapter extends RecyclerListView.SelectionAdapter {

        private Context mContext;

        public AboutAdapter(Context context) {
            mContext = context;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return holder.getAdapterPosition() == aboutWebsiteRow;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            switch (viewType){
                case 1:
                default:
                    view = new TextDetailSettingsCell(mContext);
                    break;
            }
            return new RecyclerListView.Holder(view);
        }


        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if(holder.getItemViewType() == 1){
                TextDetailSettingsCell detailSettingsCell = (TextDetailSettingsCell) holder.itemView;
                detailSettingsCell.setMultilineDetail(true);
                if (position == aboutCityRow) {
                    detailSettingsCell.setTextAndValueAndIcon(LocaleController.getString("City",R.string.City), currentShop.city, R.drawable.ic_city, true);
                } else if (position == aboutAddressRow) {
                    detailSettingsCell.setTextAndValueAndIcon(LocaleController.getString("Address",R.string.Address), currentShop.address, R.drawable.menu_location, true);
                } else if (position == aboutWebsiteRow) {
                    detailSettingsCell.setTextAndValueAndIcon(LocaleController.getString("Website",R.string.Website), currentShop.website, R.drawable.msg_link, true);
                } else if (position == aboutDescRow) {
                    detailSettingsCell.setTextAndValueAndIcon(LocaleController.getString("Description",R.string.Description), currentShop.description, R.drawable.menu_info, true);
                }
            }

        }

        @Override
        public int getItemViewType(int position) {
            if(position == aboutCityRow || position == aboutAddressRow || position == aboutWebsiteRow || position
             == aboutDescRow){
                return 1;
            }
            return super.getItemViewType(position);
        }

        @Override
        public int getItemCount() {
            return aboutRowCount;
        }
    }


    private static class ReviewSortCell extends FrameLayout {

        private TextView textView;
        private RectF rect = new RectF();

        public ReviewSortCell(@NonNull Context context) {
            super(context);

            textView = new TextView(context);
            textView.setTextColor(Theme.getColor(Theme.key_chats_menuItemText));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            textView.setLines(1);
            textView.setMaxLines(1);
            textView.setSingleLine(true);
            textView.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            textView.setCompoundDrawablePadding(AndroidUtilities.dp(8));
            addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.TOP, 19, 0, 16, 0));

        }

        public void setONSortClicked(OnClickListener onSortClicked) {
            textView.setOnClickListener(onSortClicked);
        }


        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(48), MeasureSpec.EXACTLY));
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            textView.setTextColor(Theme.getColor(Theme.key_chats_menuItemText));
        }


        public void setTextAndIcon(String text, int resId) {
            try {
                textView.setText(text);
                Drawable drawable = getResources().getDrawable(resId).mutate();
                if (drawable != null) {
                    drawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chats_menuItemIcon), PorterDuff.Mode.MULTIPLY));
                }
                textView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
            } catch (Throwable e) {
                FileLog.e(e);
            }
        }
    }

    private void scrollToTop() {
        int height;
        switch (mediaPages[0].selectedType) {
            case 0:
                height = SharedPhotoVideoCell.getItemSize(columnsCount);
                break;
            default:
                height = AndroidUtilities.dp(58);
                break;
        }
        int scrollDistance = mediaPages[0].layoutManager.findFirstVisibleItemPosition() * height;
        if (scrollDistance >= mediaPages[0].listView.getMeasuredHeight() * 1.2f) {
            mediaPages[0].scrollHelper.setScrollDirection(RecyclerAnimationScrollHelper.SCROLL_DIRECTION_UP);
            mediaPages[0].scrollHelper.scrollToPosition(0, 0, false, true);
        } else {
            mediaPages[0].listView.smoothScrollToPosition(0);
        }
    }

    private void checkLoadMoreScroll(MediaPage mediaPage, RecyclerView recyclerView, LinearLayoutManager layoutManager) {
        if (mediaPage.selectedType == 2 || recyclerView.getAdapter() == null) {
            return;
        }
        int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
        int visibleItemCount = firstVisibleItem == RecyclerView.NO_POSITION ? 0 : Math.abs(layoutManager.findLastVisibleItemPosition() - firstVisibleItem) + 1;
        int totalItemCount = recyclerView.getAdapter().getItemCount();
        final int threshold;
        if (mediaPage.selectedType == 0) {
            threshold = 3;
        } else {
            threshold = 6;
        }
        if (mediaPage.selectedType == 0) {
            if (firstVisibleItem + visibleItemCount > totalItemCount - threshold && !isLoadingProduct) {

                if (!productEndReached) {
                    isLoadingProduct = true;
                    ShopDataController.getInstance(UserConfig.selectedAccount).loadMoreProductForShop(nextLoadProduct, profileActivity.getClassGuid());
                }
            }

        } else if (mediaPage.selectedType == 1) {
            if (firstVisibleItem + visibleItemCount > totalItemCount - threshold && !loadingReview) {
                if (!commentEndReached) {
                    loadingReview = true;
                    ShopDataController.getInstance(UserConfig.selectedAccount).loadMoreReviewForShop(nextComment, profileActivity.getClassGuid());
                }
            }
        }
    }

    public int getSelectedTab() {
        return scrollSlidingTextTabStrip.getCurrentTabId();
    }

    public void onDestroy() {
        profileActivity.getNotificationCenter().removeObserver(this, NotificationCenter.didProductLoaded);
        profileActivity.getNotificationCenter().removeObserver(this, NotificationCenter.didReviewLoaded);
        profileActivity.getNotificationCenter().removeObserver(this, NotificationCenter.didBusinessLoaded);
        profileActivity.getNotificationCenter().removeObserver(this, NotificationCenter.didReviewSelfLoaded);
        profileActivity.getNotificationCenter().removeObserver(this, NotificationCenter.didNewProductListed);
        profileActivity.getNotificationCenter().removeObserver(this, NotificationCenter.didProductUpdated);



    }

    private boolean prepareForMoving(MotionEvent ev, boolean forward) {
        int id = scrollSlidingTextTabStrip.getNextPageId(forward);
        if (id < 0) {
            return false;
        }

        getParent().requestDisallowInterceptTouchEvent(true);
        maybeStartTracking = false;
        startedTracking = true;
        startedTrackingX = (int) ev.getX();
        actionBar.setEnabled(false);
        scrollSlidingTextTabStrip.setEnabled(false);
        mediaPages[1].selectedType = id;
        mediaPages[1].setVisibility(View.VISIBLE);
        animatingForward = forward;
        switchToCurrentSelectedMode(true);
        if (forward) {
            mediaPages[1].setTranslationX(mediaPages[0].getMeasuredWidth());
        } else {
            mediaPages[1].setTranslationX(-mediaPages[0].getMeasuredWidth());
        }
        return true;
    }

    @Override
    public void forceHasOverlappingRendering(boolean hasOverlappingRendering) {
        super.forceHasOverlappingRendering(hasOverlappingRendering);
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        for (int a = 0; a < mediaPages.length; a++) {
            mediaPages[a].setTranslationY(top);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = profileActivity.getListView().getHeight();
        if (heightSize == 0) {
            heightSize = MeasureSpec.getSize(heightMeasureSpec);
        }

        setMeasuredDimension(widthSize, heightSize);

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child == null || child.getVisibility() == GONE) {
                continue;
            }
            if (child instanceof MediaPage) {
                measureChildWithMargins(child, widthMeasureSpec, 0, MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY), 0);
            } else {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            }
        }
    }

    public boolean checkTabsAnimationInProgress() {
        if (tabsAnimationInProgress) {
            boolean cancel = false;
            if (backAnimation) {
                if (Math.abs(mediaPages[0].getTranslationX()) < 1) {
                    mediaPages[0].setTranslationX(0);
                    mediaPages[1].setTranslationX(mediaPages[0].getMeasuredWidth() * (animatingForward ? 1 : -1));
                    cancel = true;
                }
            } else if (Math.abs(mediaPages[1].getTranslationX()) < 1) {
                mediaPages[0].setTranslationX(mediaPages[0].getMeasuredWidth() * (animatingForward ? -1 : 1));
                mediaPages[1].setTranslationX(0);
                cancel = true;
            }
            if (cancel) {
                if (tabsAnimation != null) {
                    tabsAnimation.cancel();
                    tabsAnimation = null;
                }
                tabsAnimationInProgress = false;
            }
            return tabsAnimationInProgress;
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return checkTabsAnimationInProgress() || scrollSlidingTextTabStrip.isAnimatingIndicator() || onTouchEvent(ev);
    }

    public boolean isCurrentTabFirst() {
        return scrollSlidingTextTabStrip.getCurrentTabId() == scrollSlidingTextTabStrip.getFirstTabId();
    }

    public RecyclerListView getCurrentListView() {
        return mediaPages[0].listView;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (profileActivity.getParentLayout() != null && !profileActivity.getParentLayout().checkTransitionAnimation() && !checkTabsAnimationInProgress()) {
            if (ev != null) {
                if (velocityTracker == null) {
                    velocityTracker = VelocityTracker.obtain();
                }
                velocityTracker.addMovement(ev);
            }
            if (ev != null && ev.getAction() == MotionEvent.ACTION_DOWN && !startedTracking && !maybeStartTracking && ev.getY() >= AndroidUtilities.dp(48)) {
                startedTrackingPointerId = ev.getPointerId(0);
                maybeStartTracking = true;
                startedTrackingX = (int) ev.getX();
                startedTrackingY = (int) ev.getY();
                velocityTracker.clear();
            } else if (ev != null && ev.getAction() == MotionEvent.ACTION_MOVE && ev.getPointerId(0) == startedTrackingPointerId) {
                int dx = (int) (ev.getX() - startedTrackingX);
                int dy = Math.abs((int) ev.getY() - startedTrackingY);
                if (startedTracking && (animatingForward && dx > 0 || !animatingForward && dx < 0)) {
                    if (!prepareForMoving(ev, dx < 0)) {
                        maybeStartTracking = true;
                        startedTracking = false;
                        mediaPages[0].setTranslationX(0);
                        mediaPages[1].setTranslationX(animatingForward ? mediaPages[0].getMeasuredWidth() : -mediaPages[0].getMeasuredWidth());
                        scrollSlidingTextTabStrip.selectTabWithId(mediaPages[1].selectedType, 0);
                    }
                }
                if (maybeStartTracking && !startedTracking) {
                    float touchSlop = AndroidUtilities.getPixelsInCM(0.3f, true);
                    if (Math.abs(dx) >= touchSlop && Math.abs(dx) > dy) {
                        prepareForMoving(ev, dx < 0);
                    }
                } else if (startedTracking) {
                    mediaPages[0].setTranslationX(dx);
                    if (animatingForward) {
                        mediaPages[1].setTranslationX(mediaPages[0].getMeasuredWidth() + dx);
                    } else {
                        mediaPages[1].setTranslationX(dx - mediaPages[0].getMeasuredWidth());
                    }
                    float scrollProgress = Math.abs(dx) / (float) mediaPages[0].getMeasuredWidth();

                    scrollSlidingTextTabStrip.selectTabWithId(mediaPages[1].selectedType, scrollProgress);
                }
            } else if (ev == null || ev.getPointerId(0) == startedTrackingPointerId && (ev.getAction() == MotionEvent.ACTION_CANCEL || ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_POINTER_UP)) {
                velocityTracker.computeCurrentVelocity(1000, maximumVelocity);
                float velX;
                float velY;
                if (ev != null && ev.getAction() != MotionEvent.ACTION_CANCEL) {
                    velX = velocityTracker.getXVelocity();
                    velY = velocityTracker.getYVelocity();
                    if (!startedTracking) {
                        if (Math.abs(velX) >= 3000 && Math.abs(velX) > Math.abs(velY)) {
                            prepareForMoving(ev, velX < 0);
                        }
                    }
                } else {
                    velX = 0;
                    velY = 0;
                }
                if (startedTracking) {
                    float x = mediaPages[0].getX();
                    tabsAnimation = new AnimatorSet();
                    backAnimation = Math.abs(x) < mediaPages[0].getMeasuredWidth() / 3.0f && (Math.abs(velX) < 3500 || Math.abs(velX) < Math.abs(velY));
                    float distToMove;
                    float dx;
                    if (backAnimation) {
                        dx = Math.abs(x);
                        if (animatingForward) {
                            tabsAnimation.playTogether(
                                    ObjectAnimator.ofFloat(mediaPages[0], View.TRANSLATION_X, 0),
                                    ObjectAnimator.ofFloat(mediaPages[1], View.TRANSLATION_X, mediaPages[1].getMeasuredWidth())
                            );
                        } else {
                            tabsAnimation.playTogether(
                                    ObjectAnimator.ofFloat(mediaPages[0], View.TRANSLATION_X, 0),
                                    ObjectAnimator.ofFloat(mediaPages[1], View.TRANSLATION_X, -mediaPages[1].getMeasuredWidth())
                            );
                        }
                    } else {
                        dx = mediaPages[0].getMeasuredWidth() - Math.abs(x);
                        if (animatingForward) {
                            tabsAnimation.playTogether(
                                    ObjectAnimator.ofFloat(mediaPages[0], View.TRANSLATION_X, -mediaPages[0].getMeasuredWidth()),
                                    ObjectAnimator.ofFloat(mediaPages[1], View.TRANSLATION_X, 0)
                            );
                        } else {
                            tabsAnimation.playTogether(
                                    ObjectAnimator.ofFloat(mediaPages[0], View.TRANSLATION_X, mediaPages[0].getMeasuredWidth()),
                                    ObjectAnimator.ofFloat(mediaPages[1], View.TRANSLATION_X, 0)
                            );
                        }
                    }
                    tabsAnimation.setInterpolator(interpolator);

                    int width = getMeasuredWidth();
                    int halfWidth = width / 2;
                    float distanceRatio = Math.min(1.0f, 1.0f * dx / (float) width);
                    float distance = (float) halfWidth + (float) halfWidth * AndroidUtilities.distanceInfluenceForSnapDuration(distanceRatio);
                    velX = Math.abs(velX);
                    int duration;
                    if (velX > 0) {
                        duration = 4 * Math.round(1000.0f * Math.abs(distance / velX));
                    } else {
                        float pageDelta = dx / getMeasuredWidth();
                        duration = (int) ((pageDelta + 1.0f) * 100.0f);
                    }
                    duration = Math.max(150, Math.min(duration, 600));

                    tabsAnimation.setDuration(duration);
                    tabsAnimation.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animator) {
                            tabsAnimation = null;
                            if (backAnimation) {
                                mediaPages[1].setVisibility(View.GONE);

                            } else {
                                MediaPage tempPage = mediaPages[0];
                                mediaPages[0] = mediaPages[1];
                                mediaPages[1] = tempPage;
                                mediaPages[1].setVisibility(View.GONE);

                                scrollSlidingTextTabStrip.selectTabWithId(mediaPages[0].selectedType, 1.0f);
                                onSelectedTabChanged();
                            }
                            tabsAnimationInProgress = false;
                            maybeStartTracking = false;
                            startedTracking = false;
                            actionBar.setEnabled(true);
                            scrollSlidingTextTabStrip.setEnabled(true);
                        }
                    });
                    tabsAnimation.start();
                    tabsAnimationInProgress = true;
                    startedTracking = false;
                } else {
                    maybeStartTracking = false;
                    actionBar.setEnabled(true);
                    scrollSlidingTextTabStrip.setEnabled(true);
                }
                if (velocityTracker != null) {
                    velocityTracker.recycle();
                    velocityTracker = null;
                }
            }
            return startedTracking;
        }
        return false;
    }

    public void setVisibleHeight(int height) {
        height = Math.max(height, AndroidUtilities.dp(120));
        for (int a = 0; a < mediaPages.length; a++) {
            mediaPages[a].emptyView.setTranslationY(-(getMeasuredHeight() - height) / 2);
            mediaPages[a].progressView.setTranslationY(-(getMeasuredHeight() - height) / 2);
        }
    }

    public void onResume() {
        if (productAdapter != null) {
            productAdapter.notifyDataSetChanged();
        }
        if (reviewAdapter != null) {
            reviewAdapter.notifyDataSetChanged();
        }
        if (aboutAdapter != null) {
            aboutAdapter.notifyDataSetChanged();
        }

        for (int a = 0; a < mediaPages.length; a++) {
            fixLayoutInternal(a);
        }
    }

    protected void onSelectedTabChanged() {

    }

    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        for (int a = 0; a < mediaPages.length; a++) {
            if (mediaPages[a].listView != null) {
                final int num = a;
                ViewTreeObserver obs = mediaPages[a].listView.getViewTreeObserver();
                obs.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        mediaPages[num].getViewTreeObserver().removeOnPreDrawListener(this);
                        fixLayoutInternal(num);
                        return true;
                    }
                });
            }
        }
    }

    private void updateTabs()  {
        if (scrollSlidingTextTabStrip == null) {
            return;
        }
        if (!scrollSlidingTextTabStrip.hasTab(0)) {
            scrollSlidingTextTabStrip.addTextTab(0, LocaleController.getString("Listing",R.string.Listing));
        }

        if (!scrollSlidingTextTabStrip.hasTab(1)) {
            scrollSlidingTextTabStrip.addTextTab(1, LocaleController.getString("Review",R.string.Review));
        }

        if (!scrollSlidingTextTabStrip.hasTab(2)) {
            scrollSlidingTextTabStrip.addTextTab(2, LocaleController.getString("About",R.string.About));
        }

        int id = scrollSlidingTextTabStrip.getCurrentTabId();
        if (id >= 0) {
            mediaPages[0].selectedType = id;
        }
        scrollSlidingTextTabStrip.finishAddingTabs();
    }

    private void fixLayoutInternal(int num) {
        WindowManager manager = (WindowManager) ApplicationLoader.applicationContext.getSystemService(Activity.WINDOW_SERVICE);
        int rotation = manager.getDefaultDisplay().getRotation();

        if (AndroidUtilities.isTablet()) {
            columnsCount = 3;
            mediaPages[num].emptyTextView.setPadding(AndroidUtilities.dp(40), 0, AndroidUtilities.dp(40), AndroidUtilities.dp(128));
        } else {
            if (rotation == Surface.ROTATION_270 || rotation == Surface.ROTATION_90) {
                columnsCount = 6;
                mediaPages[num].emptyTextView.setPadding(AndroidUtilities.dp(40), 0, AndroidUtilities.dp(40), 0);
            } else {
                columnsCount = 3;
                mediaPages[num].emptyTextView.setPadding(AndroidUtilities.dp(40), 0, AndroidUtilities.dp(40), AndroidUtilities.dp(128));
            }
        }
        if (num == 0) {
            productAdapter.notifyDataSetChanged();
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


}
