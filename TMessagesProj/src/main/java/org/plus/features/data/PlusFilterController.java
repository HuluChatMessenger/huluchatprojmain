package org.plus.features.data;


import com.google.android.exoplayer2.util.Log;

import org.plus.features.PlusConfig;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BaseController;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationsController;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;


public class PlusFilterController extends BaseController {

    int selfId = getUserConfig().getClientUserId();
    public ArrayList<TLRPC.Dialog> dialogsUsers = new ArrayList<>();
    public ArrayList<TLRPC.Dialog> dialogsGroups = new ArrayList<>();
    public ArrayList<TLRPC.Dialog> dialogsChannels = new ArrayList<>();
    public ArrayList<TLRPC.Dialog> dialogsBots = new ArrayList<>();
    public ArrayList<TLRPC.Dialog> dialogsAdmin = new ArrayList<>();
    public ArrayList<TLRPC.Dialog> dialogsUnread = new ArrayList<>();
    public ArrayList<TLRPC.Dialog> dialogHidden = new ArrayList<>();
    public ArrayList<TLRPC.Dialog> dialogsLocks = new ArrayList<>();

    public ArrayList<TLRPC.Dialog> dialogsUsersArchive = new ArrayList<>();
    public ArrayList<TLRPC.Dialog> dialogsGroupsArchive  = new ArrayList<>();
    public ArrayList<TLRPC.Dialog> dialogsChannelsArchive  = new ArrayList<>();
    public ArrayList<TLRPC.Dialog> dialogsBotsArchive  = new ArrayList<>();
    public ArrayList<TLRPC.Dialog> dialogsAdminArchive  = new ArrayList<>();
    public ArrayList<TLRPC.Dialog> dialogsUnreadArchive  = new ArrayList<>();
    public ArrayList<TLRPC.Dialog> dialogHiddenArchive  = new ArrayList<>();
    public ArrayList<TLRPC.Dialog> dialogsLocksArchive  = new ArrayList<>();

    public PlusFilterController(int num) {
        super(num);
    }

    private static volatile PlusFilterController[] Instance = new PlusFilterController[UserConfig.MAX_ACCOUNT_COUNT];

    public static PlusFilterController getInstance(int num) {
        PlusFilterController localInstance = Instance[num];
        if (localInstance == null) {
            synchronized (PlusFilterController.class) {
                localInstance = Instance[num];
                if (localInstance == null) {
                    Instance[num] = localInstance = new PlusFilterController(num);
                }
            }
        }
        return localInstance;
    }

    public void cleanup() {
        dialogsUsers.clear();
        dialogsGroups.clear();
        dialogsChannels.clear();
        dialogsBots.clear();
        dialogsAdmin.clear();
        dialogsUnread.clear();
        dialogHidden.clear();
        dialogsLocks.clear();

        dialogsUsersArchive.clear();
        dialogsGroupsArchive.clear();
        dialogsChannelsArchive.clear();
        dialogsBotsArchive.clear();
        dialogsAdminArchive.clear();
        dialogsUnreadArchive.clear();
        dialogHiddenArchive.clear();
        dialogsLocksArchive.clear();
    }

    public void remove(TLRPC.Dialog dialog) {
        dialogsUsers.remove(dialog);
        dialogsGroups.remove(dialog);
        dialogsChannels.remove(dialog);
        dialogsBots.remove(dialog);
        dialogsAdmin.remove(dialog);

        dialogsUnread.remove(dialog);
        dialogHidden.remove(dialog);
        dialogsLocks.remove(dialog);


        dialogsUsersArchive.remove(dialog);
        dialogsGroupsArchive.remove(dialog);
        dialogsChannelsArchive.remove(dialog);
        dialogsBotsArchive.remove(dialog);
        dialogsAdminArchive.remove(dialog);
        dialogsUnreadArchive.remove(dialog);
        dialogHiddenArchive.remove(dialog);
        dialogsLocksArchive.remove(dialog);
    }

