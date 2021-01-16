

package org.plus.wallet.cells;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.plus.wallet.WalletUtils;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RLottieDrawable;

@SuppressWarnings("FieldCanBeLocal")
public class WalletBalanceCell extends FrameLayout {

    private SimpleTextView valueTextView;
    private TextView yourBalanceTextView;
    private FrameLayout withDrawButton;
    private FrameLayout sendButton;
    private FrameLayout depositButton;
    private SimpleTextView receiveTextView;
    private SimpleTextView sendTextView;

    private Drawable sendDrawable;
    private Drawable receiveDrawable;
    private Drawable depositDrawable;

    private Typeface defaultTypeFace;
    private RLottieDrawable gemDrawable;

    public WalletBalanceCell(Context context) {
        super(context);

        valueTextView = new SimpleTextView(context);
        valueTextView.setTextColor(Theme.getColor(Theme.key_wallet_whiteText));
        valueTextView.setTextSize(41);
        valueTextView.setDrawablePadding(AndroidUtilities.dp(7));
        valueTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        valueTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        addView(valueTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 35, 0, 0));

        gemDrawable = new RLottieDrawable(R.raw.wallet_money,"wallet_money",AndroidUtilities.dp(42), AndroidUtilities.dp(42));
        gemDrawable.setAutoRepeat(1);
        gemDrawable.setAllowDecodeSingleFrame(true);
        gemDrawable.addParentView(valueTextView);
        valueTextView.setRightDrawable(gemDrawable);
        gemDrawable.start();


        yourBalanceTextView = new TextView(context);
        yourBalanceTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        yourBalanceTextView.setTextColor(Theme.getColor(Theme.key_wallet_whiteText));
        defaultTypeFace = yourBalanceTextView.getTypeface();
        addView(yourBalanceTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 90, 0, 0));

        receiveDrawable = context.getResources().getDrawable(R.drawable.wallet_receive).mutate();
        receiveDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_wallet_buttonText), PorterDuff.Mode.MULTIPLY));
        sendDrawable = context.getResources().getDrawable(R.drawable.wallet_send).mutate();
        sendDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_wallet_buttonText), PorterDuff.Mode.MULTIPLY));

        depositDrawable = context.getResources().getDrawable(R.drawable.ic_arrow_more).mutate();
        depositDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_wallet_buttonText), PorterDuff.Mode.MULTIPLY));

        for (int a = 0; a < 2; a++) {
            FrameLayout frameLayout = new FrameLayout(context);
            frameLayout.setBackgroundDrawable(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(4), Theme.getColor(Theme.key_wallet_buttonBackground), Theme.getColor(Theme.key_wallet_buttonPressedBackground)));
            addView(frameLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 42, Gravity.LEFT | Gravity.TOP, a == 0 ? 16 : 8, 168, 0, 0));
            frameLayout.setOnClickListener(v -> {
                if (v == withDrawButton) {
                    onWithDrawPressed();
                } else if(v == sendButton){
                    onSendPressed();
                }else{
                    onDepositPressed();
                }
            });
            SimpleTextView buttonTextView = new SimpleTextView(context);
            buttonTextView.setTextColor(Theme.getColor(Theme.key_wallet_buttonText));
            buttonTextView.setTextSize(14);
            buttonTextView.setDrawablePadding(AndroidUtilities.dp(6));
            buttonTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            buttonTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));

            if(a == 0){
                buttonTextView.setText(LocaleController.getString("WalletSend", R.string.WalletSend));
                sendTextView = buttonTextView;
                sendButton = frameLayout;
            }else if(a == 1){
                buttonTextView.setText(LocaleController.getString("WalletDeposit", R.string.WalletDeposit));
                sendTextView = buttonTextView;
                depositButton = frameLayout;
            }else{
//                buttonTextView.setText(LocaleController.getString("WalletWithdraw", R.string.WalletWithdraw));
//                receiveTextView = buttonTextView;
//                withDrawButton = frameLayout;
            }
            frameLayout.addView(buttonTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int buttonWidth = (width - AndroidUtilities.dp(48)) / 2;

        LayoutParams layoutParams = (LayoutParams) sendButton.getLayoutParams();
        layoutParams.width = buttonWidth;

        layoutParams = (LayoutParams) depositButton.getLayoutParams();
        layoutParams.width = buttonWidth;
        layoutParams.leftMargin = AndroidUtilities.dp(16 + 8) + buttonWidth;

//        layoutParams = (LayoutParams) withDrawButton.getLayoutParams();
//        layoutParams.width = buttonWidth;
//        layoutParams.leftMargin = AndroidUtilities.dp(16 + 8 +8 ) + 2 * buttonWidth;

        super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(236 + 6), MeasureSpec.EXACTLY));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        gemDrawable.stop();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        gemDrawable.start();
    }


    public void setInfo(String info){
        yourBalanceTextView.setVisibility(VISIBLE);
        yourBalanceTextView.setText(info);
    }

    public void setBalance(double balance){
        if(balance >= 0){
            SpannableStringBuilder stringBuilder = new SpannableStringBuilder(WalletUtils.formatCurrencyEmpty(balance));
            valueTextView.setText(stringBuilder);
            valueTextView.setTranslationX(0);
            yourBalanceTextView.setVisibility(VISIBLE);
            yourBalanceTextView.setText("Your balance");
        }else{
            valueTextView.setText("");
            valueTextView.setTranslationX(-AndroidUtilities.dp(4));
            yourBalanceTextView.setVisibility(GONE);
        }


    }

    protected void onWithDrawPressed() {

    }

    protected void onSendPressed() {

    }

    protected void onDepositPressed(){

    }
}
