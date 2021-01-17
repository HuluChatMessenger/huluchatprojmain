package org.plus.apps;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.LayoutHelper;

public class BottomCard extends CardView {

    public interface  BottomCardDelegate{
        void onItemClicked(int id);
    }

    private BottomCardDelegate delegate;

    public void setDelegate(BottomCardDelegate delegate) {
        this.delegate = delegate;
    }

    public String [] name = {"Shop","Ride","Wallet","More"};
    public int res[]  = {R.drawable.ic_baseline_shopping,R.drawable.ic_ride,R.drawable.ic_wallet,R.drawable.ic_apps};
    public String icon_colors[]  = {Theme.key_chat_attachGalleryIcon,Theme.key_chat_attachLocationIcon,Theme.key_chat_attachAudioIcon,Theme.key_chat_attachLocationIcon};
    public String background_colors[]  = {Theme.key_chat_attachGalleryBackground,Theme.key_chat_attachLocationBackground,Theme.key_chat_attachAudioBackground,Theme.key_chat_attachLocationBackground};

    private LinearLayout container;

    public BottomCard(Context context) {
        super(context);
//        Drawable  shadowDrawable = context.getResources().getDrawable(R.drawable.sheet_shadow_round).mutate();
//        shadowDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogBackground), PorterDuff.Mode.MULTIPLY));
//        Rect padding = new Rect();
//        shadowDrawable.getPadding(padding);
//        View shadow = new View(context) {
//
//            private RectF rect = new RectF();
//
//            @Override
//            protected void onDraw(Canvas canvas) {
//                shadowDrawable.setBounds(-padding.left, 0, getMeasuredWidth() + padding.right, getMeasuredHeight());
//                shadowDrawable.draw(canvas);
//
//                int w = AndroidUtilities.dp(36);
//                int y = padding.top + AndroidUtilities.dp(10);
//                rect.set((getMeasuredWidth() - w) / 2, y, (getMeasuredWidth() + w) / 2, y + AndroidUtilities.dp(4));
//                int color = Theme.getColor(Theme.key_sheet_scrollUp);
//                int alpha = Color.alpha(color);
//                Theme.dialogs_onlineCirclePaint.setColor(color);
//                canvas.drawRoundRect(rect, AndroidUtilities.dp(2), AndroidUtilities.dp(2), Theme.dialogs_onlineCirclePaint);
//
//            }
//        };
//        FrameLayout.LayoutParams layoutParams;
//        layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, AndroidUtilities.dp(21) + padding.top);
//        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;

       // addView(shadow,layoutParams);

        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setWeightSum(name.length);
        for (int i = 0; i < name.length; i++) {
            FrameLayout holder = new FrameLayout(context);
            holder.setTag(i);
            holder.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int tag = (Integer) v.getTag();
                    if(delegate != null){
                        delegate.onItemClicked(tag);
                    }
                }
            });
            AttachButton attachButton  = new AttachButton(context);
            attachButton.setTextAndIcon(i,name[i], res[i],icon_colors[i],background_colors[i]);
            holder.addView(attachButton,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.MATCH_PARENT,Gravity.CENTER));
            container.addView(holder,LayoutHelper.createLinear(0,LayoutHelper.MATCH_PARENT,1f,Gravity.TOP|Gravity.LEFT,8,32,8,4));
        }

        addView(container,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,Gravity.LEFT|Gravity.BOTTOM,32,16,32,8));
    }

    private int attachItemSize = AndroidUtilities.dp(85);

    private class AttachButton extends LinearLayout {

        private TextView textView;
        private ImageView imageView;

        public AttachButton(Context context) {
            super(context);
           setOrientation(VERTICAL);


            imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.CENTER);
            addView(imageView, LayoutHelper.createLinear(42, 42, Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 4, 0, 8));

            textView = new TextView(context);
            textView.setMaxLines(2);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
            textView.setLineSpacing(-AndroidUtilities.dp(2), 1.0f);
            addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 0, 0, 0, 16));
        }


//
//        @Override
//        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//            super.onMeasure(MeasureSpec.makeMeasureSpec(attachItemSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(84), MeasureSpec.EXACTLY));
//        }


        public void setTextAndIcon(int id, CharSequence text, int res,String iconColor,String backgroundColor) {
            textView.setText(text);
            CombinedDrawable menuButton = Theme.createCircleDrawableWithIcon(AndroidUtilities.dp(42), res);
            menuButton.setIconSize(AndroidUtilities.dp(24), AndroidUtilities.dp(24));
            Theme.setCombinedDrawableColor(menuButton, Theme.getColor(backgroundColor), false);
            Theme.setCombinedDrawableColor(menuButton, Theme.getColor(iconColor), true);
            imageView.setImageDrawable(menuButton);
        }

        @Override
        public boolean hasOverlappingRendering() {
            return false;
        }
    }

}
