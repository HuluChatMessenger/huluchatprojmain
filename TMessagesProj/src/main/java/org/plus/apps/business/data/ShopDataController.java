package org.plus.apps.business.data;

import android.app.Activity;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import android.webkit.URLUtil;

import androidx.core.app.ShareCompat;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;
import org.plus.apps.business.ShopUtils;
import org.plus.apps.business.ui.StoreActivity;
import org.plus.apps.business.ui.components.ProductImageLayout;
import org.plus.apps.ride.RideRequest;
import org.plus.apps.ride.data.RideObjects;
import org.plus.experment.DataLoader;
import org.plus.experment.PlusBuildVars;
import org.plus.features.PlusConfig;
import org.plus.features.PlusUtils;
import org.plus.net.APIError;
import org.plus.net.CountingRequestBody;
import org.plus.net.ErrorUtils;
import org.plus.net.ShopRequest;
import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BaseController;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.DispatchQueue;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.NativeByteBuffer;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLRPC;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

import static org.plus.apps.business.ui.StoreActivity.UI_SHOP_INSTA_VIEW;

public class ShopDataController extends BaseController implements NotificationCenter.NotificationCenterDelegate{

    public ConcurrentHashMap<String, ArrayList<ShopDataSerializer.FieldSet>> configHashMap = new ConcurrentHashMap<>(100, 1.0f, 2);
    public ArrayList<ShopDataSerializer.ProductType> productTypes = new ArrayList<>();

    public ArrayList<Integer> checkedShops = new ArrayList<>();


    //logout
    public void clear(){
        configHashMap.clear();;
        productTypes.clear();
        getShopPreference(currentAccount).edit().clear().commit();

    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if(id == NotificationCenter.didFavLoadedFromLocal){
//            ArrayList<ShopDataModels.ProductLike> faveList =  new ArrayList<>((List<ShopDataModels.ProductLike>)args[0]);
//            for(int a = 0 ; a < faveList.size(); a++){
//                ShopDataModels.ProductLike like = faveList.get(a);
//                if(like == null){
//                    continue;
//                }
//                ArrayList<ShopDataModels.ProductLike> productLikeArrayList = productLikeConcurrentHashMap.get(like.chat_id);
//                if(productLikeArrayList == null){
//                    productLikeArrayList = new ArrayList<>();
//                    productLikeConcurrentHashMap.put(like.chat_id,productLikeArrayList);
//                }
//                productLikeArrayList.add(like);
//            }
//            faveProductLoaded = true;
        }
    }


    public interface ResponseDelegate{
        void run(Object response,APIError error);
    }

    public interface ProductSearchCallBack{
        void run(Object response,APIError error,String next,int count);
    }

    public interface BooleanCallBack{

        void onResponse(boolean susscess);
    }

    public interface ConfigDelegate{
        void didFinishUpdating(boolean sucess);
    }

    public static final String TAG= ShopDataController.class.getSimpleName();

    public static ShopDataController[] instance = new ShopDataController[UserConfig.MAX_ACCOUNT_COUNT];

    public static DispatchQueue shopDataControllerQueue = new DispatchQueue("ShopDataControllerQueue");

    private volatile SparseArray<Call> callSparseArray = new SparseArray<>();
    private AtomicInteger lastRequestToken = new AtomicInteger(1);

    private  ShopRequest shopRequest;
    private SharedPreferences shopPref;
    private RideRequest rideRequest;

    public static ShopDataController getInstance(int num) {
        ShopDataController localInstance = instance[num];
        if (localInstance == null) {
            synchronized (ShopDataController.class) {
                localInstance = instance[num];
                if (localInstance == null) {
                    instance[num] = localInstance = new ShopDataController(num);
                }
            }
        }
        return localInstance;
    }


