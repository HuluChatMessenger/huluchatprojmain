//package org.plus.features.data;
//
//import com.google.android.exoplayer2.util.Log;
//
//import org.plus.database.DataStorage;
//import org.telegram.messenger.AndroidUtilities;
//import org.telegram.messenger.MessagesController;
//import org.telegram.messenger.UserConfig;
//import org.telegram.tgnet.TLRPC;
//
//import java.util.ArrayList;
//
//public class FeatureDataController {
//
//    public ArrayList<TLRPC.Dialog> hiddenDialogs = new ArrayList<>();
//    public ArrayList<Long> hiddenChats = new ArrayList<>();
//
//    public static FeatureDataController[] instance = new FeatureDataController[UserConfig.MAX_ACCOUNT_COUNT];
//    private int currentAccount;
//    private DataStorage dataStorage;
//
//    public static FeatureDataController getInstance(int num) {
//        FeatureDataController localInstance = instance[num];
//        if (localInstance == null) {
//            synchronized (FeatureDataController.class) {
//                localInstance = instance[num];
//                if (localInstance == null) {
//                    instance[num] = localInstance = new FeatureDataController(num);
//                }
//            }
//        }
//        return localInstance;
//    }
//
//    public FeatureDataController(int account){
//        currentAccount = account;
//        dataStorage = DataStorage.getDatabase(account);
//        loadHiddenChat();
//    }
//
//
//
//    public void addToHiddenChat(ArrayList<Long> chat_lis){
//        if(chat_lis == null || chat_lis.isEmpty())
//            return;
//
//        dataStorage.getStorageQueue().postRunnable(new Runnable() {
//            @Override
//            public void run() {
//                dataStorage.featureDao().saveHiddenChats(chat_lis);
//                AndroidUtilities.runOnUIThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        loadHiddenChat();
//                    }
//                });
//            }
//        });
//    }
//
//
//    public boolean hiddenChatLoaded;
//
//    public void loadHiddenChat(){
//        hiddenChatLoaded = false;
//        hiddenChats.clear();
//        hiddenDialogs.clear();
//        Log.i("hiddechat","loading hidden chat");
//        dataStorage.getStorageQueue().postRunnable(new Runnable() {
//            @Override
//            public void run() {
//             ArrayList<FeatureModel.HiddenChat>   chats = dataStorage.featureDao().getHiddenChats();
//                if(chats ==null){
//                    return;
//                }
//                Log.i("hiddechat","loading hidden chats " + chats.size());
//
//                for(int a = 0; a <  chats.size(); a++)
//                {
//                    hiddenChats.add(chats.get(a).chat_id);
//                    Log.i("hiddechat","loading hidden chats for " + chats.get(a).chat_id);
//
//                    TLRPC.Dialog dialog = MessagesController.getInstance(currentAccount).dialogs_dict.get(chats.get(a).chat_id);
//                    if(dialog != null)
//                       hiddenDialogs.add(dialog);
//                }
//                hiddenChatLoaded = true;
//
//            }
//
//        });
//    }
//
//}
