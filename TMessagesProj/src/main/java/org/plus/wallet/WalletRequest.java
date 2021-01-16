package org.plus.wallet;

import org.plus.apps.wallet.WalletDataSerializer;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Url;


public interface WalletRequest {

    @GET("/hulupay/wallet.personal/")
    Call<WalletModel.Wallet> loadWallet();

    @GET("/hulupay/payment.transactions/")
    Call<ResponseBody> loadTransactions();

    @POST("/hulupay/wallet.transfer/")
    Call<ResponseBody> transferToWallet(@Body RequestBody data);

    @POST("/hulupay/amole/otp/")
    Call<ResponseBody> sendOtpToAmole(@Body RequestBody requestBody);

    @POST("hulupay/amole/deposit/")
    Call<ResponseBody> depositFromAmole(@Body RequestBody requestBody);

    @POST("/hulupay/amole/transfer/")
    Call<ResponseBody> transferFromAmoleToReceiverWallet(@Body RequestBody requestBody);

    @POST("/hulupay/hellocash/deposit/")
    Call<ResponseBody> depositFromHelloCash(@Body RequestBody requestBody);

    @POST("/hulupay/hellocash/transfer/")
    Call<ResponseBody> transferFromHelloCashToReceiverWallet(@Body RequestBody requestBody);

    @POST("/hulupay/mbirr/deposit/")
    Call<ResponseBody> depositFromMbirrCash(@Body RequestBody requestBody);

    @POST("/hulupay/mbirr/transfer/")
    Call<ResponseBody> transferFromMbirrToReceiverWallet(@Body RequestBody requestBody);

    @GET
    Call<ResponseBody> loadMoreData(@Url String url);

}
