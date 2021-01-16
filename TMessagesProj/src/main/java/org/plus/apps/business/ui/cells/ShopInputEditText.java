package org.plus.apps.business.ui.cells;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;

import java.util.ArrayList;


public class ShopInputEditText extends FrameLayout {

    private EditTextBoldCursor textView;
    private ImageView deleteImageView;
    private boolean showNextButton;
    private boolean needDivider;
    public Paint borderPaint = new Paint();
    private RectF rectF = new RectF();
    private int padding;

    public ShopInputEditText(Context context, OnClickListener onDelete,int pad) {
        super(context);
        padding = AndroidUtilities.dp(pad);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(Theme.getColor(Theme.key_divider));
        borderPaint.setStrokeWidth(3);

        textView = new EditTextBoldCursor(context) {
            @Override
            public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
                InputConnection conn = super.onCreateInputConnection(outAttrs);
                if (showNextButton) {
                    outAttrs.imeOptions &= ~EditorInfo.IME_FLAG_NO_ENTER_ACTION;
                }
                return conn;
            }

            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                onEditTextDraw(this, canvas);
            }

            @Override
            public boolean onTouchEvent(MotionEvent event) {
                if (!isEnabled()) {
                    return false;
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    onFieldTouchUp(this);
                }
                return super.onTouchEvent(event);
            }
        };
        textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        textView.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        textView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
        textView.setBackgroundDrawable(null);
        textView.setImeOptions(textView.getImeOptions() | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        textView.setInputType(textView.getInputType() | EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES);
        textView.setPadding(AndroidUtilities.dp(10), AndroidUtilities.dp(10), AndroidUtilities.dp(10), AndroidUtilities.dp(10));


        if(onDelete != null){
            deleteImageView = new ImageView(context);
            deleteImageView.setFocusable(false);
            deleteImageView.setScaleType(ImageView.ScaleType.CENTER);
            deleteImageView.setBackgroundDrawable(Theme.createSelectorDrawable(Theme.getColor(Theme.key_stickers_menuSelector)));
            deleteImageView.setImageResource(R.drawable.poll_remove);
            deleteImageView.setOnClickListener(onDelete);
            deleteImageView.setColorFilter(Theme.getColor(Theme.key_chats_actionBackground));
            deleteImageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon), PorterDuff.Mode.MULTIPLY));
            deleteImageView.setContentDescription(LocaleController.getString("Delete", R.string.Delete));
            addView(deleteImageView, LayoutHelper.createFrame(48, 50, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.CENTER_VERTICAL, LocaleController.isRTL ? 3 + pad : 0, 0, LocaleController.isRTL ? 0 : 3 + pad, 0));

        }
        addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL, 19 , 0, 19 , 0));

    }

    public void setMaxLength(int max){
        InputFilter[]  inputFilters = new InputFilter[1];
        inputFilters[0] = new InputFilter.LengthFilter(max);
        textView.setFilters(inputFilters);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        if (deleteImageView != null) {
            deleteImageView.measure(MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(48), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(48), MeasureSpec.EXACTLY));
        }

        int right;
        right = 42;
        textView.measure(MeasureSpec.makeMeasureSpec(width - getPaddingLeft() - getPaddingRight() - AndroidUtilities.dp(right), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        int h = textView.getMeasuredHeight();
        setMeasuredDimension(width, Math.max(AndroidUtilities.dp(60), textView.getMeasuredHeight()) + (needDivider ? 1 : 0));

    }



    protected void onEditTextDraw(EditTextBoldCursor editText, Canvas canvas) {

    }


    public void callOnDelete() {
        if (deleteImageView == null) {
            return;
        }
        deleteImageView.callOnClick();
    }

    public void setShowNextButton(boolean value) {
        showNextButton = value;
    }

    public EditTextBoldCursor getTextView() {
        return textView;
    }

    public ImageView getDeleteImageView() {
        return deleteImageView;
    }

    public String getText() {
        return textView.getText().toString();
    }

    public int length() {
        return textView.length();
    }

    public void setTextColor(int color) {
        textView.setTextColor(color);
    }

    public void setText(CharSequence text, boolean divider) {
        textView.setText(text);
        needDivider = divider;
        setWillNotDraw(!divider);
    }

    public void setTextAndHint(CharSequence text, String hint, boolean divider) {
        if (deleteImageView != null) {
            deleteImageView.setTag(null);
        }
        textView.setText(text);
        if (!TextUtils.isEmpty(text)) {
            textView.setSelection(textView.length());
        }
        textView.setHint(hint);
        needDivider = divider;
        setWillNotDraw(!divider);
    }

    public void setEnabled(boolean value, ArrayList<Animator> animators) {
        setEnabled(value);
    }

    protected void onFieldTouchUp(EditTextBoldCursor editText) {

    }

    protected boolean drawDivider() {
        return true;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (needDivider && drawDivider()) {
            rectF.set(padding,10,getMeasuredWidth() - padding,getMeasuredHeight() - 10);
            canvas.drawRoundRect(rectF,12,12,borderPaint);
        }
    }


}

