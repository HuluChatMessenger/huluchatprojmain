package org.plus.apps.business.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.URLUtil;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.plus.apps.business.ShopUtils;
import org.plus.experment.PlusBuildVars;
import org.plus.apps.business.data.SR_object;
import org.plus.apps.business.data.ShopDataController;
import org.plus.apps.business.data.ShopDataSerializer;
import org.plus.net.APIError;
import org.plus.experment.DataLoader;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.PollEditTextCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.EditTextEmoji;
import org.telegram.ui.Components.ImageUpdater;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RadialProgressView;
import org.telegram.ui.Components.SizeNotifierFrameLayout;
import org.telegram.ui.LocationActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class BusinessCreateActivity extends BaseFragment  implements NotificationCenter.NotificationCenterDelegate, ImageUpdater.ImageUpdaterDelegate {
    
    public boolean forEdit;
    
    private boolean uploaded;
    private long currentPictureId;


    //value for create
    private ArrayList<String> phoneNumbers = new ArrayList<>();
    private String username;
    private Double lat;
    private Double _long;
    private String _location;

    private Map<String,Object> updateMap = new HashMap<>();
    public Map<String,Object> createMap = new HashMap<>();
    private ShopDataSerializer.Shop currentShop;


    public TLRPC.FileLocation local_photo_location;

    private View doneButton;

    private AlertDialog progressDialog;

    private LinearLayout avatarContainer;
    private BackupImageView avatarImage;
    private ImageView avatarEditor;
    private View avatarOverlay;
    private AnimatorSet avatarAnimation;
    private RadialProgressView avatarProgressView;
    private AvatarDrawable avatarDrawable;
    private ImageUpdater imageUpdater;
    private EditTextEmoji nameTextView;

    private LinearLayout settingsContainer;
    private EditTextBoldCursor descriptionTextView;

    private TextCell setAvatarCell;
    private ShadowSectionCell settingsTopSectionCell;

    private LinearLayout addressContainer;
    private ShadowSectionCell addressSectionCell;
    private TextDetailSettingsCell locationCell;
    private HeaderCell headerCell;
    private PollEditTextCell addressCell;
    private TextDetailSettingsCell usernameCell;
    private TextDetailSettingsCell phoneCell;
    private PollEditTextCell websiteCell;
    private TextInfoPrivacyCell textInfoPrivacyCell;
    private PollEditTextCell cityTextCell;

    private boolean hasPhoto;

    private FrameLayout deleteContainer;
    private TextCheckCell closeCell;
    private ShadowSectionCell deleteInfoCell;

    private int chatId;

    private TLRPC.FileLocation avatar;


    private boolean donePressed;

    private TLRPC.User bot;
    private TLRPC.Chat currentChat;

    private boolean isBotAdmin;

    private final static int done_button = 1;

    public BusinessCreateActivity(Bundle args, ShopDataSerializer.Shop shop){
        super(args);
        currentShop = shop;
        forEdit = currentShop != null;
        chatId = args.getInt("chat_id", 0);
        
        if(currentShop != null){
            updateMap.put("contact_username",currentShop.contact_username);
            updateMap.put("latitude",currentShop.lat);
            updateMap.put("longtude",currentShop._long);
            updateMap.put("title",currentShop.title);
            updateMap.put("address",currentShop.address);
            updateMap.put("phonenumbers",currentShop.phoneNumbers);
        }
        avatarDrawable = new AvatarDrawable();
        imageUpdater = new ImageUpdater(false);
        if(!forEdit){
            createMap.put("telegram_channel",ShopUtils.toBotChannelId(chatId));
        }
    }
    
    private boolean checkDiscard() {
        if(!forEdit){
            if(createMap == null || createMap.isEmpty()){
                return true;
            }
        }
       AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
       builder.setTitle("Discard!");
       builder.setMessage("Discard creating shop!");
       builder.setPositiveButton(LocaleController.getString("Cancel", R.string.Cancel), new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialogInterface, int i) {
               dialogInterface.cancel();
           }
       });
       builder.setNegativeButton(LocaleController.getString("DiscardVoiceMessageAction", R.string.DiscardVoiceMessageAction), (dialog, which) -> finishFragment());
       showDialog(builder.create());
       return false;
    }


    @Override
    public boolean onFragmentCreate() {
        currentChat = getMessagesController().getChat(chatId);
        if (currentChat == null) {
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            getMessagesStorage().getStorageQueue().postRunnable(() -> {
                currentChat = getMessagesStorage().getChat(chatId);
                countDownLatch.countDown();
            });
            try {
                countDownLatch.await();
            } catch (Exception e) {
                FileLog.e(e);
            }
            if (currentChat != null) {
                getMessagesController().putChat(currentChat, true);
            } else {
                return false;
            }
        }
        TLObject object = getMessagesController().getUserOrChat(PlusBuildVars.getAuthBot());
        if(object instanceof TLRPC.User){
            bot = (TLRPC.User)object;
            addBotToAdminList(bot,true);
        }else{
            return false;
        }
        imageUpdater.parentFragment = this;
        imageUpdater.setDelegate(this);
        imageUpdater.setUploadAfterSelect(false);
         getNotificationCenter().addObserver(this,NotificationCenter.didShopCreated);
        if(forEdit){
            getNotificationCenter().addObserver(this,NotificationCenter.didShopUpdated);
        }
        return super.onFragmentCreate();
    }

    
    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (imageUpdater != null) {
            imageUpdater.clear();
        }
        getNotificationCenter().removeObserver(this, NotificationCenter.didShopCreated);
        if(forEdit){
            getNotificationCenter().removeObserver(this,NotificationCenter.didShopUpdated);
        }
        AndroidUtilities.removeAdjustResize(getParentActivity(), classGuid);
        if (nameTextView != null) {
            nameTextView.onDestroy();
        }
    }


    private void vibrate(View view){
        if (view != null) {
            Vibrator v = (Vibrator) getParentActivity().getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) {
                v.vibrate(200);
            }
            AndroidUtilities.shakeView(view, 2, 0);
      }
    }
    
    private boolean checkRequiredFiled(){
        if(forEdit){
            if(!TextUtils.isEmpty(currentShop.website)){
                boolean isValid = URLUtil.isValidUrl(currentShop.website);
                if(!isValid){
                    vibrate(websiteCell);
                    return false;
                }
            }

        }else{

            if(TextUtils.isEmpty(nameTextView.getText().toString().trim())){
                vibrate(nameTextView);
                return false;
            }

            if(TextUtils.isEmpty(addressCell.getText().trim())){
                vibrate(addressCell);
                return false;
            }


            if(TextUtils.isEmpty(cityTextCell.getText().trim())){
                vibrate(cityTextCell);
                return false;
            }

            if(phoneNumbers.isEmpty()){
                vibrate(phoneCell);
                return false;
            }


            if(TextUtils.isEmpty(username)){
                vibrate(usernameCell);
                return false;
            }



            if(!TextUtils.isEmpty(websiteCell.getText())){
                boolean isValid = URLUtil.isValidUrl(websiteCell.getText());
                if(!isValid){
                    websiteCell.setText2("Invalid website link!");
                    vibrate(websiteCell);
                    return true;
                }
            }
        }

        return true;
    }

    private void processDone(){
        if (donePressed || getParentActivity() == null) {
            return;
        }
        if(!checkRequiredFiled()){
            return;
        }

        if(forEdit){
            int reqId = ShopDataController.getInstance(currentAccount).updateShop(chatId,updateMap,true);
            progressDialog = new AlertDialog(getParentActivity(), 3);
            progressDialog.setCanCacnel(true);
            progressDialog.setOnCancelListener(dialog -> {
                ShopDataController.getInstance(currentAccount).cancelRequest(reqId);
                donePressed = false;
            });
            progressDialog.show();

        }else{
            if(!isBotAdmin){
                addBotToAdminList(bot,false);
                return;
            }
            fillCreateObject();
            donePressed = true;
            int reqId =  ShopDataController.getInstance(currentAccount).createShop(createMap,classGuid);
            progressDialog = new AlertDialog(getParentActivity(), 3);
            progressDialog.setCanCacnel(true);
            progressDialog.setOnCancelListener(dialog -> {
                ShopDataController.getInstance(currentAccount).cancelRequest(reqId);
                donePressed = false;
            });
            progressDialog.show();
        }

    }

    private void fillCreateObject(){
        if(lat != null && _long != null){
            createMap.put("latitude", lat);
            createMap.put("longtude", _long);
        }
        createMap.put("contact_username",username);
        JsonObject object = new JsonObject();
        JsonArray phones =  new JsonArray();
        for (int i = 0; i < phoneNumbers.size() ; i++) {
            phones.add(phoneNumbers.get(i));
        }
        object.add("phonenumbers",phones);
        createMap.put("telphone_contacts",object);

//        "telphone_contacts": {
//            "phonenumbers": []
//        }

    }



    @Override
    public View createView(Context context) {
        if (nameTextView != null) {
            nameTextView.onDestroy();
        }

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    if (checkDiscard()) {
                        finishFragment();
                    }
                } else if (id == done_button) {
                    processDone();
                }
            }
        });

        SizeNotifierFrameLayout sizeNotifierFrameLayout = new SizeNotifierFrameLayout(context) {

            private boolean ignoreLayout;

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int widthSize = MeasureSpec.getSize(widthMeasureSpec);
                int heightSize = MeasureSpec.getSize(heightMeasureSpec);

                setMeasuredDimension(widthSize, heightSize);
                heightSize -= getPaddingTop();

                measureChildWithMargins(actionBar, widthMeasureSpec, 0, heightMeasureSpec, 0);

                int keyboardSize = SharedConfig.smoothKeyboard ? 0 : measureKeyboardHeight();
                if (keyboardSize > AndroidUtilities.dp(20)) {
                    ignoreLayout = true;
                    nameTextView.hideEmojiView();
                    ignoreLayout = false;
                }

                int childCount = getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View child = getChildAt(i);
                    if (child == null || child.getVisibility() == GONE || child == actionBar) {
                        continue;
                    }
                    if (nameTextView != null && nameTextView.isPopupView(child)) {
                        if (AndroidUtilities.isInMultiwindow || AndroidUtilities.isTablet()) {
                            if (AndroidUtilities.isTablet()) {
                                child.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(Math.min(AndroidUtilities.dp(AndroidUtilities.isTablet() ? 200 : 320), heightSize - AndroidUtilities.statusBarHeight + getPaddingTop()), MeasureSpec.EXACTLY));
                            } else {
                                child.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(heightSize - AndroidUtilities.statusBarHeight + getPaddingTop(), MeasureSpec.EXACTLY));
                            }
                        } else {
                            child.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(child.getLayoutParams().height, MeasureSpec.EXACTLY));
                        }
                    } else {
                        measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                    }
                }
            }

            @Override
            protected void onLayout(boolean changed, int l, int t, int r, int b) {
                final int count = getChildCount();

                int keyboardSize = SharedConfig.smoothKeyboard ? 0 : measureKeyboardHeight();
                int paddingBottom = keyboardSize <= AndroidUtilities.dp(20) && !AndroidUtilities.isInMultiwindow && !AndroidUtilities.isTablet() ? nameTextView.getEmojiPadding() : 0;
                setBottomClip(paddingBottom);

                for (int i = 0; i < count; i++) {
                    final View child = getChildAt(i);
                    if (child.getVisibility() == GONE) {
                        continue;
                    }
                    final LayoutParams lp = (LayoutParams) child.getLayoutParams();

                    final int width = child.getMeasuredWidth();
                    final int height = child.getMeasuredHeight();

                    int childLeft;
                    int childTop;

                    int gravity = lp.gravity;
                    if (gravity == -1) {
                        gravity = Gravity.TOP | Gravity.LEFT;
                    }

                    final int absoluteGravity = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
                    final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;

                    switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                        case Gravity.CENTER_HORIZONTAL:
                            childLeft = (r - l - width) / 2 + lp.leftMargin - lp.rightMargin;
                            break;
                        case Gravity.RIGHT:
                            childLeft = r - width - lp.rightMargin;
                            break;
                        case Gravity.LEFT:
                        default:
                            childLeft = lp.leftMargin;
                    }

                    switch (verticalGravity) {
                        case Gravity.TOP:
                            childTop = lp.topMargin + getPaddingTop();
                            break;
                        case Gravity.CENTER_VERTICAL:
                            childTop = ((b - paddingBottom) - t - height) / 2 + lp.topMargin - lp.bottomMargin;
                            break;
                        case Gravity.BOTTOM:
                            childTop = ((b - paddingBottom) - t) - height - lp.bottomMargin;
                            break;
                        default:
                            childTop = lp.topMargin;
                    }

                    if (nameTextView != null && nameTextView.isPopupView(child)) {
                        if (AndroidUtilities.isTablet()) {
                            childTop = getMeasuredHeight() - child.getMeasuredHeight();
                        } else {
                            childTop = getMeasuredHeight() + keyboardSize - child.getMeasuredHeight();
                        }
                    }
                    child.layout(childLeft, childTop, childLeft + width, childTop + height);
                }

                notifyHeightChanged();
            }

            @Override
            public void requestLayout() {
                if (ignoreLayout) {
                    return;
                }
                super.requestLayout();
            }
        };
        sizeNotifierFrameLayout.setOnTouchListener((v, event) -> true);
        fragmentView = sizeNotifierFrameLayout;
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        ScrollView scrollView = new ScrollView(context);
        scrollView.setFillViewport(true);
        sizeNotifierFrameLayout.addView(scrollView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        LinearLayout linearLayout1 = new LinearLayout(context);
        scrollView.addView(linearLayout1, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        linearLayout1.setOrientation(LinearLayout.VERTICAL);
        if(forEdit){
            actionBar.setTitle(LocaleController.getString("ChannelEdit", R.string.ChannelEdit));
        }else{
            actionBar.setTitle("Create Shop");
        }

        avatarContainer = new LinearLayout(context);
        avatarContainer.setOrientation(LinearLayout.VERTICAL);
        avatarContainer.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        linearLayout1.addView(avatarContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        FrameLayout frameLayout = new FrameLayout(context);
        avatarContainer.addView(frameLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        avatarImage = new BackupImageView(context) {
            @Override
            public void invalidate() {
                if (avatarOverlay != null) {
                    avatarOverlay.invalidate();
                }
                super.invalidate();
            }

            @Override
            public void invalidate(int l, int t, int r, int b) {
                if (avatarOverlay != null) {
                    avatarOverlay.invalidate();
                }
                super.invalidate(l, t, r, b);
            }
        };
        avatarImage.setRoundRadius(AndroidUtilities.dp(32));
        avatarDrawable.setInfo(5, null, null);
        avatarImage.setImageDrawable(avatarDrawable);
        frameLayout.addView(avatarImage, LayoutHelper.createFrame(64, 64, Gravity.TOP | (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT), LocaleController.isRTL ? 0 : 16, 12, LocaleController.isRTL ? 16 : 0, 12));

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(0x55000000);
        avatarOverlay = new View(context) {
            @Override
            protected void onDraw(Canvas canvas) {
                if (avatarImage != null && avatarImage.getImageReceiver().hasNotThumb()) {
                    paint.setAlpha((int) (0x55 * avatarImage.getImageReceiver().getCurrentAlpha()));
                    canvas.drawCircle(getMeasuredWidth() / 2.0f, getMeasuredHeight() / 2.0f, getMeasuredWidth() / 2.0f, paint);
                }
            }
        };
        avatarOverlay.setOnClickListener(view -> imageUpdater.openMenu(avatar != null, () -> {
            avatar = null;
            currentPictureId = -1;
            local_photo_location = null;
            showAvatarProgress(false, true);
            avatarImage.setImage(null, null, avatarDrawable, null);
        }));


        frameLayout.addView(avatarOverlay, LayoutHelper.createFrame(64, 64, Gravity.TOP | (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT), LocaleController.isRTL ? 0 : 16, 12, LocaleController.isRTL ? 16 : 0, 12));


        avatarEditor = new ImageView(context);
        avatarEditor.setScaleType(ImageView.ScaleType.CENTER);
        avatarEditor.setImageResource(R.drawable.menu_camera_av);
        avatarEditor.setEnabled(true);
        avatarEditor.setClickable(true);
        frameLayout.addView(avatarEditor, LayoutHelper.createFrame(64, 64, Gravity.TOP | (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT), LocaleController.isRTL ? 0 : 16, 12, LocaleController.isRTL ? 16 : 0, 12));
        avatarEditor.setOnClickListener(v -> imageUpdater.openMenu(avatar != null, () -> {
            avatar = null;
            showAvatarProgress(false, true);
            avatarImage.setImage(null, null, avatarDrawable, null);
        }));


        avatarProgressView = new RadialProgressView(context);
        avatarProgressView.setSize(AndroidUtilities.dp(30));
        avatarProgressView.setProgressColor(0xffffffff);
        avatarProgressView.setNoProgress(false);
        frameLayout.addView(avatarProgressView, LayoutHelper.createFrame(64, 64, Gravity.TOP | (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT), LocaleController.isRTL ? 0 : 16, 12, LocaleController.isRTL ? 16 : 0, 12));


        showAvatarProgress(false,false);


        nameTextView = new EditTextEmoji(context, sizeNotifierFrameLayout, this, EditTextEmoji.STYLE_FRAGMENT);
       // nameTextView.setAllowSmoothKeybord(false);
        nameTextView.setHint("Shop name");
        nameTextView.setFocusable(nameTextView.isEnabled());
        nameTextView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s != null){
                    if(forEdit){
                        updateMap.put("title",s.toString());
                    }else{
                        createMap.put("title",s.toString());
                    }
                }
            }
        });
        InputFilter[] inputFilters = new InputFilter[1];
        inputFilters[0] = new InputFilter.LengthFilter(128);
        nameTextView.setFilters(inputFilters);
        frameLayout.addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, LocaleController.isRTL ? 5 : 96, 0, LocaleController.isRTL ? 96 : 5, 0));

        settingsContainer = new LinearLayout(context);
        settingsContainer.setOrientation(LinearLayout.VERTICAL);
        settingsContainer.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        linearLayout1.addView(settingsContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));


        setAvatarCell = new TextCell(context) {
                @Override
                protected void onDraw(Canvas canvas) {
                    canvas.drawLine(LocaleController.isRTL ? 0 : AndroidUtilities.dp(20), getMeasuredHeight() - 1, getMeasuredWidth() - (LocaleController.isRTL ? AndroidUtilities.dp(20) : 0), getMeasuredHeight() - 1, Theme.dividerPaint);
                }
            };
        if(!forEdit)
        setAvatarCell.setVisibility(View.GONE);

        setAvatarCell.setBackgroundDrawable(Theme.getSelectorDrawable(false));
        setAvatarCell.setColors(Theme.key_windowBackgroundWhiteBlueIcon, Theme.key_windowBackgroundWhiteBlueButton);
        setAvatarCell.setOnClickListener(v -> imageUpdater.openMenu(avatar != null, () -> {
            avatar = null;
            showAvatarProgress(false, true);
            avatarImage.setImage(null, null, avatarDrawable, null);
        }));

        setAvatarCell.setTextAndIcon(LocaleController.getString("SetProfilePhoto", R.string.SetProfilePhoto), R.drawable.menu_camera2, true);
        settingsContainer.addView(setAvatarCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        descriptionTextView = new EditTextBoldCursor(context);
        descriptionTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        descriptionTextView.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        descriptionTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        descriptionTextView.setPadding(0, 0, 0, AndroidUtilities.dp(6));
        descriptionTextView.setBackgroundDrawable(null);
        descriptionTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        descriptionTextView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
        descriptionTextView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        descriptionTextView.setFocusable(descriptionTextView.isEnabled());
        inputFilters = new InputFilter[1];
        inputFilters[0] = new InputFilter.LengthFilter(255);
        descriptionTextView.setFilters(inputFilters);
        descriptionTextView.setHint(LocaleController.getString("DescriptionOptionalPlaceholder", R.string.DescriptionOptionalPlaceholder));
        descriptionTextView.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        descriptionTextView.setCursorSize(AndroidUtilities.dp(20));
        descriptionTextView.setCursorWidth(1.5f);
        if (descriptionTextView.isEnabled()) {
            settingsContainer.addView(descriptionTextView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 23, 15, 23, 9));
        } else {
            settingsContainer.addView(descriptionTextView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 23, 12, 23, 6));
        }
        descriptionTextView.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE && doneButton != null) {
               // doneButton.performClick();
                return true;
            }
            return false;
        });
        descriptionTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(forEdit){
                    updateMap.put("description",editable.toString());
                }else{
                    createMap.put("description",editable.toString());
                }
            }
        });

        ActionBarMenu menu = actionBar.createMenu();
        doneButton = menu.addItemWithWidth(done_button, R.drawable.ic_done, AndroidUtilities.dp(56));
        doneButton.setContentDescription(LocaleController.getString("Done", R.string.Done));


        //start
        settingsTopSectionCell = new ShadowSectionCell(context);
        linearLayout1.addView(settingsTopSectionCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        addressContainer = new LinearLayout(context);
        addressContainer.setOrientation(LinearLayout.VERTICAL);
        addressContainer.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        linearLayout1.addView(addressContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));



        headerCell = new HeaderCell(context,Theme.key_dialogTextBlack,21);
        headerCell.setText("Address");
        addressContainer.addView(headerCell, LayoutHelper.createLinear(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        addressCell = new PollEditTextCell(context, null);
        addressCell.createErrorTextView();
        addressCell.setTextAndHint("","Edna mall,3rd floor,number 3",true);
        addressCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        addressCell.addTextWatcher(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(forEdit){
                    updateMap.put("address",s.toString());
                }else{
                    createMap.put("address",s.toString());
                }
            }
        });
        EditTextBoldCursor editText = addressCell.getTextView();
        addressCell.setShowNextButton(true);
        editText.setOnFocusChangeListener((v, hasFocus) -> addressCell.getTextView2().setAlpha(1f));
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        editText.setOnKeyListener((view1, i, keyEvent) -> false);
        addressContainer.addView(addressCell, LayoutHelper.createLinear(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        textInfoPrivacyCell = new TextInfoPrivacyCell(context);
        textInfoPrivacyCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        textInfoPrivacyCell.setText("Add your shop physical address,make sure your address is specific as possible");
        addressContainer.addView(textInfoPrivacyCell,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT));


        cityTextCell = new PollEditTextCell(context, null);
        cityTextCell.createErrorTextView();
        cityTextCell.setTextAndHint("","City",true);
        cityTextCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        cityTextCell.addTextWatcher(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(forEdit){
                    updateMap.put("city",s.toString());
                }else{
                    createMap.put("city",s.toString());
                }
            }
        });
        EditTextBoldCursor cityEditText = cityTextCell.getTextView();
        cityTextCell.setShowNextButton(true);
        cityEditText.setOnFocusChangeListener((v, hasFocus) -> cityTextCell.getTextView2().setAlpha(1f));
        cityEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        cityEditText.setOnKeyListener((view1, i, keyEvent) -> false);
        addressContainer.addView(cityTextCell, LayoutHelper.createLinear(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));


        textInfoPrivacyCell = new TextInfoPrivacyCell(context);
        textInfoPrivacyCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        textInfoPrivacyCell.setText("Set the city where the shop founds");
        addressContainer.addView(textInfoPrivacyCell,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT));


        locationCell = new TextDetailSettingsCell(context);
        //locationCell.setColors(Theme.key_windowBackgroundWhiteBlueIcon);
        locationCell.setBackgroundDrawable(Theme.getSelectorDrawable(false));
        addressContainer.addView(locationCell, LayoutHelper.createLinear(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        locationCell.setOnClickListener(v -> {
            if (!AndroidUtilities.isGoogleMapsInstalled(BusinessCreateActivity.this)) {
                return;
            }
            LocationActivity locationActivity = new LocationActivity(LocationActivity.LOCATION_TYPE_GROUP);
            locationActivity.setDialogId(0);
            locationActivity.setDelegate((location, live, notify, scheduleDate) -> {
                if(location != null) {
                    if(forEdit){
                        updateMap.put("latitude",location.geo.lat);
                        updateMap.put("longtude",location.geo._long);
                    }else{
                        lat = location.geo.lat;
                        _long = location.geo._long;
                    }
                    _location = location.address;
                    locationCell.setValue(_location);
                }
            });
            presentFragment(locationActivity);
        });

        usernameCell = new TextDetailSettingsCell(context);
       // usernameCell.setColors(Theme.key_windowBackgroundWhiteBlueIcon);
        usernameCell.setBackgroundDrawable(Theme.getSelectorDrawable(false));
        addressContainer.addView(usernameCell, LayoutHelper.createLinear(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        usernameCell.setOnClickListener(view -> {
            AddUsernameFragment addUsernameFragment = new AddUsernameFragment();
            addUsernameFragment.setAddUserNameDelegate(user -> {
                if(forEdit){
                    updateMap.put("contact_username",user);
                }else {
                    username = user;
                }
                updateFields();
            });
            presentFragment(addUsernameFragment);
        });

        phoneCell = new TextDetailSettingsCell(context);
        phoneCell.setMultilineDetail(true);
        phoneCell.setBackgroundDrawable(Theme.getSelectorDrawable(false));
        addressContainer.addView(phoneCell, LayoutHelper.createLinear(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        phoneCell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddPhoneFragment addPhoneFragment = new AddPhoneFragment();
                addPhoneFragment.setDelegate(new AddPhoneFragment.Delegate() {
                    @Override
                    public void setPhoneNumbers(ArrayList<String> numbers) {
                        if(forEdit){
                            updateMap.put("phonenumbers",numbers);
                        }else{
                            phoneNumbers.clear();
                            phoneNumbers.addAll(numbers);
                            updateFields();
                        }
                    }
                });
                presentFragment(addPhoneFragment);
            }
        });


        addressSectionCell = new ShadowSectionCell(context);
        addressSectionCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        addressContainer.addView(addressSectionCell, LayoutHelper.createLinear(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));


        websiteCell = new PollEditTextCell(context, null);
        websiteCell.createErrorTextView();
        websiteCell.setTextAndHint("","Website",true);
        websiteCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        websiteCell.addTextWatcher(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }


            @Override
            public void afterTextChanged(Editable s) {
                if(forEdit){
                    updateMap.put("website",s.toString());
                }else{
                    createMap.put("website",s.toString());
                }
            }
        });
        EditTextBoldCursor webSiteEditTExt = websiteCell.getTextView();
        websiteCell.setShowNextButton(true);
        webSiteEditTExt.setOnFocusChangeListener((v, hasFocus) -> websiteCell.getTextView2().setAlpha(1f));
        webSiteEditTExt.setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        webSiteEditTExt.setOnKeyListener((view1, i, keyEvent) -> false);
        addressContainer.addView(websiteCell, LayoutHelper.createLinear(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));


        textInfoPrivacyCell = new TextInfoPrivacyCell(context);
        textInfoPrivacyCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        textInfoPrivacyCell.setText("Link your website or social media profile with your shop");
        addressContainer.addView(textInfoPrivacyCell,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT));



        if(forEdit){
            deleteContainer = new FrameLayout(context);
            deleteContainer.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            linearLayout1.addView(deleteContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            closeCell = new TextCheckCell(context);
            closeCell.setColors(Theme.key_windowBackgroundWhiteRedText5, Theme.key_switchTrackBlue, Theme.key_switchTrackBlueChecked, Theme.key_switchTrackBlueThumb, Theme.key_switchTrackBlueThumbChecked);
            closeCell.setBackgroundDrawable(Theme.getSelectorDrawable(false));
            closeCell.setTextAndCheck("Close shop", currentShop.closed,false);
            deleteContainer.addView(closeCell, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            deleteInfoCell = new ShadowSectionCell(context);
            deleteInfoCell.setBackgroundDrawable(Theme.getThemedDrawable(context, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
            linearLayout1.addView(deleteInfoCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        }

        updateFields();
        return fragmentView;
    }

    @Override
    public boolean canBeginSlide() {
        return checkDiscard();
    }



    private void updateFields(){

        if(forEdit){

            if(currentShop.profilePic != null && currentShop.profilePic.photo != null){
                avatarImage.setImage(currentShop.profilePic.photo,null,null);
            }

            nameTextView.setText(currentShop.title);
            nameTextView.setSelection(nameTextView.length());
            if (currentShop.description != null) {
                descriptionTextView.setText(currentShop.description);
            }
            if(currentShop.address != null){
                addressCell.setText(currentShop.address,false);
            }

            if(locationCell != null){
                if (!TextUtils.isEmpty(currentShop.address)){
                    locationCell.setTextAndValueAndIcon(LocaleController.getString("AttachLocation", R.string.AttachLocation), currentShop.address, R.drawable.menu_location,true);
                } else {

                    locationCell.setTextAndValueAndIcon(LocaleController.getString("AttachLocation", R.string.AttachLocation), "Unknown address", R.drawable.menu_location,true);
                }
            }


            if(usernameCell != null){
                if(!TextUtils.isEmpty(currentShop.contact_username)){
                    String text;
                    text =  "@" + currentShop.contact_username;
                    usernameCell.setTextAndValueAndIcon( LocaleController.getString("Username", R.string.Username),text, R.drawable.menu_chats,true);
                }else{
                    usernameCell.setTextAndValueAndIcon(LocaleController.getString("Username", R.string.Username), "set username",R.drawable.menu_chats,true);

                }
            }

            if(phoneCell != null){
                if(currentShop.phoneNumbers != null){
                    StringBuilder text = new StringBuilder();
                    for(int a = 0;  a < currentShop.phoneNumbers.size(); a++ ){
                        text.append("+").append(currentShop.phoneNumbers.get(a).phonenumber);
                        if(a != currentShop.phoneNumbers.size() - 1)
                            text.append("\n");
                    }
                    phoneCell.setTextAndValueAndIcon( LocaleController.getString("Phone", R.string.Phone), text.toString(), R.drawable.menu_calls,true);
                }else{
                    phoneCell.setTextAndValueAndIcon(LocaleController.getString("Phone", R.string.Phone), "" ,R.drawable.menu_calls,true);

                }

            }

            if(websiteCell != null){
                if(!ShopUtils.isEmpty(currentShop.website)){
                    websiteCell.setText(currentShop.website,false);
                }
            }

            if(cityTextCell != null){
                if(!ShopUtils.isEmpty(currentShop.city)){
                    cityTextCell.setText(currentShop.city,false);
                }
            }

        }else{

            if(locationCell != null){
                if (!TextUtils.isEmpty(_location)){
                    locationCell.setTextAndValueAndIcon(LocaleController.getString("AttachLocation", R.string.AttachLocation), _location, R.drawable.menu_location,true);
                } else {
                    locationCell.setTextAndValueAndIcon(LocaleController.getString("AttachLocation", R.string.AttachLocation), "Unknown address", R.drawable.menu_location,true);
                }
            }


            if(usernameCell != null){
                if(!TextUtils.isEmpty(username)){
                    usernameCell.setTextAndValueAndIcon( LocaleController.getString("Username", R.string.Username),"@" + username, R.drawable.menu_chats,true);
                }else{
                    username = UserConfig.getInstance(currentAccount).getCurrentUser().username;
                    usernameCell.setTextAndValueAndIcon(LocaleController.getString("Username", R.string.Username), TextUtils.isEmpty(username)?"set username":username,R.drawable.menu_chats,true);

                }
            }


            if(phoneCell != null){
                if(phoneNumbers != null && !phoneNumbers.isEmpty()){
                    StringBuilder text = new StringBuilder();
                    for(int a = 0;  a < phoneNumbers.size(); a++ ){
                        text.append("+").append(phoneNumbers.get(a));
                        if(a != phoneNumbers.size() - 1)
                           text.append("\n");
                    }
                    phoneCell.setTextAndValueAndIcon( LocaleController.getString("Phone", R.string.Phone), text.toString(), R.drawable.menu_calls,true);
                }else{
                    String phone = UserConfig.getInstance(currentAccount).getClientPhone();
                    if(!phoneNumbers.contains(phone))
                        phoneNumbers.add(phone);
                    phoneCell.setTextAndValueAndIcon(LocaleController.getString("Phone", R.string.Phone), "+" + phone,R.drawable.menu_calls,true);

                }
            }


        }


    }


    public  void showWebsiteInputAlert(){
        if (getParentActivity() == null) {
            return;
        }
        Context context = getParentActivity();
        final EditTextBoldCursor editText = new EditTextBoldCursor(context);
        editText.setBackgroundDrawable(Theme.createEditTextDrawable(context, true));

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Website Link");
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
        builder.setPositiveButton(LocaleController.getString("Add", R.string.Add), (dialog, which) -> {

        });

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        builder.setView(linearLayout);

        final TextView message = new TextView(context);
        message.setText("Enter link!");
        message.setTextSize(16);
        message.setPadding(AndroidUtilities.dp(23), AndroidUtilities.dp(12), AndroidUtilities.dp(23), AndroidUtilities.dp(6));
        message.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        linearLayout.addView(message, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        editText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        editText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        editText.setMaxLines(1);
        editText.setLines(1);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        editText.setGravity(Gravity.LEFT | Gravity.TOP);
        editText.setSingleLine(true);
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editText.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        editText.setCursorSize(AndroidUtilities.dp(20));
        editText.setCursorWidth(1.5f);
        editText.setPadding(0, AndroidUtilities.dp(4), 0, 0);
        linearLayout.addView(editText, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 36, Gravity.TOP | Gravity.LEFT, 24, 6, 24, 0));
        editText.setOnEditorActionListener((textView, i, keyEvent) -> {
            AndroidUtilities.hideKeyboard(textView);
            return false;
        });
        editText.setHint("Eg. www.youtube.com");
        editText.setSelection(editText.length());

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(dialog -> AndroidUtilities.runOnUIThread(() -> {
            editText.requestFocus();
            AndroidUtilities.showKeyboard(editText);
        }));
        showDialog(alertDialog);
        editText.requestFocus();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (getParentActivity() == null) {
                return;
            }
            if (editText.length() == 0) {
                Vibrator vibrator  =  (Vibrator) ApplicationLoader.applicationContext.getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator != null) {
                    vibrator.vibrate(200);
                }
                AndroidUtilities.shakeView(editText, 2, 0);
                return;
            }

        });
    }


    private void showAvatarProgress(boolean show, boolean animated) {
        if (avatarProgressView == null) {
            return;
        }
        if (avatarAnimation != null) {
            avatarAnimation.cancel();
            avatarAnimation = null;
        }
        if (animated) {
            avatarAnimation = new AnimatorSet();
            if (show) {
                avatarProgressView.setVisibility(View.VISIBLE);
                avatarOverlay.setVisibility(View.VISIBLE);
                avatarAnimation.playTogether(ObjectAnimator.ofFloat(avatarProgressView, View.ALPHA, 1.0f),
                        ObjectAnimator.ofFloat(avatarOverlay, View.ALPHA, 1.0f));
            } else {
                avatarAnimation.playTogether(ObjectAnimator.ofFloat(avatarProgressView, View.ALPHA, 0.0f),
                        ObjectAnimator.ofFloat(avatarOverlay, View.ALPHA, 0.0f));
            }
            avatarAnimation.setDuration(180);
            avatarAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (avatarAnimation == null || avatarProgressView == null) {
                        return;
                    }
                    if (!show) {
                        avatarProgressView.setVisibility(View.INVISIBLE);
                        avatarOverlay.setVisibility(View.INVISIBLE);
                    }
                    avatarAnimation = null;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    avatarAnimation = null;
                }
            });
            avatarAnimation.start();
        } else {
            if (show) {
                avatarProgressView.setAlpha(1.0f);
                avatarProgressView.setVisibility(View.VISIBLE);
                avatarOverlay.setAlpha(1.0f);
                avatarOverlay.setVisibility(View.VISIBLE);
            } else {
                avatarProgressView.setAlpha(0.0f);
                avatarProgressView.setVisibility(View.INVISIBLE);
                avatarOverlay.setAlpha(0.0f);
                avatarOverlay.setVisibility(View.INVISIBLE);
            }
        }
    }


    public void addBotToAdminList(TLRPC.User bot,boolean first){
        TLRPC.TL_chatAdminRights rights = new TLRPC.TL_chatAdminRights();
        rights.invite_users = true;
        TLRPC.TL_channels_editAdmin req = new TLRPC.TL_channels_editAdmin();
        req.channel = MessagesController.getInputChannel(currentChat);
        req.user_id = getMessagesController().getInputUser(bot);
        req.admin_rights = rights;
        req.rank = LocaleController.getString("ChannelAdmin", R.string.ChannelAdmin);;
        getConnectionsManager().sendRequest(req, (response, error) -> {
            if (error == null) {
                getMessagesController().processUpdates((TLRPC.Updates) response, false);
                AndroidUtilities.runOnUIThread(() -> getMessagesController().loadFullChat(chatId, 0, true), 1000);
                AndroidUtilities.runOnUIThread(() -> {
                    isBotAdmin = true;
                    if(!first)
                        processDone();
                });
            } else {
                AndroidUtilities.runOnUIThread(() -> {
                    isBotAdmin = false;
                });
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        if (nameTextView != null) {
            nameTextView.onPause();
        }
        imageUpdater.onPause();
    }


    @Override
    public void onResume() {
        super.onResume();
        if (nameTextView != null) {
            nameTextView.onResume();
            nameTextView.getEditText().requestFocus();
        }
        AndroidUtilities.requestAdjustResize(getParentActivity(), classGuid);
        imageUpdater.onResume();
    }

    @Override
    public void dismissCurrentDialog() {
        if (imageUpdater.dismissCurrentDialog(visibleDialog)) {
            return;
        }
        super.dismissCurrentDialog();
    }

    @Override
    public boolean dismissDialogOnPause(Dialog dialog) {
        return imageUpdater.dismissDialogOnPause(dialog) && super.dismissDialogOnPause(dialog);
    }

    @Override
    public void onRequestPermissionsResultFragment(int requestCode, String[] permissions, int[] grantResults) {
        imageUpdater.onRequestPermissionsResultFragment(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onBackPressed() {
        if (nameTextView != null && nameTextView.isPopupShowing()) {
            nameTextView.hidePopup(true);
            return false;
        }
        return checkDiscard();
    }

    @Override
    public void saveSelfArgs(Bundle args) {
        if (imageUpdater != null && imageUpdater.currentPicturePath != null) {
            args.putString("path", imageUpdater.currentPicturePath);
        }
        if (nameTextView != null) {
            String text = nameTextView.getText().toString();
            if (text != null && text.length() != 0) {
                args.putString("nameTextView", text);
            }
        }
    }

    @Override
    public void restoreSelfArgs(Bundle args) {
        if (imageUpdater != null) {
            imageUpdater.currentPicturePath = args.getString("path");
        }
    }



    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.didShopCreated) {
            if (progressDialog != null) {
                try {
                    progressDialog.dismiss();
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }
            Boolean created = (Boolean)args[0];
            if(created){
                Bundle bundle = new Bundle();
                bundle.putInt("chat_id",chatId);
                presentFragment(new BusinessProfileActivity(bundle), true);
            }else{
                donePressed = false;
                APIError apiError = (APIError)args[1];
                showErrorAlert(apiError);
            }
        }else if(id == NotificationCenter.didShopUpdated){
                if (progressDialog != null) {
                    try {
                        progressDialog.dismiss();
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                }
                Boolean updated = (Boolean)args[0];
                if(updated){
                    finishFragment();
                    removeSelfFromStack();
                }else{
                    donePressed = false;
                    APIError apiError = (APIError)args[1];
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


    @Override
    public void didUploadPhoto(TLRPC.InputFile photo, TLRPC.InputFile video, double videoStartTimestamp, String videoPath, TLRPC.PhotoSize bigSize, TLRPC.PhotoSize smallSize) {
        AndroidUtilities.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                local_photo_location = smallSize.location;
                hasPhoto = true;
                uploadPhoto();
                //uploadPhotoWithProgress();
                setAvatarCell.setVisibility(View.VISIBLE);
                setAvatarCell.setTextAndIcon(LocaleController.getString("ChatSetNewPhoto", R.string.ChatSetNewPhoto), R.drawable.menu_camera2, true);
                avatarImage.setImage(FileLoader.getPathToAttach(smallSize, true).getAbsolutePath(),"100_100",null);
            }
        });
    }


    public void uploadPhotoWithProgress(){
        if(uploadReqId !=-1 ){
            ShopDataController.getInstance(currentAccount).cancelRequest(uploadReqId);
        }

        if (avatarProgressView == null) {
            return;
        }
        avatarProgressView.setProgress(0.0f);
        String path = FileLoader.getPathToAttach(local_photo_location, false).getAbsolutePath();
        uploadReqId = ShopDataController.getInstance(currentAccount).uploadPhoto(path, new DataLoader.DataLoaderDelegate() {
            @Override
            public void fileUploadProgressChanged(String location, long uploadedSize, long totalSize) {
                if (avatarProgressView == null) {
                    return;
                }
                avatarProgressView.setProgress(Math.min(1f, uploadedSize / (float) totalSize));
            }

            @Override
            public void fileDidFailedUpload(String location) {

            }

            @Override
            public void fileDidUploaded(String location, long totalFileSize) {

            }

            @Override
            public void fileDidUploaded(String location, long photo_id, long totalFileSize) {
                if(location.equals(path)){
                    uploaded = true;
                    currentPictureId = photo_id;
                }
            }

            @Override
            public int getObserverTag() {
                return 0;
            }
        });
    }



    private int uploadReqId = -1;
    private void uploadPhoto(){
        if(uploadReqId !=-1 ){
            ShopDataController.getInstance(currentAccount).cancelRequest(uploadReqId);
         }
        String path = FileLoader.getPathToAttach(local_photo_location, false).getAbsolutePath();
         uploadReqId =  ShopDataController.getInstance(currentAccount).uploadPhoto(path, (photo, id) -> {
               if(photo.equals(path)){
                   uploaded = true;
                   currentPictureId = id;
                   if(forEdit){
                       updateMap.put("profile_picture",id);
                   }else{
                       createMap.put("profile_picture",id);
                   }
               }
       });
    }
}

