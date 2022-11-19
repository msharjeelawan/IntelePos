package com.intelicle.inteliclepos;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Window;
import android.view.WindowManager;

import com.intelicle.inteliclepos.util.MyToast;
import com.intelicle.inteliclepos.util.Urls;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //check shared preference for domain
        SharedPreferences preferences = getSharedPreferences("server_url",MODE_PRIVATE);
        String url = preferences.getString("url","");
        if (url.length()>0){
            Urls.setDomain(url);
            //new MyToast().createToast(SplashScreen.this,url);
        }

        //intent
        createIntent();
    }

   private void createIntent(){

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent;
                //check user is already login or not
                SharedPreferences preferences = getSharedPreferences("loginState",MODE_PRIVATE);
                Urls.setToken(preferences.getString("AccessToken",""));
                String companyId = preferences.getString("CompanyId","");
                String outletId = preferences.getString("OutletId","");
                Urls.setCompanyId(companyId);
                Urls.setOutletId(outletId);
                AddProductActivity.companyId[1]=companyId;
                AddProductActivity.outletId[1]=outletId;
                boolean isLogin = preferences.getBoolean("isLogin",false);
                String scannerType = preferences.getString("scannerType","");
                if (isLogin) {
                    if (scannerType.equals("camera")){
                        intent = new Intent(SplashScreen.this, MainActivity.class);
                    }else if (scannerType.equals("external")){
                        intent = new Intent(SplashScreen.this, BroadCastActivity.class);
                    }else {
                        intent = new Intent(SplashScreen.this, ScannerSelectionActivity.class);
                    }

                }else{
                    intent = new Intent(SplashScreen.this, LoginActivity.class);
                }
                startActivity(intent);
                finish();
            }
        },4000);
    }
}