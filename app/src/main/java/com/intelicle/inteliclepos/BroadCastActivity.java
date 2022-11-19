package com.intelicle.inteliclepos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;

import com.intelicle.inteliclepos.databinding.ScannerMainBinding;
import com.intelicle.inteliclepos.thread.AsyncThread;
import com.intelicle.inteliclepos.thread.NetworkConnectionThread;
import com.intelicle.inteliclepos.util.CustomDialog;
import com.intelicle.inteliclepos.util.ItemsLookup;
import com.intelicle.inteliclepos.util.MyToast;
import com.intelicle.inteliclepos.util.Urls;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;

public class BroadCastActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private ScannerMainBinding binding;
    private ScanReader mScanner;
    private final StringBuffer stringBuffer = new StringBuffer();
    private int receiveCount;
    private Timer timer;
    CustomDialog customDialog;
    private String barcodeValue="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.scanner_main);
        binding.btScan.setOnClickListener(this);
        binding.btScan.setEnabled(true);
        binding.btClear.setOnClickListener(this);
       // binding.swContinueScan.setOnCheckedChangeListener(this);
        //binding.swContinueScan.setEnabled(true);
        binding.scanResult.setMovementMethod(ScrollingMovementMethod.getInstance());

        IntentFilter filter = new IntentFilter();
        filter.addAction(ScanReader.ACTION_SCAN_RESULT);
        registerReceiver(resultReceiver, filter);

        mScanner = new ScanReader(getApplicationContext());
        mScanner.init();
        customDialog = new CustomDialog(BroadCastActivity.this,"Searching");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.log_out,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.log_out:
                getSharedPreferences("loginState",MODE_PRIVATE)
                        .edit().putBoolean("isLogin",false).commit();
                startActivity(new Intent(BroadCastActivity.this,LoginActivity.class));
//                barcodeValue = "1234444446641";
//                customDialog.createLoadingDialog();
//                //check network connection and send request
//                NetworkConnectionThread networkConnectionThread = new NetworkConnectionThread(BroadCastActivity.this,"searchExternal");
//                if(networkConnectionThread.isNetworkAvailable()){
//                    networkConnectionThread.execute();
//                }else {
//                    customDialog.hideLoadingDialog();
//                    //if not available
//                    new MyToast().createToast(BroadCastActivity.this,"Not Connected to network");
//                }
                break;
            case R.id.settings:
                startActivity(new Intent(BroadCastActivity.this,ScannerSelectionActivity.class));
                finish();
                break;
            default:
        }
        return true;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        mScanner.stopScan();
        //mScanner.closeScan();
        unregisterReceiver(resultReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //it will load category, subcategory tax and unit of sale value
        ItemsLookup.load(BroadCastActivity.this);

    }
    private final BroadcastReceiver resultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                byte[] barcodeArray = intent.getByteArrayExtra(ScanReader.SCAN_RESULT);
                String barcode = new String(barcodeArray, "GBK");
               // stringBuffer.append(barcode);
               // stringBuffer.append("\n");

                binding.scanResult.setText(barcode);
                barcodeValue = barcode;
                if (barcode.length()>0){
                    customDialog.createLoadingDialog();
                    //check network connection and send request
                    NetworkConnectionThread networkConnectionThread = new NetworkConnectionThread(BroadCastActivity.this,"searchExternal");
                    if(networkConnectionThread.isNetworkAvailable()){
                        networkConnectionThread.execute();
                    }else {
                        customDialog.hideLoadingDialog();
                        //if not available
                        new MyToast().createToast(BroadCastActivity.this,"Not Connected to network");
                    }

                }
                // binding.scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                receiveCount++;
                if (receiveCount == 200) {
                    stringBuffer.delete(0, stringBuffer.length());
                    receiveCount = 0;
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.bt_scan) {
            mScanner.startScan();
        } else if (view.getId() == R.id.bt_clear) {
            stringBuffer.delete(0, stringBuffer.length());
            binding.scanResult.setText("");
            receiveCount = 0;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b) {
            if (timer == null) {
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        mScanner.startScan();
                    }
                }, 10, 300);
            }
        } else {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
        }
    }


    public static class ScanReader {

        public static final String ACTION_SCAN_RESULT = "com.action.SCAN_RESULT";

        public static final String SCAN_RESULT = "scanContext";
        public static final String ACTION_START_SCAN = "com.action.START_SCAN";
        public static final String ACTION_STOP_SCAN = "com.action.STOP_SCAN";
        public static final String ACTION_INIT = "com.action.INIT_SCAN";
        public static final String ACTION_KILL = "com.action.KILL_SCAN";

        private final Context context;

        public ScanReader(Context context) {
            this.context = context;
        }

        public void init() {
            Intent intent = new Intent();
            intent.setAction(ACTION_INIT);
            context.sendBroadcast(intent);
        }

        public void closeScan() {
            Intent intent = new Intent();
            intent.setAction(ACTION_KILL);
            context.sendBroadcast(intent);
        }

        public void startScan() {
            Intent intent = new Intent();
            intent.setAction(ACTION_START_SCAN);
            context.sendBroadcast(intent);
        }

        public void stopScan() {
            Intent intent = new Intent();
            intent.setAction(ACTION_STOP_SCAN);
            context.sendBroadcast(intent);
        }
    }

    public void execute(){
        //convert to json
        JSONObject json = convertToJson(barcodeValue);
        //Log.v("JSON",json.toString());

        //create thread
        AsyncThread thread = new AsyncThread(Urls.Type.SCAN2,BroadCastActivity.this,json.toString());
        thread.execute();
    }

    private JSONObject convertToJson(String data){
        //Log.v("barcode","mm"+data);
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Barcode",data);
            return jsonObject;
        }catch (JSONException e){
            //toast
            new MyToast().createToast(BroadCastActivity.this,"json error:"+e.getMessage());
        }
        return  null;
    }

    //this method will call from thread on post execute method
    public void result(String data){
        Log.v("client","scanresult"+data);
        customDialog.hideLoadingDialog();


        if (data!=null){
            if (data.equals("connection error:401")){
                new MyToast().createToast(BroadCastActivity.this,"Token Expire Please login again");
                SharedPreferences preferences = getSharedPreferences("loginState",MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("isLogin",false);
                editor.apply();
                startActivity(new Intent(BroadCastActivity.this,LoginActivity.class));
                finish();
                return;
            }
            if (data.equals("connection error:403")){
                new MyToast().createToast(BroadCastActivity.this,"connection error:403");
                return;
            }
            try {
                JSONObject jsonObject = new JSONObject(data);
                if (jsonObject.getString("Message").equals("Items search successfully")){
                    //product found so open product edit page
                    //inner json object
                    JSONObject innerJson = jsonObject.getJSONObject("ActionData");
                    Intent intent = new Intent(BroadCastActivity.this,ProductDetailActivity.class);
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
                    new MyToast().createToast(BroadCastActivity.this,"product not found");
                    //product not found so first hide loading dialog then show confirm dialog for
                    //new entry
                    customDialog.createConfirmDialog();
                }
            }catch (JSONException e){
                //toast
                new MyToast().createToast(BroadCastActivity.this,"json errors:"+e.getMessage());

            }
        }else{
            new MyToast().createToast(BroadCastActivity.this,"error: "+"Api link down or don't exist");
        }
    }
    public void createIntent(){
        Intent intent = new Intent(BroadCastActivity.this,AddProductActivity.class);
        intent.putExtra("barcode",barcodeValue);
        startActivity(intent);
    }

}