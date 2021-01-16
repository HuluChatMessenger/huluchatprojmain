package org.plus.apps.ride;

import org.plus.apps.wallet.WalletDataSerializer;
import org.plus.experment.PlusBuildVars;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

public interface RideRequest {

    @GET("/maps/api/place/textsearch/json")
    Call<ResponseBody> findPlaces(@QueryMap Map<String, Object> data);

    @GET("maps/api/directions/json/")
    Call<ResponseBody> getRoute(@QueryMap Map<String, Object> data);

    @GET
    Call<ResponseBody> getRoute(@Url String url);


}
