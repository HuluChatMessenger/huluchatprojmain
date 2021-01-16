package org.plus.net;

import com.google.android.exoplayer2.util.Log;
import com.google.android.gms.common.api.ApiException;

import org.plus.apps.business.data.ShopDataController;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.UserConfig;

import java.io.IOException;
import java.lang.annotation.Annotation;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.http.Body;

public class ErrorUtils {


    public static final int NETWORK_ERROR = -100;

    public static APIError parseError(Response<?> response) {
        APIError error = new APIError();
        try {
            Log.e("ErrorUtils",response.errorBody() + " error body");
            //error = response.er
        } catch (Exception e) {
            return new APIError();
        }
        return error;
    }


    public static APIError createNetworkError(){
        APIError error = new APIError();
        error.setStatusCode(NETWORK_ERROR);
        if(!ApplicationLoader.isNetworkOnline()){
            error.setMessage("connection error!");
        }else{
            error.setMessage("unknown error! try again later");

        }
        return error;
    }

    public static APIError createError(ResponseBody errorBody,int code){
        APIError apiError  = new APIError();
        try {
            apiError.setMessage(errorBody.string());
            apiError.setStatusCode(code);
        } catch (IOException e) {
            apiError.setMessage("unknown error!");
            e.printStackTrace();
        }
        return apiError;
    }


}