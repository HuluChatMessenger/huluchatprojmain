package org.plus.wallet;

import android.text.TextUtils;
import android.util.SparseArray;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;
import org.plus.apps.business.ShopUtils;
import org.plus.apps.wallet.WalletDataSerializer;
import org.plus.net.APIError;
import org.plus.net.ErrorUtils;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BaseController;
import org.telegram.messenger.DispatchQueue;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.StickersActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class WalletController extends BaseController {

    public static final int HELLO_CASH = 1;
    public static final int MBIRR = 2;
    public static final int AMOLE = 3;


    public interface ResponseCallback{
        void onResponse(Object response, APIError apiError);
    }

    private ArrayList<WalletModel.Provider> paymentProviders = new ArrayList<>();
    private HashMap<String, WalletModel.Provider> providerHashMap = new HashMap<>();

    private WalletRequest walletRequest;
    private AtomicInteger lastRequestToken = new AtomicInteger(1);

    private volatile SparseArray<Call> callSparseArray = new SparseArray<>();

    private DispatchQueue walletQueue = new DispatchQueue("walletQueue");
    private static volatile WalletController[] Instance = new WalletController[UserConfig.MAX_ACCOUNT_COUNT];

    public static WalletController getInstance(int num) {
        WalletController localInstance = Instance[num];
        if (localInstance == null) {
            synchronized (WalletController.class) {
                localInstance = Instance[num];
                if (localInstance == null) {
                    Instance[num] = localInstance = new WalletController(num);
                }
            }
        }
        return localInstance;
    }

    public WalletController(int num) {
        super(num);
        walletQueue.postRunnable(() -> walletRequest = getRequestManager().getWalletRequest());

        WalletModel.Provider provider = new WalletModel.Provider();
        provider.id = AMOLE;
        provider.key = "amole";
        provider.name = "Amole";
        provider.requireOtp = true;
        provider.res = R.drawable.amole;
        paymentProviders.add(provider);
        providerHashMap.put(provider.key,provider);


        provider = new WalletModel.Provider();
        provider.id = HELLO_CASH;
        provider.key = "hello_cash";
        provider.name = "Hello Cash";
        provider.requireOtp = false;
        provider.res = R.drawable.hellocash;
        paymentProviders.add(provider);
        providerHashMap.put(provider.key,provider);


        provider = new WalletModel.Provider();
        provider.id = MBIRR;
        provider.key = "mbirr";
        provider.name = "MBirr";
        provider.requireOtp = false;
        provider.res = R.drawable.mbirr;
        paymentProviders.add(provider);
        providerHashMap.put(provider.key,provider);

    }

    public void cancelRequest(int reqId){
        Call call =  callSparseArray.get(reqId);
        if(call != null && !call.isCanceled()){
            call.cancel();
        }
    }

    public ArrayList<WalletModel.Provider> getPaymentProviders() {
        return paymentProviders;
    }

    public void sendOtp(WalletModel.Provider provider, String phone, ResponseCallback responseCallback){
        if(provider == null || responseCallback == null || !provider.requireOtp || phone == null || phone.length() <= 0){
            return;
        }
        walletQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    if(provider.id == AMOLE){
                        JSONObject requestBody = new JSONObject();
                        requestBody.put("phonenumber",phone);
                        requestBody.put("description","otp");
                        RequestBody body = RequestBody.create(MediaType.parse("application/json"), requestBody.toString());
                        Response<ResponseBody> response;
                        switch (provider.id) {
                            case AMOLE:
                                response = walletRequest.sendOtpToAmole(body).execute();
                                break;
                            default:
                                return;
                        }
                        if(response.isSuccessful()){
                            responseCallback.onResponse(response.body().string(),null);
                        }else{
                            responseCallback.onResponse(null,ErrorUtils.createError(response.errorBody(),response.code()));
                        }

                    }

                }catch (Exception ignore){

                }
            }
        });
    }

    public int loadWallet(){
        int reqId = lastRequestToken.getAndIncrement();
        walletQueue.postRunnable(() -> {
            try {
                Call<WalletModel.Wallet> call = walletRequest.loadWallet();
                callSparseArray.put(reqId,call);
                Response<WalletModel.Wallet> response =  call.execute();
                callSparseArray.remove(reqId);
                if(response.isSuccessful()){
                    AndroidUtilities.runOnUIThread(() -> getNotificationCenter().postNotificationName(NotificationCenter.didWalletLoaded,true,response.body()));
                }else{
                    ResponseBody errorBody =  response.errorBody();
                    int code = response.code();
                    AndroidUtilities.runOnUIThread(() -> getNotificationCenter().postNotificationName(NotificationCenter.didWalletLoaded,false, ErrorUtils.createError(errorBody,code)));
                }
            } catch (Exception e) {
                callSparseArray.remove(reqId);
                AndroidUtilities.runOnUIThread(() -> getNotificationCenter().postNotificationName(NotificationCenter.didWalletLoaded,false,ErrorUtils.createNetworkError()));

            }
        });
        return reqId;
    }


    public int loadTransaction(String link){
        int reqId = lastRequestToken.getAndIncrement();
        walletQueue.postRunnable(() -> {
            try {
                Call<ResponseBody> call;
                Response<ResponseBody> response;
                if(ShopUtils.isEmpty(link)){
                    call = walletRequest.loadTransactions();
                }else{
                    call = walletRequest.loadMoreData(link);
                }
                callSparseArray.put(reqId,call);
                response = call.execute();
                callSparseArray.remove(reqId);
                if(response.isSuccessful()){
                    ArrayList<WalletModel.Transaction> transactionArrayList = new ArrayList<>();
                    Gson gson = new Gson();
                    JSONObject rawObject = new JSONObject(response.body().string());
                    int count = rawObject.getInt("count");
                    String next = rawObject.getString("next");
                    JSONArray dataArray = rawObject.getJSONArray("results");
                    for (int a = 0; a < dataArray.length(); a++){
                        JSONObject jsonObject = dataArray.getJSONObject(a);
                        WalletModel.Transaction review = gson.fromJson(jsonObject.toString(),WalletModel.Transaction.class);
                        transactionArrayList.add(review);
                    }
                    Collections.reverse(transactionArrayList);
                    AndroidUtilities.runOnUIThread(() -> getNotificationCenter().postNotificationName(NotificationCenter.didTransactionLoaded,true,transactionArrayList,next));
                }else{
                    ResponseBody errorBody =  response.errorBody();
                    int code = response.code();
                    AndroidUtilities.runOnUIThread(() ->getNotificationCenter().postNotificationName(NotificationCenter.didTransactionLoaded,false,ErrorUtils.createError(errorBody,code)));
                }
            }catch (Exception e){
                callSparseArray.remove(reqId);
                AndroidUtilities.runOnUIThread(() -> getNotificationCenter().postNotificationName(NotificationCenter.didTransactionLoaded,false,ErrorUtils.createNetworkError()));
            }

        });
        return reqId;
    }




    public void depositToSelfWallet(double amount, String phone,String description,WalletModel.Provider provider,ResponseCallback responseDelegate){
        if(responseDelegate == null || provider ==null || amount < 0 || TextUtils.isEmpty(phone)){
            return;
        }
        walletQueue.postRunnable(() -> {
            try{
                JSONObject requestBody = new JSONObject();
                requestBody.put("amount",amount);
                requestBody.put("useridentifier", phone);
                requestBody.put("description",description);
                RequestBody body = RequestBody.create(MediaType.parse("application/json"), requestBody.toString());
                Response<ResponseBody> response;
                switch (provider.id) {
                    case HELLO_CASH:
                        response = walletRequest.depositFromHelloCash(body).execute();
                        break;
                    case MBIRR:
                        response = walletRequest.depositFromMbirrCash(body).execute();
                        break;
                    case AMOLE:
                        response = walletRequest.depositFromAmole(body).execute();
                        break;
                    default:
                        return;
                }
                if(response.isSuccessful()){
                    responseDelegate.onResponse(response.body().string(),null);
                }else{
                    responseDelegate.onResponse(null,ErrorUtils.createError(response.errorBody(),response.code()));
                }
            }catch (Exception ignore){
                responseDelegate.onResponse(null,ErrorUtils.createNetworkError());
            }
        });
    }

    public void depositToOtherWallet(String otp, int to_user_id, String description, double amount, String phone, WalletModel.Provider provider, ResponseCallback callback){
        if(amount < 0 || phone == null || provider == null || callback == null){
            return;
        }
        if(provider.requireOtp && (otp == null || otp.length() <= 0)){
            return;
        }
        walletQueue.postRunnable(() -> {
            try{
                JSONObject requestBody = new JSONObject();
                if(provider.requireOtp){
                    requestBody.put("otp_code",otp);
                }
                requestBody.put("receiver", to_user_id);
                requestBody.put("amount",amount);
                requestBody.put("useridentifier", phone);
                requestBody.put("description",description);
                RequestBody body = RequestBody.create(MediaType.parse("application/json"), requestBody.toString());
                Response<ResponseBody> response;
                switch (provider.id) {
                    case HELLO_CASH:
                        response = walletRequest.transferFromHelloCashToReceiverWallet(body).execute();
                        break;
                    case MBIRR:
                        response = walletRequest.transferFromMbirrToReceiverWallet(body).execute();
                        break;
                    case AMOLE:
                        response = walletRequest.transferFromAmoleToReceiverWallet(body).execute();
                        break;
                    default:
                        return;
                }
                if(response.isSuccessful()){
                    callback.onResponse(response.body().string(),null);
                }else{
                    callback.onResponse(null,ErrorUtils.createError(response.errorBody(),response.code()));
                }
            }catch (Exception ignore){
                callback.onResponse(null,ErrorUtils.createNetworkError());
            }
        });


    }

    public void transferToWallet(double amount, int to_user_id, String description, ResponseCallback responseCallback){
        if(amount < 0 || responseCallback == null){
            return;
        }
        walletQueue.postRunnable(() -> {
            try{
                JSONObject requestBody = new JSONObject();
                requestBody.put("amount",amount);
                requestBody.put("receiver", to_user_id);
               // requestBody.put("description",description);
                RequestBody body = RequestBody.create(MediaType.parse("application/json"), requestBody.toString());
                Response<ResponseBody> response = walletRequest.transferToWallet(body).execute();
                if(response.isSuccessful()){
                    responseCallback.onResponse(response.body().string(),null);
                }else{
                    responseCallback.onResponse(null,ErrorUtils.createError(response.errorBody(),response.code()));
                }
            }catch (Exception ignore){
                responseCallback.onResponse(null,ErrorUtils.createNetworkError());
            }
        });
    }

    public void clear(){
        paymentProviders.clear();
        providerHashMap.clear();
    }

}
