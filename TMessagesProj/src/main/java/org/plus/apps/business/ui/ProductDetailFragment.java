package org.plus.apps.business.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.plus.apps.business.ShopUtils;
import org.plus.apps.business.data.SR_object;
import org.plus.apps.business.data.ShopDataController;
import org.plus.apps.business.data.ShopDataSerializer;
import org.plus.apps.business.ui.components.OfferBottomSheet;
import org.plus.apps.business.ui.components.ProductPagerCell;
import org.plus.features.data.FeatureDataModel;
import org.plus.net.APIError;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.AboutLinkCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RadialProgressView;
import org.telegram.ui.Components.ShareAlert;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.ProfileActivity;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;


@SuppressLint("ViewConstructor")
public class ProductDetailFragment extends BaseFragment implements NotificationCenter.NotificationCenterDelegate{

    public static final String TAG = ProductDetailFragment.class.getSimpleName();

    public static final String ui_picture = "ui_picture";
    public static final String ui_header_body = "ui_header_body";
    public static final String ui_table = "ui_table";
    public static final String ui_location = "location";
    public static final String ui_input_price = "ui_input_price";
    public static final String ui_input_title = "ui_input_title";
    public static final String ui_input_str = "ui_input_str";

    private View actionBarBackground;
    private AnimatorSet actionBarAnimator;

    private Gson gson = new Gson();
    private ArrayList<ShopDataSerializer.FieldSet> fieldSetArrayList = new ArrayList<>();
    private HashMap<String,Object> productFull = new HashMap<>();
    private int  product_id;
    private ShopDataSerializer.Shop currentShop;
    private String currentBusiness;

    private int chat_id;

    private  AlertDialog alertDialog;

    private ProductPagerCell productPagerCell;
    private ScrollView scrollView;
    private LinearLayout emptyLayout;
    private RadialProgressView radialProgressView;
    private LinearLayout linearLayout;

    private BottomLayout bottomContactLayout;

    private boolean productLoaded;
    private boolean confLoaded;
    private boolean shopInfoAdded;

    private boolean isAdmin;

    private final int[] location = new int[2];

    public static class TableItem{
        public String key;
        public String value;
        public String image;
    }

    public ProductDetailFragment(Bundle args) {
        super(args);
    }

    private ActionBarMenuItem like_menu;

    private final static int like_item = 1;
    private final static int share_item = 2;
    private final static int more_item = 3;
    private final static int qr_item = 4;

    public String section;

