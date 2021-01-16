package org.plus.features;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.widget.LinearLayout;

import com.google.android.exoplayer2.util.Log;

import org.plus.apps.business.data.ShopDataController;
import org.plus.experment.PlusBuildVars;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.LanguageCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.ContactsActivity;
import org.telegram.ui.LaunchActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class FeatureUtils {


    public static void startProxyForSupportedCountry(int account){
        //plus
        final SharedPreferences preferences = MessagesController.getGlobalMainSettings();
        boolean useProxySettings = preferences.getBoolean("proxy_enabled", false) && !SharedConfig.proxyList.isEmpty();
        if(!useProxySettings){
            String code =  FeatureUtils.getUserCountry();
            //ir
            if(!TextUtils.isEmpty(code) && code.toLowerCase().equals("ir")){
                ShopDataController.getInstance(account).loadProxyFromFirebase(true,true);
            }
        }
        //

    }


    public static boolean isSupportedFeature(int account){
//        if(!UserConfig.getInstance(account).getClientPhone().startsWith("251")){
//
//        }
        boolean suported = false;
        if(PlusBuildVars.DEBUG_PRIVATE){
            suported  = true;
        }
        return suported;
    }


    public static String getUserCountry() {
        try {
            final TelephonyManager tm = (TelephonyManager) ApplicationLoader.applicationContext.getSystemService(Context.TELEPHONY_SERVICE);
            final String simCountry = tm.getSimCountryIso();
            if (simCountry != null && simCountry.length() == 2) {
                return simCountry.toLowerCase(Locale.US);
            }
            else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
                String networkCountry = tm.getNetworkCountryIso();
                if (networkCountry != null && networkCountry.length() == 2) { // network country code is available
                    return networkCountry.toLowerCase(Locale.US);
                }
            }
        }
        catch (Exception ignore) {

        }
        return null;
    }



}
