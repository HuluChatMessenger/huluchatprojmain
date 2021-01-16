package org.plus.apps.business.ui.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Parcelable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import org.plus.apps.business.data.ShopDataSerializer;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BottomPagesView;
import org.telegram.ui.Components.LayoutHelper;

import java.util.ArrayList;

@SuppressLint("ViewConstructor")
public class ProductPagerCell extends FrameLayout {

    private BottomPagesView bottomPages;
    private ViewPager viewPager;
    private ArrayList<ShopDataSerializer.ProductImage> productImages;

    private static final int height = AndroidUtilities.displaySize.y /2;

    private final GradientDrawable topOverlayGradient;
    private final GradientDrawable bottomOverlayGradient;


    public ProductPagerCell(Context context, ArrayList<ShopDataSerializer.ProductImage> images) {
        super(context);

        setWillNotDraw(false);

        topOverlayGradient = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] {0x42000000, 0});
        topOverlayGradient.setShape(GradientDrawable.RECTANGLE);

        bottomOverlayGradient = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[] {0x42000000, 0});
        bottomOverlayGradient.setShape(GradientDrawable.RECTANGLE);


        productImages = images;
        viewPager = new ViewPager(context) {
            @Override
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                return super.onInterceptTouchEvent(ev);
            }

            @Override
            protected void onAttachedToWindow() {
                super.onAttachedToWindow();
                requestLayout();
            }
        };
        AndroidUtilities.setViewPagerEdgeEffectColor(viewPager, Theme.getColor(Theme.key_actionBarDefaultArchived));
        viewPager.setAdapter(new Adapter());
        viewPager.setPageMargin(0);
        viewPager.setOffscreenPageLimit(1);
        addView(viewPager, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                bottomPages.setPageOffset(position, positionOffset);
            }

            @Override
            public void onPageSelected(int i) {
                FileLog.d("test1");
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                FileLog.d("test1");
            }
        });


        TextView countTextView = new TextView(context);
        countTextView.setSingleLine();
        countTextView.setMaxLines(1);
        countTextView.setPadding(AndroidUtilities.dp(8),AndroidUtilities.dp(8),AndroidUtilities.dp(8),AndroidUtilities.dp(8));
        countTextView.setText("1 / " + productImages.size());
        countTextView.setTextColor(Color.WHITE);
        countTextView.setTextSize(14);
        countTextView.setGravity(Gravity.LEFT);
        countTextView.setBackgroundDrawable(Theme.createCircleDrawable( 0,0x42000000));


        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                countTextView.setText((position + 1) + " / " + productImages.size());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });



        View shadowView = new View(context);
        shadowView.setBackground(bottomOverlayGradient);
        addView(shadowView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, (float)( height*0.2),Gravity.LEFT | Gravity.BOTTOM , 0, 0, 0, 0));

        View topShadow = new View(context);
        topShadow.setBackground(topOverlayGradient);
        addView(topShadow, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT,  (float)( height*0.2),Gravity.LEFT | Gravity.TOP , 0, 0, 0, 0));



        addView(countTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT,Gravity.RIGHT | Gravity.BOTTOM , 0, 0, 8, 8));


        bottomPages = new BottomPagesView(context, viewPager, images.size());
        bottomPages.setColor(Theme.key_chats_unreadCounterMuted, Theme.key_chats_actionBackground);
        addView(bottomPages, LayoutHelper.createFrame(33, 5, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0, 0, 19));

        if(productImages.size() == 1){
            bottomPages.setVisibility(GONE);
        }

    }


    @Override
    public void invalidate() {
        super.invalidate();
        bottomPages.invalidate();
    }


    public ViewPager getViewPager() {
        return viewPager;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    private class Adapter extends PagerAdapter {
        @Override
        public int getCount() {
            return productImages != null?productImages.size():0;
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ProductPagerInnerCell innerCell = new ProductPagerInnerCell(container.getContext(), productImages.get(position).photo);
            if (innerCell.getParent() != null) {
                ViewGroup parent = (ViewGroup) innerCell.getParent();
                parent.removeView(innerCell);
            }
            container.addView(innerCell, 0);
            return innerCell;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            bottomPages.setCurrentPage(position);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            if (observer != null) {
                super.unregisterDataSetObserver(observer);
            }
        }
    }


}
