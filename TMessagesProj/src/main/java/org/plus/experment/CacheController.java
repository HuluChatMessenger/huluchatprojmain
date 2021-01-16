//package org.plus.experment;
//
//import androidx.annotation.NonNull;
//import androidx.room.ColumnInfo;
//import androidx.room.Dao;
//import androidx.room.Entity;
//import androidx.room.Insert;
//import androidx.room.OnConflictStrategy;
//import androidx.room.PrimaryKey;
//import androidx.room.Query;
//
//import org.plus.apps.business.data.ShopDataController;
//import org.plus.database.DataStorage;
//import org.telegram.messenger.DispatchQueue;
//import org.telegram.messenger.UserConfig;
//
//public  class CacheController{
//
//    public interface CacheCallBack{
//        void onCacheLoaded(CacheData data,boolean loaded);
//    }
//
//    public static final String TAG= ShopDataController.class.getSimpleName();
//
//    public static CacheController[] instance = new CacheController[UserConfig.MAX_ACCOUNT_COUNT];
//
//    public static DispatchQueue cacheQueue = new DispatchQueue("cacheQueue");
//
//    public static CacheController getInstance(int num) {
//        CacheController localInstance = instance[num];
//        if (localInstance == null) {
//            synchronized (CacheController.class) {
//                localInstance = instance[num];
//                if (localInstance == null) {
//                    instance[num] = localInstance = new CacheController(num);
//                }
//            }
//        }
//        return localInstance;
//    }
//
//
//
//    @Entity(tableName = "network_cache_data_table")
//    public static class CacheData{
//
//        @NonNull
//        @PrimaryKey
//        @ColumnInfo(name = "hash_code")
//        public int hash_code;
//
//
//        @ColumnInfo(name = "data")
//        public String data;
//    }
//
//
//    private int currentAccount;
//    public CacheController(int num){
//        currentAccount = num;
//    }
//
//
//
//    public void loadCache(int hashCode,CacheCallBack callBack){
//        cacheQueue.postRunnable(new Runnable() {
//            @Override
//            public void run() {
//              CacheData cacheData = DataStorage.getDatabase(currentAccount).cacheDao().getData(hashCode);
//              if(cacheData != null){
//                  callBack.onCacheLoaded(cacheData,true);
//              }else{
//                  callBack.onCacheLoaded(null,false);
//              }
//
//            }
//        });
//    }
//
//
//    public void saveCache(int hash,String data){
//        cacheQueue.postRunnable(new Runnable() {
//            @Override
//            public void run() {
//                CacheData cacheData   = new CacheData();
//                cacheData.data = data;
//                cacheData.hash_code = hash;
//                DataStorage.getDatabase(currentAccount).cacheDao().insertData(cacheData);
//            }
//        });
//    }
//
//
//}