    public ShopDataController(int num) {
        super(num);

        if (currentAccount == 0) {
             shopPref = ApplicationLoader.applicationContext.getSharedPreferences("shopConfig", Activity.MODE_PRIVATE);
        } else {
             shopPref = ApplicationLoader.applicationContext.getSharedPreferences("shopConfig" + currentAccount, Activity.MODE_PRIVATE);
        }
        shopDataControllerQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                shopRequest = getRequestManager().getShopInterface();
                rideRequest = getRequestManager().getRideRequest();
            }
        });
    }



    public static SharedPreferences getGlobalShopPreference() {
        return getInstance(0).shopPref;
    }

    public static SharedPreferences getShopPreference(int account) {
        return getInstance(account).shopPref;
    }

    public void cancelRequest(int reqId){
       Call call =  callSparseArray.get(reqId);
       if(call != null && !call.isCanceled()){
           call.cancel();
       }
    }

    public void updateProductConfiguration(ConfigDelegate configDelegate){
        shopDataControllerQueue.postRunnable(() -> {
            try {
                Response<ResponseBody> response = shopRequest.getConfigurations().execute();
                if(response.isSuccessful()) {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray sectionArray = jsonObject.getJSONArray("sections");
                    ArrayList<ShopDataSerializer.ConfigurationsObject>  configurationsObjects = new ArrayList<>();

                    for(int a = 0; a < sectionArray.length(); a++){
                        ShopDataSerializer.ConfigurationsObject configurationsObject = new ShopDataSerializer.ConfigurationsObject();
                        JSONObject object = sectionArray.getJSONObject(a);
                        configurationsObject.section_id = object.getString("section_id");
                        configurationsObject.fieldsets = object.getJSONArray("fieldsets").toString();
                        configurationsObjects.add(configurationsObject);
                    }

                    ShopDataSerializer.ConfigurationsObject configurationsObject = new ShopDataSerializer.ConfigurationsObject();
                    configurationsObject.section_id = "product_types";
                    configurationsObject.fieldsets = jsonObject.getJSONArray("product_types").toString();
                    configurationsObjects.add(configurationsObject);

                    getShopDataStorage().saveConf(configurationsObjects, new ConfigDelegate() {
                        @Override
                        public void didFinishUpdating(boolean success) {
                            if(configDelegate != null){
                                configDelegate.didFinishUpdating(success);
                            }
                        }
                    });

                }
            }catch (Exception e) {
                FileLog.debug(e.getMessage(),TAG);
                AndroidUtilities.runOnUIThread(() -> {
                    if(configDelegate != null){
                        configDelegate.didFinishUpdating(false);
                    }
                });
            }
        });
    }

    public int updateProduct(int channel_id,int product_id,Map<String,Object> fields){
        int reqId = lastRequestToken.getAndIncrement();
        shopDataControllerQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    Call<ResponseBody> call = shopRequest.updateProduct(ShopUtils.toBotChannelId(channel_id),product_id,fields);
                    callSparseArray.put(reqId,call);
                    Response<ResponseBody> response =  call.execute();
                    callSparseArray.remove(reqId);
                    if(response.isSuccessful()){
                        Gson gson = new Gson();
                        ShopDataSerializer.Product product =  gson.fromJson(response.body().string(), ShopDataSerializer.Product.class);
                        AndroidUtilities.runOnUIThread(() -> NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didProductUpdated,true,product));
                    }else {
                        APIError apiError = ErrorUtils.createError(response.errorBody(),response.code());
                        AndroidUtilities.runOnUIThread(() -> NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didProductUpdated,false,apiError));
                    }
                }catch (Exception e){
                    callSparseArray.remove(reqId);
                    FileLog.e(e);
                    postErrorNotification(NotificationCenter.didProductUpdated,ErrorUtils.createNetworkError(),-1);
                }
            }
        });
        return reqId;
    }

    public void loadSortForProductType(String type){
        if(ShopUtils.isEmpty(type)){
            return;
        }
        getShopDataStorage().loadSortForProductType(type);
    }

    public void loadSectionConfiguration(String section,boolean cache,int class_guid){
        if(ShopUtils.isEmpty(section)){
            return;
        }
        boolean loaded  = false;
        if(section.equals("product_types") && !productTypes.isEmpty()){
            loaded = true;
            AndroidUtilities.runOnUIThread(() -> {
                NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didConfigrationLoaded, true, section, productTypes, class_guid);
            });
        }else{
            if(configHashMap.get(section) != null){
                ArrayList<ShopDataSerializer.FieldSet>  fieldSets  =  configHashMap.get(section);
                loaded = !fieldSets.isEmpty();
                AndroidUtilities.runOnUIThread(() -> {
                    NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didConfigrationLoaded, true, section, fieldSets, class_guid);
                });
            }
        }
        if(loaded){
            return;
        }
        if(cache){
            getShopDataStorage().loadSectionConfiguration(section,class_guid);
        }else{
            updateProductConfiguration(new ConfigDelegate() {
                @Override
                public void didFinishUpdating(boolean sucess) {
                    loadSectionConfiguration(section,true,class_guid);

                }
            });
        }
    }

    public int  updateShop(int channel_id,Map<String,Object> shop,boolean refresh){
        int reqId = lastRequestToken.getAndIncrement();
        shopDataControllerQueue.postRunnable(() -> {
            try {
                Call<ResponseBody> call = shopRequest.updateShop(ShopUtils.toBotChannelId(channel_id),shop);
                callSparseArray.put(reqId,call);
                Response<ResponseBody> response =  call.execute();
                callSparseArray.remove(reqId);
                if(response.isSuccessful()){
                    AndroidUtilities.runOnUIThread(() -> {
                        if(refresh){
                            ShopDataController.getInstance(currentAccount).loadShop(channel_id,-1);
                        }
                        AndroidUtilities.runOnUIThread(() -> NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didShopUpdated,true));
                    });
                }
            }catch (Exception e){
                callSparseArray.remove(reqId);
                FileLog.e(e);
            }
        });
        return reqId;
    }

    public int uploadPhoto(String photo,DataLoader.DataLoaderDelegate dataLoaderDelegate){

        int reqId = lastRequestToken.getAndIncrement();
        shopDataControllerQueue.postRunnable(() -> {
            File file = new File(photo);
            if(!file.exists()){
                return;
            }

            RequestBody uploadRequestBody = RequestBody.create( MediaType.parse("image/*"),file);
            CountingRequestBody countingRequestBody = new CountingRequestBody(uploadRequestBody, new CountingRequestBody.FileUploadTaskDelegate() {
                @Override
                public void didFinishUploadingFile(long id) {

                }

                @Override
                public void didFailedUploadingFile() {

                }

                @Override
                public void didChangedUploadProgress(long uploadedSize, long totalSize) {
                    AndroidUtilities.runOnUIThread(() -> {
                        if(dataLoaderDelegate != null){
                            dataLoaderDelegate.fileUploadProgressChanged(photo,uploadedSize,totalSize);
                        }
                    });
                }
            });
            MultipartBody.Part body = MultipartBody.Part.createFormData("photo", file.getName(), countingRequestBody);
            String order = "1";
            RequestBody description = RequestBody.create(MultipartBody.FORM, order);
            try {
                Call<ShopDataSerializer.ImageUploadResult> call =  shopRequest.uploadPhoto(body,description);
                callSparseArray.put(reqId,call);
                Response<ShopDataSerializer.ImageUploadResult>  response =  call.execute();
                callSparseArray.remove(reqId);
                if(response.isSuccessful() && response.body() != null){
                    AndroidUtilities.runOnUIThread(() -> {
                        if(dataLoaderDelegate != null){
                            dataLoaderDelegate.fileDidUploaded(photo,response.body().id,0);
                        }
                    });

                }
            } catch (Exception e) {
                callSparseArray.remove(reqId);
            }
        });

        return reqId;
    }

    public int uploadPhoto(String photo, DataLoader.PhotoUploadDelegate dataLoaderDelegate){
        int reqId = lastRequestToken.getAndIncrement();
        shopDataControllerQueue.postRunnable(() -> {
            File file = new File(photo);
            if(!file.exists()){
                return;
            }
            RequestBody uploadRequestBody = RequestBody.create( MediaType.parse("image/*"),file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("photo", file.getName(), uploadRequestBody);
            String order = "1";
            RequestBody description = RequestBody.create(MultipartBody.FORM, order);
            try {
                Call<ShopDataSerializer.ImageUploadResult> call =  shopRequest.uploadPhoto(body,description);
                callSparseArray.put(reqId,call);
                Response<ShopDataSerializer.ImageUploadResult>  response =  call.execute();
                callSparseArray.remove(reqId);
                if(response.isSuccessful() && response.body() != null){
                    AndroidUtilities.runOnUIThread(() -> {
                        if(dataLoaderDelegate != null){
                            dataLoaderDelegate.onPhotoUploaded(photo,response.body().id);
                        }
                    });

                }
            } catch (Exception e) {
                callSparseArray.remove(reqId);
            }
        });

        return reqId;
    }

    public void uploadImages(ProductImageLayout.ImageInput imageInput, CountingRequestBody.FileUploadTaskDelegate delegate){
      if(imageInput  == null){
          return;
      }
        String image_loc = "";
        if(imageInput.bigSize != null){
            image_loc  = FileLoader.getPathToAttach(imageInput.bigSize, false).getAbsolutePath();
        }else if(imageInput.smallSize != null){
            image_loc  = FileLoader.getPathToAttach(imageInput.smallSize, false).getAbsolutePath();
        }
        if(TextUtils.isEmpty(image_loc)){
            return;
        }
        File file = new File(image_loc);
        if(!file.exists()){
            return;
        }
      shopDataControllerQueue.postRunnable(new Runnable() {
          @Override
          public void run() {
              try {
                  RequestBody uploadRequestBody = RequestBody.create( MediaType.parse("image/*"),file);
                  CountingRequestBody countingRequestBody = new CountingRequestBody(uploadRequestBody,delegate);
                  MultipartBody.Part body = MultipartBody.Part.createFormData("photo", file.getName(), countingRequestBody);
                  String order = String.valueOf(imageInput.pos);
                  RequestBody description = RequestBody.create(MultipartBody.FORM, order);
                  Response<ShopDataSerializer.ImageUploadResult> response =  shopRequest.uploadPhoto(body,description).execute();
                  if(response.isSuccessful()){
                      AndroidUtilities.runOnUIThread(new Runnable() {
                          @Override
                          public void run() {
                              delegate.didFinishUploadingFile(response.body().id);
                          }
                      });
                  }else{
                      AndroidUtilities.runOnUIThread(new Runnable() {
                          @Override
                          public void run() {
                              delegate.didFailedUploadingFile();
                          }
                      });
                  }
              }catch (Exception exception){
                  if(PlusBuildVars.LOGS_ENABLED){
                      Log.i(TAG,"uploadImages:" + exception.getMessage());
                  }
              }
          }
      });
    }

    public void listProduct(int channel_id, String bus_type, ArrayList<ProductImageLayout.ImageInput> imageInputs, Map<String,Object>  product_list_request){
        if(product_list_request == null || imageInputs == null || bus_type== null){
            return;
        }
        shopDataControllerQueue.postRunnable(() -> {
            ArrayList<Long> photoIds = new ArrayList<>();
            for(int a = 0;  a < imageInputs.size(); a++){
                ProductImageLayout.ImageInput  imageInput = imageInputs.get(a);
                if(imageInput == null){
                    continue;
                }
                String image_loc;
                if(imageInput.bigSize != null){
                    image_loc  = FileLoader.getPathToAttach(imageInput.bigSize, false).getAbsolutePath();
                }else if(imageInput.smallSize != null){
                    image_loc  = FileLoader.getPathToAttach(imageInput.smallSize, false).getAbsolutePath();
                }else{
                    continue;
                }
                if(TextUtils.isEmpty(image_loc)){
                   continue;
                }
                File file = new File(image_loc);
                if(!file.exists()){
                    continue;
                }
                RequestBody uploadRequestBody = RequestBody.create( MediaType.parse("image/*"),file);
                MultipartBody.Part body = MultipartBody.Part.createFormData("photo", file.getName(), uploadRequestBody);
                String order = String.valueOf(imageInput.pos);
                RequestBody description = RequestBody.create(MultipartBody.FORM, order);
                try {
                    Response<ShopDataSerializer.ImageUploadResult> response =  shopRequest.uploadPhoto(body,description).execute();
                    if(response.isSuccessful() && response.body() != null){
                       photoIds.add(response.body().id);
                    }
                } catch (Exception e) {
                  Log.i(TAG, "error in uploading images = > " + e.getMessage());
                }
            }

            if(photoIds.isEmpty()){
                return;
            }

            try {
                product_list_request.put("pictures",photoIds);
                Response<ResponseBody> response = shopRequest.listProduct(ShopUtils.toBotChannelId(channel_id),bus_type,product_list_request).execute();
                if(response.isSuccessful()){
                    Gson gson = new Gson();
                    ShopDataSerializer.Product product =  gson.fromJson(response.body().string(), ShopDataSerializer.Product.class);
                    AndroidUtilities.runOnUIThread(() -> NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didProductListed,true,product));
                }else {
                    APIError apiError = ErrorUtils.createError(response.errorBody(),response.code());
                    AndroidUtilities.runOnUIThread(() -> NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didProductListed,false,apiError));
                }
            }catch (Exception e){
                Log.i(TAG, "error in uploading product = > " + e.getMessage());
            }
        });

    }


    public void checkShop(int channel_id,BooleanCallBack booleanCallBack){

        shopDataControllerQueue.postRunnable(() -> {
            try {
                Response<okhttp3.ResponseBody> bodyResponse =  shopRequest.checkShop(ShopUtils.toBotChannelId(channel_id)).execute();
                if(bodyResponse.isSuccessful()){
                    getShopDataStorage().insertCheckedShop(channel_id);
                    AndroidUtilities.runOnUIThread(() -> booleanCallBack.onResponse(true));
                }else{
                    AndroidUtilities.runOnUIThread(() -> booleanCallBack.onResponse(false));
                }
            }catch (Exception e){
                Log.d(TAG,":"+ e.getMessage());
            }
        });
    }

    public void checkShop(int channel_id){
        Log.i("berjan","did check shopcalled for chat id" + channel_id);
        getShopDataStorage().checkShop(channel_id, exist -> {
            if(!exist){
                checkShopFromServer(channel_id);
            }else{
                AndroidUtilities.runOnUIThread(() ->  NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didCheckShop,true,channel_id));
            }
        });
    }

    public void checkShopFromServer(int chat_id){
            shopDataControllerQueue.postRunnable(() -> {
                try {
                    Response<okhttp3.ResponseBody> bodyResponse =  shopRequest.checkShop(ShopUtils.toBotChannelId(chat_id)).execute();
                    AndroidUtilities.runOnUIThread(() -> {
                       if(bodyResponse.isSuccessful()){
                           getShopDataStorage().insertCheckedShop(chat_id);
                           NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didCheckShop,true,chat_id);
                       }else if(bodyResponse.code() == 404){
                           NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didCheckShop,false,chat_id);
                       }
                    });
                }catch (Exception e){
                    FileLog.debug(e.getMessage());
                }
            });
    }


    public int loadShop(int channel_id,int classGuid){
        int reqId = lastRequestToken.getAndIncrement();
        shopDataControllerQueue.postRunnable(() -> {
            try {
                Call<ResponseBody> call = shopRequest.loadShopByChannelId(ShopUtils.toBotChannelId(channel_id));
                callSparseArray.put(reqId,call);
                Response<ResponseBody> response =  call.execute();
                callSparseArray.remove(reqId);
                if(response.isSuccessful()){
                    Gson gson = new Gson();
                    String data = response.body().string();
                    ShopDataSerializer.Shop shop = gson.fromJson(data, ShopDataSerializer.Shop.class);
                    AndroidUtilities.runOnUIThread(() -> {
                        NotificationCenter.getInstance(UserConfig.selectedAccount).postNotificationName(NotificationCenter.didShopLoaded,true,shop,classGuid);
                        getShopDataStorage().cacheShop(channel_id,data);
                    });
                }else{
                    postErrorNotification(NotificationCenter.didShopLoaded,parseNetworkError(response.errorBody(),response.code()),classGuid);
                }
            }catch (Exception e){
                callSparseArray.remove(reqId);
                getDataStorage().getStorageQueue().postRunnable(() -> {
                    ShopDataModels.Shop shop =  getDataStorage().shopDao().getShop(channel_id);
                    if(shop != null){
                        Gson gson = new Gson();
                        String data = shop.data;
                        ShopDataSerializer.Shop shopS = gson.fromJson(data, ShopDataSerializer.Shop.class);
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                NotificationCenter.getInstance(UserConfig.selectedAccount).postNotificationName(NotificationCenter.didShopLoaded,true,shopS,classGuid);
                            }
                        });
                    }else{
                        postErrorNotification(NotificationCenter.didShopLoaded,ErrorUtils.createNetworkError(),classGuid);
                    }
                });
            }
        });
        return reqId;
    }

    public int createShop(Map<String,Object> objectMap,int class_guid){
       int reqId = lastRequestToken.getAndIncrement();
        shopDataControllerQueue.postRunnable(() -> {
            try {
                Call<SR_object.shop_create_res> call = shopRequest.createShop(objectMap);
                callSparseArray.put(reqId,call);
                Response<SR_object.shop_create_res> response =  call.execute();
                callSparseArray.remove(reqId);
                if(response.isSuccessful()){
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didShopCreated,true,response.body().telegramID);
                        }
                    });
                }else{
                   postErrorNotification(NotificationCenter.didShopCreated,parseNetworkError(response.errorBody(),response.code()),class_guid);
                }
            }catch (Exception e){
                callSparseArray.remove(reqId);
                postErrorNotification(NotificationCenter.didShopLoaded,ErrorUtils.createNetworkError(),class_guid);
                if(PlusBuildVars.LOGS_ENABLED){
                    Log.d(TAG,"createShop:" + e.toString());
                }
            }
        });
        return reqId;
    }

    public void loadProductFull(int channel_id,int product_id,int class_guid){
        shopDataControllerQueue.postRunnable(() -> {
            try {
                Response<ResponseBody> response =  shopRequest.loadProductById(ShopUtils.toBotChannelId(channel_id),product_id).execute();
                if(response.isSuccessful()){
                    JSONObject object = new JSONObject(response.body().string());
                    HashMap<String,Object> otherFields = new HashMap<>();
                    Iterator<String> keys =  object.keys();
                    while (keys.hasNext()){
                       String key = keys.next();
                       Object val = object.get(key);
                       otherFields.put(key,val);
                   }
                   AndroidUtilities.runOnUIThread(() -> NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.productFullLoaded,true,otherFields,class_guid));
                }else{
                    postErrorNotification(NotificationCenter.productFullLoaded,parseNetworkError(response.errorBody(),response.code()),class_guid);
                }
            } catch (Exception e) {
                postErrorNotification(NotificationCenter.didShopLoaded,ErrorUtils.createNetworkError(),class_guid);
                if(PlusBuildVars.LOGS_ENABLED){
                    Log.d(TAG,"loadProductFull:" + e.toString());
                }
            }

        });
    }

    public void loadRecentSearchForShop(ProductSearchCallBack callBack){
        getShopDataStorage().loadRecentSearch(callBack);
    }

    public void loadMoreSearchProduct(String link,ProductSearchCallBack callBack){
        shopDataControllerQueue.postRunnable(() -> {
            try {
                Response<ResponseBody> response =  shopRequest.loadMoreProduct(link).execute();
                if(response.isSuccessful() && response.body() != null){
                    Gson gson = new Gson();
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray array = jsonObject.getJSONArray("results");
                    String next = jsonObject.getString("next");
                    int count = jsonObject.getInt("count");

                    ArrayList<ShopDataSerializer.Product> products = new ArrayList<>();
                    for(int a = 0; a < array.length(); a++){
                        ShopDataSerializer.Product product =  gson.fromJson(array.getJSONObject(a).toString(), ShopDataSerializer.Product.class);
                        products.add(product);
                    }
                    Collections.sort(products, (o1, o2) -> Integer.compare(o2.id, o1.id));
                   // AndroidUtilities.runOnUIThread(() -> NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didProductLoaded,true,products,classGuid, next,count));

                   AndroidUtilities.runOnUIThread(new Runnable() {
                       @Override
                       public void run() {
                           if(callBack != null) {
                               callBack.run(products,null,next,count);
                           }
                       }
                   });

                }else{
                    postErrorNotification(NotificationCenter.didProductLoaded,new APIError(),-1);
                }
            }catch (Exception e){
                postErrorNotification(NotificationCenter.didProductLoaded,new APIError(),-1);
                Log.d(TAG, "loadProductsForShop = " + e.toString());
            }

        });

    }


    public void loadMoreFeatureProduct(String link,ProductSearchCallBack callBack){
        shopDataControllerQueue.postRunnable(() -> {
            try {
                Response<ResponseBody> response =  shopRequest.loadMoreProduct(link).execute();
                String next = null;
                int count = - 1;
                APIError apiError = null;
                ArrayList<ShopDataSerializer.FeaturedProduct> products = new ArrayList<>();
                if(response.isSuccessful()) {
                    Gson gson = new Gson();
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray array = jsonObject.getJSONArray("results");
                    next = jsonObject.getString("next");
                    count = jsonObject.getInt("count");

                    for (int a = 0; a < array.length(); a++) {
                        ShopDataSerializer.FeaturedProduct product = gson.fromJson(array.getJSONObject(a).toString(), ShopDataSerializer.FeaturedProduct.class);
                        products.add(product);
                    }
                    Collections.sort(products, (o1, o2) -> Integer.compare(o2.id, o1.id));
                }else{
                    apiError = new APIError();
                    apiError.setMessage(response.errorBody().string());
                    apiError.setStatusCode(response.code());
                }

                if(callBack != null){
                    callBack.run(products,apiError,next,count);
                }
            }catch (Exception e){
                callBack.run(null,ErrorUtils.createNetworkError(),null,-1);
                if(PlusBuildVars.LOGS_ENABLED){
                    Log.d(TAG, "loadProductsForShop = " + e.toString());
                }
            }
        });
    }



    public void loadMoreProductForShop(String link,int classGuid){
        shopDataControllerQueue.postRunnable(() -> {
            try {
                Response<ResponseBody> response =  shopRequest.loadMoreProduct(link).execute();
                if(response.isSuccessful() && response.body() != null){
                    Gson gson = new Gson();
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray array = jsonObject.getJSONArray("results");
                    String next = jsonObject.getString("next");
                    int count = jsonObject.getInt("count");

                    ArrayList<ShopDataSerializer.Product> products = new ArrayList<>();
                    for(int a = 0; a < array.length(); a++){
                        ShopDataSerializer.Product product =  gson.fromJson(array.getJSONObject(a).toString(), ShopDataSerializer.Product.class);
                        products.add(product);
                    }
                    Collections.sort(products, (o1, o2) -> Integer.compare(o2.id, o1.id));
                    AndroidUtilities.runOnUIThread(() -> NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didProductLoaded,true,products,classGuid, next,count));


                }else{
                    postErrorNotification(NotificationCenter.didProductLoaded,new APIError(),classGuid);
                }
            }catch (Exception e){
                postErrorNotification(NotificationCenter.didProductLoaded,new APIError(),classGuid);
                Log.d(TAG, "loadProductsForShop = " + e.toString());
            }

        }

        );

    }

    public void loadMoreReviewForShop(String link,int classGuid){
        shopDataControllerQueue.postRunnable(() -> {
            try {
                Response<ResponseBody> response = shopRequest.loadMoreReview(link).execute();
                if(response.isSuccessful()){
                    if(response.body() != null){
                        ArrayList<ShopDataSerializer.Review> reviews = new ArrayList<>();
                        Gson gson = new Gson();
                        JSONObject rawObject = new JSONObject(response.body().string());
                        int count = rawObject.getInt("count");
                        String next = rawObject.getString("next");
                        JSONArray dataArray = rawObject.getJSONArray("results");
                        for (int a = 0; a < dataArray.length(); a++){
                            JSONObject jsonObject = dataArray.getJSONObject(a);
                            ShopDataSerializer.Review review = gson.fromJson(jsonObject.toString(),ShopDataSerializer.Review.class);
                            reviews.add(review);
                        }
                        AndroidUtilities.runOnUIThread(() -> NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didReviewLoaded,true,reviews,next,count));
                    }
                }else{
                    APIError apiError = ErrorUtils.parseError(response);
                    AndroidUtilities.runOnUIThread(() ->  NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didReviewLoaded,false,apiError));
                }
            }catch (Exception e){
                Log.d(TAG,":"+ e.getMessage());
            }
        });
    }

    public void loadProductsForShop(int channel_id, ShopDataSerializer.ProductType.Sort sort,int classGuid){
        shopDataControllerQueue.postRunnable(() -> {
            try {
                Response<ResponseBody> response =  shopRequest.loadProductsForShop(ShopUtils.toBotChannelId(channel_id),sort != null?sort.sortby:"").execute();
                if(response.isSuccessful() && response.body() != null){
                    Gson gson = new Gson();
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray array = jsonObject.getJSONArray("results");
                    String next = jsonObject.getString("next");
                    int count = jsonObject.getInt("count");

                    ArrayList<ShopDataSerializer.Product> products = new ArrayList<>();
                    for(int a = 0; a < array.length(); a++){
                        ShopDataSerializer.Product product =  gson.fromJson(array.getJSONObject(a).toString(), ShopDataSerializer.Product.class);
                        products.add(product);
                    }
                    Collections.sort(products, (o1, o2) -> Integer.compare(o2.id, o1.id));
                    AndroidUtilities.runOnUIThread(() ->  getShopDataStorage().cacheProduct(channel_id,jsonObject.toString()));
                    AndroidUtilities.runOnUIThread(() -> NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didProductLoaded,true,products,classGuid, next,count));

                }else{
                    postErrorNotification(NotificationCenter.didProductLoaded,parseNetworkError(response.errorBody(),response.code()),classGuid);
                }
            }catch (Exception e){
              getDataStorage().getStorageQueue().postRunnable(() -> {
                    ShopDataModels.Product pro = getDataStorage().shopDao().getProduct(channel_id);
                    if(pro != null){
                     try {
                         Gson gson = new Gson();
                         String data = pro.data;
                         JSONObject jsonObject = new JSONObject(data);
                         JSONArray array = jsonObject.getJSONArray("results");
                         String next = jsonObject.getString("next");
                         int count = jsonObject.getInt("count");

                         ArrayList<ShopDataSerializer.Product> products = new ArrayList<>();
                         for(int a = 0; a < array.length(); a++){
                             ShopDataSerializer.Product product =  gson.fromJson(array.getJSONObject(a).toString(), ShopDataSerializer.Product.class);
                             products.add(product);
                         }
                         Collections.sort(products, (o1, o2) -> Integer.compare(o2.id, o1.id));
                         AndroidUtilities.runOnUIThread(new Runnable() {
                             @Override
                             public void run() {
                                 AndroidUtilities.runOnUIThread(() -> NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didProductLoaded,true,products,classGuid, next,count));
                             }
                         });
                     }catch (Exception ignore){

                     }
                    }else{
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                APIError apiError = new APIError();
                                apiError.setStatusCode(1);
                                apiError.setMessage("product not found");
                                postErrorNotification(NotificationCenter.didProductLoaded,apiError,classGuid);
                            }
                        });
                    }

                });
            }

        });

    }

    public void postErrorNotification(int not_id,APIError error,int class_guid){
        AndroidUtilities.runOnUIThread(() -> {
            if(not_id == NotificationCenter.didShopLoaded){
                NotificationCenter.getInstance(currentAccount).postNotificationName(not_id,false,error,class_guid);
            }if(not_id == NotificationCenter.didFavoriteProductLoaded){
                NotificationCenter.getInstance(currentAccount).postNotificationName(not_id,false,class_guid,error);
            }else{
                NotificationCenter.getInstance(currentAccount).postNotificationName(not_id,false,error,class_guid);
            }
        });
    }

    public void loadProducts(int channel_id, String bus_type, Map<String,Object> filters, int classGuid){
        shopDataControllerQueue.postRunnable(() -> {
            try {
                Response<ResponseBody> response;
                if(ShopUtils.isEmpty(bus_type)){
                    response =  shopRequest.loadProducts(ShopUtils.toBotChannelId(channel_id),filters).execute();
                }else{
                    response =  shopRequest.loadProducts(ShopUtils.toBotChannelId(channel_id),bus_type,filters).execute();
                }
                if(response.isSuccessful()){
                    Gson gson = new Gson();
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray array = jsonObject.getJSONArray("results");
                    String next = jsonObject.getString("next");
                    int count = jsonObject.getInt("count");
                    ArrayList<ShopDataSerializer.Product> products = new ArrayList<>();
                    for(int a = 0; a < array.length(); a++){
                        ShopDataSerializer.Product product =  gson.fromJson(array.getJSONObject(a).toString(), ShopDataSerializer.Product.class);
                        products.add(product);
                    }
                    AndroidUtilities.runOnUIThread(() -> NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didProductLoaded,true,products,classGuid,next,count));
                }else{
                    postErrorNotification(NotificationCenter.didProductLoaded,parseNetworkError(response.errorBody(),response.code()),classGuid);
                }
            }catch (Exception e){

                    postErrorNotification(NotificationCenter.didProductLoaded,ErrorUtils.createNetworkError(),classGuid);
                   if(PlusBuildVars.LOGS_ENABLED){
                       Log.d(TAG,e.toString());
                   }
            }
        });

    }

    private APIError parseNetworkError(ResponseBody responseBody,int code){
        APIError apiError = new APIError();
        try {
            if(responseBody != null) {
                apiError.setMessage(responseBody.string());
                apiError.setStatusCode(code);
            }
        }catch (Exception ignore){
            apiError.setMessage("unknown error!");
            apiError.setStatusCode(-1);
        }
        return apiError;
    }

    public void loadProducts(String bus_type, Map<String,Object> filters, int classGuid){
        shopDataControllerQueue.postRunnable(() -> {
            try {
                Response<ResponseBody> response;
                if(filters == null){
                    response =  shopRequest.loadProducts(bus_type).execute();
                }else{
                    response =  shopRequest.loadProducts(bus_type,filters).execute();
                }
                if(response.isSuccessful()){
                    Gson gson = new Gson();
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray array = jsonObject.getJSONArray("results");
                    String next = jsonObject.getString("next");
                    int count = jsonObject.getInt("count");
                    ArrayList<ShopDataSerializer.Product> products = new ArrayList<>();
                    for(int a = 0; a < array.length(); a++){
                        ShopDataSerializer.Product product =  gson.fromJson(array.getJSONObject(a).toString(), ShopDataSerializer.Product.class);
                        products.add(product);
                    }
                    AndroidUtilities.runOnUIThread(() -> NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didProductLoaded,true,products,classGuid,next,count));
                }else{
                    APIError apiError  = new APIError();
                    apiError.setMessage(response.errorBody().string());
                    apiError.setStatusCode(response.code());
                    postErrorNotification(NotificationCenter.didProductLoaded,apiError,classGuid);

                }
            }catch (Exception e){
                APIError apiError  = new APIError();
                apiError.setMessage(e.getMessage());
                apiError.setStatusCode(100);
                postErrorNotification(NotificationCenter.didProductLoaded,apiError,classGuid);
                if(PlusBuildVars.LOGS_ENABLED){
                    Log.d(TAG,e.toString());
                }

            }

        });
    }

    public int offerProductPrice(int channel_id,int product_id,SR_object.create_offer_req create_offer_req){
        int reqId = lastRequestToken.getAndIncrement();
        shopDataControllerQueue.postRunnable(() -> {
            try {
                Call<ResponseBody> call = shopRequest.offerProductPrice(ShopUtils.toBotChannelId(channel_id),product_id,create_offer_req);
                callSparseArray.put(reqId,call);
                Response<ResponseBody> response =  call.execute();
                callSparseArray.remove(reqId);
                if(response.isSuccessful()){
                    AndroidUtilities.runOnUIThread(() -> NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didOfferCreated,true,response.body()));
                }else{
                    APIError apiError = ErrorUtils.parseError(response);
                    AndroidUtilities.runOnUIThread(() -> AndroidUtilities.runOnUIThread(() ->  NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didOfferCreated,false,apiError)));
                }
            }catch (Exception e){
                callSparseArray.remove(reqId);
                if(PlusBuildVars.LOGS_ENABLED){
                    Log.d(TAG,"error message = " + e.toString());
                }
            }
        });
        return reqId;
    }

    public void listOffer(int channel_id,int class_guid){
        shopDataControllerQueue.postRunnable(() -> {
            try {
                Response<ResponseBody> response = getRequestManager().getShopInterface().listOffers(ShopUtils.toBotChannelId(channel_id)).execute();
                if(response.isSuccessful()){
                    if(response.body() != null){


                        ArrayList<ShopDataSerializer.ProductOffer> offerArrayList = new ArrayList<>();
                        Gson gson = new Gson();
                        JSONObject rawObject = new JSONObject(response.body().string());
                        int count = rawObject.getInt("count");
                        String next = rawObject.getString("next");
                        JSONArray dataArray = rawObject.getJSONArray("results");
                        for (int a = 0; a < dataArray.length(); a++){
                            JSONObject jsonObject = dataArray.getJSONObject(a);
                            ShopDataSerializer.ProductOffer review = gson.fromJson(jsonObject.toString(),ShopDataSerializer.ProductOffer.class);
                            offerArrayList.add(review);
                        }
                        AndroidUtilities.runOnUIThread(() -> NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didOffersLoaded,true,offerArrayList,class_guid,next));
                    }


                }else{
                    APIError apiError = ErrorUtils.parseError(response);
                    AndroidUtilities.runOnUIThread(() ->  NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didOffersLoaded,false,apiError,class_guid));
                }
            }catch (Exception e){
                Log.d(TAG,":"+ e.getMessage());
            }
        });

    }

    public int postReviewToShop(int channel_id,SR_object.post_review_request post_review_request,ResponseDelegate responseDelegates){
        int reqId = lastRequestToken.getAndIncrement();
        shopDataControllerQueue.postRunnable(() -> {
            try {
                Call<ResponseBody> call = shopRequest.postReviewToaShop(ShopUtils.toBotChannelId(channel_id),post_review_request);
                callSparseArray.put(reqId,call);
                Response<ResponseBody> response =  call.execute();
                callSparseArray.remove(reqId);
                if(response.isSuccessful()){
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            if(responseDelegates != null){
                                responseDelegates.run(response,null);
                            }
                        }
                    });
                  //  AndroidUtilities.runOnUIThread(() -> NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.reviewPosted,true,response.body()));
                }else{
                    APIError apiError = ErrorUtils.parseError(response);

                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            if(responseDelegates != null){
                                responseDelegates.run(null,apiError);
                            }
                        }
                    });
                   // AndroidUtilities.runOnUIThread(() -> AndroidUtilities.runOnUIThread(() ->  NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.reviewPosted,false,apiError)));
                }

            }catch (Exception e){
                callSparseArray.remove(reqId);
                Log.d(TAG,"error message = " + e.toString());
            }
        });
        return reqId;
    }

    private Gson gson;
    public Gson getGson(){
        if(gson == null){
            gson = new Gson();
        }
        return gson;
    }


    public void loadShopReviews(int chat_id,ShopDataSerializer.ProductType.Sort sort,String nextLink,int class_guid){
        shopDataControllerQueue.postRunnable(() -> {
            String resultBody = null;
            Response<ResponseBody> response = null;
            ArrayList<ShopDataSerializer.Review> reviews = new ArrayList<>();
            APIError apiError = null;
            boolean readFromDb = false;
            boolean cache = false;
            try {
                if(nextLink != null && URLUtil.isValidUrl(nextLink)){
                     response = shopRequest.request(nextLink).execute();
                }else{
                    cache = true;
                    response = shopRequest.loadShopReviews(ShopUtils.toBotChannelId(chat_id),sort != null?sort.sortby:"").execute();;
                }
                if(response.isSuccessful()){
                    resultBody = response.body().string();
                    if(cache){
                        getShopDataStorage().cacheReview(chat_id,resultBody);
                    }
                }else{
                    apiError  = ErrorUtils.createError(response.errorBody(),response.code());
                }
            }catch (Exception exception){
                apiError = ErrorUtils.createNetworkError();
                readFromDb = true;
            }

            if(readFromDb){
                ShopDataModels.Reviews review  =  getDataStorage().shopDao().getReview(chat_id);
                if(review != null){
                    resultBody = review.data;
                }
            }
            if(resultBody != null){
                try {
                    JSONObject rawObject = new JSONObject(resultBody);
                    int count = rawObject.getInt("count");
                    String next = rawObject.getString("next");
                    JSONArray dataArray = rawObject.getJSONArray("results");
                    for (int a = 0; a < dataArray.length(); a++){
                        JSONObject jsonObject = dataArray.getJSONObject(a);
                        ShopDataSerializer.Review review = gson.fromJson(jsonObject.toString(),ShopDataSerializer.Review.class);
                        reviews.add(review);
                    }
                    AndroidUtilities.runOnUIThread(() -> NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didReviewLoaded,true,reviews,next,count));

                }catch (Exception ignored){

                }
            }else{
                postErrorNotification(NotificationCenter.didReviewLoaded,apiError,class_guid);
            }

        });
    }



