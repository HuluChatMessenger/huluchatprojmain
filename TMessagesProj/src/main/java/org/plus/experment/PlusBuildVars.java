package org.plus.experment;

import com.google.android.gms.maps.internal.IGoogleMapDelegate;

import org.checkerframework.checker.index.qual.PolyUpperBound;

public class PlusBuildVars {

    private static boolean useLocal = false;
    public static boolean DEBUG_PRIVATE = true;
    public static boolean DEBUG_VERSION = true;
    public static boolean LOGS_ENABLED = true;

    public static final String SERVER_ADDRESS = "http://104.248.163.118:8000";
    public static final String GOOGLE_MAP_API = "AIzaSyDywlkflWqYCy3_bQHu0lka5Ybr3YUN5s0";
    public static final String  AUTH_BOT_PRIVATE = "Tokentokenbot";
    public static final String  AUTH_BOT = "huluchatdevbot";

    public static String getAuthBot(){
        if(useLocal){
            return AUTH_BOT_PRIVATE;
        }else{
            return AUTH_BOT;
        }
    }


}
