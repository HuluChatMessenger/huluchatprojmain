//package org.plus.apps.wallet;
//
//import android.app.Activity;
//import android.content.SharedPreferences;
//import android.graphics.drawable.Drawable;
//import android.os.Build;
//import android.security.keystore.KeyInfo;
//import android.security.keystore.KeyProperties;
//import android.telephony.PhoneNumberUtils;
//import android.util.Log;
//import android.util.SparseArray;
//
//import com.google.gson.Gson;
//import com.google.protobuf.Api;
//
//import org.json.JSONArray;
//import org.json.JSONObject;
//import org.plus.apps.business.ShopUtils;
//import org.plus.experment.PlusBuildVars;
//import org.plus.net.APIError;
//import org.plus.net.ErrorUtils;
//import org.plus.net.RequestManager;
//import org.plus.wallet.WalletRequest;
//import org.telegram.messenger.AndroidUtilities;
//import org.telegram.messenger.ApplicationLoader;
//import org.telegram.messenger.BaseController;
//import org.telegram.messenger.DispatchQueue;
//import org.telegram.messenger.FileLog;
//import org.telegram.messenger.LocaleController;
//import org.telegram.messenger.NotificationCenter;
//import org.telegram.messenger.R;
//import org.telegram.messenger.UserConfig;
//import org.telegram.messenger.UserObject;
//import org.telegram.tgnet.RequestDelegate;
//import org.telegram.tgnet.TLRPC;
//import org.telegram.ui.ActionBar.Theme;
//import org.telegram.ui.StickersActivity;
//
//import java.security.Key;
//import java.security.KeyFactory;
//import java.security.KeyPairGenerator;
//import java.security.KeyStore;
//import java.security.PrivateKey;
//import java.security.spec.MGF1ParameterSpec;
//import java.time.Instant;
//import java.util.ArrayList;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import javax.crypto.Cipher;
//import javax.crypto.spec.OAEPParameterSpec;
//import javax.crypto.spec.PSource;
//
//import okhttp3.MediaType;
//import okhttp3.RequestBody;
//import okhttp3.ResponseBody;
//import retrofit2.Call;
//import retrofit2.Response;
//
//public class WalletController extends BaseController {
//
//    public interface ResponseDelegate{
//        void onResponse(Object response, APIError error);
//    }
//
//
//    public static Drawable [] statusDrawables = new Drawable[4];
//
//    public static String WALLET_STATUS_PENDING = "REQUESTED";
//    public static String WALLET_STATUS_SUCCESSFUL= "SUCCESSFUL";
//    public static String WALLET_STATUS_DECLINED= "DECLINED";
//
//    public static String TRANSACTION_TYPE_TRANSFER = "TRANSFER";
//    public static String TRANSACTION_TYPE_PAY= "PAY";
//    public static String TRANSACTION_TYPE_DEPOSIT = "DEPOSIT";
//    public static String TRANSACTION_TYPE_WITHDRAW = "WITHDRAW";
//
//    public static String TRANSACTION_TYPE_SEND = "SENT";
//    public static String TRANSACTION_TYPE_RECEIVED = "Received";
//
//
////    public static String getTransferType(WalletDataSerializer.Transaction transaction){
////        if(transaction.previous_balance > transaction.current_balance){
////            return TRANSACTION_TYPE_SEND;
////        }else{
////            return TRANSACTION_TYPE_RECEIVED;
////
////        }
//    //}
//
//    public static final String TAG = WalletController.class.getSimpleName();
//
//    private volatile SparseArray<Call> callSparseArray = new SparseArray<>();
//    private WalletRequest walletRequest;
//    private SharedPreferences walletPref;
//    private AtomicInteger lastRequestToken = new AtomicInteger(1);
//    public static DispatchQueue walletControllerQueue = new DispatchQueue("WalletControllerQueue");
//
//    private static volatile WalletController[] Instance = new WalletController[UserConfig.MAX_ACCOUNT_COUNT];
//
//    public static WalletController getInstance(int num) {
//        WalletController localInstance = Instance[num];
//        if (localInstance == null) {
//            synchronized (WalletController.class) {
//                localInstance = Instance[num];
//                if (localInstance == null) {
//                    Instance[num] = localInstance = new WalletController(num);
//                }
//            }
//        }
//        return localInstance;
//    }
//
//    private int currentAccount;
//
//    public final static int KEY_PROTECTION_TYPE_NONE = 0;
//    public final static int KEY_PROTECTION_TYPE_LOCKSCREEN = 1;
//    public final static int KEY_PROTECTION_TYPE_BIOMETRIC = 2;
//
//    public WalletController(int num){
//        super(num);
//        if (currentAccount == 0) {
//            walletPref = ApplicationLoader.applicationContext.getSharedPreferences("walletConfig", Activity.MODE_PRIVATE);
//        } else {
//            walletPref = ApplicationLoader.applicationContext.getSharedPreferences("walletConfig" + currentAccount, Activity.MODE_PRIVATE);
//        }
//        loadStatusDrawable();
//        walletControllerQueue.postRunnable(new Runnable() {
//            @Override
//            public void run() {
//                walletRequest = getRequestManager().getWalletRequest();
//            }
//        });
//
//    }
//
//    private void loadStatusDrawable(){
//
//        statusDrawables[0] = Theme.createCircleDrawableWithIcon(AndroidUtilities.dp(44), R.drawable.ic_round_arrow_upward_24).mutate();
//        statusDrawables[1] = Theme.createCircleDrawableWithIcon(AndroidUtilities.dp(44), R.drawable.ic_arrow_down).mutate();
//        statusDrawables[2] = Theme.createCircleDrawableWithIcon(AndroidUtilities.dp(44), R.drawable.msg_timer).mutate();
//        statusDrawables[3] = Theme.createCircleDrawableWithIcon(AndroidUtilities.dp(44), R.drawable.message_arrow).mutate();
//
//        Theme.setCombinedDrawableColor(statusDrawables[0], ShopUtils.getIntAlphaColor(Theme.getColor(Theme.key_calls_callReceivedRedIcon),0.2f), false);
//        Theme.setCombinedDrawableColor(statusDrawables[0], Theme.getColor(Theme.key_calls_callReceivedRedIcon), true);
//
//        Theme.setCombinedDrawableColor(statusDrawables[1], ShopUtils.getIntAlphaColor(Theme.getColor(Theme.key_calls_callReceivedGreenIcon),0.2f), false);
//        Theme.setCombinedDrawableColor(statusDrawables[1], Theme.getColor(Theme.key_calls_callReceivedGreenIcon), true);
//
//        Theme.setCombinedDrawableColor(statusDrawables[2], ShopUtils.getIntAlphaColor(Theme.getColor(Theme.key_dialogTextGray2),0.2f), false);
//        Theme.setCombinedDrawableColor(statusDrawables[2], Theme.getColor(Theme.key_dialogTextGray2), true);
//
//        Theme.setCombinedDrawableColor(statusDrawables[3], ShopUtils.getIntAlphaColor(Theme.getColor(Theme.key_calls_callReceivedGreenIcon),0.2f), false);
//        Theme.setCombinedDrawableColor(statusDrawables[3], Theme.getColor(Theme.key_calls_callReceivedGreenIcon), true);
//
//    }
//
//
//
//    private static KeyStore keyStore;
//    private static KeyPairGenerator keyGenerator;
//    private static Cipher cipher;
//
//    static {
//        try {
//            keyStore = KeyStore.getInstance("AndroidKeyStore");
//            keyStore.load(null);
//            if (Build.VERSION.SDK_INT >= 23) {
//                keyGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
//                cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
//            } else {
//                keyGenerator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
//                cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//            }
//        } catch (Exception e) {
//            FileLog.e(e);
//        }
//    }
//
//    public int getKeyProtectionType() {
//        if (Build.VERSION.SDK_INT >= 23) {
//            try {
//                Key key = keyStore.getKey("getUserConfig().tonKeyName", null);
//                KeyFactory factory = KeyFactory.getInstance(key.getAlgorithm(), "AndroidKeyStore");
//                KeyInfo keyInfo = factory.getKeySpec(key, KeyInfo.class);
//                if (keyInfo.isUserAuthenticationRequired()) {
//                    if (keyInfo.getUserAuthenticationValidityDurationSeconds() > 0) {
//                        return KEY_PROTECTION_TYPE_LOCKSCREEN;
//                    } else {
//                        return KEY_PROTECTION_TYPE_BIOMETRIC;
//                    }
//                }
//            } catch (Exception ignore) {
//
//            }
//        }
//        return KEY_PROTECTION_TYPE_NONE;
//    }
//
//    public static String formatCurrency(double  amount){
//        return amount + " ብር";
//    }
//    public static String formatCurrencyEmpty(double  amount){
//        return amount  + " ብር";
//    }
//
//    public static String getDateKey(String date){
//        long time = Instant.parse(date).toEpochMilli();
//        return LocaleController.formatSectionDate(time/1000);
//    }
//
//    public String formatWalletData(String data){
//        long instant = Instant.parse(data).toEpochMilli();
//        return LocaleController.formatSectionDate(instant/1000);
//    }
//
//    public static String formatSendMessage(WalletDataSerializer.Transaction transaction,int from_user_id,int to_user_id){
//        StringBuilder builder = new StringBuilder();
//        builder.append("Transfer Status: ").append(transaction.status).append("\n");
//        builder.append("Transfer Amount: ").append(transaction.amount).append("\n");
//        builder.append("Transfer Sender: ").append(from_user_id).append("\n");
//        builder.append("Transfer Recipient: ").append(to_user_id).append("\n");
//        builder.append("Transfer Date: ").append(transaction.created_at).append("\n");
//        builder.append("Transfer Processing: ").append("").append("\n");
//        builder.append("#huluchat_wallet: Wallet");
//        return builder.toString();
//    }
//
//
//
//
//    public static String formatWalletUserTransferText(TLRPC.User user){
//        String text = " Transfer to " + UserObject.getFirstName(user,false);
//        text += "\n userid : " + user.id  + " phone number : " + PhoneNumberUtils.formatNumber(user.phone);
//        return text;
//    }
//
//    public static String formatWalletAddress(int user_id,int amount){
//        String wallet_url = "wallet://transfer?user_id={userId}&amount";
//        wallet_url =  wallet_url.replace(wallet_url,String.valueOf(user_id));
//        return wallet_url;
//    }
//
//
//
//    public static String formatPhone(String phone){
//        if(phone.startsWith("251")){
//            return "+" + phone;
//        }
//        return phone;
//    }
//
//
//    public JSONObject createRequestBody(String provider, double amount, String phone){
//        if(provider == null){
//            return null;
//        }
//        try{
//
//            JSONObject requestBody = new JSONObject();
//            requestBody.put("amount",amount);
//            requestBody.put("useridentifier",formatPhone(phone));
//            requestBody.put("description","description");
//            return requestBody;
//        }catch (Exception ignore){
//
//        }
//        return null;
//    }
//
//
//
//    public Cipher getCipherForDecrypt() {
//        try {
//            PrivateKey key = (PrivateKey) keyStore.getKey(UserConfig.getInstance(currentAccount).tonKeyName, null);
//            OAEPParameterSpec spec = new OAEPParameterSpec("SHA-1", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT);
//            cipher.init(Cipher.DECRYPT_MODE, key, spec);
//            return cipher;
//        } catch (Exception e) {
//            FileLog.e(e);
//        }
//        return null;
//    }
//
//    public int loadWallet(){
//        int reqId = lastRequestToken.getAndIncrement();
//        walletControllerQueue.postRunnable(() -> {
//            try {
//                Call<WalletDataSerializer.Wallet> call = walletRequest.loadWallet();
//                callSparseArray.put(reqId,call);
//                Response<WalletDataSerializer.Wallet> response =  call.execute();
//                callSparseArray.remove(reqId);
//                if(response.isSuccessful()){
//                    AndroidUtilities.runOnUIThread(() -> getNotificationCenter().postNotificationName(NotificationCenter.didWalletLoaded,true,response.body()));
//                }else{
//                    ResponseBody errorBody =  response.errorBody();
//                    int code = response.code();
//                    AndroidUtilities.runOnUIThread(() -> getNotificationCenter().postNotificationName(NotificationCenter.didWalletLoaded,false,ErrorUtils.createError(errorBody,code)));
//                }
//            } catch (Exception e) {
//                callSparseArray.remove(reqId);
//                AndroidUtilities.runOnUIThread(() -> getNotificationCenter().postNotificationName(NotificationCenter.didWalletLoaded,false,ErrorUtils.createNetworkError()));
//
//                if(PlusBuildVars.LOGS_ENABLED){
//                    Log.i(TAG,e.toString());
//                }
//            }
//        });
//        return reqId;
//    }
//
//    public void loadTransaction(int class_guid,String link){
//        walletControllerQueue.postRunnable(() -> {
//            try {
//                Response<ResponseBody> response;
//                if(ShopUtils.isEmpty(link)){
//                    response = walletRequest.loadTransactions().execute();
//                }else{
//                    response = walletRequest.loadMoreData(link).execute();
//                }
//                if(response.isSuccessful()){
//                    ArrayList<WalletDataSerializer.Transaction> transactionArrayList = new ArrayList<>();
//                    Gson gson = new Gson();
//                    JSONObject rawObject = new JSONObject(response.body().string());
//                    int count = rawObject.getInt("count");
//                    String next = rawObject.getString("next");
//                    JSONArray dataArray = rawObject.getJSONArray("results");
//                    for (int a = 0; a < dataArray.length(); a++){
//                        JSONObject jsonObject = dataArray.getJSONObject(a);
//                        WalletDataSerializer.Transaction review = gson.fromJson(jsonObject.toString(),WalletDataSerializer.Transaction.class);
//                        transactionArrayList.add(review);
//                    }
//                    AndroidUtilities.runOnUIThread(() -> getNotificationCenter().postNotificationName(NotificationCenter.didTransactionLoaded,true,transactionArrayList,class_guid,next));
//                }else{
//                   ResponseBody errorBody =  response.errorBody();
//                   int code = response.code();
//                    AndroidUtilities.runOnUIThread(() ->getNotificationCenter().postNotificationName(NotificationCenter.didTransactionLoaded,false,ErrorUtils.createError(errorBody,code),class_guid));
//                }
//
//            }catch (Exception e){
//                AndroidUtilities.runOnUIThread(() -> getNotificationCenter().postNotificationName(NotificationCenter.didTransactionLoaded,false,ErrorUtils.createNetworkError(),class_guid));
//                if(PlusBuildVars.LOGS_ENABLED){
//                    Log.i(TAG,e.toString());
//                }
//            }
//
//        });
//    }
//
////    public void loadPaymentProviders(){
////        walletControllerQueue.postRunnable(() -> {
////            try {
////                Response<ResponseBody> response = walletRequest.loadPaymentProviders().execute();
////                if(response.isSuccessful()){
////                    ArrayList<WalletDataSerializer.PaymentProvider> providerArrayList = new ArrayList<>();
////                    Gson gson = new Gson();
////                    JSONObject rawObject = new JSONObject(response.body().string());
////                    int count = rawObject.getInt("count");
////                    String next = rawObject.getString("next");
////                    JSONArray dataArray = rawObject.getJSONArray("results");
////                    for (int a = 0; a < dataArray.length(); a++){
////                        JSONObject jsonObject = dataArray.getJSONObject(a);
////                        WalletDataSerializer.PaymentProvider review = gson.fromJson(jsonObject.toString(), WalletDataSerializer.PaymentProvider.class);
////                        providerArrayList.add(review);
////                    }
////                    AndroidUtilities.runOnUIThread(() -> getNotificationCenter().postNotificationName(NotificationCenter.didPaymentProvidersLoaded,true,providerArrayList,next));
////
////                }else{
////                    ResponseBody body = response.errorBody();
////                    int code = response.code();
////                    AndroidUtilities.runOnUIThread(() -> getNotificationCenter().postNotificationName(NotificationCenter.didPaymentProvidersLoaded,false,ErrorUtils.createError(body,code)));
////                }
////
////            }catch (Exception e){
////                AndroidUtilities.runOnUIThread(() -> getNotificationCenter().postNotificationName(NotificationCenter.didPaymentProvidersLoaded,false,ErrorUtils.createNetworkError()));
////                if(PlusBuildVars.LOGS_ENABLED){
////                    Log.i(TAG,e.toString());
////                }
////            }
////
////        });
////    }
//
//
//
//    public void depositToWallet(JSONObject payload,ResponseDelegate responseDelegate,String key){
//        walletControllerQueue.postRunnable(() -> {
//            try{
//                RequestBody body = RequestBody.create(MediaType.parse("application/json"), payload.toString());
//                Response<ResponseBody> response;
//                if(key.equals("hello_cash")){
//                    response = walletRequest.depositFromHelloCash(body).execute();
//                }else if(key.equals("mbirr")){
//                    response = walletRequest.depositFromMbirrCash(body).execute();
//                }else if(key.equals("amole")){
//                    response = walletRequest.depositFromAmole(body).execute();
//                }else{
//                    return;
//                }
//                if(response.isSuccessful()){
//                    if(responseDelegate != null){
//                        responseDelegate.onResponse(response.body().string(),null);
//                    }
//                }else{
//                    if(responseDelegate != null){
//                        responseDelegate.onResponse(null,ErrorUtils.createError(response.errorBody(),response.code()));
//                    }
//                }
//            }catch (Exception exception){
//                if(responseDelegate != null){
//                    responseDelegate.onResponse(null,ErrorUtils.createNetworkError());
//                }
//                if(PlusBuildVars.LOGS_ENABLED){
//                    Log.d(TAG,exception.getMessage());
//                }
//            }
//        });
//    }
////
//    public void transfer(double amount, String reason,int to_user_id,ResponseDelegate responseDelegate){
//        walletControllerQueue.postRunnable(() -> {
//            try{
//                JSONObject reqObj = new JSONObject();
//                reqObj.put("amount",amount);
//                reqObj.put("receiver",to_user_id);
//                reqObj.put("reason",reason);
//                Gson gson = new Gson();
//
//                RequestBody body =
//                        RequestBody.create(MediaType.parse("application/json"), reqObj.toString());
//
//
//                Response<ResponseBody> response = walletRequest.transferToWallet(body).execute();
//                if(response.isSuccessful()){
//                    WalletDataSerializer.Transaction transaction = gson.fromJson(response.body().string(),WalletDataSerializer.Transaction.class);
//                    AndroidUtilities.runOnUIThread(() -> {
//                        if(responseDelegate != null){
//                            responseDelegate.onResponse(transaction,null);
//                        }
//                    });
//                }else{
//                    AndroidUtilities.runOnUIThread(() -> {
//                        if(responseDelegate != null){
//                            responseDelegate.onResponse(null,ErrorUtils.createError(response.errorBody(),response.code()));
//                        }
//                    });
//                }
//            }catch (Exception e){
//                AndroidUtilities.runOnUIThread(() -> {
//                    if(responseDelegate != null){
//                        responseDelegate.onResponse(null,ErrorUtils.createNetworkError());
//                    }
//                });
//                Log.e(TAG,e.getMessage());
//            }
//
//
//        });
//
//    }
//
//    public void withdraw(){
//
//    }
//
//
//}