    public void sortDialogs(TLRPC.Dialog dialog, int high_id, int lower_id) {
        if (lower_id != 0 && high_id != 1) {
            if (DialogObject.isChannel(dialog)) {
                TLRPC.Chat chat = getMessagesController().getChat(-lower_id);
                if (chat != null) {
                    if (chat.megagroup) {
                        if(dialog.folder_id == 0){
                            dialogsGroups.add(dialog);
                        }else{
                            dialogsGroupsArchive.add(dialog);

                        }
                    } else {
                        if(dialog.folder_id == 0){
                            dialogsChannels.add(dialog);
                        }else{
                            dialogsChannelsArchive.add(dialog);
                        }
                }
                if (chat != null && (chat.creator || ChatObject.hasAdminRights(chat))){
                    if(dialog.folder_id == 0){
                        dialogsAdmin.add(dialog);
                    }else{
                        dialogsAdminArchive.add(dialog);
                    }
                }
                }
            } else if (lower_id < 0) {
                if(dialog.folder_id == 0){
                    dialogsGroups.add(dialog);
                }else{
                    dialogsGroupsArchive.add(dialog);
                }
            } else {
                TLRPC.User user = getMessagesController().getUser((int) dialog.id);
                if (user != null) {
                    if (user.bot) {
                        if(dialog.folder_id == 0){
                            dialogsBots.add(dialog);
                        }else{
                            dialogsBotsArchive.add(dialog);
                        }
                    } else if (lower_id > 0 && lower_id != selfId && !user.bot){
                        if(dialog.folder_id == 0){
                            dialogsUsers.add(dialog);
                        }else{
                            dialogsUsersArchive.add(dialog);
                        }
                    }
                }
            }
        } else {
            TLRPC.EncryptedChat encryptedChat = getMessagesController().getEncryptedChat(high_id);
            if (encryptedChat != null){
                if(dialog.folder_id == 0){
                    dialogsUsers.add(dialog);
                }else{
                    dialogsUsersArchive.add(dialog);
                }
            }
        }

        if(dialog != null){
            if(dialog.unread_count > 0){
                if(dialog.folder_id == 0){
                    dialogsUnread.add(dialog);
                }else{
                    dialogsUnreadArchive.add(dialog);
                }
            }
        }


    }



    //Get UnRead for Each
    private int getDialogsUnreadCount(ArrayList<TLRPC.Dialog> dialogs) {
        int count = 0;
        for (TLRPC.Dialog dialog : dialogs) {
            if (!(dialog instanceof TLRPC.TL_dialogFolder) && !getMessagesController().isDialogMuted(dialog.id)) {
                count += dialog.unread_count;
            }
        }
        return count;
    }
    public int getDialogsUnreadCountForLocalFilters(int id) {
        if (id == 111) {
            getDialogsUnreadCount(dialogsUsers);
        } else if (id == 222) {
            getDialogsUnreadCount(dialogsGroups);
        } else if (id == 333) {
            getDialogsUnreadCount(dialogsChannels);
        } else if (id == 444) {
            getDialogsUnreadCount(dialogsBots);
        } else if (id == 555) {
            getDialogsUnreadCount(dialogsAdmin);
        } else if (id == 556) {
            getDialogsUnreadCount(dialogsUnread);
        }
        return 0;
    }

    //Mark All as read
    private void setMarkAsReadDialogs(ArrayList<TLRPC.Dialog> dialogs) {
        if (dialogs != null && !dialogs.isEmpty()) {
            for (int a = 0; a < dialogs.size(); a++) {
                TLRPC.Dialog dialg = dialogs.get(a);
                if(dialg.unread_count > 0){
                    getMessagesController().markMentionsAsRead(dialg.id);
                    getMessagesController().markDialogAsRead(dialg.id, dialg.top_message, dialg.top_message, dialg.last_message_date, false, 0, 0,true, 0);
                }
            }
        }
    }
    public void setMarkAsReadForLocalFilter(int id) {
        if (id == 111) {
            setMarkAsReadDialogs(dialogsUsers);
        } else if (id == 222) {
            setMarkAsReadDialogs(dialogsGroups);
        } else if (id == 333) {
            setMarkAsReadDialogs(dialogsChannels);
        } else if (id == 444) {
            setMarkAsReadDialogs(dialogsBots);
        } else if (id == 555) {
            setMarkAsReadDialogs(dialogsAdmin);
        } else if (id == 556) {
            setMarkAsReadDialogs(dialogsUnread);
        } else if (id == Integer.MAX_VALUE) {
            setMarkAsReadDialogs(getMessagesController().getDialogs(0));
        } else {
            MessagesController.DialogFilter dialogFilter = getMessagesController().dialogFilters.get(id);
            ArrayList<TLRPC.Dialog> dialogs = dialogFilter.dialogs;
            if (dialogs != null && !dialogs.isEmpty()) {
                for (int c = 0; c < dialogs.size(); c++) {
                    TLRPC.Dialog dialg = dialogs.get(c);
                    if (dialg.unread_count > 0) {
                        getMessagesController().markMentionsAsRead(dialg.id);
                        getMessagesController().markDialogAsRead(dialg.id, dialg.top_message, dialg.top_message, dialg.last_message_date, false, 0, 0,true, 0);
                    }
                }
            }
        }
    }

    //Archive All
    private void setArchiveDialogs(ArrayList<TLRPC.Dialog> dialogs) {
        if (dialogs != null && !dialogs.isEmpty()) {
            for (int a = 0; a < dialogs.size(); a++) {
                TLRPC.Dialog dialg = dialogs.get(a);
                if(dialg.folder_id != 1){
                    getMessagesController().addDialogToFolder(dialg.id,1, -1, 0);
                }
            }
        }
    }
    public void setArchiveForLocalFilter(int id) {
        if (id == 111) {
            setArchiveDialogs(dialogsUsers);
        } else if (id == 222) {
            setArchiveDialogs(dialogsGroups);
        } else if (id == 333) {
            setArchiveDialogs(dialogsChannels);
        } else if (id == 444) {
            setArchiveDialogs(dialogsBots);
        } else if (id == 555) {
            setArchiveDialogs(dialogsAdmin);
        } else if (id == 556) {
            setArchiveDialogs(dialogsUnread);
        }
    }

