//package org.plus.apps.business;
//
//import android.animation.Animator;
//import android.animation.AnimatorListenerAdapter;
//import android.animation.AnimatorSet;
//import android.animation.ObjectAnimator;
//import android.content.Context;
//import android.graphics.Canvas;
//import android.graphics.Paint;
//import android.graphics.PorterDuff;
//import android.graphics.PorterDuffColorFilter;
//import android.graphics.drawable.Drawable;a
//import android.os.Bundle;
//import android.util.TypedValue;
//import android.view.Gravity;
//import android.view.MotionEvent;
//import android.view.VelocityTracker;
//import android.view.View;
//import android.view.ViewConfiguration;
//import android.view.ViewGroup;
//import android.view.animation.Interpolator;
//import android.widget.EditText;
//import android.widget.FrameLayout;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.GridLayoutManager;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import org.plus.apps.business.data.ShopDataController;
//import org.plus.apps.business.data.ShopDataModels;
//import org.plus.apps.business.data.ShopDataSerializer;
//import org.plus.apps.business.ui.ProductDetailFragment;
//import org.plus.apps.business.ui.cells.ProductImageCell;
//import org.plus.apps.business.ui.cells.ProductCell;
//import org.plus.apps.business.ui.components.LoadingView;
//import org.plus.apps.business.ui.components.ShopMediaLayout;
//import org.plus.apps.business.ui.components.TagTextLayout;
//import org.telegram.messenger.AndroidUtilities;
//import org.telegram.messenger.LocaleController;
//import org.telegram.messenger.MediaController;
//import org.telegram.messenger.MediaDataController;
//import org.telegram.messenger.R;
//import org.telegram.messenger.UserConfig;
//import org.telegram.messenger.browser.Browser;
//import org.telegram.tgnet.ConnectionsManager;
//import org.telegram.ui.ActionBar.ActionBar;
//import org.telegram.ui.ActionBar.ActionBarMenu;
//import org.telegram.ui.ActionBar.ActionBarMenuItem;
//import org.telegram.ui.ActionBar.BackDrawable;
//import org.telegram.ui.ActionBar.BaseFragment;
//import org.telegram.ui.ActionBar.Theme;
//import org.telegram.ui.Cells.SharedAudioCell;
//import org.telegram.ui.Cells.SharedDocumentCell;
//import org.telegram.ui.Cells.SharedLinkCell;
//import org.telegram.ui.Cells.SharedPhotoVideoCell;
//import org.telegram.ui.Components.ClippingImageView;
//import org.telegram.ui.Components.EditTextBoldCursor;
//import org.telegram.ui.Components.FragmentContextView;
//import org.telegram.ui.Components.LayoutHelper;
//import org.telegram.ui.Components.RadialProgressView;
//import org.telegram.ui.Components.RecyclerAnimationScrollHelper;
//import org.telegram.ui.Components.RecyclerListView;
//import org.telegram.ui.Components.ScrollSlidingTextTabStrip;
//import org.telegram.ui.MediaActivity;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//
//public class GlobalSearchFragment extends BaseFragment {
//
//    private ArrayList<ProductCell> cellCache = new ArrayList<>(10);
//    private ArrayList<ProductCell> cache = new ArrayList<>(10);
//
//    private static class MediaPage extends FrameLayout {
//
//        private RecyclerListView listView;
//        private LinearLayout progressView;
//        private TextView emptyTextView;
//        private GridLayoutManager layoutManager;
//        private ImageView emptyImageView;
//        private LinearLayout emptyView;
//        private RadialProgressView progressBar;
//        private LoadingView loadingView;
//        private ClippingImageView animatingImageView;
//        private RecyclerAnimationScrollHelper scrollHelper;
//        private int selectedType;
//
//        public MediaPage(Context context) {
//            super(context);
//        }
//    }
//
//    private ArrayList<String> recentSearches = new ArrayList<>();
//    private ArrayList<ShopDataModels.Product> productResult = new ArrayList<>();
//    private HashMap<String, ShopDataModels.Product> productSearchResultKeys = new HashMap<>();
//
//    private ArrayList<ShopDataModels.Shop> shopResult = new ArrayList<>();
//    private HashMap<String, ShopDataModels.Shop> shopSearchResultKeys = new HashMap<>();
//
//    private MediaPage[] mediaPages = new MediaPage[2];
//    private RecyclerListView listView;
//
//    private ProductSearchAdapter productSearchAdapter;
//    private ShopSearchAdapter shopSearchAdapter;
//
//
//
//    private ActionBarMenuItem searchItem;
//
//    private static final Interpolator interpolator = t -> {
//        --t;
//        return t * t * t * t * t + 1.0F;
//    };
//    private ScrollSlidingTextTabStrip scrollSlidingTextTabStrip;
//    private AnimatorSet tabsAnimation;
//    private boolean tabsAnimationInProgress;
//    private boolean animatingForward;
//    private boolean backAnimation;
//
//    private boolean scrolling;
//
//    private boolean loadingShop;
//    private boolean loadingProducts;
//
//
//    @Override
//    public boolean onFragmentCreate() {
//        return super.onFragmentCreate();
//    }
//
//    @Override
//    public void onFragmentDestroy() {
//        super.onFragmentDestroy();
//    }
//
//    private Paint backgroundPaint = new Paint();
//
//
//    private int maximumVelocity;
//    private Drawable pinnedHeaderShadowDrawable;
//
//    private boolean swipeBackEnabled;
//
//    public boolean isSwipeBackEnabled() {
//        return swipeBackEnabled;
//    }
//
//    @Override
//    public View createView(Context context) {
//
//        for (int a = 0; a < 10; a++) {
//            cellCache.add(new ProductCell(context));
//        }
//
//        if (AndroidUtilities.isTablet()) {
//            actionBar.setOccupyStatusBar(false);
//        }
//        actionBar.setBackButtonDrawable(new BackDrawable(false));
//        actionBar.setAddToContainer(false);
//        actionBar.setClipContent(true);
//        actionBar.setExtraHeight(AndroidUtilities.dp(44));
//
//
//        ViewConfiguration configuration = ViewConfiguration.get(context);
//        maximumVelocity = configuration.getScaledMaximumFlingVelocity();
//
//
//        ActionBarMenu menu = actionBar.createMenu();
//        searchItem = menu.addItem(0, R.drawable.ic_ab_search).setIsSearchField(true).setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener() {
//            @Override
//            public void onSearchExpand() {
//
//            }
//
//            @Override
//            public boolean canCollapseSearch() {
//                finishFragment();
//                return false;
//            }
//
//            @Override
//            public void onTextChanged(EditText editText) {
//
//            }
//
//            @Override
//            public void onSearchPressed(EditText editText) {
//                //processSearch(editText);
//            }
//        });
//        EditTextBoldCursor editText = searchItem.getSearchField();
//        editText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
//        editText.setCursorColor(Theme.getColor(Theme.key_dialogTextBlack));
//        editText.setHintTextColor(Theme.getColor(Theme.key_chat_messagePanelHint));
//
//        pinnedHeaderShadowDrawable = context.getResources().getDrawable(R.drawable.photos_header_shadow);
//        pinnedHeaderShadowDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundGrayShadow), PorterDuff.Mode.MULTIPLY));
//
//
//
//        scrollSlidingTextTabStrip = new ScrollSlidingTextTabStrip(context);
//        actionBar.addView(scrollSlidingTextTabStrip, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 44, Gravity.LEFT | Gravity.BOTTOM));
//        scrollSlidingTextTabStrip.setDelegate(new ScrollSlidingTextTabStrip.ScrollSlidingTabStripDelegate() {
//            @Override
//            public void onPageSelected(int id, boolean forward) {
//                if (mediaPages[0].selectedType == id) {
//                    return;
//                }
//                swipeBackEnabled = id == scrollSlidingTextTabStrip.getFirstTabId();
//                mediaPages[1].selectedType = id;
//                mediaPages[1].setVisibility(View.VISIBLE);
//               // switchToCurrentSelectedMode(true);
//                animatingForward = forward;
//            }
//
//            @Override
//            public void onPageScrolled(float progress) {
//                if (progress == 1 && mediaPages[1].getVisibility() != View.VISIBLE) {
//                    return;
//                }
//                if (animatingForward) {
//                    mediaPages[0].setTranslationX(-progress * mediaPages[0].getMeasuredWidth());
//                    mediaPages[1].setTranslationX(mediaPages[0].getMeasuredWidth() - progress * mediaPages[0].getMeasuredWidth());
//                } else {
//                    mediaPages[0].setTranslationX(progress * mediaPages[0].getMeasuredWidth());
//                    mediaPages[1].setTranslationX(progress * mediaPages[0].getMeasuredWidth() - mediaPages[0].getMeasuredWidth());
//                }
//                if (progress == 1) {
//                    MediaPage tempPage = mediaPages[0];
//                    mediaPages[0] = mediaPages[1];
//                    mediaPages[1] = tempPage;
//                    mediaPages[1].setVisibility(View.GONE);
//                }
//            }
//        });
//
//        FrameLayout frameLayout;
//        fragmentView = frameLayout = new FrameLayout(context) {
//
//            private int startedTrackingPointerId;
//            private boolean startedTracking;
//            private boolean maybeStartTracking;
//            private int startedTrackingX;
//            private int startedTrackingY;
//            private VelocityTracker velocityTracker;
//            private boolean globalIgnoreLayout;
//
//            private boolean prepareForMoving(MotionEvent ev, boolean forward) {
//                int id = scrollSlidingTextTabStrip.getNextPageId(forward);
//                if (id < 0) {
//                    return false;
//                }
//                getParent().requestDisallowInterceptTouchEvent(true);
//                maybeStartTracking = false;
//                startedTracking = true;
//                startedTrackingX = (int) ev.getX();
//                actionBar.setEnabled(false);
//                scrollSlidingTextTabStrip.setEnabled(false);
//                mediaPages[1].selectedType = id;
//                mediaPages[1].setVisibility(View.VISIBLE);
//                animatingForward = forward;
//                switchToCurrentSelectedMode(true);
//                if (forward) {
//                    mediaPages[1].setTranslationX(mediaPages[0].getMeasuredWidth());
//                } else {
//                    mediaPages[1].setTranslationX(-mediaPages[0].getMeasuredWidth());
//                }
//                return true;
//            }
//
//            @Override
//            public void forceHasOverlappingRendering(boolean hasOverlappingRendering) {
//                super.forceHasOverlappingRendering(hasOverlappingRendering);
//            }
//
//            @Override
//            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//                int widthSize = MeasureSpec.getSize(widthMeasureSpec);
//                int heightSize = MeasureSpec.getSize(heightMeasureSpec);
//
//                setMeasuredDimension(widthSize, heightSize);
//
//                measureChildWithMargins(actionBar, widthMeasureSpec, 0, heightMeasureSpec, 0);
//                int actionBarHeight = actionBar.getMeasuredHeight();
//                globalIgnoreLayout = true;
//                for (int a = 0; a < mediaPages.length; a++) {
//                    if (mediaPages[a] == null) {
//                        continue;
//                    }
//                    if (mediaPages[a].listView != null) {
//                        mediaPages[a].listView.setPadding(0, actionBarHeight + 0, 0, AndroidUtilities.dp(4));
//                    }
//                    if (mediaPages[a].emptyView != null) {
//                        mediaPages[a].emptyView.setPadding(0, actionBarHeight + 0, 0, 0);
//                    }
//                    if (mediaPages[a].progressView != null) {
//                        mediaPages[a].progressView.setPadding(0, actionBarHeight + 0, 0, 0);
//                    }
//                }
//                globalIgnoreLayout = false;
//
//                int childCount = getChildCount();
//                for (int i = 0; i < childCount; i++) {
//                    View child = getChildAt(i);
//                    if (child == null || child.getVisibility() == GONE || child == actionBar) {
//                        continue;
//                    }
//                    measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
//                }
//            }
//
//
//
//            @Override
//            public void setPadding(int left, int top, int right, int bottom) {
//                int actionBarHeight = actionBar.getMeasuredHeight();
//                for (int a = 0; a < mediaPages.length; a++) {
//                    if (mediaPages[a] == null) {
//                        continue;
//                    }
//                    if (mediaPages[a].emptyView != null) {
//                        mediaPages[a].emptyView.setPadding(0, actionBarHeight + 0, 0, 0);
//                    }
//                    if (mediaPages[a].progressView != null) {
//                        mediaPages[a].progressView.setPadding(0, actionBarHeight + 0, 0, 0);
//                    }
//                    if (mediaPages[a].listView != null) {
//                        mediaPages[a].listView.setPadding(0, actionBarHeight + 0, 0, AndroidUtilities.dp(4));
//                        mediaPages[a].listView.checkSection();
//                    }
//                }
//                fixScrollOffset();
//            }
//
//            @Override
//            protected void dispatchDraw(Canvas canvas) {
//                super.dispatchDraw(canvas);
//                if (parentLayout != null) {
//                    parentLayout.drawHeaderShadow(canvas, actionBar.getMeasuredHeight() + (int) actionBar.getTranslationY());
//                }
//            }
//
//            @Override
//            public void requestLayout() {
//                if (globalIgnoreLayout) {
//                    return;
//                }
//                super.requestLayout();
//            }
//
//            public boolean checkTabsAnimationInProgress() {
//                if (tabsAnimationInProgress) {
//                    boolean cancel = false;
//                    if (backAnimation) {
//                        if (Math.abs(mediaPages[0].getTranslationX()) < 1) {
//                            mediaPages[0].setTranslationX(0);
//                            mediaPages[1].setTranslationX(mediaPages[0].getMeasuredWidth() * (animatingForward ? 1 : -1));
//                            cancel = true;
//                        }
//                    } else if (Math.abs(mediaPages[1].getTranslationX()) < 1) {
//                        mediaPages[0].setTranslationX(mediaPages[0].getMeasuredWidth() * (animatingForward ? -1 : 1));
//                        mediaPages[1].setTranslationX(0);
//                        cancel = true;
//                    }
//                    if (cancel) {
//                        if (tabsAnimation != null) {
//                            tabsAnimation.cancel();
//                            tabsAnimation = null;
//                        }
//                        tabsAnimationInProgress = false;
//                    }
//                    return tabsAnimationInProgress;
//                }
//                return false;
//            }
//
//            @Override
//            public boolean onInterceptTouchEvent(MotionEvent ev) {
//                return checkTabsAnimationInProgress() || scrollSlidingTextTabStrip.isAnimatingIndicator() || onTouchEvent(ev);
//            }
//
//            @Override
//            protected void onDraw(Canvas canvas) {
//                backgroundPaint.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
//                canvas.drawRect(0, actionBar.getMeasuredHeight() + actionBar.getTranslationY(), getMeasuredWidth(), getMeasuredHeight(), backgroundPaint);
//            }
//
//            @Override
//            public boolean onTouchEvent(MotionEvent ev) {
//                if (!parentLayout.checkTransitionAnimation() && !checkTabsAnimationInProgress()) {
//                    if (ev != null) {
//                        if (velocityTracker == null) {
//                            velocityTracker = VelocityTracker.obtain();
//                        }
//                        velocityTracker.addMovement(ev);
//                    }
//                    if (ev != null && ev.getAction() == MotionEvent.ACTION_DOWN && !startedTracking && !maybeStartTracking) {
//                        startedTrackingPointerId = ev.getPointerId(0);
//                        maybeStartTracking = true;
//                        startedTrackingX = (int) ev.getX();
//                        startedTrackingY = (int) ev.getY();
//                        velocityTracker.clear();
//                    } else if (ev != null && ev.getAction() == MotionEvent.ACTION_MOVE && ev.getPointerId(0) == startedTrackingPointerId) {
//                        int dx = (int) (ev.getX() - startedTrackingX);
//                        int dy = Math.abs((int) ev.getY() - startedTrackingY);
//                        if (startedTracking && (animatingForward && dx > 0 || !animatingForward && dx < 0)) {
//                            if (!prepareForMoving(ev, dx < 0)) {
//                                maybeStartTracking = true;
//                                startedTracking = false;
//                                mediaPages[0].setTranslationX(0);
//                                mediaPages[1].setTranslationX(animatingForward ? mediaPages[0].getMeasuredWidth() : -mediaPages[0].getMeasuredWidth());
//                                scrollSlidingTextTabStrip.selectTabWithId(mediaPages[1].selectedType, 0);
//                            }
//                        }
//                        if (maybeStartTracking && !startedTracking) {
//                            float touchSlop = AndroidUtilities.getPixelsInCM(0.3f, true);
//                            if (Math.abs(dx) >= touchSlop && Math.abs(dx) > dy) {
//                                prepareForMoving(ev, dx < 0);
//                            }
//                        } else if (startedTracking) {
//                            mediaPages[0].setTranslationX(dx);
//                            if (animatingForward) {
//                                mediaPages[1].setTranslationX(mediaPages[0].getMeasuredWidth() + dx);
//                            } else {
//                                mediaPages[1].setTranslationX(dx - mediaPages[0].getMeasuredWidth());
//                            }
//                            float scrollProgress = Math.abs(dx) / (float) mediaPages[0].getMeasuredWidth();
//                            scrollSlidingTextTabStrip.selectTabWithId(mediaPages[1].selectedType, scrollProgress);
//                        }
//                    } else if (ev == null || ev.getPointerId(0) == startedTrackingPointerId && (ev.getAction() == MotionEvent.ACTION_CANCEL || ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_POINTER_UP)) {
//                        velocityTracker.computeCurrentVelocity(1000, maximumVelocity);
//                        float velX;
//                        float velY;
//                        if (ev != null && ev.getAction() != MotionEvent.ACTION_CANCEL) {
//                            velX = velocityTracker.getXVelocity();
//                            velY = velocityTracker.getYVelocity();
//                            if (!startedTracking) {
//                                if (Math.abs(velX) >= 3000 && Math.abs(velX) > Math.abs(velY)) {
//                                    prepareForMoving(ev, velX < 0);
//                                }
//                            }
//                        } else {
//                            velX = 0;
//                            velY = 0;
//                        }
//                        if (startedTracking) {
//                            float x = mediaPages[0].getX();
//                            tabsAnimation = new AnimatorSet();
//                            backAnimation = Math.abs(x) < mediaPages[0].getMeasuredWidth() / 3.0f && (Math.abs(velX) < 3500 || Math.abs(velX) < Math.abs(velY));
//                            float distToMove;
//                            float dx;
//                            if (backAnimation) {
//                                dx = Math.abs(x);
//                                if (animatingForward) {
//                                    tabsAnimation.playTogether(
//                                            ObjectAnimator.ofFloat(mediaPages[0], View.TRANSLATION_X, 0),
//                                            ObjectAnimator.ofFloat(mediaPages[1], View.TRANSLATION_X, mediaPages[1].getMeasuredWidth())
//                                    );
//                                } else {
//                                    tabsAnimation.playTogether(
//                                            ObjectAnimator.ofFloat(mediaPages[0], View.TRANSLATION_X, 0),
//                                            ObjectAnimator.ofFloat(mediaPages[1], View.TRANSLATION_X, -mediaPages[1].getMeasuredWidth())
//                                    );
//                                }
//                            } else {
//                                dx = mediaPages[0].getMeasuredWidth() - Math.abs(x);
//                                if (animatingForward) {
//                                    tabsAnimation.playTogether(
//                                            ObjectAnimator.ofFloat(mediaPages[0], View.TRANSLATION_X, -mediaPages[0].getMeasuredWidth()),
//                                            ObjectAnimator.ofFloat(mediaPages[1], View.TRANSLATION_X, 0)
//                                    );
//                                } else {
//                                    tabsAnimation.playTogether(
//                                            ObjectAnimator.ofFloat(mediaPages[0], View.TRANSLATION_X, mediaPages[0].getMeasuredWidth()),
//                                            ObjectAnimator.ofFloat(mediaPages[1], View.TRANSLATION_X, 0)
//                                    );
//                                }
//                            }
//                            tabsAnimation.setInterpolator(interpolator);
//
//                            int width = getMeasuredWidth();
//                            int halfWidth = width / 2;
//                            float distanceRatio = Math.min(1.0f, 1.0f * dx / (float) width);
//                            float distance = (float) halfWidth + (float) halfWidth * AndroidUtilities.distanceInfluenceForSnapDuration(distanceRatio);
//                            velX = Math.abs(velX);
//                            int duration;
//                            if (velX > 0) {
//                                duration = 4 * Math.round(1000.0f * Math.abs(distance / velX));
//                            } else {
//                                float pageDelta = dx / getMeasuredWidth();
//                                duration = (int) ((pageDelta + 1.0f) * 100.0f);
//                            }
//                            duration = Math.max(150, Math.min(duration, 600));
//
//                            tabsAnimation.setDuration(duration);
//                            tabsAnimation.addListener(new AnimatorListenerAdapter() {
//                                @Override
//                                public void onAnimationEnd(Animator animator) {
//                                    tabsAnimation = null;
//                                    if (backAnimation) {
//                                        mediaPages[1].setVisibility(View.GONE);
//
//                                    } else {
//                                        MediaPage tempPage = mediaPages[0];
//                                        mediaPages[0] = mediaPages[1];
//                                        mediaPages[1] = tempPage;
//                                        mediaPages[1].setVisibility(View.GONE);
//                                        swipeBackEnabled = mediaPages[0].selectedType == scrollSlidingTextTabStrip.getFirstTabId();
//                                        scrollSlidingTextTabStrip.selectTabWithId(mediaPages[0].selectedType, 1.0f);
//                                    }
//                                    tabsAnimationInProgress = false;
//                                    maybeStartTracking = false;
//                                    startedTracking = false;
//                                    actionBar.setEnabled(true);
//                                    scrollSlidingTextTabStrip.setEnabled(true);
//                                }
//                            });
//                            tabsAnimation.start();
//                            tabsAnimationInProgress = true;
//                            startedTracking = false;
//                        } else {
//                            maybeStartTracking = false;
//                            actionBar.setEnabled(true);
//                            scrollSlidingTextTabStrip.setEnabled(true);
//                        }
//                        if (velocityTracker != null) {
//                            velocityTracker.recycle();
//                            velocityTracker = null;
//                        }
//                    }
//                    return startedTracking;
//                }
//                return false;
//            }
//        };
//        frameLayout.setWillNotDraw(false);
//
//
//        int scrollToPositionOnRecreate = -1;
//        int scrollToOffsetOnRecreate = 0;
//
//        for (int a = 0; a < mediaPages.length; a++) {
//            if (a == 0) {
//                if (mediaPages[a] != null && mediaPages[a].layoutManager != null) {
//                    scrollToPositionOnRecreate = mediaPages[a].layoutManager.findFirstVisibleItemPosition();
//                    if (scrollToPositionOnRecreate != mediaPages[a].layoutManager.getItemCount() - 1) {
//                        RecyclerListView.Holder holder = (RecyclerListView.Holder) mediaPages[a].listView.findViewHolderForAdapterPosition(scrollToPositionOnRecreate);
//                        if (holder != null) {
//                            scrollToOffsetOnRecreate = holder.itemView.getTop();
//                        } else {
//                            scrollToPositionOnRecreate = -1;
//                        }
//                    } else {
//                        scrollToPositionOnRecreate = -1;
//                    }
//                }
//            }
//            final MediaPage mediaPage = new MediaPage(context) {
//                @Override
//                public void setTranslationX(float translationX) {
//                    super.setTranslationX(translationX);
//                    if (tabsAnimationInProgress) {
//                        if (mediaPages[0] == this) {
//                            float scrollProgress = Math.abs(mediaPages[0].getTranslationX()) / (float) mediaPages[0].getMeasuredWidth();
//                            scrollSlidingTextTabStrip.selectTabWithId(mediaPages[1].selectedType, scrollProgress);
//                        }
//                    }
//                }
//            };
//            frameLayout.addView(mediaPage, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.TOP, 0, 48, 0, 0));
//            mediaPages[a] = mediaPage;
//
//            final GridLayoutManager layoutManager = mediaPages[a].layoutManager = new GridLayoutManager(context, 2);
//
//            mediaPages[a].listView = new RecyclerListView(context) {
//                @Override
//                protected void onLayout(boolean changed, int l, int t, int r, int b) {
//                    super.onLayout(changed, l, t, r, b);
//                    checkLoadMoreScroll(mediaPage, mediaPage.listView, layoutManager);
//                }
//
//                @Override
//                protected boolean allowSelectChildAtPosition(View child) {
//                    if(child instanceof TagTextLayout){
//                        return false;
//                    }
//                    return super.allowSelectChildAtPosition(child);
//                }
//            };
//            mediaPages[a].listView.setFocusable(true);
//            mediaPages[a].listView.setFocusableInTouchMode(true);
//            mediaPages[a].listView.setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING);
//            mediaPages[a].listView.setPinnedSectionOffsetY(-AndroidUtilities.dp(2));
//            mediaPages[a].listView.setPadding(0, AndroidUtilities.dp(2), 0, 0);
//            mediaPages[a].listView.setItemAnimator(null);
//            mediaPages[a].listView.setClipToPadding(false);
//            mediaPages[a].listView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
//
//            mediaPages[a].listView.setOnItemClickListener((view, position) -> {
//
//                if(mediaPage.selectedType == 0){
//                    if( mediaPage.listView.getAdapter() instanceof ShopMediaLayout.ProductAdapter){
//                        ShopMediaLayout.ProductAdapter productAdapter = (ShopMediaLayout.ProductAdapter)mediaPage.listView.getAdapter();
//                        ShopDataSerializer.Product product =  productAdapter.getItem(position);
//                        if(product != null){
//                            Bundle bundle = new Bundle();
//                            bundle.putInt("chat_id",chat_id);
//                            bundle.putInt("item_id",product.id);
//
//                            ProductDetailFragment detailFragment = new ProductDetailFragment(bundle);
//                            detailFragment.setCurrentShop(currentShop);
//                            profileActivity.presentFragment(detailFragment);
//                        }
//                    }
//                }else if(mediaPage.selectedType == 1){
//
//
//                }else if(mediaPage.selectedType == 2){
//
//                    if(mediaPage.listView.getAdapter() instanceof ShopMediaLayout.AboutAdapter){
//                        if(position == aboutWebsiteRow){
//                            Browser.openUrl(context,currentShop.website);
//                        }
//                    }
//                }
//            });
//
//            mediaPages[a].listView.setLayoutManager(layoutManager);
//
//            mediaPages[a].layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
//                @Override
//                public int getSpanSize(int position) {
//                    RecyclerView.Adapter adapter = mediaPage.listView.getAdapter();
//                    return 0;
//                }
//            });
//
//
//            mediaPages[a].listView.addItemDecoration(new RecyclerView.ItemDecoration() {
//                @Override
//                public void getItemOffsets(android.graphics.Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
//                    RecyclerListView.Holder holder = (RecyclerListView.Holder) parent.getChildViewHolder(view);
//                    RecyclerView.Adapter adapter = mediaPage.listView.getAdapter();
//                    if(adapter != null){
//                        if(adapter instanceof ProductSearchAdapter) {
//                            if(holder.getItemViewType() == 4){
//                                outRect.left = AndroidUtilities.dp(4);
//                                outRect.right = AndroidUtilities.dp(4);
//                                outRect.bottom = AndroidUtilities.dp (4);
//                                outRect.top = AndroidUtilities.dp(4);
//                            }
//                        }else{
//                            outRect.left = AndroidUtilities.dp(0);
//                            outRect.right = AndroidUtilities.dp(0);
//                            outRect.bottom = AndroidUtilities.dp (0);
//                            outRect.top = AndroidUtilities.dp(0);
//                        }
//                    }
//
//                }
//
//            });
//
//            mediaPages[a].listView.setPadding(AndroidUtilities.dp(8),0,AndroidUtilities.dp(8),0);
//            mediaPages[a].addView(mediaPages[a].listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
//
//            mediaPages[a].listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
//                @Override
//                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//
//                    scrolling = newState != RecyclerView.SCROLL_STATE_IDLE;
//                }
//
//                @Override
//                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                   // checkLoadMoreScroll(mediaPage, recyclerView, layoutManager);
//                }
//            });
//            if (a == 0 && scrollToPositionOnRecreate != -1) {
//                layoutManager.scrollToPositionWithOffset(scrollToPositionOnRecreate, scrollToOffsetOnRecreate);
//            }
//
//            final RecyclerListView listView = mediaPages[a].listView;
//
//            mediaPages[a].animatingImageView = new ClippingImageView(context) {
//                @Override
//                public void invalidate() {
//                    super.invalidate();
//                    listView.invalidate();
//                }
//            };
//
//
//            mediaPages[a].animatingImageView.setVisibility(View.GONE);
//            mediaPages[a].listView.addOverlayView(mediaPages[a].animatingImageView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
//
//            mediaPages[a].emptyView = new LinearLayout(context);
//            mediaPages[a].emptyView.setWillNotDraw(false);
//            mediaPages[a].emptyView.setOrientation(LinearLayout.VERTICAL);
//            mediaPages[a].emptyView.setGravity(Gravity.CENTER);
//            mediaPages[a].emptyView.setVisibility(View.GONE);
//            mediaPages[a].addView(mediaPages[a].emptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
//
//            mediaPages[a].emptyView.setOnTouchListener((v, event) -> true);
//
//            mediaPages[a].emptyImageView = new ImageView(context);
//            mediaPages[a].emptyView.addView(mediaPages[a].emptyImageView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));
//
//            mediaPages[a].emptyTextView = new TextView(context);
//            mediaPages[a].emptyTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
//            mediaPages[a].emptyTextView.setGravity(Gravity.CENTER);
//            mediaPages[a].emptyTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
//            mediaPages[a].emptyTextView.setPadding(AndroidUtilities.dp(40), 0, AndroidUtilities.dp(40), AndroidUtilities.dp(128));
//            mediaPages[a].emptyView.addView(mediaPages[a].emptyTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 0, 24, 0, 0));
//
//            mediaPages[a].progressView = new LinearLayout(context) {
//                @Override
//                protected void onDraw(Canvas canvas) {
//                    backgroundPaint.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
//                    canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), backgroundPaint);
//                }
//            };
//            mediaPages[a].progressView.setWillNotDraw(false);
//            mediaPages[a].progressView.setGravity(Gravity.CENTER);
//            mediaPages[a].progressView.setOrientation(LinearLayout.VERTICAL);
//            mediaPages[a].progressView.setVisibility(View.GONE);
//            mediaPages[a].addView(mediaPages[a].progressView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
//
//            mediaPages[a].loadingView = new LoadingView(context){
//
//                @Override
//                public int getViewType() {
//                    return super.getViewType();
//                }
//
//                @Override
//                public int getColumnsCount() {
//                    return super.getColumnsCount();
//                }
//            };
//            mediaPages[a].progressView.addView(mediaPages[a].loadingView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));
//
//
//            //mediaPages[a].progressBar = new RadialProgressView(context);
//            // mediaPages[a].progressView.addView(mediaPages[a].progressBar, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));
//
//            if (a != 0) {
//                mediaPages[a].setVisibility(View.GONE);
//            }
//
//            mediaPages[a].scrollHelper = new RecyclerAnimationScrollHelper(mediaPages[a].listView, mediaPages[a].layoutManager);
//        }
//        frameLayout.addView(scrollSlidingTextTabStrip, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.LEFT | Gravity.TOP));
//
//
//        frameLayout.addView(actionBar, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
//
//        updateTabs();
//        switchToCurrentSelectedMode(false);
//        swipeBackEnabled = scrollSlidingTextTabStrip.getCurrentTabId() == scrollSlidingTextTabStrip.getFirstTabId();
//
//        return fragmentView;
//
//
//
//        return super.createView(context);
//    }
//
////    private void processSearch(EditText editText) {
////        if (editText.getText().length() == 0) {
////            return;
////        }
////        String text = editText.getText().toString();
////        for (int a = 0, N = recentSearches.size(); a < N; a++) {
////            String str = recentSearches.get(a);
////            if (str.equalsIgnoreCase(text)) {
////                recentSearches.remove(a);
////                break;
////            }
////        }
////        recentSearches.add(0, text);
////        while (recentSearches.size() > 20) {
////            recentSearches.remove(recentSearches.size() - 1);
////        }
////        saveRecentSearch();
////        searchResult.clear();
////        searchResultKeys.clear();
////        imageSearchEndReached = true;
////        searchImages(type == 1, text, "", true);
////        lastSearchString = text;
////        if (lastSearchString.length() == 0) {
////            lastSearchString = null;
////            emptyView.setText(LocaleController.getString("NoRecentSearches", R.string.NoRecentSearches));
////        } else {
////            emptyView.setText(LocaleController.getString("NoResult", R.string.NoResult));
////        }
////        updateSearchInterface();
////    }
//
//
//    private void updateTabs() {
//        if (scrollSlidingTextTabStrip == null) {
//            return;
//        }
//        if (!scrollSlidingTextTabStrip.hasTab(0)) {
//            scrollSlidingTextTabStrip.addTextTab(0, "Products");
//        }
//
//        if (!scrollSlidingTextTabStrip.hasTab(1)) {
//            scrollSlidingTextTabStrip.addTextTab(1, "Shops");
//        }
//
//        int id = scrollSlidingTextTabStrip.getCurrentTabId();
//        if (id >= 0) {
//            mediaPages[0].selectedType = id;
//        }
//        scrollSlidingTextTabStrip.finishAddingTabs();
//    }
//
//
//    private void switchToCurrentSelectedMode(boolean animated) {
//        for (int a = 0; a < mediaPages.length; a++) {
//            mediaPages[a].listView.stopScroll();
//        }
//        int a = animated ? 1 : 0;
//        RecyclerView.Adapter currentAdapter = mediaPages[a].listView.getAdapter();
//
//       if (mediaPages[a].emptyTextView != null) {
//                mediaPages[a].emptyTextView.setText(LocaleController.getString("NoResult", R.string.NoResult));
//                mediaPages[a].emptyTextView.setPadding(AndroidUtilities.dp(40), 0, AndroidUtilities.dp(40), AndroidUtilities.dp(30));
//                mediaPages[a].emptyTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
//                mediaPages[a].emptyImageView.setVisibility(View.GONE);
//            }
//
//            if (mediaPages[a].selectedType == 0) {
//                if (currentAdapter != productSearchAdapter) {
//                    mediaPages[a].listView.setAdapter(productAdapter);
//                }
//                mediaPages[a].listView.setPinnedHeaderShadowDrawable(pinnedHeaderShadowDrawable);
//                mediaPages[a].emptyImageView.setImageResource(R.drawable.smiles_info);
//                mediaPages[a].emptyTextView.setText("No  listing found!");
//
//
//
//                if(!isLoadingProduct && products.isEmpty()){
//                    ShopDataController.getInstance(UserConfig.selectedAccount).loadProductsForShop(chat_id,currentSort,profileActivity.getClassGuid());
//                    mediaPages[a].progressView.setVisibility(View.VISIBLE);
//                    mediaPages[a].listView.setEmptyView(null);
//                    mediaPages[a].emptyView.setVisibility(View.GONE);
//                }else{
//                    mediaPages[a].progressView.setVisibility(View.GONE);
//                    mediaPages[a].listView.setEmptyView(mediaPages[a].emptyView);
//                }
//
//
//            } else if (mediaPages[a].selectedType == 1) {
//                if (currentAdapter != shopSearchAdapter) {
//                    mediaPages[a].listView.setAdapter(shopSearchAdapter);
//                }
//                mediaPages[a].emptyImageView.setImageResource(R.drawable.smiles_info);
//                mediaPages[a].emptyTextView.setText("No  Review yet!");
//
//                if(!loadingReview && reviews.isEmpty()){
//                    ShopDataController.getInstance(UserConfig.selectedAccount).loadShopReview(false,chat_id,currentReviewSort,profileActivity.getClassGuid());
//                    mediaPages[a].progressView.setVisibility(View.VISIBLE);
//                    mediaPages[a].listView.setEmptyView(null);
//                    mediaPages[a].emptyView.setVisibility(View.GONE);
//                }else{
//                    mediaPages[a].progressView.setVisibility(View.GONE);
//                    mediaPages[a].listView.setEmptyView(mediaPages[a].emptyView);
//                }
//
//            }
//
//            mediaPages[a].emptyTextView.setPadding(AndroidUtilities.dp(40), 0, AndroidUtilities.dp(40), AndroidUtilities.dp(128));
//            mediaPages[a].listView.setVisibility(View.VISIBLE);
//
//    }
//
//
//    @Override
//    public void onResume() {
//        super.onResume();
//    }
//
//    private static class ShopSearchAdapter extends RecyclerListView.SelectionAdapter{
//
//        @Override
//        public boolean isEnabled(RecyclerView.ViewHolder holder) {
//            return false;
//        }
//
//        @NonNull
//        @Override
//        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//            return null;
//        }
//
//        @Override
//        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//
//        }
//
//        @Override
//        public int getItemCount() {
//            return 0;
//        }
//    }
//
//    private static class ProductSearchAdapter extends RecyclerListView.SelectionAdapter{
//
//        @Override
//        public boolean isEnabled(RecyclerView.ViewHolder holder) {
//
//
//        }
//
//        @NonNull
//        @Override
//        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//            return null;
//        }
//
//        @Override
//        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//
//        }
//
//        @Override
//        public int getItemCount() {
//            return 0;
//        }
//    }
//}
