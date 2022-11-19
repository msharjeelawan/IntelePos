package com.intelicle.inteliclepos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.intelicle.inteliclepos.util.MyToast;

public class ScannerSelectionActivity extends AppCompatActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    RadioButton camera,external;
    RadioGroup scannerSelection;
    Button save,cancel;
    String scannerType;
    SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner_selection);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        preferences = getSharedPreferences("loginState",MODE_PRIVATE);
        initViews();
    }

    public void initViews(){

        //Radio group
        scannerSelection = findViewById(R.id.scannerSelection);scannerSelection.setOnCheckedChangeListener(this);
        //Radio Buttons
        camera = findViewById(R.id.cameraScanner);
        external = findViewById(R.id.externalScanner);

        //buttons
        save = findViewById(R.id.save_scanner_selection); save.setOnClickListener(this);
        cancel = findViewById(R.id.cancel_scanner_selection); cancel.setOnClickListener(this);

        //get scannertype value if available then show default value for use understanding
        String type = preferences.getString("scannerType","");
        if (type!=null && type!=""){
            if (type.equals("camera"))
                 scannerSelection.check(R.id.cameraScanner);
            else if (type.equals("external"))
                scannerSelection.check(R.id.externalScanner);
        }
    }


    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.save_scanner_selection:

                if (scannerType!=null){
                    //save user login state
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("scannerType",scannerType);
                    editor.commit();
                    boolean isLogin = preferences.getBoolean("isLogin",false);
                    if (isLogin) {
                        if (scannerType.equals("camera")) {
                            startActivity(new Intent(ScannerSelectionActivity.this, MainActivity.class));
                        }else {
                            startActivity(new Intent(ScannerSelectionActivity.this, BroadCastActivity.class));
                        }
                    }else {
                        startActivity(new Intent(ScannerSelectionActivity.this, LoginActivity.class));
                    }
                    finish();
                }else{
                    new MyToast().createToast(ScannerSelectionActivity.this,"Please select scanner type");
                }

                break;
            case R.id.cancel_scanner_selection:
                new MyToast().createToast(ScannerSelectionActivity.this,"cancel");
                    finish();
                break;

        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId){
        RadioButton btn =  findViewById(checkedId);
        String value = btn.getText().toString();

        if (value.equals("Camera Scanner")){
            scannerType = "camera";
        }else  if (value.equals("External Scanner")){
            scannerType = "external";
        }

        Log.v("testbtn","radio"+value);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}