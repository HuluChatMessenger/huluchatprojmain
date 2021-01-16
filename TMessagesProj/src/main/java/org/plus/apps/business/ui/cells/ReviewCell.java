package org.plus.apps.business.ui.cells;

import android.content.Context;
import android.graphics.Canvas;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.plus.apps.business.ShopUtils;
import org.plus.apps.business.data.ShopDataSerializer;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.MentionCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Components.LayoutHelper;

public class ReviewCell extends LinearLayout {

    public static final String start_char  = "⭐";

    private MentionCell userNameCell;
    private TextView betterRatingView;
    private TextView textView;

    private boolean needDivider;
    public ReviewCell(@NonNull Context context) {
        super(context);


        setOrientation(VERTICAL);

        userNameCell = new MentionCell(context);
        addView(userNameCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT, Gravity.LEFT|Gravity.TOP,0,8,16,0));

        betterRatingView = new TextView(context);
        betterRatingView.setTextColor(Theme.getColor(Theme.key_chats_menuItemText));
        betterRatingView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        betterRatingView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        betterRatingView.setMaxLines(3);
        betterRatingView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        betterRatingView.setEllipsize(TextUtils.TruncateAt.END);
        addView(betterRatingView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 21, 8, 16, 8));

        textView = new TextView(context);
        textView.setTextColor(Theme.getColor(Theme.key_chats_menuItemText));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        textView.setMaxLines(3);
        textView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 21, 8 , 16, 8));


        ShadowSectionCell textSettingsCell = new ShadowSectionCell(context);
        textSettingsCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        addView(textSettingsCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 0, 8 , 0, 0));


    }


    public void setReview(ShopDataSerializer.Review review,boolean divider){
        if(review == null){
            return;
        }
        needDivider = divider;
       // userNameCell.setTextAndImage(review.user);
        StringBuilder rating = new StringBuilder();
        for(int a  = 0;  a < review.rating; a++){
            rating.append(start_char + " ");
        }
        rating.append(ShopUtils.formatReviewData(review.created_at));
        betterRatingView.setText(rating.toString());
        textView.setText(review.comment);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (needDivider) {
            canvas.drawLine(LocaleController.isRTL ? 0 : AndroidUtilities.dp(20), getMeasuredHeight() - 1, getMeasuredWidth() - (LocaleController.isRTL ? AndroidUtilities.dp(20) : 0), getMeasuredHeight() - 1, Theme.dividerPaint);
        }
    }




}
