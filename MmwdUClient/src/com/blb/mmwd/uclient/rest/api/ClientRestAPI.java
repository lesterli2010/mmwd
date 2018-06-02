package com.blb.mmwd.uclient.rest.api;

import java.util.List;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

import com.blb.mmwd.uclient.rest.model.OrderSubmitFoods;
import com.blb.mmwd.uclient.rest.model.ShippingAddress;
import com.blb.mmwd.uclient.rest.model.response.Addresses;
import com.blb.mmwd.uclient.rest.model.response.Cities;
import com.blb.mmwd.uclient.rest.model.response.Comments;
import com.blb.mmwd.uclient.rest.model.response.Communities;
import com.blb.mmwd.uclient.rest.model.response.Districts;
import com.blb.mmwd.uclient.rest.model.response.FavoriteMmShops;
import com.blb.mmwd.uclient.rest.model.response.Food;
import com.blb.mmwd.uclient.rest.model.response.FoodPriceRestInfos;
import com.blb.mmwd.uclient.rest.model.response.Foods;
import com.blb.mmwd.uclient.rest.model.response.Login;
import com.blb.mmwd.uclient.rest.model.response.MmShopDetails;
import com.blb.mmwd.uclient.rest.model.response.MmShops;
import com.blb.mmwd.uclient.rest.model.response.Order;
import com.blb.mmwd.uclient.rest.model.response.OrderSubmitResult;
import com.blb.mmwd.uclient.rest.model.response.Orders;
import com.blb.mmwd.uclient.rest.model.response.Provinces;
import com.blb.mmwd.uclient.rest.model.response.ResponseHead;
import com.blb.mmwd.uclient.rest.model.response.ResponseIntValue;
import com.blb.mmwd.uclient.rest.model.response.ResponseNVValue;
import com.blb.mmwd.uclient.rest.model.response.ResponseStrValue;
import com.blb.mmwd.uclient.rest.model.response.UpgradeInfo;
import com.blb.mmwd.uclient.rest.model.response.Zones;

/**
 * Define all rest API with server here Use open source Retrofit for http access
 * http://square.github.io/retrofit/
 * 
 * @author lizhiqiang3
 * 
 */
public interface ClientRestAPI {

    // Common interfaces
    @GET("/app/common/plist")
    void getProviceList(Callback<Provinces> callback);

    @GET("/app/common/hot/clist")
    void getHotCityList(Callback<Cities> callback);

    @GET("/app/common/{pid}/clist")
    void getCityList(@Path("pid") int pid, Callback<Cities> callback);

    @GET("/app/common/{pid}/clist")
    Cities getCityList(@Path("pid") int pid);

    @GET("/app/common/{cid}/dlist")
    void getDistrictList(@Path("cid") int cid, Callback<Districts> callback);

    // get nearby zones 获取附近的妈妈圈
    @GET("/app/common/qlist/{lng}/{lat}")
    void getZoneList(@Path("lng") double lng, @Path("lat") double lat,
            Callback<Zones> callback);
    
    @GET("/app/common/q2d/{qid}")
    void getDistrictName(@Path("qid") int zid, Callback<ResponseStrValue> callback);

    // get zones by districtId, 根据区县选择
    @GET("/app/common/{did}/qlist")
    void getZoneList(@Path("did") int did, Callback<Zones> callback);

    @GET("/app/common/{qid}/comlist")
    void getCommunityList(@Path("qid") int zid, Callback<Communities> callback);

    @GET("/app/common/supportcall")
    void getServiceNumber(Callback<ResponseStrValue> callback);
    
    @GET("/app/common/ranktormb")
    void getScoreExchangeRate(Callback<ResponseIntValue> callback);
    // Data interface
    @GET("/app/common/normal2cross/{qid}")
    void getCrossAreaByZoneId(@Path("qid") int zid, Callback<ResponseNVValue> callback);

    @GET("/app/common/cross/{qid}/dlist")
    void getCrossAreaDistrictList(@Path("qid") int crossAreaId, Callback<Districts> callback);
    
    // get shipping fee
    @GET("/app/common/distance/fee")
    void getShippingFee(@Query("mmId") int mmId, @Query("endAddr") String endAddr, Callback<ResponseIntValue> callback);
    
    // ------home page
    // get mm shop list
    @GET("/app/index/hot/mmlist/{mmqid}/{page}/{limit}")
    void getMmShopList(@Path("mmqid") int zid, @Path("page") int page,
            @Path("limit") int limit, Callback<MmShops> callback);

    // get hot food list
    @GET("/app/index/hot/cailist/{mmqid}/{page}/{limit}")
    void getHotFoodList(@Path("mmqid") int zid, @Path("page") int page,
            @Path("limit") int limit, Callback<Foods> callback);

    // ------Account
    // username+password login
    @POST("/app/login/login/username")
    void userLoginUserName(@Query("username") String username,
            @Query("password") String password, Callback<Login> callback);

    // mobile+vcode login
    @POST("/app/login/login/mobile")
    void userLoginMobile(@Query("mobile") String username,
            @Query("code") String password, Callback<Login> callback);

