package org.plus.wallet;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.plus.apps.business.ShopUtils;
import org.plus.apps.wallet.ProviderBottomSheet;
import org.plus.apps.wallet.cells.WalletCreatedCell;
import org.plus.net.APIError;
import org.plus.net.ErrorUtils;
import org.plus.wallet.cells.WalletBalanceCell;
import org.plus.wallet.cells.WalletDateCell;
import org.plus.wallet.cells.WalletSyncCell;
import org.plus.wallet.cells.WalletTransactionView;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.ContactsActivity;

import java.util.ArrayList;
import java.util.HashMap;

public class WalletActivity extends BaseFragment implements NotificationCenter. NotificationCenterDelegate ,WalletActionSheet.WalletActionSheetDelegate{

    private ArrayList<String> sections = new ArrayList<>();
    private ArrayList<WalletModel.Transaction> transactions = new ArrayList<>();
    private HashMap<String, ArrayList<WalletModel.Transaction>> sectionArrays = new HashMap<>();

    private WalletModel.Wallet wallet;

    private RecyclerListView listView;
    private SwipeRefreshLayout refreshLayout;
    private LinearLayoutManager layoutManager;
    private Drawable pinnedHeaderShadowDrawable;
    private Paint blackPaint = new Paint();
    private GradientDrawable backgroundDrawable;

    private WalletAdapter adapter;

    private WalletActionSheet walletActionSheet;

    private boolean walletLoaded;

    private boolean transactionsEndReached;

    private boolean loadingTransactions;
    private boolean loadingWallet;

    private String nextTransactions;


    private final static String PENDING_KEY = "pending";

    private float[] radii;

    private final int menu_settings = 1;

    private WalletBalanceCell walletBalanceCell;