//    public void loadShopReview(boolean cache, int channel_id, ShopDataSerializer.ProductType.Sort  sort,int classGuid){
//        if(cache){
//            //shopDataStorage.loadShopBusiness();
//        }else {
//            shopDataControllerQueue.postRunnable(() -> {
//                try {
//                    Response<ResponseBody> response = shopRequest.loadShopReviews(ShopUtils.toBotChannelId(channel_id),sort != null?sort.sortby:"").execute();
//                    if(response.isSuccessful()){
//                        ArrayList<ShopDataSerializer.Review> reviews = new ArrayList<>();
//                        Gson gson = new Gson();
//                        JSONObject rawObject = new JSONObject(response.body().string());
//                        int count = rawObject.getInt("count");
//                        String next = rawObject.getString("next");
//                        JSONArray dataArray = rawObject.getJSONArray("results");
//                        for (int a = 0; a < dataArray.length(); a++){
//                            JSONObject jsonObject = dataArray.getJSONObject(a);
//                            ShopDataSerializer.Review review = gson.fromJson(jsonObject.toString(),ShopDataSerializer.Review.class);
//                            reviews.add(review);
//                        }
//                        AndroidUtilities.runOnUIThread(() -> NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didReviewLoaded,true,reviews,next,count));
//                    }else{
//                        APIError apiError = ErrorUtils.parseError(response);
//                        AndroidUtilities.runOnUIThread(() ->  NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didReviewLoaded,false,apiError,classGuid));
//                    }
//                }catch (Exception e){
//
//                    //any expection happend here is assumed to be network error
//                    postErrorNotification(NotificationCenter.didReviewLoaded,ErrorUtils.createNetworkError(),classGuid);
//                    if(PlusBuildVars.LOGS_ENABLED){
//                        Log.d(TAG,":"+ e.getMessage());
//
//                    }
//                }
//            });
//        }
//    }

    public void loadShopReviewSelf(int channel_id,int class_guid){
        shopDataControllerQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    Response<ResponseBody> response =  shopRequest.loadShopReviewSelf(ShopUtils.toBotChannelId(channel_id)).execute();
                    if(response.isSuccessful()){
                        if(response.body() != null){
                            ArrayList<ShopDataSerializer.Review> reviews = new ArrayList<>();
                            Gson gson = new Gson();
                            JSONObject rawObject = new JSONObject(response.body().string());
                            int count = rawObject.getInt("count");
                            String next = rawObject.getString("next");
                            JSONArray dataArray = rawObject.getJSONArray("results");
                            for (int a = 0; a < dataArray.length(); a++){
                                JSONObject jsonObject = dataArray.getJSONObject(a);
                                ShopDataSerializer.Review review = gson.fromJson(jsonObject.toString(),ShopDataSerializer.Review.class);
                                reviews.add(review);
                            }
                            AndroidUtilities.runOnUIThread(() -> NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didReviewSelfLoaded,true,reviews,next,count));
                        }
                    }else {
                        postErrorNotification(NotificationCenter.didReviewSelfLoaded,parseNetworkError(response.errorBody(),response.code()),class_guid);
                    }
                }catch (Exception e){

                    if(PlusBuildVars.LOGS_ENABLED){
                        Log.d(TAG,"loadShopReviewSelf:"+ e.getMessage());
                    }


                }
            }
        });

    }

    public void updateShopReview(int channel_id,int review_id){

        shopDataControllerQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    Response<ResponseBody> response =  shopRequest.updateShopReview(ShopUtils.toBotChannelId(channel_id),review_id).execute();
                    if(response.isSuccessful()){

                    }

                }catch (Exception e){
                    Log.d(TAG,"updateShopReview:"+ e.getMessage());

                }
            }
        });
    }

    public void deleteShopReview(int channel_id,int review_id){

        shopDataControllerQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    Response<ResponseBody> response =  shopRequest.deleteShopReview(ShopUtils.toBotChannelId(channel_id),review_id).execute();
                    if(response.isSuccessful()){

                    }

                }catch (Exception e){
                    Log.d(TAG,"deleteShopReview:"+ e.getMessage());

                }
            }
        });
    }

    public void loadFeaturedProduct(int classGuid){

        shopDataControllerQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    Response<ResponseBody> response =  shopRequest.loadFeaturedProducts().execute();
                    if(response.isSuccessful()){
                        Gson gson = new Gson();
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONArray array = jsonObject.getJSONArray("results");
                        String next = jsonObject.getString("next");
                        int count = jsonObject.getInt("count");
                        ArrayList<ShopDataSerializer.FeaturedProduct> shops = new ArrayList<>();
                        for(int a = 0; a < array.length(); a++){
                            ShopDataSerializer.FeaturedProduct shop =  gson.fromJson(array.getJSONObject(a).toString(), ShopDataSerializer.FeaturedProduct.class);
                            shops.add(shop);
                        }
                        AndroidUtilities.runOnUIThread(() -> NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didFeaturedProductLoaded,true,shops,classGuid, next,count));
                    }
                }catch (Exception e){
                    Log.d(TAG,"loadFeaturedProduct:"+ e.getMessage());

                }
            }
        });

    }

    public void loadFeaturedShop(int classGuid){
        shopDataControllerQueue.postRunnable(() -> {
            try {
                Response<ResponseBody> response =  shopRequest.loadFeaturedShops().execute();
                if(response.isSuccessful()){
                    Gson gson = new Gson();
                    JSONObject jsonObject = new JSONObject(response.body().string());

                    JSONArray array = jsonObject.getJSONArray("results");
                    String next = jsonObject.getString("next");
                    int count = jsonObject.getInt("count");
                    ArrayList<ShopDataSerializer.ShopSnip> shops = new ArrayList<>();
                    for(int a = 0; a < array.length(); a++){
                        ShopDataSerializer.ShopSnip shop =  gson.fromJson(array.getJSONObject(a).toString(), ShopDataSerializer.ShopSnip.class);
                        shops.add(shop);
                    }
                    AndroidUtilities.runOnUIThread(() -> NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didFeaturedShopLoaded,true,shops,classGuid, next,count));

                }

            }catch (Exception e){
                Log.d(TAG,"loadFeaturedShop:"+ e.getMessage());

            }
        });
    }

    public void loadLikedProduct(int class_guid){

        shopDataControllerQueue.postRunnable(() -> {
            try {
                Response<ResponseBody> response  = shopRequest.loadFavoriteProducts().execute();
                if(response.isSuccessful()){
                    Gson gson = new Gson();
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray array = jsonObject.getJSONArray("results");
                    String next = jsonObject.getString("next");
                    int count = jsonObject.getInt("count");
                    ArrayList<ShopDataSerializer.Product> products = new ArrayList<>();
                    for(int a = 0; a < array.length(); a++){
                        ShopDataSerializer.Product product =  gson.fromJson(array.getJSONObject(a).toString(), ShopDataSerializer.Product.class);
                        products.add(product);
                    }
                    Collections.sort(products, (o1, o2) -> Integer.compare(o1.id, o2.id));
                    AndroidUtilities.runOnUIThread(() -> NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didFavoriteProductLoaded,true,class_guid,products,next,count));
                }else{
                    APIError apiError  = new APIError();
                    apiError.setMessage(response.errorBody().string());
                    apiError.setStatusCode(response.code());
                    postErrorNotification(NotificationCenter.didFavoriteProductLoaded,apiError,class_guid);

                }
            } catch (Exception e) {
                postErrorNotification(NotificationCenter.didFavoriteProductLoaded,ErrorUtils.createNetworkError(),class_guid);

                if(PlusBuildVars.LOGS_ENABLED){
                    Log.d(TAG,"loadLikedProduct:"+ e.getMessage());

                }

            }
        });
    }

    public void likeProduct(ShopDataModels.ProductLike likeProduct,boolean add){

        shopDataControllerQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    Response<ResponseBody> response;
                    if(add){
                        response  = shopRequest.addProductToFav(likeProduct.chat_id,likeProduct.product_id).execute();

                    }else{
                        response  = shopRequest.deleteProductFromFav(likeProduct.chat_id,likeProduct.product_id).execute();

                    }
                    if(response.isSuccessful()){
                        getDataStorage().getStorageQueue().postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                likeProduct.synced = true;
                                getDataStorage().shopDao().update(likeProduct);
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public void syncFavProducts(List<ShopDataModels.ProductLike> faveList){
        shopDataControllerQueue.postRunnable(() -> {
            try {
                for(int a  = 0; a < faveList.size(); a++){
                    ShopDataModels.ProductLike likeProduct =  faveList.get(a);
                    Response<ResponseBody> response  = shopRequest.addProductToFav(ShopUtils.toBotChannelId(likeProduct.chat_id),likeProduct.product_id).execute();
                    if(response.isSuccessful()){
                        if(likeProduct != null){
                            likeProduct.synced = true;
                            AndroidUtilities.runOnUIThread(new Runnable() {
                                @Override
                                public void run() {
                                    getShopDataStorage().processFavorite(0,0,false,likeProduct,ShopDataStorage.FAV_UPDATE,null);
                                }
                            });
                        }
                    }
                }
            }catch (Exception e){
                if(PlusBuildVars.LOGS_ENABLED){
                    Log.d(TAG,e.getMessage());
                }
            }
        });
    }

    public void checkFav(boolean fav,long  chat_id,int product_id,BooleanCallBack booleanCallBack){
        shopDataControllerQueue.postRunnable(() -> {
            try {
                Response<ResponseBody> response;
                if(fav){
                    response  = shopRequest.addProductToFav(chat_id,product_id).execute();
                }else{
                    response  = shopRequest.deleteProductFromFav(chat_id,product_id).execute();
                }
                if(response.isSuccessful()){
                    AndroidUtilities.runOnUIThread(() -> {
                        if(booleanCallBack != null){
                            booleanCallBack.onResponse(true);
                        }
                    });
                }
            }catch (Exception ignore){

            }
        });
    }


    public void loadRemoteData(String link,String type,int classGuid){

        shopDataControllerQueue.postRunnable(() -> {
            try {
                Response<ResponseBody> response  = shopRequest.loadRemoteData(link).execute();
                if(response.isSuccessful()){
                    String data = response.body().string();
                    int hash = link.hashCode();
                    if(hash != 0){
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                ShopDataModels.LinkCache linkCache  = new ShopDataModels.LinkCache();
                                linkCache.cache_time = System.currentTimeMillis();
                                linkCache.data = data;
                                linkCache.hash = hash;
                                getShopDataStorage().cacheLink(linkCache);
                            }
                        });
                    }
                    parseRemoteData(data,type,classGuid);
                }

            } catch (Exception e) {
                AndroidUtilities.runOnUIThread(() -> {
                    int hash = link.hashCode();
                    getShopDataStorage().loadCache(hash, new ShopDataStorage.DataLoadInterface() {
                        @Override
                        public void data(String data) {
                            shopDataControllerQueue.postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        parseRemoteData(data,type,classGuid);
                                    } catch (Exception exception) {
                                        exception.printStackTrace();
                                    }
                                }
                            });
                        }
                    });
                });
                if(PlusBuildVars.LOGS_ENABLED){
                    Log.d(TAG,e.toString());

                }
            }

        });
    }
    
    public void parseRemoteData(String data,String type,int classGuid) throws Exception{
        Gson gson = new Gson();
        JSONObject jsonObject = new JSONObject(data);
        JSONArray array = jsonObject.getJSONArray("results");
        String next = jsonObject.getString("next");
        int count = jsonObject.getInt("count");
        switch (type) {
            case StoreActivity.UI_BANNER:
                ArrayList<ShopDataSerializer.Collection> collections = new ArrayList<>();
                for (int a = 0; a < array.length(); a++) {
                    ShopDataSerializer.Collection collection = gson.fromJson(array.getJSONObject(a).toString(), ShopDataSerializer.Collection.class);
                    collections.add(collection);
                }
                AndroidUtilities.runOnUIThread(() -> NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didRemoteDataLoaded, true, type, collections, classGuid, next, count));
                break;
            case StoreActivity.UI_SHOP_HORIZONTAL:
                ArrayList<ShopDataSerializer.ShopSnip> shops = new ArrayList<>();
                for (int a = 0; a < array.length(); a++) {
                    ShopDataSerializer.ShopSnip shop = gson.fromJson(array.getJSONObject(a).toString(), ShopDataSerializer.ShopSnip.class);
                    shops.add(shop);
                }
                AndroidUtilities.runOnUIThread(() -> NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didRemoteDataLoaded, true, type, shops, classGuid, next, count));

                break;
            case StoreActivity.UI_PRODUCT_VERTICAL:
            case StoreActivity.UI_PRODUCT_HORIZONTAL:
                ArrayList<ShopDataSerializer.FeaturedProduct> featuredProducts = new ArrayList<>();
                for (int a = 0; a < array.length(); a++) {
                    ShopDataSerializer.FeaturedProduct shop = gson.fromJson(array.getJSONObject(a).toString(), ShopDataSerializer.FeaturedProduct.class);
                    featuredProducts.add(shop);
                }
                AndroidUtilities.runOnUIThread(() -> NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didRemoteDataLoaded, true, type, featuredProducts, classGuid, next, count));
                break;
            case UI_SHOP_INSTA_VIEW:
                ArrayList<ShopDataSerializer.InstaShop> instaShops = new ArrayList<>();
                for (int a = 0; a < array.length(); a++) {
                    ShopDataSerializer.InstaShop shop = gson.fromJson(array.getJSONObject(a).toString(), ShopDataSerializer.InstaShop.class);
                    instaShops.add(shop);
                }
                AndroidUtilities.runOnUIThread(() -> NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didRemoteDataLoaded, true, type, instaShops, classGuid, next, count));
                break;
        }
        
    }

    public int searchProductForBusiness(String business, String search,ShopDataSerializer.ProductType.Sort sort,Map<String,Object> filterTags, ProductSearchCallBack responseDelegate){
        int reqId = lastRequestToken.getAndIncrement();
        shopDataControllerQueue.postRunnable(() -> {
            try {
                Call<ResponseBody> call = shopRequest.searchProductForBusiness(business,search,ShopUtils.formatSort(sort));
                callSparseArray.put(reqId,call);
                Response<ResponseBody> response =  call.execute();
                callSparseArray.remove(reqId);
                if(response.isSuccessful() && response.body() != null){
                    Gson gson = new Gson();
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray array = jsonObject.getJSONArray("results");
                    String next = jsonObject.getString("next");
                    int count = jsonObject.getInt("count");

                    ArrayList<ShopDataSerializer.Product> products = new ArrayList<>();
                    for(int a = 0; a < array.length(); a++){
                        ShopDataSerializer.Product product =  gson.fromJson(array.getJSONObject(a).toString(), ShopDataSerializer.Product.class);
                        products.add(product);
                    }
                    AndroidUtilities.runOnUIThread(() -> {
                        if(responseDelegate != null){
                            responseDelegate.run(products,null,next,count);
                        }
                    });

                  //  AndroidUtilities.runOnUIThread(() -> NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didProductLoaded,true,products,classGuid,next,count));
                }
            }catch (Exception e){
                Log.i(TAG,e.toString());
            }
        });

        return  reqId;
    }

    public void loadProductCollection(int coll_id,int classGuid){
        shopDataControllerQueue.postRunnable(() -> {
            try {
                Response<ResponseBody> response =  shopRequest.loadProductInCollection(coll_id).execute();
                if(response.isSuccessful()){
                    Gson gson = new Gson();
                    JSONObject jsonObject = new JSONObject(response.body().string());

                    JSONArray array = jsonObject.getJSONArray("results");
                    String next = jsonObject.getString("next");
                    int count = jsonObject.getInt("count");
                    ArrayList<ShopDataSerializer.Product> shops = new ArrayList<>();
                    for(int a = 0; a < array.length(); a++){
                        ShopDataSerializer.Product shop =  gson.fromJson(array.getJSONObject(a).toString(), ShopDataSerializer.Product.class);
                        shops.add(shop);
                    }
                    AndroidUtilities.runOnUIThread(() -> NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.collectionLoaded,true,shops,classGuid, next,count));

                }

            }catch (Exception e){
                postErrorNotification(NotificationCenter.collectionLoaded,new APIError(),classGuid);
                if(PlusBuildVars.LOGS_ENABLED){
                    Log.d(TAG,"loadFeaturedShop:"+ e.getMessage());
                }

            }
        });
    }

    public int searchProduct(String search,String busType,String nextLink,ShopDataSerializer.ProductType.Sort sort,Map<String,Object> filterTags, ProductSearchCallBack responseDelegate){
        int reqId = lastRequestToken.getAndIncrement();
        shopDataControllerQueue.postRunnable(() -> {
            try {
                Call<ResponseBody> call;
                if(busType == null || busType.length() > 0){
                    call = shopRequest.searchProduct(search,busType);
                }else{
                    call = shopRequest.searchProduct(search);
                }
                callSparseArray.put(reqId,call);
                Response<ResponseBody> response =  call.execute();
                callSparseArray.remove(reqId);
                if(response.isSuccessful()){
                    Gson gson = new Gson();
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray array = jsonObject.getJSONArray("results");
                    String next = jsonObject.getString("next");
                    int count = jsonObject.getInt("count");

                    ArrayList<ShopDataSerializer.Product> products = new ArrayList<>();
                    for(int a = 0; a < array.length(); a++){
                        ShopDataSerializer.Product product =  gson.fromJson(array.getJSONObject(a).toString(), ShopDataSerializer.Product.class);
                        products.add(product);
                    }
                    AndroidUtilities.runOnUIThread(() -> {
                        if(responseDelegate != null){
                            responseDelegate.run(products,null,next,count);
                        }
                    });

                }
            }catch (Exception e){
                AndroidUtilities.runOnUIThread(() -> {
                    if(responseDelegate != null){
                        APIError apiError1 = new APIError();
                        apiError1.setMessage("error message!");
                        responseDelegate.run(null,apiError1,null,0);
                    }
                });
                Log.i(TAG,e.toString());
            }
        });

        return  reqId;
    }

    public int searchShop(String search,String nextLink, ProductSearchCallBack responseDelegate){
        int reqId = lastRequestToken.getAndIncrement();
        shopDataControllerQueue.postRunnable(() -> {
            try {
                Call<ResponseBody> call;
                if(nextLink != null &&  nextLink.length() > 0){
                    call = shopRequest.request(nextLink);
                }else{
                    call = shopRequest.searchShop(search);
                }
                callSparseArray.put(reqId,call);
                Response<ResponseBody> response =  call.execute();
                callSparseArray.remove(reqId);
                if(response.isSuccessful() && response.body() != null){
                    Gson gson = new Gson();
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray array = jsonObject.getJSONArray("results");
                    String next = jsonObject.getString("next");
                    int count = jsonObject.getInt("count");

                    ArrayList<ShopDataSerializer.ShopSnip> products = new ArrayList<>();
                    for(int a = 0; a < array.length(); a++){
                        ShopDataSerializer.ShopSnip snip =  gson.fromJson(array.getJSONObject(a).toString(), ShopDataSerializer.ShopSnip.class);
                        products.add(snip);
                    }
                    AndroidUtilities.runOnUIThread(() -> {
                        if(responseDelegate != null){
                            responseDelegate.run(products,null,next,count);
                        }
                    });
                }
            }catch (Exception e){
                Log.i(TAG,e.toString());
            }
        });

        return  reqId;
    }

    public void addAdmin(int chat_id,int telegram_id, ResponseDelegate responseDelegate){
       shopDataControllerQueue.postRunnable(new Runnable() {
           @Override
           public void run() {
               try {
                   Map<String,String> data = new HashMap<>();
                   data.put("telegramID",String.valueOf(telegram_id));
                   Response<ResponseBody> response  = shopRequest.addAdmin(ShopUtils.toBotChannelId(chat_id),data).execute();
                   Gson gson = new Gson();
                   if(response.isSuccessful()){
                       ShopDataSerializer.User user = gson.fromJson(response.body().string(), ShopDataSerializer.User.class);
                       responseDelegate.run(user,null);
                   }
               } catch (Exception e) {
                   if(PlusBuildVars.LOGS_ENABLED){
                       Log.i(TAG,"addAdmin:" + e.getMessage());
                   }
               }
           }
       });
    }

    public void getShopAdmins(int chat_id,ResponseDelegate delegate){
        shopDataControllerQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    Response<ResponseBody> response  = shopRequest.getShopAdmins(ShopUtils.toBotChannelId(chat_id)).execute();
                    if(delegate != null){
                        if(response.isSuccessful()){
                            Gson gson = new Gson();
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            JSONArray array = jsonObject.getJSONArray("results");
                            String next = jsonObject.getString("next");
                            int count = jsonObject.getInt("count");
                            ArrayList<ShopDataSerializer.User> users= new ArrayList<>();
                            for(int a = 0; a < array.length(); a++){
                                ShopDataSerializer.User snip =  gson.fromJson(array.getJSONObject(a).toString(), ShopDataSerializer.User.class);
                                users.add(snip);
                            }
                            delegate.run(users,null);
                        }else{
                            delegate.run(null,parseNetworkError(response.errorBody(),response.code()));
                        }
                    }
                } catch (Exception e) {
                    if(PlusBuildVars.LOGS_ENABLED){
                        Log.i(TAG,"getShopAdmins:" + e.getMessage());
                    }
                }
            }
        });
    }

    public void removeAdminFromShop(int chat_id,int user_id,ResponseDelegate delegate){
        shopDataControllerQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    Response<ResponseBody> response  = shopRequest.removeAdminFromShop(ShopUtils.toBotChannelId(chat_id),user_id).execute();
                    if(response.isSuccessful()){
                       if(delegate != null){
                           delegate.run(response,null);
                       }
                    }
                } catch (Exception e) {
                    if(PlusBuildVars.LOGS_ENABLED){
                        Log.i(TAG,"removeAdminFromShop:" + e.getMessage());
                    }
                }
            }
        });
    }

    public void deleteProductPicture(int chat_id,int product_id,int photo_id,RequestDelegate delegate){
        shopDataControllerQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    Response<ResponseBody> response  = shopRequest.deleteProductPicture(ShopUtils.toBotChannelId(chat_id),product_id,photo_id).execute();
                    if(response.isSuccessful()){
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                            }
                        });
                    }
                } catch (Exception e) {
                    if(PlusBuildVars.LOGS_ENABLED){
                        Log.i(TAG,"deleteProductPicture:" + e.getMessage());
                    }
                }
            }
        });
    }

    public void updateProductPicture(int chat_id,int product_id,int photo_id,Map<String,Object> body,RequestDelegate delegate){
        shopDataControllerQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    Response<ResponseBody> response  = shopRequest.updateProductPicture(ShopUtils.toBotChannelId(chat_id),product_id,photo_id,body).execute();
                    if(response.isSuccessful()){
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                            }
                        });
                    }
                } catch (Exception e) {
                    if(PlusBuildVars.LOGS_ENABLED){
                        Log.i(TAG,"updateProductPicture:" + e.getMessage());
                    }
                }
            }
        });
    }

    public void deleteProductPicture(int chat_id,int product_id,RequestDelegate delegate){
        shopDataControllerQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    Response<ResponseBody> response  = shopRequest.deleteProductPicture(ShopUtils.toBotChannelId(chat_id),product_id).execute();
                    if(response.isSuccessful()){
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                            }
                        });
                    }
                } catch (Exception e) {
                    if(PlusBuildVars.LOGS_ENABLED){
                        Log.i(TAG,"deleteProductPicture:" + e.getMessage());
                    }
                }
            }
        });
    }

    public void deletePhoneNumber(int chat_id,int phone_id,RequestDelegate delegate){
        shopDataControllerQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    Response<ResponseBody> response  = shopRequest.deletePhoneNumber(ShopUtils.toBotChannelId(chat_id),phone_id).execute();
                    if(response.isSuccessful()){
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                            }
                        });
                    }
                } catch (Exception e) {
                    if(PlusBuildVars.LOGS_ENABLED){
                        Log.i(TAG,"deletePhoneNumber:" + e.getMessage());
                    }
                }
            }
        });
    }

    public void updatePhoneNumber(int chat_id,int phone_id,RequestDelegate delegate){
        shopDataControllerQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    Response<ResponseBody> response  = shopRequest.updatePhoneNumber(ShopUtils.toBotChannelId(chat_id),phone_id).execute();
                    if(response.isSuccessful()){
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                            }
                        });
                    }
                } catch (Exception e) {
                    if(PlusBuildVars.LOGS_ENABLED){
                        Log.i(TAG,"updatePhoneNumber:" + e.getMessage());
                    }
                }
            }
        });
    }

    public void addPhoneNumber(int chat_id,Map<String,Object> phoneNumber,RequestDelegate delegate){
        shopDataControllerQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    Response<ResponseBody> response  = shopRequest.addPhoneNumber(ShopUtils.toBotChannelId(chat_id),phoneNumber).execute();
                    if(response.isSuccessful()){
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                            }
                        });
                    }
                } catch (Exception e) {
                    if(PlusBuildVars.LOGS_ENABLED){
                        Log.i(TAG,"addPhoneNumber:" + e.getMessage());
                    }
                }
            }
        });
    }


    public void getShopSelf(int chat_id,RequestDelegate delegate){
        shopDataControllerQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    Response<ResponseBody> response  = shopRequest.getShopSelf(ShopUtils.toBotChannelId(chat_id)).execute();
                    if(response.isSuccessful()){
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                            }
                        });
                    }
                } catch (Exception e) {
                    if(PlusBuildVars.LOGS_ENABLED){
                        Log.i(TAG,"getShopSelf:" + e.getMessage());
                    }
                }
            }
        });
    }

    public void getUserSelf(ResponseDelegate delegate){
        shopDataControllerQueue.postRunnable(() -> {
            try {
                Response<ResponseBody> response  = shopRequest.getUserSelf().execute();
                if(response.isSuccessful()){
                    if(delegate != null){
                        Gson gson = new Gson();
                        ShopDataSerializer.User user =   gson.fromJson(response.body().string(), ShopDataSerializer.User.class);
                        if(user != null){
                            delegate.run(user,null);
                        }
                    }
                }
            } catch (Exception e) {
                if(PlusBuildVars.LOGS_ENABLED){
                    Log.i(TAG,"getUserSelf:" + e.getMessage());
                }
            }
        });
    }


    public void checkForUserChange(TLRPC.User user){

        Log.i("uservalue tg",user.first_name + "first name");
        Log.i("uservalue tg ",user.phone + " phone");
        Log.i("uservalue tg",user.last_name + " = last name");
        Log.i("uservalue t",user.username + " = usename");
        Log.i("uservalue tg",user.id + " id");

        Log.i(TAG,"checkForUserChange:" + "checkForUserChange called");
        getUserSelf((response, error) -> {
            if(response != null){
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        ShopDataSerializer.User huluuser = (ShopDataSerializer.User)response;
                        Log.i("uservalue hg",huluuser.toString());


                        if(huluuser == null  || user == null){
                            return;
                        }
                        boolean change = false;
                        if(!huluuser.first_name.equals(user.first_name)){
                            huluuser.first_name = user.first_name;
                            change = true;
                        }

                        if(ShopUtils.isEmpty(huluuser.phoneNumber) || (!huluuser.phoneNumber.equals(user.phone))){
                            huluuser.phoneNumber = user.phone;
                            change = true;
                        }

                        if(ShopUtils.isEmpty(huluuser.last_name) || (!huluuser.last_name.equals(user.last_name))){
                            huluuser.last_name = user.last_name;
                            change = true;
                        }

                        if(ShopUtils.isEmpty(huluuser.username) || (!huluuser.username.equals(user.username))){
                            huluuser.username = user.username;
                            change = true;
                        }
                        huluuser.telegramId = user.id;

                        if(change){
                            Log.i(TAG,"checkForUserChange:" + "update dected and update called");
                            updateUserSelf(huluuser);
                        }

                    }
                });

            }
        });
    }

    public void updateUserSelf(ShopDataSerializer.User user){
        shopDataControllerQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    Response<ResponseBody> response  = shopRequest.updateUserSelf(user).execute();
                } catch (Exception e) {
                    if(PlusBuildVars.LOGS_ENABLED){
                        Log.i(TAG,"updateUserSelf:" + e.getMessage());
                    }
                }
            }
        });
    }

    public void getShopPhoneNumber(int chat_id,RequestDelegate delegate){
        shopDataControllerQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    Response<ResponseBody> response  = shopRequest.getShopPhoneNumber(ShopUtils.toBotChannelId(chat_id)).execute();
                    if(response.isSuccessful()){
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                            }
                        });
                    }
                } catch (Exception e) {
                    if(PlusBuildVars.LOGS_ENABLED){
                        Log.i(TAG,"getShopPhoneNumber:" + e.getMessage());
                    }
                }
            }
        });
    }

    public int searchPlaces(String query, Location location,boolean loc,boolean rad,ResponseDelegate responseDelegate){
        int reqId = lastRequestToken.getAndIncrement();
        shopDataControllerQueue.postRunnable(() -> {
            try {
                Map<String,Object> queryMap = new HashMap<>();
                queryMap.put("inputtype","textquery");
                queryMap.put("input",query);
                queryMap.put("key",PlusBuildVars.GOOGLE_MAP_API);
                queryMap.put("fields","formatted_address,name,geometry,icon");
                queryMap.put("language","am");
                if(location != null && loc){
                   queryMap.put("location",location.getLatitude() + "," + location.getLongitude());
                }
                if(true){
                    queryMap.put("radius","10000");
                }
                Call<ResponseBody> call = getRequestManager().getRideRequest().findPlaces(queryMap);
                callSparseArray.put(reqId,call);
                Response<ResponseBody> response =  call.execute();
                callSparseArray.remove(reqId);
                if(response.isSuccessful()){
                    if(responseDelegate != null){
                        responseDelegate.run(parseLoc(response.body().string()),null);
                    }
                }
            } catch (Exception e) {
                Log.i(TAG,"getShopPhoneNumber:" + e.getMessage());
                callSparseArray.remove(reqId);
            }

        });

        return reqId;
    }