    @POST("/app/login/logout")
    void userLogout(@Header("session") String session,
            Callback<ResponseHead> callback);

    @POST("/app/account/password/change")
    void changePassword(@Header("session") String session,
            @Query("oldpassword") String oldPassword,
            @Query("password") String newPassword,
            Callback<ResponseHead> callback);
    
    @POST("/app/account/username/change")
    void changeUserName(@Header("session") String session,
            @Query("username")String userName, Callback<ResponseHead> callback);
    
    @GET("/app/account/vcode/request/{mobile}/{type}")
    void requestVerifyCode(@Path("mobile") String mobile,
            @Path("type") int type, Callback<ResponseHead> callback);

    @POST("/app/account/mobile/reset")
    void changeMobile(@Header("session") String session,
            @Query("mobile") String newMobile, @Query("code") String vCode,
            Callback<ResponseHead> callback);

    // ------ Shipping address
    @GET("/app/myaddr/list")
    void getShippingAddressList(@Header("session") String session,
            Callback<Addresses> callback);

    @GET("/app/myaddr/list/{qid}")
    void getShippingAddressList(@Header("session") String session,
            @Path("qid") int zid, Callback<Addresses> callback);

    @PUT("/app/myaddr/add")
    void addShippingAddress(@Header("session") String session,
            @Body ShippingAddress address,
            Callback<ResponseIntValue> callback);

    @DELETE("/app/myaddr/delete/{addrid}")
    void delShippingAddress(@Header("session") String session,
            @Path("addrid") int addrid, Callback<ResponseHead> callback);

    @POST("/app/myaddr/modify")
    void modifyShippingAddress(@Header("session") String session,
            @Body ShippingAddress address, Callback<ResponseHead> callback);

    // ------- favorite mmshop
    @GET("/app/myfav/list")
    void getFavoriteMmShopList(@Header("session") String session,
            Callback<FavoriteMmShops> callback);

    @PUT("/app/myfav/add")
    void addFavoriteMmShop(@Header("session") String session,
            @Query("mmid") int mmid, Callback<ResponseHead> callback);

    @DELETE("/app/myfav/delete/{mmid}")
    void delFavoriteMmShop(@Header("session") String session,
            @Path("mmid") int mmid, Callback<ResponseHead> callback);

    // ------- MM details
    @GET("/app/mm/{mmid}/base")
    void getMmShopDetailBase(@Header("session") String session,
            @Path("mmid") int mmid, Callback<MmShopDetails> callback);

    @GET("/app/mm/{mmid}/cailist/{ison}")
    void getMmShopDetailFoods(@Path("mmid") int mmid,
            @Path("ison") boolean ison, Callback<Foods> callback);

    // ------- Order
    @POST("/app/order/submit")
    void submitOrder(@Header("session") String session, @Query("payType")int payType,
            @Query("address") String address,
            @Query("receiver") String receiver, @Query("phone") String phone,
            @Query("isConsumeRank") boolean isConsumeScore, @Query("consumeRank") int usedScore,
            @Body OrderSubmitFoods foods, Callback<OrderSubmitResult> callback);

    @GET("/app/my/order/list/{page}/{limit}")
    void getOrderList(@Header("session") String session,
            @Path("page") int page, @Path("limit") int limit,
            Callback<Orders> callback);
    
    @GET("/app/my/order/list/unfinish/{page}/{limit}")
    void getUnfinishedOrderList(@Header("session") String session,
            @Path("page") int page, @Path("limit") int limit,
            Callback<Orders> callback);
    
    @GET("/app/order/{oid}")
    void getOrderDetail(@Header("session") String session, @Path("oid")int id, 
            Callback<Order> callback);

    // ------ food zan and wanted
    @GET("/app/product/{id}/wanted")
    void addFoodWanted(@Header("session") String session,
            @Path("id") int foodId, Callback<ResponseIntValue> callback);

    @GET("/app/product/{id}/zan")
    void addFoodZan(@Header("session") String session, @Path("id") int foodId,
            Callback<ResponseIntValue> callback);

    @GET("/app/product/{id}/view")
    void getFoodDetail(@Path("id") int foodId, Callback<Food> callback);

    @POST("/app/product/batinfo")
    void getFoodPriceRestList(@Body OrderSubmitFoods foods,
            Callback<FoodPriceRestInfos> callback);

    @GET("/app/self/mmlist/{mmqid}")
    void getSelfMmShops(@Path("mmqid") int zid, Callback<MmShops> callback);

    // ------ comment
    @PUT("/app/myrecomm/add")
    void addComment(@Header("session") String session, @Body String comment,
            Callback<ResponseHead> callback);

    @GET("/app/myrecomm/list/{page}/{limit}")
    void getCommentList(@Header("session") String session,
            @Path("page") int page, @Path("limit") int limit,
            Callback<Comments> callback);
    
    @DELETE("/app/myrecomm/delete/{id}")
    void delComment(@Header("session") String session, @Path("id") int comId, Callback<ResponseHead> callback);
    // -- upgrade

    @GET("/app/self/check/{type}")
    void checkUpgrade(@Path("type") String type, Callback<UpgradeInfo> callback);
}
