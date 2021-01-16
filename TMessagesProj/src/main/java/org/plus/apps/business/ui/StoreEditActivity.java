package org.plus.apps.business.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Property;
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
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.plus.apps.business.ShopUtils;
import org.plus.apps.business.data.ShopDataController;
import org.plus.apps.business.data.ShopDataSerializer;
import org.plus.apps.business.ui.cells.ProductEditCell;
import org.plus.apps.business.ui.components.BusinessAlert;
import org.plus.apps.business.ui.components.ShopsEmptyCell;
import org.plus.net.APIError;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.DialogsEmptyCell;
import org.telegram.ui.Cells.StickerSetCell;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.AnimationProperties;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.ClippingImageView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RadialProgressView;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.ScrollSlidingTextTabStrip;
import org.telegram.ui.StatisticActivity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;


public class StoreEditActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private FloatingActionButton floatingButton;
    private FrameLayout floatingButtonContainer;

    private static class MediaPage extends FrameLayout {
        private RecyclerListView listView;
        private LinearLayout progressView;
        private LinearLayoutManager layoutManager;
        private FrameLayout emptyCell;
        private ShopsEmptyCell dialogsEmptyCell;
        private RadialProgressView progressBar;
        private ClippingImageView animatingImageView;
        private int selectedType;

        public MediaPage(Context context) {
            super(context);
        }
    }

    private ShopDataSerializer.Shop currentShop;


    private boolean scrolling;
    private ProductAdapter productAdapter;
    private OfferAdapter offerAdapter;

    private ScrollSlidingTextTabStrip scrollSlidingTextTabStrip;
    private View actionModeBackground;

    private int maximumVelocity;

    private Paint backgroundPaint = new Paint();

    private int additionalPadding;

    private boolean searchWas;
    private boolean searching;
    private boolean disableActionBarScrolling;

    private int initialTab;

    private AnimatorSet tabsAnimation;
    private boolean tabsAnimationInProgress;
    private boolean animatingForward;
    private boolean backAnimation;

    private Drawable pinnedHeaderShadowDrawable;

    private boolean swipeBackEnabled;

    private long dialog_id;
    private int columnsCount = 3;

    private MediaPage[] mediaPages = new MediaPage[2];
    private ActionBarMenuItem searchItem;
    private int searchItemState;

    private static final Interpolator interpolator = t -> {
        --t;
        return t * t * t * t * t + 1.0F;
    };

    private static class ProductAdminCell extends FrameLayout{

        public ProductAdminCell(@NonNull Context context) {
            super(context);
        }
    }


    public StoreEditActivity(Bundle args){
        super(args);
    }




    @Override
    public boolean onFragmentCreate() {
        chat_id = getArguments().getInt("chat_id");
        getNotificationCenter().addObserver(this,NotificationCenter.didProductLoaded);
        getNotificationCenter().addObserver(this,NotificationCenter.didOffersLoaded);
        getNotificationCenter().addObserver(this,NotificationCenter.didNewProductListed);

        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        getNotificationCenter().removeObserver(this,NotificationCenter.didProductLoaded);
        getNotificationCenter().removeObserver(this,NotificationCenter.didOffersLoaded);
        getNotificationCenter().removeObserver(this,NotificationCenter.didNewProductListed);

        super.onFragmentDestroy();
    }

    public final Property<StoreEditActivity, Float> SCROLL_Y = new AnimationProperties.FloatProperty<StoreEditActivity>("animationValue") {
        @Override
        public void setValue(StoreEditActivity object, float value) {
            object.setScrollY(value);
            for (int a = 0; a < mediaPages.length; a++) {
                mediaPages[a].listView.checkSection();
            }
        }

        @Override
        public Float get(StoreEditActivity object) {
            return actionBar.getTranslationY();
        }
    };


    public static final  String[] tabs = {"Products","Offers"};

    private boolean productEndReached;
    private boolean offerEndReached;
    private boolean showEmpty;

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {

        if(id == NotificationCenter.didNewProductListed){
            if(args[0] instanceof ShopDataSerializer.Product){
                ShopDataSerializer.Product product =(ShopDataSerializer.Product) args[0];
                products.add(0,product);
                if(productAdapter != null){
                    productAdapter.notifyItemInserted(0);
                }
            }
        } else if(id == NotificationCenter.didProductLoaded){
            boolean loaded = (boolean)args[0];
            int classGuid = (int)args[2];
            if(classGuid == getClassGuid()){
                isLoadingProduct = false;
                if(loaded){
                    ArrayList<ShopDataSerializer.Product> productArrayList = (ArrayList<ShopDataSerializer.Product>)args[1];
                    String next = (String) args[3];
                    if(TextUtils.isEmpty(next) || next.equals("null")){
                        productEndReached = true;
                    }else{
                        productEndReached = false;
                    }
                    products.addAll(productArrayList);

                    if(products.isEmpty() && productEndReached){
                        showEmpty = true;
                    }else{
                        showEmpty = false;
                    }

                    if (productAdapter != null) {
                        for (int a = 0; a < mediaPages.length; a++) {
                            if (mediaPages[a].listView.getAdapter() == productAdapter) {
                                mediaPages[a].listView.stopScroll();
                            }
                        }
                    }

                    int count = products.size();
                    if(count > 0){
                        if(productAdapter != null){
                            productAdapter.notifyItemRangeInserted(count,productArrayList.size());
                        }
                    }else{
                        if(productAdapter != null){
                            productAdapter.notifyDataSetChanged();
                        }
                    }

                }else{
                    APIError apiError = (APIError)args[1];
                }
            }

            scrolling = true;
            for (int a = 0; a < mediaPages.length; a++) {
                if (mediaPages[a].selectedType == 0) {
                    if (!isLoadingProduct) {
                        if (mediaPages[a].progressView != null) {
                            mediaPages[a].progressView.setVisibility(View.GONE);
                        }
                        if (mediaPages[a].listView != null) {
                            if (mediaPages[a].listView.getEmptyView() == null) {
                                mediaPages[a].listView.setEmptyView(mediaPages[a].emptyCell);
                            }
                        }
                    }
                }
            }
        }else if(id == NotificationCenter.didOffersLoaded){
            boolean loaded = (boolean)args[0];
            int classGuid = (int)args[2];
            if(classGuid == getClassGuid()) {
                if(loaded){
                    loadingOffers = false;
                    ArrayList<ShopDataSerializer.ProductOffer> productOffers = (ArrayList<ShopDataSerializer.ProductOffer>)args[1];
                    String next = (String) args[3];
                    if(TextUtils.isEmpty(next) || next.equals("null")){
                        offerEndReached = true;
                    }else{
                        offerEndReached = false;
                    }
                    offers.addAll(productOffers);


                if (offerAdapter != null) {
                    for (int a = 0; a < mediaPages.length; a++) {
                        if (mediaPages[a].listView.getAdapter() == offerAdapter) {
                            mediaPages[a].listView.stopScroll();
                        }
                    }
                }

                    Log.i("berhan","offer size" + offers.size());

                int count = offers.size();
                if(count > 0){
                    if(offerAdapter != null){
                        offerAdapter.notifyItemRangeInserted(count,productOffers.size());
                    }
                }else{
                    if(offerAdapter != null){
                        offerAdapter.notifyDataSetChanged();
                    }
                }


                scrolling = true;
                for (int a = 0; a < mediaPages.length; a++) {
                    if (mediaPages[a].selectedType == 1) {
                        if (!loadingOffers) {
                            if (mediaPages[a].progressView != null) {
                                mediaPages[a].progressView.setVisibility(View.GONE);
                            }
                            if (mediaPages[a].listView != null) {
                                if (mediaPages[a].listView.getEmptyView() == null) {
                                    mediaPages[a].listView.setEmptyView(mediaPages[a].emptyCell);
                                }
                            }
                        }
                    }
                }

            }
            }

        }

    }


    public void setCurrentShop(ShopDataSerializer.Shop currentShop) {
        this.currentShop = currentShop;
    }


    @Override
    public View createView(Context context) {
        actionBar.setTitle("Products");
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick(){
            @Override
            public void onItemClick(int id) {
                if(id == -1){
                    finishFragment();
                }else if(id == 1){
                    Bundle bundle = new Bundle();
                    bundle.putInt("chat_id",chat_id);
                    BusinessCreateActivity businessCreateActivity = new BusinessCreateActivity(bundle,currentShop);
                    presentFragment(businessCreateActivity);
                }else if(id == 2){
                    Bundle bundle = new Bundle();
                    bundle.putInt("chat_id",chat_id);
                    ShopAdminFragment businessCreateActivity = new ShopAdminFragment(bundle,currentShop);
                    presentFragment(businessCreateActivity);
                }
            }
        });

        ViewConfiguration configuration = ViewConfiguration.get(context);
        maximumVelocity = configuration.getScaledMaximumFlingVelocity();

        searching = false;
        searchWas = false;
        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }

        ActionBarMenu menu = actionBar.createMenu();
        menu.addItem(2,R.drawable.group_admin);
        menu.addItem(1,R.drawable.msg_edit);


        actionBar.setBackButtonDrawable(new BackDrawable(false));
        actionBar.setAddToContainer(false);
        actionBar.setClipContent(true);
        actionBar.setExtraHeight(AndroidUtilities.dp(44));
        actionBar.setAllowOverlayTitle(false);

        pinnedHeaderShadowDrawable = context.getResources().getDrawable(R.drawable.photos_header_shadow);
        pinnedHeaderShadowDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundGrayShadow), PorterDuff.Mode.MULTIPLY));

        if (scrollSlidingTextTabStrip != null) {
            initialTab = scrollSlidingTextTabStrip.getCurrentTabId();
        }

        scrollSlidingTextTabStrip = new ScrollSlidingTextTabStrip(context);
        if (initialTab != -1) {
            scrollSlidingTextTabStrip.setInitialTabId(initialTab);
            initialTab = -1;
        }
        actionBar.addView(scrollSlidingTextTabStrip, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 44, Gravity.LEFT | Gravity.BOTTOM));
        scrollSlidingTextTabStrip.setDelegate(new ScrollSlidingTextTabStrip.ScrollSlidingTabStripDelegate() {
            @Override
            public void onPageSelected(int id, boolean forward) {
                if (mediaPages[0].selectedType == id) {
                    return;
                }
                swipeBackEnabled = id == scrollSlidingTextTabStrip.getFirstTabId();
                mediaPages[1].selectedType = id;
                mediaPages[1].setVisibility(View.VISIBLE);
                switchToCurrentSelectedMode(true);
                animatingForward = forward;
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
                if (searchItemState == 1) {
                    searchItem.setAlpha(progress);
                } else if (searchItemState == 2) {
                    searchItem.setAlpha(1.0f - progress);
                }
                if (progress == 1) {
                    MediaPage tempPage = mediaPages[0];
                    mediaPages[0] = mediaPages[1];
                    mediaPages[1] = tempPage;
                    mediaPages[1].setVisibility(View.GONE);
                    if (searchItemState == 2) {
                        searchItem.setVisibility(View.INVISIBLE);
                    }
                    searchItemState = 0;
                }
            }
        });

