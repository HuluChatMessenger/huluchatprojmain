package org.plus.apps.business.data;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

public class ShopDataModels {

    @Entity(tableName = "link_cache_table")
    public static class LinkCache{

        @NonNull
        @PrimaryKey
        @SerializedName("hash")
        public int hash;

        @SerializedName("data")
        public String data;

        @SerializedName("cache_time")
        public long cache_time;

    }


    @Entity(tableName = "reviews_table")
    public  static class Reviews{

        @NonNull
        @PrimaryKey
        @SerializedName("chat_id")
        public int chat_id;

        @SerializedName("data")
        public String data;

        @SerializedName("cache_time")
        public long cache_time;

    }

    @Entity(tableName = "shop_table")
    public static class Shop{

        @NonNull
        @PrimaryKey
        @SerializedName("channel_id")
        public int channel_id;

        @SerializedName("data")
        public String data;

        @SerializedName("cache_time")
        public long cache_time;
    }

    @Entity(tableName = "product_full_table",primaryKeys = {"product_id","chat_id"})
    public static class ProductFull{

        @SerializedName("product_id")
        public int product_id;

        @SerializedName("chat_id")
        public long chat_id;

        @SerializedName("data")
        public String data;

        @SerializedName("date")
        public long date;

    }

    @Entity(tableName = "product_table")
    public static class Product{

        @NonNull
        @PrimaryKey
        @SerializedName("channel_id")
        public int channel_id;

        @SerializedName("data")
        public String data;

        @SerializedName("product_id")
        public long cache_time;
    }

    @Entity(tableName = "product_like_table",primaryKeys = {"product_id","chat_id"})
    public static class ProductLike{

        @SerializedName("product_id")
        public int product_id;

        @SerializedName("chat_id")
        public long chat_id;

        @SerializedName("time_stamp")
        public long time_stamp;

        @SerializedName("synced")
        public boolean synced;
    }

    @Entity(tableName = "recent_search_table")
    public static class RecentSearch {

        @SerializedName("search")
        @PrimaryKey
        @NonNull
        public String search;


        @SerializedName("timeStamp")
        public long timeStamp;
    }

    @Entity(tableName = "product_configuration_object")
    public static class ProductConfigurationObject {

        @NonNull
        @ColumnInfo(name = "section_id")
        @PrimaryKey
        public String section_id;

        @ColumnInfo(name = "fieldsets")
        public String fieldsets;
    }

    @Entity(tableName = "checked_shop")
    public static class CheckedShop{

        @PrimaryKey
        @NonNull
        public int channel_id;

        @ColumnInfo(name = "exist")
        public  boolean exist;


        @ColumnInfo(name = "last_checked_time")
        public long last_checked_time;

    }

}
