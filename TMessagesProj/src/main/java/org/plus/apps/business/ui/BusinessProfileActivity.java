package org.plus.apps.business.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.NestedScrollingParent3;
import androidx.core.view.NestedScrollingParentHelper;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.plus.apps.business.ShopUtils;
import org.plus.apps.business.ui.components.ShopsEmptyCell;
import org.plus.experment.ScanQrFragment;
import org.plus.apps.business.data.ShopDataController;
import org.plus.apps.business.data.ShopDataSerializer;
import org.plus.apps.business.ui.components.ShopActionListView;
import org.plus.apps.business.ui.components.ShopInfoInnerCell;
import org.plus.apps.business.ui.components.ShopMediaLayout;
import org.plus.net.APIError;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.DialogsEmptyCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RadialProgressView;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SizeNotifierFrameLayout;
import org.telegram.ui.LocationActivity;
import org.webrtc.GlRectDrawer;

import java.util.ArrayList;

public class BusinessProfileActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private TopView topView;
    private FrameLayout avatarContainer;
    private BackupImageView avatarImage;
    private View avatarOverlay;
    private AvatarDrawable avatarDrawable;
    private AnimatorSet avatarAnimation;
    private RadialProgressView avatarProgressView;
    private SimpleTextView titleTextView;
    private TextView nameTextView;
    private TextView onlineTextView;

    private int extraHeight;

    private class TopView extends View {

        private int currentColor;
        private Paint paint = new Paint();

        public TopView(Context context) {
            super(context);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), ActionBar.getCurrentActionBarHeight() + (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + AndroidUtilities.dp(91));
        }

        @Override
        public void setBackgroundColor(int color) {
            if (color != currentColor) {
                paint.setColor(color);
                invalidate();
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int height = getMeasuredHeight() - AndroidUtilities.dp(91);
            canvas.drawRect(0, 0, getMeasuredWidth(), height + extraHeight , paint);

        }
    }

    private ShopInfoInnerCell shopInfoInnerCell;

    private RecyclerListView listView;
    private ListAdapter listAdapter;
    private LinearLayoutManager layoutManager;
    private EmptyTextProgressView progressView;
    private ShopsEmptyCell retryLayout;
    

    private ShopActionListView shopActionLayout;

    private ShopMediaLayout shopMediaLayout;
    private  boolean shopMediaLayoutAttached;

    private ActionBarMenuItem qrItem;
    private ActionBarMenuItem productMenuItem;
    private ActionBarMenuItem otherItem;

    private SwipeRefreshLayout refreshLayout;

    private int chat_id;
    private int item_id;
    private ShopDataSerializer.Shop currentShop;

    private boolean isSwipeBackEnabled;

    private int rowCount;
    private int shopActionRow;
    private int productSecRow;
    private int productRow;

    private final static int qr_item = 1;
    private final static int store_item = 2;
    private final static int store_share= 3;
    private final static int sore_report = 4;
    private final static int other_item = 5;

    public int getChat_id() {
        return chat_id;
    }

    public RecyclerListView getListView() {
        return listView;
    }

    public BusinessProfileActivity(Bundle args) {
        super(args);
    }

    private class NestedFrameLayout extends SizeNotifierFrameLayout implements NestedScrollingParent3 {

        private NestedScrollingParentHelper nestedScrollingParentHelper;

        public NestedFrameLayout(Context context) {
            super(context);
            nestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        }

        @Override
        public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type, int[] consumed) {
            if (target == listView && shopMediaLayoutAttached) {
                RecyclerListView innerListView = shopMediaLayout.getCurrentListView();
                int top = shopMediaLayout.getTop();
                if (top == 0) {
                    consumed[1] = dyUnconsumed;
                    innerListView.scrollBy(0, dyUnconsumed);
                }
            }
        }

        @Override
        public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {

        }

        @Override
        public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
            return super.onNestedPreFling(target, velocityX, velocityY);
        }

        @Override
        public void onNestedPreScroll(View target, int dx, int dy, int[] consumed, int type) {
            if (target == listView && productRow != -1 && shopMediaLayoutAttached) {
                boolean searchVisible = actionBar.isSearchFieldVisible();
                int t = shopMediaLayout.getTop();
                if (dy < 0) {
                    boolean scrolledInner = false;
                    if (t <= 0) {
                        RecyclerListView innerListView = shopMediaLayout.getCurrentListView();
                        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) innerListView.getLayoutManager();
                        int pos = linearLayoutManager.findFirstVisibleItemPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            RecyclerView.ViewHolder holder = innerListView.findViewHolderForAdapterPosition(pos);
                            int top = holder != null ? holder.itemView.getTop() : -1;
                            int paddingTop = innerListView.getPaddingTop();
                            if (top != paddingTop || pos != 0) {
                                consumed[1] = pos != 0 ? dy : Math.max(dy, (top - paddingTop));
                                innerListView.scrollBy(0, dy);
                                scrolledInner = true;
                            }
                        }
                    }
                    if (searchVisible) {
                        if (!scrolledInner && t < 0) {
                            consumed[1] = dy - Math.max(t, dy);
                        } else {
                            consumed[1] = dy;
                        }
                    }
                } else {
                    if (searchVisible) {
                        RecyclerListView innerListView = shopMediaLayout.getCurrentListView();
                        consumed[1] = dy;
                        if (t > 0) {
                            consumed[1] -= Math.min(consumed[1], dy);
                        }
                        if (consumed[1] > 0) {
                            innerListView.scrollBy(0, consumed[1]);
                        }
                    }
                }
            }
        }

        @Override
        public boolean onStartNestedScroll(View child, View target, int axes, int type) {
            return productRow != -1 && axes == ViewCompat.SCROLL_AXIS_VERTICAL;
        }

        @Override
        public void onNestedScrollAccepted(View child, View target, int axes, int type) {
            nestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes);
        }

        @Override
        public void onStopNestedScroll(View target, int type) {
            nestedScrollingParentHelper.onStopNestedScroll(target);
        }

        @Override
        public void onStopNestedScroll(View child) {

        }
    }

    private boolean loadingShop;
    private int requestId;
    private void refresh(){
        if(loadingShop){
            return;
        }
        loadingShop = true;
        progressView.setVisibility(View.GONE);
        progressView.setVisibility(View.VISIBLE);
        requestId =  ShopDataController.getInstance(currentAccount).loadShop(chat_id,classGuid);
    }

    @Override
    public boolean onFragmentCreate() {
        if(getArguments() != null){
            chat_id = arguments.getInt("chat_id", 0);
            item_id = arguments.getInt("item_id",0);
        }else{
            return false;
        }
        getNotificationCenter().addObserver(this, NotificationCenter.didShopLoaded);

        loadingShop = true;
        requestId =  ShopDataController.getInstance(currentAccount).loadShop(chat_id,classGuid);
        updateRowsIds();
        return true;
    }


    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (shopMediaLayout != null) {
            shopMediaLayout.onDestroy();
        }
        getNotificationCenter().removeObserver(this, NotificationCenter.didShopLoaded);

        if(requestId != 0){
            ShopDataController.getInstance(currentAccount).cancelRequest(requestId);
        }
    }


    private void updateRowsIds(){
        rowCount = 0;
        shopActionRow = -1;
        productSecRow = -1;
        productRow = -1;
        
        if(currentShop != null){
            shopActionRow = rowCount++;
            productSecRow = rowCount++;
            productRow = rowCount++;
        }

        if(listAdapter != null){
            listAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean isSwipeBackEnabled(MotionEvent event) {
        return isSwipeBackEnabled;
    }


    @Override
    protected ActionBar createActionBar(Context context) {
        ActionBar actionBar = new ActionBar(context);
        actionBar.setItemsBackgroundColor(Theme.getColor(Theme.key_avatar_actionBarSelectorBlue), false);
        actionBar.setItemsColor(Theme.getColor(Theme.key_avatar_actionBarIconBlue), false);
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setCastShadows(false);
        actionBar.setAddToContainer(false);
        actionBar.setOccupyStatusBar(Build.VERSION.SDK_INT >= 21 && !AndroidUtilities.isTablet());
        return actionBar;
    }



    @Override
    public View createView(Context context) {

        Theme.createProfileResources(context);

        extraHeight = AndroidUtilities.dp(88);

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } if(id == store_item){
                    Bundle bundle = new Bundle();
                    bundle.putInt("chat_id",chat_id);
                    StoreEditActivity storeEditActivity = new StoreEditActivity(bundle);
                    storeEditActivity.setCurrentShop(currentShop);
                    presentFragment(storeEditActivity);

                }else if(id == qr_item){
                    ScanQrFragment.showAsSheet(BusinessProfileActivity.this, chat_id,new ScanQrFragment.ScanQrFragmentDelegate() {
                        @Override
                        public void didFindQr(String text) {
                            String string  = "hg://shop?id=";
                            int chat_id = Integer.parseInt(text.replace(string,""));

                            Bundle bundle = new Bundle();
                            bundle.putInt("chat_id",chat_id);
                            presentFragment(new BusinessProfileActivity(bundle));
                        }
                    });
                }
            }
        });

        if (shopMediaLayout != null) {
            shopMediaLayout.onDestroy();
        }

        shopMediaLayout = new ShopMediaLayout(context,chat_id,this){
            @Override
            protected void onSelectedTabChanged() {
                isSwipeBackEnabled = shopMediaLayout.getSelectedTab() == 0;
            }
        };
        shopMediaLayout.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT));

        shopActionLayout  = new ShopActionListView(context);
        shopActionLayout.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        shopActionLayout.setShopActionDelegate(action -> {
            if(action == ShopActionListView.ACTION_LOCATION){
                LocationActivity locationActivity = new LocationActivity(LocationActivity.LOCATION_TYPE_GROUP_VIEW);
                TLRPC.TL_channelLocation channelLocation = new TLRPC.TL_channelLocation();
                channelLocation.address  = currentShop.address;
                channelLocation.geo_point = new TLRPC.TL_geoPoint();
                channelLocation.geo_point._long = currentShop._long;
                channelLocation.geo_point.lat = currentShop.lat;
                locationActivity.setChatLocation(chat_id, (channelLocation));
                presentFragment(locationActivity);
            }else if(action == ShopActionListView.ACTION_CALL){
                if (getParentActivity() == null) {
                    return;
                }
                ArrayList<ShopDataSerializer.TelephoneContact> phoneNumber = currentShop.phoneNumbers;
                final LinearLayout linearLayout = new LinearLayout(getParentActivity());

                linearLayout.setOrientation(LinearLayout.VERTICAL);
                BottomSheet.Builder builder = new BottomSheet.Builder(getParentActivity());
                for (int a = 0; a < phoneNumber.size(); a++) {
                    ShopDataSerializer.TelephoneContact contact = phoneNumber.get(a);
                    if(contact == null){
                        continue;
                    }
                    TextView textView = new TextView(getParentActivity());
                    Drawable drawable = getParentActivity().getResources().getDrawable(R.drawable.menu_calls);
                    textView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
                    drawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextBlack), PorterDuff.Mode.MULTIPLY));
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                    textView.setLines(1);
                    textView.setMaxLines(1);
                    textView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                    textView.setTag(a);
                    textView.setBackgroundDrawable(Theme.getSelectorDrawable(false));
                    textView.setPadding(AndroidUtilities.dp(24), 0, AndroidUtilities.dp(24), 0);
                    textView.setSingleLine(true);
                    textView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                    textView.setCompoundDrawablePadding(AndroidUtilities.dp(26));
                    textView.setText(contact.phonenumber);
                    linearLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, Gravity.LEFT | Gravity.TOP));
                    textView.setOnClickListener(v -> {
                        int i = (Integer) v.getTag();
                        String phone =   contact.phonenumber;
                        try {
                            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:+" + phone));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            getParentActivity().startActivityForResult(intent, 500);
                        } catch (Exception e) {
                            FileLog.e(e);
                        }
                        builder.getDismissRunnable().run();
                    });}

                builder.setTitle("Call");
                builder.setCustomView(linearLayout);
                showDialog(builder.create());
            }else if(action == ShopActionListView.ACTION_CHAT){
                getMessagesController().openByUserName(currentShop.contact_username,BusinessProfileActivity.this,4);
            }else if(action == ShopActionListView.ACTION_QR){

                ScanQrFragment.showAsSheet(BusinessProfileActivity.this, chat_id, new ScanQrFragment.ScanQrFragmentDelegate() {
                    @Override
                    public void didFindQr(String text) {
                        Integer shop_id =null;
                        Integer product_id = null;
                        Uri data = Uri.parse(text);
                        String url = data.toString();
                        if(url.startsWith("tg://open")){
                            url = url.replace("tg://open", "tg://open.org");
                            data = Uri.parse(url);
                            shop_id = Utilities.parseInt(data.getQueryParameter("shop_id"));
                            if(url.contains("product_id")){
                                product_id = Utilities.parseInt(data.getQueryParameter("product_id"));
                            }

                            if(product_id == null){
                                Bundle bundle = new Bundle();
                                bundle.putInt("chat_id",shop_id);
                                presentFragment(new BusinessProfileActivity(bundle));
                            }else{
                                Bundle bundle = new Bundle();
                                bundle.putInt("chat_id", shop_id);
                                bundle.putInt("item_id",product_id);
                                ProductDetailFragment detailFragment = new ProductDetailFragment(bundle);
                                presentFragment(detailFragment);
                            }

                        }

                    }
                });



            }
        });

        ActionBarMenu menu= actionBar.createMenu();
        qrItem  = menu.addItem(qr_item,R.drawable.wallet_qr);
        productMenuItem =   menu.addItem(store_item,R.drawable.ic_store);
        otherItem = menu.addItem(other_item,R.drawable.ic_ab_other);
        otherItem.addSubItem(store_share, LocaleController.getString("ShareShop",R.string.ShareShop));
        otherItem.addSubItem(sore_report,LocaleController.getString("ReportShop",R.string.ReportShop));


        otherItem.setVisibility(View.GONE);
        productMenuItem.setVisibility(View.GONE);
        qrItem.setVisibility(View.GONE);

        listAdapter = new ListAdapter(context);

        fragmentView = new NestedFrameLayout(context){

            private Paint paint = new Paint();


            @Override
            public boolean hasOverlappingRendering() {
                return false;
            }

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                titleTextView.setTextSize(!AndroidUtilities.isTablet() && getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? 18 : 20);

                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }

            @Override
            protected void onLayout(boolean changed, int l, int t, int r, int b) {
                super.onLayout(changed, l, t, r, b);
                if (titleTextView != null) {
                    int textLeft = AndroidUtilities.dp(AndroidUtilities.isTablet() ? 80 : 72);
                    int textTop = (ActionBar.getCurrentActionBarHeight() - titleTextView.getTextHeight()) / 2 + (Build.VERSION.SDK_INT >= 21 && !AndroidUtilities.isTablet() ? AndroidUtilities.statusBarHeight : 0);
                    titleTextView.layout(textLeft, textTop, textLeft + titleTextView.getMeasuredWidth(), textTop + titleTextView.getTextHeight());
                }
                checkListViewScroll(false);
                needLayout();
            }

            @Override
            public void onDraw(Canvas c) {
                paint.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                View contentView = listView;
                c.drawRect(contentView.getLeft(), contentView.getTop() , contentView.getRight(), contentView.getBottom(), paint);
            }

        };
        fragmentView.setWillNotDraw(false);
        SizeNotifierFrameLayout frameLayout = (SizeNotifierFrameLayout) fragmentView;

        listView = new RecyclerListView(context) {

            private VelocityTracker velocityTracker;

            @Override
            protected boolean allowSelectChildAtPosition(View child) {
                return child != shopMediaLayout;
            }

            @Override
            public boolean hasOverlappingRendering() {
                return false;
            }

            @Override
            protected void requestChildOnScreen(View child, View focused) {

            }

            @Override
            public void invalidate() {
                super.invalidate();
                if (fragmentView != null) {
                    fragmentView.invalidate();
                }
            }

            @Override
            public boolean onTouchEvent(MotionEvent e) {
                final int action = e.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    if (velocityTracker == null) {
                        velocityTracker = VelocityTracker.obtain();
                    } else {
                        velocityTracker.clear();
                    }
                    velocityTracker.addMovement(e);
                } else if (action == MotionEvent.ACTION_MOVE) {
                    if (velocityTracker != null) {
                        velocityTracker.addMovement(e);
                        velocityTracker.computeCurrentVelocity(1000);
                    }
                } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    if (velocityTracker != null) {
                        velocityTracker.recycle();
                        velocityTracker = null;
                    }
                }
                final boolean result = super.onTouchEvent(e);
                if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                }
                return result;
            }
        };
        listView.setItemAnimator(null);
        listView.setLayoutAnimation(null);
        listView.setVerticalScrollBarEnabled(false);
        listView.setPadding(0, AndroidUtilities.dp(88), 0, 0);
        listView.setClipToPadding(false);
        listView.setEmptyView(null);
        layoutManager = new LinearLayoutManager(context){
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        };
        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                checkListViewScroll();
            }
        });

        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        listView.setLayoutManager(layoutManager);
        listView.setGlowColor(0);
        listView.setAdapter(listAdapter);
        frameLayout.addView(listView,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.MATCH_PARENT, Gravity.LEFT|Gravity.TOP));

        topView = new TopView(context);
        topView.setBackgroundColor(Theme.getColor(Theme.key_avatar_backgroundActionBarBlue));
        frameLayout.addView(topView);
        frameLayout.addView(actionBar);

        avatarContainer = new FrameLayout(context);
        avatarContainer.setPivotX(0);
        avatarContainer.setPivotY(0);
        frameLayout.addView(avatarContainer, LayoutHelper.createFrame(42, 42, Gravity.TOP | Gravity.LEFT, 64, 0, 0, 0));

        avatarImage = new BackupImageView(context);
        avatarImage.setRoundRadius(AndroidUtilities.dp(21));
        avatarImage.setContentDescription(LocaleController.getString("AccDescrProfilePicture", R.string.AccDescrProfilePicture));
        avatarContainer.addView(avatarImage, LayoutHelper.createFrame(42, 42));

        titleTextView = new SimpleTextView(context);
        titleTextView.setGravity(Gravity.LEFT);
        titleTextView.setTextColor(Theme.getColor(Theme.key_actionBarDefaultTitle));
        titleTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        titleTextView.setText(BuildVars.DEBUG_VERSION ? "Telegram Beta" : LocaleController.getString("AppNameHulu", R.string.AppNameHulu));
        titleTextView.setAlpha(0.0f);
        frameLayout.addView(titleTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP));


        nameTextView = new TextView(context);
        nameTextView.setTextColor(Theme.getColor(Theme.key_profile_title));
        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        nameTextView.setLines(1);
        nameTextView.setMaxLines(1);
        nameTextView.setSingleLine(true);
        nameTextView.setEllipsize(TextUtils.TruncateAt.END);
        nameTextView.setGravity(Gravity.LEFT);
        nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        nameTextView.setPivotX(0);
        nameTextView.setPivotY(0);
        frameLayout.addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 118, 0, 96, 0));

        onlineTextView = new TextView(context);
        onlineTextView.setTextColor(Theme.getColor(Theme.key_profile_status));
        onlineTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        onlineTextView.setLines(1);
        onlineTextView.setMaxLines(1);
        onlineTextView.setSingleLine(true);
        onlineTextView.setEllipsize(TextUtils.TruncateAt.END);
        onlineTextView.setGravity(Gravity.LEFT);
        frameLayout.addView(onlineTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 118, 0, 96, 0));

        int top = (actionBar.getOccupyStatusBar()?AndroidUtilities.statusBarHeight:0) + (ActionBar.getCurrentActionBarHeight());

        retryLayout = new ShopsEmptyCell(context);
        retryLayout.setVisibility(View.GONE);
        retryLayout.setType(ShopsEmptyCell.TYPE_RETRY);
        retryLayout.setRetryListener(v -> refresh());
        frameLayout.addView(retryLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT,Gravity.LEFT|Gravity.TOP,0,0,0,0));

        progressView = new EmptyTextProgressView(context);
        progressView.showProgress();
        progressView.setTextSize(18);
        progressView.setVisibility(View.VISIBLE);
        progressView.setShowAtCenter(false);
        progressView.setPadding(0, AndroidUtilities.dp(0), 0, 0);
        progressView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        frameLayout.addView(progressView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT,Gravity.LEFT|Gravity.TOP,0,0,0,0));

        needLayout();

         return fragmentView;
    }

    private void needLayout() {
        FrameLayout.LayoutParams layoutParams;
        int newTop = (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight();
        if (listView != null) {
            layoutParams = (FrameLayout.LayoutParams) listView.getLayoutParams();
            if (layoutParams.topMargin != newTop) {
                layoutParams.topMargin = newTop;
                listView.setLayoutParams(layoutParams);

                if(progressView != null){
                    progressView.setLayoutParams(layoutParams);
                }

                if(retryLayout != null){
                    retryLayout.setLayoutParams(layoutParams);
                }

            }
        }

        if(avatarContainer != null){
            float diff = extraHeight / (float) AndroidUtilities.dp(88);
            listView.setTopGlowOffset(extraHeight);

            float avatarY = (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight() / 2.0f * (1.0f + diff) - 21 * AndroidUtilities.density + 27 * AndroidUtilities.density * diff;
            avatarContainer.setScaleX((42 + 18 * diff) / 42.0f);
            avatarContainer.setScaleY((42 + 18 * diff) / 42.0f);
            avatarContainer.setTranslationX(-AndroidUtilities.dp(47) * diff);
            avatarContainer.setTranslationY((float) Math.ceil(avatarY));

            if (nameTextView != null) {
                nameTextView.setTranslationX(-21 * AndroidUtilities.density * diff);
                onlineTextView.setTranslationX(-21 * AndroidUtilities.density * diff);

                nameTextView.setTranslationY((float) Math.floor(avatarY) - (float) Math.ceil(AndroidUtilities.density) + (float) Math.floor(7 * AndroidUtilities.density * diff));
                onlineTextView.setTranslationY((float) Math.floor(avatarY) + AndroidUtilities.dp(22) + (float) Math.floor(11 * AndroidUtilities.density) * diff);

                float scale = 1.0f + 0.12f * diff;
                nameTextView.setScaleX(scale);
                nameTextView.setScaleY(scale);
                if (true) {
                    int viewWidth;
                    if (AndroidUtilities.isTablet()) {
                        viewWidth = AndroidUtilities.dp(490);
                    } else {
                        viewWidth = AndroidUtilities.displaySize.x;
                    }
                    int buttonsWidth = AndroidUtilities.dp(118 + 8 + 40 + 48);
                    int minWidth = viewWidth - buttonsWidth;

                    int width = (int) (viewWidth - buttonsWidth * Math.max(0.0f, 1.0f - (diff != 1.0f ? diff * 0.15f / (1.0f - diff) : 1.0f)) - nameTextView.getTranslationX());
                    float width2 = nameTextView.getPaint().measureText(nameTextView.getText().toString()) * scale;
                    layoutParams = (FrameLayout.LayoutParams) nameTextView.getLayoutParams();
                    if (width < width2) {
                        layoutParams.width = Math.max(minWidth, (int) Math.ceil((width - AndroidUtilities.dp(24)) / (scale + (1.12f - scale) * 7.0f)));
                    } else {
                        layoutParams.width = (int) Math.ceil(width2);
                    }
                    layoutParams.width = (int) Math.min((viewWidth - nameTextView.getX()) / scale - AndroidUtilities.dp(8), layoutParams.width);
                    nameTextView.setLayoutParams(layoutParams);

                    width2 = onlineTextView.getPaint().measureText(onlineTextView.getText().toString());
                    layoutParams = (FrameLayout.LayoutParams) onlineTextView.getLayoutParams();
                    layoutParams.rightMargin = (int) Math.ceil(onlineTextView.getTranslationX() + AndroidUtilities.dp(8) + AndroidUtilities.dp(40) * (1.0f - diff));
                    if (width < width2) {
                        layoutParams.width = (int) Math.ceil(width);
                    } else {
                        layoutParams.width = LayoutHelper.WRAP_CONTENT;
                    }
                    onlineTextView.setLayoutParams(layoutParams);
                }
            }

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (shopMediaLayout != null) {
            shopMediaLayout.onResume();
        }

        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
        fixLayout();
    }

    private void checkListViewScroll(boolean animated) {

    }

    private void checkListViewScroll() {
        if (listView.getChildCount() <= 0) {
            return;
        }

        if (shopMediaLayoutAttached) {
            shopMediaLayout.setVisibleHeight(listView.getMeasuredHeight() - shopMediaLayout.getTop());
        }

        View child = listView.getChildAt(0);
        RecyclerListView.Holder holder = (RecyclerListView.Holder) listView.findContainingViewHolder(child);
        int top = child.getTop();
        int newOffset = 0;
        if (top >= 0 && holder != null && holder.getAdapterPosition() == 0) {
            newOffset = top;
        }
        if (extraHeight != newOffset) {
            extraHeight = newOffset;
            topView.invalidate();
            needLayout();
        }
    }

    private void fixLayout() {
        if (fragmentView == null) {
            return;
        }
        fragmentView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (fragmentView != null) {
                    checkListViewScroll(true);
                    needLayout();
                    fragmentView.getViewTreeObserver().removeOnPreDrawListener(this);
                }
                return true;
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (shopMediaLayout != null) {
            shopMediaLayout.onConfigurationChanged(newConfig);
        }

        fixLayout();
    }

    private void updateUiData(){
        if(currentShop == null){
            return;
        }
        avatarDrawable = new AvatarDrawable();
        avatarDrawable.setAvatarType(AvatarDrawable.AVATAR_TYPE_FILTER_CHANNELS);
        avatarDrawable.setColor(Theme.getColor(Theme.key_avatar_backgroundInProfileBlue));
        if (avatarImage != null) {

            if(currentShop.profilePic != null)
                avatarImage.setImage(currentShop.profilePic.photo, "50_50", avatarDrawable);
            nameTextView.setText(currentShop.title);
            onlineTextView.setText(ShopUtils.formatShopAbout(currentShop));
        }

        if(otherItem != null){
            otherItem.setVisibility(View.VISIBLE);
        }

        if(currentShop.isAdmin){
            productMenuItem.setVisibility(View.VISIBLE);
        }else{
            productMenuItem.setVisibility(View.GONE);
        }

        if(qrItem != null){
          //  qrItem.setVisibility(View.VISIBLE);
        }

    }

    private void showData(){
        listView.setAlpha(0f);
        listView.setVisibility(View.VISIBLE);
        listView.animate().alpha(1f).setDuration(300).setListener(null);
        progressView.animate().alpha(0f).setStartDelay(300).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                progressView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if(id == NotificationCenter.didShopLoaded){
            int class_guid = (int)args[2];
            if(class_guid == classGuid){
                boolean loaded = (boolean)args[0];
                if(loadingShop && shopMediaLayout != null){
                    shopMediaLayout.refresh();
                }
                loadingShop = false;
                requestId = 0;
                if(retryLayout != null){
                    retryLayout.setVisibility(View.GONE);
                }
                if(progressView != null){
                    progressView.setVisibility(View.GONE);
                }
                if(loaded){
                    currentShop = (ShopDataSerializer.Shop)args[1];
                    updateRowsIds();
                    if(shopMediaLayout != null){
                        shopMediaLayout.setCurrentShop(currentShop);
                    }
                    if(shopInfoInnerCell != null){
                        shopInfoInnerCell.setShop(currentShop);
                    }
                    showData();
                    updateUiData();
                }else{

                    APIError apiError = (APIError)args[1];
                    if(currentShop == null){
                        if(retryLayout != null){
                            retryLayout.setVisibility(View.VISIBLE);
                            if(apiError.status() != -1){
                                retryLayout.setApiError(apiError);
                            }
                        }
                    }
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
            return false;
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = null;
            switch (viewType) {

                case 1: {
                    view = new ShadowSectionCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
                    break;
                }
                case 11: {
                    view = new View(mContext) {
                        @Override
                        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(32), MeasureSpec.EXACTLY));
                        }
                    };
                    break;
                }
                case 3: {
                    if (shopMediaLayout.getParent() != null) {
                        ((ViewGroup) shopMediaLayout.getParent()).removeView(shopMediaLayout);
                    }
                    view = shopMediaLayout;
                    break;
                }
                 case  4:
                    if(shopActionLayout != null){
                        ViewGroup parent1 = (ViewGroup) shopActionLayout.getParent();
                        if(parent1 != null){
                            parent1.removeView(shopActionLayout);
                        }
                    }
                    view = shopActionLayout;
                    break;
            }
            if (viewType != 3) {
                view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            }
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        }

        @Override
        public int getItemViewType(int position) {
             if(position == productSecRow){
                return 1;
            }else if(position == productRow){
                return 3;
            }else if(position == shopActionRow){
                return 4;
            }
            return super.getItemViewType(position);
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
            if (holder.itemView == shopMediaLayout) {
                shopMediaLayoutAttached = true;
            }
        }

        @Override
        public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
            if (holder.itemView == shopMediaLayout) {
                shopMediaLayoutAttached = false;
            }
        }
    }


}
