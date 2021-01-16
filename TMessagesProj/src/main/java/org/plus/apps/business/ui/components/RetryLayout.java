package org.plus.apps.business.ui.components;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RLottieImageView;
import org.w3c.dom.Text;


public class RetryLayout extends LinearLayout {

    private RLottieImageView imageView;
    private TextView textView;
    private TextView infoTextView;
    private TextView retryButton;

    public RetryLayout(Context context) {
        super(context);


        imageView = new RLottieImageView(context);
        imageView.setAutoRepeat(true);
        imageView.setAnimation(R.raw.voip_connecting, 120, 120);
        imageView.playAnimation();
        addView(imageView, LayoutHelper.createLinear(120, 120, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 20));

        textView = new TextView(context);
        textView.setTextColor(Theme.getColor(Theme.key_chats_nameMessage_threeLines));
        textView.setText("No internet connection!");
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        textView.setGravity(Gravity.CENTER);
        addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.LEFT, 52, 10, 52, 0));


        infoTextView = new TextView(context);
        String help = "Please check your internet connection!";
        if (AndroidUtilities.isTablet() && !AndroidUtilities.isSmallTablet()) {
            help = help.replace('\n', ' ');
        }
        infoTextView.setText(help);
        infoTextView.setTextColor(Theme.getColor(Theme.key_chats_message));
        infoTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        infoTextView.setGravity(Gravity.CENTER);
        infoTextView.setLineSpacing(AndroidUtilities.dp(2), 1);
        addView(infoTextView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.LEFT, 52, 7, 52, 16));

        retryButton = new TextView(context);
        retryButton.setCompoundDrawablePadding(AndroidUtilities.dp(8));
        retryButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.msg_retry, 0, 0, 0);
        retryButton.setTextColor(Theme.getColor(Theme.key_passport_authorizeText));
        retryButton.setText("Retry".toUpperCase());
        retryButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        retryButton.setGravity(Gravity.CENTER);
        retryButton.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        addView(retryButton, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));

    }

    public void setRetryClickListener(OnClickListener onClickListener){
        retryButton.setOnClickListener(onClickListener);
    }

}
