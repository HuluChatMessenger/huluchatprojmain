package org.plus.apps.business.ui.cells;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.plus.apps.business.ShopUtils;
import org.plus.apps.business.data.ShopDataSerializer;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;

public class ShopReviewCell extends FrameLayout{

    private BackupImageView avatarImageView;
    private TextView nameTextView;
    private TextView commentTextView;
    private RatingView rateTextView;
    private SimpleTextView dateTextView;

    private boolean needDivider;



    public ShopReviewCell(Context context) {
        super(context);

        setPadding(0,AndroidUtilities.dp(30),0,AndroidUtilities.dp(30));


        avatarImageView = new BackupImageView(context);
        avatarImageView.setRoundRadius(16);
        addView(avatarImageView, LayoutHelper.createFrame(32,32, Gravity.LEFT|Gravity.TOP,16,0,0,0));


        nameTextView = new TextView(context);
        nameTextView.setSingleLine();
        nameTextView.setMaxLines(1);
        nameTextView.setEllipsize(TextUtils.TruncateAt.END);
        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,14);
        nameTextView.setGravity(Gravity.LEFT);
        nameTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT,16, Gravity.LEFT|Gravity.TOP,32 + 16 + 12,0,60,0));


        rateTextView = new RatingView(context);
        addView(rateTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.RIGHT | Gravity.TOP, 0, 0, 8, 0));

        dateTextView = new SimpleTextView(context);
        dateTextView.setTextSize(13);
        dateTextView.setGravity(Gravity.LEFT);
        dateTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText4));
        addView(dateTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,16, Gravity.LEFT|Gravity.TOP,32 + 16 + 12,16 + 16,8,0));


        commentTextView = new TextView(context);
        commentTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        commentTextView.setMaxLines(3);
        commentTextView.setGravity(Gravity.LEFT);
        commentTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        commentTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,16);
        commentTextView.setEllipsize(TextUtils.TruncateAt.END);
        addView(commentTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT, Gravity.LEFT|Gravity.TOP,32 + 16 + 12,16 + 16 + 20,8,0));



    }


    public void setReview(ShopDataSerializer.Review review, boolean divider){
        if(review == null || review.user == null){
            return;
        }

        String name = review.user.first_name;
        if(TextUtils.isEmpty(name)){
            name = review.user.username;
        }


        if(TextUtils.isEmpty(name)){
            name = "user " + review.user.telegramId;
        }


        needDivider = divider;
        nameTextView.setText(name);
        commentTextView.setText(review.comment);
        rateTextView.setRating(review.rating);
        AvatarDrawable avatarDrawable = new AvatarDrawable();
        avatarDrawable.setInfo(4,name,null);
        avatarImageView.setImage(null,null,avatarDrawable);
        dateTextView.setText(ShopUtils.formatLast(review.created_at));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (needDivider) {
            canvas.drawLine(0, getMeasuredHeight() - 1, getMeasuredWidth() , getMeasuredHeight() - 1, Theme.dividerPaint);
        }
    }


    public static class RatingView extends View {

        private Bitmap filledStar, hollowStar;
        private Paint paint = new Paint();
        private int numStars = 5;
        private int selectedRating = 0;

        private int size = 16;

        public RatingView(Context context) {
            super(context);
            filledStar = BitmapFactory.decodeResource(getResources(), R.drawable.ic_rating_star_filled).extractAlpha();
            hollowStar = BitmapFactory.decodeResource(getResources(), R.drawable.ic_rating_star).extractAlpha();

            filledStar = Bitmap.createScaledBitmap(filledStar,AndroidUtilities.dp(16),AndroidUtilities.dp(16),false);
            hollowStar = Bitmap.createScaledBitmap(hollowStar,AndroidUtilities.dp(16),AndroidUtilities.dp(16),false);

        }

        public void setRating(int count){
            selectedRating = count;
            invalidate();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(numStars * AndroidUtilities.dp(size) + (numStars - 1) * AndroidUtilities.dp(16), AndroidUtilities.dp(size));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            for (int i = 0; i < numStars; i++) {
                paint.setColor(Theme.getColor(i < selectedRating ? Theme.key_dialogTextBlue : Theme.key_dialogTextHint));
                canvas.drawBitmap(i < selectedRating ? filledStar : hollowStar, i * AndroidUtilities.dp(size + 4), 0, paint);
            }
        }

        public int getRating() {
            return selectedRating;
        }


    }




}