package org.plus.apps.business.data;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.ArrayList;
import java.util.List;


@Dao
public abstract class ShopTableDAO {


    @Query("SELECT * FROM product_configuration_object WHERE section_id = :sec_id LIMIT 1")
    public abstract ShopDataModels.ProductConfigurationObject getProductConfig(String sec_id);

    @Query("DELETE  FROM product_configuration_object")
    public abstract void clearConfig();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract long insert(ShopDataModels.ProductConfigurationObject configurationObject);

    @Query("SELECT * FROM checked_shop WHERE channel_id = :channel_id LIMIT 1")
    public abstract ShopDataModels.CheckedShop getCheckedShop(int channel_id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertCheckedShop(ShopDataModels.CheckedShop checkedShop);


    @Query("SELECT * FROM recent_search_table order by timeStamp")
    public abstract List<ShopDataModels.RecentSearch> _getSearchResult();

    public ArrayList<ShopDataModels.RecentSearch>  getSearchResult(){
      return   new ArrayList<>(_getSearchResult());
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public  abstract void insert(ShopDataModels.RecentSearch recentSearch);


    //start like table
    @Query("SELECT * FROM product_like_table WHERE product_id = :product_id AND chat_id = :chat_id")
    public abstract ShopDataModels.ProductLike getFavorite(int chat_id, int product_id);

    @Query("DELETE FROM product_like_table")
    public abstract void clearLikeTable();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insert(ShopDataModels.ProductLike productLike);

    @Update()//number of updated rows
    public abstract int update(ShopDataModels.ProductLike productLike);

    @Delete
    public abstract void delete(ShopDataModels.ProductLike productLike);

    @Query("DELETE FROM product_like_table WHERE chat_id =:chat_id AND product_id = :product_id")
    public abstract void delete(long chat_id,int product_id);


    @Query("SELECT * FROM product_like_table order by time_stamp")
    public abstract List<ShopDataModels.ProductLike> _getLikedProducts();

    public List<ShopDataModels.ProductLike>  getLikedProducts(){
        return _getLikedProducts();
    }

    @Query("SELECT * FROM product_like_table WHERE synced = :sync order by time_stamp DESC")
    public abstract List<ShopDataModels.ProductLike> getProductForSync(boolean sync);
    //end


    //start shop table
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insert(ShopDataModels.Shop shop);

    @Query("SELECT * FROM shop_table WHERE channel_id = :chat_id LIMIT 1")
    public abstract ShopDataModels.Shop getShop(int chat_id);

    //end

    //start prodcut table
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insert(ShopDataModels.Product shop);

    @Query("SELECT * FROM product_table WHERE channel_id = :chat_id LIMIT 1")
    public abstract ShopDataModels.Product getProduct(int chat_id);

    //end

    //start link cache
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insert(ShopDataModels.LinkCache shop);

    @Query("SELECT * FROM link_cache_table WHERE  hash = :hash LIMIT 1")
    public abstract ShopDataModels.LinkCache getLinkHash(int hash);

    @Query("DELETE  FROM link_cache_table")
    public abstract void clearLinkHash();

    //end



    //start review cache
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insert(ShopDataModels.Reviews shop);

    @Query("SELECT * FROM reviews_table WHERE  chat_id = :chat_id LIMIT 1")
    public abstract ShopDataModels.Reviews getReview(int chat_id);

    @Query("DELETE  FROM reviews_table")
    public abstract void clearReviews();

    //end





//    @Transaction
//    @Query("SELECT * FROM product_type_table")
//    public abstract List<ShopTables.ProductTypeWithSort> getProductTypes();
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    public abstract long insert(ShopTables.ProductType productType);
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    public abstract long insert(ShopTables.ProductTypeSort productType);
//
//    @Query("DELETE  FROM product_type_table")
//    public abstract long clearProductTypeTable();
//
//    @Query("DELETE  FROM product_sort_table")
//    public abstract long clearSortTable();
//
//    @Transaction
//    public void insert(ShopTables.ProductType productType, List<ShopTables.ProductTypeSort> productTypeSortList) {
//        final long productTypeId = insert(productType);
//        for (ShopTables.ProductTypeSort productTypeSort : productTypeSortList) {
//            productTypeSort.product_type_id  = productTypeId;
//            insert(productTypeSort);
//        }
//    }
//
//    @Transaction
//    public void clearConf(){
//        clearSortTable();
//        clearProductTypeTable();
//        clearFieldSetTable();
//        clearFieldTable();
//    }
//
//    @Transaction
//    @Query("SELECT * FROM field_sets_table WHERE section_id = :secId")
//    public abstract List<ShopTables.FieldSetWithFields> getFieldSetBySecId(String secId);
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    public abstract long insert(ShopTables.FieldSet fieldSet);
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    public abstract long insert(ShopTables.Field field);
//
//    @Query("DELETE  FROM field_sets_table")
//    public abstract long clearFieldSetTable();
//
//    @Query("DELETE  FROM field_table")
//    public abstract long clearFieldTable();
//
//    @Transaction
//    public void insert(ShopTables.FieldSet productType, List<ShopTables.Field> productTypeSortList) {
//        final long productTypeId = insert(productType);
//        for (ShopTables.Field productTypeSort : productTypeSortList) {
//            productTypeSort.field_id  = productTypeId;
//            insert(productTypeSort);
//        }
//    }


}
