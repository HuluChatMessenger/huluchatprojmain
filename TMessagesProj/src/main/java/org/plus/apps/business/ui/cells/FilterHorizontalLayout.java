package org.plus.apps.business.ui.cells;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.plus.apps.business.ShopUtils;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

public  class FilterHorizontalLayout extends LinearLayout {

        private TextView filterTextView;
        private TextView categoryTextView;

        public FilterHorizontalLayout(Context context) {
            super(context);

            setOrientation(HORIZONTAL);

            filterTextView = new TextView(context);
            filterTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
            filterTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            filterTextView.setTypeface(AndroidUtilities.getTypeface("fonts/Roboto-Black.ttf"));
            filterTextView.setLines(1);
            filterTextView.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(4), AndroidUtilities.dp(8), AndroidUtilities.dp(4));
            filterTextView.setMaxLines(1);
            filterTextView.setText("Sort");
            filterTextView.setSingleLine(true);
            filterTextView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            filterTextView.setCompoundDrawablePadding(AndroidUtilities.dp(8));
            addView(filterTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 4, 16, 8, 16));
            Drawable drawable = getResources().getDrawable(R.drawable.contacts_sort_name).mutate();
            if (drawable != null) {
                drawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextBlack), PorterDuff.Mode.MULTIPLY));
            }
            filterTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);

            filterTextView.setBackground(ShopUtils.createRoundStrokeDrwable(8, 3, Theme.key_windowBackgroundGray, Theme.key_windowBackgroundWhite));


            categoryTextView = new TextView(context);
            categoryTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
            categoryTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            categoryTextView.setTypeface(AndroidUtilities.getTypeface("fonts/Roboto-Black.ttf"));
            categoryTextView.setLines(1);
            categoryTextView.setMaxLines(1);
            categoryTextView.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8));
            categoryTextView.setSingleLine(true);
            categoryTextView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            addView(categoryTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 8, 16, 0, 16));
            setText("All Categories");
            categoryTextView.setBackground(ShopUtils.createRoundStrokeDrwable(8, 3, Theme.key_windowBackgroundGray, Theme.key_windowBackgroundWhite));


        }

        public void onSortClick(OnClickListener onClickListener) {
            filterTextView.setOnClickListener(onClickListener);
        }


        public void onFilterClick(OnClickListener onClickListener) {
            categoryTextView.setOnClickListener(onClickListener);
        }

        public void setText(String text) {
            if (text == null) {
                return;
            }
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
            spannableStringBuilder.append("filter in: ");
            int end = spannableStringBuilder.length();
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Theme.getColor(Theme.key_dialogTextGray2)), 0, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.setSpan(new StyleSpan(Typeface.NORMAL), 0, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.append(text);
            categoryTextView.setText(spannableStringBuilder);
        }
    }
