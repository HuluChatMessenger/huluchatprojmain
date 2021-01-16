package org.plus.features;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;
import android.util.SparseArray;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.tgnet.SerializedData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


public class PlusConfig {



    public static boolean showTabsOnForward;
    public static boolean showUserBio;

    public static boolean isRtl = false;
    public static int screenLayout = 0;
    public static boolean showActionBarShadow;
    public static boolean showUserNameInsteadOfMobile;
    public static boolean useSystemDefaultFont;

    public static boolean notifyWhenOnline;
    public static boolean notifyWhenOffline;
    public static boolean notifyWhenChangeAvatar;
    public static boolean notifyWhenChangeName;
    public static boolean notifyWhenChangeUsername;
    public static boolean notifyWhenChangePhone;
    public static boolean notifyWhenReadMessage;
    public static boolean chatPreviewVibrateOnShow;

    public static boolean checkBeforeSendingAudio;
    public static boolean checkBeforeSendingVideo;

    private static SharedPreferences preferences;
    private static final Object sync = new Object();
    private SharedPreferences huluPreferences;
    private static boolean configLoaded;
    public static String configName = "plusConfig";



    static {
        loadConfig();
    }

    //plus Configs
    public static boolean enableHiddenChat;
    public static boolean folderIconTabs;
    public static boolean autoSortingChat;
    public static boolean hideAllChat;
    public static String default_translate_lang;
    public static String mt_translate_lang;

    public static boolean sortUserFolder; //111
    public static boolean sortGroupFolder; //222
    public static boolean sortChannelFolder; //333
    public static boolean sortBotFolder; //444
    public static boolean sortAdminFolder; //555
    public static boolean sortFavFolder; //556

    public static boolean enableSpecialContact;
    public static boolean enableSpecialContactService;

    private static final int unreadFilter= 1;
    private static final int userFilter = 2;
    private static final int groupFilter = 3;
    private static final int channelFilter = 4;
    private static final int botFilter = 5;
    private static final int adminFilter =6;

    public static class ChatSortData {

        public ChatSortData(String text, int res_outline,int res_filled, int order, int type, boolean enabled, int id) {
            this.text = text;
            this.res_outline = res_outline;
            this.res_filled = res_filled;
            this.order = order;
            this.type = type;
            this.id = id;
            this.enabled = enabled;

        }

        public ChatSortData() {

        }

        public String text;
        public int res_filled;
        public int res_outline;
        public int order;
        public int type;
        public boolean enabled;
        public int id;
    }

    public static class Panel{

        public int action;
        public int chat_type;
        public String title;
        public int icon;
        public boolean enabled;
        public int order;
        public String info;
        public boolean for_admin;

        public Panel(int action, int chat_type, String title, int icon, boolean enabled, int order, String info, boolean for_admin) {
            this.action = action;
            this.chat_type = chat_type;
            this.title = title;
            this.icon = icon;
            this.enabled = enabled;
            this.order = order;
            this.info = info;
            this.for_admin = for_admin;
        }

        public Panel() {
        }
    }

    public static final int unread_id  = 100;
    public static final int user_id  = 101;
    public static final int group_id  = 102;
    public static final int channel_id  = 103;
    public static final int bot_id  = 104;
    public static final int admin_id  = 105;


    public static ChatSortData [] chat_sort_data_array = {
            new ChatSortData(LocaleController.getString("Unread",R.string.Unread),R.drawable.ime_filter_icon_bubble_point,R.drawable.ime_filter_icon_bubble_point_filled,1,unreadFilter,true,unread_id),
            new ChatSortData(LocaleController.getString("Users",R.string.Users),R.drawable.ime_filter_icon_user,R.drawable.ime_filter_icon_user_filled,2,userFilter,true,user_id),
            new ChatSortData(LocaleController.getString("Groups",R.string.Groups),R.drawable.ime_filter_icon_users,R.drawable.ime_filter_icon_users_filled,3,groupFilter,true,group_id),
            new ChatSortData(LocaleController.getString("Channels",R.string.Channels),R.drawable.ime_filter_icon_channel,R.drawable.ime_filter_icon_channel_filled,4,channelFilter,true,channel_id),
            new ChatSortData(LocaleController.getString("Bots",R.string.Bots),R.drawable.ime_filter_icon_bot,R.drawable.ime_filter_icon_bot_filled,5,botFilter,true,bot_id),
            new ChatSortData(LocaleController.getString("ChannelAdmin",R.string.ChannelAdmin),R.drawable.ime_filter_icon_admin,R.drawable.ime_filter_icon_admin_filled,6,adminFilter,true,admin_id),
    };


