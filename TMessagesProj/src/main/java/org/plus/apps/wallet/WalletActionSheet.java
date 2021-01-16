package org.plus.apps.wallet;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.plus.apps.business.ShopUtils;
import org.plus.apps.business.data.ShopDataSerializer;
import org.plus.apps.business.ui.ProductDetailFragment;
import org.plus.apps.business.ui.components.HorizontalFilterLayout;
import org.plus.apps.business.ui.components.SpanListLayout;
import org.plus.apps.wallet.BiometricPromtHelper;
import org.plus.apps.wallet.WalletTransaction;
import org.plus.net.APIError;
import org.plus.wallet.WalletUtils;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.DialogCell;
import org.telegram.ui.Cells.DividerCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.PollEditTextCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.TypefaceSpan;
import org.telegram.ui.ContactsActivity;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.util.Util;

public class WalletActionSheet extends BottomSheet {


    public static final int TYPE_SEND = 0;
    public static final int TYPE_INVOICE = 1;
    public static final int TYPE_TRANSACTION = 2;
    public static final int TYPE_DEPOSIT = 3;
    public static final int TYPE_AIT_time= 4;
    public static final int SEND_ACTIVITY_RESULT_CODE = 33;

    private int currentType;

    private ListAdapter listAdapter;
    private NestedScrollView scrollView;
    private LinearLayout linearLayout;
    private ActionBar actionBar;
    private View actionBarShadow;
    private View shadow;
    private BiometricPromtHelper biometricPromtHelper;

//    private Drawable gemDrawable;
    private boolean inLayout;

    private Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private BaseFragment parentFragment;

    private int scrollOffsetY;
    private AnimatorSet actionBarAnimation;
    private AnimatorSet shadowAnimation;

    private TLRPC.User user;


    private boolean showUser;

    private long amountValue;
    private long currentDate;
    private long currentStorageFee;
    private long currentTransactionFee;
    private String commentString = "";
    private String recipientString = "";
    private boolean hasWalletInBack = true;
    private boolean wasFirstAttach;
    private long currentBalance = -1;

    private WalletTransaction currentTransaction;

    private boolean sendUnencrypted;

    private int titleRow;
    private int recipientHeaderRow;
    private int recipientRow;
    private int sendBalanceRow;
    private int amountHeaderRow;
    private int amountRow;
    private int commentRow;

    private int commentHeaderRow;
    private int balanceRow;
    private int dateHeaderRow;
    private int dateRow;
    private int invoiceInfoRow;
    private int rowCount;


    private int depositInfoRow;

    private int airTimeInfoRow;
    private int airTimeAmountHeader;
    private int airTimePresetValue;
    private int airTimeHistoryRow;


    private WalletActionSheetDelegate delegate;

    private static final int MAX_COMMENT_LENGTH = 500;

    public interface WalletActionSheetDelegate {

        default void onSendToUser(TLRPC.User address) {
        }

        default void onDeposit(double amount, String provider) {
        }

        default void onTopUp(double amount, TLRPC.User user){}
    }

    public static class ByteLengthFilter implements InputFilter {

        private final int mMax;

        public ByteLengthFilter(int max) {
            mMax = max;
        }

        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            int keep = mMax - (dest.toString().getBytes().length - (dend - dstart));
            if (keep <= 0) {
                return "";
            } else if (keep >= end - start) {
                return null;
            } else {
                keep += start;
                try {
                    return new String(source.toString().getBytes(), start, keep, "utf-8");
                } catch (Exception ignore) {
                    return "";
                }
            }
        }