//    public ArrayList<RideObjects.SearchLocation> parseLoc(String data){
//        ArrayList<RideObjects.SearchLocation> searchLocations = new ArrayList<>();
//        try {
//            JSONObject jsonObject = new JSONObject(data);
//            String status =   jsonObject.getString("status");
//            if(status.equals("OK")){
//                JSONArray jsonArray = jsonObject.getJSONArray("candidates");
//                for (int i = 0; i < jsonArray.length(); i++) {
//                    RideObjects.SearchLocation searchLocation  = new RideObjects.SearchLocation();
//                    JSONObject valObj = jsonArray.getJSONObject(i);
//                    searchLocation.address = valObj.getString("formatted_address");
//                    searchLocation._lat = valObj.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
//                    searchLocation._long = valObj.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
//                    searchLocation.icon = valObj.getString("icon");
//                    searchLocation.title = valObj.getString("name");
//                    searchLocations.add(searchLocation);
//                }
//            }
//
//        }catch (Exception ignore){
//
//        }
//        return searchLocations;
//    }


    public ArrayList<RideObjects.SearchLocation> parseLoc(String data){
        ArrayList<RideObjects.SearchLocation> searchLocations = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(data);
            String status =   jsonObject.getString("status");
           // String next_page_token = jsonObject.getString("next_page_token");
            if(status.equals("OK")){
                JSONArray jsonArray = jsonObject.getJSONArray("results");
                for (int i = 0; i < jsonArray.length(); i++) {
                    RideObjects.SearchLocation searchLocation  = new RideObjects.SearchLocation();
                    JSONObject valObj = jsonArray.getJSONObject(i);
                    searchLocation.address = valObj.getString("formatted_address");
                    searchLocation._lat = valObj.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                    searchLocation._long = valObj.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                    searchLocation.icon = valObj.getString("icon");
                    searchLocation.title = valObj.getString("name");
                    searchLocations.add(searchLocation);
                }
            }

        }catch (Exception ignore){

        }
        return searchLocations;
    }

    public static final String UPDATE_PROXY = "UPDATE_PROXY";
    public static final String UPDATE_SHOP_CONFIGURATIONS= "UPDATE_SHOP_CONFIGURATIONS";


    private void update(String type){
        if(type.equals(UPDATE_PROXY)){
          //  loadProxyList(false,false);
        }else if(type.equals(UPDATE_SHOP_CONFIGURATIONS)){
            productTypes.clear();
            configHashMap.clear();
            updateProductConfiguration(new ConfigDelegate() {
                @Override
                public void didFinishUpdating(boolean success) {

                }
            });
        }
    }


    public void translate(String text,ResponseDelegate responseDelegate){
        shopDataControllerQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                try{
                    String TRANSLATE_URL = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl={**}&dt=t&q=";
                    String finalUrl = TRANSLATE_URL.replace("{**}", PlusConfig.default_translate_lang) + Uri.encode(text,"utf-8");
                    Response<ResponseBody> res =  getRequestManager().getShopInterface().translate(finalUrl).execute();
                    if(res.isSuccessful()){
                        StringBuilder builder1=new StringBuilder();
                        JSONArray jsonArray = new JSONArray(res.body().string());
                        JSONArray jsonArray2 = (JSONArray) jsonArray.get(0);
                        int length=jsonArray2.length();
                        for(int i=0;i<length;i++){
                            builder1.append(jsonArray2.getJSONArray(i).get(0).toString());
                        }
                        if(responseDelegate != null){
                            responseDelegate.run(builder1.toString(),null);
                        }
                    }else{
                        if(responseDelegate != null){
                            responseDelegate.run(null,ErrorUtils.createError(res.errorBody(),res.code()));
                        }
                    }
                }catch (Exception e){
                    if(responseDelegate != null){
                        responseDelegate.run(null,ErrorUtils.createNetworkError());
                    }
                }
            }
        });
    }


    private boolean loadingProxies;




    public void loadProxyFromFirebase(boolean notify,boolean connect){
        if(loadingProxies){
            return;
        }
        loadingProxies = true;
        shopDataControllerQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = "https://huluchat-89422.firebaseio.com/proxy.json";
                    Response<ResponseBody> res =  getRequestManager().getShopInterface().getProxiesFromFirebase(url).execute();
                    loadingProxies = false;
                    String data = res.body().string();
                    JSONArray jsonArray = new JSONArray(data);
                    ArrayList<String> proxies_list = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        proxies_list.add(jsonArray.getString(i));
                    }
                    if(!proxies_list.isEmpty()){
                        SharedConfig.clearProxy();
                    }
                    ArrayList<SharedConfig.ProxyInfo> proxyInfoArrayList = new ArrayList<>();
                    for (int i = 0; i < proxies_list.size(); i++) {
                        SharedConfig.ProxyInfo proxy = PlusUtils.parseProxy(proxies_list.get(i));
                        if(proxy == null){
                            continue;
                        }
                        proxyInfoArrayList.add(proxy);
                        SharedConfig.addProxy(proxy);
                    }
                    if(!proxyInfoArrayList.isEmpty() && connect){
                        SharedConfig.ProxyInfo proxy = proxyInfoArrayList.get(0);
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                SharedPreferences preferences = MessagesController.getGlobalMainSettings();
                                SharedPreferences.Editor editor = MessagesController.getGlobalMainSettings().edit();
                                editor.putString("proxy_ip", proxy.address);
                                editor.putString("proxy_pass", proxy.password);
                                editor.putString("proxy_user", proxy.username);
                                editor.putInt("proxy_port",proxy.port);
                                editor.putString("proxy_secret", proxy.secret);
                                editor.commit();
                                MessagesController.getGlobalMainSettings().edit().putBoolean("proxy_enabled",true).commit();
                                ConnectionsManager.setProxySettings(true, proxy.address, proxy.port, proxy.username, proxy.password, proxy.secret);
                            }
                        });
                    }
                    if(notify){
                        AndroidUtilities.runOnUIThread(() -> getNotificationCenter().postNotificationName(NotificationCenter.proxyLoadedFromServer,proxyInfoArrayList));
                    }

                }catch (Exception e){
                    loadingProxies = false;
                    FileLog.debug(e.getMessage());
                }
            }
        });
    }

    public void loadProxyList(boolean notify,boolean connect){
        if(loadingProxies){
            return;
        }
        loadingProxies = true;
        shopDataControllerQueue.postRunnable(() -> {
            try {
                Response<ResponseBody> res =  getRequestManager().getShopInterface().getProxies().execute();
                loadingProxies = false;
                if(res.isSuccessful()){
                    String data = res.body().string();
                    JSONObject jsonObject = new JSONObject(data);
                    ArrayList<String> proxies_list = new ArrayList<>();
                    JSONArray jsonArray = jsonObject.getJSONArray("results");
                    for (int i = 0; i < jsonArray.length() ; i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        proxies_list.add(obj.getString("address"));
                    }
                    if(!proxies_list.isEmpty()){
                        SharedConfig.clearProxy();
                    }
                    ArrayList<SharedConfig.ProxyInfo> proxyInfoArrayList = new ArrayList<>();
                    for (int i = 0; i < proxies_list.size(); i++) {
                        SharedConfig.ProxyInfo proxy = PlusUtils.parseProxy(proxies_list.get(i));
                        if(proxy == null){
                            continue;
                        }
                        proxyInfoArrayList.add(proxy);
                        SharedConfig.addProxy(proxy);
                    }
                    if(!proxyInfoArrayList.isEmpty() && connect){
                        SharedConfig.ProxyInfo proxy = proxyInfoArrayList.get(0);
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                MessagesController.getGlobalMainSettings().edit().putBoolean("proxy_enabled",true).commit();
                                ConnectionsManager.setProxySettings(true, proxy.address, proxy.port, proxy.username, proxy.password, proxy.secret);
                            }
                        });
                    }
                    if(notify){
                        AndroidUtilities.runOnUIThread(() -> getNotificationCenter().postNotificationName(NotificationCenter.proxyLoadedFromServer,proxyInfoArrayList));
                    }

                }
            }catch (Exception e){
                loadingProxies = false;
                FileLog.debug(e.getMessage());
            }

        });
    }

