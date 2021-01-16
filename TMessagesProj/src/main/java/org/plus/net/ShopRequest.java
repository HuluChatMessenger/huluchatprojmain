package org.plus.net;

import androidx.room.Delete;

import org.checkerframework.common.reflection.qual.GetClass;
import org.plus.apps.business.data.SR_object;
import org.plus.apps.business.data.ShopDataSerializer;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

public interface ShopRequest{

    @Multipart
    @Headers("no-json:true")
    @POST("/photo.upload/")
    Call<ShopDataSerializer.ImageUploadResult> uploadPhoto(@Part MultipartBody.Part image, @Part("order") RequestBody order);

    @POST("/shops/create/")
    Call<SR_object.shop_create_res> createShop(@Body Map<String ,Object> req);


    @GET("/shops/{id}/detail")
    Call<ResponseBody> loadShopByChannelId(@Path("id") long channel_id);


    @GET("/shops/{channel_id}/exists/")
    Call<ResponseBody> checkShop(@Path("channel_id") long channel_id);

    @GET("/configurations/")
    Call<ResponseBody> getConfigurations();

    @POST("shops/{id}/contact.telphones/")
    @FormUrlEncoded
    Call<ResponseBody> updateTelephoneContact(@Path("id") long channel_id, @Field("phonenumbers") String number);

    @POST("/shops/{id}/products/{type}/create/")
    Call<ResponseBody> listProduct(@Path("id") long channel_id,@Path("type")String type,@Body Map<String,Object> productTags);

    @GET("/shops/{id}/products/")
    Call<ResponseBody> loadProductsForShop(@Path("id")long channel_id,@Query("ordering") String sort);

    @GET
    Call<ResponseBody> loadMoreProduct(@Url String url);

    @GET
    Call<ResponseBody> loadMoreReview(@Url String url);

    @GET
    Call<ResponseBody> loadRemoteData(@Url String url);


    @GET("/grandshop/{bus_type}/products/")
    Call<ResponseBody> loadProductsForBusiness(@Path("bus_type")String bus_type,@Query("ordering") String sort);


    @POST("/grandshop/{bus_type}/products.filter/")
    Call<ResponseBody> filterProductFromBus(@Path("bus_type")String bus_type,@Body Map<String,Object> productTags,@Query("ordering") String sort);


    @GET("/shops/{id}/products/")
    Call<ResponseBody> filterProduct(@Path("id")long channel_id);

    @GET("/shops/{channel_id}/products/{product_id}/")
    Call<ResponseBody> loadProductById(@Path("channel_id")long channel_id,@Path("product_id") int product_id);


    //fitler product
    @POST("/shops/{channel_id}/products/{bus_type}/")
    Call<ResponseBody>  filterPro(@Path("id")long channel_id,@Path("bus_type")String bus_type, @QueryMap Map<String,Object> filterTags,@Query("ordering") String sort);
    //fitler product



    //main
    @GET("/shops/{channel_id}/products/")
    Call<ResponseBody> loadProducts(@Path("channel_id")long channel_id,@QueryMap Map<String,Object> query_map);


    @GET("/shops/{channel_id}/products/{bus_type}/")
    Call<ResponseBody> loadProducts(@Path("channel_id")long channel_id,@Path("bus_type")String bus_type,@QueryMap Map<String,Object> query_map);


    @GET("/grandshop/products/{bus_type}/")
    Call<ResponseBody> loadProducts(@Path("bus_type")String bus_type,@QueryMap Map<String,Object> query_map);

    @GET("/grandshop/products/{bus_type}/")
    Call<ResponseBody> loadProducts(@Path("bus_type")String bus_type);

    //


    @POST("/shops/{id}/products.filter/{bus_type}/")
    Call<ResponseBody>  filterProduct(@Path("id")long channel_id,@Path("bus_type")String bus_type,@Query("ordering") String sort);


    @GET("/shops/{id}/products/")
    Call<ResponseBody>  searchProductInShop(@Path("id")long channel_id,@Query("search") String search,@Query("ordering") String sort, @QueryMap Map<String,Object> filterTags);


    @GET("/shops/{id}/products/")
    Call<ResponseBody>  searchProInShop(@Path("id")long channel_id,@Query("search") String search,@Query("ordering") String sort, @QueryMap Map<String,Object> filterTags);


    @GET("/grandshop/{bus_type}/products.search/")
    Call<ResponseBody>  searchProductForBusiness(@Path("bus_type")String channel_id,@Query("search") String search,@Query("ordering") String sort);


    @POST("/shops/{shop_id}/products/{product_id}/offers/")
    Call<ResponseBody>  offerProductPrice(@Path("shop_id")long channel_id,@Path("product_id")int product_id,@Body SR_object.create_offer_req offer);


    @POST("shops/{shop_id}/reviews/")
    Call<ResponseBody> postReviewToaShop(@Path("shop_id")long shop_id,@Body SR_object.post_review_request post_review_request);


    @GET("/shops/{shop_id}/reviews")
    Call<ResponseBody>  loadShopReviews(@Path("shop_id")long channel_id,@Query("ordering") String sort);


    @GET("/shops/{shop_id}/offers")
    Call<ResponseBody>  listOffers(@Path("shop_id")long channel_id);


