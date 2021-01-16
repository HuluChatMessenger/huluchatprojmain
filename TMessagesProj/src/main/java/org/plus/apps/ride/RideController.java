package org.plus.apps.ride;

import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;
import org.plus.apps.business.data.ShopDataController;
import org.plus.apps.ride.drawroutemap.DataRouteParser;
import org.plus.apps.ride.drawroutemap.RouteDrawerTask;
import org.plus.experment.PlusBuildVars;
import org.plus.net.APIError;
import org.plus.net.ErrorUtils;
import org.telegram.messenger.BaseController;
import org.telegram.messenger.DispatchQueue;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.UserConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class RideController extends BaseController {

    private volatile SparseArray<Call> callSparseArray = new SparseArray<>();
    private AtomicInteger lastRequestToken = new AtomicInteger(1);

    public static RideController[] instance = new RideController[UserConfig.MAX_ACCOUNT_COUNT];

    public static DispatchQueue rideQueue = new DispatchQueue("RideQueue");


    public static RideController getInstance(int num) {
        RideController localInstance = instance[num];
        if (localInstance == null) {
            synchronized (RideController.class) {
                localInstance = instance[num];
                if (localInstance == null) {
                    instance[num] = localInstance = new RideController(num);
                }
            }
        }
        return localInstance;
    }

    public RideController(int num) {
        super(num);
    }

    public interface ResponseDelegate{
        void run(Object response, APIError error);
    }

    public void cancelRequest(int reqId){
        Call call =  callSparseArray.get(reqId);
        if(call != null && !call.isCanceled()){
            call.cancel();
        }
    }

    public void createOrder(RideData.Car car){
        rideQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                
            }
        },3000);
        
    }


    public void getCars(ResponseDelegate responseDelegate){
        rideQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                ArrayList<RideData.Car> cars = new ArrayList<>();
                RideData.Car car = new RideData.Car();
                car.image = "https://png.pngtree.com/png-clipart/20200225/original/pngtree-mini-car-icon-vector-free-png-download-png-image_5310161.jpg";
                car.id = 1;
                car.price = 123;
                car.title = "Economy";
                cars.add(car);


                car = new RideData.Car();
                car.image = "https://png.pngtree.com/png-clipart/20190614/original/pngtree-vector-sports-car-icon-png-image_3783136.jpg";
                car.id = 2;
                car.price = 100;
                car.title = "Minvann";
                cars.add(car);

                car = new RideData.Car();
                car.image = "https://png.pngtree.com/png-clipart/20190904/original/pngtree-yellow-simulation-luxury-sports-car-illustration-png-image_4484884.jpg";
                car.id = 3;
                car.price = 90;
                car.title = "Minibus";
                cars.add(car);


                car = new RideData.Car();
                car.image = "https://png.pngtree.com/png-clipart/20190904/original/pngtree-simulation-black-suv-off-road-car-icon-png-image_4484871.jpg";
                car.id = 4;
                car.price = 200;
                car.title = "Corporate";
                cars.add(car);

                car = new RideData.Car();
                car.image = "https://png.pngtree.com/png-clipart/20200225/original/pngtree-car-icon-vector-travel-design-png-image_5307681.jpg";
                car.id = 5;
                car.price = 50;
                car.title = "Lada";
                cars.add(car);

                responseDelegate.run(cars,null);


            }
        },1000);
    }




    public void getDirection(LatLng origin,LatLng dest,ResponseDelegate responseDelegate){
        rideQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
                    String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
                    String sensor = "sensor=false";
                    String mode = "mode=driving";
                    String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode + "&key=" + PlusBuildVars.GOOGLE_MAP_API;
                    String url =  "https://maps.googleapis.com/maps/api/directions/json"  + "?" + parameters;
                    Response<ResponseBody> response =  getRequestManager().getRideRequest().getRoute(url).execute();
                    JSONObject jObject;
                    List<List<HashMap<String, String>>>  routes = null;
                    if(response.isSuccessful()){
                        jObject = new JSONObject(response.body().string());
                        DataRouteParser parser = new DataRouteParser();
                        routes = parser.parse(jObject);
                        if(responseDelegate != null){
                            responseDelegate.run(routes,null);
                        }
                    }else{
                        if(responseDelegate != null){
                            responseDelegate.run(null,ErrorUtils.createError(response.errorBody(),response.code()));
                        }
                    }
                }catch (Exception exception){
                    FileLog.debug(exception.getMessage());
                    if(responseDelegate != null){
                        responseDelegate.run(null,ErrorUtils.createNetworkError());
                    }
                }

            }
        });
    }


}
