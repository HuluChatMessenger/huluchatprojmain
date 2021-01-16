package org.plus.apps.business.ui.components;/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */


import android.content.Context;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.plus.net.APIError;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RLottieImageView;

import java.util.ArrayList;

@SuppressWarnings("FieldCanBeLocal")
public class ShopsEmptyCell extends LinearLayout {


    public static final int TYPE_RETRY = 60;
    public static final int TYPE_CUSTOM = 69;
    public static final int TYPE_FEED = 70;

    private RLottieImageView imageView;
    private TextView emptyTextView1;
    private TextView emptyTextView2;
    private int currentType;
    private TextView retryButton;

    private int currentAccount = UserConfig.selectedAccount;

    public ShopsEmptyCell(Context context) {
        super(context);

        setGravity(Gravity.CENTER);
        setOrientation(VERTICAL);
        setOnTouchListener((v, event) -> true);

        imageView = new RLottieImageView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        addView(imageView, LayoutHelper.createFrame(100, 100, Gravity.CENTER, 52, 4, 52, 0));
        imageView.setOnClickListener(v -> {
            if (!imageView.isPlaying()) {
                imageView.setProgress(0.0f);
                imageView.playAnimation();
            }
        });

        emptyTextView1 = new TextView(context);
        emptyTextView1.setTextColor(Theme.getColor(Theme.key_chats_nameMessage_threeLines));
        emptyTextView1.setText(LocaleController.getString("NoChats", R.string.NoChats));
        emptyTextView1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        emptyTextView1.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        emptyTextView1.setGravity(Gravity.CENTER);
        addView(emptyTextView1, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.LEFT, 52, 10, 52, 0));

        emptyTextView2 = new TextView(context);
        String help = LocaleController.getString("NoChatsHelp", R.string.NoChatsHelp);
        if (AndroidUtilities.isTablet() && !AndroidUtilities.isSmallTablet()) {
            help = help.replace('\n', ' ');
        }
        emptyTextView2.setText(help);
        emptyTextView2.setTextColor(Theme.getColor(Theme.key_chats_message));
        emptyTextView2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        emptyTextView2.setGravity(Gravity.CENTER);
        emptyTextView2.setLineSpacing(AndroidUtilities.dp(2), 1);
        addView(emptyTextView2, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.LEFT, 52, 7, 52, 0));


        retryButton = new TextView(context);
        retryButton.setText("Retry");
        retryButton.setVisibility(GONE);
        retryButton.setTextColor(Theme.getColor(Theme.key_passport_authorizeText));
        retryButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        retryButton.setGravity(Gravity.CENTER);
        retryButton.setBackgroundDrawable(Theme.createSelectorWithBackgroundDrawable(Theme.getColor(Theme.key_passport_authorizeBackground), Theme.getColor(Theme.key_passport_authorizeBackgroundSelected)));
        retryButton.setPadding(AndroidUtilities.dp(16),AndroidUtilities.dp(8),AndroidUtilities.dp(16),AndroidUtilities.dp(8));
        retryButton.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        addView(retryButton, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.LEFT, 52, 16, 52, 0));

    }


    public void setRetryListener(OnClickListener listener){
        retryButton.setOnClickListener(listener);
    }


    public void setApiError(APIError apiError){
        imageView.setVisibility(GONE);
        emptyTextView1.setText("Unexpected Error!");
        emptyTextView1.setText(apiError.message());
        retryButton.setVisibility(GONE);



    }

    public void setType(int value) {
        if (currentType == value) {
            return;
        }
        currentType = value;
        String help;
        int icon;
        if (currentType == 4) {
            imageView.setAutoRepeat(true);
            icon = R.raw.tsv_setup_monkey_tracking;
            help = "no result matching with your criteria";
            emptyTextView1.setText("No product found!");
        } else if (currentType == TYPE_RETRY) {
            imageView.setAutoRepeat(true);
            icon = R.raw.tsv_setup_monkey_tracking;
            help = "Check your internet connection!";
            emptyTextView1.setText("Connection issues!");
            retryButton.setVisibility(VISIBLE);
        } else if (currentType == 14) {
            imageView.setAutoRepeat(true);
            icon = R.raw.filter_no_chats;
            help = "no one has offered yo yet,all ofers offerd by the users will be displayed here";
            emptyTextView1.setText("no  offers!");
            retryButton.setVisibility(GONE);

        }else if (currentType == TYPE_FEED){

            imageView.setAutoRepeat(false);
            icon = R.raw.filter_no_chats;
            emptyTextView1.setText("No feed yet!");
            help = "Come back later , we will prepare your feed soon!";


        }else {
            imageView.setAutoRepeat(true);
            icon = R.raw.filter_new;
            help = LocaleController.getString("FilterAddingChatsInfo", R.string.FilterAddingChatsInfo);
            emptyTextView1.setText(LocaleController.getString("FilterAddingChats", R.string.FilterAddingChats));
        }

        if (icon != 0) {
            imageView.setVisibility(VISIBLE);
            imageView.setAnimation(icon, 100, 100);
            imageView.playAnimation();
        } else {
            imageView.setVisibility(GONE);
        }
        if (AndroidUtilities.isTablet() && !AndroidUtilities.isSmallTablet()) {
            help = help.replace('\n', ' ');
        }
        emptyTextView2.setText(help);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        updateLayout();
    }

    @Override
    public void offsetTopAndBottom(int offset) {
        super.offsetTopAndBottom(offset);
        updateLayout();
    }

    public void updateLayout() {
        if (getParent() instanceof View && (currentType == 2 || currentType == 3)) {
            View view = (View) getParent();
            int paddingTop = view.getPaddingTop();
            if (paddingTop != 0) {
                int offset = -(getTop() / 2);
                imageView.setTranslationY(offset);
                emptyTextView1.setTranslationY(offset);
                emptyTextView2.setTranslationY(offset);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int totalHeight;
        if (getParent() instanceof View) {
            View view = (View) getParent();
            totalHeight = view.getMeasuredHeight();
            if (view.getPaddingTop() != 0 && Build.VERSION.SDK_INT >= 21) {
                totalHeight -= AndroidUtilities.statusBarHeight;
            }
        } else {
            totalHeight = MeasureSpec.getSize(heightMeasureSpec);
        }
        if (totalHeight == 0) {
            totalHeight = AndroidUtilities.displaySize.y - ActionBar.getCurrentActionBarHeight() - (Build.VERSION.SDK_INT >= 21 ? AndroidUtilities.statusBarHeight : 0);
        }
        if (currentType == 0 || currentType == 2 || currentType == 3) {
            ArrayList<TLRPC.RecentMeUrl> arrayList = MessagesController.getInstance(currentAccount).hintDialogs;
            if (!arrayList.isEmpty()) {
                totalHeight -= AndroidUtilities.dp(72) * arrayList.size() + arrayList.size() - 1 + AndroidUtilities.dp(12 + 38);
            }
            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(totalHeight, MeasureSpec.EXACTLY));
        }else if(currentType == TYPE_RETRY){
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(totalHeight, MeasureSpec.EXACTLY));
        } else {
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(166), MeasureSpec.EXACTLY));
        }
    }
}
