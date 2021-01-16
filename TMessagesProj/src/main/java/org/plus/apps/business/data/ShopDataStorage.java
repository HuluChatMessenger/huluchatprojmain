package org.plus.apps.business.data;

import android.util.Log;
import android.util.SparseArray;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;
import org.plus.database.DataStorage;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BaseController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;


public class ShopDataStorage extends BaseController {

    public interface DataLoadInterface{
        void data(String data);
    }

    public interface TaskInterFace {
        void result(boolean success);
    }

    public static String TAG = ShopDataStorage.class.getSimpleName();

    public static ShopDataStorage[] instance = new ShopDataStorage[UserConfig.MAX_ACCOUNT_COUNT];

    private AtomicLong lastTaskId = new AtomicLong(System.currentTimeMillis());
    private SparseArray<ArrayList<Runnable>> tasks = new SparseArray<>();

    private DataStorage dataStorage;

    public static ShopDataStorage getInstance(int num) {
        ShopDataStorage localInstance = instance[num];
        if (localInstance == null) {
            synchronized (ShopDataStorage.class) {
                localInstance = instance[num];
                if (localInstance == null) {
                    instance[num] = localInstance = new ShopDataStorage(num);
                }
            }
        }
        return localInstance;
    }


    public ShopDataStorage(int num) {
        super(num);
        dataStorage = getDataStorage();
    }

    public void saveConf(ArrayList<ShopDataSerializer.ConfigurationsObject> configurationsObjects, ShopDataController.ConfigDelegate configDelegate) {
        dataStorage.getStorageQueue().postRunnable(() -> {
            dataStorage.shopDao().clearConfig();
            for (int a = 0; a < configurationsObjects.size(); a++) {
                ShopDataSerializer.ConfigurationsObject configurationsObject = configurationsObjects.get(a);
                if (configurationsObject == null) {
                    continue;
                }
                dataStorage.shopDao().insert(configurationsObject.toDatabaseModel());
            }
            AndroidUtilities.runOnUIThread(() -> {
                if (configDelegate != null) {
                    configDelegate.didFinishUpdating(true);
                }
            });
        });

    }

    public void loadRecentSearch(ShopDataController.ProductSearchCallBack callBack) {
        dataStorage.getStorageQueue().postRunnable(() -> {
            ArrayList<ShopDataModels.RecentSearch> searchResults = dataStorage.shopDao().getSearchResult();
            AndroidUtilities.runOnUIThread(() -> {
                if (callBack != null) {
                    callBack.run(searchResults, null, null, -1);
                }
            });

        });
    }

