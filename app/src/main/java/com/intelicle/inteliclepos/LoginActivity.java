package com.intelicle.inteliclepos;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.intelicle.inteliclepos.thread.AsyncThread;
import com.intelicle.inteliclepos.thread.NetworkConnectionThread;
import com.intelicle.inteliclepos.util.CustomDialog;
import com.intelicle.inteliclepos.util.ItemsLookup;
import com.intelicle.inteliclepos.util.MyToast;
import com.intelicle.inteliclepos.util.Urls;
import com.intelicle.inteliclepos.util.UserPermission;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    EditText userName,password;
    Button signIn,serverSetting;
    TextView frogetPassword;
    CustomDialog dialog;
    String tempEmail="";
    public static Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
       requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null)
            actionBar.hide();

        //get tempEmail
        SharedPreferences preferences = getSharedPreferences("loginState",MODE_PRIVATE);
        tempEmail = preferences.getString("email","");
        //init views
        initViews();
        context = LoginActivity.this;

    }

    private void initViews(){
        //get reference of widgets
        userName = findViewById(R.id.username);userName.setText(tempEmail);
        password = findViewById(R.id.password);
        signIn = findViewById(R.id.sign_in);
        frogetPassword = findViewById(R.id.forget_password);
        serverSetting = findViewById(R.id.setting);

        //set onclick listener
        signIn.setOnClickListener(this);
        frogetPassword.setOnClickListener(this);
        serverSetting.setOnClickListener(this);
    }

    @Override
    public void onResume(){
        super.onResume();

    }

    @Override
    public void onClick(View v){
        Intent intent;

        switch (v.getId()){
            case R.id.sign_in:
                Log.v("test","before permission");
                new UserPermission().getPermission(this);
                Log.v("test","after permission");
                //init network checker object
                NetworkConnectionThread networkConnectionThread = new NetworkConnectionThread(LoginActivity.this,"login");
                //first check internet and then send server request
                if(networkConnectionThread.isNetworkAvailable()){
                    networkConnectionThread.execute();
                }else {
                    //if not available
                    new MyToast().createToast(this,"Not Connected to network");
                }
                break;
            case R.id.forget_password:

                break;
            case R.id.setting:
                intent = new Intent(LoginActivity.this,SettingList.class);
                startActivity(intent);
                break;
            default:

        }

    }

    //this method has logic of input validation, thread execution and will call from net-con class
    public void execute(){
        //show loading
        dialog = new CustomDialog(LoginActivity.this,"Checking");

        String u=userName.getText().toString().trim(),
                p=password.getText().toString().trim();
        //check server url save or not
        if (Urls.isDomainAvailable()){
            if (Patterns.EMAIL_ADDRESS.matcher(u).matches() && !p.isEmpty()){
                JSONObject json = convertToJson(u,p);
                Log.v("JSON",json.toString());
                checkPermission(json.toString());
            }else {
                //toast
                new MyToast().createToast(LoginActivity.this,"Please check email or password");
            }
        }else {
            //toast
            new MyToast().createToast(LoginActivity.this,"Please set Server url");
        }
    }

    //this method will call from thread method onpostexecute
    public void result(String result){
        Log.v("permission","result");
        Log.v("permission","result in json"+result);
        dialog.hideLoadingDialog();
        if (result!=null){
            if (result.equals("connection error:403")){
                new MyToast().createToast(LoginActivity.this,"connection error:403");
                return;
            }

            Log.v("permission","result not null");
            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONObject jsonOutput = jsonObject.optJSONObject("outputModel");
                String userId="";
                if(jsonOutput!=null){
                    userId = jsonOutput.optString("UserId");
                }else if(jsonObject.optString("Status")!=""){
                    new MyToast().createToast(LoginActivity.this,""+jsonObject.optString("Status"));
                }

                if (userId!=null &&  userId!="" ) {
                    //reset itemlookup reader value for updated result
                    ItemsLookup.reader=0;
                    //save user login state
                    SharedPreferences preferences = getSharedPreferences("loginState",MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("isLogin",true);
                    //get access token and save in urls class for authorizatoin in api
                    String token = jsonOutput.optString("AccessToken");
                    String company = String.valueOf(jsonOutput.optInt("CompanyId"));
                    String outlet = String.valueOf(jsonOutput.optInt("OutletId"));
                    Urls.setToken(token);
                    Urls.setCompanyId(company);
                    Urls.setOutletId(outlet);
                    editor.putString("AccessToken",token);
                    editor.putString("CompanyId",company);
                    editor.putString("OutletId",outlet);
                    //store user email
                    String u=userName.getText().toString().trim();
                    editor.putString("email",u);
                    AddProductActivity.companyId[1]=company;
                    AddProductActivity.outletId[1]=outlet;
                    //save preference by invoking commit method
                    editor.commit();
                    String scannerType = preferences.getString("scannerType","");
                    if (scannerType.equals("camera")){
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    }else if (scannerType.equals("external")){
                        Intent intent = new Intent(LoginActivity.this, BroadCastActivity.class);
                        startActivity(intent);
                    }else {
                        Intent intent = new Intent(LoginActivity.this, ScannerSelectionActivity.class);
                        startActivity(intent);
                    }

                    finish();
                }else {
                    Log.v("permission","result null");
                    new MyToast().createToast(LoginActivity.this,"Email or Password is Incorrect");
                   // new MyToast().createToast(LoginActivity.this,""+jsonObject.toString());
                    //Log.v("login",jsonObject.toString());
                }
            }catch (JSONException e){
                Log.v("permission","result exception"+e.getMessage());
                //CREATE TOAST
                new MyToast().createToast(LoginActivity.this,"error:"+e.getMessage());
            }
        }else{
            new MyToast().createToast(LoginActivity.this,"error: "+"Api link down or don't exist");
        }

    }

    private JSONObject convertToJson(String u,String p){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email",u);
            jsonObject.put("password",p);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private void checkPermission(String data){
        //{"id":"1"}
        if (ActivityCompat.checkSelfPermission(
                LoginActivity.this,Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED){
            Log.v("permission","yes");
            dialog.createLoadingDialog();
            AsyncThread thread = new AsyncThread(
                    Urls.Type.LOGIN,
                    LoginActivity.this,
                    data);
            thread.execute();
        }else {
            Log.v("permission","no");
            new UserPermission().getPermission(this);
//            ActivityCompat.requestPermissions(LoginActivity.this,
//                    new String[]{Manifest.permission.INTERNET},100);
        }
    }


    @Override
    protected void onActivityResult(int requestCode,int responseCode,Intent data){
        Log.v("permission","onActivityResult");
            super.onActivityResult(requestCode,responseCode,data);
        if (requestCode==100 && responseCode==RESULT_OK){
            Log.v("client","Permission granteed");
            new MyToast().createToast(LoginActivity.this,"Permission guaranteed");
        }
    }

    public class UserPermission {
        String[] permissions = new String[]{Manifest.permission.INTERNET,
                Manifest.permission.CAMERA,Manifest.permission.ACCESS_NETWORK_STATE};
        int requestCode = 100;
        public void getPermission(LoginActivity activity){
            ActivityCompat.requestPermissions(activity,permissions,requestCode);
        }

    }
}