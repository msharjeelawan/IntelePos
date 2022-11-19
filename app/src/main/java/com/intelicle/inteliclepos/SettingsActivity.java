package com.intelicle.inteliclepos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.intelicle.inteliclepos.util.MyToast;
import com.intelicle.inteliclepos.util.Urls;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    EditText serverUrl;
    Button submit,cancel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initViews();
    }

    private void initViews(){
        serverUrl = findViewById(R.id.server_url);
        if (Urls.getDomain() != null) {
            serverUrl.setText(Urls.getDomain());
        } else {
            serverUrl.setText("");
        }
        submit = findViewById(R.id.url_submit);
        cancel = findViewById(R.id.url_cancel);

        //attach on click listener
        submit.setOnClickListener(this);
        cancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.url_submit:
                String url = serverUrl.getText().toString().trim();
                String portNo = "";
                if (url.contains("http")){
                    String[] urlParts = url.split("://");
                    url = urlParts[1];
                }
                Log.v("url out ",url);
                //Patterns.DOMAIN_NAME.matcher(url).matches()
                //split url in two parts from :
                if (url.contains(":")) {
                    String[] urlParts = url.split(":");
                    url = urlParts[0];
                    portNo = urlParts[1];
//                    //again split because url will be start from http so it will not pass from validation
//                    if (url.contains("http")){
//                        urlParts = url.split("://");
//                        url = urlParts[1];
//                    }
                    Log.v("url in ",url);
                    if (Patterns.IP_ADDRESS.matcher(url).matches()){
                        //if matches pass then concat server ip and port no
                        url = url+":"+portNo;
                        //show toast for success saved
                        new MyToast().createToast(SettingsActivity.this,"Url saved");
                        //updated domain value in urls class & shared preference for later use
                        Urls.setDomain(url);
                        SharedPreferences preferences = getSharedPreferences("server_url",MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("url",url);
                        editor.commit();
                        //toast
                        finish();
                    }else{
                        new MyToast().createToast(SettingsActivity.this,"Url pattern is invalid");
                    }
                }else{
                    //toast
                    new MyToast().createToast(SettingsActivity.this,"Please enter valid url");
                }
                break;
            case R.id.url_cancel:
                finish();
                break;
            default:

        }
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