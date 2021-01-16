package org.plus.apps.business.ui.cells;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BetterRatingView;
import org.telegram.ui.Components.LayoutHelper;

public class ShopReviewInputCell  extends LinearLayout {


    public interface  ReviewDelegate{
        void onReview(int count);
    }

    public TextView reviewTitleView;
    public TextView reviewDescView;
    public BetterRatingView betterRatingView;
    private TextView reviewCell;


    private ReviewDelegate reviewDelegate;

    public void setReviewDelegate(ReviewDelegate reviewDelegate) {
        this.reviewDelegate = reviewDelegate;
    }


    @SuppressLint("SetTextI18n")
    public ShopReviewInputCell(@NonNull Context context) {
        super(context);


        setPadding(AndroidUtilities.dp(16),AndroidUtilities.dp(16),AndroidUtilities.dp(16),AndroidUtilities.dp(16));

        setOrientation(VERTICAL);

        reviewTitleView = new TextView(getContext());
        reviewTitleView.setLines(1);
        reviewTitleView.setSingleLine(true);
        reviewTitleView.setText("Rate this shop");
        reviewTitleView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        reviewTitleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        reviewTitleView.setTypeface(AndroidUtilities.getTypeface("fonts/Roboto-Black.ttf"));
        reviewTitleView.setEllipsize(TextUtils.TruncateAt.END);
        reviewTitleView.setGravity(Gravity.CENTER_VERTICAL|Gravity.LEFT);
        addView(reviewTitleView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT,Gravity.LEFT|Gravity.CENTER_VERTICAL ,16,0,48,0));

        reviewDescView = new TextView(getContext());
        reviewDescView.setLines(1);
        reviewDescView.setSingleLine(true);
        reviewDescView.setText("Tell others what you think");
        reviewDescView.setTextColor(Theme.getColor(Theme.key_dialogTextGray2));
        reviewDescView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        reviewDescView.setEllipsize(TextUtils.TruncateAt.END);
        reviewDescView.setGravity(Gravity.CENTER_VERTICAL|Gravity.LEFT);
        addView(reviewDescView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT,Gravity.LEFT|Gravity.CENTER_VERTICAL ,16,8,0,0));

         betterRatingView = new BetterRatingView(context);
         betterRatingView.setOnRatingChangeListener(newRating -> {
             if(reviewDelegate != null){
                 reviewDelegate.onReview(newRating);
             }
         });
         addView(betterRatingView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 16, 16 ,16, 16));


         reviewCell = new TextView(context);
         reviewCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader));
         reviewCell.setLines(1);
         reviewCell.setSingleLine(true);
         reviewCell.setText("Review");
         reviewCell.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
         reviewCell.setGravity(Gravity.CENTER_VERTICAL|Gravity.LEFT);
         addView(reviewCell, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 16, 8 ,0, 0));

         setOnClickListener(view -> {
             if(reviewDelegate != null){
                 reviewDelegate.onReview(betterRatingView.getRating());
             }
         });
    }






}
