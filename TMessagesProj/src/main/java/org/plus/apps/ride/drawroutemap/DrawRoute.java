package org.plus.apps.ride.drawroutemap;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;

import org.plus.apps.business.data.ShopDataController;
import org.plus.apps.ride.RideRequest;
import org.plus.net.RequestManager;
import org.telegram.messenger.UserConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Created by ocittwo on 11/14/16.
 *
 * @Author Ahmad Rosid
 * @Email ocittwo@gmail.com
 * @Github https://github.com/ar-android
 * @Web http://ahmadrosid.com
 */

public class DrawRoute extends AsyncTask<String, Void, String> {

    private GoogleMap mMap;

    public DrawRoute(GoogleMap mMap) {
        this.mMap = mMap;
    }

    @Override
    protected String doInBackground(String... url) {
        String data = "";
        try {
            data = getJsonRoutePoint(url[0]);
            Log.d("Background Task data", data);
        } catch (Exception e) {
            Log.d("Background Task", e.toString());
        }
        return data;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        RouteDrawerTask routeDrawerTask = new RouteDrawerTask(mMap);
        routeDrawerTask.execute(result);
    }

    private String getJsonRoutePoint(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
          Response<ResponseBody> responseBody =  RequestManager.getInstance(UserConfig.selectedAccount).getRideRequest().getRoute(strUrl).execute();
          if(responseBody.isSuccessful()){
              data = responseBody.body().string();
          }
//            URL url = new URL(strUrl);
//
//            // Creating an http connection to communicate with url
//            urlConnection = (HttpURLConnection) url.openConnection();
//
//            // Connecting to url
//            urlConnection.connect();
//
//            // Reading data from url
//            iStream = urlConnection.getInputStream();
//
//            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
//
//            StringBuffer sb = new StringBuffer();
//
//            String line = "";
//            while ((line = br.readLine()) != null) {
//                sb.append(line);
//            }
//
//            data = sb.toString();
//            Log.d("getJsonRoutePoint", data.toString());
//            br.close();

        } catch (Exception e) {
            Log.d("trymetryme", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

}
