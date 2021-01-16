//package org.plus.apps.business.ui;
//
//import android.animation.Animator;
//import android.animation.AnimatorListenerAdapter;
//import android.animation.AnimatorSet;
//import android.animation.ObjectAnimator;
//import android.content.Context;
//import android.graphics.Canvas;
//import android.graphics.Paint;
//import android.graphics.drawable.Drawable;
//import android.util.Log;
//import android.util.TypedValue;
//import android.view.Gravity;
//import android.view.MotionEvent;
//import android.view.VelocityTracker;
//import android.view.View;
//import android.view.ViewConfiguration;
//import android.view.ViewGroup;
//import android.view.WindowManager;
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
//import org.plus.apps.business.ProductCell;
//import org.plus.apps.business.ShopUtils;
//import org.plus.apps.business.data.ShopDataController;
//import org.plus.apps.business.data.ShopDataSerializer;
//import org.plus.apps.business.ui.cells.FilterHorizontalLayout;
//import org.plus.apps.business.ui.components.BusinessAlert;
//import org.plus.apps.business.ui.components.LoadingView;
//import org.plus.apps.business.ui.components.ShopMediaLayout;
//import org.plus.apps.business.ui.components.ShopsEmptyCell;
//import org.plus.apps.business.ui.components.TagTextLayout;
//import org.plus.net.APIError;
//import org.telegram.messenger.AndroidUtilities;
//import org.telegram.messenger.R;
//import org.telegram.messenger.SharedConfig;
//import org.telegram.messenger.Utilities;
//import org.telegram.tgnet.ConnectionsManager;
//import org.telegram.ui.ActionBar.ActionBar;
//import org.telegram.ui.ActionBar.ActionBarMenu;
//import org.telegram.ui.ActionBar.ActionBarMenuItem;
//import org.telegram.ui.ActionBar.BaseFragment;
//import org.telegram.ui.ActionBar.Theme;
//import org.telegram.ui.ActionBar.ThemeDescription;
//import org.telegram.ui.Cells.LoadingCell;
//import org.telegram.ui.Components.EditTextBoldCursor;
//import org.telegram.ui.Components.EmptyTextProgressView;
//import org.telegram.ui.Components.LayoutHelper;
//import org.telegram.ui.Components.RadialProgressView;
//import org.telegram.ui.Components.RecyclerListView;
//import org.telegram.ui.Components.ScrollSlidingTextTabStrip;
//import org.telegram.ui.Components.SizeNotifierFrameLayout;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//
//
//public class ShopSearchActivity extends BaseFragment {
//
//    public static final String TAG = ShopSearchActivity.class.getSimpleName();
//
//    private ArrayList<ProductCell> cellCache = new ArrayList<>(10);
//    private ArrayList<ProductCell> cache = new ArrayList<>(10);
//
//    private HashMap<String,String> queryLoadMore = new HashMap<>();
//
//    private ShopDataSerializer.ProductType.Sort sort = new ShopDataSerializer.ProductType.Sort();
//    private HashMap<String,Object> filter = new HashMap<>();
//
//
//    private ArrayList<ShopDataSerializer.Product> productResult = new ArrayList<>();
//    private ArrayList<ShopDataSerializer.Shop> shopResult = new ArrayList<>();
//
//    private static class ViewPage extends FrameLayout {
//
//        private RecyclerListView listView;
//        private int selectedType;
//        private GridLayoutManager layoutManager;
//
//        private LinearLayout emptyView;
//        private TextView emptyTextView;
//        private ImageView emptyImageView;
//
//        private LinearLayout progressView;
//        private RadialProgressView progressBar;
//
//        public ViewPage(Context context) {
//            super(context);
//        }
//    }
//
//    private ActionBarMenuItem searchItem;
//
//    private boolean swipeBackEnabled = true;
//
//    private final static int search_button = 0;
//
//    private Paint backgroundPaint = new Paint();
//    private ScrollSlidingTextTabStrip scrollSlidingTextTabStrip;
//    private ViewPage[] viewPages = new ViewPage[2];
//    private AnimatorSet tabsAnimation;
//    private boolean tabsAnimationInProgress;
//    private boolean animatingForward;
//    private boolean backAnimation;
//    private int maximumVelocity;
//    private static final Interpolator interpolator = t -> {
//        --t;
//        return t * t * t * t * t + 1.0F;
//    };
//
//    private ProductAdapter productAdapter;
//    private SearchShopAdapter shopAdapter;
//
//    private Runnable searchRunnable;
//
//    private boolean searchInProgress;
//    private boolean searching;
//    private String lastSearchQuery;
//
//    private int currentRequestNum;
//
//
//    private boolean searchingProduct;
//    private boolean searchingShop;
//
//    private boolean productEndReached;
//
//    private int rowCount;
//    private int filterRow;
//    private int productStartRow;
//    private int productEndRow;
//    private int progressRow;
//
//    private int shopStartRow;
//    private int shopEndRow;
//    private int shopProgressRow;
//
//    private void updateRow(int id){
//
//        rowCount = 0;
//        filterRow= -1;
//        productStartRow= -1;
//        productEndRow= -1;
//        progressRow= -1;
//
//        shopStartRow = -1;
//        shopEndRow = -1;
//        shopProgressRow = -1;
//
//        if(id == 1){
//            filterRow = rowCount++;
//            int count = productResult.size();
//            if (count != 0) {
//                productStartRow = rowCount;
//                rowCount += count;
//                productEndRow = rowCount;
//            }
//            if(searching){
//                progressRow = rowCount++;
//            }
//        }else{
//            int count = shopResult.size();
//
//            if (count != 0) {
//                shopStartRow = rowCount;
//                rowCount += count;
//                shopEndRow = rowCount;
//            }
//
//            if(searchingShop){
//                progressRow = rowCount++;
//            }
//        }
//        if(productAdapter != null){
//            productAdapter.notifyDataSetChanged();
//        }
//    }
//
//
//    @Override
//    public View createView(Context context) {
//
//        actionBar.setBackgroundColor(Theme.getColor(Theme.key_dialogBackground));
//        actionBar.setTitleColor(Theme.getColor(Theme.key_dialogTextBlack));
//        actionBar.setItemsColor(Theme.getColor(Theme.key_dialogTextBlack), false);
//        actionBar.setItemsBackgroundColor(Theme.getColor(Theme.key_dialogButtonSelector), false);
//        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
//        if (AndroidUtilities.isTablet()) {
//            actionBar.setOccupyStatusBar(false);
//        }
//        actionBar.setExtraHeight(AndroidUtilities.dp(44));
//        actionBar.setAllowOverlayTitle(false);
//        actionBar.setAddToContainer(false);
//        actionBar.setClipContent(true);
//        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
//            @Override
//            public void onItemClick(int id) {
//                if (id == -1) {
//                    finishFragment();
//                }
//            }
//        });
//        hasOwnBackground = true;
//
//
////        spanListLayout = new SpanListLayout(context);
////        spanListLayout.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//
//
//        productAdapter = new ProductAdapter(context);
//        shopAdapter = new SearchShopAdapter(context);
//
//
//        ActionBarMenu menu = actionBar.createMenu();
//        searchItem = menu.addItem(search_button, R.drawable.ic_ab_search).setIsSearchField(true).setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener() {
//            @Override
//            public void onSearchExpand() {
//               // imagesSearch.getActionBar().openSearchField("", false);
//               // gifsSearch.getActionBar().openSearchField("", false);
//                searchItem.getSearchField().requestFocus();
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
//               // imagesSearch.getActionBar().setSearchFieldText(editText.getText().toString());
//               // gifsSearch.getActionBar().setSearchFieldText(editText.getText().toString());
//            }
//
//
//            @Override
//            public void onSearchPressed(EditText editText) {
//                String text = editText.getText().toString();
//                searchDelayed(text,1);
//            }
//        });
//        searchItem.setSearchFieldHint("Search for product");
//        EditTextBoldCursor editText = searchItem.getSearchField();
//        editText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
//        editText.setCursorColor(Theme.getColor(Theme.key_dialogTextBlack));
//        editText.setHintTextColor(Theme.getColor(Theme.key_chat_messagePanelHint));
//
//        scrollSlidingTextTabStrip = new ScrollSlidingTextTabStrip(context);
//        scrollSlidingTextTabStrip.setUseSameWidth(true);
//        scrollSlidingTextTabStrip.setColors(Theme.key_chat_attachActiveTab, Theme.key_chat_attachActiveTab, Theme.key_chat_attachUnactiveTab, Theme.key_dialogButtonSelector);
//        actionBar.addView(scrollSlidingTextTabStrip, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 44, Gravity.LEFT | Gravity.BOTTOM));
//        scrollSlidingTextTabStrip.setDelegate(new ScrollSlidingTextTabStrip.ScrollSlidingTabStripDelegate() {
//            @Override
//            public void onPageSelected(int id, boolean forward) {
//                if (viewPages[0].selectedType == id) {
//                    return;
//                }
//                swipeBackEnabled = id == scrollSlidingTextTabStrip.getFirstTabId();
//                viewPages[1].selectedType = id;
//                viewPages[1].setVisibility(View.VISIBLE);
//                switchToCurrentSelectedMode(true);
//                animatingForward = forward;
//                if (id == 0) {
//                    searchItem.setSearchFieldHint("Search Product");
//                } else {
//                    searchItem.setSearchFieldHint("Search Shops");
//                }
//            }
//
//            @Override
//            public void onPageScrolled(float progress) {
//                if (progress == 1 && viewPages[1].getVisibility() != View.VISIBLE) {
//                    return;
//                }
//                if (animatingForward) {
//                    viewPages[0].setTranslationX(-progress * viewPages[0].getMeasuredWidth());
//                    viewPages[1].setTranslationX(viewPages[0].getMeasuredWidth() - progress * viewPages[0].getMeasuredWidth());
//                } else {
//                    viewPages[0].setTranslationX(progress * viewPages[0].getMeasuredWidth());
//                    viewPages[1].setTranslationX(progress * viewPages[0].getMeasuredWidth() - viewPages[0].getMeasuredWidth());
//                }
//                if (progress == 1) {
//                    ViewPage tempPage = viewPages[0];
//                    viewPages[0] = viewPages[1];
//                    viewPages[1] = tempPage;
//                    viewPages[1].setVisibility(View.GONE);
//                }
//            }
//        });
//
//        ViewConfiguration configuration = ViewConfiguration.get(context);
//        maximumVelocity = configuration.getScaledMaximumFlingVelocity();
//
//        SizeNotifierFrameLayout sizeNotifierFrameLayout;
//
//        fragmentView = sizeNotifierFrameLayout = new SizeNotifierFrameLayout(context) {
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
//                viewPages[1].selectedType = id;
//                viewPages[1].setVisibility(View.VISIBLE);
//                animatingForward = forward;
//                switchToCurrentSelectedMode(true);
//                if (forward) {
//                    viewPages[1].setTranslationX(viewPages[0].getMeasuredWidth());
//                } else {
//                    viewPages[1].setTranslationX(-viewPages[0].getMeasuredWidth());
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
//                int keyboardSize = SharedConfig.smoothKeyboard ? 0 : measureKeyboardHeight();
//                if (keyboardSize <= AndroidUtilities.dp(20)) {
//                    if (!AndroidUtilities.isInMultiwindow) {
//                        //heightSize -= commentTextView.getEmojiPadding();
//                        heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
//                    }
//                } else {
//                    globalIgnoreLayout = true;
//                   // commentTextView.hideEmojiView();
//                    globalIgnoreLayout = false;
//                }
//
//                int actionBarHeight = actionBar.getMeasuredHeight();
//                globalIgnoreLayout = true;
//                for (int a = 0; a < viewPages.length; a++) {
//                    if (viewPages[a] == null) {
//                        continue;
//                    }
//                    if (viewPages[a].listView != null) {
//                        viewPages[a].listView.setPadding(AndroidUtilities.dp(4), actionBarHeight + AndroidUtilities.dp(4), AndroidUtilities.dp(4), AndroidUtilities.dp(4));
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
//                    if (false) {
//                        if (AndroidUtilities.isInMultiwindow || AndroidUtilities.isTablet()) {
//                            if (AndroidUtilities.isTablet()) {
//                                child.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(Math.min(AndroidUtilities.dp(AndroidUtilities.isTablet() ? 200 : 320), heightSize - AndroidUtilities.statusBarHeight + getPaddingTop()), MeasureSpec.EXACTLY));
//                            } else {
//                                child.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(heightSize - AndroidUtilities.statusBarHeight + getPaddingTop(), MeasureSpec.EXACTLY));
//                            }
//                        } else {
//                            child.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(child.getLayoutParams().height, MeasureSpec.EXACTLY));
//                        }
//                    } else {
//                        measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
//                    }
//                }
//            }
//
//            @Override
//            protected void onLayout(boolean changed, int l, int t, int r, int b) {
//                final int count = getChildCount();
//
//                int keyboardSize = SharedConfig.smoothKeyboard ? 0 : measureKeyboardHeight();
//                int paddingBottom = keyboardSize <= AndroidUtilities.dp(20) && !AndroidUtilities.isInMultiwindow && !AndroidUtilities.isTablet() ? 0: 0;
//                setBottomClip(paddingBottom);
//
//                for (int i = 0; i < count; i++) {
//                    final View child = getChildAt(i);
//                    if (child.getVisibility() == GONE) {
//                        continue;
//                    }
//                    final LayoutParams lp = (LayoutParams) child.getLayoutParams();
//
//                    final int width = child.getMeasuredWidth();
//                    final int height = child.getMeasuredHeight();
//
//                    int childLeft;
//                    int childTop;
//
//                    int gravity = lp.gravity;
//                    if (gravity == -1) {
//                        gravity = Gravity.TOP | Gravity.LEFT;
//                    }
//
//                    final int absoluteGravity = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
//                    final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;
//
//                    switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
//                        case Gravity.CENTER_HORIZONTAL:
//                            childLeft = (r - l - width) / 2 + lp.leftMargin - lp.rightMargin;
//                            break;
//                        case Gravity.RIGHT:
//                            childLeft = (r - l) - width - lp.rightMargin - getPaddingRight();
//                            break;
//                        case Gravity.LEFT:
//                        default:
//                            childLeft = lp.leftMargin + getPaddingLeft();
//                    }
//
//                    switch (verticalGravity) {
//                        case Gravity.TOP:
//                            childTop = lp.topMargin + getPaddingTop();
//                            break;
//                        case Gravity.CENTER_VERTICAL:
//                            childTop = ((b - paddingBottom) - t - height) / 2 + lp.topMargin - lp.bottomMargin;
//                            break;
//                        case Gravity.BOTTOM:
//                            childTop = ((b - paddingBottom) - t) - height - lp.bottomMargin;
//                            break;
//                        default:
//                            childTop = lp.topMargin;
//                    }
//
//                    if (false) {
//                        if (AndroidUtilities.isTablet()) {
//                            childTop = getMeasuredHeight() - child.getMeasuredHeight();
//                        } else {
//                            childTop = getMeasuredHeight() + keyboardSize - child.getMeasuredHeight();
//                        }
//                    }
//                    child.layout(childLeft, childTop, childLeft + width, childTop + height);
//                }
//
//                notifyHeightChanged();
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
//                        if (Math.abs(viewPages[0].getTranslationX()) < 1) {
//                            viewPages[0].setTranslationX(0);
//                            viewPages[1].setTranslationX(viewPages[0].getMeasuredWidth() * (animatingForward ? 1 : -1));
//                            cancel = true;
//                        }
//                    } else if (Math.abs(viewPages[1].getTranslationX()) < 1) {
//                        viewPages[0].setTranslationX(viewPages[0].getMeasuredWidth() * (animatingForward ? -1 : 1));
//                        viewPages[1].setTranslationX(0);
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
//                backgroundPaint.setColor(Theme.getColor(Theme.key_windowBackgroundGray));
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
//                                viewPages[0].setTranslationX(0);
//                                viewPages[1].setTranslationX(animatingForward ? viewPages[0].getMeasuredWidth() : -viewPages[0].getMeasuredWidth());
//                                scrollSlidingTextTabStrip.selectTabWithId(viewPages[1].selectedType, 0);
//                            }
//                        }
//                        if (maybeStartTracking && !startedTracking) {
//                            float touchSlop = AndroidUtilities.getPixelsInCM(0.3f, true);
//                            if (Math.abs(dx) >= touchSlop && Math.abs(dx) > dy) {
//                                prepareForMoving(ev, dx < 0);
//                            }
//                        } else if (startedTracking) {
//                            viewPages[0].setTranslationX(dx);
//                            if (animatingForward) {
//                                viewPages[1].setTranslationX(viewPages[0].getMeasuredWidth() + dx);
//                            } else {
//                                viewPages[1].setTranslationX(dx - viewPages[0].getMeasuredWidth());
//                            }
//                            float scrollProgress = Math.abs(dx) / (float) viewPages[0].getMeasuredWidth();
//                            scrollSlidingTextTabStrip.selectTabWithId(viewPages[1].selectedType, scrollProgress);
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
//                            float x = viewPages[0].getX();
//                            tabsAnimation = new AnimatorSet();
//                            backAnimation = Math.abs(x) < viewPages[0].getMeasuredWidth() / 3.0f && (Math.abs(velX) < 3500 || Math.abs(velX) < Math.abs(velY));
//                            float distToMove;
//                            float dx;
//                            if (backAnimation) {
//                                dx = Math.abs(x);
//                                if (animatingForward) {
//                                    tabsAnimation.playTogether(
//                                            ObjectAnimator.ofFloat(viewPages[0], View.TRANSLATION_X, 0),
//                                            ObjectAnimator.ofFloat(viewPages[1], View.TRANSLATION_X, viewPages[1].getMeasuredWidth())
//                                    );
//                                } else {
//                                    tabsAnimation.playTogether(
//                                            ObjectAnimator.ofFloat(viewPages[0], View.TRANSLATION_X, 0),
//                                            ObjectAnimator.ofFloat(viewPages[1], View.TRANSLATION_X, -viewPages[1].getMeasuredWidth())
//                                    );
//                                }
//                            } else {
//                                dx = viewPages[0].getMeasuredWidth() - Math.abs(x);
//                                if (animatingForward) {
//                                    tabsAnimation.playTogether(
//                                            ObjectAnimator.ofFloat(viewPages[0], View.TRANSLATION_X, -viewPages[0].getMeasuredWidth()),
//                                            ObjectAnimator.ofFloat(viewPages[1], View.TRANSLATION_X, 0)
//                                    );
//                                } else {
//                                    tabsAnimation.playTogether(
//                                            ObjectAnimator.ofFloat(viewPages[0], View.TRANSLATION_X, viewPages[0].getMeasuredWidth()),
//                                            ObjectAnimator.ofFloat(viewPages[1], View.TRANSLATION_X, 0)
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
//                                        viewPages[1].setVisibility(View.GONE);
//                                    } else {
//                                        ViewPage tempPage = viewPages[0];
//                                        viewPages[0] = viewPages[1];
//                                        viewPages[1] = tempPage;
//                                        viewPages[1].setVisibility(View.GONE);
//                                        swipeBackEnabled = viewPages[0].selectedType == scrollSlidingTextTabStrip.getFirstTabId();
//                                        scrollSlidingTextTabStrip.selectTabWithId(viewPages[0].selectedType, 1.0f);
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
//        sizeNotifierFrameLayout.setWillNotDraw(false);
//
//        for (int a = 0; a < viewPages.length; a++) {
//            viewPages[a] = new ViewPage(context) {
//                @Override
//                public void setTranslationX(float translationX) {
//                    super.setTranslationX(translationX);
//                    if (tabsAnimationInProgress) {
//                        if (viewPages[0] == this) {
//                            float scrollProgress = Math.abs(viewPages[0].getTranslationX()) / (float) viewPages[0].getMeasuredWidth();
//                            scrollSlidingTextTabStrip.selectTabWithId(viewPages[1].selectedType, scrollProgress);
//                        }
//                    }
//                }
//            };
//            sizeNotifierFrameLayout.addView(viewPages[a], LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
//            viewPages[a].listView = new RecyclerListView(context) {
//                @Override
//                protected void onLayout(boolean changed, int l, int t, int r, int b) {
//                    super.onLayout(changed, l, t, r, b);
//                  //  checkLoadMoreScroll(mediaPage, mediaPage.listView, layoutManager);
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
//            final GridLayoutManager layoutManager = viewPages[a].layoutManager = new GridLayoutManager(context, 2);
//            viewPages[a].listView.setFocusable(true);
//            viewPages[a].listView.setFocusableInTouchMode(true);
//            viewPages[a].listView.setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING);
//            viewPages[a].listView.setPinnedSectionOffsetY(-AndroidUtilities.dp(2));
//            viewPages[a].listView.setPadding(8, AndroidUtilities.dp(0), 8, 0);
//            viewPages[a].listView.setItemAnimator(null);
//            viewPages[a].listView.setClipToPadding(false);
//            viewPages[a].listView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
//            viewPages[a].listView.setLayoutManager(layoutManager);
//            viewPages[a].addView(viewPages[a].listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
//
//
//            viewPages[a].emptyView = new LinearLayout(context);
//            viewPages[a].emptyView.setWillNotDraw(false);
//            viewPages[a].emptyView.setOrientation(LinearLayout.VERTICAL);
//            viewPages[a].emptyView.setGravity(Gravity.CENTER);
//            viewPages[a].emptyView.setVisibility(View.GONE);
//            viewPages[a].addView(viewPages[a].emptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
//
//            viewPages[a].emptyView.setOnTouchListener((v, event) -> true);
//
//            viewPages[a].emptyImageView = new ImageView(context);
//            viewPages[a].emptyView.addView(viewPages[a].emptyImageView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));
//
//            viewPages[a].emptyTextView = new TextView(context);
//            viewPages[a].emptyTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
//            viewPages[a].emptyTextView.setGravity(Gravity.CENTER);
//            viewPages[a].emptyTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
//            viewPages[a].emptyTextView.setPadding(AndroidUtilities.dp(40), 0, AndroidUtilities.dp(40), AndroidUtilities.dp(128));
//            viewPages[a].emptyView.addView(viewPages[a].emptyTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 0, 24, 0, 0));
//
//            viewPages[a].progressView = new LinearLayout(context) {
//                @Override
//                protected void onDraw(Canvas canvas) {
//                    backgroundPaint.setColor(Theme.getColor(Theme.key_windowBackgroundGray));
//                    canvas.drawRect(0, actionBar.getMeasuredHeight() + actionBar.getTranslationY(), getMeasuredWidth(), getMeasuredHeight(), backgroundPaint);
//                }
//            };
//            viewPages[a].progressView.setWillNotDraw(false);
//            viewPages[a].progressView.setGravity(Gravity.CENTER);
//            viewPages[a].progressView.setOrientation(LinearLayout.VERTICAL);
//            viewPages[a].progressView.setVisibility(View.GONE);
//            viewPages[a].addView(viewPages[a].progressView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
//
//
//            viewPages[a].progressBar = new RadialProgressView(context);
//            viewPages[a].progressView.addView(viewPages[a].progressBar, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));
//
//            if (a != 0) {
//                viewPages[a].setVisibility(View.GONE);
//            }
//
//
//            RecyclerView.OnScrollListener onScrollListener = viewPages[a].listView.getOnScrollListener();
//            viewPages[a].listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
//                @Override
//                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                    onScrollListener.onScrollStateChanged(recyclerView, newState);
//                    if (newState != RecyclerView.SCROLL_STATE_DRAGGING) {
//                        int scrollY = (int) -actionBar.getTranslationY();
//                        int actionBarHeight = ActionBar.getCurrentActionBarHeight();
//                        if (scrollY != 0 && scrollY != actionBarHeight) {
//                            if (scrollY < actionBarHeight / 2) {
//                                viewPages[0].listView.smoothScrollBy(0, -scrollY);
//                            } else {
//                                viewPages[0].listView.smoothScrollBy(0, actionBarHeight - scrollY);
//                            }
//                        }
//                    }
//                }
//
//                @Override
//                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                    onScrollListener.onScrolled(recyclerView, dx, dy);
//                    if (recyclerView == viewPages[0].listView) {
//                        float currentTranslation = actionBar.getTranslationY();
//                        float newTranslation = currentTranslation - dy;
//                        if (newTranslation < -ActionBar.getCurrentActionBarHeight()) {
//                            newTranslation = -ActionBar.getCurrentActionBarHeight();
//                        } else if (newTranslation > 0) {
//                            newTranslation = 0;
//                        }
//                        if (newTranslation != currentTranslation) {
//                            setScrollY(newTranslation);
//                        }
//                    }
//                }
//            });
//        }
//
//        sizeNotifierFrameLayout.addView(actionBar, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
//        updateTabs();
//        switchToCurrentSelectedMode(false);
//        swipeBackEnabled = scrollSlidingTextTabStrip.getCurrentTabId() == scrollSlidingTextTabStrip.getFirstTabId();
//
//        return fragmentView;
//    }
//
//
//    private void checkLoadMoreScroll(ViewPage mediaPage, RecyclerView recyclerView, LinearLayoutManager layoutManager) {
//        if(recyclerView.getAdapter() == null){
//            return;
//        }
//        String nextLink = queryLoadMore.get(lastSearchQuery);
//        if(ShopUtils.isEmpty(nextLink)){
//            return;
//        }
//        int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
//        int visibleItemCount = firstVisibleItem == RecyclerView.NO_POSITION ? 0 : Math.abs(layoutManager.findLastVisibleItemPosition() - firstVisibleItem) + 1;
//        int totalItemCount = recyclerView.getAdapter().getItemCount();
//
//        final int threshold;
//        if (mediaPage.selectedType == 1) {
//            threshold = 3;
//        } else  {
//            threshold = 6;
//        }
//        if(mediaPage.selectedType == 1){
//            if (firstVisibleItem + visibleItemCount > totalItemCount - threshold && !searching) {
//                currentRequestNum =  ShopDataController.getInstance(currentAccount).searchProduct(lastSearchQuery,"",nextLink,sort,filter, new ShopDataController.ProductSearchCallBack() {
//                    @Override
//                    public void run(Object response, APIError error, String next, int count) {
//                        currentRequestNum = 0;
//                        searching = false;
//                        productResult.clear();
//                        searchInProgress = false;
//                        if(error == null){
//                            if(ShopUtils.isEmpty(next)){
//                                queryLoadMore.put(lastSearchQuery,next);
//                            }
//                            ArrayList<ShopDataSerializer.Product> products = (ArrayList<ShopDataSerializer.Product>)response;
//                            productResult.addAll(products);
//                            int oldItemCount = productAdapter.getItemCount();
//                            if(oldItemCount > 0){
//                                productAdapter.notifyItemRangeInserted(oldItemCount,products.size());
//                            }else{
//                                productAdapter.notifyDataSetChanged();
//                            }
//                        }
//                    }
//                });
//
//
//            }
//
//        }else if(mediaPage.selectedType == 2){
////            if (firstVisibleItem + visibleItemCount > totalItemCount - threshold && !loadingReview) {
//////                if(!commentEndReached){
//////                    loadingReview = true;
//////                    ShopDataController.getInstance(UserConfig.selectedAccount).loadMoreReviewForShop(nextComment,profileActivity.getClassGuid());
//////                }
////            }
//        }
//    }
//
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        if (searchItem != null) {
//            searchItem.openSearch(true);
//            getParentActivity().getWindow().setSoftInputMode(SharedConfig.smoothKeyboard ? WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN : WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
//        }
//    }
//
//
//    @Override
//    public boolean isSwipeBackEnabled(MotionEvent event) {
//        return swipeBackEnabled;
//    }
//
//    @Override
//    public void onFragmentDestroy() {
//        super.onFragmentDestroy();
//    }
//
//
//
//    private void setScrollY(float value) {
//        actionBar.setTranslationY(value);
//        for (int a = 0; a < viewPages.length; a++) {
//            viewPages[a].listView.setPinnedSectionOffsetY((int) value);
//        }
//        fragmentView.invalidate();
//    }
//
//    private void searchText(String text) {
//        searchItem.getSearchField().setText(text);
//        searchItem.getSearchField().setSelection(text.length());
//        actionBar.onSearchPressed();
//    }
//
//
//    private void updateTabs() {
//        if (scrollSlidingTextTabStrip == null) {
//            return;
//        }
//        scrollSlidingTextTabStrip.addTextTab(1,"Products");
//        scrollSlidingTextTabStrip.addTextTab(2, "Shops");
//        scrollSlidingTextTabStrip.setVisibility(View.VISIBLE);
//        actionBar.setExtraHeight(AndroidUtilities.dp(44));
//        int id = scrollSlidingTextTabStrip.getCurrentTabId();
//        if (id >= 0) {
//            viewPages[0].selectedType = id;
//        }
//        scrollSlidingTextTabStrip.finishAddingTabs();
//    }
//
//    private void switchToCurrentSelectedMode(boolean animated) {
//        for (int a = 0; a < viewPages.length; a++) {
//            viewPages[a].listView.stopScroll();
//        }
//        int a = animated ? 1 : 0;
//        RecyclerView.Adapter currentAdapter = viewPages[a].listView.getAdapter();
//        viewPages[a].listView.setPinnedHeaderShadowDrawable(null);
//        if (actionBar.getTranslationY() != 0) {
//            LinearLayoutManager layoutManager = (LinearLayoutManager) viewPages[a].listView.getLayoutManager();
//            layoutManager.scrollToPositionWithOffset(0, (int) actionBar.getTranslationY());
//        }
//
//        viewPages[a].emptyTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
//        viewPages[a].emptyImageView.setVisibility(View.VISIBLE);
//        viewPages[a].listView.setPinnedHeaderShadowDrawable(null);
//
//        if(viewPages[a].selectedType == 1){
//
//            if (currentAdapter != productAdapter) {
//                viewPages[a].listView.setAdapter(productAdapter);
//            }
//            viewPages[a].emptyImageView.setImageResource(R.drawable.smiles_info);
//            viewPages[a].emptyTextView.setText("No  product listing found!");
//
//            if(!searching){
//                viewPages[a].progressView.setVisibility(View.VISIBLE);
//                viewPages[a].listView.setEmptyView(null);
//                viewPages[a].emptyView.setVisibility(View.GONE);
//            }else{
//                viewPages[a].progressView.setVisibility(View.GONE);
//                viewPages[a].listView.setEmptyView(viewPages[a].emptyView);
//            }
//
//        }else if(viewPages[a].selectedType == 2){
//
//        }
//
//
//
//    }
//
//    private void processSelectedFilter(){
//        productResult.clear();
//        searching = true;
//        productEndReached = false;
//        for(int a = 0; a < viewPages.length; a++) {
//            if(viewPages[a].selectedType == 0) {
//                viewPages[a].progressView.setVisibility(View.VISIBLE);
//                viewPages[a].listView.setEmptyView(null);
//                viewPages[a].emptyView.setVisibility(View.VISIBLE);
//            }
//        }
//        searchDelayed(lastSearchQuery,1);
//        //ShopDataController.getInstance(UserConfig.selectedAccount).searchProduct(chat_id,selected_business,currentFilter,currentSort,profileActivity.getClassGuid());
//    }
//
//
//    private class ProductAdapter extends RecyclerListView.SelectionAdapter{
//
//        private Context mContext;
//
//        public ProductAdapter(Context context) {
//            mContext = context;
//        }
//
//
//        @Override
//        public boolean isEnabled(RecyclerView.ViewHolder holder) {
//            int viewType = holder.getItemViewType();
//            return viewType  == 4;
//        }
//
//
//        @NonNull
//        @Override
//        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//            View view;
//            switch (viewType){
//                case 4:
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
//                case 3:
//                    FilterHorizontalLayout filterHorizontalLayout = new FilterHorizontalLayout(mContext);
//                    view  = filterHorizontalLayout;
//                    filterHorizontalLayout.onFilterClick(v -> {
//                        BusinessAlert businessAlert = new BusinessAlert(mContext,false);
//                        businessAlert.setDelegate(business -> {
//                            ProductFilterActivity filterActivity = new ProductFilterActivity(business.key);
//                            filterActivity.setFilterDelegate(new ProductFilterActivity.ProductFilterDelegate() {
//                                @Override
//                                public void onFilterSelected(HashMap<String, Object> filterMap) {
//                                    filter.put("product_type",business.key);
//                                    filter = filterMap;
//                                    processSelectedFilter();
//                                }
//                            });
//                            presentFragment(filterActivity);
//                            filterHorizontalLayout.setText(business.display_name);
//
//                        });
//                        showDialog(businessAlert);
//
//                    });
////                    filterHorizontalLayout.onSortClick(v -> {
////                        ProductSortAlert productSortAlert = new ProductSortAlert(mContext,false,false,sort);
////                        productSortAlert.setDelegate(new ProductSortAlert.SortAlertDelegate() {
////                            @Override
////                            public void didSortSelected(ShopDataSerializer.ProductType.Sort  sort) {
////                                if(sort != null){
////                                    ShopSearchActivity.this.sort = sort;
////                                    processSelectedFilter();
////                                }
////                            }
////                        });
////                        showDialog(productSortAlert);
////                    });
//                    break;
//                case 53:
//                    EmptyTextProgressView emptyTextProgressView = new EmptyTextProgressView(mContext);
//                    emptyTextProgressView.setTopImage(R.drawable.files_empty);
//                    emptyTextProgressView.setText("No Result matching with your criteria");
//                    view = emptyTextProgressView;
//                    emptyTextProgressView.setShowAtTop(true);
//                    emptyTextProgressView.showTextView();
//                    view.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//                    break;
//                case 5:
//                    ShopsEmptyCell dialogsEmptyCell = new ShopsEmptyCell(mContext);
//                    dialogsEmptyCell.setType(4);
//                    view = dialogsEmptyCell;
//                    break;
//                case 7:
//                    LoadingView loadingView = new LoadingView(mContext){
//                        @Override
//                        public int getColumnsCount() {
//                            return 2;
//                        }
//                    };
//                    view = loadingView;
//                    loadingView.setViewType(LoadingView.PHOTOS_TYPE);
//                    view.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//                    break;
//                case 6:
//                default:
//                    view = new LoadingCell(mContext, AndroidUtilities.dp(32), AndroidUtilities.dp(74));
//                    break;
//            }
//
//
//            if(viewType != 5 && viewType!= 7)
//                view.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//            return new RecyclerListView.Holder(view);
//        }
//
//        public ShopDataSerializer.Product getItem(int position) {
//            int newPos = position - productStartRow;
//            if(newPos < 0 || newPos >= productResult.size()){
//                return null;
//            }
//            return productResult.get(position - productStartRow);
//        }
//
//        @Override
//        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//            if(holder.getItemViewType() == 4){
//                ProductCell cell = (ProductCell) holder.itemView;
//                ShopDataSerializer.Product product = getItem(position);
//                //  if(product.isFav)
//                cell.setProduct(product);
//                // cell.setFav(product.isFav);
//            }
//        }
//
//        @Override
//        public int getItemViewType(int position) {
//            if(position == filterRow){
//                return 3;
//            }else if (position >= productStartRow && position < productEndRow) {
//                return 4;
//            }else if(position == progressRow){
//                return 6;
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
//
//
//    private class SearchShopAdapter extends RecyclerListView.SelectionAdapter{
//
//        private Context mContext;
//
//        public SearchShopAdapter(Context mContext) {
//
//            this.mContext = mContext;
//        }
//
//
//        @Override
//        public boolean isEnabled(RecyclerView.ViewHolder holder) {
//            return true;
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
//    public void searchDelayed(final String query,int id) {
//        Log.i(TAG,"searchDelayed FOR TERM = " + query);
//        if(id == 1){
//            if (query == null || query.length() == 0) {
//                productResult.clear();
//                productAdapter.notifyDataSetChanged();
//            } else {
//
//                if (searchRunnable != null) {
//                    Utilities.searchQueue.cancelRunnable(searchRunnable);
//                    searchRunnable = null;
//                }
//                searchInProgress = true;
//                Utilities.searchQueue.postRunnable(searchRunnable = () -> AndroidUtilities.runOnUIThread(() -> {
//                    searchRunnable = null;
//                    lastSearchQuery = null;
//                    searchForQuery(query);
//                }), 400);
//            }
//
//        }else if(id == 2){
//
//        }
//    }
//
//    public void searchForQuery(final String query) {
//        if (query== null || lastSearchQuery != null) {
//            return;
//        }
//        lastSearchQuery = query;
//        if (searching) {
//            searching = false;
//            if (currentRequestNum != 0) {
//                ConnectionsManager.getInstance(currentAccount).cancelRequest(currentRequestNum, true);
//                currentRequestNum = 0;
//            }
//        }
//        searching = true;
//        AndroidUtilities.runOnUIThread(new Runnable() {
//            @Override
//            public void run() {
//                currentRequestNum =  ShopDataController.getInstance(currentAccount).searchProduct(query,"", sort, filter, (response, error, next, count) -> {
//                    currentRequestNum = 0;
//                    searching = false;
//                    productResult.clear();
//                    searchInProgress = false;
//                    lastSearchQuery = query;
//                    if(error == null){
//                        if(ShopUtils.isEmpty(next)){
//                            queryLoadMore.put(query,next);
//                        }
//                        ArrayList<ShopDataSerializer.Product> products = (ArrayList<ShopDataSerializer.Product>)response;
//                        productResult.addAll(products);
//                        updateRow(1);
////                    if(oldItemCount > 0){
////                        productAdapter.notifyItemRangeInserted(oldItemCount,products.size());
////                    }else{
////                        productAdapter.notifyDataSetChanged();
////                    }
////
//                    }
//                });
//
//            }
//        });
//    }
//
//
//
//
//    @Override
//    public ArrayList<ThemeDescription> getThemeDescriptions() {
//        ArrayList<ThemeDescription> arrayList = new ArrayList<>();
//
//        arrayList.add(new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_dialogBackground));
//        arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_dialogBackground));
//        arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_dialogTextBlack));
//        arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_dialogTextBlack));
//        arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_dialogButtonSelector));
//        arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SEARCH, null, null, null, null, Theme.key_dialogTextBlack));
//        arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SEARCHPLACEHOLDER, null, null, null, null, Theme.key_chat_messagePanelHint));
//        arrayList.add(new ThemeDescription(searchItem.getSearchField(), ThemeDescription.FLAG_CURSORCOLOR, null, null, null, null, Theme.key_dialogTextBlack));
//
//        arrayList.add(new ThemeDescription(scrollSlidingTextTabStrip.getTabsContainer(), ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, new Class[]{TextView.class}, null, null, null, Theme.key_chat_attachActiveTab));
//        arrayList.add(new ThemeDescription(scrollSlidingTextTabStrip.getTabsContainer(), ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, new Class[]{TextView.class}, null, null, null, Theme.key_chat_attachUnactiveTab));
//        arrayList.add(new ThemeDescription(scrollSlidingTextTabStrip.getTabsContainer(), ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, new Class[]{TextView.class}, null, null, null, Theme.key_dialogButtonSelector));
//        arrayList.add(new ThemeDescription(null, 0, null, null, new Drawable[]{scrollSlidingTextTabStrip.getSelectorDrawable()}, null, Theme.key_chat_attachActiveTab));
//
//        return arrayList;
//    }
//}
