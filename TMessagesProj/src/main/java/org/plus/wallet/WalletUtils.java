package org.plus.wallet;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.plus.apps.business.ShopUtils;
import org.plus.net.APIError;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RLottieDrawable;
import org.telegram.ui.Components.SvgHelper;

import java.time.Instant;

public class WalletUtils {

    public static final String transfer_url = "wallet://transfer/tg_user_id";

    public static final String CASH_IN = "cashin";
    public static final String CASH_OUT = "cashout";


    public static String formatCurrencyEmpty(double  amount){
        return amount  + " ብር";
    }

    public static  int getColorForType(String type){
        if(type.equals(CASH_IN)){
            return   Theme.getColor(Theme.key_calls_callReceivedGreenIcon);
        }
        return Theme.getColor(Theme.key_calls_callReceivedRedIcon);
    }

    public static String formatCurrencyWithSign(double  amount,String type){
        if(type.equals(CASH_IN)){
            return  "+"+ amount  + " ብር";
        }
        return  "-"+ amount  + " ብር";
    }

    public static String getTextForType(String type){
        if(type.equals(CASH_IN)){
            return  "Received";
        }
        return  "Sent";

    }

    public static boolean canTransfer(WalletModel.Wallet wallet){
        boolean send = true;
        if(wallet.payable > 0){
           send = false;
        }
        return send;
    }

    public static String getDateKey(String date){
        long time = Instant.parse(date).toEpochMilli();
        return LocaleController.formatSectionDate(time/1000);
    }

    public static String formatCurrency(double  amount){
        return amount  + " ብር";
    }



    public static Drawable[] statusDrawables = new Drawable[2];
    public static Drawable[] sentStatusDrawable = new Drawable[2];


    public static void loadStatusDrawable(){

        statusDrawables[0] = Theme.createCircleDrawableWithIcon(AndroidUtilities.dp(44), R.drawable.ic_round_arrow_upward_24).mutate();
        statusDrawables[1] = Theme.createCircleDrawableWithIcon(AndroidUtilities.dp(44), R.drawable.ic_arrow_down).mutate();

        sentStatusDrawable[0] = Theme.createCircleDrawableWithIcon(AndroidUtilities.dp(100), R.drawable.ic_baseline_check).mutate();
        sentStatusDrawable[1] = Theme.createCircleDrawableWithIcon(AndroidUtilities.dp(100), R.drawable.ic_baseline_close).mutate();

        Theme.setCombinedDrawableColor(statusDrawables[0], ShopUtils.getIntAlphaColor(Theme.getColor(Theme.key_calls_callReceivedRedIcon),0.2f), false);
        Theme.setCombinedDrawableColor(statusDrawables[0], Theme.getColor(Theme.key_calls_callReceivedRedIcon), true);
        Theme.setCombinedDrawableColor(statusDrawables[1], ShopUtils.getIntAlphaColor(Theme.getColor(Theme.key_calls_callReceivedGreenIcon),0.2f), false);
        Theme.setCombinedDrawableColor(statusDrawables[1], Theme.getColor(Theme.key_calls_callReceivedGreenIcon), true);

        Theme.setCombinedDrawableColor(sentStatusDrawable[0], ShopUtils.getIntAlphaColor(Theme.getColor(Theme.key_calls_callReceivedGreenIcon),0.2f), false);
        Theme.setCombinedDrawableColor(sentStatusDrawable[0], Theme.getColor(Theme.key_calls_callReceivedGreenIcon), true);
        Theme.setCombinedDrawableColor(sentStatusDrawable[1], ShopUtils.getIntAlphaColor(Theme.getColor(Theme.key_calls_callReceivedRedIcon),0.2f), false);
        Theme.setCombinedDrawableColor(sentStatusDrawable[1], Theme.getColor(Theme.key_calls_callReceivedRedIcon), true);


    }

    public static Drawable getDrawableForTransactionType(String type){
        if(type.equals(CASH_OUT)){
            return statusDrawables[0];
        }else{
            return statusDrawables[1];
        }
    }

    public static AlertDialog.Builder createPaymentStatusAlert(Context activity, boolean  success, APIError apiError, Runnable runnable) {
        if (activity == null) {
            return null;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LinearLayout  linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setClipToOutline(true);
        linearLayout.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight() + AndroidUtilities.dp(6), AndroidUtilities.dp(6));
            }
        });


        TextView titleView = new TextView(activity);
        titleView.setLines(1);
        titleView.setSingleLine(true);
        titleView.setText(success?"Success":"Failed");
        titleView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        titleView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        titleView.setPadding(AndroidUtilities.dp(22), AndroidUtilities.dp(15), AndroidUtilities.dp(22), AndroidUtilities.dp(8));
        titleView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        titleView.setGravity(Gravity.CENTER_HORIZONTAL);
        linearLayout.addView(titleView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 60));

        TextView nameTextView = new TextView(activity);
        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        nameTextView.setLines(1);
        nameTextView.setMaxLines(1);
        nameTextView.setText(success?"Payment sent successfully":apiError.message());
        nameTextView.setSingleLine(true);
        nameTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        nameTextView.setEllipsize(TextUtils.TruncateAt.END);
        linearLayout.addView(nameTextView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.BOTTOM, 16, 0, 16, 16));

        ImageView imageView  = new ImageView(activity);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        imageView.setImageDrawable(success?sentStatusDrawable[0]:sentStatusDrawable[1]);
        linearLayout.addView(imageView, LayoutHelper.createLinear(100, 100,Gravity.CENTER_HORIZONTAL,16,16,16,16));

        TextView acceptTextView = new TextView(activity);
        acceptTextView.setTextColor(Theme.getColor(Theme.key_passport_authorizeText));
        acceptTextView.setText(success?"Continue":"Try again");
        acceptTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        acceptTextView.setGravity(Gravity.CENTER);
        acceptTextView.setPadding(AndroidUtilities.dp(16),AndroidUtilities.dp(16),AndroidUtilities.dp(16),AndroidUtilities.dp(16));
        acceptTextView.setBackgroundDrawable(Theme.createRoundRectDrawable(8,success?Theme.getColor(Theme.key_calls_callReceivedGreenIcon):Theme.getColor(Theme.key_calls_callReceivedRedIcon)));
        acceptTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder.getDismissRunnable().run();
            }
        });
        acceptTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        linearLayout.addView(acceptTextView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT,Gravity.CENTER_HORIZONTAL,16,100,16,16));
        builder.setView(linearLayout);
        return builder;
    }

}