    @Override
    protected ActionBar createActionBar(Context context) {
        ActionBar actionBar = new ActionBar(context);
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setBackgroundColor(Theme.getColor(Theme.key_wallet_blackBackground));
        actionBar.setTitleColor(Theme.getColor(Theme.key_wallet_whiteText));
        actionBar.setItemsColor(Theme.getColor(Theme.key_wallet_whiteText), false);
        actionBar.setItemsBackgroundColor(Theme.getColor(Theme.key_wallet_blackBackgroundSelector), false);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if (id == menu_settings) {
                }
            }
        });
        ActionBarMenu menu = actionBar.createMenu();
        menu.addItem(menu_settings, R.drawable.notifications_settings);
        return actionBar;
    }

    @Override
    public boolean onFragmentCreate() {
        getNotificationCenter().addObserver(this, NotificationCenter.didWalletLoaded);
        getNotificationCenter().addObserver(this,NotificationCenter.didTransactionLoaded);
        loadWallet();

        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        getNotificationCenter().removeObserver(this,NotificationCenter.didWalletLoaded);
        getNotificationCenter().removeObserver(this,NotificationCenter.didTransactionLoaded);
        super.onFragmentDestroy();
    }


    @Override
    public View createView(Context context) {

        WalletUtils.loadStatusDrawable();
        blackPaint.setColor(Theme.getColor(Theme.key_wallet_blackBackground));
        backgroundDrawable = new GradientDrawable();
        backgroundDrawable.setShape(GradientDrawable.RECTANGLE);
        int r = AndroidUtilities.dp(13);
        backgroundDrawable.setCornerRadii(radii = new float[] { r, r, r, r, 0, 0, 0, 0 });
        backgroundDrawable.setColor(Theme.getColor(Theme.key_wallet_whiteBackground));

        FrameLayout frameLayout = new FrameLayout(context) {
            @Override
            protected void onDraw(Canvas canvas) {
                int bottom;
                RecyclerView.ViewHolder holder = listView.findViewHolderForAdapterPosition(0);
                if (holder != null) {
                    bottom = holder.itemView.getBottom();
                } else {
                    bottom = 0;
                }
                float rad = AndroidUtilities.dp(13);
                if (bottom < rad) {
                    rad *= bottom / rad;
                }
                radii[0] = radii[1] = radii[2] = radii[3] = rad;
                canvas.drawRect(0, 0, getMeasuredWidth(), bottom + AndroidUtilities.dp(6), blackPaint);
                backgroundDrawable.setBounds(0, bottom - AndroidUtilities.dp(7), getMeasuredWidth(), getMeasuredHeight());
                backgroundDrawable.draw(canvas);
            }
        };
        frameLayout.setWillNotDraw(false);
        fragmentView = frameLayout;

        pinnedHeaderShadowDrawable = context.getResources().getDrawable(R.drawable.photos_header_shadow);
        pinnedHeaderShadowDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundGrayShadow), PorterDuff.Mode.MULTIPLY));

        listView = new RecyclerListView(context);
        listView.setSectionsType(2);
        listView.setItemAnimator(null);
        listView.setAdapter(adapter = new WalletAdapter(context));
        listView.setPinnedHeaderShadowDrawable(pinnedHeaderShadowDrawable);
        listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

            }
        });
        listView.setLayoutManager(layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setGlowColor(Theme.getColor(Theme.key_wallet_blackBackground));
        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                fragmentView.invalidate();
                if (!loadingTransactions && !transactionsEndReached) {
                    int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                    int visibleItemCount = lastVisibleItem == RecyclerView.NO_POSITION ? 0 : lastVisibleItem;
                    if (visibleItemCount > 0 && lastVisibleItem > adapter.getItemCount() - 4 && !transactionsEndReached) {
                        loadTransactions();
                    }
                }
            }
        });

        refreshLayout = new SwipeRefreshLayout(context);
        frameLayout.addView(refreshLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        refreshLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fromRefresh = true;
                loadWallet();
               // loadTransactions();
            }
        });


        return fragmentView;
    }

    private boolean fromRefresh;

    @Override
    public void onResume() {
        super.onResume();
        if(adapter != null){
            adapter.notifyDataSetChanged();
        }

        if(walletActionSheet != null){
            walletActionSheet.onResume();
        }
    }

    @Override
    public void sendOtp(TLRPC.User user, WalletModel.Provider provider, double amount, String comment, int type) {
        WalletController.getInstance(currentAccount).sendOtp(provider, user.phone, new WalletController.ResponseCallback() {
            @Override
            public void onResponse(Object response, APIError apiError) {
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        WalletActionSheet walletActionSheet = new WalletActionSheet(getParentActivity(),WalletActionSheet.TYPE_OTP,wallet,provider,user);
                        walletActionSheet.show();
                    }
                });


            }
        });

    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if(id == NotificationCenter.didWalletLoaded){
            loadingWallet = false;
            boolean loaded = (boolean)args[0];
            if(refreshLayout != null && refreshLayout.isRefreshing()){
                refreshLayout.setRefreshing(false);
            }
            if(loaded){
                wallet = (WalletModel.Wallet)(args[1]);
                walletLoaded = true;
                if(walletBalanceCell != null){
                    walletBalanceCell.setBalance(wallet.balance);
                }else{
                    if(adapter != null){
                        adapter.notifyDataSetChanged();
                    }
                }
                loadTransactions();
            }else{
                APIError apiError =(APIError)(args[1]);
                if(walletBalanceCell != null){
                    if(apiError.status() == ErrorUtils.NETWORK_ERROR){
                        walletBalanceCell.setInfo("network error!");
                    }else{
                        walletBalanceCell.setInfo("unknown error!try again later");
                    }
                }
            }
        }else if(id == NotificationCenter.didTransactionLoaded){
            if(refreshLayout != null && refreshLayout.isRefreshing()){
                refreshLayout.setRefreshing(false);
            }
            loadingTransactions = false;
            boolean loaded = (boolean)args[0];
            if(loaded){
                if(fromRefresh){
                    sections.clear();
                    transactions.clear();
                    sectionArrays.clear();
                    fromRefresh = false;
                }
                int oldItemCount;
                if (adapter != null) {
                    oldItemCount = adapter.getItemCount();
                    adapter.notifySectionsChanged();
                } else {
                    oldItemCount = 0;
                }

                nextTransactions = (String) args[2];
                transactionsEndReached = ShopUtils.isEmpty(nextTransactions);
                ArrayList<WalletModel.Transaction> transactionArrayList  = (ArrayList<WalletModel.Transaction>)args[1];
                parseTransaction(transactionArrayList);

                int newItemCount = adapter.getItemCount();
                if (oldItemCount > 1) {
                    adapter.notifyItemChanged(oldItemCount - 1);
                }
                if (newItemCount > oldItemCount) {
                    adapter.notifyItemRangeInserted(oldItemCount, newItemCount);
                }

            }
        }
    }



    private void parseTransaction(ArrayList<WalletModel.Transaction> arrayList){
        int count = arrayList.size();
        for(int a = 0; a < count; a++){
            WalletModel.Transaction currentTransaction = arrayList.get(a);
            String monthKey  =  WalletUtils.getDateKey(currentTransaction.created_at);
            ArrayList<WalletModel.Transaction> transactionArrayList = sectionArrays.get(monthKey);
            if (transactionArrayList == null) {
                transactionArrayList = new ArrayList<>();
                sectionArrays.put(monthKey, transactionArrayList);
                sections.add(monthKey);
            }
            transactionArrayList.add(currentTransaction);
            transactions.add(currentTransaction);
        }
    }



    public void loadWallet(){
        if(loadingWallet){
            return;
        }
        loadingWallet = true;
        getWalletController().loadWallet();
    }

    private void loadTransactions(){
        if(loadingTransactions){
            return;
        }
        loadingTransactions = true;
        getWalletController().loadTransaction(nextTransactions);
    }

    private void deposit(TLRPC.User user){
        if(getParentActivity() == null){
            return;
        }
        if(wallet == null){
            AlertsCreator.showSimpleAlert(WalletActivity.this, LocaleController.getString("Wallet", R.string.Wallet), LocaleController.getString("WalletPendingWait", R.string.WalletPendingWait));
            return;
        }
        ProviderBottomSheet providerBottomSheet = new ProviderBottomSheet(getParentActivity(),false);
        providerBottomSheet.setDelegate(new ProviderBottomSheet.ProviderDelegate() {
            @Override
            public void didSelected(WalletModel.Provider provider) {
                if(provider == null){
                    return;
                }
                walletActionSheet = new WalletActionSheet(getParentActivity(),WalletActionSheet.TYPE_DEPOSIT,wallet, provider,user);
                walletActionSheet.setDelegate(WalletActivity.this);
                walletActionSheet.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if(walletActionSheet == dialog){
                            walletActionSheet = null;
                        }
                    }
                });
                walletActionSheet.show();
            }
        });
        showDialog(providerBottomSheet);
    }

    private void transfer(){
        if(wallet == null){
            AlertsCreator.showSimpleAlert(WalletActivity.this, LocaleController.getString("Wallet", R.string.Wallet), LocaleController.getString("WalletPendingWait", R.string.WalletPendingWait));
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putBoolean("onlyUsers",true);
        bundle.putBoolean("destroyAfterSelect",true);
        bundle.putBoolean("returnAsResult",true);
        bundle.putBoolean("allowBots",false);
        bundle.putBoolean("allowSelf",false);
        bundle.putBoolean("disableSections",false);
        ContactsActivity contactsActivity = new ContactsActivity(bundle);
        contactsActivity.setDelegate(new ContactsActivity.ContactsActivityDelegate() {
            @Override
            public void didSelectContact(TLRPC.User user, String param, ContactsActivity activity) {
                 walletActionSheet = new WalletActionSheet(getParentActivity(),WalletActionSheet.TYPE_SEND,wallet,user);
                 walletActionSheet.setDelegate(WalletActivity.this);
                 walletActionSheet.setOnDismissListener(new DialogInterface.OnDismissListener() {
                     @Override
                     public void onDismiss(DialogInterface dialog) {
                         if(walletActionSheet == dialog){
                             walletActionSheet = null;
                         }
                     }
                 });
                 walletActionSheet.show();
            }
        });
        presentFragment(contactsActivity);

    }



    public void showDepositToAlert(){
        if(getParentActivity() == null || wallet == null){
            return;
        }
        BottomSheet.Builder builder = new BottomSheet.Builder(getParentActivity());
        builder.setTitle("Deposit To");
        builder.setApplyBottomPadding(true);
        builder.setUseFullscreen(false);
        String[] texts = {"My Wallet","For other"};
        int[] res = {R.drawable.menu_wallet,R.drawable.menu_contacts};
        builder.setItems(texts, res, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0){
                    deposit(null);
                }else {
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("onlyUsers",true);
                    bundle.putBoolean("destroyAfterSelect",true);
                    bundle.putBoolean("returnAsResult",true);
                    bundle.putBoolean("allowBots",false);
                    bundle.putBoolean("allowSelf",false);
                    bundle.putBoolean("disableSections",false);
                    ContactsActivity contactsActivity = new ContactsActivity(bundle);
                    contactsActivity.setDelegate(new ContactsActivity.ContactsActivityDelegate() {
                        @Override
                        public void didSelectContact(TLRPC.User user, String param, ContactsActivity activity) {
                            deposit(user);
                        }
                    });
                    presentFragment(contactsActivity);
                }
            }
        });
        showDialog(builder.create());

    }

    @Override
    public void openContact() {
        if(walletActionSheet != null){
            walletActionSheet.dismiss();
            walletActionSheet = null;
        }
        transfer();
    }

    private class WalletAdapter extends RecyclerListView.SectionsAdapter{

        private Context context;

        public WalletAdapter(Context c) {
            context = c;
        }

        @Override
        public String getLetter(int position) {
            return null;
        }

        @Override
        public int getPositionForScrollProgress(float progress) {
            return 0;
        }

        @Override
        public int getSectionCount() {
            int count = 1;
            if (!sections.isEmpty()) {
                count += sections.size() + 1;
            }
            return count;
        }

        @Override
        public int getCountForSection(int section) {
            if (section == 0) {
                return sections.isEmpty() ? 2 : 1;
            }
            section -= 1;
            if (section < sections.size()) {
                return sectionArrays.get(sections.get(section)).size() + 1;
            } else {
                return 1;
            }
        }

        @Override
        public boolean isEnabled(int section, int row) {
            return section != 0 && row != 0;
        }

        @Override
        public int getItemViewType(int section, int position) {
            if (section == 0) {
                if(position == 0){
                    return 0;
                }else{
                    if(loadingTransactions){
                        return 4;
                    }else{
                        return 2;
                    }
                }
            } else {
                section -= 1;
                if (section < sections.size()) {
                    return position == 0 ? 3 : 1;
                } else {
                    return 5;
                }
            }
        }

        @Override
        public Object getItem(int section, int position) {
            return null;
        }



        @Override
        public View getSectionHeaderView(int section, View view) {
            if (view == null) {
                view = new WalletDateCell(context);
                view.setBackgroundColor(Theme.getColor(Theme.key_wallet_whiteBackground) & 0xe5ffffff);
            }
            WalletDateCell dateCell = (WalletDateCell) view;
            if (section == 0) {
                dateCell.setAlpha(0.0f);
            } else {
                section -= 1;
                if (section < sections.size()) {
                    view.setAlpha(1.0f);
                    String key = sections.get(section);
                    dateCell.setDate(key);
                }
            }
            return view;
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 0: {
                  view = walletBalanceCell  = new WalletBalanceCell(context) {
                        @Override
                        protected void onDepositPressed() {
                            showDepositToAlert();
                        }

                        @Override
                        protected void onSendPressed() {
                            transfer();
                        }

                    };
                    break;
                }
                case 1: {
                    view = new WalletTransactionView(context);
                    break;
                }
                case 2: {
                    view = new WalletCreatedCell(context) {
                        @Override
                        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                            int height = Math.max(AndroidUtilities.dp(280), fragmentView.getMeasuredHeight() - AndroidUtilities.dp(236 + 6));
                            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
                        }
                    };
                    break;
                }
                case 3: {
                    view = new WalletDateCell(context);
                    break;
                }
                case 5: {
                    view  = new View(context) {
                        @Override
                        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                            int n = listView.getChildCount();
                            int itemsCount = adapter.getItemCount();
                            int totalHeight = 0;
                            for (int i = 0; i < n; i++) {
                                View view = listView.getChildAt(i);
                                int pos = listView.getChildAdapterPosition(view);
                                if (pos != 0 && pos != itemsCount - 1) {
                                    totalHeight += listView.getChildAt(i).getMeasuredHeight();
                                }
                            }
                            int paddingHeight = fragmentView.getMeasuredHeight() - totalHeight;
                            if (paddingHeight <= 0) {
                                paddingHeight = 0;
                            }
                            setMeasuredDimension(listView.getMeasuredWidth(), paddingHeight);
                        }
                    };
                    break;
                }
                case 4:
                default: {
                    view = new WalletSyncCell(context) {
                        @Override
                        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                            int height = Math.max(AndroidUtilities.dp(280), fragmentView.getMeasuredHeight() - AndroidUtilities.dp(236 + 6));
                            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
                        }
                    };
                    break;
                }
            }
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(int section, int position, RecyclerView.ViewHolder holder) {
            switch (holder.getItemViewType()) {
                case 0: {
                    WalletBalanceCell balanceCell = (WalletBalanceCell) holder.itemView;
                    if(wallet != null){
                        balanceCell.setBalance(wallet.balance);
                    }else{
                        balanceCell.setBalance(-1);
                    }
                    break;
                }
                case 1: {
                    WalletTransactionView transactionView = (WalletTransactionView)holder.itemView;
                    section -= 1;
                    String key = sections.get(section);
                    ArrayList<WalletModel.Transaction> arrayList = sectionArrays.get(key);
                    transactionView.setTransaction(arrayList.get(position - 1),position != arrayList.size());
                    break;
                }
                case 2: {
                    WalletCreatedCell createdCell = (WalletCreatedCell) holder.itemView;
                    createdCell.setAddress("walletAddress");
                    break;
                }
                case 3: {
                    WalletDateCell dateCell = (WalletDateCell) holder.itemView;
                    section -= 1;
                    String key = sections.get(section);
                    dateCell.setDate(key);
                    break;
                }
            }
        }
    }
}
