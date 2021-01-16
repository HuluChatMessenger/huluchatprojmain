package org.plus.wallet;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Vibrator;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.RelativeSizeSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.widget.NestedScrollView;

import org.plus.features.PlusTheme;
import org.plus.net.APIError;
import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.PollEditTextCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static android.view.View.GONE;

public class WalletActionSheet extends BottomSheet {

    public static final int TYPE_SEND = 0;
    public static final int TYPE_TRANSACTION = 1;
    public static final int TYPE_DEPOSIT = 2;
    public static final int TYPE_OTP = 3;



    private int currentType;

    private ListAdapter listAdapter;
    private NestedScrollView scrollView;
    private LinearLayout linearLayout;
    private ActionBar actionBar;
    private View actionBarShadow;
    private View shadow;

    private Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);


    private BaseFragment parentFragment;

    private int scrollOffsetY;
    private AnimatorSet actionBarAnimation;
    private AnimatorSet shadowAnimation;

    private double amountValue;
    private String commentString = "";
    private String currentUserString = "";
    private double currentBalance = -1;
    private WalletModel.Transaction currentTransaction;
    private WalletModel.Wallet wallet;

    public WalletModel.Provider currentProvider;

    private TLRPC.User currentUser;

    private int titleRow;
    private int recipientHeaderRow;
    private int recipientRow;
    private int sendBalanceRow;
    private int amountHeaderRow;
    private int amountRow;
    private int commentRow;
    private int commentHeaderRow;
    private int dateHeaderRow;
    private int dateRow;
    private int invoiceInfoRow;
    private int rowCount;
    private int otpSentInfoRow;
    private int otpInputRow;
    private int otpEnterInfoRow;
    private int timerRow;




    private WalletActionSheetDelegate delegate;

    private boolean wasFirstAttach;


    private boolean inLayout;


    private static final int MAX_COMMENT_LENGTH = 500;

    public interface WalletActionSheetDelegate {

         void openContact();

         void sendOtp(TLRPC.User user, WalletModel.Provider provider,double amount,String comment,int type);
    }

    public void setDelegate(WalletActionSheetDelegate delegate) {
        this.delegate = delegate;
    }

    @SuppressWarnings("FieldCanBeLocal")
    private static class TitleCell extends FrameLayout {

        private TextView titleView;

        public TitleCell(Context context) {
            super(context);

            titleView = new TextView(getContext());
            titleView.setLines(1);
            titleView.setSingleLine(true);
            titleView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            titleView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            titleView.setPadding(AndroidUtilities.dp(22), AndroidUtilities.dp(15), AndroidUtilities.dp(22), AndroidUtilities.dp(8));
            titleView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
            titleView.setGravity(Gravity.CENTER_VERTICAL);
            addView(titleView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 60));

        }

        public TextView getTitleView() {
            return titleView;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(60), MeasureSpec.EXACTLY));
        }

        public void setText(String text) {
            titleView.setText(text);
        }
    }


    @SuppressWarnings("FieldCanBeLocal")
    private class RecipientUserCell extends PollEditTextCell {


        public RecipientUserCell(Context context) {
            super(context, null);

            EditTextBoldCursor editText = getTextView();
            editText.setSingleLine(false);
            editText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            editText.setMinLines(2);
            editText.setEnabled(false);
            editText.setTypeface(Typeface.DEFAULT);
            editText.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP);

            if (currentType == TYPE_SEND) {
                editText.setBackground(Theme.createEditTextDrawable(context, true));
            }else if(currentType == TYPE_DEPOSIT){
                editText.setBackground(Theme.createEditTextDrawable(context, true));

            }

        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public WalletActionSheet(Context context, int type, WalletModel.Wallet wallet, TLRPC.User user) {
        super(context, true);
        currentType = type;
        currentUser = user;
        this.wallet = wallet;
        currentBalance = wallet.payable;
        if(currentUser != null){
            currentUserString = UserObject.getFirstName(user) + "(+" + user.phone + ")";
        }

        init(context);
    }

    public WalletActionSheet(Context context, int type, WalletModel.Wallet wallet, WalletModel.Provider provider, TLRPC.User user) {
        super(context, true);
        currentType = type;
        currentProvider = provider;
        this.wallet = wallet;
        currentBalance = wallet.payable;
        currentUser = user;
        if(currentType == TYPE_DEPOSIT){
            currentUserString = user == null?"Wallet":"Deposit to " +  UserObject.getFirstName(user) + "(+" + user.phone + ") Wallet";
        }
        init(context);
    }


    private void updateRows() {
        rowCount = 0;
        recipientHeaderRow = -1;
        recipientRow = -1;
        amountHeaderRow = -1;
        amountRow = -1;
        commentRow = -1;
        commentHeaderRow = -1;
        dateRow = -1;
        invoiceInfoRow = -1;
        dateHeaderRow = -1;
        sendBalanceRow = -1;

         otpSentInfoRow = -1;
         otpInputRow = -1;
         otpEnterInfoRow = -1;
         timerRow = -1;

        if (currentType == TYPE_SEND) {
            titleRow = rowCount++;
            recipientHeaderRow = rowCount++;
            recipientRow = rowCount++;
            amountHeaderRow = rowCount++;
            amountRow = rowCount++;
            sendBalanceRow = rowCount++;
            commentRow = rowCount++;
        }else if(currentType == TYPE_DEPOSIT){
            titleRow = rowCount++;
            recipientHeaderRow = rowCount++;
            recipientRow = rowCount++;
            amountHeaderRow = rowCount++;
            amountRow = rowCount++;
            commentRow = rowCount++;
        }else if(currentType == TYPE_OTP){
            titleRow = rowCount++;
           // recipientHeaderRow = rowCount++;
            //recipientRow = rowCount++;
            otpSentInfoRow = rowCount++;
            otpInputRow = rowCount++;
           // otpEnterInfoRow = rowCount++;
           // timerRow = rowCount++;
        }else if(currentType == TYPE_TRANSACTION){

        }
    }

    private long last_otp_sent;
    private boolean waitingForOtp;

    private Timer timeTimer;
    private Timer codeTimer;
    private int openTime;
    private final Object timerSync = new Object();
    private int time = 60000;
    private int codeTime = 15000;
    private double lastCurrentTime;
    private double lastCodeTime;
    private void createCodeTimer() {
        if (codeTimer != null) {
            return;
        }
        codeTime = 15000;
        codeTimer = new Timer();
        lastCodeTime = System.currentTimeMillis();
        codeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                AndroidUtilities.runOnUIThread(() -> {
                    double currentTime = System.currentTimeMillis();
                    double diff = currentTime - lastCodeTime;
                    lastCodeTime = currentTime;
                    codeTime -= diff;
                    if (codeTime <= 1000) {
                       // problemText.setVisibility(VISIBLE);
                       // timeText.setVisibility(GONE);
                        destroyCodeTimer();
                    }
                });
            }
        }, 0, 1000);
    }

    private void destroyCodeTimer() {
        try {
            synchronized (timerSync) {
                if (codeTimer != null) {
                    codeTimer.cancel();
                    codeTimer = null;
                }
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
    }


    private TextView buttonTextView;

    private void init(Context context) {
        updateRows();

        FrameLayout frameLayout = new FrameLayout(context) {

            private RectF rect = new RectF();
            private boolean ignoreLayout;

            @Override
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                if (ev.getAction() == MotionEvent.ACTION_DOWN && scrollOffsetY != 0 && ev.getY() < scrollOffsetY && actionBar.getAlpha() == 0.0f) {
                    dismiss();
                    return true;
                }
                return super.onInterceptTouchEvent(ev);
            }

            @Override
            public boolean onTouchEvent(MotionEvent event) {
                return !isDismissed() && super.onTouchEvent(event);
            }

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int totalHeight = MeasureSpec.getSize(heightMeasureSpec);
                if (Build.VERSION.SDK_INT >= 21) {
                    ignoreLayout = true;
                    setPadding(backgroundPaddingLeft, AndroidUtilities.statusBarHeight, backgroundPaddingLeft, 0);
                    ignoreLayout = false;
                }
                int availableHeight = totalHeight - getPaddingTop();

                int availableWidth = MeasureSpec.getSize(widthMeasureSpec) - backgroundPaddingLeft * 2;

                LayoutParams layoutParams = (LayoutParams) actionBarShadow.getLayoutParams();
                layoutParams.topMargin = ActionBar.getCurrentActionBarHeight();

                ignoreLayout = true;

                int padding;
                int contentSize = AndroidUtilities.dp(currentTransaction != null ? 20 : 80);

                int count = listAdapter.getItemCount();
                for (int a = 0; a < count; a++) {
                    View view = listAdapter.createView(context, a);
                    view.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                    contentSize += view.getMeasuredHeight();
                }
                if (contentSize < availableHeight) {
                    padding = availableHeight - contentSize;
                } else {
                    if (currentType == TYPE_TRANSACTION) {
                        padding = availableHeight / 5;
                    } else {
                        padding = 0;
                    }
                }
                if (scrollView.getPaddingTop() != padding) {
                    int diff = scrollView.getPaddingTop() - padding;
                    scrollView.setPadding(0, padding, 0, 0);
                }
                ignoreLayout = false;
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(totalHeight, MeasureSpec.EXACTLY));
            }

            @Override
            protected void onLayout(boolean changed, int l, int t, int r, int b) {
                inLayout = true;
                super.onLayout(changed, l, t, r, b);
                inLayout = false;
                updateLayout(false);
            }

            @Override
            public void requestLayout() {
                if (ignoreLayout) {
                    return;
                }
                super.requestLayout();
            }

            @Override
            protected void onDraw(Canvas canvas) {
                int top = scrollOffsetY - backgroundPaddingTop;

                int height = getMeasuredHeight() + AndroidUtilities.dp(30) + backgroundPaddingTop;
                float rad = 1.0f;

                float r = AndroidUtilities.dp(12);
                if (top + backgroundPaddingTop < r) {
                    rad = 1.0f - Math.min(1.0f, (r - top - backgroundPaddingTop) / r);
                }

                if (Build.VERSION.SDK_INT >= 21) {
                    top += AndroidUtilities.statusBarHeight;
                    height -= AndroidUtilities.statusBarHeight;
                }

                shadowDrawable.setBounds(0, top, getMeasuredWidth(), height);
                shadowDrawable.draw(canvas);

                if (rad != 1.0f) {
                    backgroundPaint.setColor(Theme.getColor(Theme.key_dialogBackground));
                    rect.set(backgroundPaddingLeft, backgroundPaddingTop + top, getMeasuredWidth() - backgroundPaddingLeft, backgroundPaddingTop + top + AndroidUtilities.dp(24));
                    canvas.drawRoundRect(rect, r * rad, r * rad, backgroundPaint);
                }

                int color1 = Theme.getColor(Theme.key_dialogBackground);
                int finalColor = Color.argb((int) (255 * actionBar.getAlpha()), (int) (Color.red(color1) * 0.8f), (int) (Color.green(color1) * 0.8f), (int) (Color.blue(color1) * 0.8f));
                backgroundPaint.setColor(finalColor);
                canvas.drawRect(backgroundPaddingLeft, 0, getMeasuredWidth() - backgroundPaddingLeft, AndroidUtilities.statusBarHeight, backgroundPaint);
            }
        };
        frameLayout.setWillNotDraw(false);
        containerView = frameLayout;
        setApplyTopPadding(false);
        setApplyBottomPadding(false);

        listAdapter = new ListAdapter();

        scrollView = new NestedScrollView(context) {

            private View focusingView;

            @Override
            public void requestChildFocus(View child, View focused) {
                focusingView = focused;
                super.requestChildFocus(child, focused);
            }

            @Override
            protected int computeScrollDeltaToGetChildRectOnScreen(Rect rect) {
                if (linearLayout.getTop() != getPaddingTop()) {
                    return 0;
                }
                int delta = super.computeScrollDeltaToGetChildRectOnScreen(rect);
                int currentViewY = focusingView.getTop() - getScrollY() + rect.top + delta;
                int diff = ActionBar.getCurrentActionBarHeight() - currentViewY;
                if (diff > 0) {
                    delta -= diff + AndroidUtilities.dp(10);
                }
                return delta;
            }
        };
        scrollView.setClipToPadding(false);
        scrollView.setVerticalScrollBarEnabled(false);
        frameLayout.addView(scrollView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT, 0, 0, 0, currentTransaction != null ? 20 : 80));

        linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(linearLayout, LayoutHelper.createScroll(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.TOP));
        scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> updateLayout(!inLayout));
        for (int a = 0, N = listAdapter.getItemCount(); a < N; a++) {
            View view = listAdapter.createView(context, a);
            if(view.getTag() != null){
                linearLayout.addView(view, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 37,Gravity.CENTER_HORIZONTAL));

            }else{
                linearLayout.addView(view, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            }
            if (currentType == TYPE_TRANSACTION && a == commentRow) {
                view.setBackgroundDrawable(Theme.getSelectorDrawable(false));
                view.setOnClickListener(v -> {
                    AndroidUtilities.addToClipboard(commentString);
                    Toast.makeText(v.getContext(), LocaleController.getString("TextCopied", R.string.TextCopied), Toast.LENGTH_SHORT).show();
                });
                view.setOnLongClickListener(v -> {
                    AndroidUtilities.addToClipboard(commentString);
                    Toast.makeText(v.getContext(), LocaleController.getString("TextCopied", R.string.TextCopied), Toast.LENGTH_SHORT).show();
                    return true;
                });
            }
        }

        actionBar = new ActionBar(context) {
            @Override
            public void setAlpha(float alpha) {
                super.setAlpha(alpha);
                containerView.invalidate();
            }
        };
        actionBar.setBackgroundColor(Theme.getColor(Theme.key_dialogBackground));
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setItemsColor(Theme.getColor(Theme.key_dialogTextBlack), false);
        actionBar.setItemsBackgroundColor(Theme.getColor(Theme.key_dialogButtonSelector), false);
        actionBar.setTitleColor(Theme.getColor(Theme.key_dialogTextBlack));
        actionBar.setOccupyStatusBar(false);
        actionBar.setAlpha(0.0f);
        if (currentType == TYPE_SEND) {
            actionBar.setTitle(LocaleController.getString("WalletSend", R.string.WalletSend));
        }else if(currentType == TYPE_DEPOSIT){
            actionBar.setTitle("Deposit");
        }
        containerView.addView(actionBar, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    dismiss();
                }
            }
        });

        actionBarShadow = new View(context);
        actionBarShadow.setAlpha(0.0f);
        actionBarShadow.setBackgroundColor(Theme.getColor(Theme.key_dialogShadowLine));
        containerView.addView(actionBarShadow, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 1));

        shadow = new View(context);
        shadow.setBackgroundColor(Theme.getColor(Theme.key_dialogShadowLine));
        shadow.setAlpha(0.0f);
        containerView.addView(shadow, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 1, Gravity.BOTTOM | Gravity.LEFT, 0, 0, 0, 80));

        if (currentType == TYPE_SEND || currentType == TYPE_DEPOSIT || currentType == TYPE_OTP) {
            buttonTextView = new TextView(context);
            buttonTextView.setPadding(AndroidUtilities.dp(34), 0, AndroidUtilities.dp(34), 0);
            buttonTextView.setGravity(Gravity.CENTER);
            buttonTextView.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
            buttonTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            if (currentType == TYPE_SEND) {
                buttonTextView.setText(LocaleController.getString("WalletSend", R.string.WalletSend));
            }else if(currentType == TYPE_DEPOSIT){
                buttonTextView.setText("Deposit");
            }else if(currentType == TYPE_OTP){
                buttonTextView.setText("Resend Code");
                checkButton();

            }
            buttonTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            buttonTextView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(4), Theme.getColor(Theme.key_featuredStickers_addButton), Theme.getColor(Theme.key_featuredStickers_addButtonPressed)));
            frameLayout.addView(buttonTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 42, Gravity.LEFT | Gravity.BOTTOM, 16, 16, 16, 16));
            buttonTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentType == TYPE_SEND) {
                        if (!WalletUtils.canTransfer(wallet)) {
                            onFieldError(recipientRow);
                            return;
                        }
                        if (amountValue <= 0 || amountValue > currentBalance) {
                            onFieldError(amountRow);
                            return;
                        }
                        if (currentUser != null && currentUser.id == UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                            builder.setTitle(LocaleController.getString("Wallet", R.string.Wallet));
                            builder.setMessage(LocaleController.getString("WalletSendSameWalletText", R.string.WalletSendSameWalletText));
                            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                            builder.show();
                            return;
                        }
                        doSend();
                    }else if(currentType == TYPE_DEPOSIT){
                        if (amountValue <= 0) {
                            onFieldError(amountRow);
                            return;
                        }
                        if (commentString.isEmpty()) {
                            onFieldError(commentRow);
                            return;
                        }
                        if(currentProvider.requireOtp){
                            last_otp_sent = System.currentTimeMillis();
                            waitingForOtp = true;
                            if(currentUser == null){
                                currentUser = UserConfig.getInstance(currentAccount).getCurrentUser();
                            }
                            if(delegate != null){
                                delegate.sendOtp(currentUser,currentProvider,amountValue,commentString,currentType);
                                dismiss();

                            }
                        }else{
                            doDeposit();
                        }
                    }else if(currentType == TYPE_OTP){

                    }
                }
            });
        }

    }

    public void checkButton(){
        long dur = (System.currentTimeMillis() - last_otp_sent/1000);
        if(dur > 3600){
            buttonTextView.setAlpha(1f);
            buttonTextView.setClickable(true);
            buttonTextView.setEnabled(true);
        }else{
            buttonTextView.setAlpha(0.3f);
            buttonTextView.setClickable(false);
            buttonTextView.setEnabled(false);
        }
    }

    @Override
    public void dismiss() {
        AndroidUtilities.hideKeyboard(getCurrentFocus());
        super.dismiss();
    }

    @Override
    protected boolean canDismissWithSwipe() {
        return false;
    }



    private void doDepositWithOtp(String  otp){
        if(TextUtils.isEmpty(otp)){
            return;
        }
        AndroidUtilities.hideKeyboard(getCurrentFocus());
        AlertDialog progressDialog = new AlertDialog(getContext(), 3);
        progressDialog.setCanCacnel(false);
        progressDialog.show();
        WalletController.getInstance(currentAccount).depositToOtherWallet(otp,currentUser.id,commentString,amountValue, UserConfig.getInstance(currentAccount).getClientPhone(), currentProvider, (response, apiError) -> AndroidUtilities.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                dismiss();
                showStatus(getContext(),apiError);
            }
        }));

    }

    private void doDeposit(){
        AndroidUtilities.hideKeyboard(getCurrentFocus());
        AlertDialog progressDialog = new AlertDialog(getContext(), 3);
        progressDialog.setCanCacnel(false);
        progressDialog.show();
        if(currentUser == null){
            WalletController.getInstance(currentAccount).depositToSelfWallet(amountValue, UserConfig.getInstance(currentAccount).getClientPhone(), commentString, currentProvider, new WalletController.ResponseCallback() {
                @Override
                public void onResponse(Object response, APIError apiError) {
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            dismiss();
                            showStatus(getContext(),apiError);
                        }
                    });

                }
            });
        }

    }


    public void showStatus(Context context,APIError apiError){

        AlertDialog.Builder alert = null;
        AlertDialog.Builder finalAlert = alert;
        alert =   WalletUtils.createPaymentStatusAlert(context, apiError == null,apiError, () -> {
            if (finalAlert != null) {
               // finalAlert.getDismissRunnable().run();
            }
        });
        if(wallet != null && alert != null){
            alert.create().show();
        }
    }

    private void doSend() {
        if(currentUser == null){
            return;
        }
        AndroidUtilities.hideKeyboard(getCurrentFocus());

        AlertDialog progressDialog = new AlertDialog(getContext(), 3);
        progressDialog.setCanCacnel(false);
        progressDialog.show();
        WalletController.getInstance(currentAccount).transferToWallet(amountValue, currentUser.id, commentString, (response, apiError) -> AndroidUtilities.runOnUIThread(() -> {
            progressDialog.cancel();
            dismiss();
            showStatus(getContext(),apiError);

        }));
    }

    public void onResume() {

    }


    private void setTextLeft(View cell) {
        if (currentType == TYPE_TRANSACTION) {
            return;
        }
        if (cell instanceof PollEditTextCell) {
            PollEditTextCell textCell = (PollEditTextCell) cell;
            int left = MAX_COMMENT_LENGTH - commentString.getBytes().length;
            if (left <= MAX_COMMENT_LENGTH - MAX_COMMENT_LENGTH * 0.7f) {
                textCell.setText2(String.format(Locale.getDefault(), "%d", left));
                SimpleTextView textView = textCell.getTextView2();
                String key = left < 0 ? Theme.key_windowBackgroundWhiteRedText5 : Theme.key_windowBackgroundWhiteGrayText3;
                textView.setTextColor(Theme.getColor(key));
                textView.setTag(key);
            } else {
                textCell.setText2("");
            }
        } else if (cell instanceof TextInfoPrivacyCell) {
            if (currentBalance >= 0 && currentType == TYPE_SEND) {
                TextInfoPrivacyCell privacyCell = (TextInfoPrivacyCell) cell;
                String key = amountValue > currentBalance ? Theme.key_windowBackgroundWhiteRedText5 : Theme.key_windowBackgroundWhiteBlueHeader;
                privacyCell.getTextView().setTag(key);
                privacyCell.getTextView().setTextColor(Theme.getColor(key));
            }
        }
    }

    private void onFieldError(int row) {
        View view = linearLayout.getChildAt(row);
        if (view == null) {
            return;
        }
        AndroidUtilities.shakeView(view, 2, 0);
        try {
            Vibrator v = (Vibrator) parentFragment.getParentActivity().getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) {
                v.vibrate(200);
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    private void updateLayout(boolean animated) {
        View child = scrollView.getChildAt(0);
        int top = child.getTop() - scrollView.getScrollY();
        int newOffset = 0;
        if (top >= 0) {
            newOffset = top;
        }
        boolean show = newOffset <= 0;
        if (show && actionBar.getTag() == null || !show && actionBar.getTag() != null) {
            actionBar.setTag(show ? 1 : null);
            if (actionBarAnimation != null) {
                actionBarAnimation.cancel();
                actionBarAnimation = null;
            }
            if (animated) {
                actionBarAnimation = new AnimatorSet();
                actionBarAnimation.setDuration(180);
                actionBarAnimation.playTogether(
                        ObjectAnimator.ofFloat(actionBar, View.ALPHA, show ? 1.0f : 0.0f),
                        ObjectAnimator.ofFloat(actionBarShadow, View.ALPHA, show ? 1.0f : 0.0f));
                actionBarAnimation.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        actionBarAnimation = null;
                    }
                });
                actionBarAnimation.start();
            } else {
                actionBar.setAlpha(show ? 1.0f : 0.0f);
                actionBarShadow.setAlpha(show ? 1.0f : 0.0f);
            }
        }
        if (scrollOffsetY != newOffset) {
            scrollOffsetY = newOffset;
            containerView.invalidate();
        }

        int b = child.getBottom();
        int h = scrollView.getMeasuredHeight();
        show = child.getBottom() - scrollView.getScrollY() > scrollView.getMeasuredHeight();
        if (show && shadow.getTag() == null || !show && shadow.getTag() != null) {
            shadow.setTag(show ? 1 : null);
            if (shadowAnimation != null) {
                shadowAnimation.cancel();
                shadowAnimation = null;
            }
            if (animated) {
                shadowAnimation = new AnimatorSet();
                shadowAnimation.setDuration(180);
                shadowAnimation.playTogether(ObjectAnimator.ofFloat(shadow, View.ALPHA, show ? 1.0f : 0.0f));
                shadowAnimation.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        shadowAnimation = null;
                    }
                });
                shadowAnimation.start();
            } else {
                shadow.setAlpha(show ? 1.0f : 0.0f);
            }
        }
    }
    private EditTextBoldCursor[] codeField;
    private boolean ignoreOnTextChange;
    private LinearLayout codeFieldContainer;

    private String getCode() {
        if (codeField == null) {
            return "";
        }
        StringBuilder codeBuilder = new StringBuilder();
        for (int a = 0; a < codeField.length; a++) {
            codeBuilder.append(PhoneFormat.stripExceptNumbers(codeField[a].getText().toString()));
        }
        return codeBuilder.toString();
    }

    private LinearLayout createCodeFieldView(Context context){
        int length = 4;
        codeFieldContainer = new LinearLayout(context);
        codeFieldContainer.setOrientation(LinearLayout.HORIZONTAL);
        codeField = new EditTextBoldCursor[length];
        for (int a = 0; a < length; a++) {
            final int num = a;
            codeField[a] = new EditTextBoldCursor(getContext());
            codeField[a].setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            codeField[a].setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            codeField[a].setCursorSize(AndroidUtilities.dp(20));
            codeField[a].setCursorWidth(1.5f);

            Drawable pressedDrawable = PlusTheme.getDrawable(R.drawable.search_dark_activated).mutate();
            pressedDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteInputFieldActivated), PorterDuff.Mode.MULTIPLY));

            codeField[a].setBackgroundDrawable(pressedDrawable);
            codeField[a].setImeOptions(EditorInfo.IME_ACTION_NEXT | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            codeField[a].setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            codeField[a].setMaxLines(1);
            codeField[a].setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            codeField[a].setPadding(0, 0, 0, 0);
            codeField[a].setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
            codeField[a].setInputType(InputType.TYPE_CLASS_PHONE);
            codeFieldContainer.addView(codeField[a], LayoutHelper.createLinear(34, 36, Gravity.CENTER_HORIZONTAL, 0, 0, a != length - 1 ? 7 : 0, 0));
            codeField[a].addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (ignoreOnTextChange) {
                        return;
                    }
                    int len = s.length();
                    if (len >= 1) {
                        if (len > 1) {
                            String text = s.toString();
                            ignoreOnTextChange = true;
                            for (int a = 0; a < Math.min(length - num, len); a++) {
                                if (a == 0) {
                                    s.replace(0, len, text.substring(a, a + 1));
                                } else {
                                    codeField[num + a].setText(text.substring(a, a + 1));
                                }
                            }
                            ignoreOnTextChange = false;
                        }

                        if (num != length - 1) {
                            codeField[num + 1].setSelection(codeField[num + 1].length());
                            codeField[num + 1].requestFocus();
                        }
                        if ((num == length - 1 || num == length - 2 && len >= 2) && getCode().length() == length) {
                            onNextPressed();
                        }
                    }
                }
            });
            codeField[a].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL && codeField[num].length() == 0 && num > 0) {
                    codeField[num - 1].setSelection(codeField[num - 1].length());
                    codeField[num - 1].requestFocus();
                    codeField[num - 1].dispatchKeyEvent(event);
                    return true;
                }
                return false;
            });
            codeField[a].setOnEditorActionListener((textView, i, keyEvent) -> {
                if (i == EditorInfo.IME_ACTION_NEXT) {
                    onNextPressed();
                    return true;
                }
                return false;
            });
        }

        return codeFieldContainer;
    }

    private void onFieldError(View view) {
        try {
            Vibrator v = (Vibrator) ApplicationLoader.applicationContext.getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) {
                v.vibrate(200);
            }
        } catch (Throwable ignore) {

        }
        AndroidUtilities.shakeView(view, 2, 0);
    }


    private void onNextPressed(){
        String code = getCode();
        if (TextUtils.isEmpty(code)) {
            onFieldError(codeFieldContainer);
            return;
        }
    }


    private class ListAdapter {

        public int getItemCount() {
            return rowCount;
        }

        public void onBindViewHolder(View itemView, int position, int type) {
            switch (type) {
                case 0: {
                    HeaderCell cell = (HeaderCell) itemView;
                    if (position == recipientHeaderRow) {
                        if (currentType == TYPE_TRANSACTION) {
                            if (amountValue > 0) {
                                cell.setText(LocaleController.getString("WalletTransactionSender", R.string.WalletTransactionSender));
                            } else {
                                cell.setText(LocaleController.getString("WalletTransactionRecipient", R.string.WalletTransactionRecipient));
                            }
                        } else {
                            if(currentType == TYPE_DEPOSIT){
                                cell.setText("Deposit To:");
                            }else{
                                cell.setText(LocaleController.getString("WalletSendRecipient", R.string.WalletSendRecipient));

                            }
                        }
                    } else if (position == commentHeaderRow) {
                        cell.setText(LocaleController.getString("WalletTransactionComment", R.string.WalletTransactionComment));
                    } else if (position == dateHeaderRow) {
                        cell.setText(LocaleController.getString("WalletDate", R.string.WalletDate));
                    } else if (position == amountHeaderRow) {
                        cell.setText(LocaleController.getString("WalletAmount", R.string.WalletAmount));
                    }
                    break;
                }
                case 1: {
                    TextInfoPrivacyCell cell = (TextInfoPrivacyCell) itemView;
                    if (position == invoiceInfoRow) {
                        cell.setText(LocaleController.getString("WalletInvoiceInfo", R.string.WalletInvoiceInfo));
                    }else  if (position == otpSentInfoRow) {
                        cell.getTextView().setGravity(Gravity.CENTER_HORIZONTAL);
                        cell.setText("To complete your request please insert the confirmation OTP send to your mobile phone");
                    }else  if (position == otpEnterInfoRow) {
                        cell.getTextView().setGravity(Gravity.CENTER_HORIZONTAL);
                        cell.setText("Enter your pin");
                    }
                    break;
                }
                case 3: {
                    PollEditTextCell textCell = (PollEditTextCell) itemView;
                    if (position == commentRow) {
                        textCell.setTextAndHint(commentString, LocaleController.getString("WalletComment", R.string.WalletComment), false);
                    }
                    break;
                }
                case 4: {
                    PollEditTextCell textCell = (PollEditTextCell) itemView;
                   // textCell.setText(amountValue != 0 ? WalletUtils.formatCurrency(amountValue) : "", true);
                    break;
                }
                case 6: {
                    RecipientUserCell textCell = (RecipientUserCell) itemView;
                    textCell.setTextAndHint(currentUserString, LocaleController.getString("WalletEnterWalletAddress", R.string.WalletEnterWalletAddress), false);
                    break;
                }
                case 7: {
                    TextInfoPrivacyCell cell = (TextInfoPrivacyCell) itemView;
                    if (position == sendBalanceRow) {
                        if (wallet != null) {
                            double balance = wallet.amount;
                            cell.setText(LocaleController.formatString("WalletSendBalance", R.string.WalletSendBalance, WalletUtils.formatCurrency(currentBalance = balance)));
                        }
                    }
                    break;
                }

                case 9: {
                    TitleCell cell = (TitleCell) itemView;
                    if (currentType == TYPE_SEND) {
                        cell.setText(LocaleController.getString("Send", R.string.Send));
                    }else if(currentType == TYPE_DEPOSIT){
                        cell.setText("Deposit");
                    }else if(currentType == TYPE_TRANSACTION){
                        cell.setText("Transaction info");
                    }else if(currentType == TYPE_OTP){
                        cell.getTitleView().setGravity(Gravity.CENTER_HORIZONTAL);
                        cell.setText("Enter your pin");
                    }
                }
                break;
            }
        }


        public View createView(Context context, int position) {
            int viewType = getItemViewType(position);
            View view;
            switch (viewType) {
                case 0:
                    view = new HeaderCell(context);
                    break;
                case 1:
                    view = new TextInfoPrivacyCell(context);
                    break;
                case 3: {
                    PollEditTextCell cell = new PollEditTextCell(context, null) {
                        @Override
                        protected void onAttachedToWindow() {
                            super.onAttachedToWindow();
                            setTextLeft(this);
                        }
                    };
                    EditTextBoldCursor editText = cell.getTextView();
                    if (currentType == TYPE_TRANSACTION) {
                        editText.setEnabled(false);
                        editText.setFocusable(false);
                        editText.setClickable(false);
                    } else {
                        editText.setBackground(Theme.createEditTextDrawable(context, true));
                        editText.setPadding(0, AndroidUtilities.dp(14), AndroidUtilities.dp(37), AndroidUtilities.dp(14));
                        cell.createErrorTextView();
                        InputFilter[] inputFilters = new InputFilter[1];
                        inputFilters[0] = new InputFilter.LengthFilter(MAX_COMMENT_LENGTH);
                        editText.setFilters(inputFilters);
                        cell.addTextWatcher(new TextWatcher() {

                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {

                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                if (cell.getTag() != null) {
                                    return;
                                }
                                commentString = s.toString();
                                View view = linearLayout.getChildAt(commentRow);
                                if (view != null) {
                                    setTextLeft(view);
                                }
                            }
                        });
                    }
                    view = cell;
                    break;
                }
                case 4: {
                    PollEditTextCell cell = new PollEditTextCell(context, null) {
                        @Override
                        protected boolean drawDivider() {
                            return false;
                        }

                        @Override
                        protected void onEditTextDraw(EditTextBoldCursor editText, Canvas canvas) {
                            int left = 0;
                            int top = AndroidUtilities.dp(7);
                            Layout layout;
                            if (editText.length() > 0) {
                                layout = editText.getLayout();
                            } else {
                                layout = editText.getHintLayoutEx();
                            }
                            if (layout != null) {
                                left = (int) Math.ceil(layout.getLineWidth(0)) + AndroidUtilities.dp(6);
                            }
                            if (left != 0) {
                                float scale = 0.74f;
                                // gemDrawable.setBounds(left, top, left + (int) (gemDrawable.getIntrinsicWidth() * scale), top + (int) (gemDrawable.getIntrinsicHeight() * scale));
                                //  gemDrawable.draw(canvas);
                            }
                        }

                        @Override
                        protected void onAttachedToWindow() {
                            super.onAttachedToWindow();
                            if (!wasFirstAttach && currentType != TYPE_TRANSACTION) {
//                                if (recipientString.codePointCount(0, recipientString.length()) == 48) {
//                                    getTextView().requestFocus();
//                                }
                                wasFirstAttach = true;
                            }
                        }
                    };
                    EditTextBoldCursor editText = cell.getTextView();
                    cell.setShowNextButton(true);
                    editText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
                    editText.setHintColor(Theme.getColor(Theme.key_dialogTextHint));
                    editText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
                    editText.setBackground(Theme.createEditTextDrawable(context, true));
                    editText.setImeOptions(editText.getImeOptions() | EditorInfo.IME_ACTION_NEXT);
                    editText.setCursorSize(AndroidUtilities.dp(30));
                    editText.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
                    SpannableStringBuilder stringBuilder = new SpannableStringBuilder("0.0");
                    stringBuilder.setSpan(new RelativeSizeSpan(0.73f), stringBuilder.length() - 1, stringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    editText.setHintText(stringBuilder);
                    editText.setInputType(InputType.TYPE_CLASS_PHONE);
                    editText.setOnEditorActionListener((v, actionId, event) -> {
                        if (actionId == EditorInfo.IME_ACTION_NEXT) {
                            View commentView = linearLayout.getChildAt(commentRow);
                            if (commentView != null) {
                                PollEditTextCell editTextCell = (PollEditTextCell) commentView;
                                editTextCell.getTextView().requestFocus();
                            }
                            return true;
                        }
                        return false;
                    });
                    cell.addTextWatcher(new TextWatcher() {

                        private boolean ignoreTextChange;
                        private boolean adding;
                        private RelativeSizeSpan sizeSpan = new RelativeSizeSpan(0.73f);

                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            adding = count == 0 && after == 1;
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            if (ignoreTextChange || editText.getTag() != null) {
                                return;
                            }
                            if(TextUtils.isEmpty(s)){
                                return;
                            }
                            amountValue = Double.parseDouble(s.toString());
                            View view = linearLayout.getChildAt(sendBalanceRow);
                            if (view != null) {
                                setTextLeft(view);
                            }
                        }
                    });
                    view = cell;
                    break;
                }
                case 6: {
                    view = new RecipientUserCell(context);
                    break;
                }
                case 7: {
                    view = new TextInfoPrivacyCell(context) {
                        @Override
                        protected void onAttachedToWindow() {
                            super.onAttachedToWindow();
                            setTextLeft(this);
                        }
                    };
                    break;
                }
                case 10:
                    view = createCodeFieldView(context);
                    view.setTag(1);
                    break;
                case 9:
                default: {
                    view = new TitleCell(context);
                    break;
                }
            }
            onBindViewHolder(view, position, viewType);
            return view;
        }


        public int getItemViewType(int position) {
            if (position == recipientHeaderRow || position == commentHeaderRow || position == dateHeaderRow || position == amountHeaderRow) {
                return 0;
            } else if (position == invoiceInfoRow || position == otpSentInfoRow || position ==  otpEnterInfoRow) {
                return 1;
            } else if (position == commentRow || position == dateRow) {
                return 3;
            } else if (position == amountRow) {
                return 4;
            } else if (position == recipientRow) {
                return 6;
            } else if (position == sendBalanceRow) {
                return 7;
            } else if(position == otpInputRow){
                return 10;
            } else{
                return 9;
            }
        }
    }

}