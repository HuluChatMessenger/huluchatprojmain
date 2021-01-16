package org.plus.apps.business.ui.components;

import android.app.Activity;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

public class ShopAlertCreator {

    public static AlertDialog.Builder  showInfo(Activity parentActivity,String info) {
        if (parentActivity == null) {
            return null;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity);
        builder.setTopImage(R.drawable.menu_info, Theme.getColor(Theme.key_dialogTopBackground));
        builder.setTitle("Info");
        builder.setMessage(info);
        return builder;
    }
}
