package org.plus.net;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import org.plus.apps.business.data.ShopDataController;
import org.plus.apps.ride.RideRequest;
import org.plus.experment.PlusBuildVars;
import org.plus.wallet.WalletRequest;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BaseController;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.DispatchQueue;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.SerializedData;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;

import java.io.File;
import java.io.IOException;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class  RequestManager extends BaseController {

    public static final String TAG = RequestManager.class.getSimpleName();
    public static final String server = PlusBuildVars.SERVER_ADDRESS;

    private final SharedPreferences sharedPreferences;

    //request interfaces
    private ShopRequest shopInterface;
    private WalletRequest walletRequest;
    private RideRequest rideRequest;



    private OkHttpClient okHttpClient;


    public TokenSerializer tokenSerializer;
    public boolean resolvingBot;
    public String auth_bot;
    public String auth_bot_query;

    public boolean isRequestingTokenFromBot;
    public boolean savingToken;

    private static final RequestManager[] Instances = new RequestManager[UserConfig.MAX_ACCOUNT_COUNT];

    public static RequestManager getInstance(int num) {
        RequestManager localInstance = Instances[num];
        if (localInstance == null) {
            synchronized (RequestManager.class) {
                localInstance = Instances[num];
                if (localInstance == null) {
                    Instances[num] = localInstance = new RequestManager(num);
                }
            }
        }
        return localInstance;
    }


    //logout
    public void clear(){
        getPreferences(currentAccount).edit().clear().commit();
    }

    private RequestManager(int num) {
        super(num);
        if (currentAccount == 0) {
            sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("appPref", Activity.MODE_PRIVATE);
        } else {
            sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("appPref" + currentAccount, Activity.MODE_PRIVATE);
        }

        auth_bot_query = sharedPreferences.getString("auth_bot_query","devtest");
        auth_bot = sharedPreferences.getString("auth_bot", "devtest");

        auth_bot_query = "devtest";
        auth_bot = PlusBuildVars.getAuthBot();

        String token = sharedPreferences.getString("token","");

        if(!TextUtils.isEmpty(token)){
            byte[] bytes = Base64.decode(token, Base64.URL_SAFE);
            SerializedData data = new SerializedData(bytes);
            tokenSerializer = new TokenSerializer();
            tokenSerializer.access = data.readString(false);
            tokenSerializer.refresh = data.readString(false);
        }
    }


    public RideRequest getRideRequest() {
        if(rideRequest == null){
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();


            HttpLoggingInterceptor loggerInterceptor = new HttpLoggingInterceptor();
            loggerInterceptor.level(HttpLoggingInterceptor.Level.BODY);


            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(NetworkInterceptor.getInstance(currentAccount))
                    .addInterceptor(loggerInterceptor)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://maps.googleapis.com/")
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(httpClient.build())
                    .build();
            rideRequest = retrofit.create(RideRequest.class);
        }
        return rideRequest;
    }
    public ShopRequest getShopInterface(){
        if(shopInterface == null){
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(server)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(getOkHttpClient())
                    .build();

            shopInterface = retrofit.create(ShopRequest.class);
        }
        return shopInterface;
    }
    public WalletRequest getWalletRequest(){
        if(walletRequest == null){
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(server)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(getOkHttpClient())
                    .build();
            walletRequest = retrofit.create(WalletRequest.class);
        }
        return walletRequest;
    }

    public void checkForRemoveConfig(){
        FirebaseTask task = new FirebaseTask(currentAccount);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null, null, null);
    }

    public static SharedPreferences getPreferences(int account) {
        return getInstance(account).sharedPreferences;
    }

    public static SharedPreferences getGlobalPreferences() {
        return getInstance(0).sharedPreferences;
    }


    public  OkHttpClient getOkHttpClient(){
        if(okHttpClient == null){

            HttpLoggingInterceptor loggerInterceptor = new HttpLoggingInterceptor();
            loggerInterceptor.level(HttpLoggingInterceptor.Level.BODY);

            File httpCacheDirectory = new File(ApplicationLoader.getFilesDirFixed(), "http-cache");
            int cacheSize = 10 * 1024 * 1024; // 10 MiB
            Cache cache = new Cache(httpCacheDirectory, cacheSize);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(NetworkInterceptor.getInstance(currentAccount))
                    .addInterceptor(loggerInterceptor)
                    .cache(cache)
                    .build();

            okHttpClient = httpClient.build();
        }
        return okHttpClient;
    }


    public static class TokenSerializer {

        @SerializedName("access")
        public String access;

        @SerializedName("refresh")
        public String refresh;
    }

    private void activateAndSaveAuthBot() {
        if(resolvingBot){
            return;
        }
        resolvingBot = true;
        TLRPC.TL_contacts_resolveUsername req = new TLRPC.TL_contacts_resolveUsername();
        req.username = auth_bot;
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> {
            resolvingBot = false;
            if (response != null) {
                AndroidUtilities.runOnUIThread(() -> {
                    TLRPC.TL_contacts_resolvedPeer res = (TLRPC.TL_contacts_resolvedPeer) response;
                    MessagesController.getInstance(currentAccount).putUsers(res.users, false);
                    MessagesController.getInstance(currentAccount).putChats(res.chats, false);
                    MessagesStorage.getInstance(currentAccount).putUsersAndChats(res.users, res.chats, true, true);
                    requestTokenFromBot();
                });
            }
        });
    }

    public void refreshToken(String refresh){
        ShopDataController.shopDataControllerQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    retrofit2.Response<ResponseBody> response  = getShopInterface().refreshToken(refresh).execute();
                    if(response.isSuccessful()){
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    saveTokensToFile(response.body().string());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }else {
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                requestTokenFromBot();
                            }
                        });
                    }
                } catch (Exception e) {
                    if(PlusBuildVars.LOGS_ENABLED){
                        Log.i(TAG,"refreshToken:" + e.getMessage());
                    }
                }
            }
        });
    }


    public void requestTokenFromBot() {
        if(isRequestingTokenFromBot){
            return;
        }
        isRequestingTokenFromBot = true;
        TLObject object = MessagesController.getInstance(currentAccount).getUserOrChat(auth_bot);
        if (object instanceof TLRPC.User) {
            TLRPC.User user = (TLRPC.User) object;
            long dialogId = UserConfig.getInstance(currentAccount).getClientUserId();
            TLRPC.TL_messages_getInlineBotResults req = new TLRPC.TL_messages_getInlineBotResults();
            req.query = auth_bot_query;
            req.bot = MessagesController.getInstance(currentAccount).getInputUser(user);
            req.offset = "";
            int lower_id = (int) dialogId;
            if (lower_id != 0) {
                req.peer = MessagesController.getInstance(currentAccount).getInputPeer(lower_id);
            } else {
                req.peer = new TLRPC.TL_inputPeerEmpty();
            }
            RequestDelegate requestDelegate = (response, error) -> {
                isRequestingTokenFromBot = false;
                if (error == null) {
                    if (response instanceof TLRPC.TL_messages_botResults) {
                        TLRPC.TL_messages_botResults res = (TLRPC.TL_messages_botResults) response;
                        for (int a = 0; a < res.results.size(); a++) {
                            TLRPC.BotInlineResult result = res.results.get(a);
                            if (result.send_message != null && result.send_message.message.length() > 1) {
                                saveTokensToFile(result.send_message.message);
                            }
                        }
                    }
                }
            };
            ConnectionsManager.getInstance(currentAccount).sendRequest(req, requestDelegate, ConnectionsManager.RequestFlagFailOnServerErrors);
        } else {
            isRequestingTokenFromBot = false;
            activateAndSaveAuthBot();

        }
    }

    private boolean shouldProceedRequest(){
        return !isRequestingTokenFromBot && !savingToken && !resolvingBot;

    }

    public void saveTokensToFile(String jwt){
        try {
            savingToken = true;
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();
            TokenSerializer tokenSer =  gson.fromJson(jwt,TokenSerializer.class);
            if(tokenSer == null){
                return;
            }
            SerializedData serializedData = new SerializedData();
            serializedData.writeString(tokenSer.access);
            serializedData.writeString(tokenSer.refresh);
            tokenSerializer = tokenSer;
            getPreferences(currentAccount).edit().putString("token",  Base64.encodeToString(serializedData.toByteArray(), Base64.URL_SAFE)).commit();
            NetworkInterceptor.getInstance(currentAccount).updateAccessToken(tokenSerializer);
            savingToken = false;
        }catch (Exception e){
           FileLog.e(e);
        }

    }

    public static class NetworkInterceptor implements Interceptor {

        private TokenSerializer tokenSerializer;
        private final int currentAccount;
        private boolean refreshing;

        private static final NetworkInterceptor[] InterceptorsInstance = new NetworkInterceptor[UserConfig.MAX_ACCOUNT_COUNT];

        public static NetworkInterceptor getInstance(int num) {
            NetworkInterceptor localInstance = InterceptorsInstance[num];
            if (localInstance == null) {
                synchronized (NetworkInterceptor.class) {
                    localInstance = InterceptorsInstance[num];
                    if (localInstance == null) {
                        InterceptorsInstance[num] = localInstance = new NetworkInterceptor(num);
                    }
                }
            }
            return localInstance;
        }

        public NetworkInterceptor(int a) {
            currentAccount = a;
            tokenSerializer = RequestManager.getInstance(a).tokenSerializer;
        }

        public void updateAccessToken(TokenSerializer access_token) {
            tokenSerializer = access_token;
        }


        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            Request.Builder newRequest;
            if (!TextUtils.isEmpty(request.header("no-json"))) {
                newRequest = request.newBuilder()
                        .addHeader("Accept", "application/json")
                        .addHeader("Content-Type", "multipart/form-data");
            } else {
                newRequest = request.newBuilder().addHeader("Accept", "application/json");
            }
            String auth = request.header("no-auth");
            if (!TextUtils.isEmpty(auth)) {
                return chain.proceed(newRequest.build());
            }

            if (tokenSerializer != null) {
                newRequest.addHeader("Authorization", "Bearer " + tokenSerializer.access);
            }

            if (tokenSerializer == null && RequestManager.getInstance(currentAccount).shouldProceedRequest()) {
                RequestManager.getInstance(currentAccount).requestTokenFromBot();
                return chain.proceed(newRequest.build());
            }


            Response response = chain.proceed(newRequest.build());
            if (response.code() == 403 && RequestManager.getInstance(currentAccount).shouldProceedRequest()) {
                if(tokenSerializer != null && tokenSerializer.refresh != null){
                   // RequestManager.getInstance(currentAccount).requestTokenFromBot();

                    RequestManager.getInstance(currentAccount).refreshToken(tokenSerializer.refresh);
                }else{
                    RequestManager.getInstance(currentAccount).requestTokenFromBot();

                    //  RequestManager.getInstance(currentAccount).requestTokenFromBot();

                }

            }

//            int tryCount = 0;
//            while (!response.isSuccessful() && tryCount < 3) {
//                tryCount++;
//                response = chain.proceed(newRequest.build());
//            }

            return response;
        }



    }


    public void applyRemoteConfig(RemoteObject remoteObject){
        if (remoteObject == null) {
            return;
        }
        sharedPreferences.edit().putString("auth_bot_query",remoteObject.authQuery).commit();
        sharedPreferences.edit().putString("auth_bot",remoteObject.authBot).commit();
        auth_bot = remoteObject.authBot;
        auth_bot_query = remoteObject.authQuery;

       long conf_update = sharedPreferences.getLong("conf_update_number", 0);
       if(remoteObject.conf_update > conf_update){
           AndroidUtilities.runOnUIThread(() -> ShopDataController.getInstance(currentAccount).updateProductConfiguration(new ShopDataController.ConfigDelegate() {
               @Override
               public void didFinishUpdating(boolean success) {
                   sharedPreferences.edit().putLong("conf_update_number",remoteObject.conf_update).commit();
                   Log.i("berhan","configraion updateed with value " + remoteObject.conf_update);
               }
           }));
       }

    }

    public static class RemoteObject{

        public String authBot;
        public String authQuery;
        public long conf_update;
        public long last_update_time;

        @Override
        public String toString() {
            return "RemoteObject{" +
                    "authBot='" + authBot + '\'' +
                    ", authQuery='" + authQuery + '\'' +
                    ", conf_update=" + conf_update +
                    '}';
        }
    }

    private static class FirebaseTask extends AsyncTask<Void, Void, RemoteObject> {

        private int currentAccount;
        private FirebaseRemoteConfig firebaseRemoteConfig;

        public FirebaseTask(int instance) {
            super();
            currentAccount = instance;
        }


        protected RemoteObject doInBackground(Void... voids) {
            try {
                firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
                FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder().setDeveloperModeEnabled(BuildConfig.DEBUG).build();
                firebaseRemoteConfig.setConfigSettings(configSettings);
                RemoteObject current_remoteObject = new RemoteObject();

                current_remoteObject.authBot = firebaseRemoteConfig.getString("auth_bot");
                current_remoteObject.authQuery = firebaseRemoteConfig.getString("auth_query");
                current_remoteObject.conf_update = firebaseRemoteConfig.getLong("conf_update");
                 if (BuildVars.LOGS_ENABLED) {
                      Log.d("RemoteObject","current firebase value = " + current_remoteObject.toString());
                }

                firebaseRemoteConfig.fetch(0).addOnCompleteListener(finishedTask -> {
                    final boolean success = finishedTask.isSuccessful();
                    Utilities.stageQueue.postRunnable(() -> {
                        RemoteObject config = null;
                        if (success) {
                            firebaseRemoteConfig.activateFetched();
                            config =new RemoteObject();
                            config.authBot = firebaseRemoteConfig.getString("auth_bot");
                            config.authQuery = firebaseRemoteConfig.getString("auth_query");
                            config.conf_update = firebaseRemoteConfig.getLong("conf_update");
                            config.last_update_time = firebaseRemoteConfig.getInfo().getFetchTimeMillis();
                            if (config != null) {
                                RequestManager.getInstance(currentAccount).applyRemoteConfig(config);
                            }
                        }
                    });
                });
            } catch (Throwable e) {
                Utilities.stageQueue.postRunnable(() -> {
                    if (BuildVars.LOGS_ENABLED) {
                        FileLog.d("failed to get firebase result");
                        FileLog.d("start dns txt task");
                    }

                });
                FileLog.e(e);
            }
            return null;
        }


        @Override
        protected void onPostExecute(RemoteObject result) {

        }
    }

}