package com.intelicle.inteliclepos.thread;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.intelicle.inteliclepos.AddProductActivity;
import com.intelicle.inteliclepos.BroadCastActivity;
import com.intelicle.inteliclepos.LoginActivity;
import com.intelicle.inteliclepos.MainActivity;
import com.intelicle.inteliclepos.ProductDetailActivity;
import com.intelicle.inteliclepos.util.MyToast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

public class NetworkConnectionThread extends AsyncTask<Void,Void,Boolean> {

    private Context context;
    private String type;
    public NetworkConnectionThread(Context context,String type){
        this.context = context;
        this.type = type;
    }

    //this will check is connect to network either internet is available or not
    public boolean isNetworkAvailable(){
       ConnectivityManager manager =  (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info!=null && info.isConnected()){
            return true;
        }
        return false;
    }

    @Override
    protected Boolean doInBackground(Void... params){
        try{
            int timeOut = 1500;
            Socket socket = new Socket();
            SocketAddress address = new InetSocketAddress("8.8.8.8",53);
            socket.connect(address,timeOut);
            socket.close();

            return true;
        }catch (IOException e){
                e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result){
        //here result is boolean object so call booleanvalue() for getting result value
        if (result.booleanValue()){
            if (type.equals("login")){
                ((LoginActivity)context).execute();
            }else if (type.equals("search")){
                ((MainActivity)context).execute();
            }else if (type.equals("add")){
                ((AddProductActivity)context).execute();
            }else if (type.equals("update")){
                ((ProductDetailActivity)context).execute();
            }else if (type.equals("searchExternal")){
                ((BroadCastActivity)context).execute();
            }
        }else{
            if (type.equals("search")){
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ((MainActivity)context).resetCount();
                    }
                },1000);

            }
            new MyToast().createToast(context,"No Internet Access");
        }
    }

}