//        searchItem = menu.addItem(0, R.drawable.ic_ab_search).setIsSearchField(true).setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener() {
//            @Override
//            public void onSearchExpand() {
//                searching = true;
//               // resetScroll();
//            }
//
//            @Override
//            public void onSearchCollapse() {
//                searching = false;
//                searchWas = false;
////                documentsSearchAdapter.search(null);
////                linksSearchAdapter.search(null);
////                audioSearchAdapter.search(null);
////                if (ignoreSearchCollapse) {
////                    ignoreSearchCollapse = false;
////                    return;
////                }
////
////                switchToCurrentSelectedMode(false);
//            }
//
//            @Override
//            public void onTextChanged(EditText editText) {
//                String text = editText.getText().toString();
////                if (text.length() != 0) {
////                    searchWas = true;
////                    switchToCurrentSelectedMode(false);
////                } else {
////                    searchWas = false;
////                    switchToCurrentSelectedMode(false);
////                }
////                if (mediaPages[0].selectedType == 1) {
////                    if (documentsSearchAdapter == null) {
////                        return;
////                    }
////                    documentsSearchAdapter.search(text);
////                } else if (mediaPages[0].selectedType == 3) {
////                    if (linksSearchAdapter == null) {
////                        return;
////                    }
////                    linksSearchAdapter.search(text);
////                } else if (mediaPages[0].selectedType == 4) {
////                    if (audioSearchAdapter == null) {
////                        return;
////                    }
////                    audioSearchAdapter.search(text);
////                }
//            }
//        });
//        searchItem.setSearchFieldHint(LocaleController.getString("Search", R.string.Search));
//        searchItem.setContentDescription(LocaleController.getString("Search", R.string.Search));
//        searchItem.setVisibility(View.VISIBLE);
//        searchItemState = 0;
//        hasOwnBackground = true;

        productAdapter = new ProductAdapter(context);
        offerAdapter = new OfferAdapter(context);
     //   productSearchAdapter  = new ProductSearchAdapter(context);

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
                if (searchItemState != 0) {
                    if (searchItemState == 2) {
                        searchItem.setAlpha(1.0f);
                    } else if (searchItemState == 1) {
                        searchItem.setAlpha(0.0f);
                        searchItem.setVisibility(View.INVISIBLE);
                    }
                    searchItemState = 0;
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
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int widthSize = MeasureSpec.getSize(widthMeasureSpec);
                int heightSize = MeasureSpec.getSize(heightMeasureSpec);

                setMeasuredDimension(widthSize, heightSize);

                measureChildWithMargins(actionBar, widthMeasureSpec, 0, heightMeasureSpec, 0);
                int actionBarHeight = actionBar.getMeasuredHeight();
                globalIgnoreLayout = true;
                for (int a = 0; a < mediaPages.length; a++) {
                    if (mediaPages[a] == null) {
                        continue;
                    }
                    if (mediaPages[a].listView != null) {
                        mediaPages[a].listView.setPadding(0, actionBarHeight + additionalPadding, 0, AndroidUtilities.dp(4));
                    }
                    if (mediaPages[a].emptyCell != null) {
                        mediaPages[a].emptyCell.setPadding(0, actionBarHeight + additionalPadding, 0, 0);
                    }
                    if (mediaPages[a].progressView != null) {
                        mediaPages[a].progressView.setPadding(0, actionBarHeight + additionalPadding, 0, 0);
                    }
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
            public void setPadding(int left, int top, int right, int bottom) {
                additionalPadding = top;

                int actionBarHeight = actionBar.getMeasuredHeight();
                for (int a = 0; a < mediaPages.length; a++) {
                    if (mediaPages[a] == null) {
                        continue;
                    }
                    if (mediaPages[a].emptyCell != null) {
                        mediaPages[a].emptyCell.setPadding(0, actionBarHeight + additionalPadding, 0, 0);
                    }
                    if (mediaPages[a].progressView != null) {
                        mediaPages[a].progressView.setPadding(0, actionBarHeight + additionalPadding, 0, 0);
                    }
                    if (mediaPages[a].listView != null) {
                        mediaPages[a].listView.setPadding(0, actionBarHeight + additionalPadding, 0, AndroidUtilities.dp(4));
                        mediaPages[a].listView.checkSection();
                    }
                }
                fixScrollOffset();
            }

            @Override
            protected void dispatchDraw(Canvas canvas) {
                super.dispatchDraw(canvas);
                if (parentLayout != null) {
                    parentLayout.drawHeaderShadow(canvas, actionBar.getMeasuredHeight() + (int) actionBar.getTranslationY());
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

            @Override
            protected void onDraw(Canvas canvas) {
                backgroundPaint.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
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
                            if (searchItemState == 2) {
                                searchItem.setAlpha(1.0f - scrollProgress);
                            } else if (searchItemState == 1) {
                                searchItem.setAlpha(scrollProgress);
                            }
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
                                        if (searchItemState == 2) {
                                            searchItem.setAlpha(1.0f);
                                        } else if (searchItemState == 1) {
                                            searchItem.setAlpha(0.0f);
                                            searchItem.setVisibility(View.INVISIBLE);
                                        }
                                        searchItemState = 0;
                                    } else {
                                        MediaPage tempPage = mediaPages[0];
                                        mediaPages[0] = mediaPages[1];
                                        mediaPages[1] = tempPage;
                                        mediaPages[1].setVisibility(View.GONE);
                                        if (searchItemState == 2) {
                                            searchItem.setVisibility(View.INVISIBLE);
                                        }
                                        searchItemState = 0;
                                        swipeBackEnabled = mediaPages[0].selectedType == scrollSlidingTextTabStrip.getFirstTabId();
                                        scrollSlidingTextTabStrip.selectTabWithId(mediaPages[0].selectedType, 1.0f);
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
        frameLayout.setWillNotDraw(false);

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
                            if (searchItemState == 2) {
                                searchItem.setAlpha(1.0f - scrollProgress);
                            } else if (searchItemState == 1) {
                                searchItem.setAlpha(scrollProgress);
                            }
                        }
                    }
                }
            };
            frameLayout.addView(mediaPage, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
            mediaPages[a] = mediaPage;


            final LinearLayoutManager layoutManager = mediaPages[a].layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false) {
                @Override
                public boolean supportsPredictiveItemAnimations() {
                    return false;
                }
            };
            mediaPages[a].listView = new RecyclerListView(context) {
                @Override
                protected void onLayout(boolean changed, int l, int t, int r, int b) {
                    super.onLayout(changed, l, t, r, b);
                }
            };
            mediaPages[a].listView.setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING);
            mediaPages[a].listView.setItemAnimator(null);
            mediaPages[a].listView.setClipToPadding(false);
            mediaPages[a].listView.setSectionsType(2);
            mediaPages[a].listView.setLayoutManager(layoutManager);
            mediaPages[a].addView(mediaPages[a].listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

            mediaPages[a].listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    if(mediaPage.selectedType == 0 && view instanceof ProductEditCell){
                        StoreEditActivity.this.onItemClick(position,view,((ProductEditCell) view).getProduct(),0,mediaPage.selectedType);
                    }else if(mediaPage.selectedType == 1 && view instanceof OfferCell){
                        StoreEditActivity.this.showOfferDetail(((OfferCell) view).getProductOffer());
                    }
                }
            });

            mediaPages[a].listView.setOnScrollListener(new RecyclerView.OnScrollListener() {

                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        AndroidUtilities.hideKeyboard(getParentActivity().getCurrentFocus());
                    }
                    scrolling = newState != RecyclerView.SCROLL_STATE_IDLE;
                    if (newState != RecyclerView.SCROLL_STATE_DRAGGING) {
                        int scrollY = (int) -actionBar.getTranslationY();
                        int actionBarHeight = ActionBar.getCurrentActionBarHeight();
                        if (scrollY != 0 && scrollY != actionBarHeight) {
                            if (scrollY < actionBarHeight / 2) {
                                mediaPages[0].listView.smoothScrollBy(0, -scrollY);
                            } else if (mediaPages[0].listView.canScrollVertically(1)) {
                                mediaPages[0].listView.smoothScrollBy(0, actionBarHeight - scrollY);
                            }
                        }
                    }
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    if (searching && searchWas) {
                        return;
                    }
                    int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                    int visibleItemCount = firstVisibleItem == RecyclerView.NO_POSITION ? 0 : Math.abs(layoutManager.findLastVisibleItemPosition() - firstVisibleItem) + 1;
                    int totalItemCount = recyclerView.getAdapter().getItemCount();

//                    final int threshold = mediaPage.selectedType == 0 ? 3 : 6;
//                    if (visibleItemCount != 0 && firstVisibleItem + visibleItemCount > totalItemCount - threshold && !sharedMediaData[mediaPage.selectedType].loading) {
//                        int type;
//                        if (mediaPage.selectedType == 0) {
//                            type = MediaDataController.MEDIA_PHOTOVIDEO;
//                        } else if (mediaPage.selectedType == 1) {
//                            type = MediaDataController.MEDIA_FILE;
//                        } else if (mediaPage.selectedType == 2) {
//                            type = MediaDataController.MEDIA_AUDIO;
//                        } else if (mediaPage.selectedType == 4) {
//                            type = MediaDataController.MEDIA_MUSIC;
//                        } else if (mediaPage.selectedType == 5) {
//                            type = MediaDataController.MEDIA_GIF;
//                        } else {
//                            type = MediaDataController.MEDIA_URL;
//                        }

//                        if (!sharedMediaData[mediaPage.selectedType].endReached[0]) {
//                            sharedMediaData[mediaPage.selectedType].loading = true;
//                            MediaDataController.getInstance(currentAccount).loadMedia(dialog_id, 50, sharedMediaData[mediaPage.selectedType].max_id[0], type, 1, classGuid);
//                        } else if (mergeDialogId != 0 && !sharedMediaData[mediaPage.selectedType].endReached[1]) {
//                            sharedMediaData[mediaPage.selectedType].loading = true;
//                            MediaDataController.getInstance(currentAccount).loadMedia(mergeDialogId, 50, sharedMediaData[mediaPage.selectedType].max_id[1], type, 1, classGuid);
//                        }
                    //}
                    if (recyclerView == mediaPages[0].listView && !searching && !actionBar.isActionModeShowed() && !disableActionBarScrolling) {
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
                    //updateSections(recyclerView, false);
                }
            });

            if (a == 0 && scrollToPositionOnRecreate != -1) {
                layoutManager.scrollToPositionWithOffset(scrollToPositionOnRecreate, scrollToOffsetOnRecreate);
            }

            final RecyclerListView listView = mediaPages[a].listView;
            mediaPages[a].animatingImageView = new ClippingImageView(context) {
                @Override
                public void invalidate() {
                    super.invalidate();
                    listView.invalidate();
                }
            };
            mediaPages[a].animatingImageView.setVisibility(View.GONE);
            mediaPages[a].listView.addOverlayView(mediaPages[a].animatingImageView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));


            mediaPages[a].dialogsEmptyCell  = new ShopsEmptyCell(context);
            mediaPages[a].dialogsEmptyCell .setType(4);

            mediaPages[a].emptyCell= new FrameLayout(context) {
                @Override
                protected void onDraw(Canvas canvas) {
                    backgroundPaint.setColor(Theme.getColor(Theme.key_windowBackgroundGray));
                    canvas.drawRect(0, actionBar.getMeasuredHeight() + actionBar.getTranslationY(), getMeasuredWidth(), getMeasuredHeight(), backgroundPaint);
                }
            };
            mediaPages[a].emptyCell.setOnTouchListener((v, event) -> true);

            mediaPages[a].addView(mediaPages[a].emptyCell, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

            mediaPages[a].emptyCell.addView(mediaPages[a].dialogsEmptyCell ,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.MATCH_PARENT,Gravity.CENTER));

            mediaPages[a].progressView = new LinearLayout(context) {
                @Override
                protected void onDraw(Canvas canvas) {
                    backgroundPaint.setColor(Theme.getColor(Theme.key_windowBackgroundGray));
                    canvas.drawRect(0, actionBar.getMeasuredHeight() + actionBar.getTranslationY(), getMeasuredWidth(), getMeasuredHeight(), backgroundPaint);
                }
            };
            mediaPages[a].progressView.setWillNotDraw(false);
            mediaPages[a].progressView.setGravity(Gravity.CENTER);
            mediaPages[a].progressView.setOrientation(LinearLayout.VERTICAL);
            mediaPages[a].progressView.setVisibility(View.GONE);
            mediaPages[a].addView(mediaPages[a].progressView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

            mediaPages[a].progressBar = new RadialProgressView(context);
            mediaPages[a].progressView.addView(mediaPages[a].progressBar, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));
           // mediaPages[a].emptyCell.addView(mediaPages[a].dialogsEmptyCell ,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.MATCH_PARENT,Gravity.CENTER));


            mediaPages[a].listView.setEmptyView(mediaPages[a].emptyCell);
            if (a != 0) {
                mediaPages[a].setVisibility(View.GONE);
            }
        }

        frameLayout.addView(actionBar, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        updateTabs();
        switchToCurrentSelectedMode(false);
        swipeBackEnabled = scrollSlidingTextTabStrip.getCurrentTabId() == scrollSlidingTextTabStrip.getFirstTabId();

        floatingButtonContainer = new FrameLayout(context);
        frameLayout.addView(floatingButtonContainer, LayoutHelper.createFrame((Build.VERSION.SDK_INT >= 21 ? 56 : 60) + 20, (Build.VERSION.SDK_INT >= 21 ? 56 : 60) + 14, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.BOTTOM, LocaleController.isRTL ? 4 : 0, 0, LocaleController.isRTL ? 0 : 4, 0));
        floatingButtonContainer.setOnClickListener(v -> {
            if(currentShop != null){
                BusinessAlert businessAlert = BusinessAlert.createBusinessAlert(context);
                businessAlert.setDelegate(new BusinessAlert.BusinessAlertDelegate() {
                    @Override
                    public void didPressBusiness(ShopDataSerializer.ProductType business) {
                        ProductInputActivity productInputActivity = new ProductInputActivity(chat_id,business);
                        presentFragment(productInputActivity);
                    }
                });
                showDialog(businessAlert);
            }
        });
        floatingButton = new FloatingActionButton(context);
        floatingButton.setSize(FloatingActionButton.SIZE_MINI);
        floatingButton.setBackgroundColor(Theme.getColor(Theme.key_chats_actionBackground));
        floatingButton.setScaleType(ImageView.ScaleType.CENTER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            floatingButton.setElevation(8);
        }
        floatingButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chats_actionIcon), PorterDuff.Mode.MULTIPLY));
        floatingButton.setImageResource(R.drawable.ic_create_shop);
        floatingButtonContainer.addView(floatingButton, LayoutHelper.createFrame((Build.VERSION.SDK_INT >= 21 ? 56 : 60), (Build.VERSION.SDK_INT >= 21 ? 56 : 60), Gravity.LEFT | Gravity.TOP, 10, 0, 10, 0));

        return fragmentView;
    }



    private void openOfferMessage(ShopDataSerializer.ProductOffer offer) {
        if (offer == null) {
            return;
        }
        String url = offer.message_link;
        Uri data;
        if (url.startsWith("tg:openmessage") || url.startsWith("tg://openmessage")) {
            url = url.replace("tg:openmessage", "tg://telegram.org").replace("tg://openmessage", "tg://telegram.org");
            data = Uri.parse(url);

            int push_user_id = 0;
            int push_msg_id = 0;

            String userID = data.getQueryParameter("user_id");
            String msgID = data.getQueryParameter("message_id");
            if (userID != null) {
                try {
                    push_user_id = Integer.parseInt(userID);
                } catch (NumberFormatException ignore) {
                }
            }

            if (msgID != null) {
                try {
                    push_msg_id = Integer.parseInt(msgID);
                } catch (NumberFormatException ignore) {
                }
            }

            Bundle args = new Bundle();
            args.putInt("user_id", push_user_id);
            if (push_msg_id != 0) {
                args.putInt("message_id", push_msg_id);
            }
            ChatActivity fragment = new ChatActivity(args);
            presentFragment(fragment);

        }

        }



    private void onItemClick(int index, View view, ShopDataSerializer.Product product, int a, int selectedMode) {
        if (product == null) {
            return;
        }

        if(selectedMode == 0){
            ProductEditFragment editFragment = new ProductEditFragment(chat_id,product,product.business_type);
            presentFragment(editFragment);
        }

    }

    private void setScrollY(float value) {
        actionBar.setTranslationY(value);
        for (int a = 0; a < mediaPages.length; a++) {
            mediaPages[a].listView.setPinnedSectionOffsetY((int) value);
        }
        fragmentView.invalidate();
    }


    @Override
    public void onResume() {
        super.onResume();
        scrolling = true;
        if (productAdapter != null) {
            productAdapter.notifyDataSetChanged();
        }
        if (offerAdapter != null) {
            offerAdapter.notifyDataSetChanged();
        }
        for (int a = 0; a < mediaPages.length; a++) {
            fixLayoutInternal(a);
        }
    }

    @Override
    public boolean isSwipeBackEnabled(MotionEvent event) {
        return swipeBackEnabled;
    }



    private void resetScroll() {
        if (actionBar.getTranslationY() == 0) {
            return;
        }
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(ObjectAnimator.ofFloat(this, SCROLL_Y, 0));
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.setDuration(180);
        animatorSet.start();
    }


    private class ProductAdapter extends RecyclerListView.SelectionAdapter{

        private Context mContext;

        public ProductAdapter(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return true;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ProductEditCell productEditCell = new ProductEditCell(mContext);
            productEditCell.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(productEditCell);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ProductEditCell productEditCell = (ProductEditCell) holder.itemView;
            ShopDataSerializer.Product product = products.get(position);
            productEditCell.setLink(product,true);
        }

        @Override
        public int getItemCount() {
            return products != null?products.size():0;
        }
    }

    private ArrayList<ShopDataSerializer.ProductOffer> productOffers = new ArrayList<>();

    private class OfferAdapter extends RecyclerListView.SelectionAdapter{


        private Context mContext;

        public OfferAdapter(Context mContext) {
            this.mContext = mContext;
        }


        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return true;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            OfferCell offerCell= new OfferCell(mContext);
            offerCell.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(offerCell);
        }


        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            OfferCell offerCell = (OfferCell)holder.itemView;
            ShopDataSerializer.ProductOffer productOffer = offers.get(position);
            offerCell.setOffer(productOffer);
        }

        @Override
        public int getItemCount() {
            return offers != null?offers.size():0;
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
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



    private boolean isLoadingProduct;
    private boolean loadingOffers;
    private int chat_id;
    private ArrayList<ShopDataSerializer.Product> products = new ArrayList<>();
    private ArrayList<ShopDataSerializer.ProductOffer> offers = new ArrayList<>();

    private void updateTabs() {
        if (scrollSlidingTextTabStrip == null) {
            return;
        }

        scrollSlidingTextTabStrip.addTextTab(0,"Listing");
        scrollSlidingTextTabStrip.addTextTab(1,"Offers");


        if (scrollSlidingTextTabStrip.getTabsCount() <= 1) {
            scrollSlidingTextTabStrip.setVisibility(View.GONE);
            actionBar.setExtraHeight(0);
        } else {
            scrollSlidingTextTabStrip.setVisibility(View.VISIBLE);
            actionBar.setExtraHeight(AndroidUtilities.dp(44));
        }
        int id = scrollSlidingTextTabStrip.getCurrentTabId();
        if (id >= 0) {
            mediaPages[0].selectedType = id;
        }
        scrollSlidingTextTabStrip.finishAddingTabs();


    }

    private void switchToCurrentSelectedMode(boolean animated) {
        for (int a = 0; a < mediaPages.length; a++) {
            mediaPages[a].listView.stopScroll();
        }
        int a = animated ? 1 : 0;
        RecyclerView.Adapter currentAdapter = mediaPages[a].listView.getAdapter();
        if (searching && searchWas) {
            if (animated) {
                if (mediaPages[a].selectedType == 1) {
                    searching = false;
                    searchWas = false;
                    switchToCurrentSelectedMode(true);
                    return;
                } else {
                    //String text = searchItem.getSearchField().getText().toString();
                    if (mediaPages[a].selectedType == 1) {
//                        if (productSearchAdapter != null) {
//                            // productSearchAdapter.search(text);
////                            if (currentAdapter != productSearchAdapter) {
////                                mediaPages[a].listView.setAdapter(productSearchAdapter);
////                            }
//                        }
                    }
                    if (mediaPages[a].emptyCell != null) {
                        mediaPages[a].emptyCell.setVisibility(View.GONE);
                    }
                }
            } else {
                if (mediaPages[a].selectedType == 1) {
//                    if (productSearchAdapter != null) {
//                        // productSearchAdapter.search(text);
////                        if (currentAdapter != productSearchAdapter) {
////                            mediaPages[a].listView.setAdapter(productSearchAdapter);
////                        }
//                    }
                }
                if (mediaPages[a].emptyCell != null) {
                    mediaPages[a].emptyCell.setVisibility(View.GONE);
                }
                if (searchItemState != 2 && mediaPages[a].emptyCell != null) {
                    mediaPages[a].emptyCell.setVisibility(View.GONE);
                }
            }

        } else {

            mediaPages[a].emptyCell.setVisibility(View.VISIBLE);
            mediaPages[a].listView.setPinnedHeaderShadowDrawable(null);


            if (mediaPages[a].selectedType == 0) {

                mediaPages[a].dialogsEmptyCell.setType(4);

                if (currentAdapter != productAdapter) {
                    mediaPages[a].listView.setAdapter(productAdapter);
                }
                mediaPages[a].listView.setPinnedHeaderShadowDrawable(pinnedHeaderShadowDrawable);

                if(!isLoadingProduct && products.isEmpty()){
                    ShopDataController.getInstance(UserConfig.selectedAccount).loadProductsForShop(chat_id,null,getClassGuid());
                    mediaPages[a].progressView.setVisibility(View.VISIBLE);
                }else{
                    mediaPages[a].progressView.setVisibility(View.GONE);
                    mediaPages[a].listView.setEmptyView(null);
                    mediaPages[a].emptyCell.setVisibility(View.GONE);
                }

            } else if (mediaPages[a].selectedType == 1) {

                mediaPages[a].dialogsEmptyCell.setType(14);

                if (currentAdapter != offerAdapter) {
                    mediaPages[a].listView.setAdapter(offerAdapter);
                }
                if(!loadingOffers && offers.isEmpty()){
                    ShopDataController.getInstance(UserConfig.selectedAccount).listOffer(chat_id,getClassGuid());
                    mediaPages[a].progressView.setVisibility(View.VISIBLE);
                    mediaPages[a].listView.setEmptyView(null);
                    mediaPages[a].emptyCell.setVisibility(View.GONE);
                }else{
                    mediaPages[a].progressView.setVisibility(View.GONE);
                    mediaPages[a].listView.setEmptyView(null);
                    mediaPages[a].emptyCell.setVisibility(View.GONE);
                }
            }
            mediaPages[a].listView.setVisibility(View.VISIBLE);


        }
    }


    private void fixLayoutInternal(int num) {
        WindowManager manager = (WindowManager) ApplicationLoader.applicationContext.getSystemService(Activity.WINDOW_SERVICE);
        int rotation = manager.getDefaultDisplay().getRotation();
        if (num == 0) {
            if (!AndroidUtilities.isTablet() && ApplicationLoader.applicationContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                //selectedMessagesCountTextView.setTextSize(18);
            } else {
               // selectedMessagesCountTextView.setTextSize(20);
            }
        }

        if (AndroidUtilities.isTablet()) {
            columnsCount = 3;
            mediaPages[num].emptyCell.setPadding(AndroidUtilities.dp(40), 0, AndroidUtilities.dp(40), AndroidUtilities.dp(128));
        } else {
            if (rotation == Surface.ROTATION_270 || rotation == Surface.ROTATION_90) {
                columnsCount = 6;
                mediaPages[num].emptyCell.setPadding(AndroidUtilities.dp(40), 0, AndroidUtilities.dp(40), 0);
            } else {
                columnsCount = 3;
                mediaPages[num].emptyCell.setPadding(AndroidUtilities.dp(40), 0, AndroidUtilities.dp(40), AndroidUtilities.dp(128));
            }
        }
        if (num == 0) {
            productAdapter.notifyDataSetChanged();
            fixScrollOffset();
        }
    }

    private void fixScrollOffset() {
        if (actionBar.getTranslationY() != 0f) {
            final RecyclerListView listView = mediaPages[0].listView;
            final View child = listView.getChildAt(0);
            if (child != null) {
                final int offset = (int) (child.getY() - (actionBar.getMeasuredHeight() + actionBar.getTranslationY() + additionalPadding));
                if (offset > 0) {
                    scrollWithoutActionBar(listView, offset);
                }
            }
        }
    }

    private void scrollWithoutActionBar(RecyclerView listView, int dy) {
        disableActionBarScrolling = true;
        listView.scrollBy(0, dy);
        disableActionBarScrolling = false;
    }


    //title


    private void showOfferDetail(ShopDataSerializer.ProductOffer offer){
        Log.i("aleanwatts","offer clicked was null");

        if(offer == null || getParentActivity() == null){
            return;
        }
        Log.i("aleanwatts","offer clicked");
        Activity context = getParentActivity();
        BottomSheet.Builder builder = new BottomSheet.Builder(context, false);
        builder.setApplyBottomPadding(false);

        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);

        TextView titleView = new TextView(context);
        titleView.setText("Offer Detail");
        titleView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        titleView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        container.addView(titleView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 16, 12, 0, 0));
        titleView.setOnTouchListener((v, event) -> true);

        OfferCell offerCell = new OfferCell(context);
        offerCell.setOffer(offer);
        container.addView(offerCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 0, 12, 0, 0));


        TextView buttonTextView = new TextView(context) {
            @Override
            public CharSequence getAccessibilityClassName() {
                return Button.class.getName();
            }
        };
        buttonTextView.setPadding(AndroidUtilities.dp(34), 0, AndroidUtilities.dp(34), 0);
        buttonTextView.setGravity(Gravity.CENTER);
        buttonTextView.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
        buttonTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        buttonTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        buttonTextView.setBackgroundDrawable(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(4), Theme.getColor(Theme.key_featuredStickers_addButton), Theme.getColor(Theme.key_featuredStickers_addButtonPressed)));
        container.addView(buttonTextView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, Gravity.LEFT | Gravity.BOTTOM, 16, 15, 16, 16));

        if(offer.status.equals("accepted")){
            buttonTextView.setText("Show Message");
        }else{
            buttonTextView.setText("Accept");
        }

        buttonTextView.setOnClickListener(v -> {
            if(offer.status.equals("accepted")){
            }else{
                openOfferMessage(offer);
            }
            builder.getDismissRunnable().run();
        });

        builder.setCustomView(container);
        showDialog(builder.create());
    }


    private class OfferCell extends FrameLayout {

        private TextView title;
        private TextView price;
        private TextView date;
        private TextView offer;
        private BackupImageView imageView;
        private AvatarDrawable avatarDrawable = new AvatarDrawable();

        private ShopDataSerializer.ProductOffer productOffer;

        public ShopDataSerializer.ProductOffer getProductOffer() {
            return productOffer;
        }

        public OfferCell(Context context) {
            super(context);
            imageView = new BackupImageView(context);
            addView(imageView, LayoutHelper.createFrame(46, 46, Gravity.START | Gravity.CENTER_VERTICAL, 12, 0, 16, 0));

            LinearLayout contentLayout = new LinearLayout(context);
            contentLayout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);

            title = new TextView(context);
            title.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            title.setTextSize(15);
            title.setTextColor(Color.BLACK);
            title.setLines(1);
            title.setEllipsize(TextUtils.TruncateAt.END);

            price = new TextView(context);
            price.setTextSize(15);
            price.setTextColor(Color.BLACK);

            linearLayout.addView(title, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f, Gravity.NO_GRAVITY, 0, 0, 16, 0));
            linearLayout.addView(price, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));
            contentLayout.addView(linearLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.START | Gravity.TOP, 0, 8, 0, 0));

            offer = new TextView(context);
            offer.setTextSize(13);
            offer.setTextColor(Color.BLACK);
            offer.setLines(1);
            offer.setEllipsize(TextUtils.TruncateAt.END);

            date = new TextView(context);
            date.setTextSize(13);
            date.setTextColor(Color.BLACK);

            linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);

            linearLayout.addView(offer, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f, Gravity.NO_GRAVITY, 0, 0, 8, 0));
            linearLayout.addView(date, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));
            contentLayout.addView(linearLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.START | Gravity.TOP, 0, 2, 0, 8));

            addView(contentLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.NO_GRAVITY, 72, 0, 12, 0));

            title.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
            price.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
            offer.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3));
            date.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3));
        }

        public void setOffer(ShopDataSerializer.ProductOffer productOffer){
            if(productOffer == null || productOffer.productSnip == null){
                return;
            }
            this.productOffer = productOffer;
            title.setText(productOffer.productSnip.title);
            AvatarDrawable avatarDrawable = new AvatarDrawable();
            avatarDrawable.setInfo(5,productOffer.productSnip.title,null);
            if(productOffer.productSnip.pictureSnip != null){
                imageView.setImage(productOffer.productSnip.pictureSnip.photo,"50_50",avatarDrawable);
            }else{
                imageView.setImage(null,null,avatarDrawable);
            }
            imageView.setRoundRadius(AndroidUtilities.dp(4));
            offer.setText("OFFERED " + ShopUtils.formatCurrency(productOffer.price));
            price.setText(ShopUtils.formatCurrency(productOffer.productSnip.price));
            try {
                Instant instant = Instant.parse(productOffer.created_at);
                date.setText(LocaleController.formatDateAudio(instant.getEpochSecond(), false));
            }catch (Exception e){
                date.setVisibility(GONE);
            }


        }



        public void setData(StatisticActivity.MemberData memberData) {
            avatarDrawable.setInfo(memberData.user);
            imageView.setImage(ImageLocation.getForUser(memberData.user, false), "50_50", avatarDrawable, memberData.user);
            imageView.setRoundRadius(AndroidUtilities.dp(46) >> 1);
            title.setText(memberData.user.first_name);
            offer.setText(memberData.description);

            price.setVisibility(View.GONE);
            date.setVisibility(View.GONE);
        }
    }



}
