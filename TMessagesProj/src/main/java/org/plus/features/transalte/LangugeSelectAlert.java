package org.plus.features.transalte;

import android.content.Context;
import android.widget.LinearLayout;

import org.plus.features.PlusConfig;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.RadioColorCell;
import org.telegram.ui.LaunchActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class LangugeSelectAlert {

    private static class Country {
        public String name;
        public String shortname;
    }

    //chatActivity
    public static AlertDialog chooseLangchat(Context context, final Runnable onSelectRunnable) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(LocaleController.getString("ChooseYourLanguage", R.string.ChooseYourLanguage));
        final LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        builder.setView(linearLayout);

        HashMap<String, String> langList= Language.getInstance().hashLanguage;

        Map<String, String> map =  Language.getInstance().hashLanguage;
        Map<String, String> treeMap = new TreeMap<String, String>(map);

        for (String str : treeMap.keySet()) {
            Country c = new Country();
            c.shortname = str;
            c.name = map.get(c.shortname);

            RadioColorCell cell = new RadioColorCell(context);
            cell.setPadding(AndroidUtilities.dp(4), 0, AndroidUtilities.dp(4), 0);
            cell.setTag(str);
            cell.setCheckColor(Theme.getColor(Theme.key_radioBackground), Theme.getColor(Theme.key_dialogRadioBackgroundChecked));
            cell.setTextAndValue(c.name, PlusConfig.default_translate_lang.equals(c.shortname));
            linearLayout.addView(cell);
            cell.setOnClickListener(v -> {
                PlusConfig.setTranslatedLang(c.shortname);
                if (onSelectRunnable != null) {
                    onSelectRunnable.run();
                }
                builder.getDismissRunnable().run();
            });
        }

        AlertDialog dialog = builder.show();

        return dialog;

    }

    //ChatActivityEnterView
    public static AlertDialog chooseLang(Context context, LaunchActivity activity, final Runnable onSelectRunnable) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(LocaleController.getString("ChooseYourLanguage", R.string.ChooseYourLanguage));
        final LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        builder.setView(linearLayout);


        HashMap<String, String> langList= Language.getInstance().hashLanguage;


        Map<String, String> map =  Language.getInstance().hashLanguage;
        Map<String, String> treeMap = new TreeMap<String, String>(map);

        for (String str : treeMap.keySet()) {
            Country c = new Country();
            c.shortname = str;
            c.name = map.get(c.shortname);



            RadioColorCell cell = new RadioColorCell(context);
            cell.setPadding(AndroidUtilities.dp(4), 0, AndroidUtilities.dp(4), 0);
            cell.setTag(str);
            cell.setCheckColor(Theme.getColor(Theme.key_radioBackground), Theme.getColor(Theme.key_dialogRadioBackgroundChecked));
            cell.setTextAndValue(c.name, PlusConfig.mt_translate_lang.equals(c.shortname));
            linearLayout.addView(cell);
            cell.setOnClickListener(v -> {
                PlusConfig.setMTTranslatedLang(c.shortname);
                if (onSelectRunnable != null) {
                    onSelectRunnable.run();
                }
                builder.getDismissRunnable().run();
            });
        }






        AlertDialog dialog = builder.show();

        return dialog;

    }

}