    @Override
    public boolean onFragmentCreate() {
        product_id = getArguments().getInt("item_id");
        chat_id = getArguments().getInt("chat_id");;
        if (chat_id == 0 || product_id == 0) {
            return false;
        }
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.productFullLoaded);
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.didOfferCreated);
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.didConfigrationLoaded);

        getShopDataController().loadProductFull(chat_id,product_id,classGuid);
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.productFullLoaded);
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.didOfferCreated);
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.didConfigrationLoaded);
        super.onFragmentDestroy();
    }


    private CombinedDrawable createDetailMenuDrawable(int res){
        CombinedDrawable menuButton = Theme.createCircleDrawableWithIcon(AndroidUtilities.dp(32), res);
        menuButton.setIconSize(AndroidUtilities.dp(16), AndroidUtilities.dp(16));
        Theme.setCombinedDrawableColor(menuButton, Theme.getColor(Theme.key_actionBarDefault), false);
        Theme.setCombinedDrawableColor(menuButton, Theme.getColor(Theme.key_dialogTextBlack), true);
        return  menuButton;
    }


    private void showProductSheet(String url) {
        if (getParentActivity() == null) {
            return;
        }
        Context context = getParentActivity();
        BottomSheet.Builder builder = new BottomSheet.Builder(context);
        builder.setApplyBottomPadding(false);
        builder.setApplyTopPadding(false);
        builder.setUseFullWidth(false);


        FrameLayout frameLayout = new FrameLayout(context) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(472), MeasureSpec.EXACTLY));
            }
        };

        TextView titleView = new TextView(context);
        titleView.setLines(1);
        titleView.setSingleLine(true);
        titleView.setText("Product link");
        titleView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        titleView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        titleView.setEllipsize(TextUtils.TruncateAt.END);
        titleView.setGravity(Gravity.CENTER_VERTICAL);
        frameLayout.addView(titleView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 21, 22, 21, 0));

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        frameLayout.addView(linearLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.LEFT, 0, 78, 0, 0));

        TextView descriptionText = new TextView(context);
        descriptionText.setTextColor(Theme.getColor(Theme.key_dialogTextGray2));
        descriptionText.setGravity(Gravity.CENTER_HORIZONTAL);
        descriptionText.setLineSpacing(AndroidUtilities.dp(2), 1);
        descriptionText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        descriptionText.setText("Scan the qr code to open the prodcut");

        descriptionText.setPadding(AndroidUtilities.dp(32), 0, AndroidUtilities.dp(32), 0);
        linearLayout.addView(descriptionText, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 0, 0, 0));

        TextView addressValueTextView = new TextView(context);

        ImageView imageView = new ImageView(context);
        imageView.setImageBitmap(ShopUtils.createTonQR(context, url, null));
        linearLayout.addView(imageView, LayoutHelper.createLinear(190, 190, Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 16, 0, 0));
        imageView.setOnLongClickListener(v -> {
           ShopUtils.shareBitmap(getParentActivity(), v, url);
            return true;
        });

        ActionBarMenuItem menuItem = new ActionBarMenuItem(context, null, 0, Theme.getColor(Theme.key_dialogTextBlack));
        menuItem.setLongClickEnabled(false);
        menuItem.setIcon(R.drawable.ic_ab_other);
        menuItem.setContentDescription(LocaleController.getString("AccDescrMoreOptions", R.string.AccDescrMoreOptions));
        menuItem.addSubItem(1, "Copy to clipboard");
        menuItem.addSubItem(2,"Share Qr Photo");
        menuItem.setSubMenuOpenSide(2);
        menuItem.setDelegate(id -> {
            builder.getDismissRunnable().run();
            if (id == 1) {
                AndroidUtilities.addToClipboard(url);
                Toast.makeText(getParentActivity(), LocaleController.getString("LinkCopied", R.string.LinkCopied), Toast.LENGTH_SHORT).show();
            } else if (id == 2) {
                ShopUtils.shareBitmap(getParentActivity(),imageView,url);
            }
        });
        menuItem.setTranslationX(AndroidUtilities.dp(6));
        menuItem.setBackgroundDrawable(Theme.createSelectorDrawable(Theme.getColor(Theme.key_dialogButtonSelector), 6));
        frameLayout.addView(menuItem, LayoutHelper.createFrame(48, 48, Gravity.TOP | Gravity.RIGHT, 0, 12, 10, 0));
        menuItem.setOnClickListener(v -> menuItem.toggleSubMenu());

        addressValueTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
        addressValueTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmono.ttf"));
        addressValueTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        StringBuilder stringBuilder = new StringBuilder("");
        stringBuilder.insert(stringBuilder.length() / 2, '\n');
        addressValueTextView.setText(stringBuilder);
        //linearLayout.addView(addressValueTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 16, 0, 0));
        addressValueTextView.setOnLongClickListener(v -> {
            AndroidUtilities.addToClipboard(url);
            Toast.makeText(getParentActivity(), LocaleController.getString("WalletTransactionAddressCopied", R.string.WalletTransactionAddressCopied), Toast.LENGTH_SHORT).show();
            return true;
        });

        TextView buttonTextView = new TextView(context);
        buttonTextView.setPadding(AndroidUtilities.dp(34), 0, AndroidUtilities.dp(34), 0);
        buttonTextView.setGravity(Gravity.CENTER);
        buttonTextView.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
        buttonTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        buttonTextView.setText("Share");
        buttonTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        buttonTextView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(4), Theme.getColor(Theme.key_featuredStickers_addButton), Theme.getColor(Theme.key_featuredStickers_addButtonPressed)));
        linearLayout.addView(buttonTextView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 42, Gravity.LEFT | Gravity.TOP, 16, 20, 16, 16));
        buttonTextView.setOnClickListener(v -> AndroidUtilities.openSharing(this, url));

        ScrollView scrollView = new ScrollView(context);
        scrollView.setVerticalScrollBarEnabled(false);
        scrollView.addView(frameLayout, LayoutHelper.createScroll(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP));
        if (Build.VERSION.SDK_INT >= 21) {
            scrollView.setNestedScrollingEnabled(true);
        }

        builder.setCustomView(scrollView);
        BottomSheet bottomSheet = builder.create();
        bottomSheet.setCanDismissWithSwipe(false);
        showDialog(bottomSheet);
    }


    private void showProductDetail(String detail){
        if (getParentActivity() == null) {
            return;
        }
        Context context = getParentActivity();
        BottomSheet.Builder builder = new BottomSheet.Builder(context);
        builder.setApplyBottomPadding(false);
        builder.setApplyTopPadding(false);
        builder.setUseFullWidth(false);


        FrameLayout frameLayout = new FrameLayout(context) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(472), MeasureSpec.EXACTLY));
            }
        };

      TextView titleView = new TextView(context);
        titleView.setLines(1);
        titleView.setSingleLine(true);
          titleView.setText("Product Detail");

        titleView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        titleView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        titleView.setEllipsize(TextUtils.TruncateAt.END);
        titleView.setGravity(Gravity.CENTER_VERTICAL);
        frameLayout.addView(titleView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 21, 22, 21, 0));


        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        frameLayout.addView(linearLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.LEFT, 0, 78, 0, 0));




        AboutLinkCell aboutLinkCell = new AboutLinkCell(context,this){
            @Override
            protected void didPressUrl(String url) {
                if (url.startsWith("@")) {
                    getMessagesController().openByUserName(url.substring(1), ProductDetailFragment.this, 0);
                } else if (url.startsWith("#")) {
                    DialogsActivity fragment = new DialogsActivity(null);
                    fragment.setSearchString(url);
                    presentFragment(fragment);
                } else{
                    Browser.openUrl(context,url);
                }

            }
        };
        aboutLinkCell.setText(detail,true);
        linearLayout.addView(aboutLinkCell,LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 21, 22, 21, 0));



        ScrollView scrollView = new ScrollView(context);
        scrollView.setVerticalScrollBarEnabled(false);
        scrollView.addView(frameLayout, LayoutHelper.createScroll(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP));
        if (Build.VERSION.SDK_INT >= 21) {
            scrollView.setNestedScrollingEnabled(true);
        }

        builder.setCustomView(scrollView);
        BottomSheet bottomSheet = builder.create();
        bottomSheet.setCanDismissWithSwipe(false);
        showDialog(bottomSheet);

    }

    @Override
    public View createView(Context context) {
        Theme.createProfileResources(context);

        actionBar.setBackButtonDrawable(createDetailMenuDrawable(R.drawable.ic_ab_back));
        actionBar.setBackgroundDrawable(null);
        actionBar.setTitleColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        actionBar.setCastShadows(false);
        actionBar.setAddToContainer(false);
        actionBar.setOccupyStatusBar(Build.VERSION.SDK_INT >= 21 && !AndroidUtilities.isTablet());
        actionBar.setTitle("");
        actionBar.getTitleTextView().setAlpha(0.0f);
        if (!AndroidUtilities.isTablet()) {
            actionBar.showActionModeTop();
        }
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }else if(id == like_item){
                    boolean isFav = (boolean)productFull.get("is_favorite");
                    ShopDataController.getInstance(currentAccount).checkFav(!isFav, chat_id, product_id, new ShopDataController.BooleanCallBack() {
                        @Override
                        public void onResponse(boolean susscess) {
                            if(susscess){
                                productFull.put("is_favorite",!isFav);
                                if(isFav){
                                    like_menu.setIcon(createDetailMenuDrawable(R.drawable.ic_love_filled));
                                }else{
                                    like_menu.setIcon(createDetailMenuDrawable(R.drawable.ic_like_line));
                                }
                            }
                        }
                    });
                }else if(id == share_item){
                    String shaere_link = ShopUtils.getProductLink(chat_id,product_id);
                    showDialog(new ShareAlert(context,null,shaere_link,false,shaere_link,false));
                }else if(id == qr_item){
                    String share_link = ShopUtils.getProductLink(chat_id,product_id);
                    showProductSheet(share_link);
                }
            }
        });

        ActionBarMenu menu = actionBar.createMenu();

        //like_menu =  menu.addItem(like_item,createDetailMenuDrawable(R.drawable.ic_love_filled));
       // like_menu.setVisibility(View.GONE);
        menu.addItem(share_item,createDetailMenuDrawable(R.drawable.msg_share));
        ActionBarMenuItem otherItem = menu.addItem(more_item,createDetailMenuDrawable(R.drawable.ic_ab_other));
        otherItem.addSubItem(qr_item,R.drawable.wallet_qr,"Qr Code");




        fragmentView = new FrameLayout(context) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                LayoutParams layoutParams = (LayoutParams) actionBarBackground.getLayoutParams();
                layoutParams.height = ActionBar.getCurrentActionBarHeight() + (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + AndroidUtilities.dp(3);

                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }

            @Override
            protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                super.onLayout(changed, left, top, right, bottom);
                checkScroll(false);
            }
        };
        FrameLayout frameLayout = (FrameLayout) fragmentView;
        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

        scrollView = new ScrollView(context) {

            @Override
            protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
                return false;
            }

            @Override
            public boolean requestChildRectangleOnScreen(View child, Rect rectangle, boolean immediate) {
                rectangle.offset(child.getLeft() - child.getScrollX(), child.getTop() - child.getScrollY());
                rectangle.top += AndroidUtilities.dp(20);
                rectangle.bottom += AndroidUtilities.dp(50);
                return super.requestChildRectangleOnScreen(child, rectangle, immediate);
            }
        };
        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> checkScroll(false));


        scrollView.setFillViewport(true);
        scrollView.setVerticalScrollBarEnabled(false);
        AndroidUtilities.setScrollViewEdgeEffectColor(scrollView, Theme.getColor(Theme.key_actionBarDefault));
        frameLayout.addView(scrollView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.TOP, 0, 0, 0,  55));

        linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(linearLayout, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        emptyLayout = new LinearLayout(context);
        emptyLayout.setOrientation(LinearLayout.VERTICAL);
        emptyLayout.setGravity(Gravity.CENTER);
        emptyLayout.setVisibility(View.GONE);
        emptyLayout.setOnTouchListener((v, event) -> true);

        ImageView emptyImageView = new ImageView(context);
        emptyImageView.setImageResource(R.drawable.files_empty);
        emptyLayout.addView(emptyImageView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        TextView emptyTextView = new TextView(context);
        emptyTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
        emptyTextView.setGravity(Gravity.CENTER);
        emptyTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
        emptyTextView.setPadding(AndroidUtilities.dp(40), 0, AndroidUtilities.dp(40), AndroidUtilities.dp(128));
        emptyLayout.addView(emptyTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 0, 24, 0, 0));
        emptyTextView.setText(LocaleController.getString("NoResult", R.string.NoResult));
        frameLayout.addView(emptyLayout, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        
         bottomContactLayout  = new BottomLayout(context,true){

            @Override
            protected void onCallPressed() {

                if (getParentActivity() == null) {
                    return;
                }
                ArrayList<ShopDataSerializer.TelephoneContact> calls = currentShop.phoneNumbers;
                if(calls == null){
                    return;
                }
                final LinearLayout linearLayout = new LinearLayout(getParentActivity());
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                BottomSheet.Builder builder = new BottomSheet.Builder(getParentActivity());
                for (int a = 0; a < calls.size(); a++) {
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
                    textView.setText(calls.get(a).phonenumber);
                    linearLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, Gravity.LEFT | Gravity.TOP));
                    textView.setOnClickListener(v -> {
                        int i = (Integer) v.getTag();
                        if(currentShop.phoneNumbers != null && !currentShop.phoneNumbers.isEmpty()){
                            String phone =   currentShop.phoneNumbers.get(i).phonenumber;
                            try {
                                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:+" + phone));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                getParentActivity().startActivityForResult(intent, 500);
                            } catch (Exception e) {
                                FileLog.e(e);
                            }
                        }
                        builder.getDismissRunnable().run();
                    });}

                builder.setTitle("Call");
                builder.setCustomView(linearLayout);
                showDialog(builder.create());
            }

            @Override
            protected void onMsgPressed() {
                if(currentShop.contact_username == null){
                    return;
                }
                getMessagesController().openByUserName(currentShop.contact_username,ProductDetailFragment.this,4);
            }

            @Override
            protected void onOfferPressed() {


                OfferBottomSheet offerBottomSheet = new OfferBottomSheet(ProductDetailFragment.this,"");
                offerBottomSheet.setOfferBottomSheetDelegate((price, comment) -> {

                    Log.i("offer","username delegate sent price " + price + " comment "+ comment);

                    TLObject object =  getMessagesController().getUserOrChat(currentShop.contact_username);
                    if(object instanceof TLRPC.User){
                        TLRPC.User user = (TLRPC.User)object;
                        TLRPC.TL_messages_sendMessage reqSend = new TLRPC.TL_messages_sendMessage();
                        reqSend.message = "OFFER MADE \uD83D\uDC47\uD83D\uDC47 \n\n"
                                + "Title => "+  productFull.get("title") + "\n"
                                + "Item Price => "+  productFull.get("price") + "\n"
                                + "Offer Price =>  " + price + "\n"
                                + "Link  =>  "+ ShopUtils.getProductLink(chat_id,product_id) + "\n";


                        reqSend.silent = false;
                        reqSend.peer = getMessagesController().getInputPeer(user.id);
                        reqSend.random_id = Utilities.random.nextLong();
                        reqSend.no_webpage = true;
                        ConnectionsManager.getInstance(currentAccount).sendRequest(reqSend, new RequestDelegate() {
                            @Override
                            public void run(TLObject response, TLRPC.TL_error error) {
                                if(error == null){
                                    Log.i("offer","null error ");

                                    AndroidUtilities.runOnUIThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Log.i("offer","send message from run");


                                            if(response instanceof TLRPC.TL_updateShortSentMessage){
                                                Log.i("offer","response is unstnace of TL_updateShortSentMessage ");

                                                final TLRPC.TL_updateShortSentMessage res = (TLRPC.TL_updateShortSentMessage) response;
                                                String messageLink = "tg://openmessage?user_id={user_id}&message_id=" + res.id;
                                                messageLink = messageLink.replace("{user_id}",user.id + "");
                                                createOffer(messageLink,comment,price);
                                            }
                                        }
                                    });

                                }
                            }
                        });
                    }else{
                        Log.i("offer","username not found");

                        getMessagesController().openByUserName(currentShop.contact_username,ProductDetailFragment.this,1);
                    }


                });
                showDialog(offerBottomSheet);
            }

            @Override
            protected void onShopPressed() {

                Bundle bundle = new Bundle();
                bundle.putInt("chat_id", chat_id);
                BusinessProfileActivity businessProfileActivity = new BusinessProfileActivity(bundle);
                presentFragment(businessProfileActivity);            }
        };
        frameLayout.addView(bottomContactLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT,Gravity.BOTTOM,0,1,0,0));

        View shadow = new View(context){
            @Override
            protected void onDraw(Canvas canvas) {
                int h = getMeasuredHeight() - AndroidUtilities.dp(1);
                parentLayout.drawHeaderShadow(canvas, h);
            }
        };
        bottomContactLayout.addView(shadow, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 1,Gravity.TOP,0,0,0,55));


        radialProgressView = new RadialProgressView(context);
        radialProgressView.setSize(AndroidUtilities.dp(48));
        radialProgressView.setVisibility(View.VISIBLE);
        radialProgressView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        frameLayout.addView(radialProgressView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        actionBarBackground = new View(context) {

            private Paint paint = new Paint();

            @Override
            protected void onDraw(Canvas canvas) {
                paint.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                int h = getMeasuredHeight() - AndroidUtilities.dp(3);
                canvas.drawRect(0, 0, getMeasuredWidth(), h, paint);
                parentLayout.drawHeaderShadow(canvas, h);
            }
        };
        actionBarBackground.setAlpha(0.0f);
        frameLayout.addView(actionBarBackground, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        frameLayout.addView(actionBar, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        return fragmentView;
    }

    private void checkScroll(boolean animated) {
        boolean show = true;

        if(productPagerCell != null){
            productPagerCell.getLocationOnScreen(location);
            show = location[1] + productPagerCell.getMeasuredHeight()   < 0.7 * productPagerCell.getMeasuredHeight() ;
        }

        boolean visible = actionBarBackground.getTag() == null;
        if (show != visible) {
            actionBarBackground.setTag(show ? null : 1);
            if (actionBarAnimator != null) {
                actionBarAnimator.cancel();
                actionBarAnimator = null;
            }
            if (animated) {
                actionBarAnimator = new AnimatorSet();
                actionBarAnimator.playTogether(
                        ObjectAnimator.ofFloat(actionBarBackground, View.ALPHA, show ? 1.0f : 0.0f),
                        ObjectAnimator.ofFloat(actionBar.getTitleTextView(), View.ALPHA, show ? 1.0f : 0.0f)
                );
                actionBarAnimator.setDuration(150);
                actionBarAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (animation.equals(actionBarAnimator)) {
                            actionBarAnimator = null;
                        }
                    }
                });
                actionBarAnimator.start();
            } else {
                actionBarBackground.setAlpha(show ? 1.0f : 0.0f);
                actionBar.getTitleTextView().setAlpha(show ? 1.0f : 0.0f);
            }
        }
    }

    private void createUiPictureView(Context context, ShopDataSerializer.FieldSet fieldSet){
        Type type = new TypeToken<ArrayList<ShopDataSerializer.ProductImage>>(){}.getType();
        String key =fieldSet.fields.get(0).key;
        Object ob = productFull.get(key);
        if(ob != null){
            ArrayList<ShopDataSerializer.ProductImage> productImages = gson.fromJson(ob.toString(),type);
            productPagerCell = new ProductPagerCell(context,productImages);
            linearLayout.addView(productPagerCell,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT));
        }
    }

    private void createUiHeaderView(Context context, ShopDataSerializer.FieldSet fieldSet){
        if(fieldSet.fields == null || fieldSet.fields.isEmpty()){
            return;
        }
        String sub_type = fieldSet.fields.get(0).ui_type;
        switch (sub_type) {
            case ui_input_price: {
                String key = fieldSet.fields.get(0).key;
                if (!productFull.containsKey(key)) {
                    return;
                }
                Object object = productFull.get(key);
                if (object == null) {
                    return;
                }

                HeaderCell textCell = new HeaderCell(context,16);
                textCell.setText("Price");
                linearLayout.addView(textCell,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT));

                TextSettingsCell textSettingsCell = new TextSettingsCell(context,16);
                textSettingsCell.setText( "ETB "+ object.toString() ,false);
                linearLayout.addView(textSettingsCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 0, 0, 0, 0));

                actionBar.setTitle("ETB "+ object.toString());

                break;
            }
            case ui_input_title: {
                String key = fieldSet.fields.get(0).key;
                if (!productFull.containsKey(key)) {
                    return;
                }
                Object object = productFull.get(key);
                if (object == null) {
                    return;
                }

                HeaderCell textCell = new HeaderCell(context,16);
                textCell.setText("Title");
                linearLayout.addView(textCell,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT));


                TextSettingsCell textSettingsCell = new TextSettingsCell(context,16);
                textSettingsCell.setText(  object.toString() ,false);
                linearLayout.addView(textSettingsCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 0, 0, 0, 0));
                break;
            }
            case ui_input_str: {
                String key;
                if ((key = fieldSet.fields.get(0).key) == null || productFull.get(key) == null || ShopUtils.isEmpty(productFull.get(key).toString())) {
                    return;
                }
                HeaderCell textCell = new HeaderCell(context,16);
                textCell.setText("Description");
                linearLayout.addView(textCell,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT));

                AboutLinkCell aboutLinkCell = new AboutLinkCell(context,ProductDetailFragment.this){
                    @Override
                    protected void didPressUrl(String url) {
                        if (url.startsWith("@")) {
                            getMessagesController().openByUserName(url.substring(1), ProductDetailFragment.this, 0);
                        } else if (url.startsWith("#")) {
                            DialogsActivity fragment = new DialogsActivity(null);
                            fragment.setSearchString(url);
                            presentFragment(fragment);
                        } else{
                            Browser.openUrl(context,url);
                        }
                    }
                };
                String text = productFull.get(key).toString();
                while (text.contains("\n\n\n")) {
                    text = text.replace("\n\n\n", "\n\n");
                }
                aboutLinkCell.setText(text,true);
                aboutLinkCell.setBackground(Theme.getSelectorDrawable(true));
                String finalText = text;
                aboutLinkCell.setOnClickListener(v -> showProductDetail(finalText));
                linearLayout.addView(aboutLinkCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 0, 0, 0, 0));
                break;
            }
        }
    }

    private void createTableListView(ShopDataSerializer.FieldSet fieldSet){
        Context context = getParentActivity();
        if(context == null || fieldSet == null || fieldSet.fields == null || fieldSet.fields.isEmpty()){
            return;
        }

        ShadowSectionCell shadowSectionCell = new ShadowSectionCell(context);
        shadowSectionCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        linearLayout.addView(shadowSectionCell,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,8));

        HeaderCell textCell = new HeaderCell(context,16);
        textCell.setText(fieldSet.header);
        linearLayout.addView(textCell,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT));

        ArrayList<TableItem> tableItems = new ArrayList<>();
        for (int a = 0; a < fieldSet.fields.size(); a++) {
            ShopDataSerializer.Field field = fieldSet.fields.get(a);
            if (field == null || TextUtils.isEmpty(field.label)) {
                continue;
            }
            if (TextUtils.isEmpty(field.key)) {
                continue;
            }
            if (!productFull.containsKey(field.key)) {
                continue;
            }
            Object value = productFull.get(field.key);
            if (value == null) {
                continue;
            }
            TableItem tableItem = new TableItem();
            if(field.meta != null){
                tableItem.image =field.meta.icon;
            }


            if(value.toString().length() == 0){
                continue;
            }

            tableItem.value = value.toString().substring(0,1).toUpperCase() + value.toString().substring(1);
            tableItem.key = field.key.substring(0,1).toUpperCase() + field.key.substring(1);
            tableItems.add(tableItem);

        }

        productDetailLayout detailLayout = new productDetailLayout(context,tableItems.size());
        detailLayout.setData(tableItems);
        linearLayout.addView(detailLayout,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.MATCH_PARENT,Gravity.LEFT,0,8,8,0));

        shadowSectionCell = new ShadowSectionCell(context);
        shadowSectionCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        linearLayout.addView(shadowSectionCell,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,8));

    }

    private void createUiLocationView(Context context,ShopDataSerializer.FieldSet fieldSet){

        ShadowSectionCell shadowSectionCell = new ShadowSectionCell(context);
        shadowSectionCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        linearLayout.addView(shadowSectionCell,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,8));

        HeaderCell textCell = new HeaderCell(context,16);
        textCell.setText("Location");
        linearLayout.addView(textCell,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT));

        TextSettingsCell textSettingsCell = new TextSettingsCell(context,16);
        textSettingsCell.setBackground(Theme.getSelectorDrawable(true));
        textSettingsCell.setTextAndIcon("Bole Ednamall 2nd floor",R.drawable.message_arrow,false);
        linearLayout.addView(textSettingsCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 0, 0, 0, 0));

        textSettingsCell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    private void createOffer(String messageLink,String comment,double price){
        alertDialog = new AlertDialog(getParentActivity(),3);
        alertDialog.setCanCacnel(true);
        SR_object.create_offer_req create_offer_req = new SR_object.create_offer_req();
        create_offer_req.note = comment;
        create_offer_req.price = price;
        create_offer_req.message_link = messageLink;
        int reqId =  ShopDataController.getInstance(currentAccount).offerProductPrice(chat_id,product_id,create_offer_req);
        alertDialog.setOnCancelListener(dialog -> ShopDataController.getInstance(currentAccount).cancelRequest(reqId));
        showDialog(alertDialog);
    }


    public void createShopInfoView(Context context){
        if(shopInfoAdded){
            return;
        }
        shopInfoAdded = true;
        Object object =   productFull.get("shop");
        if(object == null){
            return;
        }

        try {
            ShopDataSerializer.Shop shop = gson.fromJson(object.toString(), ShopDataSerializer.Shop.class);
            currentShop = shop;
            if(currentShop != null){
                ShadowSectionCell shadowSectionCell = new ShadowSectionCell(context);
                shadowSectionCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
                linearLayout.addView(shadowSectionCell,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,8));

                HeaderCell textCell = new HeaderCell(context,16);
                textCell.setText("Store");
                linearLayout.addView(textCell,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT));


                ContactChangeCell  shopInfoInnerFrame = new ContactChangeCell(context);
                shopInfoInnerFrame.setUser(shop);
                shopInfoInnerFrame.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putInt("chat_id", ShopUtils.toClientChannelId(shop.channel_id));
                        BusinessProfileActivity businessProfileActivity = new BusinessProfileActivity(bundle);
                        presentFragment(businessProfileActivity);
                    }
                });
                linearLayout.addView(shopInfoInnerFrame,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,Gravity.LEFT));


                shadowSectionCell = new ShadowSectionCell(context);
                shadowSectionCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
                linearLayout.addView(shadowSectionCell,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,8));


            }else{
                // bottomContactLayout.setVisibility(View.GONE);
            }


        }catch (Exception e){
            Log.i(TAG,e.toString());
        }
    }

    private void createFiledSetView(){
        Context context =  getParentActivity();
        if(context == null || productLoaded){
            return;
        }
        productLoaded = true;
        for(ShopDataSerializer.FieldSet fieldSet: fieldSetArrayList){
            if(fieldSet == null){
                continue;
            }
            String field_set_type = fieldSet.type;
            if(TextUtils.isEmpty(field_set_type)){
                continue;
            }
            switch (field_set_type){
                case ui_picture :
                    createUiPictureView(context,fieldSet);
                    break;
                case  ui_header_body :
                    createUiHeaderView(context,fieldSet);
                    break;
                case ui_table:
                   createTableListView(fieldSet);
                    break;
                case ui_location:
                    createUiLocationView(context,fieldSet);
                    break;
            }
            showData();
        }}

    private void showData(){
        linearLayout.setAlpha(0f);
        linearLayout.setVisibility(View.VISIBLE);
        linearLayout.animate().alpha(1f).setDuration(300).setListener(null);
        radialProgressView.animate().alpha(0f).setStartDelay(300).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                radialProgressView.setVisibility(View.GONE);
            }
        });
    }

    public void setCurrentShop(ShopDataSerializer.Shop currentShop) {
        this.currentShop = currentShop;
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args){

        if (id == NotificationCenter.productFullLoaded) {
            boolean loaded = (boolean)args[0];
            int guid  = (int)args[2];
            if(guid == classGuid){
                if(loaded){
                    productFull = (HashMap<String,Object>)args[1];
                    if(productFull.containsKey("product_type")){
                        currentBusiness = (String)productFull.get("product_type");
                        section = currentBusiness + "-detail";
                        ShopDataController.getInstance(currentAccount).loadSectionConfiguration(section,true,classGuid);
//                        if(productFull.get("is_favorite") instanceof Boolean){
//                            boolean isFav = (boolean)productFull.get("is_favorite");
//                            like_menu.setVisibility(View.VISIBLE);
//                            if(isFav){
//                                like_menu.setIcon(createDetailMenuDrawable(R.drawable.ic_love_filled));
//                            }else{
//                                like_menu.setIcon(createDetailMenuDrawable(R.drawable.ic_like_line));
//                            }
//                        }
                    }

                }
            }
        }else if(id  == NotificationCenter.didConfigrationLoaded){
            boolean loaded = (boolean)args[0];
            String sec = (String)args[1];
            int guid = (int)args[3];
            if(classGuid == guid && sec.equals(section)){
                fieldSetArrayList = (ArrayList<ShopDataSerializer.FieldSet>)args[2];
                if(loaded && !confLoaded){
                    ShopDataController.getInstance(currentAccount).loadProductFull(chat_id,product_id,classGuid);
                }
                confLoaded = true;
                createFiledSetView();
                createShopInfoView(getParentActivity());
            }
        }else if (id == NotificationCenter.didOfferCreated){
            boolean loaded = (boolean)args[0];
            if(alertDialog != null){
                alertDialog.cancel();
                alertDialog = null;
            }

            if(loaded){
                APIError apiError = new APIError();
                apiError.setMessage("Offer made");
                showErrorAlert(apiError);
            }else{
                APIError apiError = new APIError();
                apiError.setMessage("Filed to create offer");
                showErrorAlert(apiError);
            }

        }
    }

    private void showErrorAlert(APIError error) {
        if (getParentActivity() == null) {
            return;
        }
        if(error != null && TextUtils.isEmpty(error.message())){
            error.setMessage("Unknown error, try again!");
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(LocaleController.getString("AppNameHulu", R.string.AppNameHulu));
        builder.setMessage(error.message());
        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
        showDialog(builder.create());
    }

    public static class productDetailLayout extends LinearLayout{

      TextView[] primary;
      TextView[] secondary;
      TextView[] title;

      public productDetailLayout(Context context,int count) {
          super(context);
          setOrientation(VERTICAL);
          primary = new TextView[count];
          secondary = new TextView[count];
          title = new TextView[count];


          setPadding(AndroidUtilities.dp(16), 0, AndroidUtilities.dp(16), AndroidUtilities.dp(16));

          //number of coll
          int collCount = (int) Math.ceil(count/2);
          for (int i = 0; i < collCount; i++) {
              LinearLayout linearLayout = new LinearLayout(context);
              linearLayout.setOrientation(HORIZONTAL);

              //row loop
              for (int j = 0; j < 2; j++) {
                  LinearLayout contentCell = new LinearLayout(context);
                  contentCell.setOrientation(VERTICAL);

                  LinearLayout infoLayout = new LinearLayout(context);
                  infoLayout.setOrientation(HORIZONTAL);
                  primary[i * 2 + j] = new TextView(context);
                  secondary[i * 2 + j] = new TextView(context);
                  title[i * 2 + j] = new TextView(context);

                  primary[i * 2 + j].setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
                  primary[i * 2 + j].setTextSize(17);
                  title[i * 2 + j].setTextSize(13);
                  secondary[i * 2 + j].setTextSize(13);

                  secondary[i * 2 + j].setPadding(AndroidUtilities.dp(4), 0, 0, 0);
                  //title[i * 2 + j].setPadding(AndroidUtilities.dp(4), AndroidUtilities.dp(4), AndroidUtilities.dp(4), AndroidUtilities.dp(4));


                  infoLayout.addView(primary[i * 2 + j]);
                  infoLayout.addView(secondary[i * 2 + j]);

                  contentCell.addView(infoLayout);
                  contentCell.addView(title[i * 2 + j]);
                  linearLayout.addView(contentCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 1f));
              }
              addView(linearLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 0, 16));
          }
      }

      public void setData(ArrayList<TableItem> tableItems){

          for(int a = 0; a < tableItems.size(); a++){
              if(title[a] == null || primary[a] == null){
                  continue;
              }
              TableItem item = tableItems.get(a);
              title[a].setText(item.key);
              primary[a].setText(item.value);
              //title[a].setText("Product Detail");
          }
          updateColors(tableItems.size());
      }

      private void updateColors(int count) {
          for (int i = 0; i < count; i++) {
              if(title[i] == null || primary[i] == null){
                  continue;
              }
              primary[i].setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
              title[i].setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));

              String colorKey = (String) secondary[i].getTag();
              if (colorKey != null) {
                  secondary[i].setTextColor(Theme.getColor(colorKey));
              }
          }
      }

  }

    public static  class BottomLayout extends FrameLayout{

        private FrameLayout callButton;
        private FrameLayout msgButton;
        private FrameLayout offerButton;
        private FrameLayout shopButton;

       private boolean showShop;

       public BottomLayout(Context context,boolean show_shop) {
           super(context);


           setWillNotDraw(false);
            showShop = show_shop;

           Drawable callDrawable = context.getResources().getDrawable(R.drawable.menu_calls).mutate();
           callDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextBlack), PorterDuff.Mode.MULTIPLY));

           Drawable msgDrawable = context.getResources().getDrawable(R.drawable.menu_chats).mutate();
           msgDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextBlack), PorterDuff.Mode.MULTIPLY));

           Drawable shopDrawable = context.getResources().getDrawable(R.drawable.ic_store).mutate();
           shopDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextBlack), PorterDuff.Mode.MULTIPLY));

            int count = 4;
            for(int a = 0; a < count; a++){

                FrameLayout frameLayout = new FrameLayout(context);
                if(a == 3){
                    frameLayout.setBackgroundDrawable(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(0), Theme.getColor(Theme.key_wallet_buttonBackground), Theme.getColor(Theme.key_wallet_buttonPressedBackground)));
                }else{
                    frameLayout.setBackground(Theme.getSelectorDrawable(true));
                }
                addView(frameLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.TOP, 0, 0, 0, 0));
                frameLayout.setOnClickListener(v -> {
                    if(v  == offerButton){
                        onOfferPressed();
                    }if(v  == callButton){
                        onCallPressed();
                    }if(v  == msgButton){
                        onMsgPressed();
                    }else if(v == shopButton){
                        onShopPressed();
                    }
                });
                ImageView imageView = new ImageView(context);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                if (a == 0) {
                    imageView.setImageDrawable(shopDrawable);
                    shopButton = frameLayout;
                }else if(a == 1){
                    imageView.setImageDrawable(msgDrawable);
                    msgButton = frameLayout;
                }else if(a == 2){
                    imageView.setImageDrawable(callDrawable);
                    callButton = frameLayout;
                }
                frameLayout.addView(imageView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));

                if(a == 3){
                    SimpleTextView offerTextView = new SimpleTextView(context);
                    offerTextView.setTextColor(Theme.getColor(Theme.key_avatar_text));
                    offerTextView.setTextSize(16);
                    offerTextView.setText("OFFER");
                    offerTextView.setDrawablePadding(AndroidUtilities.dp(0));
                    offerTextView.setGravity(Gravity.CENTER);
                    offerTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
                    frameLayout.addView(offerTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));
                    offerButton = frameLayout;
                }

                if(!show_shop){
                    shopButton.setVisibility(GONE);
                }
            }

        }


       @Override
       protected void onDraw(Canvas canvas) {
           super.onDraw(canvas);

        //   canvas.drawLine(child.getMeasuredWidth(),0,child.getMeasuredWidth() + 1,child.getMeasuredHeight(),Theme.dividerPaint);

       }

       @Override
       protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
           int width = MeasureSpec.getSize(widthMeasureSpec);
           int buttonWidth;
           int offerWidth;
           if(showShop){
               buttonWidth = (int) (width*0.2);
               offerWidth = (int) (width*0.4);
           } else {
               buttonWidth = (int) (width*0.3);
               offerWidth = (int) (width*0.4);
           }

           LayoutParams layoutParams = (LayoutParams) shopButton.getLayoutParams();
           if(showShop){
               layoutParams.width = (int) (width* 0.2);
               layoutParams.leftMargin = 0;
           }else{
               layoutParams.width = 0;
               layoutParams.leftMargin = 0;
           }

           layoutParams = (LayoutParams) msgButton.getLayoutParams();
           layoutParams.width =buttonWidth;
           layoutParams.leftMargin = showShop?buttonWidth:0;

           layoutParams = (LayoutParams) callButton.getLayoutParams();
           layoutParams.width = buttonWidth;
           layoutParams.leftMargin = showShop?2 * buttonWidth:buttonWidth;


           layoutParams = (LayoutParams) offerButton.getLayoutParams();
           layoutParams.width = offerWidth;
           layoutParams.leftMargin = showShop?3 * buttonWidth:2* buttonWidth;


           super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(56), MeasureSpec.EXACTLY));
       }

       protected void onOfferPressed(){

        }

       protected void onShopPressed(){

       }

       protected void onCallPressed(){

       }

       protected void onMsgPressed(){

       }


   }

    public static class ContactChangeCell extends FrameLayout {

        protected int currentAccount = UserConfig.selectedAccount;

        private SimpleTextView nameTextView;
        private SimpleTextView statusTextView;
        private SimpleTextView profilePhotoChanged;
        private BackupImageView avatarImageView;
        private ImageView newImageView;

        public ImageView getNewImageView() {
            return newImageView;
        }

        public void setName(String name){
            nameTextView.setText(name);
        }

        public void setImage(Bitmap bitmap){
            avatarImageView.setImageBitmap(bitmap);
        }


        public void setUser(ShopDataSerializer.Shop detailShop){
            if (detailShop == null) {
                return;
            }
            Log.i("setShopcalled",detailShop.title);

            if(detailShop.profilePic != null){
                Log.i("setShopcalled",detailShop.profilePic.photo);

            }else{
                Log.i("setShopcalled","detial shop wass caleld");

            }


            AvatarDrawable avatarDrawable = new AvatarDrawable();
            avatarDrawable.setInfo(5,detailShop.title,"");
            avatarDrawable.setSmallSize(true);
            avatarDrawable.setColor(Theme.getColor(Theme.key_avatar_backgroundInProfileBlue));
            if(detailShop.profilePic != null){
                avatarImageView.setImage(detailShop.profilePic.photo, "50_50", avatarDrawable);
            }else{
                avatarImageView.setImage(null, null, avatarDrawable);
            }

            nameTextView.setText(detailShop.title);
            statusTextView.setText(detailShop.address);
            profilePhotoChanged.setText("");

        }


        public void setData(FeatureDataModel.ProfileChange profileChange){
            TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(profileChange.user_id);

            setUser(user);
        }

        public void setUser(TLRPC.User user){
            if (user == null || user.deleted) {
                return;
            }
            nameTextView.setText(UserObject.getFirstName(user,false));
            statusTextView.setText(UserObject.getFirstName(user,false) + "\n" + "userid: " + user.id);
            profilePhotoChanged.setText("+" + user.phone);
            statusTextView.setText("");

            AvatarDrawable avatarDrawable = new AvatarDrawable(user);
            avatarDrawable.setColor(Theme.getColor(Theme.key_avatar_backgroundInProfileBlue));
            avatarImageView.setImage(ImageLocation.getForUser(user,false), "50_50", avatarDrawable,user);
        }

        public void setChat(TLRPC.Chat chat){
            if (chat == null) {
                return;
            }
            TLRPC.FileLocation photo = null;
            if (chat.photo != null) {
                photo = chat.photo.photo_small;

            }
            AvatarDrawable avatarDrawable = new AvatarDrawable(chat);
            avatarDrawable.setColor(Theme.getColor(Theme.key_avatar_backgroundInProfileBlue));
            avatarImageView.setImage(ImageLocation.getForChat(chat,false), "50_50", avatarDrawable,chat);
        }

        public void setAbout(String about){
            statusTextView.setText(about);
        }

        public BackupImageView getAvatarImageView() {
            return avatarImageView;
        }


        public void setAvatarImageViewClickListener(final OnClickListener onClickListener) {
            avatarImageView.setOnClickListener(v -> {
                onClickListener.onClick(ContactChangeCell.this);
            });
        }

        public ContactChangeCell(@NonNull Context context) {
            super(context);
            setWillNotDraw(false);

            avatarImageView = new BackupImageView(context);
            avatarImageView.setRoundRadius(AndroidUtilities.dp(24));
            addView(avatarImageView, LayoutHelper.createFrame(50, 50, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, (LocaleController.isRTL ? 0 : 11), 11, (LocaleController.isRTL ? 11 : 0), 0));


            nameTextView = new SimpleTextView(context);
            nameTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            nameTextView.setTextSize(17);
            nameTextView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP);
            addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 20, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, (LocaleController.isRTL ? 0 : 72), 14, (LocaleController.isRTL ? 72 : 0), 0));

            profilePhotoChanged = new SimpleTextView(context);
            profilePhotoChanged.setTextColor( Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
            profilePhotoChanged.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            profilePhotoChanged.setTextSize(14);
            profilePhotoChanged.setText(LocaleController.getString("ProfilePhotoChanged", R.string.ProfilePhotoChanged));
            profilePhotoChanged.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP);
            addView(profilePhotoChanged, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 20, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, (LocaleController.isRTL ? 0 : 72), 40, (LocaleController.isRTL ? 72 : 0), 0));

            statusTextView = new SimpleTextView(context);
            statusTextView.setTextColor( Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
            statusTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            statusTextView.setTextSize(14);
            statusTextView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP);
            addView(statusTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, (LocaleController.isRTL ? 0 : 72), 65, (LocaleController.isRTL ? 72 : 0), 0));

        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(100), MeasureSpec.EXACTLY));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawLine(AndroidUtilities.dp(LocaleController.isRTL ? 0 : 60), getHeight() - 1, getMeasuredWidth() - AndroidUtilities.dp(LocaleController.isRTL ? 60 : 0), getHeight() - 1, Theme.dividerPaint);

        }

    }


}
