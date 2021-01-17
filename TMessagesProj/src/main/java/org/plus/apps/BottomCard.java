package org.plus.apps;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.ColorUtils;

import org.plus.features.PlusTheme;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RLottieImageView;

public class BottomCard extends CardView {


    public String [] name = {"Scan","Shop","Ride","Wallet","More"};
    public int res[]  = {R.drawable.wallet_qr,R.drawable.ic_store,R.drawable.ic_ride,R.drawable.menu_wallet,R.drawable.ic_ab_other};

    private LinearLayout container;

    public BottomCard(Context context) {
        super(context);

        setCardElevation(12);
        setCardBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        setRadius(16);

        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setWeightSum(name.length);

        for (int i = 0; i < name.length; i++) {
            FrameLayout holder = new FrameLayout(context);
            holder.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            AttachButton attachButton  = new AttachButton(context);
            attachButton.setTextAndIcon(i,name[i], res[i]);
            holder.addView(attachButton,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.MATCH_PARENT,Gravity.CENTER));
            container.addView(holder,LayoutHelper.createLinear(0,LayoutHelper.MATCH_PARENT,1f,Gravity.TOP|Gravity.LEFT,0,0,0,0));
        }

        addView(container,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT));
    }



    private int attachItemSize = AndroidUtilities.dp(85);

    private class AttachButton extends FrameLayout {

        private TextView textView;
        private ImageView imageView;

        public AttachButton(Context context) {
            super(context);
            setWillNotDraw(false);

            imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.CENTER);
            addView(imageView, LayoutHelper.createFrame(32, 32, Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 16, 0, 8));

            textView = new TextView(context);
            textView.setMaxLines(2);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setTypeface(AndroidUtilities.getTypeface("fonts/mw_bold.ttf"));
            textView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            textView.setLineSpacing(-AndroidUtilities.dp(2), 1.0f);
            addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 0, 62, 0, 0));
        }



        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(attachItemSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(84), MeasureSpec.EXACTLY));
        }


        public void setTextAndIcon(int id, CharSequence text, int res) {
            textView.setText(text);
            CombinedDrawable menuButton = Theme.createCircleDrawableWithIcon(AndroidUtilities.dp(32), res);
            menuButton.setIconSize(AndroidUtilities.dp(24), AndroidUtilities.dp(24));
            Theme.setCombinedDrawableColor(menuButton, Theme.getColor(Theme.key_actionBarDefault), false);
            Theme.setCombinedDrawableColor(menuButton, Theme.getColor(Theme.key_dialogTextBlack), true);
            imageView.setImageDrawable(menuButton);
        }

        @Override
        public boolean hasOverlappingRendering() {
            return false;
        }
    }

}
