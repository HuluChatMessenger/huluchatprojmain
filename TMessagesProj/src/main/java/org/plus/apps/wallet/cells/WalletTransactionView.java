//package org.plus.apps.wallet.cells;
//
//import android.content.Context;
//import android.graphics.Canvas;
//import android.media.Image;
//import android.text.TextUtils;
//import android.util.TypedValue;
//import android.view.Gravity;
//import android.widget.FrameLayout;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//
//import com.google.android.gms.wallet.MaskedWallet;
//
//import org.plus.apps.wallet.WalletController;
//import org.plus.apps.wallet.WalletDataSerializer;
//import org.plus.wallet.WalletModel;
//import org.telegram.messenger.AndroidUtilities;
//import org.telegram.messenger.LocaleController;
//import org.telegram.ui.ActionBar.Theme;
//import org.telegram.ui.Components.LayoutHelper;
//
//import java.time.Instant;
//
//public class WalletTransactionView extends FrameLayout {
//
//    private boolean needDivider;
//
//    private ImageView statusImageView;
//    private TextView transactionTextView;
//    private TextView statusTextView;
//    private TextView dateTextView;
//    private TextView amountTextView;
//
//    private WalletModel.Transaction transaction;
//
//    public WalletTransactionView(Context context) {
//        super(context);
//
//
//        setWillNotDraw(false);
//
//        LinearLayout linearLayout = new LinearLayout(context);
//        linearLayout.setOrientation(LinearLayout.VERTICAL);
//
//        needDivider = true;
//        statusImageView = new ImageView(context);
//        statusImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
//        addView(statusImageView, LayoutHelper.createFrame(32,32, Gravity.LEFT|Gravity.TOP,16,16,0,0));
//
//        LinearLayout topLinearLayout = new LinearLayout(context);
//        topLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
//        topLinearLayout.setWeightSum(1f);
//        linearLayout.addView(topLinearLayout,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,Gravity.LEFT|Gravity.TOP,0,16,0,0));
//
//        transactionTextView = new TextView(context);
//        transactionTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
//        transactionTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
//        transactionTextView.setLines(1);
//        transactionTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
//        transactionTextView.setMaxLines(1);
//        transactionTextView.setSingleLine(true);
//        transactionTextView.setEllipsize(TextUtils.TruncateAt.END);
//        transactionTextView.setGravity(Gravity.CENTER_VERTICAL|Gravity.LEFT);
//        topLinearLayout.addView(transactionTextView,LayoutHelper.createLinear(0,LayoutHelper.WRAP_CONTENT,0.7f,Gravity.CENTER_VERTICAL,0,0,0,0));
//
//        amountTextView = new TextView(context);
//        amountTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
//        amountTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
//        amountTextView.setLines(1);
//        amountTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
//        amountTextView.setMaxLines(1);
//        amountTextView.setSingleLine(true);
//        amountTextView.setEllipsize(TextUtils.TruncateAt.END);
//        amountTextView.setGravity((Gravity.LEFT) | Gravity.CENTER_VERTICAL);
//        topLinearLayout.addView(amountTextView,LayoutHelper.createLinear(0,LayoutHelper.WRAP_CONTENT,0.3f,Gravity.CENTER_VERTICAL,0,0,0,0));
//
//
//        LinearLayout bottomLinearLayout = new LinearLayout(context);
//        bottomLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
//        linearLayout.addView(bottomLinearLayout,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,Gravity.LEFT|Gravity.TOP,0,8,0,0));
//
//        statusTextView = new TextView(context);
//        statusTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3));
//        statusTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
//        statusTextView.setLines(1);
//        statusTextView.setMaxLines(1);
//        statusTextView.setSingleLine(true);
//        statusTextView.setEllipsize(TextUtils.TruncateAt.END);
//        statusTextView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP);
//        bottomLinearLayout.addView(statusTextView,LayoutHelper.createLinear(0,LayoutHelper.WRAP_CONTENT,0.7f,Gravity.CENTER_VERTICAL,0,0,0,0));
//
//        dateTextView = new TextView(context);
//        dateTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
//        amountTextView.setMaxLines(1);
//        amountTextView.setSingleLine(true);
//        amountTextView.setEllipsize(TextUtils.TruncateAt.END);
//        amountTextView.setGravity((Gravity.LEFT) | Gravity.CENTER_VERTICAL);
//        bottomLinearLayout.addView(dateTextView,LayoutHelper.createLinear(0,LayoutHelper.WRAP_CONTENT,0.3f,Gravity.CENTER_VERTICAL,0,0,0,0));
//
//        addView(linearLayout,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,Gravity.TOP|Gravity.LEFT,64,0,0,16));
//    }
//
//
//    @Override
//    protected void onDraw(Canvas canvas) {
//        if (needDivider) {
//              canvas.drawLine(AndroidUtilities.dp(32 + 16 + 16), getMeasuredHeight() - 1, getMeasuredWidth(), getMeasuredHeight() - 1, Theme.dividerPaint);
//        }
//    }
//
//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
//    }
//
//    public WalletModel.Transaction getTransaction() {
//        return transaction;
//    }
//
//    public void setTransaction(WalletModel.Transaction transaction, boolean needDivider) {
//        if(transaction == null){
//            return;
//        }
//        this.needDivider = needDivider;
//        this.transaction = transaction;
//
////
////        if(transaction.status.equals("pending")){
////
////        }else{
////
////        }
//
//
//        if(transaction.transaction_type.equals("cashout")){
//            statusImageView.setImageDrawable(WalletController.statusDrawables[0]);
//        }else{
//            statusImageView.setImageDrawable(WalletController.statusDrawables[1]);
//        }
////        if(transaction.transaction_type.equals(WalletController.TRANSACTION_TYPE_TRANSFER)){
////               String type = WalletController.getTransferType(transaction);
////               if(type.equals(WalletController.TRANSACTION_TYPE_SEND)){
////                   statusImageView.setImageDrawable(WalletController.statusDrawables[0]);
////               }else{
////                   statusImageView.setImageDrawable(WalletController.statusDrawables[1]);
////               }
////            transactionTextView.setText(type);
////        }else if(transaction.transaction_type.equals("cashout"){
////            statusImageView.setImageDrawable(WalletController.statusDrawables[1]);
////            transactionTextView.setText(WalletController.TRANSACTION_TYPE_WITHDRAW);
////        }else if(transaction.transaction_type.equals(WalletController.TRANSACTION_TYPE_PAY)){
////            statusImageView.setImageDrawable(WalletController.statusDrawables[1]);
////            transactionTextView.setText(WalletController.TRANSACTION_TYPE_PAY);
////        }else if(transaction.transaction_type.equals(WalletController.TRANSACTION_TYPE_DEPOSIT)){
////            statusImageView.setImageDrawable(WalletController.statusDrawables[3]);
////            transactionTextView.setText(WalletController.TRANSACTION_TYPE_DEPOSIT);
////        }
//
//
//        transactionTextView.setText(transaction.transaction_type);
//
//
//        if(transaction.status.equals("pending")){
//            setAlpha(0.5f);
//        }else{
//            setAlpha(1.0f);
//        }
//
//        statusTextView.setText(transaction.status);
//        amountTextView.setText(WalletController.formatCurrency(transaction.amount));
//        dateTextView.setText(LocaleController.getInstance().formatterDay.format(Instant.parse(transaction.created_at).toEpochMilli()));
//
//    }
//}
