//package org.plus.net;
//
//import org.plus.apps.wallet.WalletDataSerializer;
//
//import okhttp3.RequestBody;
//import okhttp3.ResponseBody;
//import retrofit2.Call;
//import retrofit2.http.Body;
//import retrofit2.http.GET;
//import retrofit2.http.POST;
//import retrofit2.http.Url;
//
//
//public interface WalletRequest {
//
//
//
//
//    @GET("/hulupay/wallet.personal/")
//    Call<WalletDataSerializer.Wallet> loadWallet();
//
//
//
//    @GET("/hulupay/payment.transactions/")
//    Call<ResponseBody> transactions();
//
//
//    /**
//     *
//     * wallet to wallwet transfer
//     *
//     * "amount" : 1000,
//     *     "receiver" : 392957340
//     *
//     * @param data
//     * @return
//     */
//
//    @POST("/hulupay/wallet.transfer/")
//    Call<ResponseBody> transfer(@Body RequestBody data);
//
//
//    @GET
//    Call<ResponseBody> loadRemoteData(@Url String url);
//    /**
//     *
//     * amole api
//     *
//     *
//     * request body
//     * {
//     *     "phonenumber" : "251923427836",
//     *     "description" : "justany"
//     *  }
//     */
//    @POST("/hulupay/amole/otp/")
//    Call<ResponseBody> sendOtpToAmole(@Body RequestBody requestBody);
//
//
//    /**
//     *
//     *   "otp_code" : "1234",
//     *     "useridentifier" : "251923427836",
//     *     "description" : "anydescription",
//     *     "amount" : 1000
//     */
//    @POST("hulupay/amole/deposit/")
//    Call<ResponseBody> depositFromAmole(@Body RequestBody requestBody);
//
//
//    /**
//     *
//     *    "otp_code" : "1234",
//     *     "useridentifier" : "251923427836",
//     *     "description" : "anydescription",
//     *     "amount" : 1000 ,
//     *     "receiver" : 392957340
//     *
//     * @param requestBody
//     * @return
//     */
//
//    @POST("/hulupay/amole/transfer/")
//    Call<ResponseBody> transferFromAmoleToReceiverWallet(@Body RequestBody requestBody);
//
//
//    /**
//     * deposti to hello cash
//     * amole
//     *
//     *  "useridentifier" : "251923427836",
//     *     "description" : "anydescription",
//     *     "amount" : 1000
//     *
//     *
//     * @param requestBody
//     * @return
//     */
//
//    @POST("/hulupay/hellocash/deposit/")
//    Call<ResponseBody> depositFromHelloCash(@Body RequestBody requestBody);
//
//
//    /**
//     *  "otp_code" : "1234",
//     *     "useridentifier" : "251923427836",
//     *     "description" : "anydescription",
//     *     "amount" : 1000 ,
//     *     "receiver" : 392957340
//     *
//     * @param requestBody
//     * @return
//     */
//
//    @POST("/hulupay/hellocash/transfer/")
//    Call<ResponseBody> transferFromHelloCashToReceiverWallet(@Body RequestBody requestBody);
//
//
//    @POST("/hulupay/mbirr/deposit/")
//    Call<ResponseBody> depositFromMbirrCash(@Body RequestBody requestBody);
//
//
//    /**
//     *  "otp_code" : "1234",
//     *     "useridentifier" : "251923427836",
//     *     "description" : "anydescription",
//     *     "amount" : 1000 ,
//     *     "receiver" : 392957340
//     *
//     * @param requestBody
//     * @return
//     */
//
//    @POST("/hulupay/mbirr/transfer/")
//    Call<ResponseBody> transferFromMbirrToReceiverWallet(@Body RequestBody requestBody);
//
//
//
//
//}
