package org.plus.apps.business.ui.cells;
import android.content.Context;
import android.widget.FrameLayout;

import org.plus.apps.business.ShopUtils;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;

public class Ui_chooser_cell extends FrameLayout {


    private TextSettingsCell textSettingsCell;

    public Ui_chooser_cell(Context context) {
        this(context, 21);
    }

    public Ui_chooser_cell(Context context, int padding) {
        super(context);

        textSettingsCell = new TextSettingsCell(context,16);
        addView(textSettingsCell,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,padding,0,10
        ,10));
        textSettingsCell.setBackground(Theme.createSimpleSelectorRoundRectDrawable(20,Theme.getColor(Theme.key_windowBackgroundGray), ShopUtils.getIntAlphaColor(Theme.getColor(Theme.key_windowBackgroundGray),0.5f)));

    }

    public TextSettingsCell getTextSettingsCell() {
        return textSettingsCell;
    }
}
