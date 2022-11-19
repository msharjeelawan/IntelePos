package com.intelicle.inteliclepos.util;

//import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.intelicle.inteliclepos.AddProductActivity;
import com.intelicle.inteliclepos.BroadCastActivity;
import com.intelicle.inteliclepos.MainActivity;
import com.intelicle.inteliclepos.R;

public class CustomDialog {

    private Context context;
    private String title;
    private AlertDialog loadingDialog, confirmDialog;

    public CustomDialog(Context context,String title) {
        this.context = context;
        this.title = title;
    }

    public void createLoadingDialog(){
//        Dialog dialog = new Dialog(context);
//        dialog.setTitle(title);
//        dialog.setContentView(R.layout.loader);
//        dialog.setCancelable(false);
        //dialog.

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(title);
        alertDialog.setView(R.layout.loader);
        alertDialog.setCancelable(false);
        loadingDialog = alertDialog.create();
        loadingDialog.show();
    }

    public void hideLoadingDialog(){
        if (this.loadingDialog!=null){
            this.loadingDialog.dismiss();
        }
    }

    public void createConfirmDialog(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle("Confirm");
        alertDialog.setCancelable(false);
        alertDialog.setMessage("Do you want to add product?");
        alertDialog.setPositiveButton("Create", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int which){
                    hideConfirmDialog();
                    if (context instanceof MainActivity)
                         ((MainActivity)context).createIntent();
                    else if (context instanceof BroadCastActivity)
                        ((BroadCastActivity)context).createIntent();
                }

        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                hideConfirmDialog();
                if (context instanceof MainActivity)
                    ((MainActivity)context).resetCount();
            }
        });
        confirmDialog = alertDialog.create();
        confirmDialog.show();
    }

    public void hideConfirmDialog(){
        if (confirmDialog!=null)
        this.confirmDialog.dismiss();
    }
}