    public void loadSortForProductType(String type) {
        dataStorage.getStorageQueue().postRunnable(new Runnable() {
            @Override
            public void run() {
                ShopDataModels.ProductConfigurationObject confObject = dataStorage.shopDao().getProductConfig("product_types");
                Gson gson = new Gson();
                try {
                    JSONArray jsonArray = new JSONArray(confObject.fieldsets);
                    for (int a = 0; a < jsonArray.length(); a++) {
                        JSONObject object = jsonArray.getJSONObject(a);
                        ShopDataSerializer.ProductType productType = gson.fromJson(object.toString(), ShopDataSerializer.ProductType.class);
                        if (productType.key.equals(type)) {
                            AndroidUtilities.runOnUIThread(() -> {
                                NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didSortLoaded, true, type, productType.sorts);
                            });
                            break;
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });
    }


    public void loadSectionConfiguration(String sec, int class_guid) {
        dataStorage.getStorageQueue().postRunnable(() -> {
            ShopDataModels.ProductConfigurationObject confObject = dataStorage.shopDao().getProductConfig(sec);
            if (confObject == null) {
                AndroidUtilities.runOnUIThread(() -> ShopDataController.getInstance(currentAccount).loadSectionConfiguration(sec, false, class_guid));
            } else {
                try {
                    Gson gson = new Gson();
                    JSONArray jsonArray = new JSONArray(confObject.fieldsets);

                    if (sec.equals("product_types")) {
                        ArrayList<ShopDataSerializer.ProductType> productTypes = new ArrayList<>();
                        for (int a = 0; a < jsonArray.length(); a++) {
                            JSONObject object = jsonArray.getJSONObject(a);
                            ShopDataSerializer.ProductType productType = gson.fromJson(object.toString(), ShopDataSerializer.ProductType.class);
                            productTypes.add(productType);
                        }
                        AndroidUtilities.runOnUIThread(() -> {
                            NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didConfigrationLoaded, true, sec, productTypes, class_guid);
                            ShopDataController.getInstance(currentAccount).productTypes = productTypes;

                        });
                    } else {
                        ArrayList<ShopDataSerializer.FieldSet> fieldSets = new ArrayList<>();
                        for (int a = 0; a < jsonArray.length(); a++) {
                            JSONObject object = jsonArray.getJSONObject(a);
                            ShopDataSerializer.FieldSet fieldSet = gson.fromJson(object.toString(), ShopDataSerializer.FieldSet.class);
                            fieldSets.add(fieldSet);
                        }
                        AndroidUtilities.runOnUIThread(() -> {
                            NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didConfigrationLoaded, true, sec, fieldSets, class_guid);
                            ShopDataController.getInstance(currentAccount).configHashMap.put(sec,fieldSets);
                        });
                    }
                } catch (Exception e) {
                    Log.d(TAG, e.getMessage());
                }

            }
        });
    }


    public void checkShop(int channel_id, ShopDataController.BooleanCallBack callBack) {
        dataStorage.getStorageQueue().postRunnable(() -> {
            ShopDataModels.CheckedShop checked_shop = dataStorage.shopDao().getCheckedShop(channel_id);
            AndroidUtilities.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if(callBack != null){
                        callBack.onResponse(checked_shop != null);
                    }
                }
            });
        });
    }

    public void insertCheckedShop(int channel_id) {
        dataStorage.getStorageQueue().postRunnable(() -> {
            ShopDataModels.CheckedShop shop = new ShopDataModels.CheckedShop();
            shop.exist = true;
            shop.channel_id = channel_id;
            shop.last_checked_time = System.currentTimeMillis();
            dataStorage.shopDao().insertCheckedShop(shop);
        });
    }

    public static final int FAV_CLEAR_ALL = 1;
    public static final int FAV_SYNC = 2;
    public static final int FAV_INSERT = 3;
    public static final int FAV_CLEAR_SINGLE = 4;
    public static final int FAV_LOAD = 5;
    public static final int FAV_UPDATE = 6;

    public void processFavorite(long chat_id, int product_id, boolean synced, ShopDataModels.ProductLike like, int task, TaskInterFace taskInterFace) {
        dataStorage.getStorageQueue().postRunnable(() -> {
            if (task == FAV_CLEAR_ALL) {
                dataStorage.shopDao().clearLikeTable();
                AndroidUtilities.runOnUIThread(() -> {
                    if (taskInterFace != null) {
                        taskInterFace.result(true);
                    }
                });
            } else if (task == FAV_SYNC) {
                List<ShopDataModels.ProductLike> faveList = dataStorage.shopDao().getProductForSync(false);
                if (faveList != null && faveList.size() > 1) {
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            ShopDataController.getInstance(currentAccount).syncFavProducts(faveList);
                        }
                    });

                }
            } else if (task == FAV_INSERT) {
                ShopDataModels.ProductLike productLike = new ShopDataModels.ProductLike();
                productLike.chat_id = chat_id;
                productLike.product_id = product_id;
                productLike.synced = synced;
                productLike.time_stamp = System.currentTimeMillis();
                dataStorage.shopDao().insert(productLike);
               if(true){
                   AndroidUtilities.runOnUIThread(new Runnable() {
                       @Override
                       public void run() {
                           ShopDataController.getInstance(currentAccount).likeProduct(productLike,true);

                       }
                   });
                   //processFavorite(0,0,false, null,FAV_LOAD,null);
               }
            } else if (task == FAV_CLEAR_SINGLE) {
                dataStorage.shopDao().delete(chat_id, product_id);
                ShopDataModels.ProductLike productLike = new ShopDataModels.ProductLike();
                productLike.chat_id = chat_id;
                productLike.product_id = product_id;
                productLike.synced = synced;
                productLike.time_stamp = System.currentTimeMillis();
                ShopDataController.getInstance(currentAccount).likeProduct(productLike,false);

            } else if (task == FAV_LOAD) {
                List<ShopDataModels.ProductLike> faveList = dataStorage.shopDao().getLikedProducts();
                if (faveList == null) {
                    faveList = new ArrayList<>();
                }
                List<ShopDataModels.ProductLike> finalFaveList = faveList;
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didFavLoadedFromLocal, finalFaveList);

                    }
                });
            } else if (task == FAV_UPDATE) {
                dataStorage.shopDao().update(like);
            }
        });
    }




    public void cacheProduct(int chat_id, String data) {
        dataStorage.getStorageQueue().postRunnable(new Runnable() {
            @Override
            public void run() {
                ShopDataModels.Product shop = new ShopDataModels.Product();
                shop.cache_time = System.currentTimeMillis();
                shop.channel_id = chat_id;
                shop.data = data;
                dataStorage.shopDao().insert(shop);
            }
        });
    }


    public void loadCache(int hash,DataLoadInterface dataLoadInterface){
        dataStorage.getStorageQueue().postRunnable(new Runnable() {
            @Override
            public void run() {
               ShopDataModels.LinkCache linkCache =  dataStorage.shopDao().getLinkHash(hash);
               if(linkCache != null){
                   AndroidUtilities.runOnUIThread(new Runnable() {
                       @Override
                       public void run() {
                           dataLoadInterface.data(linkCache.data);
                       }
                   });
               }
            }
        });
    }

    public void cacheLink(ShopDataModels.LinkCache linkCache ){
        dataStorage.getStorageQueue().postRunnable(new Runnable() {
            @Override
            public void run() {
                dataStorage.shopDao().insert(linkCache);
            }
        });
    }

    public void cacheReview(int chat_id, String data) {
        dataStorage.getStorageQueue().postRunnable(new Runnable() {
            @Override
            public void run() {
                ShopDataModels.Reviews shop = new ShopDataModels.Reviews();
                shop.cache_time = System.currentTimeMillis();
                shop.chat_id = chat_id;
                shop.data = data;
                dataStorage.shopDao().insert(shop);
            }
        });
    }

    public void cacheShop(int chat_id, String data) {
        dataStorage.getStorageQueue().postRunnable(new Runnable() {
            @Override
            public void run() {
                ShopDataModels.Shop shop = new ShopDataModels.Shop();
                shop.cache_time = System.currentTimeMillis();
                shop.channel_id = chat_id;
                shop.data = data;
                dataStorage.shopDao().insert(shop);
            }
        });
    }


