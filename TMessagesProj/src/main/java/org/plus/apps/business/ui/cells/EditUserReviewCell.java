package org.plus.apps.business.ui.cells;

import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.plus.apps.business.data.ShopDataSerializer;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

public class EditUserReviewCell extends LinearLayout {


    public interface EditUserReviewCellDelegate{
        void showDeleteOption(ShopDataSerializer.Review review);
        void showEditReview(ShopDataSerializer.Review review);
    }

    private ShopDataSerializer.Review review;

    private EditUserReviewCellDelegate editUserReviewCellDelegate;

    public void setEditUserReviewCellDelegate(EditUserReviewCellDelegate editUserReviewCellDelegate) {
        this.editUserReviewCellDelegate = editUserReviewCellDelegate;
    }

    private TextView headerTextView;
    private UserReviewCell userReviewCell;
    private SimpleTextView editReviewButton;

    public EditUserReviewCell(Context context) {
        super(context);

        setOrientation(VERTICAL);

       headerTextView = new TextView(getContext());
       headerTextView.setLines(1);
       headerTextView.setSingleLine(true);
       headerTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
       headerTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
       headerTextView.setTypeface(AndroidUtilities.getTypeface("fonts/Roboto-Black.ttf"));
       headerTextView.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(4), AndroidUtilities.dp(4), AndroidUtilities.dp(4));
       headerTextView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
       headerTextView.setGravity(Gravity.CENTER_VERTICAL);
       addView(headerTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT,Gravity.LEFT|Gravity.TOP ,0,0,0,0));


        userReviewCell = new UserReviewCell(context);
        addView(userReviewCell, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT,Gravity.LEFT|Gravity.TOP ,8,0,0,0));


        editReviewButton = new SimpleTextView(context);
        editReviewButton.setTextSize(13);
        editReviewButton.setGravity(Gravity.LEFT);
        editReviewButton.setText("Edit your review");
        editReviewButton.setTextColor(Theme.getColor(Theme.key_dialogTextBlue));
        addView(editReviewButton, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT, Gravity.LEFT|Gravity.TOP,8,16,8,8));

        editReviewButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editUserReviewCellDelegate != null){
                    editUserReviewCellDelegate.showEditReview(review);
                }

            }
        });

        userReviewCell.setOnOptionsClick(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editUserReviewCellDelegate != null){
                    editUserReviewCellDelegate.showDeleteOption(review);
                }
            }
        });


    }


    public void setData(ShopDataSerializer.Review review){
        this.review =review;
        headerTextView.setText("Your Review");
        userReviewCell.setReview(review);
    }


}
