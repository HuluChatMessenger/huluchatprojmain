package org.plus.apps.business.ui.cells;

import android.content.Context;
import android.widget.FrameLayout;

public class Ui_space_cell extends FrameLayout {


    private int cellHeight;

    public Ui_space_cell(Context context) {
        this(context, 8);
    }

    public Ui_space_cell(Context context, int height) {
        super(context);
        cellHeight = height;
    }

    public void setHeight(int height) {
        if (cellHeight != height) {
            cellHeight = height;
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(cellHeight, MeasureSpec.EXACTLY));
    }
}