//    public void checkForUpdate(){
//        FirebaseTask task = new FirebaseTask(currentAccount);
//        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null, null, null);
//        currentTask = task;
//    }

    private static AsyncTask currentTask;
//    private static class FirebaseTask extends AsyncTask<Void, Void, Void>{
//
//        private int currentAccount;
//        private FirebaseRemoteConfig firebaseRemoteConfig;
//
//        public FirebaseTask(int instance) {
//            super();
//            currentAccount = instance;
//        }
//
//        protected Void doInBackground(Void... voids) {
//            try {
//                firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
//                FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder().setDeveloperModeEnabled(BuildConfig.DEBUG).build();
//                firebaseRemoteConfig.setConfigSettings(configSettings);
//                String currentValue = firebaseRemoteConfig.getString("ipconfigv3");
//                String currentfilm = "";
//                if (BuildVars.LOGS_ENABLED) {
//                    FileLog.d("current firebase value = " + currentValue);
//                }
//                firebaseRemoteConfig.fetch(0).addOnCompleteListener(finishedTask -> {
//                    final boolean success = finishedTask.isSuccessful();
//                    Utilities.stageQueue.postRunnable(() -> {
//                        currentTask = null;
//                        String config = null;
//                        if (success) {
//                            firebaseRemoteConfig.activateFetched();
//                            config = firebaseRemoteConfig.getString("ipconfigv3");
//                        }
//                        if (!TextUtils.isEmpty(config)) {
//                            byte[] bytes = Base64.decode(config, Base64.DEFAULT);
//                            try {
//                                NativeByteBuffer buffer = new NativeByteBuffer(bytes.length);
//                                buffer.writeBytes(bytes);
//                                int date = (int) (firebaseRemoteConfig.getInfo().getFetchTimeMillis() / 1000);
//                            } catch (Exception e) {
//                                FileLog.e(e);
//                            }
//                        } else {
//                            if (PlusBuildVars.LOGS_ENABLED) {
//                                FileLog.d("failed to get firebase result");
//                                FileLog.d("start dns txt task");
//                            }
//                        }
//                    });
//                });
//            } catch (Throwable e) {
//                Utilities.stageQueue.postRunnable(() -> {
//                    if (PlusBuildVars.LOGS_ENABLED) {
//                        FileLog.debug("failed to get firebase result");
//                        FileLog.debug("start dns txt task");
//                    }
//
//                });
//                FileLog.e(e);
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void result) {
//
//        }
//    }



}