    public static int enabledTabCount;
    private static ArrayList<ChatSortData> sortDataArrayList = new ArrayList<>();


    public static ChatSortData getSortDataById(int id) {
        ChatSortData chatSortData = null;
        ArrayList<ChatSortData> sortDataArrayList = getSortDataArrayList();
        for (ChatSortData chartData : sortDataArrayList) {
            if (chartData.id == id) {
                chatSortData = chartData;
                break;
            }
        }
        return chatSortData;
    }

    public static ArrayList<ChatSortData> getSortDataArrayList() {
        if (sortDataArrayList == null || sortDataArrayList.isEmpty()) {
            loadChatSortList();
            return sortDataArrayList;
        }
        return sortDataArrayList;
    }

    public static void loadChatSortList() {
        enabledTabCount = 0;
        preferences = ApplicationLoader.applicationContext.getSharedPreferences(configName, Activity.MODE_PRIVATE);
        sortDataArrayList.clear();
        String list = preferences.getString("cat_sort_list", null);
        if (!TextUtils.isEmpty(list)) {
            byte[] bytes = Base64.decode(list, Base64.DEFAULT);
            SerializedData data = new SerializedData(bytes);
            int count = data.readInt32(false);
            for (int a = 0; a < count; a++) {
                ChatSortData sortData = new ChatSortData(
                        data.readString(false),
                        data.readInt32(false),
                        data.readInt32(false),
                        data.readInt32(false),
                        data.readInt32(false),
                        data.readBool(false),
                        data.readInt32(false)
                );
                sortDataArrayList.add(sortData);
                if (sortData.enabled) {
                    enabledTabCount++;
                }
            }
            data.cleanup();
        } else {
            //default
            sortDataArrayList.addAll(Arrays.asList(chat_sort_data_array));
            enabledTabCount = sortDataArrayList.size();
        }
    }

    public static void saveChatSortList() {
        SerializedData serializedData = new SerializedData();
        int count = getSortDataArrayList().size();
        serializedData.writeInt32(count);
        for (int a = 0; a < count; a++) {
            ChatSortData info = sortDataArrayList.get(a);
            serializedData.writeString(info.text);
            serializedData.writeInt32(info.res_outline);
            serializedData.writeInt32(info.res_filled);
            serializedData.writeInt32(info.order);
            serializedData.writeInt32(info.type);
            serializedData.writeBool(info.enabled);
            serializedData.writeInt32(info.id);
        }
        preferences = ApplicationLoader.applicationContext.getSharedPreferences(configName, Activity.MODE_PRIVATE);
        preferences.edit().putString("cat_sort_list", Base64.encodeToString(serializedData.toByteArray(), Base64.NO_WRAP)).commit();
        serializedData.cleanup();
        loadChatSortList();
    }

