
package org.plus.apps.business;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.plus.apps.business.data.ShopDataController;
import org.plus.apps.business.data.ShopDataModels;
import org.plus.apps.business.data.ShopDataSerializer;
import org.plus.apps.business.ui.BusinessProfileActivity;
import org.plus.apps.business.ui.ProductDetailFragment;
import org.plus.apps.business.ui.cells.ShopCell;
import org.plus.database.DataStorage;
import org.plus.net.APIError;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.CrossfadeDrawable;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RadialProgressView;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.ScrollSlidingTextTabStrip;

import java.util.ArrayList;

public class GlobalSearchActivity extends BaseFragment {

    private static class ViewPage extends FrameLayout {
        private RecyclerListView listView;
        private LinearLayout progressView;
        private TextView emptyTextView;
        private GridLayoutManager layoutManager;
        private ImageView emptyImageView;
        private LinearLayout emptyView;
        private RadialProgressView progressBar;
        private int selectedType;

        public ViewPage(Context context) {
            super(context);
        }
    }




    public GlobalSearchActivity(Bundle args) {
        super(args);
        if(args!= null){
            busType = getArguments().getString("bus_type");
            title = getArguments().getString("title");
        }
    }

    public GlobalSearchActivity() {
        this.busType = "";
    }



    private RecyclerListView recentListView;
    private ViewPage[] viewPages = new ViewPage[2];
    private ProductSearchAdapter productSearchAdapter;
    private ShopSearchAdapter shopSearchAdapter;
    private RecentAdapter recentAdapter;

    private ArrayList<ProductCell> productCellCache = new ArrayList<>(10);
    private ArrayList<ProductCell> productCache = new ArrayList<>(10);


    private ScrollSlidingTextTabStrip scrollSlidingTextTabStrip;

    private ArrayList<ShopDataSerializer.ShopSnip> shopSearchResults = new ArrayList<>();
    private ArrayList<ShopDataSerializer.Product> productSearchResults = new ArrayList<>();

    private AnimatorSet tabsAnimation;
    private Paint backgroundPaint = new Paint();
    private boolean tabsAnimationInProgress;
    private boolean animatingForward;
    private boolean backAnimation;
    private int maximumVelocity;
    private static final Interpolator interpolator = t -> {
        --t;
        return t * t * t * t * t + 1.0F;
    };

    private ActionBarMenuItem searchItem;
    private final static int search_button = 0;

    private boolean swipeBackEnabled = true;

    private Runnable searchRunnable;

    private String[] lastSearchQueries = new String[2];
    private String[] lastFoundQueries = new String[2];
    private int currentRequestNum[] = new int[2];
    private boolean[] searching = new boolean[2];

    public void searchDelayed(final String query) {
        if (query == null || query.length() == 0) {
            productSearchResults.clear();
            shopSearchResults.clear();
            searching[0] = false;
            searching[1] = false;

            if (productSearchAdapter != null) {
                productSearchAdapter.notifyDataSetChanged();
            }
            if (shopSearchAdapter != null) {
                shopSearchAdapter.notifyDataSetChanged();
            }

        } else {
            if (searchRunnable != null) {
                Utilities.searchQueue.cancelRunnable(searchRunnable);
                searchRunnable = null;
            }
            searching[0] = true;
            searching[1] = true;
            Utilities.searchQueue.postRunnable(searchRunnable = () -> AndroidUtilities.runOnUIThread(() -> {
                searchRunnable = null;
                searchWithQuery(query);
            }), 400);
        }
    }

