package com.intelicle.inteliclepos.util;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MyToast {

    public void createToast(Context context,String msg){
//        Toast toast = Toast.makeText(context,msg,Toast.LENGTH_SHORT);
//        ViewGroup group = (ViewGroup) toast.getView();
//        TextView textView = (TextView) group.getChildAt(0);
//        textView.setTextSize(22);
//        toast.show();
        Toast.makeText(context,msg,Toast.LENGTH_SHORT).show();
    }
}