    public static void saveConfig() {
        synchronized (sync) {
            try {
                preferences = ApplicationLoader.applicationContext.getSharedPreferences(configName, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();

                editor.putInt("screenLayout", screenLayout);
                editor.putBoolean("isRtl", isRtl);
                editor.putBoolean("showActionBarShadow", showActionBarShadow);
                editor.putBoolean("showUserNameInsteadOfMobile", showUserNameInsteadOfMobile);
                editor.putBoolean("useSystemDefaultFont", useSystemDefaultFont);
                editor.putBoolean("chatPreviewVibrateOnShow", chatPreviewVibrateOnShow);

                editor.putBoolean("checkBeforeSendingAudio", checkBeforeSendingAudio);
                editor.putBoolean("checkBeforeSendingVideo", checkBeforeSendingVideo);

                editor.putBoolean("showUserBio", showUserBio);
                editor.putBoolean("showTabsOnForward", showTabsOnForward);
                editor.putBoolean("enableHiddenChat", enableHiddenChat);
                editor.putBoolean("autoSortingChat", autoSortingChat);
                editor.putBoolean("folderIconTabs", folderIconTabs);
                editor.putBoolean("hideAllChat", hideAllChat);
                editor.putString("default_translate_lang", default_translate_lang);
                editor.putString("mt_translate_lang", mt_translate_lang);

                editor.putBoolean("sortUserFolder", sortUserFolder);
                editor.putBoolean("sortGroupFolder", sortGroupFolder);
                editor.putBoolean("sortChannelFolder", sortChannelFolder);
                editor.putBoolean("sortBotFolder", sortBotFolder);
                editor.putBoolean("sortAdminFolder", sortAdminFolder);
                editor.putBoolean("sortFavFolder", sortFavFolder);

                editor.putBoolean("enableSpecialContact", enableSpecialContact);
                editor.putBoolean("enableSpecialContactService", enableSpecialContactService);


                editor.putBoolean("notifyWhenOnline", notifyWhenOnline);
                editor.putBoolean("notifyWhenOffline", notifyWhenOffline);
                editor.putBoolean("notifyWhenChangeAvatar", notifyWhenChangeAvatar);
                editor.putBoolean("notifyWhenChangeName", notifyWhenChangeName);
                editor.putBoolean("notifyWhenChangeUsername", notifyWhenChangeUsername);
                editor.putBoolean("notifyWhenChangePhone", notifyWhenChangePhone);
                editor.putBoolean("notifyWhenReadMessage", notifyWhenReadMessage);


                editor.commit();
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
    }

    public static void loadConfig() {
        synchronized (sync) {
            if (configLoaded) {
                return;
            }
            preferences = ApplicationLoader.applicationContext.getSharedPreferences(configName, Activity.MODE_PRIVATE);

            screenLayout = preferences.getInt("screenLayout", 0);
            isRtl = preferences.getBoolean("isRtl", false);
            showActionBarShadow = preferences.getBoolean("showActionBarShadow", false);
            showUserNameInsteadOfMobile = preferences.getBoolean("showUserNameInsteadOfMobile", false);
            useSystemDefaultFont = preferences.getBoolean("useSystemDefaultFont", false);
            chatPreviewVibrateOnShow = preferences.getBoolean("chatPreviewVibrateOnShow", true);

            checkBeforeSendingAudio = preferences.getBoolean("checkBeforeSendingAudio", false);
            checkBeforeSendingVideo = preferences.getBoolean("checkBeforeSendingVideo", false);

            showUserBio = preferences.getBoolean("showUserBio", true);
            showTabsOnForward = preferences.getBoolean("showTabsOnForward", true);
            enableHiddenChat = preferences.getBoolean("enableHiddenChat", false);
            autoSortingChat = preferences.getBoolean("autoSortingChat", true);
            folderIconTabs = preferences.getBoolean("folderIconTabs", true);
            hideAllChat = preferences.getBoolean("hideAllChat", false);
            default_translate_lang = preferences.getString("default_translate_lang", "en");
            mt_translate_lang = preferences.getString("mt_translate_lang", "en");

            sortUserFolder = preferences.getBoolean("sortUserFolder", true);
            sortGroupFolder = preferences.getBoolean("sortGroupFolder", true);
            sortChannelFolder = preferences.getBoolean("sortChannelFolder", true);
            sortBotFolder = preferences.getBoolean("sortBotFolder", true);
            sortAdminFolder = preferences.getBoolean("sortAdminFolder", true);
            sortFavFolder = preferences.getBoolean("sortFavFolder", true);


            notifyWhenOnline = preferences.getBoolean("notifyWhenOnline", false);
            notifyWhenOffline = preferences.getBoolean("notifyWhenOffline", false);
            notifyWhenChangeAvatar = preferences.getBoolean("notifyWhenChangeAvatar", false);

            notifyWhenChangeName = preferences.getBoolean("notifyWhenChangeName", false);
            notifyWhenChangeUsername = preferences.getBoolean("notifyWhenChangeUsername", false);
            notifyWhenChangePhone = preferences.getBoolean("notifyWhenChangePhone", false);

            notifyWhenReadMessage = preferences.getBoolean("notifyWhenReadMessage", false);


            configLoaded = true;
        }
    }


    public static void clearConfig() {

        screenLayout = 0;
        isRtl = false;
        showActionBarShadow = false;
        showUserNameInsteadOfMobile = false;
        useSystemDefaultFont = false;
        chatPreviewVibrateOnShow = true;

        checkBeforeSendingAudio = false;
        checkBeforeSendingVideo = false;

        showUserBio =true;
        showTabsOnForward = true;
        enableHiddenChat = false;
        autoSortingChat = true;
        folderIconTabs = false;
        hideAllChat =false;
        default_translate_lang = "en";
        mt_translate_lang = "en";

        sortUserFolder = true;
        sortGroupFolder = true;
        sortChannelFolder = true;
        sortBotFolder = true;
        sortAdminFolder = true;
        sortFavFolder = true;


        notifyWhenOnline = false;
        notifyWhenOffline = false;
        notifyWhenChangeAvatar = false;

        notifyWhenChangeName =false;
        notifyWhenChangeUsername = false;
        notifyWhenChangePhone = false;

        notifyWhenReadMessage =false;
        enabledTabCount  = 0;

        configLoaded = false;
        filterListLoaded = false;
        if(currentFilterSparseArray != null){
            currentFilterSparseArray.clear();;
            currentFilterSparseArray = null;
        }
        preferences = ApplicationLoader.applicationContext.getSharedPreferences(configName, Activity.MODE_PRIVATE);
        preferences.edit().putString("cat_sort_list", null).commit();
        preferences.edit().putString("filter_list", null).commit();
        if(sortDataArrayList != null){
            sortDataArrayList.clear();
        }
        saveConfig();
    }



    public static class FilterPos {
        public int id;
        public int pos;

        public FilterPos(int idd, int poss) {
            id = idd;
            pos = poss;
        }

    }

    private static boolean filterListLoaded;

    public static SparseArray<FilterPos> currentFilterSparseArray;

    public static void loadFilterList() {
        if (filterListLoaded) {
            return;
        }
        preferences = ApplicationLoader.applicationContext.getSharedPreferences(configName, Activity.MODE_PRIVATE);
        filterListLoaded = true;
        currentFilterSparseArray = new SparseArray<>();
        String list = preferences.getString("filter_list", null);
        if (!TextUtils.isEmpty(list)) {
            byte[] bytes = Base64.decode(list, Base64.DEFAULT);
            SerializedData data = new SerializedData(bytes);
            int count = data.readInt32(false);
            for (int a = 0; a < count; a++) {
                FilterPos info = new FilterPos(data.readInt32(false), data.readInt32(false));
                currentFilterSparseArray.put(info.id, info);
            }
            data.cleanup();
        }
    }

    public static void updateFilter(FilterPos filterPos) {

    }


    public static void addFilter(FilterPos filterInfo) {
        currentFilterSparseArray.put(filterInfo.id, filterInfo);
        saveFilterList();
    }

    public static void saveFilterList() {
        SerializedData serializedData = new SerializedData();
        int count = currentFilterSparseArray.size();
        serializedData.writeInt32(count);
        for (int a = 0; a < count; a++) {
            int key = currentFilterSparseArray.keyAt(a);

            FilterPos info = currentFilterSparseArray.get(key);
            serializedData.writeInt32(info.id);
            serializedData.writeInt32(info.pos);
        }
        preferences = ApplicationLoader.applicationContext.getSharedPreferences(configName, Activity.MODE_PRIVATE);
        preferences.edit().putString("filter_list", Base64.encodeToString(serializedData.toByteArray(), Base64.NO_WRAP)).commit();


        serializedData.cleanup();
    }

    public static void deleteFilter(FilterPos filterInfo) {
    }

    public static void setMTTranslatedLang(String code){
        mt_translate_lang = code;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(configName, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("mt_translate_lang", mt_translate_lang);
        editor.commit();
    }

    public static void setTranslatedLang(String code) {
        default_translate_lang = code;
        preferences = ApplicationLoader.applicationContext.getSharedPreferences(configName, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("default_translate_lang", default_translate_lang);
        editor.commit();
    }

    public static void toggleHideAllChat() {
        hideAllChat = !hideAllChat;
        preferences = ApplicationLoader.applicationContext.getSharedPreferences(configName, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("hideAllChat", hideAllChat);
        editor.commit();
    }

    public static void toggleEnableHiddenChat() {
        enableHiddenChat = !enableHiddenChat;
        preferences = ApplicationLoader.applicationContext.getSharedPreferences(configName, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("enableHiddenChat", enableHiddenChat);
        editor.commit();
    }

    public static void toggleShowTabsOnForward() {
        showTabsOnForward = !showTabsOnForward;
        preferences = ApplicationLoader.applicationContext.getSharedPreferences(configName, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("showTabsOnForward", showTabsOnForward);
        editor.commit();
    }
    public static void toggleShowUserBio() {
        showUserBio = !showUserBio;
        preferences = ApplicationLoader.applicationContext.getSharedPreferences(configName, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("showUserBio", showUserBio);
        editor.commit();
    }

    public static void toggleAutoSortingChat() {
        autoSortingChat = !autoSortingChat;
        preferences = ApplicationLoader.applicationContext.getSharedPreferences(configName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("autoSortingChat", autoSortingChat);
        editor.commit();
    }

    public static void toggleSortUserFolder() {
        sortUserFolder = !sortUserFolder;
        preferences = ApplicationLoader.applicationContext.getSharedPreferences(configName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("sortUserFolder", sortUserFolder);
        editor.commit();
    }

    public static void toggleSortGroupFolder() {
        sortGroupFolder = !sortGroupFolder;
        preferences = ApplicationLoader.applicationContext.getSharedPreferences(configName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("sortGroupFolder", sortGroupFolder);
        editor.commit();
    }

    public static void toggleSortChannelFolder() {
        sortChannelFolder = !sortChannelFolder;
        preferences = ApplicationLoader.applicationContext.getSharedPreferences(configName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("sortChannelFolder", sortChannelFolder);
        editor.commit();
    }

    public static void toggleSortBotFolder() {
        sortBotFolder = !sortBotFolder;
        preferences = ApplicationLoader.applicationContext.getSharedPreferences(configName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("sortBotFolder", sortBotFolder);
        editor.commit();
    }

    public static void toggleSortAdminFolder() {
        sortAdminFolder = !sortAdminFolder;
        preferences = ApplicationLoader.applicationContext.getSharedPreferences(configName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("sortAdminFolder", sortAdminFolder);
        editor.commit();
    }

    public static void toggleSortFavFolder() {
        sortFavFolder = !sortFavFolder;
        preferences = ApplicationLoader.applicationContext.getSharedPreferences(configName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("sortFavFolder", sortFavFolder);
        editor.commit();
    }

    public static void toggleFolderIconTabs() {
        folderIconTabs = !folderIconTabs;
        preferences = ApplicationLoader.applicationContext.getSharedPreferences(configName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("folderIconTabs", folderIconTabs);
        editor.commit();
    }

    public static void toggleEnableSpecialContact() {
        enableSpecialContact = !enableSpecialContact;
        preferences = ApplicationLoader.applicationContext.getSharedPreferences(configName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("enableSpecialContact", enableSpecialContact);
        editor.commit();
    }

    public static void toggleEnableSpecialContactService() {
        enableSpecialContactService = !enableSpecialContactService;
        preferences = ApplicationLoader.applicationContext.getSharedPreferences(configName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("enableSpecialContactService", enableSpecialContactService);
        editor.commit();
    }


    public static SharedPreferences prefer = ApplicationLoader.applicationContext.getSharedPreferences("config", Activity.MODE_PRIVATE);



    public static boolean containValue(String pref) {
        return prefer.contains(pref);
    }

    public static void removeValue(String pref) {
        prefer.edit().remove(pref).commit();
    }

    public static void setIntValue(String pref, int value) {
        prefer.edit().putInt(pref, value).commit();
    }


    public static void toggleNotifyWhenOnline() {
        notifyWhenOnline = !notifyWhenOnline;
        preferences = ApplicationLoader.applicationContext.getSharedPreferences(configName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("notifyWhenOnline", notifyWhenOnline);
        editor.commit();
    }

    public static void toggleNotifyWhenOffline() {
        notifyWhenOffline = !notifyWhenOffline;
        preferences = ApplicationLoader.applicationContext.getSharedPreferences(configName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("notifyWhenOffline", notifyWhenOffline);
        editor.commit();
    }

    public static void toggleNotifyWhenChangeAvatar() {
        notifyWhenChangeAvatar = !notifyWhenChangeAvatar;
        preferences = ApplicationLoader.applicationContext.getSharedPreferences(configName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("notifyWhenChangeAvatar", notifyWhenChangeAvatar);
        editor.commit();
    }

    public static void toggleNotifyWhenChangeName() {
        notifyWhenChangeName = !notifyWhenChangeName;
        preferences = ApplicationLoader.applicationContext.getSharedPreferences(configName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("notifyWhenChangeName", notifyWhenChangeName);
        editor.commit();
    }

    public static void toggleNotifyWhenChangeUsername() {
        notifyWhenChangeUsername = !notifyWhenChangeUsername;
        preferences = ApplicationLoader.applicationContext.getSharedPreferences(configName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("notifyWhenChangeUsername", notifyWhenChangeUsername);
        editor.commit();
    }

    public static void toggleNotifyWhenChangePhone() {
        notifyWhenChangePhone = !notifyWhenChangePhone;
        preferences = ApplicationLoader.applicationContext.getSharedPreferences(configName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("notifyWhenChangePhone", notifyWhenChangePhone);
        editor.commit();
    }

    public static void toggleNotifyWhenReadMessage() {
        notifyWhenReadMessage = !notifyWhenReadMessage;
        preferences = ApplicationLoader.applicationContext.getSharedPreferences(configName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("notifyWhenReadMessage", notifyWhenReadMessage);
        editor.commit();
    }

    public static void setScreenLayoutRow(boolean value) {
        isRtl = value;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(configName, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isRtl", isRtl);
        editor.commit();
    }

    public static void setScreenLayoutRow(int value) {
        screenLayout = value;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(configName, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("screenLayout", screenLayout);
        editor.commit();
    }

    public static void toggleUseSystemDefaultFont() {
        useSystemDefaultFont = !useSystemDefaultFont;
        preferences = ApplicationLoader.applicationContext.getSharedPreferences(configName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("useSystemDefaultFont", useSystemDefaultFont);
        editor.commit();
    }

    public static void toggleChatPreviewVibrateOnShow() {
        chatPreviewVibrateOnShow = !chatPreviewVibrateOnShow;
        preferences = ApplicationLoader.applicationContext.getSharedPreferences(configName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("chatPreviewVibrateOnShow", chatPreviewVibrateOnShow);
        editor.commit();
    }
    public static void toggleCheckBeforeSendingAudio() {
        checkBeforeSendingAudio = !checkBeforeSendingAudio;
        preferences = ApplicationLoader.applicationContext.getSharedPreferences(configName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("checkBeforeSendingAudio", checkBeforeSendingAudio);
        editor.commit();
    }
    public static void toggleCheckBeforeSendingVideo() {
        checkBeforeSendingVideo = !checkBeforeSendingVideo;
        preferences = ApplicationLoader.applicationContext.getSharedPreferences(configName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("checkBeforeSendingVideo", checkBeforeSendingVideo);
        editor.commit();
    }


    public static void toggleShowActionBarShadow() {
        showActionBarShadow = !showActionBarShadow;
        preferences = ApplicationLoader.applicationContext.getSharedPreferences(configName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("showActionBarShadow", showActionBarShadow);
        editor.commit();
    }

    public static void toggleShowUserNameInsteadOfMobile() {
        showUserNameInsteadOfMobile = !showUserNameInsteadOfMobile;
        preferences = ApplicationLoader.applicationContext.getSharedPreferences(configName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("showUserNameInsteadOfMobile", showUserNameInsteadOfMobile);
        editor.commit();
    }




}