    private void searchWithQuery(String query) {
        for (int a = 0; a < 2; a++) {
            lastSearchQueries[a] = query;
            if (searching[a]) {
                searching[a] = false;
                if (currentRequestNum[a] != 0) {
                    ShopDataController.getInstance(currentAccount).cancelRequest(currentRequestNum[a]);
                    currentRequestNum[a] = 0;
                }
            }
            boolean wasSearching = searching[a];
            searching[a] = true;
            if(a == 0){
              currentRequestNum [0] =  ShopDataController.getInstance(currentAccount).searchProduct(query, busType,null, null, null, new ShopDataController.ProductSearchCallBack() {
                    @Override
                    public void run(Object response, APIError error, String next, int count) {
                        currentRequestNum[0] = 0;
                        searching[0] = false;
                        productSearchResults.clear();
                        lastFoundQueries[0] =  query;
                        if(error == null){
                            ArrayList<ShopDataSerializer.Product> products = (ArrayList<ShopDataSerializer.Product>)response;
                            productSearchResults.addAll(products);
                            int oldItemCount = productSearchAdapter.getItemCount();
                            if(oldItemCount > 0){
                                productSearchAdapter.notifyItemRangeInserted(oldItemCount,products.size());
                            }else{
                                productSearchAdapter.notifyDataSetChanged();
                            }
                            for(int a =0; a < viewPages.length; a++){
                                if(viewPages[a].selectedType == 0){
                                    viewPages[a].progressView.setVisibility(View.GONE);
                                }
                            }
                        }
                        addToRecent(query);
                    }
                });

            }else{

                currentRequestNum [1] =  ShopDataController.getInstance(currentAccount).searchShop(query, null, (response, error, next, count) -> {
                    currentRequestNum[1] = 0;
                    searching[1] = false;
                    shopSearchResults.clear();
                    lastFoundQueries[1] =  query;
                    if(error == null){
                        ArrayList<ShopDataSerializer.ShopSnip> shopSnips = (ArrayList<ShopDataSerializer.ShopSnip>)response;
                        shopSearchResults.addAll(shopSnips);
                        int oldItemCount = productSearchAdapter.getItemCount();
                        if(oldItemCount > 0){
                            productSearchAdapter.notifyItemRangeInserted(oldItemCount,shopSnips.size());
                        }else{
                            productSearchAdapter.notifyDataSetChanged();
                        }
                        for(int i =0; i < viewPages.length; i++){
                            if(viewPages[i].selectedType == 1){
                                viewPages[i].progressView.setVisibility(View.GONE);
                            }
                        }

                    }
                    addToRecent(query);

                });
            }
        }



    }
    public String busType;
    public String title;

    public void addToRecent(String query){
        ShopDataModels.RecentSearch recentSearch = new ShopDataModels.RecentSearch();
        recentSearch.search = query;
        recentSearch.timeStamp = System.currentTimeMillis();
        getDataStorage().getStorageQueue().postRunnable(new Runnable() {
            @Override
            public void run() {
                getDataStorage().shopDao().insert(recentSearch);
                loadRecentSearches();
            }
        });
    }