//    public long createPendingTask(ShopDataModels.PendingTask pendingTask){
//        if(pendingTask == null){
//            return 0;
//        }
//        final long id = lastTaskId.getAndAdd(1);
//        dataStorage.getStorageQueue().postRunnable(new Runnable() {
//            @Override
//            public void run() {
//
//                //dataStorage.shopDao().insertPendingTask(pendingTask);
//            }
//        });
//
//        return id;
//    }
//
//
//    public void removePendingTask(final long id) {
//        dataStorage.getStorageQueue().postRunnable(() -> {
//            try {
//              //  dataStorage.shopDao().deletePendingTask(id);
//            } catch (Exception e) {
//                FileLog.e(e);
//            }
//        });
//    }

//    public void loadPendingTask(){
//       dataStorage.getStorageQueue().postRunnable(new Runnable() {
//           @Override
//           public void run() {
//             List<ShopDataModels.PendingTask> pendingTaskList = dataStorage.shopDao().loadPendingTask();
//             for(int a = 0,N = pendingTaskList.size();a < N; a++){
//                 ShopDataModels.PendingTask pendingTask = pendingTaskList.get(a);
//                 if(pendingTask == null){
//                     a--;
//                     N--;
//                     continue;
//                 }
//                 SerializedData data = new SerializedData(pendingTask.bytes);
//                int task_type =  data.readInt32(false);
//                switch (task_type) {
//                    case 1:
//                        TaskStore.FavTask favTask = new TaskStore.FavTask();
//                        favTask.chat_id = data.readInt64(false);
//                        favTask.product_id = data.readInt32(false);
//                        favTask.delete = data.readBool(false);
//                        AndroidUtilities.runOnUIThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                if(favTask.delete){
//                                    ShopDataController.getInstance(currentAccount).deleteProductFromFav(favTask.chat_id,favTask.product_id,pendingTask.id);
//
//                                }else{
//                                    ShopDataController.getInstance(currentAccount).addProductToFav(favTask.chat_id,favTask.product_id,pendingTask.id);
//
//                                }
//                            }
//                        });
//                        break;
//                }
//             }
//           }
//       });
//    }


