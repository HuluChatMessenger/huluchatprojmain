package org.plus.apps.business.ui.cells;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

public class Ui_info_bold_cell extends LinearLayout {

    private TextView titleView;
    private ImageView imageView;

    public Ui_info_bold_cell(Context context) {
        super(context);

        setOrientation(HORIZONTAL);

        titleView = new TextView(getContext());
        titleView.setLines(1);
        titleView.setSingleLine(true);
        titleView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        titleView.setTypeface(AndroidUtilities.getTypeface("fonts/Roboto-Black.ttf"));
        titleView.setPadding(AndroidUtilities.dp(21), AndroidUtilities.dp(0), AndroidUtilities.dp(22), AndroidUtilities.dp(0));
        titleView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        titleView.setGravity(Gravity.CENTER_VERTICAL);
        addView(titleView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 60,Gravity.LEFT|Gravity.CENTER_VERTICAL ,0,0,0,0));


        imageView = new ImageView(context);
        imageView.setVisibility(GONE);
        imageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogFloatingButton), PorterDuff.Mode.MULTIPLY));
        imageView.setImageResource(R.drawable.menu_info);
        imageView.setPadding(AndroidUtilities.dp(16),AndroidUtilities.dp(16),AndroidUtilities.dp(16),AndroidUtilities.dp(16));
        addView(imageView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT,LayoutHelper.WRAP_CONTENT,Gravity.LEFT|Gravity.CENTER_VERTICAL ,0,0,0,0));


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(60), MeasureSpec.EXACTLY));
    }


    public void showInfo(boolean show){
        imageView.setVisibility(show?VISIBLE:GONE);
    }

    public void setOnclick(OnClickListener onclick){
        imageView.setOnClickListener(onclick);
    }

    public TextView getTitleView() {
        return titleView;
    }

    public void setText(String text) {
        titleView.setText(text);
    }
}