    //Mute All
    private void setMuteDialogs(ArrayList<TLRPC.Dialog> dialogs) {
        if (dialogs != null && !dialogs.isEmpty()) {
            for (int a = 0; a < dialogs.size(); a++) {
                TLRPC.Dialog dialg = dialogs.get(a);
                if(!getMessagesController().isDialogMuted(dialg.id)){
                    getNotificationsController().setDialogNotificationsSettings(dialg.id, NotificationsController.SETTING_MUTE_FOREVER);
                }
            }
        }
    }
    public void setMuteForLocalFilter(int id) {
        if (id == 111) {
            setMuteDialogs(dialogsUsers);
        } else if (id == 222) {
            setMuteDialogs(dialogsGroups);
        } else if (id == 333) {
            setMuteDialogs(dialogsChannels);
        } else if (id == 444) {
            setMuteDialogs(dialogsBots);
        } else if (id == 555) {
            setMuteDialogs(dialogsAdmin);
        } else if (id == 556) {
            setMuteDialogs(dialogsUnread);
        }
    }

    public boolean isLocalDialog(int tabId){
        return tabId >= 100 && tabId < 107;
    }

    public int getUnreadCount(int tabId){
        int count = 0;
        ArrayList<TLRPC.Dialog> dialogs = new ArrayList<>();
        if(tabId == PlusConfig.user_id){
            dialogs = dialogsUsers;
        }else if(tabId == PlusConfig.channel_id){
            dialogs = dialogsChannels;
        }else if(tabId == PlusConfig.group_id){
            dialogs = dialogsGroups;
        }else if(tabId == PlusConfig.bot_id){
            dialogs = dialogsBots;
        }else if(tabId == PlusConfig.admin_id){
            dialogs = dialogsAdmin;
        }else if(tabId == PlusConfig.unread_id){
            dialogs = dialogsUnread;
        }
        for (TLRPC.Dialog dialog : dialogs) {
            if (!(dialog instanceof TLRPC.TL_dialogFolder) && !getMessagesController().isDialogMuted(dialog.id)) {
                count += dialog.unread_count;
            }
        }
        return count;
    }


    private boolean resolvingBot;
    public void resolveRegBot(){
        if(resolvingBot){
            return;
        }
        resolvingBot = true;
        TLRPC.TL_contacts_resolveUsername req = new TLRPC.TL_contacts_resolveUsername();
        req.username = "creationdatebot";
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> {
            resolvingBot = false;
            if (response != null) {
                AndroidUtilities.runOnUIThread(() -> {
                    TLRPC.TL_contacts_resolvedPeer res = (TLRPC.TL_contacts_resolvedPeer) response;
                    MessagesController.getInstance(currentAccount).putUsers(res.users, false);
                    MessagesController.getInstance(currentAccount).putChats(res.chats, false);
                    MessagesStorage.getInstance(currentAccount).putUsersAndChats(res.users, res.chats, true, true);
                    getUserRegistrationDate();
                });
            }
        });

    }

    private boolean isRequestingTokenFromBot;
    public void getUserRegistrationDate() {
        if(isRequestingTokenFromBot){
            return;
        }
        isRequestingTokenFromBot = true;
        TLObject object = MessagesController.getInstance(currentAccount).getUserOrChat("creationdatebot");
        if (object instanceof TLRPC.User) {
            TLRPC.User user = (TLRPC.User) object;
            long dialogId = UserConfig.getInstance(currentAccount).getClientUserId();
            TLRPC.TL_messages_getInlineBotResults req = new TLRPC.TL_messages_getInlineBotResults();
            req.query = "";
            req.bot = MessagesController.getInstance(currentAccount).getInputUser(user);
            req.offset = "";
            int lower_id = (int) dialogId;
            if (lower_id != 0) {
                req.peer = MessagesController.getInstance(currentAccount).getInputPeer(lower_id);
            } else {
                req.peer = new TLRPC.TL_inputPeerEmpty();
            }
            RequestDelegate requestDelegate = (response, error) -> {
                isRequestingTokenFromBot = false;
                if (error == null) {
                    if (response instanceof TLRPC.TL_messages_botResults) {
                        TLRPC.TL_messages_botResults res = (TLRPC.TL_messages_botResults) response;
                        for (int a = 0; a < res.results.size(); a++) {
                            TLRPC.BotInlineResult result = res.results.get(a);
                            if (result.send_message != null && result.send_message.message.length() > 1) {
                                String text = result.send_message.message;
                                int start = text.indexOf("registered:");
                                if(start != -1){
                                   String date = text.substring(start).replace("registered:","").trim();
                                    Log.i("messageserver",date);

                                }
                            }
                        }
                    }
                }
            };
            ConnectionsManager.getInstance(currentAccount).sendRequest(req, requestDelegate, ConnectionsManager.RequestFlagFailOnServerErrors);
        } else {
            isRequestingTokenFromBot = false;
            resolveRegBot();
        }
    }

}