//    public void loadProductConfiguration(String key,boolean forListing,boolean forDetail,int class_guid){
//        if(ShopUtils.isEmpty(key)){
//            return;
//        }
//        dataStorage.getStorageQueue().postRunnable(() -> {
//            ShopDataModels.ProductConfigData productConfiguration = dataStorage.shopDao().loadProductConfiguration(key);
//            if(productConfiguration == null){
//                ShopDataController.getInstance(currentAccount).loadProductConfiguration(key,forListing,forDetail,false,class_guid);
//            }else{
//                if(forListing){
//
//                    try {
//                            Gson gson = new Gson();
//                            JSONObject object = new JSONObject(productConfiguration.create_data);
//                            Iterator<String> fieldSetKeys = object.keys();
//                            ArrayList<ShopDataSerializer.FieldSet> fieldSets = new ArrayList<>();
//                           while (fieldSetKeys.hasNext()){
//                               String fieldSetKey = fieldSetKeys.next();
//                               ShopDataSerializer.FieldSet fieldSet =   gson.fromJson(object.getJSONObject(fieldSetKey).toString(), ShopDataSerializer.FieldSet.class);
//                               fieldSets.add(fieldSet);
//                           }
//                           AndroidUtilities.runOnUIThread(() -> NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didConfigrationLoaded,true,fieldSets,class_guid));
//                        }catch (Exception e){
//                            FileLog.e(e);
//                        }
//                    }else if(forDetail){
//
//                         try {
//                             Gson gson = new Gson();
//                             JSONArray jsonArray = new JSONArray(productConfiguration.detail_data);
//                             ArrayList<ShopDataSerializer.FieldSet> fieldSets = new ArrayList<>();
//                             for(int a =0; a < jsonArray.length(); a++){
//                                 JSONObject object = jsonArray.getJSONObject(a);
//                                 ShopDataSerializer.FieldSet fieldSet =  gson.fromJson(object.toString(), ShopDataSerializer.FieldSet.class);
//                                 fieldSets.add(fieldSet);
//                             }
//                             AndroidUtilities.runOnUIThread(() -> {
//                                 NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didConfigrationLoaded,true,fieldSets,class_guid);
//                             });
//                         }catch (Exception e){
//                             FileLog.e(e);
//                         }
//                     }else{
//                        try {
//                            ArrayList<ShopDataSerializer.FilterModel> filterModels;
//                            Gson gson = new Gson();
//                            Type founderListType = new TypeToken<ArrayList<ShopDataSerializer.FilterModel>>(){}.getType();
//                            filterModels = gson.fromJson(productConfiguration.filter_data,founderListType);
//                            if(filterModels != null){
//                                AndroidUtilities.runOnUIThread(() -> NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didConfigrationLoaded,true,filterModels,class_guid));
//                            }
//                        }catch (Exception e){
//                            FileLog.e(e);
//                        }
//                    }
//                }
//        });
//    }
//
//    public void cacheConf(ArrayList<ShopDataSerializer.ConfigObject> configObjects){
//        if(configObjects == null){
//            return;
//        }
//        dataStorage.getStorageQueue().postRunnable(() -> {
//            int count = configObjects.size();
//            for (int a = 0, N = count; a < N; a++) {
//                ShopDataSerializer.ConfigObject object = configObjects.get(a);
//                if(object == null){
//                    a--;
//                    N--;
//                    continue;
//                }
//                dataStorage.shopDao().insertProductConfiguration(object.toDatabaseModel());
//            }
//        });
//    }
//
//    public void cacheConf(ArrayList<ShopDataSerializer.ConfigObject> configObjects,boolean load,String key,boolean forListing,boolean forDetail,int class_guid){
//        if(configObjects == null){
//            return;
//        }
//        dataStorage.getStorageQueue().postRunnable(() -> {
//            int count = configObjects.size();
//            for (int a = 0, N = count; a < N; a++) {
//                ShopDataSerializer.ConfigObject object = configObjects.get(a);
//                if(object == null){
//                    a--;
//                    N--;
//                    continue;
//                }
//                dataStorage.shopDao().insertProductConfiguration(object.toDatabaseModel());
//
//            }
//
//            if(load){
//                AndroidUtilities.runOnUIThread(() -> loadProductConfiguration(key,forListing,forDetail,class_guid));
//            }
//
//        });
//    }
//
//    public void loadSortData(){
//        dataStorage.getStorageQueue().postRunnable(() -> {
//            ArrayList<ShopDataModels.Sort> sortData = dataStorage.shopDao().getSortData();
//            if(sortData == null || sortData.isEmpty()){
//
//                AndroidUtilities.runOnUIThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        ShopDataController.getInstance(currentAccount).updateProductConfiguration(() -> ShopDataController.getInstance(UserConfig.selectedAccount).loadSortData(true));
//                    }
//                });
//            }else{
//                AndroidUtilities.runOnUIThread(() -> {
//                    NotificationCenter.getInstance(UserConfig.selectedAccount).postNotificationName(NotificationCenter.didSortLoaded, true,sortData);
//                });
//            }
//        });
//    }
//
//    public void loadStoreFields(){
//        dataStorage.getStorageQueue().postRunnable(() -> {
//            ArrayList<ShopDataModels.StoreFiled> storeFileds = dataStorage.shopDao().getStoreFieldData();
//            if(storeFileds == null || storeFileds.isEmpty()){
//                AndroidUtilities.runOnUIThread(() -> {
//                    ShopDataController.getInstance(currentAccount).updateProductConfiguration(new ShopDataController.ConfigDelegate() {
//                        @Override
//                        public void didFinishUpdating() {
//                            ShopDataController.getInstance(UserConfig.selectedAccount).loadStoreFields(true);
//                        }
//                    });
//                    // ShopDataController.getInstance(currentAccount).updateProductConfiguration(() -> ShopDataController.getInstance(UserConfig.selectedAccount).loadShopBusiness(true));
//                });
//            }else{
//                AndroidUtilities.runOnUIThread(() -> {
//                    NotificationCenter.getInstance(UserConfig.selectedAccount).postNotificationName(NotificationCenter.didStoreFieldLoaded, true,storeFileds);
//                });
//            }
//        });
//    }
//
//
//    public void loadShopBusiness(){
//        dataStorage.getStorageQueue().postRunnable(() -> {
//            ArrayList<ShopDataSerializer.ProductType> businesses = dataStorage.shopDao().getBusiness();
//            if(businesses == null || businesses.isEmpty()){
//                AndroidUtilities.runOnUIThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        ShopDataController.getInstance(currentAccount).updateProductConfiguration(new ShopDataController.ConfigDelegate() {
//                            @Override
//                            public void didFinishUpdating() {
//                                ShopDataController.getInstance(UserConfig.selectedAccount).loadShopBusiness(true);
//                            }
//                        });
//
//                       // ShopDataController.getInstance(currentAccount).updateProductConfiguration(() -> ShopDataController.getInstance(UserConfig.selectedAccount).loadShopBusiness(true));
//                    }
//                });
//            }else{
//                AndroidUtilities.runOnUIThread(() -> {
//                    NotificationCenter.getInstance(UserConfig.selectedAccount).postNotificationName(NotificationCenter.didBusinessLoaded, true,businesses);
//                });
//            }
//        });
//    }
//
//    public void saveBusiness(ArrayList<ShopDataSerializer.ProductType> businesses ){
//
//        dataStorage.getStorageQueue().postRunnable(() -> dataStorage.shopDao().insertOrUpdateBusiness(businesses));
//    }
//
//    public void saveStoreFields(ArrayList<ShopDataModels.StoreFiled> storeFileds ){
//
//        dataStorage.getStorageQueue().postRunnable(() -> dataStorage.shopDao().insertStoreFields(storeFileds));
//    }
//
//
//    public void saveConf(ArrayList<ShopDataSerializer.ProductType> businesses, ArrayList<ShopDataSerializer.ConfigObject> configObjects, ArrayList<ShopDataModels.Sort> sortArrayList, ArrayList<ShopDataModels.StoreFiled> storeFileds, ShopDataController.ConfigDelegate configDelegate){
//        dataStorage.getStorageQueue().postRunnable(new Runnable() {
//            @Override
//            public void run() {
//                if(businesses != null && !businesses.isEmpty()){
//                    Log.i("youmase","saving busienss data in shopd data storeage");
//
//                    dataStorage.shopDao().insertOrUpdateBusiness(businesses);
//                }
//
//                if(configObjects != null && !configObjects.isEmpty()){
//                    int count = configObjects.size();
//                    for (int a = 0, N = count; a < N; a++) {
//                        ShopDataSerializer.ConfigObject object = configObjects.get(a);
//                        if(object == null){
//                            a--;
//                            N--;
//                            continue;
//                        }
//                        dataStorage.shopDao().insertProductConfiguration(object.toDatabaseModel());
//                    }
//                }
//
//                if(sortArrayList != null && !sortArrayList.isEmpty()){
//                    dataStorage.shopDao().insertSortData(sortArrayList);
//                    Log.i("youmase","inserting sort data");
//
//                }
//
//                if(storeFileds != null  && !storeFileds.isEmpty()){
//                    dataStorage.shopDao().insertStoreFields(storeFileds);
//                }
//
//                AndroidUtilities.runOnUIThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Log.i("youmase","done finishing so config update interace");
//
//                        if(configDelegate != null){
//                            configDelegate.didFinishUpdating();
//                        }
//                    }
//                });
//
//            }
//        });
//    }
//
//    public void  saveSort(ArrayList<ShopDataModels.Sort> sortArrayList){
//        dataStorage.getStorageQueue().postRunnable(new Runnable() {
//            @Override
//            public void run() {
//                dataStorage.shopDao().insertSortData(sortArrayList);
//            }
//        });
//    }
//
//    public void saveShop(ShopDataModels.Shop shop){
//        dataStorage.getStorageQueue().postRunnable(() -> dataStorage.shopDao().insertOrUpdateShop(shop));
//    }
//
//    public void loadShop(int channel_id,int class_guid){
//        dataStorage.getStorageQueue().postRunnable(() -> {
//            ShopDataModels.Shop shop = dataStorage.shopDao().getShopByChannelId(channel_id);
//            AndroidUtilities.runOnUIThread(() -> {
//                if(shop != null){
//                    NotificationCenter.getInstance(UserConfig.selectedAccount).postNotificationName(NotificationCenter.didShopLoaded,true,shop,class_guid);
//                }else{
//                    ShopDataController.getInstance(UserConfig.selectedAccount).loadShop(false,channel_id,true,class_guid);
//                }
//            });
//
//        });
//    }
//

//
//    public void loadProductsForShop(){
//
//    }

}
