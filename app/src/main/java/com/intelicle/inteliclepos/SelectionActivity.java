package com.intelicle.inteliclepos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.intelicle.inteliclepos.databinding.ActivitySelectionBinding;

public class SelectionActivity extends AppCompatActivity {
    private ActivitySelectionBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_selection);
        binding.btCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SelectionActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        binding.btBroadcast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SelectionActivity.this, BroadCastActivity.class);
                startActivity(intent);
            }
        });
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
                startActivity(new Intent(SelectionActivity.this,LoginActivity.class));
                break;
            case R.id.settings:
                startActivity(new Intent(SelectionActivity.this,ScannerSelectionActivity.class));
                finish();
                break;
            default:
        }
        return true;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}