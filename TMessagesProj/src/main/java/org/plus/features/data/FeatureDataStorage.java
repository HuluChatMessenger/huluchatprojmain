package org.plus.features.data;

import org.plus.apps.business.data.ShopDataController;
import org.plus.database.DataStorage;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BaseController;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.Comparator;

public class FeatureDataStorage extends BaseController {

    public static FeatureDataStorage[] instance = new FeatureDataStorage[UserConfig.MAX_ACCOUNT_COUNT];
    public static FeatureDataStorage getInstance(int num) {
        FeatureDataStorage localInstance = instance[num];
        if (localInstance == null) {
            synchronized (FeatureDataStorage.class) {
                localInstance = instance[num];
                if (localInstance == null) {
                    instance[num] = localInstance = new FeatureDataStorage(num);
                }
            }
        }
        return localInstance;
    }
    public ArrayList<Long> hiddenList = new ArrayList<>();
    public ArrayList<TLRPC.Dialog> hiddenDialogList= new ArrayList<>();


    public FeatureDataStorage(int num) {
        super(num);
        currentAccount = num;
    }


    public void clearProfileChange(ShopDataController.BooleanCallBack booleanCallBack){
        DataStorage.storageQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                getDataStorage().featureDao().clearProfileChange();
                if(booleanCallBack != null){
                    booleanCallBack.onResponse(true);
                }
            }
        });
    }


    public void updateUserChange(TLRPC.Update baseUpdate){
        FeatureDataModel.ProfileChange profileChange = new FeatureDataModel.ProfileChange();
        if(baseUpdate instanceof TLRPC.TL_updateUserPhoto){
            TLRPC.TL_updateUserPhoto update = (TLRPC.TL_updateUserPhoto) baseUpdate;
            TLRPC.User user = getMessagesController().getUser(update.user_id);
            if(user != null){
                profileChange.timeStamp = update.date;
                profileChange.user_id = update.user_id;
                profileChange.photo_id = user.photo.photo_id;
                if(user.photo == null || user.photo.photo_small == null){
                    profileChange.type = -1;
                }

            }else{
                profileChange.type = -1;
            }
        }
        if(profileChange.type == -1){
            return;
        }
        Utilities.globalQueue.postRunnable(() -> {
            FeatureDao dao = getDataStorage().featureDao();
            dao.insert(profileChange);
            AndroidUtilities.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                   // NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.updateInterfaces,100);
                }
            });
        });
    }


    private final Comparator<FeatureDataModel.ProfileChange> profileChangeComparator = (o1, o2) -> {
        if (o1.timeStamp > o2.timeStamp) {
            return -1;
        } else if (o1.timeStamp < o2.timeStamp) {
            return 1;
        }
        return 0;
    };


}