    @Override
    public View createView(Context context) {

        Theme.createProfileResources(context);
        for(int a = 0; a < 10; a++){
            productCellCache.add(new ProductCell(context));
        }

        actionBar.setBackgroundColor(Theme.getColor(Theme.key_dialogBackground));
        actionBar.setTitleColor(Theme.getColor(Theme.key_dialogTextBlack));
        actionBar.setItemsColor(Theme.getColor(Theme.key_dialogTextBlack), false);
        actionBar.setItemsBackgroundColor(Theme.getColor(Theme.key_dialogButtonSelector), false);
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }
        actionBar.setExtraHeight(AndroidUtilities.dp(44));
        actionBar.setAllowOverlayTitle(false);
        actionBar.setAddToContainer(false);
        actionBar.setClipContent(true);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });
        hasOwnBackground = true;

        FrameLayout frameLayout;
        fragmentView = frameLayout = new FrameLayout(context) {

            private int startedTrackingPointerId;
            private boolean startedTracking;
            private boolean maybeStartTracking;
            private int startedTrackingX;
            private int startedTrackingY;
            private VelocityTracker velocityTracker;
            private boolean globalIgnoreLayout;

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
                viewPages[1].selectedType = id;
                viewPages[1].setVisibility(View.VISIBLE);
                animatingForward = forward;
                switchToCurrentSelectedMode(true);
                if (forward) {
                    viewPages[1].setTranslationX(viewPages[0].getMeasuredWidth());
                } else {
                    viewPages[1].setTranslationX(-viewPages[0].getMeasuredWidth());
                }
                return true;
            }

            @Override
            public void forceHasOverlappingRendering(boolean hasOverlappingRendering) {
                super.forceHasOverlappingRendering(hasOverlappingRendering);
            }

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int widthSize = MeasureSpec.getSize(widthMeasureSpec);
                int heightSize = MeasureSpec.getSize(heightMeasureSpec);

                setMeasuredDimension(widthSize, heightSize);

                measureChildWithMargins(actionBar, widthMeasureSpec, 0, heightMeasureSpec, 0);
                int actionBarHeight = actionBar.getMeasuredHeight();
                globalIgnoreLayout = true;
                for (int a = 0; a < viewPages.length; a++) {
                    if (viewPages[a] == null) {
                        continue;
                    }
                    if (viewPages[a].listView != null) {
                        viewPages[a].listView.setPadding(0, actionBarHeight , 0, AndroidUtilities.dp(4));
                    }
                    if (viewPages[a].emptyView != null) {
                        viewPages[a].emptyView.setPadding(0, actionBarHeight , 0, 0);
                    }
                    if (viewPages[a].progressView != null) {
                        viewPages[a].progressView.setPadding(0, actionBarHeight , 0, 0);
                    }

//                    if(recentListView != null){
//                        recentListView.setPadding(0, actionBarHeight , 0, 0);
//                    }
                }
                globalIgnoreLayout = false;

                int childCount = getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View child = getChildAt(i);
                    if (child == null || child.getVisibility() == GONE || child == actionBar) {
                        continue;
                    }
                    measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                }
            }

            @Override
            protected void dispatchDraw(Canvas canvas) {
                super.dispatchDraw(canvas);
                if(parentLayout != null){
                    parentLayout.drawHeaderShadow(canvas, actionBar.getMeasuredHeight() + (int) actionBar.getTranslationY() - ((recentListView != null && recentListView.getVisibility() == VISIBLE)? + AndroidUtilities.dp(44):0));

                }
            }

            @Override
            public void requestLayout() {
                if (globalIgnoreLayout) {
                    return;
                }
                super.requestLayout();
            }

            public boolean checkTabsAnimationInProgress() {
                if (tabsAnimationInProgress) {
                    boolean cancel = false;
                    if (backAnimation) {
                        if (Math.abs(viewPages[0].getTranslationX()) < 1) {
                            viewPages[0].setTranslationX(0);
                            viewPages[1].setTranslationX(viewPages[0].getMeasuredWidth() * (animatingForward ? 1 : -1));
                            cancel = true;
                        }
                    } else if (Math.abs(viewPages[1].getTranslationX()) < 1) {
                        viewPages[0].setTranslationX(viewPages[0].getMeasuredWidth() * (animatingForward ? -1 : 1));
                        viewPages[1].setTranslationX(0);
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

            @Override
            protected void onDraw(Canvas canvas) {
                backgroundPaint.setColor(Theme.getColor(Theme.key_windowBackgroundGray));
                canvas.drawRect(0, actionBar.getMeasuredHeight() + actionBar.getTranslationY(), getMeasuredWidth(), getMeasuredHeight(), backgroundPaint);
            }

            @Override
            public boolean onTouchEvent(MotionEvent ev) {
                if (!parentLayout.checkTransitionAnimation() && !checkTabsAnimationInProgress()) {
                    if (ev != null) {
                        if (velocityTracker == null) {
                            velocityTracker = VelocityTracker.obtain();
                        }
                        velocityTracker.addMovement(ev);
                    }
                    if (ev != null && ev.getAction() == MotionEvent.ACTION_DOWN && !startedTracking && !maybeStartTracking) {
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
                                viewPages[0].setTranslationX(0);
                                viewPages[1].setTranslationX(animatingForward ? viewPages[0].getMeasuredWidth() : -viewPages[0].getMeasuredWidth());
                                scrollSlidingTextTabStrip.selectTabWithId(viewPages[1].selectedType, 0);
                            }
                        }
                        if (maybeStartTracking && !startedTracking) {
                            float touchSlop = AndroidUtilities.getPixelsInCM(0.3f, true);
                            if (Math.abs(dx) >= touchSlop && Math.abs(dx) > dy) {
                                prepareForMoving(ev, dx < 0);
                            }
                        } else if (startedTracking) {
                            viewPages[0].setTranslationX(dx);
                            if (animatingForward) {
                                viewPages[1].setTranslationX(viewPages[0].getMeasuredWidth() + dx);
                            } else {
                                viewPages[1].setTranslationX(dx - viewPages[0].getMeasuredWidth());
                            }
                            float scrollProgress = Math.abs(dx) / (float) viewPages[0].getMeasuredWidth();
                            scrollSlidingTextTabStrip.selectTabWithId(viewPages[1].selectedType, scrollProgress);
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
                            float x = viewPages[0].getX();
                            tabsAnimation = new AnimatorSet();
                            backAnimation = Math.abs(x) < viewPages[0].getMeasuredWidth() / 3.0f && (Math.abs(velX) < 3500 || Math.abs(velX) < Math.abs(velY));
                            float distToMove;
                            float dx;
                            if (backAnimation) {
                                dx = Math.abs(x);
                                if (animatingForward) {
                                    tabsAnimation.playTogether(
                                            ObjectAnimator.ofFloat(viewPages[0], View.TRANSLATION_X, 0),
                                            ObjectAnimator.ofFloat(viewPages[1], View.TRANSLATION_X, viewPages[1].getMeasuredWidth())
                                    );
                                } else {
                                    tabsAnimation.playTogether(
                                            ObjectAnimator.ofFloat(viewPages[0], View.TRANSLATION_X, 0),
                                            ObjectAnimator.ofFloat(viewPages[1], View.TRANSLATION_X, -viewPages[1].getMeasuredWidth())
                                    );
                                }
                            } else {
                                dx = viewPages[0].getMeasuredWidth() - Math.abs(x);
                                if (animatingForward) {
                                    tabsAnimation.playTogether(
                                            ObjectAnimator.ofFloat(viewPages[0], View.TRANSLATION_X, -viewPages[0].getMeasuredWidth()),
                                            ObjectAnimator.ofFloat(viewPages[1], View.TRANSLATION_X, 0)
                                    );
                                } else {
                                    tabsAnimation.playTogether(
                                            ObjectAnimator.ofFloat(viewPages[0], View.TRANSLATION_X, viewPages[0].getMeasuredWidth()),
                                            ObjectAnimator.ofFloat(viewPages[1], View.TRANSLATION_X, 0)
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
                                        viewPages[1].setVisibility(View.GONE);
                                    } else {
                                        ViewPage tempPage = viewPages[0];
                                        viewPages[0] = viewPages[1];
                                        viewPages[1] = tempPage;
                                        viewPages[1].setVisibility(View.GONE);
                                        swipeBackEnabled = viewPages[0].selectedType == scrollSlidingTextTabStrip.getFirstTabId();
                                        scrollSlidingTextTabStrip.selectTabWithId(viewPages[0].selectedType, 1.0f);
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
        };
        fragmentView.setWillNotDraw(false);
        ActionBarMenu menu = actionBar.createMenu();
        searchItem = menu.addItem(search_button, R.drawable.ic_ab_search).setIsSearchField(true).setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener() {
            @Override
            public void onSearchExpand() {
                searchItem.getSearchField().requestFocus();
            }

            @Override
            public boolean canCollapseSearch() {
                finishFragment();
                return false;
            }

            @Override
            public void onTextChanged(EditText editText) {
                if(editText != null && editText.getText() != null && editText.getText().length()  == 0){
                    if(recentListView != null){
                        recentListView.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onSearchPressed(EditText editText) {
                String text = editText.getText().toString();
                prepareForSearch(false);
                searchDelayed(text);
            }
        });
        searchItem.setSearchFieldHint(LocaleController.getString("SearchProductTitle", R.string.SearchProductTitle) + (busType.isEmpty()?"":"in " + busType));
        EditTextBoldCursor editText = searchItem.getSearchField();
        editText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        editText.setCursorColor(Theme.getColor(Theme.key_dialogTextBlack));
        editText.setHintTextColor(Theme.getColor(Theme.key_chat_messagePanelHint));

        scrollSlidingTextTabStrip = new ScrollSlidingTextTabStrip(context);
        scrollSlidingTextTabStrip.setUseSameWidth(true);
        scrollSlidingTextTabStrip.setColors(Theme.key_chat_attachActiveTab, Theme.key_chat_attachActiveTab, Theme.key_chat_attachUnactiveTab, Theme.key_dialogButtonSelector);
        actionBar.addView(scrollSlidingTextTabStrip, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 44, Gravity.LEFT | Gravity.BOTTOM));
        scrollSlidingTextTabStrip.setDelegate(new ScrollSlidingTextTabStrip.ScrollSlidingTabStripDelegate() {
            @Override
            public void onPageSelected(int id, boolean forward) {
                if (viewPages[0].selectedType == id) {
                    return;
                }
                swipeBackEnabled = id == scrollSlidingTextTabStrip.getFirstTabId();
                viewPages[1].selectedType = id;
                viewPages[1].setVisibility(View.VISIBLE);
                switchToCurrentSelectedMode(true);
                animatingForward = forward;
                if (id == 0) {
                    searchItem.setSearchFieldHint(LocaleController.getString("SearchProductTitle", R.string.SearchProductTitle));
                } else {
                    searchItem.setSearchFieldHint(LocaleController.getString("SearchShopTitle", R.string.SearchShopTitle));
                }
            }

            @Override
            public void onPageScrolled(float progress) {
                if (progress == 1 && viewPages[1].getVisibility() != View.VISIBLE) {
                    return;
                }
                if (animatingForward) {
                    viewPages[0].setTranslationX(-progress * viewPages[0].getMeasuredWidth());
                    viewPages[1].setTranslationX(viewPages[0].getMeasuredWidth() - progress * viewPages[0].getMeasuredWidth());
                } else {
                    viewPages[0].setTranslationX(progress * viewPages[0].getMeasuredWidth());
                    viewPages[1].setTranslationX(progress * viewPages[0].getMeasuredWidth() - viewPages[0].getMeasuredWidth());
                }
                if (progress == 1) {
                    ViewPage tempPage = viewPages[0];
                    viewPages[0] = viewPages[1];
                    viewPages[1] = tempPage;
                    viewPages[1].setVisibility(View.GONE);
                }
            }
        });

        ViewConfiguration configuration = ViewConfiguration.get(context);
        maximumVelocity = configuration.getScaledMaximumFlingVelocity();

        productSearchAdapter = new ProductSearchAdapter(context);
        shopSearchAdapter  = new ShopSearchAdapter(context);
        recentAdapter = new RecentAdapter(context);

        for(int a = 0; a < viewPages.length; a++){

          final ViewPage  viewPage = new ViewPage(context) {
                @Override
                public void setTranslationX(float translationX) {
                    super.setTranslationX(translationX);
                    if (tabsAnimationInProgress) {
                        if (viewPages[0] == this) {
                            float scrollProgress = Math.abs(viewPages[0].getTranslationX()) / (float) viewPages[0].getMeasuredWidth();
                            scrollSlidingTextTabStrip.selectTabWithId(viewPages[1].selectedType, scrollProgress);
                        }
                    }
                }
            };

          frameLayout.addView(viewPage, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
          viewPages[a] = viewPage;



          final GridLayoutManager layoutManager = viewPages[a].layoutManager = new GridLayoutManager(context,2){
              @Override
              public boolean supportsPredictiveItemAnimations() {
                  return false;
              }
          };
          viewPages[a].listView = new RecyclerListView(context);
          viewPages[a].listView.setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING);
          viewPages[a].listView.setItemAnimator(null);
          viewPages[a].listView.setClipToPadding(false);
          viewPages[a].listView.setLayoutManager(layoutManager);
          viewPages[a].addView(viewPages[a].listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

            viewPages[a].layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {

                    RecyclerView.Adapter adapter = viewPage.listView.getAdapter();
                    if(adapter != null){
                        if(adapter instanceof ProductSearchAdapter){
                            ProductSearchAdapter productAdapter =(ProductSearchAdapter) adapter;
                            return  1;
//                            if(productAdapter.getItemViewType(position) == 4){
//                                return 1;
//                            }else{
//                                return layoutManager.getSpanCount();
//                            }
                        }else if(adapter instanceof ShopSearchAdapter){
                            return 2;
                        }

                    }
                    return 0;
                }
            });


            viewPages[a].listView.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(android.graphics.Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                    RecyclerView.Adapter adapter = viewPage.listView.getAdapter();
                    if(adapter != null){
                        if(adapter instanceof ProductSearchAdapter) {
                                outRect.left = AndroidUtilities.dp(4);
                                outRect.right = AndroidUtilities.dp(4);
                                outRect.bottom = AndroidUtilities.dp (4);
                                outRect.top = AndroidUtilities.dp(4);
                        }else{
                            outRect.left = AndroidUtilities.dp(0);
                            outRect.right = AndroidUtilities.dp(0);
                            outRect.bottom = AndroidUtilities.dp (0);
                            outRect.top = AndroidUtilities.dp(0);
                        }
                    }

                }

            });

            viewPages[a].listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    if(viewPage.listView.getAdapter() == productSearchAdapter){
                        ShopDataSerializer.Product product =  productSearchAdapter.getItem(position);
                        if(product != null){
                            Bundle bundle = new Bundle();
                            bundle.putInt("chat_id",ShopUtils.toClientChannelId(product.shop.chanel));
                            bundle.putInt("item_id",product.id);

                            ProductDetailFragment detailFragment = new ProductDetailFragment(bundle);
                            presentFragment(detailFragment);
                        }

                    }else if(viewPage.listView.getAdapter() == shopSearchAdapter){
                        ShopDataSerializer.ShopSnip shopSnip =  shopSearchAdapter.getItem(position);
                        if(shopSnip != null){
                            Bundle bundle = new Bundle();
                            bundle.putInt("chat_id", ShopUtils.toClientChannelId(shopSnip.channel));
                            BusinessProfileActivity businessProfileActivity = new BusinessProfileActivity(bundle);
                            presentFragment(businessProfileActivity);
                        }

                    }
                }
            });



            viewPages[a].emptyView = new LinearLayout(context) {
                @Override
                protected void onDraw(Canvas canvas) {
                    backgroundPaint.setColor(Theme.getColor(Theme.key_windowBackgroundGray));
                    canvas.drawRect(0, actionBar.getMeasuredHeight() + actionBar.getTranslationY(), getMeasuredWidth(), getMeasuredHeight(), backgroundPaint);
                }
            };
         viewPages[a].emptyView.setWillNotDraw(false);
         viewPages[a].emptyView.setOrientation(LinearLayout.VERTICAL);
         viewPages[a].emptyView.setGravity(Gravity.CENTER);
         viewPages[a].emptyView.setVisibility(View.GONE);
         viewPages[a].addView(viewPages[a].emptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
         viewPages[a].emptyView.setOnTouchListener((v, event) -> true);

         viewPages[a].emptyImageView = new ImageView(context);
         viewPages[a].emptyView.addView(viewPages[a].emptyImageView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

         viewPages[a].emptyTextView = new TextView(context);
         viewPages[a].emptyTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
         viewPages[a].emptyTextView.setGravity(Gravity.CENTER);
         viewPages[a].emptyTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
         viewPages[a].emptyTextView.setPadding(AndroidUtilities.dp(40), 0, AndroidUtilities.dp(40), AndroidUtilities.dp(128));
         viewPages[a].emptyView.addView(viewPages[a].emptyTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 0, 24, 0, 0));

         viewPages[a].progressView = new LinearLayout(context) {
             @Override
             protected void onDraw(Canvas canvas) {
                 backgroundPaint.setColor(Theme.getColor(Theme.key_windowBackgroundGray));
                 canvas.drawRect(0, actionBar.getMeasuredHeight() + actionBar.getTranslationY(), getMeasuredWidth(), getMeasuredHeight(), backgroundPaint);
             }
         };
         viewPages[a].progressView.setWillNotDraw(false);
         viewPages[a].progressView.setGravity(Gravity.CENTER);
         viewPages[a].progressView.setOrientation(LinearLayout.VERTICAL);
         viewPages[a].progressView.setVisibility(View.GONE);
         viewPages[a].addView(viewPages[a].progressView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

         viewPages[a].progressBar = new RadialProgressView(context);
         viewPages[a].progressView.addView(viewPages[a].progressBar, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

         if (a != 0) {
             viewPages[a].setVisibility(View.GONE);
         }
            viewPages[a].listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    if (newState != RecyclerView.SCROLL_STATE_DRAGGING) {
                        int scrollY = (int) -actionBar.getTranslationY();
                        int actionBarHeight = ActionBar.getCurrentActionBarHeight();
                        if (scrollY != 0 && scrollY != actionBarHeight) {
                            if (scrollY < actionBarHeight / 2) {
                                viewPages[0].listView.smoothScrollBy(0, -scrollY);
                            } else {
                                viewPages[0].listView.smoothScrollBy(0, actionBarHeight - scrollY);
                            }
                        }
                    }
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    if (recyclerView == viewPages[0].listView) {
                        float currentTranslation = actionBar.getTranslationY();
                        float newTranslation = currentTranslation - dy;
                        if (newTranslation < -ActionBar.getCurrentActionBarHeight()) {
                            newTranslation = -ActionBar.getCurrentActionBarHeight();
                        } else if (newTranslation > 0) {
                            newTranslation = 0;
                        }
                        if (newTranslation != currentTranslation) {
                            setScrollY(newTranslation);
                        }
                    }
                }
            });
        }
        frameLayout.addView(actionBar, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        updateTabs();
        switchToCurrentSelectedMode(false);
        swipeBackEnabled = scrollSlidingTextTabStrip.getCurrentTabId() == scrollSlidingTextTabStrip.getFirstTabId();

        recentListView = new RecyclerListView(context);
        recentListView.setLayoutManager(new LinearLayoutManager(context,RecyclerView.VERTICAL,false));
        recentListView.setItemAnimator(null);
        recentListView.setClipToPadding(false);
        recentListView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        recentListView.setOnItemClickListener(new RecyclerListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if(position >= recentSearchArrayList.size() || position < 0){
                    return;
                }
                ShopDataModels.RecentSearch recentSearch = recentSearchArrayList.get(position);
                if(recentSearch != null){
                    prepareForSearch(false);
                    searchItem.getSearchField().setText(recentSearch.search);
                    searchItem.getSearchField().setSelection(recentSearch.search.length());
                    AndroidUtilities.hideKeyboard(searchItem.getSearchField());
                    searchDelayed(recentSearch.search);
                }

            }
        });
        recentListView.setAdapter(recentAdapter = new RecentAdapter(context));
        int top = actionBar.getOccupyStatusBar()?AndroidUtilities.statusBarHeight:0 + ActionBar.getCurrentActionBarHeight();
        frameLayout.addView(recentListView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT,Gravity.TOP|Gravity.LEFT,0,top,0,0));

        return fragmentView;
    }

    private void prepareForSearch(boolean showRecent){
        if(!showRecent){
            recentListView.setVisibility(View.GONE);
            for(int a =0; a < viewPages.length; a++){
                viewPages[a].progressView.setVisibility(View.VISIBLE);
            }
        }else{
            recentListView.setVisibility(View.VISIBLE);
            for(int a =0; a < viewPages.length; a++){
                viewPages[a].progressView.setVisibility(View.GONE);
            }
        }
    }



    private ArrayList<ShopDataModels.RecentSearch> recentSearchArrayList = new ArrayList<>();


    private void loadRecentSearches(){
        ShopDataController.getInstance(currentAccount).loadRecentSearchForShop(new ShopDataController.ProductSearchCallBack() {
            @Override
            public void run(Object response, APIError error, String next, int count) {
                if(response == null){
                    return;
                }
                recentSearchArrayList =  (ArrayList<ShopDataModels.RecentSearch>)response;
                if(recentAdapter != null){
                    recentAdapter.notifyDataSetChanged();
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        if (searchItem != null) {
            searchItem.openSearch(true);
            getParentActivity().getWindow().setSoftInputMode(SharedConfig.smoothKeyboard ? WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN : WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        loadRecentSearches();

    }


    private void updateTabs() {
        if (scrollSlidingTextTabStrip == null) {
            return;
        }
        scrollSlidingTextTabStrip.addTextTab(0, LocaleController.getString("ProductTab", R.string.ProductTab));
        scrollSlidingTextTabStrip.addTextTab(1, LocaleController.getString("ShopTab", R.string.ShopTab));
        scrollSlidingTextTabStrip.setVisibility(View.VISIBLE);
        actionBar.setExtraHeight(AndroidUtilities.dp(44));
        int id = scrollSlidingTextTabStrip.getCurrentTabId();
        if (id >= 0) {
            viewPages[0].selectedType = id;
        }
        scrollSlidingTextTabStrip.finishAddingTabs();
    }

    private void switchToCurrentSelectedMode(boolean animated) {
        for (int a = 0; a < viewPages.length; a++) {
            viewPages[a].listView.stopScroll();
        }
        int a = animated ? 1 : 0;
        RecyclerView.Adapter currentAdapter = viewPages[a].listView.getAdapter();
        viewPages[a].listView.setPinnedHeaderShadowDrawable(null);
        if (actionBar.getTranslationY() != 0) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) viewPages[a].listView.getLayoutManager();
            layoutManager.scrollToPositionWithOffset(0, (int) actionBar.getTranslationY());
        }

        viewPages[a].emptyTextView.setText(LocaleController.getString("NoResult", R.string.NoResult));
        viewPages[a].emptyTextView.setPadding(AndroidUtilities.dp(40), 0, AndroidUtilities.dp(40), AndroidUtilities.dp(30));
        viewPages[a].emptyTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        viewPages[a].emptyImageView.setVisibility(View.GONE);
        viewPages[a].listView.setEmptyView(viewPages[a].emptyView);

        if (viewPages[a].selectedType == 0) {
            if (currentAdapter != productSearchAdapter) {
                viewPages[a].listView.setAdapter(productSearchAdapter);
            }
            productSearchAdapter.notifyDataSetChanged();
        }else {
            if (currentAdapter != shopSearchAdapter) {
                viewPages[a].listView.setAdapter(shopSearchAdapter);
            }
            shopSearchAdapter.notifyDataSetChanged();
        }


    }

    private void setScrollY(float value) {
        actionBar.setTranslationY(value);
        for (int a = 0; a < viewPages.length; a++) {
            viewPages[a].listView.setPinnedSectionOffsetY((int) value);
        }
        fragmentView.invalidate();
    }

    @Override
    public boolean isSwipeBackEnabled(MotionEvent event) {
        return swipeBackEnabled;
    }

    private class ProductSearchAdapter extends RecyclerListView.SelectionAdapter{

        private Context mContext;

        public ProductSearchAdapter(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return true;
        }

        public ShopDataSerializer.Product getItem(int pos){
            return productSearchResults.get(pos);
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ProductCell ProductCell = new ProductCell(mContext);
            return new RecyclerListView.Holder(ProductCell);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ProductCell ProductCell =(ProductCell)holder.itemView;
            ShopDataSerializer.Product product  = productSearchResults.get(position);
            ProductCell.setProduct(product);

        }


        @Override
        public int getItemCount() {
            int count = 0;
            if(productSearchResults != null){
                count = productSearchResults.size();
            }
            return count;
        }
    }

    private class ShopSearchAdapter extends RecyclerListView.SelectionAdapter{

        private Context mContext;

        public ShopSearchAdapter(Context mContext) {
            this.mContext = mContext;
        }


        public ShopDataSerializer.ShopSnip getItem(int pos){
            return shopSearchResults.get(pos);
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return true;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ShopCell shopCell = new ShopCell(mContext,16);
            shopCell.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(shopCell);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ShopCell ProductCell =(ShopCell)holder.itemView;
            ShopDataSerializer.ShopSnip shopSnip  = shopSearchResults.get(position);
            ProductCell.setShop(shopSnip,true);
            ProductCell.setVerified(true?getVerifiedCrossfadeDrawable():null);
        }

        @Override
        public int getItemCount() {
            int count = 0;
            if(shopSearchResults != null){
                count = shopSearchResults.size();
            }
            return count;
        }
    }

    private class RecentAdapter extends RecyclerListView.SelectionAdapter{

        private Context mContext;

        public RecentAdapter(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return true;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new RecyclerListView.Holder(new TextSettingsCell(mContext,16));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ShopDataModels.RecentSearch recentSearch = recentSearchArrayList.get(position);
            TextSettingsCell textSettingsCell =(TextSettingsCell)holder.itemView;
            textSettingsCell.setTextAndIcon(recentSearch.search,R.drawable.wallet_send,recentSearchArrayList.size() - 1 != position);
        }

        @Override
        public int getItemCount() {
            return recentSearchArrayList != null?recentSearchArrayList.size():0;
        }
    }

    private Drawable verifiedDrawable;
    private Drawable verifiedCheckDrawable;
    private CrossfadeDrawable verifiedCrossfadeDrawable;
    private Drawable getVerifiedCrossfadeDrawable() {
        if (verifiedCrossfadeDrawable == null) {
            verifiedDrawable = Theme.profile_verifiedDrawable.getConstantState().newDrawable().mutate();
            verifiedCheckDrawable = Theme.profile_verifiedCheckDrawable.getConstantState().newDrawable().mutate();
            verifiedCrossfadeDrawable = new CrossfadeDrawable(new CombinedDrawable(verifiedDrawable, verifiedCheckDrawable), ContextCompat.getDrawable(getParentActivity(), R.drawable.verified_profile));
        }
        return verifiedCrossfadeDrawable;
    }

}