        public int getMax() {
            return mMax;
        }
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

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(60), MeasureSpec.EXACTLY));
        }

        public void setText(String text) {
            titleView.setText(text);
        }
    }

    public class BalanceCell extends FrameLayout {

        private SimpleTextView valueTextView;
        private TextView yourBalanceTextView;

        public BalanceCell(Context context) {
            super(context);

            valueTextView = new SimpleTextView(context);
            valueTextView.setTextSize(30);
            valueTextView.setRightDrawable(R.drawable.gem);
            valueTextView.setRightDrawableScale(0.8f);
            valueTextView.setDrawablePadding(AndroidUtilities.dp(7));
            valueTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            valueTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
            valueTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            if (currentTransaction != null && currentTransaction.isEmpty) {
                valueTextView.setVisibility(GONE);
            }
            addView(valueTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 18, 0, 0));

            yourBalanceTextView = new TextView(context);
            yourBalanceTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
            yourBalanceTextView.setTextColor(Theme.getColor(Theme.key_dialogTextGray2));
            yourBalanceTextView.setLineSpacing(AndroidUtilities.dp(4), 1);
            yourBalanceTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            addView(yourBalanceTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, currentTransaction != null && currentTransaction.isEmpty ? 30 : 59, 0, 0));
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int h = AndroidUtilities.dp(currentTransaction != null && currentTransaction.isEmpty ? 80 : 100);
            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY));
        }

        public void setBalance(long value, long storageFee, long transactionFee) {
            SpannableStringBuilder stringBuilder = new SpannableStringBuilder((value > 0 ? "+" : "") + WalletUtils.formatCurrencyEmpty(value));
            int index = TextUtils.indexOf(stringBuilder, '.');
            if (index >= 0) {
                stringBuilder.setSpan(new TypefaceSpan(AndroidUtilities.getTypeface("fonts/rmedium.ttf"), AndroidUtilities.dp(22)), index + 1, stringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            valueTextView.setText(stringBuilder);

            if (storageFee != 0 || transactionFee != 0) {
                StringBuilder builder = new StringBuilder();
                if (transactionFee != 0) {
                    builder.append(LocaleController.formatString("WalletTransactionFee", R.string.WalletTransactionFee, WalletUtils.formatCurrencyEmpty(transactionFee)));
                }
                if (storageFee != 0) {
                    if (builder.length() != 0) {
                        builder.append('\n');
                    }
                    builder.append(LocaleController.formatString("WalletStorageFee", R.string.WalletStorageFee,  WalletUtils.formatCurrencyEmpty(storageFee)));
                }
                yourBalanceTextView.setText(builder);
                yourBalanceTextView.setVisibility(VISIBLE);
            } else {
                yourBalanceTextView.setVisibility(INVISIBLE);
            }
        }
    }

    @SuppressWarnings("FieldCanBeLocal")
    private class SendAddressCell extends PollEditTextCell {

        private ImageView qrButton;
        private ImageView copyButton;

        public SendAddressCell(Context context) {
            super(context, null);

            EditTextBoldCursor editText = getTextView();
            editText.setSingleLine(false);
            editText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            editText.setMinLines(2);
            editText.setTypeface(Typeface.DEFAULT);
            editText.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP);
            addTextWatcher(new TextWatcher() {

                private boolean ignoreTextChange;
                private boolean isPaste;

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    isPaste = after >= 24;
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (ignoreTextChange) {
                        return;
                    }
                    String str = s.toString();
                    if (isPaste && str.toLowerCase().startsWith("ton://transfer")) {
                        ignoreTextChange = true;
                        parseTonUrl(s, str);
                        ignoreTextChange = false;
                    } else {
                        recipientString = str;
                    }
                }
            });

            if (currentType == TYPE_SEND) {
                editText.setBackground(Theme.createEditTextDrawable(context, true));

                qrButton = new ImageView(context);
                qrButton.setImageResource(R.drawable.wallet_qr);
                qrButton.setScaleType(ImageView.ScaleType.CENTER);
                qrButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader), PorterDuff.Mode.MULTIPLY));
                qrButton.setBackgroundDrawable(Theme.createSelectorDrawable(Theme.getColor(Theme.key_actionBarWhiteSelector), 6));
               addView(qrButton, LayoutHelper.createFrame(48, 48, Gravity.TOP | (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT), 6, 0, 6, 0));
                qrButton.setOnClickListener(v -> {
                    AndroidUtilities.hideKeyboard(getCurrentFocus());
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("onlyUsers",true);
                    bundle.putBoolean("destroyAfterSelect",true);
                    bundle.putBoolean("returnAsResult",true);
                    bundle.putBoolean("allowBots",false);
                    bundle.putBoolean("allowSelf",false);
                    bundle.putBoolean("disableSections",true);
                    ContactsActivity contactsActivity = new ContactsActivity(bundle);
                    contactsActivity.setDelegate(new ContactsActivity.ContactsActivityDelegate() {
                        @Override
                        public void didSelectContact(TLRPC.User user, String param, ContactsActivity activity) {
                           // openTransfer(user);
                        }
                    });
                    parentFragment.presentFragment(contactsActivity);
                    //presentFragment(contactsActivity);
                });
                editText.setPadding(LocaleController.isRTL ? AndroidUtilities.dp(60) : 0, AndroidUtilities.dp(13), LocaleController.isRTL ? 0 : AndroidUtilities.dp(60), AndroidUtilities.dp(8));
            } else {
                editText.setFocusable(false);
                editText.setEnabled(false);
                editText.setTypeface(Typeface.MONOSPACE);
                editText.setPadding(0, AndroidUtilities.dp(13), 0, AndroidUtilities.dp(10));

                copyButton = new ImageView(context);
                copyButton.setImageResource(R.drawable.msg_copy);
                copyButton.setScaleType(ImageView.ScaleType.CENTER);
                copyButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader), PorterDuff.Mode.MULTIPLY));
                copyButton.setBackgroundDrawable(Theme.createSelectorDrawable(Theme.getColor(Theme.key_actionBarWhiteSelector), 6));
                addView(copyButton, LayoutHelper.createFrame(48, 48, Gravity.TOP | (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT), 6, 10, 6, 0));
                copyButton.setOnClickListener(v -> {
                    AndroidUtilities.addToClipboard("ton://transfer/" + recipientString.replace("\n", ""));
                    Toast.makeText(v.getContext(), LocaleController.getString("WalletTransactionAddressCopied", R.string.WalletTransactionAddressCopied), Toast.LENGTH_SHORT).show();
                });
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            if (qrButton != null) {
                measureChildWithMargins(qrButton, widthMeasureSpec, 0, heightMeasureSpec, 0);
            }
