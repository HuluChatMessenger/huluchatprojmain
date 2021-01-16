package org.plus.features.transalte;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.plus.features.PlusConfig;
import org.telegram.messenger.ApplicationLoader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TranslatorApi {

    private static int totalEvents = 1;

    public static int didReceiveTranslatedText = totalEvents++;


    private static String TRANSLATE_URL = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl={**}&dt=t&q=";


    public interface ApiCallBack{
        void didReceiveData(int type, Object... object);
        void onError(String error_message);
    }

    private static TranslatorApi instance;
    private Gson gson;

    private TranslatorApi() {
        gson = new GsonBuilder().create();
    }

    public static TranslatorApi getInstance() {
        TranslatorApi localInstance = instance;
        if (localInstance == null) {
            synchronized (TranslatorApi.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new TranslatorApi();
                }
            }
        }
        return localInstance;
    }


    @SuppressLint("StaticFieldLeak")
    public void getTranslatedText(String text,final ApiCallBack callBack){

        String finalUrl = TRANSLATE_URL.replace("{**}", PlusConfig.default_translate_lang) + Uri.encode(text,"utf-8");
        Log.i("fianlrl",finalUrl);

        new AsyncTask<String, Void, String>() {
            private String callUrlAndParseResult(String url) throws Exception
            {
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestProperty("User-Agent", "Mozilla/5.0");
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream(),"UTF-8"));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return parseResult(response.toString());
            }
            private String parseResult(String inputJson) throws Exception
            {
                StringBuilder builder1=new StringBuilder();
                JSONArray jsonArray = new JSONArray(inputJson);
                JSONArray jsonArray2 = (JSONArray) jsonArray.get(0);
                int length=jsonArray2.length();
                for(int i=0;i<length;i++){
                    builder1.append(jsonArray2.getJSONArray(i).get(0).toString());
                }
                return builder1.toString();
            }

            @Override
            protected String doInBackground(String... strings) {

                try {
                    return  callUrlAndParseResult(strings[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return "";
            }

            @Override
            protected void onPostExecute(String s) {
                if(s==null || s.isEmpty()){

                    return;
                }
                callBack.didReceiveData(didReceiveTranslatedText, s);
            }
        }.execute(finalUrl);
    }

    @SuppressLint("StaticFieldLeak")
    public void mTgetTranslatedText(String text,final ApiCallBack callBack){

        String finalUrl = TRANSLATE_URL.replace("{**}", PlusConfig.mt_translate_lang) + Uri.encode(text,"utf-8");
        Log.i("fianlrl",finalUrl);

        new AsyncTask<String, Void, String>() {
            private String callUrlAndParseResult(String url) throws Exception
            {
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestProperty("User-Agent", "Mozilla/5.0");
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream(),"UTF-8"));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return parseResult(response.toString());
            }
            private String parseResult(String inputJson) throws Exception
            {
                StringBuilder builder1=new StringBuilder();
                JSONArray jsonArray = new JSONArray(inputJson);
                JSONArray jsonArray2 = (JSONArray) jsonArray.get(0);
                int length=jsonArray2.length();
                for(int i=0;i<length;i++){
                    builder1.append(jsonArray2.getJSONArray(i).get(0).toString());
                }
                return builder1.toString();
            }

            @Override
            protected String doInBackground(String... strings) {

                try {
                    return  callUrlAndParseResult(strings[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return "";
            }

            @Override
            protected void onPostExecute(String s) {
                if(s==null || s.isEmpty()){

                    return;
                }
                callBack.didReceiveData(didReceiveTranslatedText,s);
            }
        }.execute(finalUrl);
    }


}
