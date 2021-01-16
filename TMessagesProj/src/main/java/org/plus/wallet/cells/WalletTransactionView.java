package org.plus.wallet.cells;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.plus.features.PlusTheme;
import org.plus.wallet.WalletModel;
import org.plus.wallet.WalletUtils;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

import java.time.Instant;

public class WalletTransactionView extends FrameLayout {

    private boolean needDivider;

    private ImageView statusImageView;
    private TextView transactionTextView;
    private TextView statusTextView;
    private TextView dateTextView;
    private TextView amountTextView;

    private WalletModel.Transaction transaction;

    public WalletTransactionView(Context context) {
        super(context);

        setWillNotDraw(false);

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        needDivider = true;
        statusImageView = new ImageView(context);
        statusImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        addView(statusImageView, LayoutHelper.createFrame(42,42, Gravity.LEFT|Gravity.TOP,16,16,0,0));

        LinearLayout topLinearLayout = new LinearLayout(context);
        topLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        topLinearLayout.setWeightSum(1f);
        linearLayout.addView(topLinearLayout,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,Gravity.LEFT|Gravity.TOP,0,16,0,0));

        transactionTextView = new TextView(context);
        transactionTextView.setTextColor(Theme.getColor(Theme.key_wallet_blackText));
        transactionTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        transactionTextView.setLines(1);
        transactionTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        transactionTextView.setMaxLines(1);
        transactionTextView.setSingleLine(true);
        transactionTextView.setEllipsize(TextUtils.TruncateAt.END);
        transactionTextView.setGravity(Gravity.CENTER_VERTICAL|Gravity.LEFT);
        topLinearLayout.addView(transactionTextView,LayoutHelper.createLinear(0,LayoutHelper.WRAP_CONTENT,0.7f,Gravity.CENTER_VERTICAL,0,0,0,0));

        amountTextView = new TextView(context);
        amountTextView.setTextColor(Theme.getColor(Theme.key_wallet_blackText));
        amountTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        amountTextView.setLines(1);
        amountTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        amountTextView.setMaxLines(1);
        amountTextView.setSingleLine(true);
        amountTextView.setEllipsize(TextUtils.TruncateAt.END);
        amountTextView.setGravity((Gravity.LEFT) | Gravity.CENTER_VERTICAL);
        topLinearLayout.addView(amountTextView,LayoutHelper.createLinear(0,LayoutHelper.WRAP_CONTENT,0.3f,Gravity.CENTER_VERTICAL,0,0,0,0));


        LinearLayout bottomLinearLayout = new LinearLayout(context);
        bottomLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.addView(bottomLinearLayout,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,Gravity.LEFT|Gravity.TOP,0,8,0,0));

        statusTextView = new TextView(context);
        statusTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3));
        statusTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        statusTextView.setLines(1);
        statusTextView.setMaxLines(1);
        statusTextView.setSingleLine(true);
        statusTextView.setEllipsize(TextUtils.TruncateAt.END);
        statusTextView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP);
        bottomLinearLayout.addView(statusTextView,LayoutHelper.createLinear(0,LayoutHelper.WRAP_CONTENT,0.7f,Gravity.CENTER_VERTICAL,0,0,0,0));

        dateTextView = new TextView(context);
        dateTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        dateTextView.setMaxLines(1);
        dateTextView.setSingleLine(true);
        dateTextView.setTextColor(Theme.getColor(Theme.key_wallet_dateText));
        dateTextView.setEllipsize(TextUtils.TruncateAt.END);
        dateTextView.setGravity((Gravity.LEFT) | Gravity.CENTER_VERTICAL);
        bottomLinearLayout.addView(dateTextView,LayoutHelper.createLinear(0,LayoutHelper.WRAP_CONTENT,0.3f,Gravity.CENTER_VERTICAL,0,0,0,0));

        addView(linearLayout,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,Gravity.TOP|Gravity.LEFT,64,0,0,16));
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (needDivider) {
              canvas.drawLine(AndroidUtilities.dp(32 + 16 + 16), getMeasuredHeight() - 1, getMeasuredWidth(), getMeasuredHeight() - 1, Theme.dividerPaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
    }

    public WalletModel.Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(WalletModel.Transaction transaction, boolean needDivider) {
        if(transaction == null){
            return;
        }
        this.needDivider = needDivider;
        this.transaction = transaction;
        statusImageView.setImageDrawable(WalletUtils.getDrawableForTransactionType(transaction.transaction_type));


        String text = "";
        if(transaction.transaction_type.equals(WalletUtils.CASH_OUT)){
            text += "-";
        }else{
            text += "+";
        }
        text += String.valueOf(transaction.amount);
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        int end =text.length();
        spannableStringBuilder.append(text);
        spannableStringBuilder.setSpan(new ForegroundColorSpan(WalletUtils.getColorForType(transaction.transaction_type)), 0, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.append("  ብር");
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Theme.getColor(Theme.key_wallet_blackText)), end, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        amountTextView.setText(spannableStringBuilder);


        transactionTextView.setText(WalletUtils.getTextForType(transaction.transaction_type));

        if(transaction.status.equals("pending")){
            setAlpha(0.5f);

            statusTextView.setText("Pending");
            try{
                Drawable statusDrawable = PlusTheme.getDrawable(R.drawable.msg_timer);
                statusDrawable.setBounds(0,0,AndroidUtilities.dp(14),AndroidUtilities.dp(14));
                statusDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3), PorterDuff.Mode.MULTIPLY));
                statusTextView.setCompoundDrawables(statusDrawable,null,null,null);
            }catch (Exception ignore){

            }

        }else{
            setAlpha(1.0f);
            statusTextView.setText("0 % transfer fee");
        }
        dateTextView.setText(LocaleController.getInstance().formatterDay.format(Instant.parse(transaction.created_at).toEpochMilli()));

    }
}
