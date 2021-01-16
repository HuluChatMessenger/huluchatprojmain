package org.plus.database;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.google.android.exoplayer2.util.Log;

import org.plus.apps.business.data.ShopDataModels;
import org.plus.apps.business.data.ShopDataSerializer;
import org.plus.apps.business.data.ShopTableDAO;
import org.plus.features.data.FeatureDao;
import org.plus.features.data.FeatureDataModel;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.DispatchQueue;
import org.telegram.messenger.UserConfig;

import java.io.File;


@Database(
 entities = {
         ShopDataModels.ProductConfigurationObject.class,
         ShopDataModels.CheckedShop.class,
         ShopDataModels.RecentSearch.class,
         ShopDataModels.Shop.class,
         ShopDataModels.Reviews.class,
         ShopDataModels.Product.class,
         ShopDataModels.ProductLike.class,
         ShopDataModels.LinkCache.class,
         ShopDataModels.ProductFull.class,

         FeatureDataModel.ProfileChange.class,
 },
 version = 1,
 exportSchema = false

)

public abstract class DataStorage extends RoomDatabase{

    public static DispatchQueue storageQueue = new DispatchQueue("appDatabaseQueue");
    private static volatile DataStorage[] Instance = new DataStorage[UserConfig.MAX_ACCOUNT_COUNT];

    public abstract ShopTableDAO shopDao();
    public abstract FeatureDao featureDao();


    public static DataStorage getInstance(int num) {
        DataStorage localInstance = Instance[num];
        if (localInstance == null) {
            synchronized (DataStorage.class) {
                localInstance = Instance[num];
                if (localInstance == null) {
                    Instance[num] = localInstance = Room.databaseBuilder(ApplicationLoader.applicationContext,
                            DataStorage.class, "app_Database_" + num)
                            .enableMultiInstanceInvalidation()
                            .addCallback(callback)
                            .fallbackToDestructiveMigration()
                            .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                            .build();
                }
            }
        }
        return localInstance;
    }

    static  RoomDatabase.Callback callback = new Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
        }
    };


    public boolean  hasInstance(int num){
        return Instance[num] != null;
    }

    public DispatchQueue getStorageQueue() {
        return storageQueue;
    }


    public void clear(int account){
        storageQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
               deleteDatabaseFile("app_Database_" + account);
            }
        });
    }
    public  void deleteDatabaseFile( String databaseName) {
        File databases = new File(ApplicationLoader.applicationContext.getApplicationInfo().dataDir + "/databases");
        File db = new File(databases, databaseName);
        db.delete();
    }


}