package org.plus.apps.wallet;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.widget.NestedScrollView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.PollEditTextCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;

import java.util.Locale;

public class WalletProcessingAlert extends BottomSheet{

    public interface OfferBottomSheetDelegate {

        void onOfferMade(double price,String comment);
    }


    private OfferBottomSheetDelegate offerBottomSheetDelegate;

    public void setOfferBottomSheetDelegate(OfferBottomSheetDelegate offerBottomSheetDelegate) {
        this.offerBottomSheetDelegate = offerBottomSheetDelegate;
    }


    private boolean inLayout;
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

    private String commentString = "";
    private double amountValue ;


    private int amountHeaderRow;
    private int amountRow;
    private int commentRow;
    private int invoiceInfoRow;
    private int rowCount;
    private int titleRow;

    private int type;

    private static final int MAX_COMMENT_LENGTH = 500;

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

    public WalletProcessingAlert(BaseFragment fragment, int  type) {
        super(fragment.getParentActivity(), true);
        parentFragment = fragment;
        this.type = type;
        init(fragment.getParentActivity());
    }

    private void updateRows() {

        titleRow = -1;
        invoiceInfoRow = -1;
        amountHeaderRow = -1;
        amountRow = -1;
        commentRow = -1;

        titleRow = rowCount++;
        invoiceInfoRow = rowCount++;
        amountHeaderRow = rowCount++;
        amountRow = rowCount++;
        commentRow = rowCount++;
    }


    private void init(Context context){

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
                int contentSize = AndroidUtilities.dp(80);

                int count = listAdapter.getItemCount();
                for (int a = 0; a < count; a++) {
                    View view = listAdapter.createView(context, a);
                    view.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                    contentSize += view.getMeasuredHeight();
                }
                if (contentSize < availableHeight) {
                    padding = availableHeight - contentSize;
                } else {
                    padding = 0;
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
        frameLayout.addView(scrollView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT, 0, 0, 0, 80));
        linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(linearLayout, LayoutHelper.createScroll(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.TOP));
        scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> updateLayout(!inLayout));

        for (int a = 0, N = listAdapter.getItemCount(); a < N; a++) {
            View view = listAdapter.createView(context, a);
            linearLayout.addView(view, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
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
        actionBar.setTitle("Offer");
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

        TextView buttonTextView = new TextView(context);
        buttonTextView.setPadding(AndroidUtilities.dp(34), 0, AndroidUtilities.dp(34), 0);
        buttonTextView.setGravity(Gravity.CENTER);
        buttonTextView.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
        buttonTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        buttonTextView.setText("Offer");


        buttonTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        buttonTextView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(4), Theme.getColor(Theme.key_featuredStickers_addButton), Theme.getColor(Theme.key_featuredStickers_addButtonPressed)));
        frameLayout.addView(buttonTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 42, Gravity.LEFT | Gravity.BOTTOM, 16, 16, 16, 16));
        buttonTextView.setOnClickListener(v -> {
            if (amountValue <= 0) {
                //onFieldError(amountRow);
                return;
            }
            dismiss();
            offerBottomSheetDelegate.onOfferMade(amountValue,commentString);
        });

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

    @Override
    protected boolean canDismissWithSwipe() {
        return false;
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
                    return new String(source.toString().getBytes(), start, keep, "UTF-8");
                } catch (Exception ignore) {
                    return "";
                }
            }
        }

        public int getMax() {
            return mMax;
        }
    }


    private void setTextLeft(View cell) {
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
//            if (currentBalance >= 0 && currentType == TYPE_SEND) {
//                TextInfoPrivacyCell privacyCell = (TextInfoPrivacyCell) cell;
//                String key = amountValue > currentBalance ? Theme.key_windowBackgroundWhiteRedText5 : Theme.key_windowBackgroundWhiteBlueHeader;
//                privacyCell.getTextView().setTag(key);
//                privacyCell.getTextView().setTextColor(Theme.getColor(key));
//            }
        }
    }

    public WalletProcessingAlert(Context context, boolean needFocus) {
        super(context, needFocus);
    }

    private PollEditTextCell commentEditText;
    private class ListAdapter {

        public int getItemCount() {
            return rowCount;
        }

        public void onBindViewHolder(View itemView, int position, int type) {
            switch (type) {
                case 0: {
                    HeaderCell cell = (HeaderCell) itemView;
                    if (position == amountHeaderRow) {
                        cell.setText("Amount");
                    }
                    break;
                }
                case 1: {
                    TextInfoPrivacyCell cell = (TextInfoPrivacyCell) itemView;
                    if (position == invoiceInfoRow) {
                        cell.setText("Offer your price");
                    }
                    break;
                }
                case 3: {
                    PollEditTextCell textCell = (PollEditTextCell) itemView;
                    if (position == commentRow) {
                        textCell.setTextAndHint(commentString, "offer Comment", false);
                    }
                    break;
                }
                case 4: {
                    PollEditTextCell textCell = (PollEditTextCell) itemView;
                    textCell.setText(amountValue != 0 ? "ETB " +  amountValue + "" : "", true);
                    break;
                }
                case 9: {
                    TitleCell cell = (TitleCell) itemView;
                    if (position == titleRow) {
                        cell.setText("Offer price");
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
                    view = new HeaderCell(context, Theme.key_dialogTextBlack, 21, 12, false);
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
                    view = cell;
                    commentEditText =cell;
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
                            amountValue = Utilities.parseLong(s.toString());

                        }
                    });
                    view = cell;
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
            if (position == amountHeaderRow) {
                return 0;
            } else if (position == invoiceInfoRow) {
                return 1;
            } else if (position == commentRow ) {
                return 3;
            } else if (position == amountRow) {
                return 4;
            } else {
                return 9;
            }
        }
    }


}