    @PUT("/shops/{channelID}/self/")
    Call<ResponseBody> updateShop(@Path("channelID")long channel_id,@Body Map<String,Object> shop);


    @GET("shops/{shop_id}/reviews.self/")
    Call<ResponseBody>  loadShopReviewSelf(@Path("shop_id")long channel_id);


    @PUT("shops/{shop_id}/reviews/{review_id}/")
    Call<ResponseBody>  updateShopReview(@Path("shop_id")long channel_id,@Path("review_id") int review_id);

    @DELETE("shops/{shop_id}/reviews/{review_id}/")
    Call<ResponseBody>  deleteShopReview(@Path("shop_id")long channel_id,@Path("review_id") int review_id);

    @GET("/grandshop/featured.products/")
    Call<ResponseBody>  loadFeaturedProducts();

    @GET("/grandshop/featured.shops/")
    Call<ResponseBody>  loadFeaturedShops();

    @POST("/shops/{channel_id}/products/{product_id}/favorite/")
    Call<ResponseBody> addProductToFav(@Path("channel_id")long channel_id,@Path("product_id")int product_id);

    @DELETE("/shops/{channel_id}/products/{product_id}/favorite/")
    Call<ResponseBody> deleteProductFromFav(@Path("channel_id")long channel_id,@Path("product_id")int product_id);

    @GET("shops/favorites.products/")
    Call<ResponseBody> loadFavoriteProducts();

    @GET("/grandshop/product-collections/")
    Call<ResponseBody> loadCollection();

    @GET("/grandshop/product-collections/{product_id}/")
    Call<ResponseBody> loadProductInCollection(@Path("product_id") int product_id);

    @GET("grandshop/products/")
    Call<ResponseBody> searchProduct(@Query("search") String search,@Query("ordering") String sort, @QueryMap Map<String,Object> filterTags);

    @GET("grandshop/products/")
    Call<ResponseBody> searchProduct(@Query("search") String search);

    @GET("grandshop/products/{bus_type}/")
    Call<ResponseBody> searchProduct(@Query("search") String search,@Path("bus_type") String busType);


    @GET("grandshop/shops/")
    Call<ResponseBody> searchShop(@Query("search") String search);

    @GET
    Call<ResponseBody> request(@Url String url);



    @PUT("/shops/{channel_id}/products/{product_id}/update/")
    Call<ResponseBody> updateProduct(@Path("channel_id")long channel_id,@Path("product_id")int product_id,@Body Map<String,Object> fields);


    //admins
    @POST("/shops/{channel_id}/admins.create/")
    Call<ResponseBody> addAdmin(@Path("channel_id")long channel_id,@Body Map<String,String > telegramID);


    @GET("/shops/{channel_id}/admins/")
    Call<ResponseBody> getShopAdmins(@Path("channel_id")long channel_id);


    @DELETE("/shops/{channel_id}/admins/{user_id}/")
    Call<ResponseBody> removeAdminFromShop(@Path("channel_id")long channel_id,@Path("user_id")int user_id);

    @DELETE("/shops/{channel_id}/products/{product_id}/photos/{photo_id}/")
    Call<ResponseBody> deleteProductPicture(@Path("channel_id")long channel_id,@Path("product_id")int product_id,@Path("photo_id")int photo_id);

    @PUT("/shops/{channel_id}/products/{product_id}/photos/{photo_id}/")
    Call<ResponseBody> updateProductPicture(@Path("channel_id")long channel_id,@Path("product_id")int product_id,@Path("photo_id")int photo_id,@Body Map<String,Object> body);


    @DELETE("/shops/{channel_id}/products/{product_id}/")
    Call<ResponseBody> deleteProductPicture(@Path("channel_id")long channel_id,@Path("product_id")int product_id);


    @DELETE("/shops/{channel_id}/contact.telphones/{phone_id}/")
    Call<ResponseBody> deletePhoneNumber(@Path("channel_id")long channel_id,@Path("phone_id")int phone_id);

    @PUT("/shops/{channel_id}/contact.telphones/{phone_id}/")
    Call<ResponseBody> updatePhoneNumber(@Path("channel_id")long channel_id,@Path("phone_id")int phone_id);


    @GET("/shops/{channel_id}/self/")
    Call<ResponseBody> getShopSelf(@Path("channel_id")long channel_id);


    @POST("/shops/{channel_id}/contact.telphones/")
    Call<ResponseBody> addPhoneNumber(@Path("channel_id")long channel_id,@Body Map<String,Object> body);


    @POST("/shops/{channel_id}/contact.telphones/")
    Call<ResponseBody> getShopPhoneNumber(@Path("channel_id")long channel_id);


    //user endpoints
    @GET("/user/")
    Call<ResponseBody> getUserSelf();


    @PUT("/user/")
    Call<ResponseBody> updateUserSelf(@Body ShopDataSerializer.User user);

    //user endpoints
    @POST("/api/token/refresh/")
    Call<ResponseBody> refreshToken(@Body String refresh_token);

    @GET("/huluproxy/proxies/")
    Call<ResponseBody> getProxies();
    @GET
    Call<ResponseBody> getProxiesFromFirebase(@Url String url);

    @GET
    Call<ResponseBody> translate(@Url String url);

}

