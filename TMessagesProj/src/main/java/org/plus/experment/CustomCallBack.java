package org.plus.experment;

import android.util.Log;

import org.json.JSONObject;
import org.plus.net.APIError;
import org.plus.net.ResponseDelegate;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomCallBack<T> implements Callback<T> {

    private ResponseDelegate<T> callback;
    private boolean canceled;

    public CustomCallBack(ResponseDelegate<T> callback) {
        this.callback = callback;
        canceled = false;
    }


    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if(!canceled){
            Log.d("onResponse",response.code() + " =  code");
            Log.d("onResponse", response.message() + " response");
            if(response.errorBody() != null){
                try {
                    JSONObject jObjError = new JSONObject(response.errorBody().string());
                    Log.d("onResponse", jObjError.toString() + " response");

                } catch (Exception e) {
                    Log.d("onResponse", "json ex " + e.getMessage());

                }
            }
            if(response.isSuccessful()){
                callback.run(response,null);
            }else{

                APIError error = new APIError();
                error.setStatusCode(response.code());
                error.setMessage("server error!");
                callback.run(response,error);
            }
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        if(!canceled){
            APIError error = new APIError();
            error.setMessage("Network problem!");
            error.setStatusCode(-1);
            callback.run(null,error);
        }
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void cancel() {
        canceled = true;
        callback = null;
    }

}
