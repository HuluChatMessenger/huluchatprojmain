//package org.plus.apps.business.data;
//
//import android.app.Activity;
//import android.content.SharedPreferences;
//import android.util.Log;
//import android.util.SparseArray;
//
//import com.google.gson.ExclusionStrategy;
//import com.google.gson.FieldAttributes;
//import com.google.gson.Gson;
//import com.google.gson.reflect.TypeToken;
//
//import org.json.JSONObject;
//import org.plus.database.DataStorage;
//import org.plus.net.RequestManager;
//import org.plus.net.ShopRequest;
//import org.telegram.messenger.AndroidUtilities;
//import org.telegram.messenger.ApplicationLoader;
//import org.telegram.messenger.BaseController;
//import org.telegram.messenger.DispatchQueue;
//import org.telegram.messenger.UserConfig;
//
//import java.lang.reflect.Type;
//import java.util.ArrayList;
//import java.util.Comparator;
//import java.util.List;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import okhttp3.ResponseBody;
//import retrofit2.Call;
//import retrofit2.Response;
//
//public class ShopController extends BaseController {
//
//    public static final String TAG = ShopController.class.getSimpleName();
//
//    public static ShopController[] instance = new ShopController[UserConfig.MAX_ACCOUNT_COUNT];
//    public static DispatchQueue shopControllerQueue = new DispatchQueue("ShopControllerQueue");
//
//    private volatile SparseArray<Call> callSparseArray = new SparseArray<>();
//    private AtomicInteger lastRequestToken = new AtomicInteger(1);
//
//    private ShopRequest shopRequest;
//    private ShopDataStorage shopDataStorage;
//    private SharedPreferences shopPref;
//    private ShopTableDAO shopTableDAO;
//
//
//    public static Comparator<ShopDataSerializer.FieldSet> fieldSetComparator = (fieldSet1, fieldSet2) -> {
//        if (fieldSet1.order > fieldSet2.order) {
//            return 1;
//        } else if (fieldSet1.order < fieldSet2.order) {
//            return -1;
//        }
//        return 0;
//    };
//
//    public static Comparator<ShopDataSerializer.Field> fieldComparator = (field1, field2) -> {
//        if (field1.order > field2.order) {
//            return -1;
//        } else if (field1.order < field2.order) {
//            return 1;
//        }
//        return 0;
//    };
//
//
//    public static Comparator<ShopDataSerializer.Field> productTypeComparator = (field1, field2) -> {
//        if (field1.order > field2.order) {
//            return -1;
//        } else if (field1.order < field2.order) {
//            return 1;
//        }
//        return 0;
//    };
//
//    ExclusionStrategy exclusionStrategy = new ExclusionStrategy() {
//        @Override
//        public boolean shouldSkipClass(Class<?> clazz) {
//            return false;
//        }
//
//        @Override
//        public boolean shouldSkipField(FieldAttributes field) {
//            return field.getAnnotation(Exclude.class) != null;
//        }
//    };
//
//
//    public ShopController(int num) {
//        super(num);
//       // shopDataStorage = ShopDataStorage.getInstance(currentAccount);
//        shopTableDAO = DataStorage.getDatabase(currentAccount).shopTableDAO();
//        shopRequest = RequestManager.getInstance(currentAccount).getShopInterface();
//        if (currentAccount == 0) {
//            shopPref = ApplicationLoader.applicationContext.getSharedPreferences("shopConfig", Activity.MODE_PRIVATE);
//        } else {
//            shopPref = ApplicationLoader.applicationContext.getSharedPreferences("shopConfig" + currentAccount, Activity.MODE_PRIVATE);
//        }
//    }
//
//
//    public static ShopController getGlobalInstance() {
//        return getInstance(0);
//    }
//
//    public static ShopController getInstance(int num) {
//        ShopController localInstance = instance[num];
//        if (localInstance == null) {
//            synchronized (ShopController.class) {
//                localInstance = instance[num];
//                if (localInstance == null) {
//                    instance[num] = localInstance = new ShopController(num);
//                }
//            }
//        }
//        return localInstance;
//    }
//
//    public static SharedPreferences getGlobalShopPreference() {
//        return getInstance(0).shopPref;
//    }
//
//    public static SharedPreferences getShopPreference(int account) {
//        return getInstance(account).shopPref;
//    }
//
//    public void cancelRequest(int reqId) {
//        Call call = callSparseArray.get(reqId);
//        if (call != null && !call.isCanceled()) {
//            call.cancel();
//        }
//    }
//
//    public void updateConfiguration(){
//
//        shopControllerQueue.postRunnable(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Response<ResponseBody> response = shopRequest.getConfigurations().execute();
//                    if(response.isSuccessful()) {
//                        JSONObject jsonObject = new JSONObject(response.body().string());
//                        JSONObject sections = jsonObject.getJSONObject("sections");
//                        JSONObject product_types = jsonObject.getJSONObject("product_types");
//                        Gson gson  = new Gson();
//
//                        Type listType = new TypeToken<List<ShopTables.ProductType>>() {}.getType();
//                        List<ShopTables.ProductType> json = gson.fromJson(product_types.toString(), listType);
//
//
//
//
//
//
//                    }
//                }catch (Exception exception){
//                    Log.d(TAG,exception.toString());
//                }
//            }
//        });
//    }
//
//
//
//
//}
