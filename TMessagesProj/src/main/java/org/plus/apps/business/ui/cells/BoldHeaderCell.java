package org.plus.apps.business.ui.cells;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

import java.util.ArrayList;

public class BoldHeaderCell extends FrameLayout {

    private TextView textView;
    private SimpleTextView textView2;
    private int height = 40;


    public BoldHeaderCell(Context context) {
        this(context, Theme.key_dialogTextBlack, 21, 15, false);
    }

    public BoldHeaderCell(Context context,int padding) {
        this(context, Theme.key_dialogTextBlack, padding, 15, false);
    }

    public BoldHeaderCell(Context context,String textColorKey, int padding, int topMargin, boolean text2) {
        super(context);

        textView = new TextView(getContext());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 21);
        textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
        textView.setMinHeight(AndroidUtilities.dp(height - topMargin));
        textView.setTextColor(Theme.getColor(textColorKey));
        textView.setTag(textColorKey);
        addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, padding, topMargin, padding, 0));

        if (text2) {
            textView2 = new SimpleTextView(getContext());
            textView2.setTextSize(13);
            textView2.setGravity((LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.TOP);
            addView(textView2, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.TOP, padding, 21, padding, 0));
        }
    }

    public void setHeight(int value) {
        textView.setMinHeight(AndroidUtilities.dp(height = value) - ((LayoutParams) textView.getLayoutParams()).topMargin);
    }

    public void setEnabled(boolean value, ArrayList<Animator> animators) {
        if (animators != null) {
            animators.add(ObjectAnimator.ofFloat(textView, "alpha", value ? 1.0f : 0.5f));
        } else {
            textView.setAlpha(value ? 1.0f : 0.5f);
        }
    }

    public void setText(CharSequence text) {
        textView.setText(text);
    }

    public void setText2(CharSequence text) {
        if (textView2 == null) {
            return;
        }
        textView2.setText(text);
    }

    public TextView getTextView() {
        return textView;
    }

    public SimpleTextView getTextView2() {
        return textView2;
    }

}
