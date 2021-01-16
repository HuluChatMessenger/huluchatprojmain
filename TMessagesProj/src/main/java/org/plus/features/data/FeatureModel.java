package org.plus.features.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

public class FeatureModel {

    @Entity(tableName =  "hidden_chat_table")
    public static class HiddenChat{

        @PrimaryKey
        @NonNull
        @ColumnInfo(name ="chat_id")
        public long chat_id;
    }


    @Entity(tableName = "multi_panel_table")
    public static  class Panel{

        @NonNull
        @PrimaryKey
        @ColumnInfo(name = "action")
        public int action;

        @ColumnInfo(name = "chat_type")
        public int chat_type;

        @ColumnInfo(name = "title")
        public String title;

        @ColumnInfo(name = "enabled")
        public int enabled;

        @ColumnInfo(name = "order")
        public int order;

        @ColumnInfo(name = "info")
        public String info;

        @ColumnInfo(name = "onlyAdmin")
        public boolean onlyAdmin;

    }


}
