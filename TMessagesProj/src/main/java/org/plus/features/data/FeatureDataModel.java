package org.plus.features.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

public class FeatureDataModel {


    @Entity(tableName = "profile_change")
    public static class ProfileChange {

        @PrimaryKey(autoGenerate = true)
        @NonNull
        @ColumnInfo(name = "id")
        public int id;

        @ColumnInfo(name = "user_id")
        public int user_id;

        @ColumnInfo(name = "timeStamp")
        public long timeStamp;

        @ColumnInfo(name = "type")
        public int type;

        @ColumnInfo(name = "photo_id")
        public long photo_id;
    }


}
