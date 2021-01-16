package org.plus.features.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.ArrayList;
import java.util.List;

@Dao
public abstract class FeatureDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insert(FeatureDataModel.ProfileChange profileChange);

    @Delete
    public abstract void delete(FeatureDataModel.ProfileChange profileChange);

    @Query("SELECT * from profile_change where type = :type group by user_id order by timeStamp")
    public abstract List<FeatureDataModel.ProfileChange> getProfileChangesByType(int type);


    @Query("SELECT * from profile_change group by user_id order by timeStamp")
    public abstract List<FeatureDataModel.ProfileChange> getProfileChanges();


    @Query("DELETE  FROM profile_change")
    public abstract void clearProfileChange();


}