//            if (copyButton != null) {
//                measureChildWithMargins(copyButton, widthMeasureSpec, 0, heightMeasureSpec, 0);
//            }
        }
    }

    public WalletActionSheet(BaseFragment fragment, int type, TLRPC.User user, String provider,boolean showUser) {
        super(fragment.getParentActivity(), true);
        this.user = user;
        this.showUser = showUser;
        currentType = type;
        parentFragment = fragment;
        this.paymentProvider = provider;
        init(fragment.getParentActivity());
    }

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
                int contentSize = AndroidUtilities.dp(currentTransaction != null && currentTransaction.isEmpty ? 20 : 80);

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
        frameLayout.addView(scrollView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT, 0, 0, 0, currentTransaction != null && currentTransaction.isEmpty ? 20 : 80));
        linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(linearLayout, LayoutHelper.createScroll(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.TOP));
        scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> updateLayout(!inLayout));

        for (int a = 0, N = listAdapter.getItemCount(); a < N; a++) {
            View view = listAdapter.createView(context, a);
            linearLayout.addView(view, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
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
        if (currentType == TYPE_INVOICE) {
            actionBar.setTitle(LocaleController.getString("WalletCreateInvoiceTitle", R.string.WalletCreateInvoiceTitle));
        } else if (currentType == TYPE_TRANSACTION) {
            actionBar.setTitle(LocaleController.getString("WalletTransaction", R.string.WalletTransaction));
        } else if (currentType == TYPE_DEPOSIT) {
            actionBar.setTitle(LocaleController.getString("WalletDeposit", R.string.WalletDeposit));
        } else if (currentType == TYPE_AIT_time) {
            actionBar.setTitle(LocaleController.getString("AirTime", R.string.AirTime));
        }else {
            actionBar.setTitle("Transfer");
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

        if (currentType != TYPE_TRANSACTION || !TextUtils.isEmpty(recipientString)) {
            TextView buttonTextView = new TextView(context);
            buttonTextView.setPadding(AndroidUtilities.dp(34), 0, AndroidUtilities.dp(34), 0);
            buttonTextView.setGravity(Gravity.CENTER);
            buttonTextView.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
            buttonTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            if (currentType == TYPE_TRANSACTION) {
                buttonTextView.setText(LocaleController.getString("WalletTransactionSendGrams", R.string.WalletTransactionSendGrams));
            } else if (currentType == TYPE_SEND) {
                buttonTextView.setText("Send");
            } else if (currentType == TYPE_DEPOSIT) {
                buttonTextView.setText("Deposit");
            } else if (currentType == TYPE_AIT_time) {
                buttonTextView.setText("Buy");
            } else {
                buttonTextView.setText(LocaleController.getString("WalletCreateInvoiceTitle", R.string.WalletCreateInvoiceTitle));
            }
            buttonTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            buttonTextView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(4), Theme.getColor(Theme.key_featuredStickers_addButton), Theme.getColor(Theme.key_featuredStickers_addButtonPressed)));
            frameLayout.addView(buttonTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 42, Gravity.LEFT | Gravity.BOTTOM, 16, 16, 16, 16));
            buttonTextView.setOnClickListener(v -> {
                if (currentType == TYPE_TRANSACTION) {
                   // delegate.openSendToAddress(recipientString.replace("\n", ""));
                    dismiss();
                } else if (currentType == TYPE_SEND) {
                    if (amountValue <= 0) {
                        onFieldError(amountRow);
                        return;
                    }

                    doSend();
                } else if (currentType == TYPE_INVOICE) {
                    if (amountValue <= 0) {
                        onFieldError(amountRow);
                        return;
                    }
                  //  String url = "ton://transfer/" + walletAddress + "/?amount=" + amountValue;
                    if (!TextUtils.isEmpty(commentString)) {
                        try {
                           // url += "&text=" + URLEncoder.encode(commentString, "UTF-8").replaceAll("\\+", "%20");
                        } catch (Exception e) {
                            FileLog.e(e);
                        }
                    }
                    dismiss();
                   // delegate.openInvoice(url, amountValue);
                }else if(currentType == TYPE_DEPOSIT){
                    if (amountValue <= 0) {
                        onFieldError(amountRow);
                        return;
                    }
                    if(delegate != null){
                        delegate.onDeposit(amountValue,paymentProvider);
                    }

                    dismiss();


                }else if(currentType == TYPE_AIT_time){
                    if (preset == null || Integer.parseInt(preset.amount) <= 0) {
                        onFieldError(airTimePresetValue);
                        return;
                    }
                    if(delegate != null){
                        delegate.onTopUp(Integer.parseInt(preset.amount),user);
                    }
                    dismiss();
                }
            });
        }
    }

    private Preset preset;
    private String paymentProvider;

    public void setPaymentProvider(String paymentProvider) {
        this.paymentProvider = paymentProvider;
    }

    public void setRecipientString(String value, boolean hasWallet) {
        recipientString = value;
        hasWalletInBack = hasWallet;
        if (scrollView != null) {
            View view = linearLayout.getChildAt(recipientRow);
            if (view != null) {
                listAdapter.onBindViewHolder(view, recipientRow, listAdapter.getItemViewType(recipientRow));
            }
        }
    }

    public void parseTonUrl(Editable s, String url) {
        try {
            Uri uri = Uri.parse(url);
            String path = uri.getPath();
            String text = uri.getQueryParameter("text");
            String amount = uri.getQueryParameter("amount");
            if (!TextUtils.isEmpty(path) && path.length() > 1) {
                recipientString = path.replace("/", "");
                if (s == null && scrollView != null) {
                    View view = linearLayout.getChildAt(recipientRow);
                    if (view != null) {
                        listAdapter.onBindViewHolder(view, recipientRow, listAdapter.getItemViewType(recipientRow));
                    }
                }
            }
            if (!TextUtils.isEmpty(text)) {
                commentString = text;
                if (scrollView != null) {
                    View view = linearLayout.getChildAt(commentRow);
                    if (view != null) {
                        listAdapter.onBindViewHolder(view, commentRow, listAdapter.getItemViewType(commentRow));
                    }
                }
            }
            if (!TextUtils.isEmpty(amount)) {
                amountValue = Utilities.parseLong(amount);
            }
            if (scrollView != null) {
                View view = linearLayout.getChildAt(amountRow);
                if (view != null) {
                    if (!TextUtils.isEmpty(amount)) {
                        listAdapter.onBindViewHolder(view, amountRow, listAdapter.getItemViewType(amountRow));
                    }
                    PollEditTextCell pollEditTextCell = (PollEditTextCell) view;
                    EditTextBoldCursor editText = pollEditTextCell.getTextView();
                    editText.setSelection(editText.length());
                    editText.requestFocus();
                    AndroidUtilities.showKeyboard(editText);
                }
            }
            if (s != null) {
                s.replace(0, s.length(), recipientString);
            }
        } catch (Exception ignore) {

        }
    }

    public void onPause() {
        if (biometricPromtHelper != null) {
            biometricPromtHelper.onPause();
        }
    }

    public void onResume() {

    }

    public void setDelegate(WalletActionSheetDelegate walletActionSheetDelegate) {
        delegate = walletActionSheetDelegate;
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

    @Override
    public void dismiss() {
        AndroidUtilities.hideKeyboard(getCurrentFocus());
        super.dismiss();
    }

    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        if (requestCode == SEND_ACTIVITY_RESULT_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                dismiss();
               // parentFragment.presentFragment(new WalletPasscodeActivity(false, null, walletAddress, recipientString, amountValue, commentString, sendUnencrypted, hasWalletInBack));
            }
        }
    }


    private void doSend() {
//        AlertDialog progressDialog = new AlertDialog(getContext(), 3);
//        progressDialog.setCanCacnel(false);
//        progressDialog.show();
//        WalletController.getInstance(currentAccount).transfer(amountValue, commentString, user.id, new WalletController.ResponseDelegate() {
//            @Override
//            public void onResponse(Object response, APIError error) {
//                progressDialog.cancel();
//                if(error == null){
//                    Context context = getContext();
//                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                    builder.setTitle(LocaleController.getString("WalletConfirmation", R.string.WalletConfirmation));
//                    builder.setMessage("Successful!");
//                    builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
//                    builder.show();
//                    builder.create().show();
//                  //  WalletController.getInstance(currentAccount).loadTransaction(parentFragment.getClassGuid(),null);
//
//
//
////                    WalletDataSerializer.Transaction transaction = (WalletDataSerializer.Transaction)response;
////                    int user_id = UserConfig.getInstance(currentAccount).clientUserId;
////                    String message= WalletController.formatSendMessage(transaction,user_id,user.id);
////                    SendMessagesHelper.getInstance(currentAccount).sendMessage(message,user.id,null,null,null,false,null,null,null,false,0);
////
//                }
//                dismiss();
//            }
//        });


    }





    @Override
    protected boolean canDismissWithSwipe() {
        return false;
    }

    private void updateRows() {
        rowCount = 0;
        recipientHeaderRow = -1;
        recipientRow = -1;
        amountHeaderRow = -1;
        amountRow = -1;
        commentRow = -1;
        commentHeaderRow = -1;
        balanceRow = -1;
        dateRow = -1;
        invoiceInfoRow = -1;
        depositInfoRow = -1;
        airTimeInfoRow = -1;
        dateHeaderRow = -1;
        sendBalanceRow = -1;

        airTimeInfoRow = -1;
        airTimePresetValue  = -1;
        airTimeHistoryRow = -1;
        airTimeAmountHeader = -1;


        titleRow = rowCount++;
        if (currentType == TYPE_INVOICE) {
            invoiceInfoRow = rowCount++;
            amountHeaderRow = rowCount++;
            amountRow = rowCount++;
            commentRow = rowCount++;
        } else if (currentType == TYPE_TRANSACTION) {
            balanceRow = rowCount++;
            if (!TextUtils.isEmpty(recipientString)) {
                recipientHeaderRow = rowCount++;
                recipientRow = rowCount++;
            }
            dateHeaderRow = rowCount++;
            dateRow = rowCount++;
            if (!TextUtils.isEmpty(commentString)) {
                commentHeaderRow = rowCount++;
                commentRow = rowCount++;
            }
        } else if (currentType == TYPE_DEPOSIT) {
            depositInfoRow = rowCount++;
            amountHeaderRow = rowCount++;
            amountRow = rowCount++;
        } else if (currentType == TYPE_AIT_time) {
            airTimeInfoRow = rowCount++;
            airTimeAmountHeader = rowCount++;
            airTimePresetValue  = rowCount++;
        } else {
            if(showUser){
                recipientHeaderRow = rowCount++;
                recipientRow = rowCount++;
            }
            amountHeaderRow = rowCount++;
            amountRow = rowCount++;
            sendBalanceRow = rowCount++;
            commentRow = rowCount++;
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

    private class ListAdapter {

        public int getItemCount() {
            return rowCount;
        }

        public void onBindViewHolder(View itemView, int position, int type) {
            switch (type) {
                case 0: {
                    HeaderCell cell = (HeaderCell) itemView;
                    if (position == recipientHeaderRow) {
                        cell.setText(LocaleController.getString("WalletTransactionRecipient", R.string.WalletTransactionRecipient));

                    } else if (position == commentHeaderRow) {
                        cell.setText(LocaleController.getString("WalletTransactionComment", R.string.WalletTransactionComment));
                    } else if (position == dateHeaderRow) {
                        cell.setText(LocaleController.getString("WalletDate", R.string.WalletDate));
                    } else if (position == amountHeaderRow) {
                        cell.setText(LocaleController.getString("WalletAmount", R.string.WalletAmount));
                    }else if (position == airTimeAmountHeader) {
                        cell.setText("Choose Amount");
                    }
                    break;
                }
                case 1: {
                    TextInfoPrivacyCell cell = (TextInfoPrivacyCell) itemView;
                    if (position == invoiceInfoRow) {
                        cell.setText(LocaleController.getString("WalletInvoiceInfo", R.string.WalletInvoiceInfo));
                    }else  if (position == depositInfoRow) {
                        cell.setText("Deposit from " + paymentProvider + " to your wallet");
                    }else  if (position == airTimeInfoRow) {
                        cell.setText("Buy airtime for  " + user.first_name + "(+"+ user.phone + ")");
                    }
                    break;
                }
                case 3: {
                    PollEditTextCell textCell = (PollEditTextCell) itemView;
                    if (position == dateRow) {
                        textCell.setTextAndHint(LocaleController.getInstance().formatterStats.format(currentDate * 1000), "", false);
                    } else if (position == commentRow) {
                        textCell.setTextAndHint(commentString, LocaleController.getString("WalletComment", R.string.WalletComment), false);
                    }
                    break;
                }
                case 4: {
                    PollEditTextCell textCell = (PollEditTextCell) itemView;
                   // textCell.setText(amountValue != 0 ?  WalletController.getInstance(currentAccount).formatCurrency(amountValue) : "", true);
                    break;
                }


                case 6: {
                    ProductDetailFragment.ContactChangeCell dialogCell = (ProductDetailFragment.ContactChangeCell ) itemView;
                    dialogCell.setUser(user);
                    break;
                }
                case 7: {
                    TextInfoPrivacyCell cell = (TextInfoPrivacyCell) itemView;
                    if (position == sendBalanceRow) {
//                        TonApi.FullAccountState state = TonController.getInstance(currentAccount).getCachedAccountState();
//                        if (state != null) {
//                            long balance = TonController.isRWallet(state) ? TonController.getUnlockedBalance(state) : TonController.getBalance(state);
//                            cell.setText(LocaleController.formatString("WalletSendBalance", R.string.WalletSendBalance, TonController.formatCurrency(currentBalance = balance)));
//                        }
                    }
                    break;
                }
                case 8: {
                    BalanceCell cell = (BalanceCell) itemView;
                    cell.setBalance(amountValue, currentStorageFee, currentTransactionFee);
                    break;
                }
                case 9: {
                    TitleCell cell = (TitleCell) itemView;
                    if (position == titleRow) {
                        if (currentType == TYPE_INVOICE) {
                            cell.setText(LocaleController.getString("WalletCreateInvoiceTitle", R.string.WalletCreateInvoiceTitle));
                        } else if (currentType == TYPE_TRANSACTION) {
                            if (currentTransaction.isInit) {
                                cell.setText(LocaleController.getString("WalletInitTransaction", R.string.WalletInitTransaction));
                            } else if (currentTransaction.isEmpty) {
                                cell.setText(LocaleController.getString("WalletEmptyTransaction", R.string.WalletEmptyTransaction));
                            } else {
                                cell.setText(LocaleController.getString("WalletTransaction", R.string.WalletTransaction));
                            }
                        }  else if (currentType == TYPE_DEPOSIT) {
                            cell.setText("Deposit");
                        }else if (currentType == TYPE_SEND) {
                            cell.setText("Transfer");
                        }else if (currentType == TYPE_AIT_time) {
                            cell.setText("Airtime");
                        }
                    }
                    break;
                }
            }
        }

        public View createView(Context context, int position) {
            int viewType = getItemViewType(position);
            View view;
            switch (viewType) {
                case 0:
                    view = new HeaderCell(context, Theme.key_dialogTextBlack, 21, 12, false);
                    break;
                case 1:
                    view = new TextInfoPrivacyCell(context);
                    break;
                case 10:
                    PresetAmountLayout presetAmountLayout = new PresetAmountLayout(getContext(), new PresetAmountDelegate() {
                        @Override
                        public void onPresetClicked(Preset preset, View view) {
                            WalletActionSheet.this.preset = preset;
                        }
                    });
                    presetAmountLayout.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    view = presetAmountLayout;
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
                        editText.setFilters(new InputFilter[]{new ByteLengthFilter(MAX_COMMENT_LENGTH)});
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
//                                gemDrawable.setBounds(left, top, left + (int) (gemDrawable.getIntrinsicWidth() * scale), top + (int) (gemDrawable.getIntrinsicHeight() * scale));
//                                gemDrawable.draw(canvas);
                            }
                        }

                        @Override
                        protected void onAttachedToWindow() {
                            super.onAttachedToWindow();
                            if (!wasFirstAttach && currentType != TYPE_TRANSACTION) {
                                if (recipientString.codePointCount(0, recipientString.length()) == 48) {
                                    getTextView().requestFocus();
                                }
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
                            int selection = editText.getSelectionStart();
                            ignoreTextChange = true;
                             amountValue = Utilities.parseLong(s.toString());
                            ignoreTextChange = false;
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
                    ProductDetailFragment.ContactChangeCell contactChangeCell = new ProductDetailFragment.ContactChangeCell(context);
                    view = contactChangeCell;
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
                case 8: {
                    view = new BalanceCell(context);
                    break;
                }
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
            if (position == recipientHeaderRow || position == commentHeaderRow || position == dateHeaderRow || position == amountHeaderRow || position == airTimeAmountHeader) {
                return 0;
            } else if (position == invoiceInfoRow || position == depositInfoRow || position ==  airTimeInfoRow) {
                return 1;
            } else if (position == commentRow || position == dateRow) {
                return 3;
            } else if (position == amountRow) {
                return 4;
            } else if (position == recipientRow) {
                return 6;
            } else if (position == sendBalanceRow) {
                return 7;
            } else if (position == balanceRow) {
                return 8;
            } else if (position == airTimePresetValue) {
                return 10;
            } else {
                return 9;
            }
        }
    }

    public  static class Preset{
        public  String amount;

        public Preset(String amount){
            this.amount = amount;
        }
    }
    public interface PresetAmountDelegate{
        void onPresetClicked(Preset preset, View view);
    }
    private  class PresetAmountLayout extends FrameLayout {

        private ArrayList<Preset>  presets = new ArrayList<>();

        private PresetAmountDelegate delegate;
        private RecyclerListView listView;
        private LinearLayoutManager layoutManager;

        private PresetAdapter adapter;



        private void updateRow(){
            presets.add(new Preset("5"));
            presets.add(new Preset("10"));
            presets.add(new Preset("15"));
            presets.add(new Preset("25"));
            presets.add(new Preset("50"));
            presets.add(new Preset("100"));
            presets.add(new Preset("200"));
            presets.add(new Preset("1000"));
            presets.add(new Preset("2000"));
            presets.add(new Preset("3000"));

        }

        public PresetAmountLayout(Context context, PresetAmountDelegate layoutImageDelegate) {
            super(context);

            updateRow();

            delegate = layoutImageDelegate;
            listView = new RecyclerListView(context);
            listView.setPadding(AndroidUtilities.dp(4),0,AndroidUtilities.dp(4),AndroidUtilities.dp(4));
            listView.setClipToPadding(false);
            listView.setLayoutManager(layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            listView.setHorizontalScrollBarEnabled(false);
            listView.setGlowColor(ShopUtils.getIntAlphaColor(Theme.getColor(Theme.key_chats_actionBackground),0.1f));
            listView.setAdapter(adapter = new PresetAdapter(context));
            addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT));
            listView.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(android.graphics.Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                    outRect.left = AndroidUtilities.dp(4);
                    outRect.bottom = AndroidUtilities.dp(4);
                    outRect.top = AndroidUtilities.dp(4);
                    outRect.right = AndroidUtilities.dp(4);
                }
            });
        }

        public RecyclerListView getListView() {
            return listView;
        }



        private class PresetAdapter extends RecyclerListView.SelectionAdapter{

            private Context mContext;

            public PresetAdapter(Context context) {
                this.mContext = context;
            }

            @Override
            public boolean isEnabled(RecyclerView.ViewHolder holder) {
                return false;
            }


            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new RecyclerListView.Holder(new PresetItemView(mContext){

                    @Override
                    public void onPresetPressed(Preset preset) {
                        WalletActionSheet.this.preset = preset;
                    }
                });

            }


            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                Preset preset = presets.get(position);
                PresetItemView itemView = (PresetItemView) holder.itemView;
                itemView.setPreset(preset);
                if( WalletActionSheet.this.preset  != null && preset != null){
                    itemView.setSelected( WalletActionSheet.this.preset.amount.equals(preset.amount));
                }
            }

            @Override
            public int getItemCount() {
                return presets.size();
            }

        }

        private  class PresetItemView extends FrameLayout{
            
            
            private TextView filterTextView;

            public PresetItemView(@NonNull Context context) {
                super(context);

                filterTextView = new TextView(context);
                filterTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
                filterTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
                filterTextView.setTypeface(AndroidUtilities.getTypeface("fonts/Roboto-Black.ttf"));
                filterTextView.setLines(1);
                filterTextView.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(8), AndroidUtilities.dp(16), AndroidUtilities.dp(8));
                filterTextView.setMaxLines(1);
                filterTextView.setSingleLine(true);
                filterTextView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                addView(filterTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 4, 16, 8, 16));
                filterTextView.setBackground(ShopUtils.createRoundStrokeDrwable(8, 3, Theme.key_windowBackgroundGray, Theme.key_windowBackgroundWhite));

                filterTextView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(preset != null){
                           onPresetPressed(preset);
                           setSelected();
                        }
                    }
                });
            }

            public void onPresetPressed(Preset preset){

            }

            public void setSelected(){
                if(preset.amount.equals(WalletActionSheet.this.preset.amount)){
                    filterTextView.setBackground(Theme.createRoundRectDrawable(8, Theme.getColor(Theme.key_dialogTextBlue)));
                    filterTextView.setTextColor(Theme.getColor(Theme.key_avatar_text));
                }else{
                    filterTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
                    filterTextView.setBackground(ShopUtils.createRoundStrokeDrwable(8, 1, Theme.key_windowBackgroundGray, Theme.key_windowBackgroundWhite));
                }

            }


            private Preset preset;
            public void setPreset(Preset preset){
                this.preset = preset;
//                String text =  preset.amount;
//                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
//                spannableStringBuilder.append("ETB ");
//                int end = spannableStringBuilder.length();
//                spannableStringBuilder.setSpan(new ForegroundColorSpan(Theme.getColor(Theme.key_dialogTextGray2)), 0, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                spannableStringBuilder.setSpan(new StyleSpan(Typeface.NORMAL), 0, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                spannableStringBuilder.append(text);
                filterTextView.setText( "ETB "+ preset.amount);
            }

        }




    }


}