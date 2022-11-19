package com.intelicle.inteliclepos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.intelicle.inteliclepos.model.ProductUpdate;
import com.intelicle.inteliclepos.model.TaxId;
import com.intelicle.inteliclepos.thread.AsyncThread;
import com.intelicle.inteliclepos.thread.NetworkConnectionThread;
import com.intelicle.inteliclepos.util.CustomDialog;
import com.intelicle.inteliclepos.util.ItemsLookup;
import com.intelicle.inteliclepos.util.MyToast;
import com.intelicle.inteliclepos.util.Urls;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private SurfaceView surfaceView;
    private BarcodeDetector detector;
    private CameraSource cameraSource;
    private int CAMERA_PERMISSION = 122;
    int count = 0;
    String barcodeValue = "";
    CustomDialog customDialog;
    int scanCount = 0;
    Button reScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //init views
        initWidgets();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_bar,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.reScan:
                cameraSource.stop();
                startCamera();
                break;
            case R.id.log_out:
                getSharedPreferences("loginState",MODE_PRIVATE)
                        .edit().putBoolean("isLogin",false).commit();
                startActivity(new Intent(MainActivity.this,LoginActivity.class));
                break;
            case R.id.settings:
                startActivity(new Intent(MainActivity.this,ScannerSelectionActivity.class));
                finish();
                break;
            default:
        }
        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();
        //it will load category, subcategory tax and unit of sale value
        ItemsLookup.load(MainActivity.this);

        Log.v("detector", "on resume");
        initBarcodeDetector();
        if (scanCount > 0) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    cameraSource.stop();
                    startCamera();
                }
            },1000);

        }
        scanCount++;
    }

    @Override
    protected void onStop() {
        super.onStop();
        //cameraSource.release();
        cameraSource.stop();
        //reset to zero for another scan
        count = 0;
    }

    private void initWidgets() {
        surfaceView = findViewById(R.id.surfaceView);
    }

    private void initBarcodeDetector(){

        detector = new BarcodeDetector.Builder(this).
                setBarcodeFormats(Barcode.ALL_FORMATS).
                build();
        cameraSource = new CameraSource.Builder(this,detector).setRequestedPreviewSize(1920,1080).setAutoFocusEnabled(true).build();


        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback(){
            @Override
            public void surfaceCreated(SurfaceHolder holder){
                Log.v("detector","surface created");
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                    startCamera();

                }else {
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CAMERA},CAMERA_PERMISSION);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder,int format,int width,int height){
                Log.v("detector","changed");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder){
                //cameraSource.release();
                Log.v("detector","destroy");
            }

        });

        detector.setProcessor(new Detector.Processor<Barcode>(){
            @Override
            public void release(){

            }

            @Override
            public  void receiveDetections(Detector.Detections detections){
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size()!=0 && count==0){
                    surfaceView.post(new Runnable() {
                        @Override
                        public void run() {
                            //for one time scanning,  do increment of count
                            count++;
                            //create dialog
                            customDialog = new CustomDialog(MainActivity.this,"Searching");
                            customDialog.createLoadingDialog();

                            barcodeValue = barcodes.valueAt(0).displayValue;

                            Log.v("scanning","test"+barcodeValue);
                            //if every input is okay then before sending request first
                            NetworkConnectionThread networkConnectionThread = new NetworkConnectionThread(MainActivity.this,"search");
                            if(networkConnectionThread.isNetworkAvailable()){
                                //networkConnectionThread.execute();
                            }else {
                                customDialog.hideLoadingDialog();
                                //if not available
                                new MyToast().createToast(MainActivity.this,"Not Connected to network");
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        count=0;
                                    }
                                },2000);

                            }

                           // Toast.makeText(MainActivity.this, barcodeValue, Toast.LENGTH_LONG).show();
//                            Intent intent =  new Intent(MainActivity.this,ProductDetailActivity.class);
//                            intent.putExtra("barcode",barcodeValue);
//                            startActivity(intent);
                        }
                    });
                }
            }
        });
    }

    public void startCamera(){
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {
            cameraSource.start(surfaceView.getHolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void execute(){
        //convert to json
        JSONObject json = convertToJson(barcodeValue);
        //Log.v("JSON",json.toString());

        //create thread
        AsyncThread thread = new AsyncThread(Urls.Type.SCAN,MainActivity.this,json.toString());
        thread.execute();
    }
    private JSONObject convertToJson(String data){
        Log.v("barcode","mm"+data);
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Barcode",data);
            return jsonObject;
        }catch (JSONException e){
            //toast
            new MyToast().createToast(MainActivity.this,"json error:"+e.getMessage());
            //reset count to zero if error occur and ready for next scanning
            count=0;
        }
        return  null;
    }

    //this method will call from thread on post execute method
    public void result(String data){
        Log.v("client","scanresult"+data);
        customDialog.hideLoadingDialog();
        if (data!=null){
            if (data.equals("connection error:401")){
                new MyToast().createToast(MainActivity.this,"Token Expire Please login again");
                SharedPreferences preferences = getSharedPreferences("loginState",MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("isLogin",false);
                editor.apply();
                startActivity(new Intent(MainActivity.this,LoginActivity.class));
                finish();
                return;
            }
            if (data.equals("connection error:403")){
                new MyToast().createToast(MainActivity.this,"connection error:403");
                return;
            }
            try {
                JSONObject jsonObject = new JSONObject(data);
                if (jsonObject.getString("Message").equals("Items search successfully")){
                    //product found so open product edit page
                    //inner json object
                    JSONObject innerJson = jsonObject.getJSONObject("ActionData");
                    Intent intent = new Intent(MainActivity.this,ProductDetailActivity.class);
                    intent.putExtra("id",""+innerJson.optInt("Id"));
                    intent.putExtra("cat",""+innerJson.optInt("CategoryId"));
                    intent.putExtra("sub",""+innerJson.optInt("SubCategoryId"));
                    intent.putExtra("barcode",innerJson.optString("ItemBarCode"));
                    intent.putExtra("name",innerJson.optString("ItemName"));//
                    intent.putExtra("description",innerJson.optString("Description"));
                    intent.putExtra("qty",innerJson.optInt("Quantity"));
                    intent.putExtra("showontill",innerJson.optBoolean("IsShowOnTill"));
                    String webPrice="",itemSalesPrice="";
                    if(innerJson.optDouble("WebPrices")>=0){
                        webPrice = String.valueOf(innerJson.optDouble("WebPrices"));
                    }
                    if(innerJson.optDouble("ItemSalesPrice")>=0){
                        itemSalesPrice = String.valueOf(innerJson.optDouble("ItemSalesPrice"));
                    }
                    intent.putExtra("webprice",webPrice);
                    intent.putExtra("price",itemSalesPrice);
                    startActivity(intent);
                }else {
                    new MyToast().createToast(MainActivity.this,"product not found");
                    //product not found so first hide loading dialog then show confirm dialog for
                    //new entry
                    customDialog.createConfirmDialog();
                }
            }catch (JSONException e){
                //toast
                new MyToast().createToast(MainActivity.this,"json errors:"+e.getMessage());
                //reset count to zero if error occur and ready for next scanning
                count=0;
            }
        }else{
            count=0;
            new MyToast().createToast(MainActivity.this,"error: "+"Api link down or don't exist");
        }
    }

    public void createIntent(){
        Intent intent = new Intent(MainActivity.this,AddProductActivity.class);
        intent.putExtra("barcode",barcodeValue);
        startActivity(intent);
    }

    public void resetCount(){
        customDialog.hideLoadingDialog();
        count=0;
    }

}