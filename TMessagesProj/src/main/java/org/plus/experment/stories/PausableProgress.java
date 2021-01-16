package org.plus.experment.stories;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.FrameLayout;

import org.telegram.ui.Components.LayoutHelper;


public class PausableProgress extends FrameLayout {

    public View back_progress;
    public View front_progress;
    public View max_progress;

    public PausableProgress(Context context) {
        super(context);

        back_progress = new View(context);
        back_progress.setBackgroundColor(Color.parseColor("#8affffff"));
        addView(back_progress, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,2));

        front_progress = new View(context);
        front_progress.setVisibility(INVISIBLE);
        front_progress.setBackgroundColor(Color.WHITE);
        addView(front_progress, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,2));

        max_progress = new View(context);
        max_progress.setVisibility(GONE);
        max_progress.setBackgroundColor(Color.WHITE);
        addView(max_progress, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,2));
    }
}
