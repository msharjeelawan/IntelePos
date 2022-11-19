package com.intelicle.inteliclepos.service;

import android.util.Log;

import com.intelicle.inteliclepos.AddProductActivity;
import com.intelicle.inteliclepos.LoginActivity;
import com.intelicle.inteliclepos.ProductDetailActivity;
import com.intelicle.inteliclepos.util.MyToast;
import com.intelicle.inteliclepos.util.Urls;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiService {

    private String url = "";
    private String data = "";
    private Urls.Type type ;
    private String method = "";
    public ApiService(String url,String data,Urls.Type type){
        this.url = url;
        this.data = data;
        this.method = Urls.getRequestMethod(type);
        this.type = type;
    }

    public String createClientAndSendRequest(){
        Log.v("client","method"+method);
        Log.v("client","url"+url);
        Log.v("client","token"+ Urls.getToken());
        if ( method.equals("PUT")){
            //new MyToast().createToast(ProductDetailActivity.update,"in api class before client creation");
        }
        //verify and create url according to type
        if (method.equals("GET")) {
            url = getUrlAccordingToRequestMethod();
            Log.v("client", "url2" + url);
        }
        String result = "";
        try{
            URL url = new URL(this.url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(15000);
            connection.setDoInput(true);
            if (method.equals("POST") || method.equals("PUT"))
                connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type","application/json");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");

            //set authorization code if incoming request not from login page
            if (!type.equals(Urls.Type.LOGIN)) {
                connection.setRequestProperty("Authorization", "Bearer " + Urls.getToken());
                Log.v("client","TOKEN");
            }
            if (method.equals("POST") || method.equals("PUT")) {

                OutputStream outputStream = connection.getOutputStream();
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");
                BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();
            }
            connection.connect();
            int response = connection.getResponseCode();
            if (response==200 || response==201){
                //Log.v("client","request response 200: "+response);
                InputStream inputStream = connection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String tempResult;
                while ((tempResult=bufferedReader.readLine())!=null){
                    result+=tempResult;
                }
                if (bufferedReader!=null)
                    bufferedReader.close();
                if (connection!=null)
                    connection.disconnect();
                return result;
            }else {
                if ( method.equals("PUT")){
                   // new MyToast().createToast(ProductDetailActivity.update,"in api class receiving error");
                }
               // Log.v("client","request response other: "+response);
               return "connection error:"+response;
            }

        }catch(IOException e){
            e.printStackTrace();
            if ( method.equals("PUT")){
               // new MyToast().createToast(ProductDetailActivity.update,"api exception: "+e.getMessage());
            }
        }
      return null;
    }

    //signal client using get,post,put method so on basis of method url will generate
    public String getUrlAccordingToRequestMethod(){
        String url="";

        if (type.equals(Urls.Type.SCAN) || type.equals(Urls.Type.SCAN2)){
            Log.v("client","urlcreationbefore"+url);
            try {
                JSONObject login = new JSONObject(data);
                String barcode = login.optString("Barcode");
                url = this.url+"?Barcode="+barcode;
                Log.v("client","urlcreationscan"+url);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }else if (type.equals(Urls.Type.SHOW)){
            Log.v("client","urlcreationshow"+url);
            try {
                JSONObject lookUp = new JSONObject(data);
                int companyId = lookUp.optInt("CompanyId");
                int outletId = lookUp.optInt("OutletId");
                url = this.url+"?CompanyId="+companyId+"&OutletId="+outletId;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.v("client","urlcreationafter"+url);
        return url;
    }

}
