//package org.plus.apps.wallet;
//
//import android.animation.Animator;
//import android.animation.AnimatorListenerAdapter;
//import android.animation.ValueAnimator;
//import android.app.Dialog;
//import android.content.Context;
//import android.graphics.Canvas;
//import android.graphics.Paint;
//import android.graphics.PorterDuff;
//import android.graphics.PorterDuffColorFilter;
//import android.graphics.drawable.Drawable;
//import android.graphics.drawable.GradientDrawable;
//import android.os.Build;
//import android.os.Bundle;
//import android.text.Editable;
//import android.text.InputType;
//import android.text.Layout;
//import android.text.SpannableStringBuilder;
//import android.text.Spanned;
//import android.text.TextUtils;
//import android.text.TextWatcher;
//import android.text.style.RelativeSizeSpan;
//import android.util.Log;
//import android.util.TypedValue;
//import android.view.Gravity;
//import android.view.HapticFeedbackConstants;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.inputmethod.EditorInfo;
//import android.widget.FrameLayout;
//import android.widget.LinearLayout;
//import android.widget.ScrollView;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
//
//import com.google.android.exoplayer2.drm.ExoMediaDrm;
//
//import org.json.JSONObject;
//import org.plus.apps.business.ShopUtils;
//import org.plus.apps.wallet.cells.WalletBalanceCell;
//import org.plus.apps.wallet.cells.WalletCreatedCell;
//import org.plus.apps.wallet.cells.WalletDateCell;
//import org.plus.apps.wallet.cells.WalletSyncCell;
//import org.plus.apps.wallet.cells.WalletTransactionView;
//import org.plus.apps.wallet.component.PaymentProviderAlert;
//import org.telegram.messenger.AndroidUtilities;
//import org.telegram.messenger.LocaleController;
//import org.telegram.messenger.NotificationCenter;
//import org.telegram.messenger.R;
//import org.telegram.messenger.Utilities;
//import org.telegram.tgnet.TLRPC;
//import org.telegram.ui.ActionBar.ActionBar;
//import org.telegram.ui.ActionBar.ActionBarMenu;
//import org.telegram.ui.ActionBar.AlertDialog;
//import org.telegram.ui.ActionBar.BaseFragment;
//import org.telegram.ui.ActionBar.BottomSheet;
//import org.telegram.ui.ActionBar.SimpleTextView;
//import org.telegram.ui.ActionBar.Theme;
//import org.telegram.ui.CameraScanActivity;
//import org.telegram.ui.Cells.HeaderCell;
//import org.telegram.ui.Cells.PollEditTextCell;
//import org.telegram.ui.Cells.TextSettingsCell;
//import org.telegram.ui.Components.AlertsCreator;
//import org.telegram.ui.Components.CubicBezierInterpolator;
//import org.telegram.ui.Components.EditTextBoldCursor;
//import org.telegram.ui.Components.LayoutHelper;
//import org.telegram.ui.Components.PullForegroundDrawable;
//import org.telegram.ui.Components.RecyclerListView;
//import org.telegram.ui.ContactsActivity;
//
//import java.time.Instant;
//import java.util.ArrayList;
//import java.util.HashMap;
//
//public class WalletFragment extends BaseFragment implements NotificationCenter.NotificationCenterDelegate,WalletActionSheet.WalletActionSheetDelegate{
//
//    private ArrayList<String> sections = new ArrayList<>();
//    private ArrayList<WalletDataSerializer.Transaction> transactions = new ArrayList<>();
//    private HashMap<String, ArrayList<WalletDataSerializer.Transaction>> sectionArrays = new HashMap<>();
//
//    private WalletDataSerializer.Wallet wallet;
//
//    private WalletActionSheet walletActionSheet;
//
//    private float[] radii;
//
//    private SimpleTextView statusTextView;
//    private PullRecyclerView listView;
//    private LinearLayoutManager layoutManager;
//    private WalletAdapter adapter;
//    private Drawable pinnedHeaderShadowDrawable;
//
//    private boolean wasPulled;
//    private boolean canShowHiddenPull;
//    private long startArchivePullingTime;
//    private PullForegroundDrawable pullForegroundDrawable;
//
//    private long lastUpdateTime;
//
//    private Paint blackPaint = new Paint();
//    private GradientDrawable backgroundDrawable;
//
//    private static final int menu_settings = 1;
//
//    private static final int SHORT_POLL_DELAY = 3 * 1000;
//
//    private boolean walletLoaded;
//    private boolean loadingTransactions;
//    private boolean transactionLoaded;
//    private boolean transactionsEndReached;
//    private boolean loadingWallet;
//    private String nextTransactions;
//
//    private final static String PENDING_KEY = "pending";
//
//
//    private SwipeRefreshLayout refreshLayout;
//
//
//    @Override
//    protected ActionBar createActionBar(Context context) {
//        ActionBar actionBar = new ActionBar(context) {
//            @Override
//            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//                if (Build.VERSION.SDK_INT >= 21 && statusTextView != null) {
//                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) statusTextView.getLayoutParams();
//                    layoutParams.topMargin = AndroidUtilities.statusBarHeight;
//                }
//                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//            }
//        };
//
//        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
//        actionBar.setBackgroundColor(Theme.getColor(Theme.key_wallet_blackBackground));
//        actionBar.setTitleColor(Theme.getColor(Theme.key_wallet_whiteText));
//        actionBar.setItemsColor(Theme.getColor(Theme.key_wallet_whiteText), false);
//        actionBar.setItemsBackgroundColor(Theme.getColor(Theme.key_wallet_blackBackgroundSelector), false);
//        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
//            @Override
//            public void onItemClick(int id) {
//                if (id == -1) {
//                    finishFragment();
//                } else if (id == menu_settings) {
//                    presentFragment(new WalletSettingActivity());
//                }
//            }
//        });
//        ActionBarMenu menu = actionBar.createMenu();
//        menu.addItem(menu_settings, R.drawable.notifications_settings);
//
//        statusTextView = new SimpleTextView(context);
//        statusTextView.setTextSize(14);
//        statusTextView.setGravity(Gravity.CENTER);
//        statusTextView.setTextColor(Theme.getColor(Theme.key_wallet_statusText));
//        actionBar.addView(statusTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.BOTTOM, 48, 0, 48, 0));
//
//        return actionBar;
//    }
//
//    @Override
//    public boolean onFragmentCreate() {
//        getNotificationCenter().addObserver(this, NotificationCenter.didWalletLoaded);
//        getNotificationCenter().addObserver(this,NotificationCenter.didTransactionLoaded);
//
//        getWalletController().loadWallet();
//        getWalletController().loadTransaction(classGuid,null);
//
//        return super.onFragmentCreate();
//    }
//
//    @Override
//    public void onFragmentDestroy() {
//        getNotificationCenter().removeObserver(this,NotificationCenter.didWalletLoaded);
//        getNotificationCenter().removeObserver(this,NotificationCenter.didTransactionLoaded);
//        super.onFragmentDestroy();
//    }
//
//    @Override
//    public View createView(Context context) {
//        pullForegroundDrawable = new PullForegroundDrawable(LocaleController.getString("WalletSwipeToRefresh", R.string.WalletSwipeToRefresh), LocaleController.getString("WalletReleaseToRefresh", R.string.WalletReleaseToRefresh)) {
//            @Override
//            protected float getViewOffset() {
//                return listView.getViewOffset();
//            }
//        };
//        pullForegroundDrawable.setColors(Theme.key_wallet_pullBackground, Theme.key_wallet_releaseBackground);
//        pullForegroundDrawable.showHidden();
//        pullForegroundDrawable.setWillDraw(true);
//
//
//        blackPaint.setColor(Theme.getColor(Theme.key_wallet_blackBackground));
//        backgroundDrawable = new GradientDrawable();
//        backgroundDrawable.setShape(GradientDrawable.RECTANGLE);
//        int r = AndroidUtilities.dp(13);
//        backgroundDrawable.setCornerRadii(radii = new float[] { r, r, r, r, 0, 0, 0, 0 });
//        backgroundDrawable.setColor(Theme.getColor(Theme.key_wallet_whiteBackground));
//
//        FrameLayout frameLayout = new FrameLayout(context) {
//            @Override
//            protected void onDraw(Canvas canvas) {
//                int bottom;
//                RecyclerView.ViewHolder holder = listView.findViewHolderForAdapterPosition(1);
//                if (holder != null) {
//                    bottom = holder.itemView.getBottom();
//                } else {
//                    bottom = 0;
//                }
//                float rad = AndroidUtilities.dp(13);
//                if (bottom < rad) {
//                    rad *= bottom / rad;
//                }
//                bottom += viewOffset;
//                radii[0] = radii[1] = radii[2] = radii[3] = rad;
//                canvas.drawRect(0, 0, getMeasuredWidth(), bottom + AndroidUtilities.dp(6), blackPaint);
//                backgroundDrawable.setBounds(0, bottom - AndroidUtilities.dp(7), getMeasuredWidth(), getMeasuredHeight());
//                backgroundDrawable.draw(canvas);
//            }
//        };
//        frameLayout.setWillNotDraw(false);
//        fragmentView = frameLayout;
//
//        pinnedHeaderShadowDrawable = context.getResources().getDrawable(R.drawable.photos_header_shadow);
//        pinnedHeaderShadowDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundGrayShadow), PorterDuff.Mode.MULTIPLY));
//
//        refreshLayout = new SwipeRefreshLayout(context);
//        frameLayout.addView(refreshLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
//
//
//        listView = new PullRecyclerView(context);
//        listView.setSectionsType(2);
//        listView.setItemAnimator(null);
//        listView.setPinnedHeaderShadowDrawable(pinnedHeaderShadowDrawable);
//        listView.setLayoutManager(la  youtManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false) {
//            @Override
//            public boolean supportsPredictiveItemAnimations() {
//                return false;
//            }
//
//            @Override
//            public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
//                boolean isDragging = listView.getScrollState() == RecyclerView.SCROLL_STATE_DRAGGING;
//
//                int measuredDy = dy;
//                if (dy < 0) {
//                    listView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
//                    int currentPosition = layoutManager.findFirstVisibleItemPosition();
//                    if (currentPosition == 0) {
//                        View view = layoutManager.findViewByPosition(currentPosition);
//                        if (view != null && view.getBottom() <= AndroidUtilities.dp(1)) {
//                            currentPosition = 1;
//                        }
//                    }
//                    if (!isDragging) {
//                        View view = layoutManager.findViewByPosition(currentPosition);
//                        int dialogHeight = AndroidUtilities.dp(72) + 1;
//                        int canScrollDy = -view.getTop() + (currentPosition - 1) * dialogHeight;
//                        int positiveDy = Math.abs(dy);
//                        if (canScrollDy < positiveDy) {
//                            measuredDy = -canScrollDy;
//                        }
//                    } else if (currentPosition == 0) {
//                        View v = layoutManager.findViewByPosition(currentPosition);
//                        float k = 1f + (v.getTop() / (float) v.getMeasuredHeight());
//                        if (k > 1f) {
//                            k = 1f;
//                        }
//                        listView.setOverScrollMode(View.OVER_SCROLL_NEVER);
//                        measuredDy *= PullForegroundDrawable.startPullParallax - PullForegroundDrawable.endPullParallax * k;
//                        if (measuredDy > -1) {
//                            measuredDy = -1;
//                        }
//                    }
//                }
//
//                if (viewOffset != 0 && dy > 0 && isDragging) {
//                    float ty = (int) viewOffset;
//                    ty -= dy;
//                    if (ty < 0) {
//                        measuredDy = (int) ty;
//                        ty = 0;
//                    } else {
//                        measuredDy = 0;
//                    }
//                    listView.setViewsOffset(ty);
//                }
//
//                int usedDy = super.scrollVerticallyBy(measuredDy, recycler, state);
//                if (pullForegroundDrawable != null) {
//                    pullForegroundDrawable.scrollDy = usedDy;
//                }
//                int currentPosition = layoutManager.findFirstVisibleItemPosition();
//                View firstView = null;
//                if (currentPosition == 0) {
//                    firstView = layoutManager.findViewByPosition(currentPosition);
//                }
//                if (currentPosition == 0 && firstView != null && firstView.getBottom() >= AndroidUtilities.dp(4)) {
//                    if (startArchivePullingTime == 0) {
//                        startArchivePullingTime = System.currentTimeMillis();
//                    }
//                    if (pullForegroundDrawable != null) {
//                        pullForegroundDrawable.showHidden();
//                    }
//                    float k = 1f + (firstView.getTop() / (float) firstView.getMeasuredHeight());
//                    if (k > 1f) {
//                        k = 1f;
//                    }
//                    long pullingTime = System.currentTimeMillis() - startArchivePullingTime;
//                    boolean canShowInternal = k > PullForegroundDrawable.SNAP_HEIGHT && pullingTime > PullForegroundDrawable.minPullingTime + 20;
//                    if (canShowHiddenPull != canShowInternal) {
//                        canShowHiddenPull = canShowInternal;
//                        if (!wasPulled) {
//                            listView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
//                            if (pullForegroundDrawable != null) {
//                                pullForegroundDrawable.colorize(canShowInternal);
//                            }
//                        }
//                    }
//                    if (measuredDy - usedDy != 0 && dy < 0 && isDragging) {
//                        float ty;
//                        float tk = (viewOffset / PullForegroundDrawable.getMaxOverscroll());
//                        tk = 1f - tk;
//                        ty = (viewOffset - dy * PullForegroundDrawable.startPullOverScroll * tk);
//                        listView.setViewsOffset(ty);
//                    }
//                    if (pullForegroundDrawable != null) {
//                        pullForegroundDrawable.pullProgress = k;
//                        pullForegroundDrawable.setListView(listView);
//                    }
//                } else {
//                    startArchivePullingTime = 0;
//                    canShowHiddenPull = false;
//                    if (pullForegroundDrawable != null) {
//                        pullForegroundDrawable.resetText();
//                        pullForegroundDrawable.pullProgress = 0f;
//                        pullForegroundDrawable.setListView(listView);
//                    }
//                }
//                if (firstView != null) {
//                    firstView.invalidate();
//                }
//                return usedDy;
//            }
//        });
//        listView.setAdapter(adapter = new WalletAdapter(context));
//        listView.setGlowColor(Theme.getColor(Theme.key_wallet_blackBackground));
//        refreshLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
//        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                fragmentView.invalidate();
//                if (!loadingTransactions && !transactionsEndReached) {
//                    int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
//                    int visibleItemCount = lastVisibleItem == RecyclerView.NO_POSITION ? 0 : lastVisibleItem;
//                    if (visibleItemCount > 0 && lastVisibleItem > adapter.getItemCount() - 4 && !transactionsEndReached) {
//                        loadTransaction();
//                    }
//                }
//            }
//        });
//
//        listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener() {
//            @Override
//            public void onItemClick(View view, int position) {
//                if(view instanceof WalletTransactionView){
//                    WalletTransactionView walletTransactionView = (WalletTransactionView)view;
//                    WalletDataSerializer.Transaction transaction =  walletTransactionView.getTransaction();
//                    showTransactionDialog(transaction);
//                }
//            }
//        });
//
//        return fragmentView;
//    }
//
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        if (walletActionSheet != null) {
//            walletActionSheet.onResume();
//        }
//        if (adapter != null) {
//            adapter.notifyDataSetChanged();
//        }
//    }
//
//
//
//    public void openTransfer() {
//        if (getParentActivity() == null) {
//            return;
//        }
//        Bundle bundle = new Bundle();
//        bundle.putBoolean("onlyUsers",true);
//        bundle.putBoolean("destroyAfterSelect",true);
//        bundle.putBoolean("returnAsResult",true);
//        bundle.putBoolean("allowBots",false);
//        bundle.putBoolean("allowSelf",false);
//        bundle.putBoolean("disableSections",true);
//        ContactsActivity contactsActivity = new ContactsActivity(bundle);
//        contactsActivity.setDelegate((user, param, activity) -> openTransfer(user));
//        presentFragment(contactsActivity);
//    }
//
//    private void openTransfer(TLRPC.User user){
//        walletActionSheet = new WalletActionSheet(WalletFragment.this, WalletActionSheet.TYPE_SEND, user,null,true
//        );
//        walletActionSheet.setDelegate(this);
//        walletActionSheet.setOnDismissListener(dialog -> {
//            if (walletActionSheet == dialog) {
//                walletActionSheet = null;
//            }
//        });
//        walletActionSheet.show();
//    }
//
//
//
//    @Override
//    public boolean dismissDialogOnPause(Dialog dialog) {
//        if (dialog instanceof WalletActionSheet) {
//            return false;
//        }
//        return super.dismissDialogOnPause(dialog);
//    }
//
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        if (walletActionSheet != null) {
//            walletActionSheet.onPause();
//        }
//    }
//
//
//    private void loadTransaction(){
//       if(loadingTransactions){
//           return;
//       }
//       loadingTransactions = true;
//      // getWalletController().loadTransaction(classGuid,nextTransactions);
//
//    }
//
//    private boolean fromRefresh;
//    private void refreshWallet() {
//        if(loadingWallet){
//            return;
//        }
//        fromRefresh = true;
//        loadingWallet = true;
//        if(refreshLayout != null){
//            refreshLayout.setRefreshing(true);
//        }
//       // getWalletController().loadWallet();
//    }
//
//
//    @Override
//    public void didReceivedNotification(int id, int account, Object... args) {
//
//        if(id == NotificationCenter.didWalletLoaded){
//            loadingWallet = false;
//            boolean loaded = (boolean)args[0];
//            if(loaded){
//                wallet = (WalletDataSerializer.Wallet)(args[1]);
//                walletLoaded = true;
//                if(adapter != null){
//                    adapter.notifyDataSetChanged();
//                }
//                lastUpdateTime = getConnectionsManager().getCurrentTime();
//                loadTransaction();
//            }else{
//                if(refreshLayout != null && refreshLayout.isRefreshing()){
//                    refreshLayout.setRefreshing(false);
//                }
//                if(!walletLoaded){
//                    showDialog(ShopUtils.createConnectionAlert(getParentActivity(), this::refreshWallet).create());
//                }
//            }
//            loadingWallet = false;
//        }else if(id == NotificationCenter.didTransactionLoaded){
//            if(refreshLayout != null && refreshLayout.isRefreshing()){
//                refreshLayout.setRefreshing(false);
//            }
//            loadingTransactions = false;
//            boolean loaded = (boolean)args[0];
//            int guid = (int)args[2];
//            if(guid == classGuid){
//                if(loaded){
//                    ArrayList<WalletDataSerializer.Transaction> transactionArrayList  = (ArrayList<WalletDataSerializer.Transaction>)args[1];
//                    if(transactions.isEmpty() || fromRefresh){
//                        transactions.clear();
//                        sectionArrays.clear();
//                        sections.clear();
//                        transactions = transactionArrayList;
//                    }else{
//                        transactions.addAll(transactionArrayList);
//                    }
//                    nextTransactions = (String) args[3];
//                    transactionsEndReached = ShopUtils.isEmpty(nextTransactions);
//                    parseTransaction(transactionArrayList);
//                    if(adapter != null){
//                        adapter.notifyDataSetChanged();
//                    }
//                    lastUpdateTime = getConnectionsManager().getCurrentTime();
//
//                }
//
//            }
//
//
//        }
//
//    }
//
//    private void createDepositForProvider(String provider) {
//        if (getParentActivity() == null || provider == null) {
//             return;
//        }
//        walletActionSheet = new WalletActionSheet(WalletFragment.this, WalletActionSheet.TYPE_DEPOSIT, getUserConfig().getCurrentUser(),provider,false);
//        walletActionSheet.setDelegate(new WalletActionSheet.WalletActionSheetDelegate() {
//            @Override
//            public void onDeposit(double amount, String provider) {
//                deposit(provider,amount);
//            }
//        });
//        walletActionSheet.setOnDismissListener(dialog -> {
//            if (walletActionSheet == dialog) {
//                walletActionSheet = null;
//            }
//        });
//        walletActionSheet.show();
//    }
//
//
//
//
////    double depositAmount = 0;
////    private void createDepositForProvider(WalletDataSerializer.PaymentProvider provider){
////        if (getParentActivity() == null || provider == null || !provider.allow_deposit) {
////            return;
////        }
////
////        Context context = getParentActivity();
////        BottomSheet.Builder builder = new BottomSheet.Builder(context);
////        builder.setApplyBottomPadding(false);
////        builder.setApplyTopPadding(false);
////        builder.setUseFullWidth(false);
////
////        FrameLayout frameLayout = new FrameLayout(context) {
////            @Override
////            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
////                super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(472), MeasureSpec.EXACTLY));
////            }
////        };
////
////        TextView titleView = new TextView(context);
////        titleView.setLines(1);
////        titleView.setSingleLine(true);
////        titleView.setText("Deposit");
////        titleView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
////        titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
////        titleView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
////        titleView.setEllipsize(TextUtils.TruncateAt.END);
////        titleView.setGravity(Gravity.CENTER_VERTICAL);
////        frameLayout.addView(titleView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 21, 22, 21, 0));
////
////        LinearLayout linearLayout = new LinearLayout(context);
////        linearLayout.setOrientation(LinearLayout.VERTICAL);
////        frameLayout.addView(linearLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.LEFT, 0, 78, 0, 0));
////
////        TextView descriptionText = new TextView(context);
////        descriptionText.setTextColor(Theme.getColor(Theme.key_dialogTextGray2));
////        descriptionText.setGravity(Gravity.CENTER_HORIZONTAL);
////        descriptionText.setLineSpacing(AndroidUtilities.dp(2), 1);
////        descriptionText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
////        descriptionText.setText("Deposit from " + provider.name + " to you wallet");
////        descriptionText.setPadding(AndroidUtilities.dp(32), 0, AndroidUtilities.dp(32), 0);
////        linearLayout.addView(descriptionText, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 0, 0, 0));
////
////
////        HeaderCell headerCell = new HeaderCell(context);
////        headerCell.setText("Amount");
////        linearLayout.addView(headerCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 16, 20, 16, 16));
////
////
////
////        PollEditTextCell cell = new PollEditTextCell(context, null) {
////            @Override
////            protected boolean drawDivider() {
////                return false;
////            }
////
////            @Override
////            protected void onAttachedToWindow() {
////                super.onAttachedToWindow();
////                getTextView().requestFocus();
////            }
////        };
////
////        EditTextBoldCursor editText = cell.getTextView();
////        cell.setShowNextButton(true);
////        editText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
////        editText.setHintColor(Theme.getColor(Theme.key_dialogTextHint));
////        editText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
////        editText.setBackground(Theme.createEditTextDrawable(context, true));
////        editText.setImeOptions(editText.getImeOptions() | EditorInfo.IME_ACTION_NEXT);
////        editText.setCursorSize(AndroidUtilities.dp(30));
////        editText.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
////        SpannableStringBuilder stringBuilder = new SpannableStringBuilder("0.0");
////        stringBuilder.setSpan(new RelativeSizeSpan(0.73f), stringBuilder.length() - 1, stringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
////        editText.setHintText(stringBuilder);
////        editText.setInputType(InputType.TYPE_CLASS_PHONE);
//////        editText.setOnEditorActionListener((v, actionId, event) -> {
//////            if (actionId == EditorInfo.IME_ACTION_NEXT) {
//////                View commentView = linearLayout.getChildAt(commentRow);
//////                if (commentView != null) {
//////                    PollEditTextCell editTextCell = (PollEditTextCell) commentView;
//////                    editTextCell.getTextView().requestFocus();
//////                }
//////                return true;
//////            }
//////            return false;
//////        });
////        cell.addTextWatcher(new TextWatcher() {
////
////            private boolean ignoreTextChange;
////            private boolean adding;
////            private RelativeSizeSpan sizeSpan = new RelativeSizeSpan(0.73f);
////
////            @Override
////            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
////                adding = count == 0 && after == 1;
////            }
////
////            @Override
////            public void onTextChanged(CharSequence s, int start, int before, int count) {
////
////            }
////
////            @Override
////            public void afterTextChanged(Editable s) {
////                if (ignoreTextChange || editText.getTag() != null) {
////                    return;
////                }
////                depositAmount = Double.parseDouble(s.toString());
////            }
////        });
////        linearLayout.addView(cell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 42, Gravity.LEFT | Gravity.TOP, 16, 20, 16, 16));
////
////
////
////        TextView buttonTextView = new TextView(context);
////        buttonTextView.setPadding(AndroidUtilities.dp(34), 0, AndroidUtilities.dp(34), 0);
////        buttonTextView.setGravity(Gravity.CENTER);
////        buttonTextView.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
////        buttonTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
////
////        buttonTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
////        buttonTextView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(4), Theme.getColor(Theme.key_featuredStickers_addButton), Theme.getColor(Theme.key_featuredStickers_addButtonPressed)));
////        linearLayout.addView(buttonTextView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 42, Gravity.LEFT | Gravity.TOP, 16, 20, 16, 16));
////        //buttonTextView.setOnClickListener(v -> AndroidUtilities.openSharing(this, url));
////        buttonTextView.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                if(depositAmount <= 0){
////                    return;
////                }
////                deposit(provider,depositAmount);
////            }
////        });
////
////        ScrollView scrollView = new ScrollView(context);
////        scrollView.setVerticalScrollBarEnabled(false);
////        scrollView.addView(frameLayout, LayoutHelper.createScroll(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP));
////        if (Build.VERSION.SDK_INT >= 21) {
////            scrollView.setNestedScrollingEnabled(true);
////        }
////
////        builder.setCustomView(scrollView);
////        BottomSheet bottomSheet = builder.create();
////        bottomSheet.setCanDismissWithSwipe(false);
////        showDialog(bottomSheet);
////
////    }
//
//    private AlertDialog alertDialog;
//    private boolean depositing;
//    private void deposit(String provider,double amount){
//        try{
//            if(depositing){
//                return;
//            }
//            depositing = true;
//            if(true){
//                alertDialog = new AlertDialog(getParentActivity(),3);
//                alertDialog.setCanCacnel(false);
//                showDialog(alertDialog);
//
//                getWalletController().depositToWallet(getWalletController().createRequestBody(provider,amount,getUserConfig().getClientPhone()), (response, error) -> AndroidUtilities.runOnUIThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        depositing = false;
//                        if(alertDialog != null){
//                            alertDialog.cancel();
//                            alertDialog   = null;
//                        }
//                        if(error == null){
//                            showDialog(AlertsCreator.createSimpleAlert(getParentActivity(),"Deposit","Successfully deposited to your wallet").create());
//                        }else{
//                            showDialog(AlertsCreator.createSimpleAlert(getParentActivity(),"Deposit",error.message()).create());
//                        }
//                    }
//                }),provider);
//
//            }
//        }catch (Exception ignore){
//
//        }
//
//    }
//
//    @Override
//    public void onDeposit(double amount, String provider) {
//        deposit(provider,amount);
//    }
//
//    private void parseTransaction(ArrayList<WalletDataSerializer.Transaction> arrayList){
//        int count = arrayList.size();
//        for(int a = 0; a < count; a++){
//            WalletDataSerializer.Transaction currentTransaction = arrayList.get(a);
//            String monthKey  =  WalletController.getDateKey(currentTransaction.created_at);
//            ArrayList<WalletDataSerializer.Transaction> transactionArrayList = sectionArrays.get(monthKey);
//            if (transactionArrayList == null) {
//                transactionArrayList = new ArrayList<>();
//                sectionArrays.put(monthKey, transactionArrayList);
//                sections.add(monthKey);
//            }
//            transactionArrayList.add(currentTransaction);
//            sectionArrays.put(monthKey, transactionArrayList);
//        }
//    }
//
//
//    private class WalletAdapter extends RecyclerListView.SectionsAdapter {
//
//        private Context context;
//
//        public WalletAdapter(Context c) {
//            context = c;
//        }
//
//        @Override
//        public boolean isEnabled(int section, int row) {
//            return section != 0 && row != 0;
//        }
//
//        @Override
//        public Object getItem(int section, int position) {
//            return null;
//        }
//
//        @Override
//        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            View view;
//            switch (viewType) {
//                case 0: {
//                    view = new WalletBalanceCell(context) {
//                        @Override
//                        protected void onWithDrawPressed() {
//                            if(wallet == null){
//                                AlertsCreator.showSimpleAlert(WalletFragment.this, LocaleController.getString("Wallet", R.string.Wallet), LocaleController.getString("WalletPendingWait", R.string.WalletPendingWait));
//                                //return;
//                            }
//
//                            ProviderBottomSheet providerBottomSheet = new ProviderBottomSheet(context,false);
//                            showDialog(providerBottomSheet);
//
////                            PaymentProviderAlert providerAlert = new PaymentProviderAlert(context,false,PaymentProviderAlert.from_withdraw);
////                            providerAlert.setDelegate(new PaymentProviderAlert.PaymentProviderDelegate() {
////                                @Override
////                                public void didPressedProvider(WalletDataSerializer.PaymentProvider provider) {
////                                    //initPaymentProvider(provider);
////                                    providerAlert.dismiss();
////                                }
////                            });
////                            showDialog(providerAlert);
//
//                        }
//
//                        @Override
//                        protected void onDepositPressed() {
//                            if(wallet == null){
//                                AlertsCreator.showSimpleAlert(WalletFragment.this, LocaleController.getString("Wallet", R.string.Wallet), LocaleController.getString("WalletPendingWait", R.string.WalletPendingWait));
//                                return;
//                            }
//
//                            ProviderBottomSheet providerBottomSheet = new ProviderBottomSheet(context,false);
//                            providerBottomSheet.setDelegate(new ProviderBottomSheet.ProviderDelegate() {
//                                @Override
//                                public void didSelected(String key) {
//                                    createDepositForProvider(key);
//                                }
//                            });
//                            showDialog(providerBottomSheet);
//
//
////                            ProviderBottomSheet providerBottomSheet = new ProviderBottomSheet(context,false);
////                            showDialog(providerBottomSheet);
////                            PaymentProviderAlert providerAlert = new PaymentProviderAlert(context,false,PaymentProviderAlert.from_deposit);
////                            providerAlert.setDelegate(provider -> {
////                                createDepositForProvider(provider);
////                                providerAlert.dismiss();
////
////                            });
////                            showDialog(providerAlert);
//                        }
//
//
//
//                        @Override
//                        protected void onSendPressed() {
//                            if(wallet == null){
//                                AlertsCreator.showSimpleAlert(WalletFragment.this, LocaleController.getString("Wallet", R.string.Wallet), LocaleController.getString("WalletPendingWait", R.string.WalletPendingWait));
//                                return;
//                            }
//                            openTransfer();
//                        }
//
//                    };
//                    break;
//                }
//                case 1: {
//
//                    //  view = new WalletTransactionCell(context);
//
//                    view = new WalletTransactionView(context);
//
//                    //view = new TransactionCell(context,1);
//                    break;
//                }
//                case 2: {
//                    view = new WalletCreatedCell(context) {
//                        @Override
//                        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//                            int height = Math.max(AndroidUtilities.dp(280), fragmentView.getMeasuredHeight() - AndroidUtilities.dp(236 + 6));
//                            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
//                        }
//                    };
//                    break;
//                }
//                case 3: {
//                    view = new WalletDateCell(context);
//                    break;
//                }
//                case 4: {
//                    view = new View(context) {
//                        @Override
//                        protected void onDraw(Canvas canvas) {
//                            pullForegroundDrawable.draw(canvas);
//                        }
//
//                        @Override
//                        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//                            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(AndroidUtilities.dp(72)), MeasureSpec.EXACTLY));
//                        }
//                    };
//                    pullForegroundDrawable.setCell(view);
//                    break;
//                }
//                case 5: {
//                    view  = new View(context) {
//                        @Override
//                        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//                            int n = listView.getChildCount();
//                            int itemsCount = adapter.getItemCount();
//                            int totalHeight = 0;
//                            for (int i = 0; i < n; i++) {
//                                View view = listView.getChildAt(i);
//                                int pos = listView.getChildAdapterPosition(view);
//                                if (pos != 0 && pos != itemsCount - 1) {
//                                    totalHeight += listView.getChildAt(i).getMeasuredHeight();
//                                }
//                            }
//                            int paddingHeight = fragmentView.getMeasuredHeight() - totalHeight;
//                            if (paddingHeight <= 0) {
//                                paddingHeight = 0;
//                            }
//                            setMeasuredDimension(listView.getMeasuredWidth(), paddingHeight);
//                        }
//                    };
//                    break;
//                }
//                case 6:
//                default: {
//                    view = new WalletSyncCell(context) {
//                        @Override
//                        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//                            int height = Math.max(AndroidUtilities.dp(280), fragmentView.getMeasuredHeight() - AndroidUtilities.dp(236 + 6));
//                            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
//                        }
//                    };
//                    break;
//                }
//            }
//            return new RecyclerListView.Holder(view);
//        }
//
//        @Override
//        public void onBindViewHolder(int section, int position, RecyclerView.ViewHolder holder) {
//            switch (holder.getItemViewType()) {
//                case 0: {
//                    WalletBalanceCell balanceCell = (WalletBalanceCell) holder.itemView;
//                    if(wallet != null){
//                        balanceCell.setBalance(wallet.amount);
//                    }else{
//                        balanceCell.setBalance(-1);
//                    }
//                    break;
//                }
//                case 1: {
//                    WalletTransactionView transactionView = (WalletTransactionView)holder.itemView;
//                    section -= 1;
//                    String key = sections.get(section);
//                    ArrayList<WalletDataSerializer.Transaction> arrayList = sectionArrays.get(key);
//                    transactionView.setTransaction(arrayList.get(position - 1),position != arrayList.size());
//                    break;
//                }
//                case 2: {
//                    WalletCreatedCell createdCell = (WalletCreatedCell) holder.itemView;
//                    createdCell.setAddress("walletAddress");
//                    break;
//                }
//                case 3: {
//                    WalletDateCell dateCell = (WalletDateCell) holder.itemView;
//                    section -= 1;
//                    String key = sections.get(section);
//                    if (PENDING_KEY.equals(key)) {
//                        dateCell.setText(LocaleController.getString("WalletPendingTransactions", R.string.WalletPendingTransactions));
//                    } else {
//                        ArrayList<WalletDataSerializer.Transaction> arrayList = sectionArrays.get(key);
//                        dateCell.setDate(key);
//
//                    }
//                    break;
//                }
//            }
//        }
//
//        @Override
//        public int getItemViewType(int section, int position) {
//            if (section == 0) {
//                if (position == 0) {
//                    return 4;//wallet balance cell
//                } else if (position == 1) {
//                    return 0;//trasnaction cell
//                } else {
////                    if (walletLoaded) {
////                        return 2;//walelt has been created
////                    } else {
////                        return 6;
////                    }
//                    return 6;//wallet loading ell
//                }
//            } else {
//                section -= 1;
//                if (section < sections.size()) {
//                    return position == 0 ? 3 : 1;
//                } else {
//                    return 5;
//                }
//            }
//        }
//
//        @Override
//        public int getSectionCount() {
//            int count = 1;
//            if (!sections.isEmpty()) {
//                count += sections.size() + 1;
//            }
//            return count;
//        }
//
//        @Override
//        public int getCountForSection(int section) {
//            if (section == 0) {
//                return sections.isEmpty() ? 3 : 2;
//            }
//            section -= 1;
//            if (section < sections.size()) {
//                return sectionArrays.get(sections.get(section)).size() + 1;
//            } else {
//                return 1;
//            }
//        }
//
//        @Override
//        public View getSectionHeaderView(int section, View view) {
//            if (view == null) {
//                view = new WalletDateCell(context);
//                view.setBackgroundColor(Theme.getColor(Theme.key_wallet_whiteBackground) & 0xe5ffffff);
//            }
//            WalletDateCell dateCell = (WalletDateCell) view;
//            if (section == 0) {
//                dateCell.setAlpha(0.0f);
//            } else {
//                section -= 1;
//                if (section < sections.size()) {
//                    view.setAlpha(1.0f);
//                    String key = sections.get(section);
//                    ArrayList<WalletDataSerializer.Transaction> arrayList = sectionArrays.get(key);
//                    dateCell.setDate(key);
//                }
//            }
//            return view;
//        }
//
//        @Override
//        public String getLetter(int position) {
//            return null;
//        }
//
//        @Override
//        public int getPositionForScrollProgress(float progress) {
//            return 0;
//        }
//    }
//
//    public static float viewOffset = 0.0f;
//    public class PullRecyclerView extends RecyclerListView {
//
//        private boolean firstLayout = true;
//        private boolean ignoreLayout;
//
//        public PullRecyclerView(Context context) {
//            super(context);
//        }
//
//        public void setViewsOffset(float offset) {
//            viewOffset = offset;
//            int n = getChildCount();
//            for (int i = 0; i < n; i++) {
//                getChildAt(i).setTranslationY(viewOffset);
//            }
//            invalidate();
//            fragmentView.invalidate();
//        }
//
//        public float getViewOffset() {
//            return viewOffset;
//        }
//
//        @Override
//        protected void onMeasure(int widthSpec, int heightSpec) {
//            if (firstLayout) {
//                ignoreLayout = true;
//                layoutManager.scrollToPositionWithOffset(1, 0);
//                ignoreLayout = false;
//                firstLayout = false;
//            }
//            super.onMeasure(widthSpec, heightSpec);
//        }
//
//        @Override
//        public void requestLayout() {
//            if (ignoreLayout) {
//                return;
//            }
//            super.requestLayout();
//        }
//
//        @Override
//        public void setAdapter(RecyclerView.Adapter adapter) {
//            super.setAdapter(adapter);
//            firstLayout = true;
//        }
//
//        @Override
//        public void addView(View child, int index, ViewGroup.LayoutParams params) {
//            super.addView(child, index, params);
//            child.setTranslationY(viewOffset);
//        }
//
//        @Override
//        public void removeView(View view) {
//            super.removeView(view);
//            view.setTranslationY(0);
//        }
//
//        @Override
//        public void onDraw(Canvas canvas) {
//            if (pullForegroundDrawable != null && viewOffset != 0) {
//                pullForegroundDrawable.drawOverScroll(canvas);
//            }
//            super.onDraw(canvas);
//        }
//
//        @Override
//        public boolean onTouchEvent(MotionEvent e) {
//            int action = e.getAction();
//            if (action == MotionEvent.ACTION_DOWN) {
//                listView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
//                if (wasPulled) {
//                    wasPulled = false;
//                    if (pullForegroundDrawable != null) {
//                        pullForegroundDrawable.doNotShow();
//                    }
//                    canShowHiddenPull = false;
//                }
//            }
//            boolean result = super.onTouchEvent(e);
//            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
//                int currentPosition = layoutManager.findFirstVisibleItemPosition();
//                if (currentPosition == 0) {
//                    View view = layoutManager.findViewByPosition(currentPosition);
//                    int height = (int) (AndroidUtilities.dp(72) * PullForegroundDrawable.SNAP_HEIGHT);
//                    int diff = view.getTop() + view.getMeasuredHeight();
//                    if (view != null) {
//                        long pullingTime = System.currentTimeMillis() - startArchivePullingTime;
//                        listView.smoothScrollBy(0, diff, CubicBezierInterpolator.EASE_OUT_QUINT);
//                        if (diff >= height && pullingTime >= PullForegroundDrawable.minPullingTime) {
//                            wasPulled = true;
//                            refreshWallet();
//                        }
//
//                        if (viewOffset != 0) {
//                            ValueAnimator valueAnimator = ValueAnimator.ofFloat(viewOffset, 0f);
//                            valueAnimator.addUpdateListener(animation -> listView.setViewsOffset((float) animation.getAnimatedValue()));
//
//                            valueAnimator.setDuration((long) (350f - 120f * (viewOffset / PullForegroundDrawable.getMaxOverscroll())));
//                            valueAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
//                            listView.setScrollEnabled(false);
//                            valueAnimator.addListener(new AnimatorListenerAdapter() {
//                                @Override
//                                public void onAnimationEnd(Animator animation) {
//                                    super.onAnimationEnd(animation);
//                                    listView.setScrollEnabled(true);
//                                }
//                            });
//                            valueAnimator.start();
//                        }
//                    }
//                }
//            }
//            return result;
//        }
//    }
//
//
//
//    private void showTransactionDialog(WalletDataSerializer.Transaction transaction){
//        if(getParentActivity() == null || transactions == null){
//            return;
//        }
//        int count = 0;
//        int header = count++;
//        int amount = count++;
//        int transaction_fee = count++;
//        int transaction_type = count++;
//        int reason = count++;
//        int status = count++;
//        int created_at = count++;
//
//        transaction_fee = -1;
//
//        BottomSheet bottomSheet = new BottomSheet(getParentActivity(),false);
//        RecyclerListView listView = new RecyclerListView(getParentActivity());
//        listView.setLayoutManager(new LinearLayoutManager(getParentActivity(),RecyclerView.VERTICAL,false));
//        int finalCount = count;
//        listView.setAdapter(new RecyclerListView.SelectionAdapter() {
//            @Override
//            public boolean isEnabled(RecyclerView.ViewHolder holder) {
//                return false;
//            }
//
//            @NonNull
//            @Override
//            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//                View view = null;
//                switch (viewType){
//                    case 1:
//                        view = new TextSettingsCell(getParentActivity());
//                        break;
//                    case 0:
//                    default:
//                        view = new HeaderCell(getParentActivity());
//                        break;
//                }
//                return new RecyclerListView.Holder(view);
//            }
//
//            @Override
//            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//                if(holder.getItemViewType() == 1){
//                    TextSettingsCell textSettingsCell =  (TextSettingsCell)holder.itemView;
//                    if(position == amount){
//                        textSettingsCell.setTextAndValue("Amount",WalletController.formatCurrency(transaction.amount),true);
//                    }else  if(position == transaction_type){
//                        textSettingsCell.setTextAndValue("Type",String.valueOf(transaction.transaction_type),true);
//                    }else  if(position == reason){
//                        textSettingsCell.setTextAndValue("Reason",String.valueOf(transaction.reason),true);
//                    }else  if(position == status){
//                        textSettingsCell.setTextAndValue("Status",String.valueOf(transaction.status),true);
//                    }else  if(position == created_at){
//                        textSettingsCell.setTextAndValue("Date",LocaleController.formatDateChat(Instant.parse(transaction.created_at).toEpochMilli()/1000,true),true);
//                    }
//                }else if(holder.getItemViewType() == 0){
//                    HeaderCell headerCell = (HeaderCell)holder.itemView;
//                    headerCell.setText("Transaction Detail");
//                }
//
//
//            }
//
//            @Override
//            public int getItemViewType(int position) {
//                if(position == header){
//                    return 0;
//                }else{
//                    return 1;
//                }
//            }
//
//            @Override
//            public int getItemCount() {
//                return finalCount;
//            }
//        });
//        bottomSheet.setCustomView(listView);
//        showDialog(bottomSheet);
//
//    }
//
//
//
//}
