package org.plus.apps.business.ui.cells;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.plus.apps.business.ShopUtils;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

@SuppressLint("ViewConstructor")
public class Ui_text_tag_cell extends FrameLayout{

    private TextView filterTextView;

    public Ui_text_tag_cell(Context context, int padding) {
        super(context);
        filterTextView = new TextView(context);
        filterTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        filterTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        filterTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        filterTextView.setLines(1);
        filterTextView.setPadding(AndroidUtilities.dp(16),AndroidUtilities.dp(8),AndroidUtilities.dp(16),AndroidUtilities.dp(8));
        filterTextView.setMaxLines(1);
        filterTextView.setSingleLine(true);
        filterTextView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        addView(filterTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 8, 8, 8, 8));
        GradientDrawable shape =  new GradientDrawable();
        shape.setCornerRadius( 40);
        shape.setStroke(1,Theme.getColor(Theme.key_dialogTextBlack));
        shape.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        filterTextView.setBackground(shape);
        filterTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));

    }


    public void setText(String  text){
        filterTextView.setText(text);
    }

    public void setTagSelected(boolean selected){
         if(selected){
             GradientDrawable shape =  new GradientDrawable();
             shape.setCornerRadius( 40);
             shape.setStroke(1,Theme.getColor(Theme.key_dialogTextBlack));
             shape.setColor(ShopUtils.getIntAlphaColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader),0.3f));
             filterTextView.setBackground(shape);

         }else{
             GradientDrawable shape =  new GradientDrawable();
             shape.setCornerRadius( 40);
             shape.setStroke(1,Theme.getColor(Theme.key_dialogTextBlack));
             filterTextView.setBackground(shape);
         }
    }



//    public void setTagSelected(boolean selected){
//        Drawable drawable = filterTextView.getBackground();
//        int color;
//        if(selected){
//            color = ShopUtils.getIntAlphaColor(Theme.getColor(Theme.key_location_actionActiveIcon),1f);
//            drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
//            filterTextView.setBackgroundDrawable(drawable);
//            filterTextView.setTextColor(Theme.getColor(Theme.key_profile_actionIcon));
//        }else{
//            color =  Theme.getColor(Theme.key_windowBackgroundGray);
//            drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
//            filterTextView.setBackgroundDrawable(drawable);
//            filterTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
//
//        }
//    }

//    public void setTagSelected(boolean selected){
//        if(selected){
//            Drawable drawable = Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(20), ShopUtils.getIntAlphaColor(Theme.getColor(Theme.key_location_actionActiveIcon),1f),  ShopUtils.getIntAlphaColor(Theme.getColor(Theme.key_location_actionActiveIcon),0.1f)).mutate();
//
//            textView.setBackgroundDrawable(drawable);
//            textView.setTextColor(Theme.getColor(Theme.key_profile_actionIcon));
//        }else{
//            Drawable drawable = Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(40), Theme.getColor(Theme.key_location_actionBackground), Theme.getColor(Theme.key_location_actionPressedBackground));
//
//           // Drawable drawable = Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(20), ShopUtils.getIntAlphaColor(Theme.getColor(Theme.key_location_actionActiveIcon),0.1f),  ShopUtils.getIntAlphaColor(Theme.getColor(Theme.key_location_actionActiveIcon),0.1f)).mutate();
//            textView.setBackgroundDrawable(drawable);
//            textView.setTextColor(Theme.getColor(Theme.key_location_actionActiveIcon));
//
//           // textView.setTextColor(Theme.getColor(Theme.key_location_actionActiveIcon));
//        }
//    }


}
