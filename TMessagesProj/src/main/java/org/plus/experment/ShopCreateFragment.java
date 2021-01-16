package org.plus.experment;//package org.plus.apps.Business.ui;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.os.Vibrator;
//import android.telephony.TelephonyManager;
//import android.text.Editable;
//import android.text.InputFilter;
//import android.text.InputType;
//import android.text.Selection;
//import android.text.Spannable;
//import android.text.SpannableStringBuilder;
//import android.text.Spanned;
//import android.text.TextPaint;
//import android.text.TextUtils;
//import android.text.TextWatcher;
//import android.text.method.LinkMovementMethod;
//import android.text.style.ClickableSpan;
//import android.util.Log;
//import android.util.TypedValue;
//import android.view.Gravity;
//import android.view.KeyEvent;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.inputmethod.EditorInfo;
//import android.widget.FrameLayout;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.DefaultItemAnimator;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import org.plus.PlusBuildVars;
//import org.plus.apps.Business.CitySelectFragment;
//import org.plus.apps.Business.ShopUtils;
//import org.plus.apps.Business.data.SR_object;
//import org.plus.apps.Business.data.ShopDataController;
//import org.plus.apps.Business.ui.cells.SimpleImageTextCell;
//import org.plus.net.APIError;
//import org.telegram.PhoneFormat.PhoneFormat;
//import org.telegram.messenger.AndroidUtilities;
//import org.telegram.messenger.ApplicationLoader;
//import org.telegram.messenger.FileLog;
//import org.telegram.messenger.LocaleController;
//import org.telegram.messenger.MessagesController;
//import org.telegram.messenger.MessagesStorage;
//import org.telegram.messenger.NotificationCenter;
//import org.telegram.messenger.R;
//import org.telegram.messenger.UserConfig;
//import org.telegram.tgnet.ConnectionsManager;
//import org.telegram.tgnet.TLObject;
//import org.telegram.tgnet.TLRPC;
//import org.telegram.ui.ActionBar.ActionBar;
//import org.telegram.ui.ActionBar.ActionBarMenu;
//import org.telegram.ui.ActionBar.AlertDialog;
//import org.telegram.ui.ActionBar.BaseFragment;
//import org.telegram.ui.ActionBar.Theme;
//import org.telegram.ui.Cells.EmptyCell;
//import org.telegram.ui.Cells.HeaderCell;
//import org.telegram.ui.Cells.PollEditTextCell;
//import org.telegram.ui.Cells.SendLocationCell;
//import org.telegram.ui.Cells.ShadowSectionCell;
//import org.telegram.ui.Cells.TextCheckCell;
//import org.telegram.ui.Cells.TextInfoPrivacyCell;
//import org.telegram.ui.Cells.TextSettingsCell;
//import org.telegram.ui.Components.AlertsCreator;
//import org.telegram.ui.Components.EditTextBoldCursor;
//import org.telegram.ui.Components.HintEditText;
//import org.telegram.ui.Components.LayoutHelper;
//import org.telegram.ui.Components.RLottieImageView;
//import org.telegram.ui.Components.RecyclerListView;
//import org.telegram.ui.CountrySelectActivity;
//import org.telegram.ui.LocationActivity;
//
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.concurrent.CountDownLatch;
//
//
//
//public class ShopCreateFragment extends BaseFragment implements NotificationCenter.NotificationCenterDelegate{
//
//    SR_object.shop_create_req shop_create_req = new SR_object.shop_create_req();
//
//
//    public static final String TAG = ShopCreateFragment.class.getSimpleName();
//
//    private TLRPC.GeoPoint currentLocation;
//    private String city;
//    private String address;
//    private String gpsAddress;
//    private ArrayList<String> phoneNumbers  = new ArrayList<>(2);
//
//    private RecyclerListView listView;
//    private ListAdapter adapter;
//    private PollEditTextCell pollEditTextCell;
//
//    private AlertDialog progressDialog;
//
//    private int currentStep;
//    private int chatId;
//    private TLRPC.User bot;
//    private TLRPC.Chat currentChat;
//
//    private boolean isBotAdmin;
//
//    private boolean show;
//
//    private View doneButton;
//    private boolean donePressed;
//
//    private int rowCount;
//    private int channelRow;
//    private int addressSecRow;
//    private int addressHeaderRow;
//    private int addressRow;
//    private int locationSecRow;
//    private int locationRow;
//    private int citySecRow;
//    private int cityRow;
//    private int bottomPaddingRow;
//
//    private final static int done_button = 1;
//
//
//    @SuppressWarnings("FieldCanBeLocal")
//    public static class HintInnerCell extends FrameLayout {
//
//        private RLottieImageView imageView;
//        private TextView messageTextView;
//
//        public HintInnerCell(Context context) {
//            super(context);
//
//            imageView = new RLottieImageView(context);
//            imageView.setAnimation(R.raw.filters, 90, 90);
//            imageView.setScaleType(ImageView.ScaleType.CENTER);
//            imageView.playAnimation();
//            addView(imageView, LayoutHelper.createFrame(90, 90, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 14, 0, 0));
//            imageView.setOnClickListener(v -> {
//                if (!imageView.isPlaying()) {
//                    imageView.setProgress(0.0f);
//                    imageView.playAnimation();
//                }
//            });
//
//            messageTextView = new TextView(context);
//            messageTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText4));
//            messageTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
//            messageTextView.setGravity(Gravity.CENTER);
//            messageTextView.setText(AndroidUtilities.replaceTags(LocaleController.formatString("CreateNewShopInfo", R.string.CreateNewShopInfo)));
//            addView(messageTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 40, 121, 40, 24));
//        }
//
//        @Override
//        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), heightMeasureSpec);
//        }
//    }
//
//    public ShopCreateFragment(Bundle args) {
//        super(args);
//        currentStep = args.getInt("step", 0);
//        chatId = args.getInt("chat_id",0);
//    }
//
//    private void updateRow(boolean notify){
//
//       rowCount = 0;
//       channelRow= -1;
//       addressSecRow= -1;
//       addressHeaderRow= -1;
//       addressRow= -1;
//       locationSecRow= -1;
//       locationRow= -1;
//       citySecRow= -1;
//       cityRow= -1;
//       bottomPaddingRow= -1;
//
//        if(currentStep == 0){
//            channelRow= rowCount++;
//            locationSecRow= rowCount++;
//            locationRow= rowCount++;
//            if(show){
//                citySecRow= rowCount++;
//                cityRow= rowCount++;
//                addressSecRow= rowCount++;
//                addressHeaderRow= rowCount++;
//                addressRow= rowCount++;
//            }
//            bottomPaddingRow= rowCount++;
//        }
//
//    }
//
//    @Override
//    public boolean onFragmentCreate() {
//        currentChat = getMessagesController().getChat(chatId);
//        if (currentChat == null) {
//            final CountDownLatch countDownLatch = new CountDownLatch(1);
//            getMessagesStorage().getStorageQueue().postRunnable(() -> {
//                currentChat = getMessagesStorage().getChat(chatId);
//                countDownLatch.countDown();
//            });
//            try {
//                countDownLatch.await();
//            } catch (Exception e) {
//                FileLog.e(e);
//            }
//            if (currentChat != null) {
//                getMessagesController().putChat(currentChat, true);
//            } else {
//                return false;
//            }
//        }
//        TLObject object = getMessagesController().getUserOrChat(PlusBuildVars.getAuthBot());
//        if(object instanceof TLRPC.User){
//            bot = (TLRPC.User)object;
//            addBotToAdminList(bot,true);
//        }else{
//            return false;
//        }
//        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.didShopCreated);
//        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.didShopUpdated);
//        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.didLoadedTypeObject);
//        updateRow(false);
//        return super.onFragmentCreate();
//    }
//
//
//    @Override
//    public void onFragmentDestroy() {
//        super.onFragmentDestroy();
//        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.didShopCreated);
//        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.didShopUpdated);
//        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.didLoadedTypeObject);
//    }
//
//    private void processDone(){
//        if (donePressed || getParentActivity() == null) {
//            return;
//        }
//
//        if(!isAllRequiredFieldFilled()){
//            return;
//        }
//
//        if(!isBotAdmin){
//            addBotToAdminList(bot,false);
//            return;
//        }
//
//        donePressed = true;
//        SR_object.shop_create_req currentShop = new SR_object.shop_create_req();
//        currentShop.address = address;
//        currentShop.city = city;
//        currentShop.channel_id = ShopUtils.toBotChannelId(currentChat.id);
//        if(currentLocation != null){
//            currentShop.lat = currentLocation.lat;
//            currentShop._long = currentLocation._long;
//        }else{
//            currentShop.lat = -1;
//            currentShop._long = -1;
//        }
//
//       //int reqId =  ShopDataController.getInstance(currentAccount).createShop(currentShop);
////        progressDialog = new AlertDialog(getParentActivity(), 3);
////        progressDialog.setCanCacnel(true);
////        progressDialog.setOnCancelListener(dialog -> {
////            ShopDataController.getInstance(currentAccount).cancelRequest(reqId);
////            donePressed = false;
////        });
////        progressDialog.show();
//    }
//
//    private void updateDone(){
//        if (donePressed || getParentActivity() == null) {
//            return;
//        }
//
//        if(useCurrentValue){
//            phoneNumbers.add(UserConfig.getInstance(currentAccount).getClientPhone());
//        }
//
//        if(inputFields != null &&  inputFields[FIELD_PHONE] != null)
//        {
//            String phone = PhoneFormat.stripExceptNumbers("" + inputFields[FIELD_PHONECODE].getText() + inputFields[FIELD_PHONE].getText());
//            if(!phoneNumbers.contains(phone)){
//                phoneNumbers.add(phone);
//            }
//        }
//
//        if(!isAllRequiredFieldFilled()){
//            return;
//        }
//
//        SR_object.shop_phone_update_req req = new SR_object.shop_phone_update_req();
//        req.phones = UserConfig.getInstance(currentAccount).getClientPhone();
//        req.channel_id = ShopUtils.toBotChannelId(chatId);;
//        final int reqId = ShopDataController.getInstance(currentAccount).updateShopPhoneNumber(req);
//        progressDialog = new AlertDialog(getParentActivity(), 3);
//        progressDialog.setOnCancelListener(dialog -> {
//            donePressed = false;
//        });
//        progressDialog.show();
//    }
//
//    @Override
//    public View createView(Context context) {
//
//        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick(){
//            @Override
//            public void onItemClick(int id) {
//              if(id == -1){
//                  finishFragment();
//              }else if(id == done_button){
//                  if(currentStep == 0){
//                      processDone();
//                  }else if(currentStep == 1){
//                      updateDone();
//                  }else if(currentStep == 2){
//                      saveName();
//
//                  }
//              }
//            }
//        });
//
//        ActionBarMenu menu = actionBar.createMenu();
//        doneButton = menu.addItemWithWidth(done_button, R.drawable.ic_done, AndroidUtilities.dp(56));
//        if(currentStep == 0){
//
//            actionBar.setBackButtonImage(R.drawable.ic_ab_back);
//            actionBar.setAllowOverlayTitle(true);
//            actionBar.setCastShadows(true);
//            actionBar.setTitle("Create Shop");
//
//            fragmentView = new FrameLayout(context);
//            FrameLayout frameLayout = (FrameLayout) fragmentView;
//            frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
//            listView = new RecyclerListView(context);
//            ((DefaultItemAnimator) listView.getItemAnimator()).setDelayAnimations(false);
//            listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
//            listView.setVerticalScrollBarEnabled(false);
//            listView.setAdapter(adapter = new ListAdapter(context));
//            listView.setOnItemClickListener((view, position) -> {
//                 if(position == locationRow){
//                    LocationActivity locationActivity = new LocationActivity(LocationActivity.LOCATION_TYPE_GROUP);
//                    locationActivity.setDialogId(0);
//                    locationActivity.setDelegate((location, live, notify, scheduleDate) -> {
//                        if(location != null) {
//                            currentLocation = location.geo;
//                            gpsAddress = location.address;
//                            if(show){
//                                adapter.notifyItemChanged(locationRow);
//                            }else{
//                                show = true;
//                                updateRow(false);
//                                if(adapter != null){
//                                    adapter.notifyItemRangeInserted(citySecRow,5);
//                                }
//                            }
//                        }
//                    });
//                    presentFragment(locationActivity);
//                }else if(position == addressRow){
//                    pollEditTextCell.getTextView().requestFocus();
//                    AndroidUtilities.showKeyboard(pollEditTextCell.getTextView());
//                } else if (position == cityRow) {
//                    CitySelectFragment citySelectFragment = new CitySelectFragment();
//                    citySelectFragment.setCountrySelectActivityDelegate(name -> {
//                        city = name;
//                        adapter.notifyItemChanged(cityRow);
//                    });
//                    presentFragment(citySelectFragment);
//                }
//            });
//            frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
//        }else if(currentStep == 1){
//            actionBar.setBackButtonImage(R.drawable.ic_ab_back);
//            actionBar.setAllowOverlayTitle(true);
//            actionBar.setCastShadows(true);
//            fragmentView = new FrameLayout(context);
//            FrameLayout frameLayout = (FrameLayout) fragmentView;
//            frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
//            createPhoneInterface(context);
//            actionBar.setTitle(LocaleController.getString("PassportPhone", R.string.PassportPhone));
//            frameLayout.addView(linearLayout2, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
//        }else if(currentStep == 2){
//           actionBar.setBackButtonImage(R.drawable.ic_ab_back);
//           actionBar.setAllowOverlayTitle(true);
//           actionBar.setCastShadows(true);
//           fragmentView = new FrameLayout(context);
//           FrameLayout frameLayout = (FrameLayout) fragmentView;
//           frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
//           createUsernameInterface(context);
//            actionBar.setTitle(LocaleController.getString("Username", R.string.Username));
//           frameLayout.addView(linearLayout3, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
//
//        }
//        return fragmentView;
//    }
//
//
//    private LinearLayout linearLayout3;
//    private EditTextBoldCursor firstNameField;
//    private TextView checkTextView;
//    private TextView helpTextView;
//
//    private int checkReqId;
//    private String lastCheckName;
//    private Runnable checkRunnable;
//    private boolean lastNameAvailable;
//    private boolean ignoreCheck;
//    private CharSequence infoText;
//
//
//    public class LinkSpan extends ClickableSpan {
//
//        private String url;
//
//        public LinkSpan(String value) {
//            url = value;
//        }
//
//        @Override
//        public void updateDrawState(TextPaint ds) {
//            super.updateDrawState(ds);
//            ds.setUnderlineText(false);
//        }
//
//        @Override
//        public void onClick(View widget) {
//            try {
//                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) ApplicationLoader.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE);
//                android.content.ClipData clip = android.content.ClipData.newPlainText("label", url);
//                clipboard.setPrimaryClip(clip);
//                Toast.makeText(getParentActivity(), LocaleController.getString("LinkCopied", R.string.LinkCopied), Toast.LENGTH_SHORT).show();
//            } catch (Exception e) {
//                FileLog.e(e);
//            }
//        }
//    }
//
//    private static class LinkMovementMethodMy extends LinkMovementMethod {
//        @Override
//        public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
//            try {
//                boolean result = super.onTouchEvent(widget, buffer, event);
//                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
//                    Selection.removeSelection(buffer);
//                }
//                return result;
//            } catch (Exception e) {
//                FileLog.e(e);
//            }
//            return false;
//        }
//    }
//
//
//    public void createUsernameInterface(Context context){
//       linearLayout3 = new LinearLayout(context);
//       linearLayout3.setOrientation(LinearLayout.VERTICAL);
//
//        TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(UserConfig.getInstance(currentAccount).getClientUserId());
//        if (user == null) {
//            user = UserConfig.getInstance(currentAccount).getCurrentUser();
//        }
//
//
//        firstNameField = new EditTextBoldCursor(context);
//        firstNameField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
//        firstNameField.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
//        firstNameField.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
//        firstNameField.setBackgroundDrawable(Theme.createEditTextDrawable(context, false));
//        firstNameField.setMaxLines(1);
//        firstNameField.setLines(1);
//        firstNameField.setPadding(0, 0, 0, 0);
//        firstNameField.setSingleLine(true);
//        firstNameField.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
//        firstNameField.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
//        firstNameField.setImeOptions(EditorInfo.IME_ACTION_DONE);
//        firstNameField.setHint(LocaleController.getString("UsernamePlaceholder", R.string.UsernamePlaceholder));
//        firstNameField.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
//        firstNameField.setCursorSize(AndroidUtilities.dp(20));
//        firstNameField.setCursorWidth(1.5f);
//        firstNameField.setOnEditorActionListener((textView, i, keyEvent) -> {
//            if (i == EditorInfo.IME_ACTION_DONE && doneButton != null) {
//                doneButton.performClick();
//                return true;
//            }
//            return false;
//        });
//        firstNameField.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
//                if (ignoreCheck) {
//                    return;
//                }
//                checkUserName(firstNameField.getText().toString(), false);
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//                if (firstNameField.length() > 0) {
//                    String url = "https://" + MessagesController.getInstance(currentAccount).linkPrefix + "/" + firstNameField.getText();
//                    String text = LocaleController.formatString("UsernameHelpLink", R.string.UsernameHelpLink, url);
//                    int index = text.indexOf(url);
//                    SpannableStringBuilder textSpan = new SpannableStringBuilder(text);
//                    if (index >= 0) {
//                        textSpan.setSpan(new LinkSpan(url), index, index + url.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                    }
//                    helpTextView.setText(TextUtils.concat(infoText, "\n\n", textSpan));
//                } else {
//                    helpTextView.setText(infoText);
//                }
//            }
//        });
//
//        linearLayout3.addView(firstNameField, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 36, 24, 24, 24, 0));
//
//        checkTextView = new TextView(context);
//        checkTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
//        checkTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
//        linearLayout3.addView(checkTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 24, 12, 24, 0));
//
//        helpTextView = new TextView(context);
//        helpTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
//        helpTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText8));
//        helpTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
//        helpTextView.setText(infoText = AndroidUtilities.replaceTags(LocaleController.getString("UsernameHelp", R.string.UsernameHelp)));
//        helpTextView.setLinkTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteLinkText));
//        helpTextView.setHighlightColor(Theme.getColor(Theme.key_windowBackgroundWhiteLinkSelection));
//        helpTextView.setMovementMethod(new LinkMovementMethodMy());
//        linearLayout3.addView(helpTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 24, 10, 24, 0));
//
//        checkTextView.setVisibility(View.GONE);
//
//        if (user != null && user.username != null && user.username.length() > 0) {
//            ignoreCheck = true;
//            firstNameField.setText(user.username);
//            firstNameField.setSelection(firstNameField.length());
//            ignoreCheck = false;
//        }
//
//
//    }
//
//    @Override
//    public boolean isSwipeBackEnabled(MotionEvent event) {
//        return false;
//    }
//
//    private boolean checkUserName(final String name, boolean alert) {
//        if (name != null && name.length() > 0) {
//            checkTextView.setVisibility(View.VISIBLE);
//        } else {
//            checkTextView.setVisibility(View.GONE);
//        }
//        if (alert && name.length() == 0) {
//            return true;
//        }
//        if (checkRunnable != null) {
//            AndroidUtilities.cancelRunOnUIThread(checkRunnable);
//            checkRunnable = null;
//            lastCheckName = null;
//            if (checkReqId != 0) {
//                ConnectionsManager.getInstance(currentAccount).cancelRequest(checkReqId, true);
//            }
//        }
//        lastNameAvailable = false;
//        if (name != null) {
//            if (name.startsWith("_") || name.endsWith("_")) {
//                checkTextView.setText(LocaleController.getString("UsernameInvalid", R.string.UsernameInvalid));
//                checkTextView.setTag(Theme.key_windowBackgroundWhiteRedText4);
//                checkTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteRedText4));
//                return false;
//            }
//            for (int a = 0; a < name.length(); a++) {
//                char ch = name.charAt(a);
//                if (a == 0 && ch >= '0' && ch <= '9') {
//                    if (alert) {
//                        AlertsCreator.showSimpleAlert(this, LocaleController.getString("UsernameInvalidStartNumber", R.string.UsernameInvalidStartNumber));
//                    } else {
//                        checkTextView.setText(LocaleController.getString("UsernameInvalidStartNumber", R.string.UsernameInvalidStartNumber));
//                        checkTextView.setTag(Theme.key_windowBackgroundWhiteRedText4);
//                        checkTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteRedText4));
//                    }
//                    return false;
//                }
//                if (!(ch >= '0' && ch <= '9' || ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch == '_')) {
//                    if (alert) {
//                        AlertsCreator.showSimpleAlert(this, LocaleController.getString("UsernameInvalid", R.string.UsernameInvalid));
//                    } else {
//                        checkTextView.setText(LocaleController.getString("UsernameInvalid", R.string.UsernameInvalid));
//                        checkTextView.setTag(Theme.key_windowBackgroundWhiteRedText4);
//                        checkTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteRedText4));
//                    }
//                    return false;
//                }
//            }
//        }
//        if (name == null || name.length() < 5) {
//            if (alert) {
//                AlertsCreator.showSimpleAlert(this, LocaleController.getString("UsernameInvalidShort", R.string.UsernameInvalidShort));
//            } else {
//                checkTextView.setText(LocaleController.getString("UsernameInvalidShort", R.string.UsernameInvalidShort));
//                checkTextView.setTag(Theme.key_windowBackgroundWhiteRedText4);
//                checkTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteRedText4));
//            }
//            return false;
//        }
//        if (name.length() > 32) {
//            if (alert) {
//                AlertsCreator.showSimpleAlert(this, LocaleController.getString("UsernameInvalidLong", R.string.UsernameInvalidLong));
//            } else {
//                checkTextView.setText(LocaleController.getString("UsernameInvalidLong", R.string.UsernameInvalidLong));
//                checkTextView.setTag(Theme.key_windowBackgroundWhiteRedText4);
//                checkTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteRedText4));
//            }
//            return false;
//        }
//
//        if (!alert) {
//            String currentName = UserConfig.getInstance(currentAccount).getCurrentUser().username;
//            if (currentName == null) {
//                currentName = "";
//            }
//            if (name.equals(currentName)) {
//                checkTextView.setText(LocaleController.formatString("UsernameAvailable", R.string.UsernameAvailable, name));
//                checkTextView.setTag(Theme.key_windowBackgroundWhiteGreenText);
//                checkTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGreenText));
//                return true;
//            }
//
//
//            checkTextView.setText(LocaleController.getString("UsernameChecking", R.string.UsernameChecking));
//            checkTextView.setTag(Theme.key_windowBackgroundWhiteGrayText8);
//            checkTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText8));
//            lastCheckName = name;
//            checkRunnable = () -> {
//                TLRPC.TL_account_checkUsername req = new TLRPC.TL_account_checkUsername();
//                req.username = name;
//                checkReqId = ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
//                    checkReqId = 0;
//                    if (lastCheckName != null && lastCheckName.equals(name)) {
//                        if (error == null && response instanceof TLRPC.TL_boolTrue) {
//                            checkTextView.setText(LocaleController.formatString("UsernameAvailable", R.string.UsernameAvailable, name));
//                            checkTextView.setTag(Theme.key_windowBackgroundWhiteGreenText);
//                            checkTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGreenText));
//                            lastNameAvailable = true;
//                        } else {
//                            checkTextView.setText(LocaleController.getString("UsernameInUse", R.string.UsernameInUse));
//                            checkTextView.setTag(Theme.key_windowBackgroundWhiteRedText4);
//                            checkTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteRedText4));
//                            lastNameAvailable = false;
//                        }
//                    }
//                }), ConnectionsManager.RequestFlagFailOnServerErrors);
//            };
//            AndroidUtilities.runOnUIThread(checkRunnable, 300);
//        }
//        return true;
//    }
//
//    private void saveName() {
//
//        if (!checkUserName(firstNameField.getText().toString(), true)) {
//            return;
//        }
//        TLRPC.User user = UserConfig.getInstance(currentAccount).getCurrentUser();
//        if (getParentActivity() == null || user == null) {
//            return;
//        }
//        String currentName = user.username;
//        if (currentName == null) {
//            currentName = "";
//        }
//        String newName = firstNameField.getText().toString();
//        if (currentName.equals(newName)) {
//            finishFragment();
//            return;
//        }
//
//        final AlertDialog progressDialog = new AlertDialog(getParentActivity(), 3);
//
//        final TLRPC.TL_account_updateUsername req = new TLRPC.TL_account_updateUsername();
//        req.username = newName;
//
//        NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.updateInterfaces, MessagesController.UPDATE_MASK_NAME);
//        final int reqId = ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> {
//            if (error == null) {
//                final TLRPC.User user1 = (TLRPC.User)response;
//                AndroidUtilities.runOnUIThread(() -> {
//                    try {
//                        progressDialog.dismiss();
//                    } catch (Exception e) {
//                        FileLog.e(e);
//                    }
//                    ArrayList<TLRPC.User> users = new ArrayList<>();
//                    users.add(user1);
//                    MessagesController.getInstance(currentAccount).putUsers(users, false);
//                    MessagesStorage.getInstance(currentAccount).putUsersAndChats(users, null, false, true);
//                    UserConfig.getInstance(currentAccount).saveConfig(true);
//
//                    finishFragment();
//                });
//            } else {
//                AndroidUtilities.runOnUIThread(() -> {
//                    try {
//                        progressDialog.dismiss();
//                    } catch (Exception e) {
//                        FileLog.e(e);
//                    }
//                    AlertsCreator.processError(currentAccount, error, ShopCreateFragment.this, req);
//                });
//            }
//        }, ConnectionsManager.RequestFlagFailOnServerErrors);
//        ConnectionsManager.getInstance(currentAccount).bindRequestToGuid(reqId, classGuid);
//
//        progressDialog.setOnCancelListener(dialog -> ConnectionsManager.getInstance(currentAccount).cancelRequest(reqId, true));
//        progressDialog.show();
//    }
//
//
//    @Override
//    public void onTransitionAnimationEnd(boolean isOpen, boolean backward) {
//        if (isOpen && currentStep == 2) {
//            firstNameField.requestFocus();
//            AndroidUtilities.showKeyboard(firstNameField);
//        }
//    }
//
//
//    private boolean isAllRequiredFieldFilled() {
//        if (currentStep == 0) {
//            int row = -1;
//
//            if(currentLocation == null){
//                row = locationRow;
//            }
//            if (row == -1) {
//                return true;
//            }
//
//            RecyclerView.ViewHolder holder = listView.findViewHolderForAdapterPosition(row);
//            if (holder != null) {
//                Vibrator v = (Vibrator) getParentActivity().getSystemService(Context.VIBRATOR_SERVICE);
//                if (v != null) {
//                    v.vibrate(200);
//                }
//                AndroidUtilities.shakeView(holder.itemView, 2, 0);
//            }
//            return false;
//        }else if(currentStep == 1){
//            if(!useCurrentValue && TextUtils.isEmpty(inputFields[FIELD_PHONE].getText())){
//                if (inputFields[FIELD_PHONE] != null) {
//                    Vibrator v = (Vibrator) getParentActivity().getSystemService(Context.VIBRATOR_SERVICE);
//                    if (v != null) {
//                        v.vibrate(200);
//                    }
//                    AndroidUtilities.shakeView(inputFields[FIELD_PHONE], 2, 0);
//                }
//
//            }else{
//                return true;
//            }
//        }
//        return false;
//    }
//
//
//    @Override
//    public void didReceivedNotification(int id, int account, Object... args) {
//        if (id == NotificationCenter.didShopCreated) {
//            if (progressDialog != null) {
//                try {
//                    progressDialog.dismiss();
//                } catch (Exception e) {
//                    FileLog.e(e);
//                }
//            }
//            Boolean created = (Boolean)args[0];
//            if(created){
//                long chat_id = (long) args[1];
//                Bundle bundle = new Bundle();
//                bundle.putInt("step", 1);
//                bundle.putInt("chat_id", ShopUtils.toClientChannelId(chat_id));
//                presentFragment(new ShopCreateFragment(bundle), true);
//            }else{
//                donePressed = false;
//                APIError apiError = (APIError)args[1];
//                showErrorAlert(apiError);
//            }
//        }else if(id == NotificationCenter.didShopUpdated){
//            if (progressDialog != null) {
//                try {
//                    progressDialog.dismiss();
//                } catch (Exception e) {
//                    FileLog.e(e);
//                }
//            }
//            Boolean updated = (Boolean)args[0];
//            long chat_id = (long) args[1];
//            if(updated){
//                if(currentStep == 1){
//                    Bundle bundle = new Bundle();
//                    bundle.putInt("step", 2);
//                    bundle.putInt("chat_id", ShopUtils.toClientChannelId(chat_id));
//                    presentFragment(new ShopCreateFragment(bundle), true);
//                }else if(currentStep == 2){
//                    Bundle bundle = new Bundle();
//                    bundle.putInt("chat_id",ShopUtils.toClientChannelId(chat_id));
//                    presentFragment(new BusinessProfileActivity(bundle),true);
//                }
//            }else{
//                donePressed = false;
//                APIError apiError = (APIError)args[1];
//                apiError.setMessage("error updating shop!");
//                showErrorAlert(apiError);
//            }
//        }
//    }
//
//    private void showErrorAlert(APIError error) {
//        if (getParentActivity() == null) {
//            return;
//        }
//        if(error != null && TextUtils.isEmpty(error.message())){
//            error.setMessage("Unknown error, try again!");
//        }
//        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
//        builder.setTitle(LocaleController.getString("AppNameHulu", R.string.AppNameHulu));
//        builder.setMessage(error.message());
//        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
//        showDialog(builder.create());
//    }
//
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        if (adapter != null) {
//            adapter.notifyDataSetChanged();
//        }
//
//        if(currentStep == 2){
//            SharedPreferences preferences = MessagesController.getGlobalMainSettings();
//            boolean animations = preferences.getBoolean("view_animations", true);
//            if (!animations) {
//                firstNameField.requestFocus();
//                AndroidUtilities.showKeyboard(firstNameField);
//            }
//        }
//
//    }
//
//    private void requestFocusForAddressInput(){
//        RecyclerView.ViewHolder holder = listView.findViewHolderForAdapterPosition(addressRow);
//        if(holder != null){
//            if(holder.itemView instanceof PollEditTextCell){
//                PollEditTextCell cell = (PollEditTextCell) holder.itemView;
//                cell.getTextView().requestFocus();
//                AndroidUtilities.showKeyboard(cell.getTextView());
//            }
//        }
//    }
//
//    private  class ListAdapter extends RecyclerListView.SelectionAdapter{
//
//        private Context mContext;
//
//        public ListAdapter(Context mContext) {
//            this.mContext = mContext;
//        }
//
//        @Override
//        public boolean isEnabled(RecyclerView.ViewHolder holder) {
//            int type = holder.getItemViewType();
//            return type != 0 &&  type != 1 && type != 4 && type != 8;
//        }
//
//
//        @Override
//        public int getItemViewType(int position) {
//            if(position == channelRow){
//                return 0;
//            }else if(position ==  addressSecRow || position == locationSecRow || position == citySecRow){
//                return 1;
//            }else if(position == addressHeaderRow){
//                return 4;
//            }else if(position == locationRow || position == cityRow){
//                return 5;
//            }else if(position == addressRow){
//                return 7;
//            }else if(position == bottomPaddingRow){
//                return 8;
//            }
//            return super.getItemViewType(position);
//        }
//
//
//        @NonNull
//        @Override
//        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//            View view;
//            switch (viewType){
//                case 0:
//                     view  = new HintInnerCell(mContext);
//                     view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
//                    break;
//                case 1:
//                    view = new ShadowSectionCell(mContext);
//                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
//                    break;
//                case 2:
//                    view = new SimpleImageTextCell(mContext, 8);
//                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
//                    break;
//                case 3:
//                    view = new TextInfoPrivacyCell(mContext);
//                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
//                    break;
//                case 4:
//                    view = new HeaderCell(mContext,Theme.key_dialogTextBlack,21);
//                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
//                    break;
//                case 5:
//                    view = new SendLocationCell(mContext,false);
//                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
//                    break;
//                case 9:
//                    view = new TextSettingsCell(mContext);
//                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
//                    break;
//                case 7:
//                    PollEditTextCell cell = new PollEditTextCell(mContext, null);
//                    pollEditTextCell = cell;
//                    cell.createErrorTextView();
//                    cell.setTextAndHint("","Edna mall,3rd floor,number 3",true);
//                    cell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
//                    cell.addTextWatcher(new TextWatcher() {
//                        @Override
//                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//                        }
//
//                        @Override
//                        public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//                        }
//
//                        @Override
//                        public void afterTextChanged(Editable s) {
//                            if (cell.getTag() != null) {
//                                return;
//                            }
//                            address =  s.toString();
//                        }
//                    });
//                    EditTextBoldCursor editText = cell.getTextView();
//                    cell.setShowNextButton(true);
//                    editText.setOnFocusChangeListener((v, hasFocus) -> cell.getTextView2().setAlpha(1f));
//                    editText.setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
//                    editText.setOnKeyListener((view1, i, keyEvent) -> false);
//                    view = cell;
//                    break;
//                case 8:
//                default:
//                    view = new EmptyCell(mContext,100);
//                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
//                    break;
//
//            }
//            view.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//            return new RecyclerListView.Holder(view);
//        }
//
//
//        @Override
//        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//            switch (holder.getItemViewType()){
//                case 2:{
//                    SimpleImageTextCell cell = (SimpleImageTextCell)holder.itemView;
//                    break;
//                }
//                case 3:{
//                    TextInfoPrivacyCell cell = (TextInfoPrivacyCell)holder.itemView;
//                    break;
//                }
//                case 4:{
//                    HeaderCell cell = (HeaderCell)holder.itemView;
//                    if(position == addressHeaderRow){
//                        cell.setText(LocaleController.getString("ShopAddresss",R.string.ShopAddresss));
//                    }
//                    break;
//                }
//                case 5:{
//                    SendLocationCell cell = (SendLocationCell)holder.itemView;
//                    if(position == locationRow){
//                        cell.setText("Location",TextUtils.isEmpty(gpsAddress)?"Set your shop GPS location":gpsAddress);
//                    }else if(position == cityRow){
//                        cell.setText("City",TextUtils.isEmpty(city)?"Set your city":city);
//                        cell.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_city));
//                    }
//                    break;
//                }
//            }
//        }
//
//        @Override
//        public int getItemCount() {
//            return rowCount;
//        }
//    }
//
//
//    public void addBotToAdminList(TLRPC.User bot,boolean first){
//        TLRPC.TL_chatAdminRights rights = new TLRPC.TL_chatAdminRights();
//        rights.invite_users = true;
//        TLRPC.TL_channels_editAdmin req = new TLRPC.TL_channels_editAdmin();
//        req.channel = MessagesController.getInputChannel(currentChat);
//        req.user_id = getMessagesController().getInputUser(bot);
//        req.admin_rights = rights;
//        req.rank = LocaleController.getString("ChannelAdmin", R.string.ChannelAdmin);;
//        getConnectionsManager().sendRequest(req, (response, error) -> {
//            if (error == null) {
//                getMessagesController().processUpdates((TLRPC.Updates) response, false);
//                AndroidUtilities.runOnUIThread(() -> getMessagesController().loadFullChat(chatId, 0, true), 1000);
//                AndroidUtilities.runOnUIThread(() -> {
//                    is//package org.plus.apps.Business.ui;
////
////import android.content.Context;
////import android.content.SharedPreferences;
////import android.os.Bundle;
////import android.os.Vibrator;
////import android.telephony.TelephonyManager;
////import android.text.Editable;
////import android.text.InputFilter;
////import android.text.InputType;
////import android.text.Selection;
////import android.text.Spannable;
////import android.text.SpannableStringBuilder;
////import android.text.Spanned;
////import android.text.TextPaint;
////import android.text.TextUtils;
////import android.text.TextWatcher;
////import android.text.method.LinkMovementMethod;
////import android.text.style.ClickableSpan;
////import android.util.Log;
////import android.util.TypedValue;
////import android.view.Gravity;
////import android.view.KeyEvent;
////import android.view.MotionEvent;
////import android.view.View;
////import android.view.ViewGroup;
////import android.view.inputmethod.EditorInfo;
////import android.widget.FrameLayout;
////import android.widget.ImageView;
////import android.widget.LinearLayout;
////import android.widget.TextView;
////import android.widget.Toast;
////
////import androidx.annotation.NonNull;
////import androidx.recyclerview.widget.DefaultItemAnimator;
////import androidx.recyclerview.widget.LinearLayoutManager;
////import androidx.recyclerview.widget.RecyclerView;
////
////import org.plus.PlusBuildVars;
////import org.plus.apps.Business.CitySelectFragment;
////import org.plus.apps.Business.ShopUtils;
////import org.plus.apps.Business.data.SR_object;
////import org.plus.apps.Business.data.ShopDataController;
////import org.plus.apps.Business.ui.cells.SimpleImageTextCell;
////import org.plus.net.APIError;
////import org.telegram.PhoneFormat.PhoneFormat;
////import org.telegram.messenger.AndroidUtilities;
////import org.telegram.messenger.ApplicationLoader;
////import org.telegram.messenger.FileLog;
////import org.telegram.messenger.LocaleController;
////import org.telegram.messenger.MessagesController;
////import org.telegram.messenger.MessagesStorage;
////import org.telegram.messenger.NotificationCenter;
////import org.telegram.messenger.R;
////import org.telegram.messenger.UserConfig;
////import org.telegram.tgnet.ConnectionsManager;
////import org.telegram.tgnet.TLObject;
////import org.telegram.tgnet.TLRPC;
////import org.telegram.ui.ActionBar.ActionBar;
////import org.telegram.ui.ActionBar.ActionBarMenu;
////import org.telegram.ui.ActionBar.AlertDialog;
////import org.telegram.ui.ActionBar.BaseFragment;
////import org.telegram.ui.ActionBar.Theme;
////import org.telegram.ui.Cells.EmptyCell;
////import org.telegram.ui.Cells.HeaderCell;
////import org.telegram.ui.Cells.PollEditTextCell;
////import org.telegram.ui.Cells.SendLocationCell;
////import org.telegram.ui.Cells.ShadowSectionCell;
////import org.telegram.ui.Cells.TextCheckCell;
////import org.telegram.ui.Cells.TextInfoPrivacyCell;
////import org.telegram.ui.Cells.TextSettingsCell;
////import org.telegram.ui.Components.AlertsCreator;
////import org.telegram.ui.Components.EditTextBoldCursor;
////import org.telegram.ui.Components.HintEditText;
////import org.telegram.ui.Components.LayoutHelper;
////import org.telegram.ui.Components.RLottieImageView;
////import org.telegram.ui.Components.RecyclerListView;
////import org.telegram.ui.CountrySelectActivity;
////import org.telegram.ui.LocationActivity;
////
////import java.io.BufferedReader;
////import java.io.InputStreamReader;
////import java.util.ArrayList;
////import java.util.Collections;
////import java.util.HashMap;
////import java.util.concurrent.CountDownLatch;
////
////
////
////public class ShopCreateFragment extends BaseFragment implements NotificationCenter.NotificationCenterDelegate{
////
////    SR_object.shop_create_req shop_create_req = new SR_object.shop_create_req();
////
////
////    public static final String TAG = ShopCreateFragment.class.getSimpleName();
////
////    private TLRPC.GeoPoint currentLocation;
////    private String city;
////    private String address;
////    private String gpsAddress;
////    private ArrayList<String> phoneNumbers  = new ArrayList<>(2);
////
////    private RecyclerListView listView;
////    private ListAdapter adapter;
////    private PollEditTextCell pollEditTextCell;
////
////    private AlertDialog progressDialog;
////
////    private int currentStep;
////    private int chatId;
////    private TLRPC.User bot;
////    private TLRPC.Chat currentChat;
////
////    private boolean isBotAdmin;
////
////    private boolean show;
////
////    private View doneButton;
////    private boolean donePressed;
////
////    private int rowCount;
////    private int channelRow;
////    private int addressSecRow;
////    private int addressHeaderRow;
////    private int addressRow;
////    private int locationSecRow;
////    private int locationRow;
////    private int citySecRow;
////    private int cityRow;
////    private int bottomPaddingRow;
////
////    private final static int done_button = 1;
////
////
////    @SuppressWarnings("FieldCanBeLocal")
////    public static class HintInnerCell extends FrameLayout {
////
////        private RLottieImageView imageView;
////        private TextView messageTextView;
////
////        public HintInnerCell(Context context) {
////            super(context);
////
////            imageView = new RLottieImageView(context);
////            imageView.setAnimation(R.raw.filters, 90, 90);
////            imageView.setScaleType(ImageView.ScaleType.CENTER);
////            imageView.playAnimation();
////            addView(imageView, LayoutHelper.createFrame(90, 90, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 14, 0, 0));
////            imageView.setOnClickListener(v -> {
////                if (!imageView.isPlaying()) {
////                    imageView.setProgress(0.0f);
////                    imageView.playAnimation();
////                }
////            });
////
////            messageTextView = new TextView(context);
////            messageTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText4));
////            messageTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
////            messageTextView.setGravity(Gravity.CENTER);
////            messageTextView.setText(AndroidUtilities.replaceTags(LocaleController.formatString("CreateNewShopInfo", R.string.CreateNewShopInfo)));
////            addView(messageTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 40, 121, 40, 24));
////        }
////
////        @Override
////        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
////            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), heightMeasureSpec);
////        }
////    }
////
////    public ShopCreateFragment(Bundle args) {
////        super(args);
////        currentStep = args.getInt("step", 0);
////        chatId = args.getInt("chat_id",0);
////    }
////
////    private void updateRow(boolean notify){
////
////       rowCount = 0;
////       channelRow= -1;
////       addressSecRow= -1;
////       addressHeaderRow= -1;
////       addressRow= -1;
////       locationSecRow= -1;
////       locationRow= -1;
////       citySecRow= -1;
////       cityRow= -1;
////       bottomPaddingRow= -1;
////
////        if(currentStep == 0){
////            channelRow= rowCount++;
////            locationSecRow= rowCount++;
////            locationRow= rowCount++;
////            if(show){
////                citySecRow= rowCount++;
////                cityRow= rowCount++;
////                addressSecRow= rowCount++;
////                addressHeaderRow= rowCount++;
////                addressRow= rowCount++;
////            }
////            bottomPaddingRow= rowCount++;
////        }
////
////    }
////
////    @Override
////    public boolean onFragmentCreate() {
////        currentChat = getMessagesController().getChat(chatId);
////        if (currentChat == null) {
////            final CountDownLatch countDownLatch = new CountDownLatch(1);
////            getMessagesStorage().getStorageQueue().postRunnable(() -> {
////                currentChat = getMessagesStorage().getChat(chatId);
////                countDownLatch.countDown();
////            });
////            try {
////                countDownLatch.await();
////            } catch (Exception e) {
////                FileLog.e(e);
////            }
////            if (currentChat != null) {
////                getMessagesController().putChat(currentChat, true);
////            } else {
////                return false;
////            }
////        }
////        TLObject object = getMessagesController().getUserOrChat(PlusBuildVars.getAuthBot());
////        if(object instanceof TLRPC.User){
////            bot = (TLRPC.User)object;
////            addBotToAdminList(bot,true);
////        }else{
////            return false;
////        }
////        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.didShopCreated);
////        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.didShopUpdated);
////        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.didLoadedTypeObject);
////        updateRow(false);
////        return super.onFragmentCreate();
////    }
////
////
////    @Override
////    public void onFragmentDestroy() {
////        super.onFragmentDestroy();
////        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.didShopCreated);
////        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.didShopUpdated);
////        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.didLoadedTypeObject);
////    }
////
////    private void processDone(){
////        if (donePressed || getParentActivity() == null) {
////            return;
////        }
////
////        if(!isAllRequiredFieldFilled()){
////            return;
////        }
////
////        if(!isBotAdmin){
////            addBotToAdminList(bot,false);
////            return;
////        }
////
////        donePressed = true;
////        SR_object.shop_create_req currentShop = new SR_object.shop_create_req();
////        currentShop.address = address;
////        currentShop.city = city;
////        currentShop.channel_id = ShopUtils.toBotChannelId(currentChat.id);
////        if(currentLocation != null){
////            currentShop.lat = currentLocation.lat;
////            currentShop._long = currentLocation._long;
////        }else{
////            currentShop.lat = -1;
////            currentShop._long = -1;
////        }
////
////       //int reqId =  ShopDataController.getInstance(currentAccount).createShop(currentShop);
//////        progressDialog = new AlertDialog(getParentActivity(), 3);
//////        progressDialog.setCanCacnel(true);
//////        progressDialog.setOnCancelListener(dialog -> {
//////            ShopDataController.getInstance(currentAccount).cancelRequest(reqId);
//////            donePressed = false;
//////        });
//////        progressDialog.show();
////    }
////
////    private void updateDone(){
////        if (donePressed || getParentActivity() == null) {
////            return;
////        }
////
////        if(useCurrentValue){
////            phoneNumbers.add(UserConfig.getInstance(currentAccount).getClientPhone());
////        }
////
////        if(inputFields != null &&  inputFields[FIELD_PHONE] != null)
////        {
////            String phone = PhoneFormat.stripExceptNumbers("" + inputFields[FIELD_PHONECODE].getText() + inputFields[FIELD_PHONE].getText());
////            if(!phoneNumbers.contains(phone)){
////                phoneNumbers.add(phone);
////            }
////        }
////
////        if(!isAllRequiredFieldFilled()){
////            return;
////        }
////
////        SR_object.shop_phone_update_req req = new SR_object.shop_phone_update_req();
////        req.phones = UserConfig.getInstance(currentAccount).getClientPhone();
////        req.channel_id = ShopUtils.toBotChannelId(chatId);;
////        final int reqId = ShopDataController.getInstance(currentAccount).updateShopPhoneNumber(req);
////        progressDialog = new AlertDialog(getParentActivity(), 3);
////        progressDialog.setOnCancelListener(dialog -> {
////            donePressed = false;
////        });
////        progressDialog.show();
////    }
////
////    @Override
////    public View createView(Context context) {
////
////        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick(){
////            @Override
////            public void onItemClick(int id) {
////              if(id == -1){
////                  finishFragment();
////              }else if(id == done_button){
////                  if(currentStep == 0){
////                      processDone();
////                  }else if(currentStep == 1){
////                      updateDone();
////                  }else if(currentStep == 2){
////                      saveName();
////
////                  }
////              }
////            }
////        });
////
////        ActionBarMenu menu = actionBar.createMenu();
////        doneButton = menu.addItemWithWidth(done_button, R.drawable.ic_done, AndroidUtilities.dp(56));
////        if(currentStep == 0){
////
////            actionBar.setBackButtonImage(R.drawable.ic_ab_back);
////            actionBar.setAllowOverlayTitle(true);
////            actionBar.setCastShadows(true);
////            actionBar.setTitle("Create Shop");
////
////            fragmentView = new FrameLayout(context);
////            FrameLayout frameLayout = (FrameLayout) fragmentView;
////            frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
////            listView = new RecyclerListView(context);
////            ((DefaultItemAnimator) listView.getItemAnimator()).setDelayAnimations(false);
////            listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
////            listView.setVerticalScrollBarEnabled(false);
////            listView.setAdapter(adapter = new ListAdapter(context));
////            listView.setOnItemClickListener((view, position) -> {
////                 if(position == locationRow){
////                    LocationActivity locationActivity = new LocationActivity(LocationActivity.LOCATION_TYPE_GROUP);
////                    locationActivity.setDialogId(0);
////                    locationActivity.setDelegate((location, live, notify, scheduleDate) -> {
////                        if(location != null) {
////                            currentLocation = location.geo;
////                            gpsAddress = location.address;
////                            if(show){
////                                adapter.notifyItemChanged(locationRow);
////                            }else{
////                                show = true;
////                                updateRow(false);
////                                if(adapter != null){
////                                    adapter.notifyItemRangeInserted(citySecRow,5);
////                                }
////                            }
////                        }
////                    });
////                    presentFragment(locationActivity);
////                }else if(position == addressRow){
////                    pollEditTextCell.getTextView().requestFocus();
////                    AndroidUtilities.showKeyboard(pollEditTextCell.getTextView());
////                } else if (position == cityRow) {
////                    CitySelectFragment citySelectFragment = new CitySelectFragment();
////                    citySelectFragment.setCountrySelectActivityDelegate(name -> {
////                        city = name;
////                        adapter.notifyItemChanged(cityRow);
////                    });
////                    presentFragment(citySelectFragment);
////                }
////            });
////            frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
////        }else if(currentStep == 1){
////            actionBar.setBackButtonImage(R.drawable.ic_ab_back);
////            actionBar.setAllowOverlayTitle(true);
////            actionBar.setCastShadows(true);
////            fragmentView = new FrameLayout(context);
////            FrameLayout frameLayout = (FrameLayout) fragmentView;
////            frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
////            createPhoneInterface(context);
////            actionBar.setTitle(LocaleController.getString("PassportPhone", R.string.PassportPhone));
////            frameLayout.addView(linearLayout2, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
////        }else if(currentStep == 2){
////           actionBar.setBackButtonImage(R.drawable.ic_ab_back);
////           actionBar.setAllowOverlayTitle(true);
////           actionBar.setCastShadows(true);
////           fragmentView = new FrameLayout(context);
////           FrameLayout frameLayout = (FrameLayout) fragmentView;
////           frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
////           createUsernameInterface(context);
////            actionBar.setTitle(LocaleController.getString("Username", R.string.Username));
////           frameLayout.addView(linearLayout3, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
////
////        }
////        return fragmentView;
////    }
////
////
////    private LinearLayout linearLayout3;
////    private EditTextBoldCursor firstNameField;
////    private TextView checkTextView;
////    private TextView helpTextView;
////
////    private int checkReqId;
////    private String lastCheckName;
////    private Runnable checkRunnable;
////    private boolean lastNameAvailable;
////    private boolean ignoreCheck;
////    private CharSequence infoText;
////
////
////    public class LinkSpan extends ClickableSpan {
////
////        private String url;
////
////        public LinkSpan(String value) {
////            url = value;
////        }
////
////        @Override
////        public void updateDrawState(TextPaint ds) {
////            super.updateDrawState(ds);
////            ds.setUnderlineText(false);
////        }
////
////        @Override
////        public void onClick(View widget) {
////            try {
////                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) ApplicationLoader.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE);
////                android.content.ClipData clip = android.content.ClipData.newPlainText("label", url);
////                clipboard.setPrimaryClip(clip);
////                Toast.makeText(getParentActivity(), LocaleController.getString("LinkCopied", R.string.LinkCopied), Toast.LENGTH_SHORT).show();
////            } catch (Exception e) {
////                FileLog.e(e);
////            }
////        }
////    }
////
////    private static class LinkMovementMethodMy extends LinkMovementMethod {
////        @Override
////        public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
////            try {
////                boolean result = super.onTouchEvent(widget, buffer, event);
////                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
////                    Selection.removeSelection(buffer);
////                }
////                return result;
////            } catch (Exception e) {
////                FileLog.e(e);
////            }
////            return false;
////        }
////    }
////
////
////    public void createUsernameInterface(Context context){
////       linearLayout3 = new LinearLayout(context);
////       linearLayout3.setOrientation(LinearLayout.VERTICAL);
////
////        TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(UserConfig.getInstance(currentAccount).getClientUserId());
////        if (user == null) {
////            user = UserConfig.getInstance(currentAccount).getCurrentUser();
////        }
////
////
////        firstNameField = new EditTextBoldCursor(context);
////        firstNameField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
////        firstNameField.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
////        firstNameField.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
////        firstNameField.setBackgroundDrawable(Theme.createEditTextDrawable(context, false));
////        firstNameField.setMaxLines(1);
////        firstNameField.setLines(1);
////        firstNameField.setPadding(0, 0, 0, 0);
////        firstNameField.setSingleLine(true);
////        firstNameField.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
////        firstNameField.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
////        firstNameField.setImeOptions(EditorInfo.IME_ACTION_DONE);
////        firstNameField.setHint(LocaleController.getString("UsernamePlaceholder", R.string.UsernamePlaceholder));
////        firstNameField.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
////        firstNameField.setCursorSize(AndroidUtilities.dp(20));
////        firstNameField.setCursorWidth(1.5f);
////        firstNameField.setOnEditorActionListener((textView, i, keyEvent) -> {
////            if (i == EditorInfo.IME_ACTION_DONE && doneButton != null) {
////                doneButton.performClick();
////                return true;
////            }
////            return false;
////        });
////        firstNameField.addTextChangedListener(new TextWatcher() {
////            @Override
////            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
////
////            }
////
////            @Override
////            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
////                if (ignoreCheck) {
////                    return;
////                }
////                checkUserName(firstNameField.getText().toString(), false);
////            }
////
////            @Override
////            public void afterTextChanged(Editable editable) {
////                if (firstNameField.length() > 0) {
////                    String url = "https://" + MessagesController.getInstance(currentAccount).linkPrefix + "/" + firstNameField.getText();
////                    String text = LocaleController.formatString("UsernameHelpLink", R.string.UsernameHelpLink, url);
////                    int index = text.indexOf(url);
////                    SpannableStringBuilder textSpan = new SpannableStringBuilder(text);
////                    if (index >= 0) {
////                        textSpan.setSpan(new LinkSpan(url), index, index + url.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
////                    }
////                    helpTextView.setText(TextUtils.concat(infoText, "\n\n", textSpan));
////                } else {
////                    helpTextView.setText(infoText);
////                }
////            }
////        });
////
////        linearLayout3.addView(firstNameField, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 36, 24, 24, 24, 0));
////
////        checkTextView = new TextView(context);
////        checkTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
////        checkTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
////        linearLayout3.addView(checkTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 24, 12, 24, 0));
////
////        helpTextView = new TextView(context);
////        helpTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
////        helpTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText8));
////        helpTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
////        helpTextView.setText(infoText = AndroidUtilities.replaceTags(LocaleController.getString("UsernameHelp", R.string.UsernameHelp)));
////        helpTextView.setLinkTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteLinkText));
////        helpTextView.setHighlightColor(Theme.getColor(Theme.key_windowBackgroundWhiteLinkSelection));
////        helpTextView.setMovementMethod(new LinkMovementMethodMy());
////        linearLayout3.addView(helpTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 24, 10, 24, 0));
////
////        checkTextView.setVisibility(View.GONE);
////
////        if (user != null && user.username != null && user.username.length() > 0) {
////            ignoreCheck = true;
////            firstNameField.setText(user.username);
////            firstNameField.setSelection(firstNameField.length());
////            ignoreCheck = false;
////        }
////
////
////    }
////
////    @Override
////    public boolean isSwipeBackEnabled(MotionEvent event) {
////        return false;
////    }
////
////    private boolean checkUserName(final String name, boolean alert) {
////        if (name != null && name.length() > 0) {
////            checkTextView.setVisibility(View.VISIBLE);
////        } else {
////            checkTextView.setVisibility(View.GONE);
////        }
////        if (alert && name.length() == 0) {
////            return true;
////        }
////        if (checkRunnable != null) {
////            AndroidUtilities.cancelRunOnUIThread(checkRunnable);
////            checkRunnable = null;
////            lastCheckName = null;
////            if (checkReqId != 0) {
////                ConnectionsManager.getInstance(currentAccount).cancelRequest(checkReqId, true);
////            }
////        }
////        lastNameAvailable = false;
////        if (name != null) {
////            if (name.startsWith("_") || name.endsWith("_")) {
////                checkTextView.setText(LocaleController.getString("UsernameInvalid", R.string.UsernameInvalid));
////                checkTextView.setTag(Theme.key_windowBackgroundWhiteRedText4);
////                checkTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteRedText4));
////                return false;
////            }
////            for (int a = 0; a < name.length(); a++) {
////                char ch = name.charAt(a);
////                if (a == 0 && ch >= '0' && ch <= '9') {
////                    if (alert) {
////                        AlertsCreator.showSimpleAlert(this, LocaleController.getString("UsernameInvalidStartNumber", R.string.UsernameInvalidStartNumber));
////                    } else {
////                        checkTextView.setText(LocaleController.getString("UsernameInvalidStartNumber", R.string.UsernameInvalidStartNumber));
////                        checkTextView.setTag(Theme.key_windowBackgroundWhiteRedText4);
////                        checkTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteRedText4));
////                    }
////                    return false;
////                }
////                if (!(ch >= '0' && ch <= '9' || ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch == '_')) {
////                    if (alert) {
////                        AlertsCreator.showSimpleAlert(this, LocaleController.getString("UsernameInvalid", R.string.UsernameInvalid));
////                    } else {
////                        checkTextView.setText(LocaleController.getString("UsernameInvalid", R.string.UsernameInvalid));
////                        checkTextView.setTag(Theme.key_windowBackgroundWhiteRedText4);
////                        checkTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteRedText4));
////                    }
////                    return false;
////                }
////            }
////        }
////        if (name == null || name.length() < 5) {
////            if (alert) {
////                AlertsCreator.showSimpleAlert(this, LocaleController.getString("UsernameInvalidShort", R.string.UsernameInvalidShort));
////            } else {
////                checkTextView.setText(LocaleController.getString("UsernameInvalidShort", R.string.UsernameInvalidShort));
////                checkTextView.setTag(Theme.key_windowBackgroundWhiteRedText4);
////                checkTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteRedText4));
////            }
////            return false;
////        }
////        if (name.length() > 32) {
////            if (alert) {
////                AlertsCreator.showSimpleAlert(this, LocaleController.getString("UsernameInvalidLong", R.string.UsernameInvalidLong));
////            } else {
////                checkTextView.setText(LocaleController.getString("UsernameInvalidLong", R.string.UsernameInvalidLong));
////                checkTextView.setTag(Theme.key_windowBackgroundWhiteRedText4);
////                checkTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteRedText4));
////            }
////            return false;
////        }
////
////        if (!alert) {
////            String currentName = UserConfig.getInstance(currentAccount).getCurrentUser().username;
////            if (currentName == null) {
////                currentName = "";
////            }
////            if (name.equals(currentName)) {
////                checkTextView.setText(LocaleController.formatString("UsernameAvailable", R.string.UsernameAvailable, name));
////                checkTextView.setTag(Theme.key_windowBackgroundWhiteGreenText);
////                checkTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGreenText));
////                return true;
////            }
////
////
////            checkTextView.setText(LocaleController.getString("UsernameChecking", R.string.UsernameChecking));
////            checkTextView.setTag(Theme.key_windowBackgroundWhiteGrayText8);
////            checkTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText8));
////            lastCheckName = name;
////            checkRunnable = () -> {
////                TLRPC.TL_account_checkUsername req = new TLRPC.TL_account_checkUsername();
////                req.username = name;
////                checkReqId = ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
////                    checkReqId = 0;
////                    if (lastCheckName != null && lastCheckName.equals(name)) {
////                        if (error == null && response instanceof TLRPC.TL_boolTrue) {
////                            checkTextView.setText(LocaleController.formatString("UsernameAvailable", R.string.UsernameAvailable, name));
////                            checkTextView.setTag(Theme.key_windowBackgroundWhiteGreenText);
////                            checkTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGreenText));
////                            lastNameAvailable = true;
////                        } else {
////                            checkTextView.setText(LocaleController.getString("UsernameInUse", R.string.UsernameInUse));
////                            checkTextView.setTag(Theme.key_windowBackgroundWhiteRedText4);
////                            checkTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteRedText4));
////                            lastNameAvailable = false;
////                        }
////                    }
////                }), ConnectionsManager.RequestFlagFailOnServerErrors);
////            };
////            AndroidUtilities.runOnUIThread(checkRunnable, 300);
////        }
////        return true;
////    }
////
////    private void saveName() {
////
////        if (!checkUserName(firstNameField.getText().toString(), true)) {
////            return;
////        }
////        TLRPC.User user = UserConfig.getInstance(currentAccount).getCurrentUser();
////        if (getParentActivity() == null || user == null) {
////            return;
////        }
////        String currentName = user.username;
////        if (currentName == null) {
////            currentName = "";
////        }
////        String newName = firstNameField.getText().toString();
////        if (currentName.equals(newName)) {
////            finishFragment();
////            return;
////        }
////
////        final AlertDialog progressDialog = new AlertDialog(getParentActivity(), 3);
////
////        final TLRPC.TL_account_updateUsername req = new TLRPC.TL_account_updateUsername();
////        req.username = newName;
////
////        NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.updateInterfaces, MessagesController.UPDATE_MASK_NAME);
////        final int reqId = ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> {
////            if (error == null) {
////                final TLRPC.User user1 = (TLRPC.User)response;
////                AndroidUtilities.runOnUIThread(() -> {
////                    try {
////                        progressDialog.dismiss();
////                    } catch (Exception e) {
////                        FileLog.e(e);
////                    }
////                    ArrayList<TLRPC.User> users = new ArrayList<>();
////                    users.add(user1);
////                    MessagesController.getInstance(currentAccount).putUsers(users, false);
////                    MessagesStorage.getInstance(currentAccount).putUsersAndChats(users, null, false, true);
////                    UserConfig.getInstance(currentAccount).saveConfig(true);
////
////                    finishFragment();
////                });
////            } else {
////                AndroidUtilities.runOnUIThread(() -> {
////                    try {
////                        progressDialog.dismiss();
////                    } catch (Exception e) {
////                        FileLog.e(e);
////                    }
////                    AlertsCreator.processError(currentAccount, error, ShopCreateFragment.this, req);
////                });
////            }
////        }, ConnectionsManager.RequestFlagFailOnServerErrors);
////        ConnectionsManager.getInstance(currentAccount).bindRequestToGuid(reqId, classGuid);
////
////        progressDialog.setOnCancelListener(dialog -> ConnectionsManager.getInstance(currentAccount).cancelRequest(reqId, true));
////        progressDialog.show();
////    }
////
////
////    @Override
////    public void onTransitionAnimationEnd(boolean isOpen, boolean backward) {
////        if (isOpen && currentStep == 2) {
////            firstNameField.requestFocus();
////            AndroidUtilities.showKeyboard(firstNameField);
////        }
////    }
////
////
////    private boolean isAllRequiredFieldFilled() {
////        if (currentStep == 0) {
////            int row = -1;
////
////            if(currentLocation == null){
////                row = locationRow;
////            }
////            if (row == -1) {
////                return true;
////            }
////
////            RecyclerView.ViewHolder holder = listView.findViewHolderForAdapterPosition(row);
////            if (holder != null) {
////                Vibrator v = (Vibrator) getParentActivity().getSystemService(Context.VIBRATOR_SERVICE);
////                if (v != null) {
////                    v.vibrate(200);
////                }
////                AndroidUtilities.shakeView(holder.itemView, 2, 0);
////            }
////            return false;
////        }else if(currentStep == 1){
////            if(!useCurrentValue && TextUtils.isEmpty(inputFields[FIELD_PHONE].getText())){
////                if (inputFields[FIELD_PHONE] != null) {
////                    Vibrator v = (Vibrator) getParentActivity().getSystemService(Context.VIBRATOR_SERVICE);
////                    if (v != null) {
////                        v.vibrate(200);
////                    }
////                    AndroidUtilities.shakeView(inputFields[FIELD_PHONE], 2, 0);
////                }
////
////            }else{
////                return true;
////            }
////        }
////        return false;
////    }
////
////
////    @Override
////    public void didReceivedNotification(int id, int account, Object... args) {
////        if (id == NotificationCenter.didShopCreated) {
////            if (progressDialog != null) {
////                try {
////                    progressDialog.dismiss();
////                } catch (Exception e) {
////                    FileLog.e(e);
////                }
////            }
////            Boolean created = (Boolean)args[0];
////            if(created){
////                long chat_id = (long) args[1];
////                Bundle bundle = new Bundle();
////                bundle.putInt("step", 1);
////                bundle.putInt("chat_id", ShopUtils.toClientChannelId(chat_id));
////                presentFragment(new ShopCreateFragment(bundle), true);
////            }else{
////                donePressed = false;
////                APIError apiError = (APIError)args[1];
////                showErrorAlert(apiError);
////            }
////        }else if(id == NotificationCenter.didShopUpdated){
////            if (progressDialog != null) {
////                try {
////                    progressDialog.dismiss();
////                } catch (Exception e) {
////                    FileLog.e(e);
////                }
////            }
////            Boolean updated = (Boolean)args[0];
////            long chat_id = (long) args[1];
////            if(updated){
////                if(currentStep == 1){
////                    Bundle bundle = new Bundle();
////                    bundle.putInt("step", 2);
////                    bundle.putInt("chat_id", ShopUtils.toClientChannelId(chat_id));
////                    presentFragment(new ShopCreateFragment(bundle), true);
////                }else if(currentStep == 2){
////                    Bundle bundle = new Bundle();
////                    bundle.putInt("chat_id",ShopUtils.toClientChannelId(chat_id));
////                    presentFragment(new BusinessProfileActivity(bundle),true);
////                }
////            }else{
////                donePressed = false;
////                APIError apiError = (APIError)args[1];
////                apiError.setMessage("error updating shop!");
////                showErrorAlert(apiError);
////            }
////        }
////    }
////
////    private void showErrorAlert(APIError error) {
////        if (getParentActivity() == null) {
////            return;
////        }
////        if(error != null && TextUtils.isEmpty(error.message())){
////            error.setMessage("Unknown error, try again!");
////        }
////        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
////        builder.setTitle(LocaleController.getString("AppNameHulu", R.string.AppNameHulu));
////        builder.setMessage(error.message());
////        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
////        showDialog(builder.create());
////    }
////
////
////    @Override
////    public void onResume() {
////        super.onResume();
////        if (adapter != null) {
////            adapter.notifyDataSetChanged();
////        }
////
////        if(currentStep == 2){
////            SharedPreferences preferences = MessagesController.getGlobalMainSettings();
////            boolean animations = preferences.getBoolean("view_animations", true);
////            if (!animations) {
////                firstNameField.requestFocus();
////                AndroidUtilities.showKeyboard(firstNameField);
////            }
////        }
////
////    }
////
////    private void requestFocusForAddressInput(){
////        RecyclerView.ViewHolder holder = listView.findViewHolderForAdapterPosition(addressRow);
////        if(holder != null){
////            if(holder.itemView instanceof PollEditTextCell){
////                PollEditTextCell cell = (PollEditTextCell) holder.itemView;
////                cell.getTextView().requestFocus();
////                AndroidUtilities.showKeyboard(cell.getTextView());
////            }
////        }
////    }
////
////    private  class ListAdapter extends RecyclerListView.SelectionAdapter{
////
////        private Context mContext;
////
////        public ListAdapter(Context mContext) {
////            this.mContext = mContext;
////        }
////
////        @Override
////        public boolean isEnabled(RecyclerView.ViewHolder holder) {
////            int type = holder.getItemViewType();
////            return type != 0 &&  type != 1 && type != 4 && type != 8;
////        }
////
////
////        @Override
////        public int getItemViewType(int position) {
////            if(position == channelRow){
////                return 0;
////            }else if(position ==  addressSecRow || position == locationSecRow || position == citySecRow){
////                return 1;
////            }else if(position == addressHeaderRow){
////                return 4;
////            }else if(position == locationRow || position == cityRow){
////                return 5;
////            }else if(position == addressRow){
////                return 7;
////            }else if(position == bottomPaddingRow){
////                return 8;
////            }
////            return super.getItemViewType(position);
////        }
////
////
////        @NonNull
////        @Override
////        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
////            View view;
////            switch (viewType){
////                case 0:
////                     view  = new HintInnerCell(mContext);
////                     view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
////                    break;
////                case 1:
////                    view = new ShadowSectionCell(mContext);
////                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
////                    break;
////                case 2:
////                    view = new SimpleImageTextCell(mContext, 8);
////                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
////                    break;
////                case 3:
////                    view = new TextInfoPrivacyCell(mContext);
////                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
////                    break;
////                case 4:
////                    view = new HeaderCell(mContext,Theme.key_dialogTextBlack,21);
////                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
////                    break;
////                case 5:
////                    view = new SendLocationCell(mContext,false);
////                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
////                    break;
////                case 9:
////                    view = new TextSettingsCell(mContext);
////                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
////                    break;
////                case 7:
////                    PollEditTextCell cell = new PollEditTextCell(mContext, null);
////                    pollEditTextCell = cell;
////                    cell.createErrorTextView();
////                    cell.setTextAndHint("","Edna mall,3rd floor,number 3",true);
////                    cell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
////                    cell.addTextWatcher(new TextWatcher() {
////                        @Override
////                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
////
////                        }
////
////                        @Override
////                        public void onTextChanged(CharSequence s, int start, int before, int count) {
////
////                        }
////
////                        @Override
////                        public void afterTextChanged(Editable s) {
////                            if (cell.getTag() != null) {
////                                return;
////                            }
////                            address =  s.toString();
////                        }
////                    });
////                    EditTextBoldCursor editText = cell.getTextView();
////                    cell.setShowNextButton(true);
////                    editText.setOnFocusChangeListener((v, hasFocus) -> cell.getTextView2().setAlpha(1f));
////                    editText.setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
////                    editText.setOnKeyListener((view1, i, keyEvent) -> false);
////                    view = cell;
////                    break;
////                case 8:
////                default:
////                    view = new EmptyCell(mContext,100);
////                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
////                    break;
////
////            }
////            view.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
////            return new RecyclerListView.Holder(view);
////        }
////
////
////        @Override
////        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
////            switch (holder.getItemViewType()){
////                case 2:{
////                    SimpleImageTextCell cell = (SimpleImageTextCell)holder.itemView;
////                    break;
////                }
////                case 3:{
////                    TextInfoPrivacyCell cell = (TextInfoPrivacyCell)holder.itemView;
////                    break;
////                }
////                case 4:{
////                    HeaderCell cell = (HeaderCell)holder.itemView;
////                    if(position == addressHeaderRow){
////                        cell.setText(LocaleController.getString("ShopAddresss",R.string.ShopAddresss));
////                    }
////                    break;
////                }
////                case 5:{
////                    SendLocationCell cell = (SendLocationCell)holder.itemView;
////                    if(position == locationRow){
////                        cell.setText("Location",TextUtils.isEmpty(gpsAddress)?"Set your shop GPS location":gpsAddress);
////                    }else if(position == cityRow){
////                        cell.setText("City",TextUtils.isEmpty(city)?"Set your city":city);
////                        cell.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_city));
////                    }
////                    break;
////                }
////            }
////        }
////
////        @Override
////        public int getItemCount() {
////            return rowCount;
////        }
////    }
////
////
////    public void addBotToAdminList(TLRPC.User bot,boolean first){
////        TLRPC.TL_chatAdminRights rights = new TLRPC.TL_chatAdminRights();
////        rights.invite_users = true;
////        TLRPC.TL_channels_editAdmin req = new TLRPC.TL_channels_editAdmin();
////        req.channel = MessagesController.getInputChannel(currentChat);
////        req.user_id = getMessagesController().getInputUser(bot);
////        req.admin_rights = rights;
////        req.rank = LocaleController.getString("ChannelAdmin", R.string.ChannelAdmin);;
////        getConnectionsManager().sendRequest(req, (response, error) -> {
////            if (error == null) {
////                getMessagesController().processUpdates((TLRPC.Updates) response, false);
////                AndroidUtilities.runOnUIThread(() -> getMessagesController().loadFullChat(chatId, 0, true), 1000);
////                AndroidUtilities.runOnUIThread(() -> {
////                    isBotAdmin = true;
////                    if(!first)
////                      processDone();
////                });
////            } else {
////                Log.i(TAG,"error bot = " + error.text + " codde: "+ error.code);
////                AndroidUtilities.runOnUIThread(() -> {
////                    isBotAdmin = false;
////                });
////            }
////        });
////
////    }
////
////    private final static int FIELD_PHONECOUNTRY = 0;
////    private final static int FIELD_PHONECODE = 1;
////    private final static int FIELD_PHONE = 2;
////    private boolean ignoreOnTextChange;
////    private boolean ignoreOnPhoneChange;
////    private TextView plusTextView;
////    private ArrayList<View> dividers = new ArrayList<>();
////    private LinearLayout linearLayout2;
////    private boolean useCurrentValue = true;
////    private String currentPhone;
////
////    private ArrayList<String> countriesArray = new ArrayList<>();
////    private HashMap<String, String> countriesMap = new HashMap<>();
////    private HashMap<String, String> codesMap = new HashMap<>();
////    private HashMap<String, String> phoneFormatMap = new HashMap<>();
////    private HashMap<String, String> languageMap;
////    private TextCheckCell textSettingsCell;
////    EditTextBoldCursor[] inputFields;
////
////
////    private void createPhoneInterface(Context context) {
////
////        linearLayout2 = new LinearLayout(context);
////        linearLayout2.setOrientation(LinearLayout.VERTICAL);
////        TextInfoPrivacyCell bottomCell;
////        HeaderCell headerCell;
////
////        actionBar.setTitle(LocaleController.getString("PassportPhone", R.string.PassportPhone));
////
////
////        languageMap = new HashMap<>();
////        try {
////            BufferedReader reader = new BufferedReader(new InputStreamReader(context.getResources().getAssets().open("countries.txt")));
////            String line;
////            while ((line = reader.readLine()) != null) {
////                String[] args = line.split(";");
////                countriesArray.add(0, args[2]);
////                countriesMap.put(args[2], args[0]);
////                codesMap.put(args[0], args[2]);
////                if (args.length > 3) {
////                    phoneFormatMap.put(args[0], args[3]);
////                }
////                languageMap.put(args[1], args[2]);
////            }
////            reader.close();
////        } catch (Exception e) {
////            FileLog.e(e);
////        }
////
////        Collections.sort(countriesArray, String::compareTo);
////        String currentPhone = UserConfig.getInstance(currentAccount).getCurrentUser().phone;
////
////
////        textSettingsCell = new TextCheckCell(context);
////        textSettingsCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
////        textSettingsCell.setTextAndCheck("Use " + PhoneFormat.getInstance().format("+" + currentPhone),useCurrentValue,true);
////        textSettingsCell.setOnClickListener(v -> {
////            useCurrentValue = !useCurrentValue;
////            ((TextCheckCell) v).setChecked(useCurrentValue);
////        });
////        linearLayout2.addView(textSettingsCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
////
////
////
////
////        bottomCell = new TextInfoPrivacyCell(context);
////        bottomCell.setBackgroundDrawable(Theme.getThemedDrawable(context, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
////        bottomCell.setText(LocaleController.getString("PassportPhoneUseSameInfo", R.string.PassportPhoneUseSameInfo));
////        linearLayout2.addView(bottomCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
////
////        headerCell = new HeaderCell(context);
////        headerCell.setText("or add new  phone number");
////        headerCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
////        linearLayout2.addView(headerCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
////
////        inputFields = new EditTextBoldCursor[3];
////        for (int a = 0; a < 3; a++) {
////
////            if (a == FIELD_PHONE) {
////                inputFields[a] = new HintEditText(context);
////            } else {
////                inputFields[a] = new EditTextBoldCursor(context);
////            }
////
////            ViewGroup container;
////            if (a == FIELD_PHONECODE) {
////                container = new LinearLayout(context);
////                ((LinearLayout) container).setOrientation(LinearLayout.HORIZONTAL);
////                linearLayout2.addView(container, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 50));
////                container.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
////            } else if (a == FIELD_PHONE) {
////                container = (ViewGroup) inputFields[FIELD_PHONECODE].getParent();
////            } else {
////                container = new FrameLayout(context);
////                linearLayout2.addView(container, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 50));
////                container.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
////            }
////
////            inputFields[a].setTag(a);
////            inputFields[a].setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
////            inputFields[a].setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
////            inputFields[a].setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
////            inputFields[a].setBackgroundDrawable(null);
////            inputFields[a].setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
////            inputFields[a].setCursorSize(AndroidUtilities.dp(20));
////            inputFields[a].setCursorWidth(1.5f);
////            if (a == FIELD_PHONECOUNTRY) {
////                inputFields[a].setOnTouchListener((v, event) -> {
////                    if (getParentActivity() == null) {
////                        return false;
////                    }
////                    if (event.getAction() == MotionEvent.ACTION_UP) {
////                        CountrySelectActivity fragment = new CountrySelectActivity(false);
////                        fragment.setCountrySelectActivityDelegate((name, shortName) -> {
////                            inputFields[FIELD_PHONECOUNTRY].setText(name);
////                            int index = countriesArray.indexOf(name);
////                            if (index != -1) {
////                                ignoreOnTextChange = true;
////                                String code = countriesMap.get(name);
////                                inputFields[FIELD_PHONECODE].setText(code);
////                                String hint = phoneFormatMap.get(code);
////                                inputFields[FIELD_PHONE].setHintText(hint != null ? hint.replace('X', '–') : null);
////                                ignoreOnTextChange = false;
////                            }
////                            AndroidUtilities.runOnUIThread(() -> AndroidUtilities.showKeyboard(inputFields[FIELD_PHONE]), 300);
////                            inputFields[FIELD_PHONE].requestFocus();
////                            inputFields[FIELD_PHONE].setSelection(inputFields[FIELD_PHONE].length());
////                        });
////                        presentFragment(fragment);
////                    }
////                    return true;
////                });
////                inputFields[a].setText(LocaleController.getString("ChooseCountry", R.string.ChooseCountry));
////                inputFields[a].setInputType(0);
////                inputFields[a].setFocusable(false);
////            } else {
////                inputFields[a].setInputType(InputType.TYPE_CLASS_PHONE);
////                if (a == FIELD_PHONE) {
////                    inputFields[a].setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
////                } else {
////                    inputFields[a].setImeOptions(EditorInfo.IME_ACTION_NEXT | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
////                }
////            }
////            inputFields[a].setSelection(inputFields[a].length());
////
////            if (a == FIELD_PHONECODE) {
////                plusTextView = new TextView(context);
////                plusTextView.setText("+");
////                plusTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
////                plusTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
////                container.addView(plusTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 21, 12, 0, 6));
////
////                inputFields[a].setPadding(AndroidUtilities.dp(10), 0, 0, 0);
////                InputFilter[] inputFilters = new InputFilter[1];
////                inputFilters[0] = new InputFilter.LengthFilter(5);
////                inputFields[a].setFilters(inputFilters);
////                inputFields[a].setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
////                container.addView(inputFields[a], LayoutHelper.createLinear(55, LayoutHelper.WRAP_CONTENT, 0, 12, 16, 6));
////                inputFields[a].addTextChangedListener(new TextWatcher() {
////                    @Override
////                    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
////
////                    }
////
////                    @Override
////                    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
////
////                    }
////
////                    @Override
////                    public void afterTextChanged(Editable editable) {
////                        if (ignoreOnTextChange) {
////                            return;
////                        }
////                        ignoreOnTextChange = true;
////                        String text = PhoneFormat.stripExceptNumbers(inputFields[FIELD_PHONECODE].getText().toString());
////                        inputFields[FIELD_PHONECODE].setText(text);
////                        HintEditText phoneField = (HintEditText) inputFields[FIELD_PHONE];
////                        if (text.length() == 0) {
////                            phoneField.setHintText(null);
////                            phoneField.setHint(LocaleController.getString("PaymentShippingPhoneNumber", R.string.PaymentShippingPhoneNumber));
////                            inputFields[FIELD_PHONECOUNTRY].setText(LocaleController.getString("ChooseCountry", R.string.ChooseCountry));
////                        } else {
////                            String country;
////                            boolean ok = false;
////                            String textToSet = null;
////                            if (text.length() > 4) {
////                                for (int a = 4; a >= 1; a--) {
////                                    String sub = text.substring(0, a);
////                                    country = codesMap.get(sub);
////                                    if (country != null) {
////                                        ok = true;
////                                        textToSet = text.substring(a) + inputFields[FIELD_PHONE].getText().toString();
////                                        inputFields[FIELD_PHONECODE].setText(text = sub);
////                                        break;
////                                    }
////                                }
////                                if (!ok) {
////                                    textToSet = text.substring(1) + inputFields[FIELD_PHONE].getText().toString();
////                                    inputFields[FIELD_PHONECODE].setText(text = text.substring(0, 1));
////                                }
////                            }
////                            country = codesMap.get(text);
////                            boolean set = false;
////                            if (country != null) {
////                                int index = countriesArray.indexOf(country);
////                                if (index != -1) {
////                                    inputFields[FIELD_PHONECOUNTRY].setText(countriesArray.get(index));
////                                    String hint = phoneFormatMap.get(text);
////                                    set = true;
////                                    if (hint != null) {
////                                        phoneField.setHintText(hint.replace('X', '–'));
////                                        phoneField.setHint(null);
////                                    }
////                                }
////                            }
////                            if (!set) {
////                                phoneField.setHintText(null);
////                                phoneField.setHint(LocaleController.getString("PaymentShippingPhoneNumber", R.string.PaymentShippingPhoneNumber));
////                                inputFields[FIELD_PHONECOUNTRY].setText(LocaleController.getString("WrongCountry", R.string.WrongCountry));
////                            }
////                            if (!ok) {
////                                inputFields[FIELD_PHONECODE].setSelection(inputFields[FIELD_PHONECODE].getText().length());
////                            }
////
////                            if (textToSet != null) {
////                                phoneField.requestFocus();
////                                phoneField.setText(textToSet);
////                                phoneField.setSelection(phoneField.length());
////
////                            }
////                        }
////                        ignoreOnTextChange = false;
////                    }
////                });
////            } else if (a == FIELD_PHONE) {
////                inputFields[a].setPadding(0, 0, 0, 0);
////                inputFields[a].setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
////                inputFields[a].setHintText(null);
////                inputFields[a].setHint(LocaleController.getString("PaymentShippingPhoneNumber", R.string.PaymentShippingPhoneNumber));
////                container.addView(inputFields[a], LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 12, 21, 6));
////                inputFields[a].addTextChangedListener(new TextWatcher() {
////                    private int characterAction = -1;
////                    private int actionPosition;
////
////                    @Override
////                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
////                        if (count == 0 && after == 1) {
////                            characterAction = 1;
////                        } else if (count == 1 && after == 0) {
////                            if (s.charAt(start) == ' ' && start > 0) {
////                                characterAction = 3;
////                                actionPosition = start - 1;
////                            } else {
////                                characterAction = 2;
////                            }
////                        } else {
////                            characterAction = -1;
////                        }
////                    }
////
////                    @Override
////                    public void onTextChanged(CharSequence s, int start, int before, int count) {
////
////                    }
////
////                    @Override
////                    public void afterTextChanged(Editable s) {
////                        if (ignoreOnPhoneChange) {
////                            return;
////                        }
////                        HintEditText phoneField = (HintEditText) inputFields[FIELD_PHONE];
////                        int start = phoneField.getSelectionStart();
////                        String phoneChars = "0123456789";
////                        String str = phoneField.getText().toString();
////                        if (characterAction == 3) {
////                            str = str.substring(0, actionPosition) + str.substring(actionPosition + 1);
////                            start--;
////                        }
////                        StringBuilder builder = new StringBuilder(str.length());
////                        for (int a = 0; a < str.length(); a++) {
////                            String ch = str.substring(a, a + 1);
////                            if (phoneChars.contains(ch)) {
////                                builder.append(ch);
////                            }
////                        }
////                        ignoreOnPhoneChange = true;
////                        String hint = phoneField.getHintText();
////                        if (hint != null) {
////                            for (int a = 0; a < builder.length(); a++) {
////                                if (a < hint.length()) {
////                                    if (hint.charAt(a) == ' ') {
////                                        builder.insert(a, ' ');
////                                        a++;
////                                        if (start == a && characterAction != 2 && characterAction != 3) {
////                                            start++;
////                                        }
////                                    }
////                                } else {
////                                    builder.insert(a, ' ');
////                                    if (start == a + 1 && characterAction != 2 && characterAction != 3) {
////                                        start++;
////                                    }
////                                    break;
////                                }
////                            }
////                        }
////                        phoneField.setText(builder);
////                        if (start >= 0) {
////                            phoneField.setSelection(Math.min(start, phoneField.length()));
////                        }
////                        phoneField.onTextChange();
////                        ignoreOnPhoneChange = false;
////                    }
////                });
////            } else {
////                inputFields[a].setPadding(0, 0, 0, AndroidUtilities.dp(6));
////                inputFields[a].setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
////                container.addView(inputFields[a], LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 21, 12, 21, 6));
////            }
////            inputFields[a].setOnEditorActionListener((textView, i, keyEvent) -> {
////                if (i == EditorInfo.IME_ACTION_NEXT) {
////                    inputFields[FIELD_PHONE].requestFocus();
////                    return true;
////                } else if (i == EditorInfo.IME_ACTION_DONE) {
////                    doneButton.callOnClick();
////                    return true;
////                }
////                return false;
////            });
////            if (a == FIELD_PHONE) {
////                inputFields[a].setOnKeyListener((v, keyCode, event) -> {
////                    if (keyCode == KeyEvent.KEYCODE_DEL && inputFields[FIELD_PHONE].length() == 0) {
////                        inputFields[FIELD_PHONECODE].requestFocus();
////                        inputFields[FIELD_PHONECODE].setSelection(inputFields[FIELD_PHONECODE].length());
////                        inputFields[FIELD_PHONECODE].dispatchKeyEvent(event);
////                        return true;
////                    }
////                    return false;
////                });
////            }
////
////            if (a == FIELD_PHONECOUNTRY) {
////                View divider = new View(context);
////                dividers.add(divider);
////                divider.setBackgroundColor(Theme.getColor(Theme.key_divider));
////                container.addView(divider, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1, Gravity.LEFT | Gravity.BOTTOM));
////            }
////        }
////
////        String country = null;
////        try {
////            TelephonyManager telephonyManager = (TelephonyManager) ApplicationLoader.applicationContext.getSystemService(Context.TELEPHONY_SERVICE);
////            if (telephonyManager != null) {
////                country = telephonyManager.getSimCountryIso().toUpperCase();
////            }
////        } catch (Exception e) {
////            FileLog.e(e);
////        }
////        if (country != null) {
////            String countryName = languageMap.get(country);
////            if (countryName != null) {
////                int index = countriesArray.indexOf(countryName);
////                if (index != -1) {
////                    inputFields[FIELD_PHONECODE].setText(countriesMap.get(countryName));
////                }
////            }
////        }
////
////    }
////
////}BotAdmin = true;
//                    if(!first)
//                      processDone();
//                });
//            } else {
//                Log.i(TAG,"error bot = " + error.text + " codde: "+ error.code);
//                AndroidUtilities.runOnUIThread(() -> {
//                    isBotAdmin = false;
//                });
//            }
//        });
//
//    }
//
//    private final static int FIELD_PHONECOUNTRY = 0;
//    private final static int FIELD_PHONECODE = 1;
//    private final static int FIELD_PHONE = 2;
//    private boolean ignoreOnTextChange;
//    private boolean ignoreOnPhoneChange;
//    private TextView plusTextView;
//    private ArrayList<View> dividers = new ArrayList<>();
//    private LinearLayout linearLayout2;
//    private boolean useCurrentValue = true;
//    private String currentPhone;
//
//    private ArrayList<String> countriesArray = new ArrayList<>();
//    private HashMap<String, String> countriesMap = new HashMap<>();
//    private HashMap<String, String> codesMap = new HashMap<>();
//    private HashMap<String, String> phoneFormatMap = new HashMap<>();
//    private HashMap<String, String> languageMap;
//    private TextCheckCell textSettingsCell;
//    EditTextBoldCursor[] inputFields;
//
//
//    private void createPhoneInterface(Context context) {
//
//        linearLayout2 = new LinearLayout(context);
//        linearLayout2.setOrientation(LinearLayout.VERTICAL);
//        TextInfoPrivacyCell bottomCell;
//        HeaderCell headerCell;
//
//        actionBar.setTitle(LocaleController.getString("PassportPhone", R.string.PassportPhone));
//
//
//        languageMap = new HashMap<>();
//        try {
//            BufferedReader reader = new BufferedReader(new InputStreamReader(context.getResources().getAssets().open("countries.txt")));
//            String line;
//            while ((line = reader.readLine()) != null) {
//                String[] args = line.split(";");
//                countriesArray.add(0, args[2]);
//                countriesMap.put(args[2], args[0]);
//                codesMap.put(args[0], args[2]);
//                if (args.length > 3) {
//                    phoneFormatMap.put(args[0], args[3]);
//                }
//                languageMap.put(args[1], args[2]);
//            }
//            reader.close();
//        } catch (Exception e) {
//            FileLog.e(e);
//        }
//
//        Collections.sort(countriesArray, String::compareTo);
//        String currentPhone = UserConfig.getInstance(currentAccount).getCurrentUser().phone;
//
//
//        textSettingsCell = new TextCheckCell(context);
//        textSettingsCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
//        textSettingsCell.setTextAndCheck("Use " + PhoneFormat.getInstance().format("+" + currentPhone),useCurrentValue,true);
//        textSettingsCell.setOnClickListener(v -> {
//            useCurrentValue = !useCurrentValue;
//            ((TextCheckCell) v).setChecked(useCurrentValue);
//        });
//        linearLayout2.addView(textSettingsCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
//
//
//
//
//        bottomCell = new TextInfoPrivacyCell(context);
//        bottomCell.setBackgroundDrawable(Theme.getThemedDrawable(context, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
//        bottomCell.setText(LocaleController.getString("PassportPhoneUseSameInfo", R.string.PassportPhoneUseSameInfo));
//        linearLayout2.addView(bottomCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
//
//        headerCell = new HeaderCell(context);
//        headerCell.setText("or add new  phone number");
//        headerCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
//        linearLayout2.addView(headerCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
//
//        inputFields = new EditTextBoldCursor[3];
//        for (int a = 0; a < 3; a++) {
//
//            if (a == FIELD_PHONE) {
//                inputFields[a] = new HintEditText(context);
//            } else {
//                inputFields[a] = new EditTextBoldCursor(context);
//            }
//
//            ViewGroup container;
//            if (a == FIELD_PHONECODE) {
//                container = new LinearLayout(context);
//                ((LinearLayout) container).setOrientation(LinearLayout.HORIZONTAL);
//                linearLayout2.addView(container, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 50));
//                container.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
//            } else if (a == FIELD_PHONE) {
//                container = (ViewGroup) inputFields[FIELD_PHONECODE].getParent();
//            } else {
//                container = new FrameLayout(context);
//                linearLayout2.addView(container, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 50));
//                container.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
//            }
//
//            inputFields[a].setTag(a);
//            inputFields[a].setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
//            inputFields[a].setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
//            inputFields[a].setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
//            inputFields[a].setBackgroundDrawable(null);
//            inputFields[a].setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
//            inputFields[a].setCursorSize(AndroidUtilities.dp(20));
//            inputFields[a].setCursorWidth(1.5f);
//            if (a == FIELD_PHONECOUNTRY) {
//                inputFields[a].setOnTouchListener((v, event) -> {
//                    if (getParentActivity() == null) {
//                        return false;
//                    }
//                    if (event.getAction() == MotionEvent.ACTION_UP) {
//                        CountrySelectActivity fragment = new CountrySelectActivity(false);
//                        fragment.setCountrySelectActivityDelegate((name, shortName) -> {
//                            inputFields[FIELD_PHONECOUNTRY].setText(name);
//                            int index = countriesArray.indexOf(name);
//                            if (index != -1) {
//                                ignoreOnTextChange = true;
//                                String code = countriesMap.get(name);
//                                inputFields[FIELD_PHONECODE].setText(code);
//                                String hint = phoneFormatMap.get(code);
//                                inputFields[FIELD_PHONE].setHintText(hint != null ? hint.replace('X', '–') : null);
//                                ignoreOnTextChange = false;
//                            }
//                            AndroidUtilities.runOnUIThread(() -> AndroidUtilities.showKeyboard(inputFields[FIELD_PHONE]), 300);
//                            inputFields[FIELD_PHONE].requestFocus();
//                            inputFields[FIELD_PHONE].setSelection(inputFields[FIELD_PHONE].length());
//                        });
//                        presentFragment(fragment);
//                    }
//                    return true;
//                });
//                inputFields[a].setText(LocaleController.getString("ChooseCountry", R.string.ChooseCountry));
//                inputFields[a].setInputType(0);
//                inputFields[a].setFocusable(false);
//            } else {
//                inputFields[a].setInputType(InputType.TYPE_CLASS_PHONE);
//                if (a == FIELD_PHONE) {
//                    inputFields[a].setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
//                } else {
//                    inputFields[a].setImeOptions(EditorInfo.IME_ACTION_NEXT | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
//                }
//            }
//            inputFields[a].setSelection(inputFields[a].length());
//
//            if (a == FIELD_PHONECODE) {
//                plusTextView = new TextView(context);
//                plusTextView.setText("+");
//                plusTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
//                plusTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
//                container.addView(plusTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 21, 12, 0, 6));
//
//                inputFields[a].setPadding(AndroidUtilities.dp(10), 0, 0, 0);
//                InputFilter[] inputFilters = new InputFilter[1];
//                inputFilters[0] = new InputFilter.LengthFilter(5);
//                inputFields[a].setFilters(inputFilters);
//                inputFields[a].setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
//                container.addView(inputFields[a], LayoutHelper.createLinear(55, LayoutHelper.WRAP_CONTENT, 0, 12, 16, 6));
//                inputFields[a].addTextChangedListener(new TextWatcher() {
//                    @Override
//                    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
//
//                    }
//
//                    @Override
//                    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
//
//                    }
//
//                    @Override
//                    public void afterTextChanged(Editable editable) {
//                        if (ignoreOnTextChange) {
//                            return;
//                        }
//                        ignoreOnTextChange = true;
//                        String text = PhoneFormat.stripExceptNumbers(inputFields[FIELD_PHONECODE].getText().toString());
//                        inputFields[FIELD_PHONECODE].setText(text);
//                        HintEditText phoneField = (HintEditText) inputFields[FIELD_PHONE];
//                        if (text.length() == 0) {
//                            phoneField.setHintText(null);
//                            phoneField.setHint(LocaleController.getString("PaymentShippingPhoneNumber", R.string.PaymentShippingPhoneNumber));
//                            inputFields[FIELD_PHONECOUNTRY].setText(LocaleController.getString("ChooseCountry", R.string.ChooseCountry));
//                        } else {
//                            String country;
//                            boolean ok = false;
//                            String textToSet = null;
//                            if (text.length() > 4) {
//                                for (int a = 4; a >= 1; a--) {
//                                    String sub = text.substring(0, a);
//                                    country = codesMap.get(sub);
//                                    if (country != null) {
//                                        ok = true;
//                                        textToSet = text.substring(a) + inputFields[FIELD_PHONE].getText().toString();
//                                        inputFields[FIELD_PHONECODE].setText(text = sub);
//                                        break;
//                                    }
//                                }
//                                if (!ok) {
//                                    textToSet = text.substring(1) + inputFields[FIELD_PHONE].getText().toString();
//                                    inputFields[FIELD_PHONECODE].setText(text = text.substring(0, 1));
//                                }
//                            }
//                            country = codesMap.get(text);
//                            boolean set = false;
//                            if (country != null) {
//                                int index = countriesArray.indexOf(country);
//                                if (index != -1) {
//                                    inputFields[FIELD_PHONECOUNTRY].setText(countriesArray.get(index));
//                                    String hint = phoneFormatMap.get(text);
//                                    set = true;
//                                    if (hint != null) {
//                                        phoneField.setHintText(hint.replace('X', '–'));
//                                        phoneField.setHint(null);
//                                    }
//                                }
//                            }
//                            if (!set) {
//                                phoneField.setHintText(null);
//                                phoneField.setHint(LocaleController.getString("PaymentShippingPhoneNumber", R.string.PaymentShippingPhoneNumber));
//                                inputFields[FIELD_PHONECOUNTRY].setText(LocaleController.getString("WrongCountry", R.string.WrongCountry));
//                            }
//                            if (!ok) {
//                                inputFields[FIELD_PHONECODE].setSelection(inputFields[FIELD_PHONECODE].getText().length());
//                            }
//
//                            if (textToSet != null) {
//                                phoneField.requestFocus();
//                                phoneField.setText(textToSet);
//                                phoneField.setSelection(phoneField.length());
//
//                            }
//                        }
//                        ignoreOnTextChange = false;
//                    }
//                });
//            } else if (a == FIELD_PHONE) {
//                inputFields[a].setPadding(0, 0, 0, 0);
//                inputFields[a].setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
//                inputFields[a].setHintText(null);
//                inputFields[a].setHint(LocaleController.getString("PaymentShippingPhoneNumber", R.string.PaymentShippingPhoneNumber));
//                container.addView(inputFields[a], LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 12, 21, 6));
//                inputFields[a].addTextChangedListener(new TextWatcher() {
//                    private int characterAction = -1;
//                    private int actionPosition;
//
//                    @Override
//                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                        if (count == 0 && after == 1) {
//                            characterAction = 1;
//                        } else if (count == 1 && after == 0) {
//                            if (s.charAt(start) == ' ' && start > 0) {
//                                characterAction = 3;
//                                actionPosition = start - 1;
//                            } else {
//                                characterAction = 2;
//                            }
//                        } else {
//                            characterAction = -1;
//                        }
//                    }
//
//                    @Override
//                    public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//                    }
//
//                    @Override
//                    public void afterTextChanged(Editable s) {
//                        if (ignoreOnPhoneChange) {
//                            return;
//                        }
//                        HintEditText phoneField = (HintEditText) inputFields[FIELD_PHONE];
//                        int start = phoneField.getSelectionStart();
//                        String phoneChars = "0123456789";
//                        String str = phoneField.getText().toString();
//                        if (characterAction == 3) {
//                            str = str.substring(0, actionPosition) + str.substring(actionPosition + 1);
//                            start--;
//                        }
//                        StringBuilder builder = new StringBuilder(str.length());
//                        for (int a = 0; a < str.length(); a++) {
//                            String ch = str.substring(a, a + 1);
//                            if (phoneChars.contains(ch)) {
//                                builder.append(ch);
//                            }
//                        }
//                        ignoreOnPhoneChange = true;
//                        String hint = phoneField.getHintText();
//                        if (hint != null) {
//                            for (int a = 0; a < builder.length(); a++) {
//                                if (a < hint.length()) {
//                                    if (hint.charAt(a) == ' ') {
//                                        builder.insert(a, ' ');
//                                        a++;
//                                        if (start == a && characterAction != 2 && characterAction != 3) {
//                                            start++;
//                                        }
//                                    }
//                                } else {
//                                    builder.insert(a, ' ');
//                                    if (start == a + 1 && characterAction != 2 && characterAction != 3) {
//                                        start++;
//                                    }
//                                    break;
//                                }
//                            }
//                        }
//                        phoneField.setText(builder);
//                        if (start >= 0) {
//                            phoneField.setSelection(Math.min(start, phoneField.length()));
//                        }
//                        phoneField.onTextChange();
//                        ignoreOnPhoneChange = false;
//                    }
//                });
//            } else {
//                inputFields[a].setPadding(0, 0, 0, AndroidUtilities.dp(6));
//                inputFields[a].setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
//                container.addView(inputFields[a], LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 21, 12, 21, 6));
//            }
//            inputFields[a].setOnEditorActionListener((textView, i, keyEvent) -> {
//                if (i == EditorInfo.IME_ACTION_NEXT) {
//                    inputFields[FIELD_PHONE].requestFocus();
//                    return true;
//                } else if (i == EditorInfo.IME_ACTION_DONE) {
//                    doneButton.callOnClick();
//                    return true;
//                }
//                return false;
//            });
//            if (a == FIELD_PHONE) {
//                inputFields[a].setOnKeyListener((v, keyCode, event) -> {
//                    if (keyCode == KeyEvent.KEYCODE_DEL && inputFields[FIELD_PHONE].length() == 0) {
//                        inputFields[FIELD_PHONECODE].requestFocus();
//                        inputFields[FIELD_PHONECODE].setSelection(inputFields[FIELD_PHONECODE].length());
//                        inputFields[FIELD_PHONECODE].dispatchKeyEvent(event);
//                        return true;
//                    }
//                    return false;
//                });
//            }
//
//            if (a == FIELD_PHONECOUNTRY) {
//                View divider = new View(context);
//                dividers.add(divider);
//                divider.setBackgroundColor(Theme.getColor(Theme.key_divider));
//                container.addView(divider, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1, Gravity.LEFT | Gravity.BOTTOM));
//            }
//        }
//
//        String country = null;
//        try {
//            TelephonyManager telephonyManager = (TelephonyManager) ApplicationLoader.applicationContext.getSystemService(Context.TELEPHONY_SERVICE);
//            if (telephonyManager != null) {
//                country = telephonyManager.getSimCountryIso().toUpperCase();
//            }
//        } catch (Exception e) {
//            FileLog.e(e);
//        }
//        if (country != null) {
//            String countryName = languageMap.get(country);
//            if (countryName != null) {
//                int index = countriesArray.indexOf(countryName);
//                if (index != -1) {
//                    inputFields[FIELD_PHONECODE].setText(countriesMap.get(countryName));
//                }
//            }
//        }
//
//    }
//
//}
