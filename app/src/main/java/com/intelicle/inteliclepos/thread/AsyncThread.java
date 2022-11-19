package com.intelicle.inteliclepos.thread;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.intelicle.inteliclepos.AddProductActivity;
import com.intelicle.inteliclepos.BroadCastActivity;
import com.intelicle.inteliclepos.LoginActivity;
import com.intelicle.inteliclepos.MainActivity;
import com.intelicle.inteliclepos.ProductDetailActivity;
import com.intelicle.inteliclepos.service.ApiService;
import com.intelicle.inteliclepos.util.ItemsLookup;
import com.intelicle.inteliclepos.util.Urls;

public class AsyncThread extends AsyncTask<Void,Void,String> {

    private Urls.Type url;
    String data;
    private Context context;
    //constructor
    public AsyncThread(Urls.Type url, Context context, String data){
        this.url = url;
        this.data = data;
        this.context = context;
    }

    @Override
    protected void onPreExecute(){
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Void... parms){
        //get url  through url type and instantiate service...
        String url = Urls.getUrl(this.url);
        ApiService service = new ApiService(url,data,this.url);
        String result = service.createClientAndSendRequest();
        return result;
    }

    @Override
    protected void onPostExecute(String result){
        super.onPostExecute(result);

        if (url== Urls.Type.LOGIN){
            ((LoginActivity)context).result(result);
            //Log.v("client","result of api call on post execute: login"+result);
        }else if (url==Urls.Type.UPDATE){
            ((ProductDetailActivity) context).result(result);
            //Log.v("client","result of api call on post execute: update "+result);
        }else if (url==Urls.Type.ADD){
            ((AddProductActivity)context).result(result);
            //Log.v("client","result of api call on post execute: add"+result);
        }else if (url==Urls.Type.SHOW){
            ItemsLookup.resultCategory(result);
           // Log.v("client","result of api call on post execute: show"+result);
        }else if (url==Urls.Type.SCAN){
            ((MainActivity)context).result(result);
           // Log.v("client","result of api call on post execute: scan"+result);
        }else if (url==Urls.Type.SCAN2){
            ((BroadCastActivity)context).result(result);
            // Log.v("client","result of api call on post execute: scan"+result);
        }

//        Intent intent = new Intent(context, AddProductActivity.class);
//        intent.putExtra("data",result);
//        context.startActivity(intent);
        //Toast.makeText(context,"post execute"+result+Urls.getUrl(this.url),Toast.LENGTH_LONG).show();

    }
}
