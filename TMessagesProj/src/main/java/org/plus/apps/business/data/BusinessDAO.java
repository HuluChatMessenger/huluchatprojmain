//package org.plus.apps.business.data;
//
//
//import androidx.room.Dao;
//import androidx.room.Insert;
//import androidx.room.OnConflictStrategy;
//import androidx.room.Query;
//import androidx.room.Transaction;
//import androidx.room.Update;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Dao
//public abstract class BusinessDAO {
//
//    @Query("SELECT * FROM business_table")
//    abstract List<ShopDataSerializer.ProductType> _getBusinessInternal();
//
//    @Transaction
//    public ArrayList<ShopDataSerializer.ProductType> getBusiness() {
//        return new ArrayList<>(_getBusinessInternal());
//    }
//
//    @Insert(onConflict = OnConflictStrategy.IGNORE)
//    public  abstract List<Long> saveBusiness(ArrayList<ShopDataSerializer.ProductType> businesses);
//
//    @Update
//    public abstract void updateBusiness(ArrayList<ShopDataSerializer.ProductType> businesses);
//
//    @Transaction
//    public void insertOrUpdateBusiness(ArrayList<ShopDataSerializer.ProductType> businesses) {
//        List<Long> insertResult = saveBusiness(businesses);
//        List<ShopDataSerializer.ProductType> updateList = new ArrayList<>();
//
//        for (int i = 0; i < insertResult.size(); i++) {
//            if (insertResult.get(i) == -1) {
//                updateList.add(businesses.get(i));
//            }
//        }
//        if (!updateList.isEmpty()) {
//            updateBusiness(new ArrayList<>(updateList));
//        }
//    }
//
//    @Query("SELECT * FROM shop_table WHERE channel_id = :channel_id LIMIT 1")
//    public abstract ShopDataModels.Shop getShopByChannelId(int channel_id);
//
//    @Insert(onConflict = OnConflictStrategy.IGNORE)
//    public abstract Long saveShop(ShopDataModels.Shop businesses);
//
//    @Update
//    public abstract int updateShop(ShopDataModels.Shop businesses);
//
//    @Transaction
//    public   void insertOrUpdateShop(ShopDataModels.Shop shop) {
//        long insertResult = saveShop(shop);
//        if (insertResult == -1) {
//            updateShop(shop);
//        }
//    }
//
//    @Insert(onConflict = OnConflictStrategy.IGNORE)
//    public abstract List<Long> saveConfigurations(ArrayList<ShopDataModels.Configuration> businesses);
//
//    @Update
//    public abstract void updateConfigurations(ArrayList<ShopDataModels.Configuration> configurationArrayList);
//
//    @Transaction
//    public void insertOrUpdateConfigurations(ArrayList<ShopDataModels.Configuration> configurationArrayList) {
//        List<Long> insertResult = saveConfigurations(configurationArrayList);
//        List<ShopDataModels.Configuration> updateList = new ArrayList<>();
//
//        for (int i = 0; i < insertResult.size(); i++) {
//            if (insertResult.get(i) == -1) {
//                updateList.add(configurationArrayList.get(i));
//            }
//        }
//
//        if (!updateList.isEmpty()) {
//            updateConfigurations(new ArrayList<>(updateList));
//        }
//    }
//
//    @Query("SELECT * FROM checked_shop WHERE channel_id = :channel_id LIMIT 1")
//    public abstract ShopDataModels.CheckedShop getCheckedShop(int channel_id);
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    public abstract void insertCheckedShop(ShopDataModels.CheckedShop checkedShop);
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    public abstract void  insertProductConfiguration(ShopDataModels.ProductConfigData productConfigData);
//
//    @Query("SELECT * FROM config_data WHERE   type = :type LIMIT 1")
//    public abstract ShopDataModels.ProductConfigData loadProductConfiguration(String type);
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    public abstract void insertSortData(ArrayList<ShopDataModels.Sort> sortArrayList);
//
//    @Query("SELECT * FROM product_sort_table")
//    public abstract List<ShopDataModels.Sort> _getSortData();
//
//    public ArrayList<ShopDataModels.Sort> getSortData() {
//        return new ArrayList<>(_getSortData());
//    }
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    public abstract void insertStoreFields(ArrayList<ShopDataModels.StoreFiled> storeFileds);
//
//    @Query("SELECT * FROM store_filed_table")
//    public abstract List<ShopDataModels.StoreFiled> _getStoreFieldData();
//
//    @Transaction
//    public ArrayList<ShopDataModels.StoreFiled> getStoreFieldData() {
//        return new ArrayList<>(_getStoreFieldData());
//    }
//
//}
//
//
//
