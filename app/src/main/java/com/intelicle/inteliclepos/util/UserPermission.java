package com.intelicle.inteliclepos.util;

import android.Manifest;
import android.content.Context;

import androidx.core.app.ActivityCompat;

import com.intelicle.inteliclepos.LoginActivity;

public class UserPermission {
    String[] permissions = new String[]{Manifest.permission.INTERNET,
            Manifest.permission.CAMERA,Manifest.permission.ACCESS_NETWORK_STATE};
    int requestCode = 100;
    public void getPermission(LoginActivity activity){
        ActivityCompat.requestPermissions(activity,permissions,requestCode);
    }

}
