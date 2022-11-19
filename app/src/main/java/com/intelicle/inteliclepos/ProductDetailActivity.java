package com.intelicle.inteliclepos;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.intelicle.inteliclepos.thread.AsyncThread;
import com.intelicle.inteliclepos.thread.NetworkConnectionThread;
import com.intelicle.inteliclepos.util.ItemsLookup;
import com.intelicle.inteliclepos.util.MyToast;
import com.intelicle.inteliclepos.util.Urls;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ProductDetailActivity extends AppCompatActivity implements View.OnClickListener,
        AdapterView.OnItemSelectedListener, RadioGroup.OnCheckedChangeListener{

    //widgets reference
    EditText nameEdit,descriptionEdit,qtyEdit,webPrices,itemPrice;
    TextView barcodeTextView;
    Button editProduct,saveEditProduct,cancelEditProduct;
    Spinner editCategory,editSubcategory;
    RadioGroup tillRadioGroup;
    //adapter reference
    ArrayAdapter editCategoryAdapter;
    ArrayAdapter editSubCategoryAdapter;
    //values reference
    String name="",description="",barcode="",category="",subCategory="",id="0",updateWebPrices="",price="";
    boolean showOnTill = false;
    int qty,screenLife=0;
    String[] subCat = {"select one"};
    public static  ProductDetailActivity update;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        update = this;
        //get Intent Values
        getIntentValues();
        //init views
        initViews();
        //init adapter for spinner
        initAdapter();
        //set data to views
        setData();
    }

    public void getIntentValues(){
        if (getIntent()!=null) {
            name = getIntent().getStringExtra("name");
            description = getIntent().getStringExtra("description");
            qty = getIntent().getIntExtra("qty", -1);
            barcode = getIntent().getStringExtra("barcode");
            id = getIntent().getStringExtra("id");
            category = getIntent().getStringExtra("cat");
            subCategory = getIntent().getStringExtra("sub");
            showOnTill = getIntent().getBooleanExtra("showontill",false);
            updateWebPrices = getIntent().getStringExtra("webprice");
            price = getIntent().getStringExtra("price");
        }
    }

    public void initViews(){
        //edit text
        barcodeTextView = findViewById(R.id.barcode);
        nameEdit = findViewById(R.id.name);
        descriptionEdit = findViewById(R.id.description);
        qtyEdit = findViewById(R.id.quantity);
        webPrices = findViewById(R.id.update_web_price);
        itemPrice = findViewById(R.id.update_item_price);
        //radio buttons
        tillRadioGroup = findViewById(R.id.update_show_on_till); tillRadioGroup.setOnCheckedChangeListener(this);

        //spinner
        editCategory = findViewById(R.id.category); editCategory.setOnItemSelectedListener(this);
        editSubcategory = findViewById(R.id.sub_category); editSubcategory.setOnItemSelectedListener(this);

        //buttons
        editProduct = findViewById(R.id.edit_product); editProduct.setOnClickListener(this);
        saveEditProduct = findViewById(R.id.save_product); saveEditProduct.setOnClickListener(this);
        cancelEditProduct = findViewById(R.id.cancel_edit); cancelEditProduct.setOnClickListener(this);
    }

    public void initAdapter(){
        editCategoryAdapter = new ArrayAdapter(ProductDetailActivity.this,
                android.R.layout.simple_spinner_dropdown_item, ItemsLookup.getCategoryNames());
        subCat = ItemsLookup.getSubCategoryNames(Integer.parseInt(category));
        editSubCategoryAdapter = new ArrayAdapter(ProductDetailActivity.this,
                android.R.layout.simple_spinner_dropdown_item, subCat);
//        if (subCategory!="") {
//
//
//        }else {
//            editSubCategoryAdapter = new ArrayAdapter(ProductDetailActivity.this,
//                    android.R.layout.simple_spinner_item, subCat);
//        }

    }

    @Override
    public void onCheckedChanged(RadioGroup group,int checkedId){
        RadioButton btn = findViewById(group.getCheckedRadioButtonId());
        String checkedValue = btn.getText().toString();
        Log.v("Test","out"+checkedValue);
        boolean isChecked=false;
        if (checkedValue.equals("yes")){
            Log.v("Test","in yes");
            isChecked = true;
        }else{
            Log.v("Test","in no");
            isChecked = false;
        }

        if (group.getId() == R.id.update_show_on_till) {
            showOnTill = isChecked;
        }

    }

    public void setData(){
        //textview
        barcodeTextView.setText(barcode);
        //edit text
        nameEdit.setText(name);
        descriptionEdit.setText(description);
        qtyEdit.setText(Integer.toString(qty));
        webPrices.setText(updateWebPrices);
        itemPrice.setText(price);

        //radioButtons
        if (showOnTill){
            tillRadioGroup.check(R.id.update_show_on_till_yes);
        }else {
            tillRadioGroup.check(R.id.update_show_on_till_no);
        }

        //spinner
        editCategory.setAdapter(editCategoryAdapter);
        editSubcategory.setAdapter(editSubCategoryAdapter);
        //set dropdown default values
        if (category!="")
            editCategory.setSelection(ItemsLookup.getCategoryIndex(Integer.parseInt(category)));
//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if (subCategory!=""){
//                    int i = ItemsLookup.getSubCategoryIndex(Integer.parseInt(category), Integer.parseInt(subCategory));
//                   // Log.v("cat","index:+"+i);
//                    editSubcategory.setSelection(i);
//
//                }
//            }
//        },2000);

    }

    @Override
    public void onItemSelected(AdapterView<?> parent,View view,int pos,long id){
        if (pos>0){
             if (parent.getId() == R.id.category){
                 String categoryName = parent.getItemAtPosition(pos).toString();
                 category = ItemsLookup.getCategoryId(categoryName);
                 //Log.v("test",parent.getItemAtPosition(pos).toString());

                  //onselection of category create adapter and attach to subcategory dropdown for related subcategories
                 String[] array = ItemsLookup.getSubCategoryNames(Integer.parseInt(category));
                 editSubcategory.setAdapter(new ArrayAdapter(ProductDetailActivity.this,
                         android.R.layout.simple_spinner_item,array));
                if (screenLife==1 && subCategory!=""){
                    int i = ItemsLookup.getSubCategoryIndex(Integer.parseInt(category), Integer.parseInt(subCategory));
                    // Log.v("cat","index:+"+i);
                    editSubcategory.setSelection(i);

                }
             }else if (parent.getId() == R.id.sub_category){
                 String subCategoryName = parent.getItemAtPosition(pos).toString();
                 subCategory = ItemsLookup.getSubCategoryId(subCategoryName);
                 //Log.v("test",parent.getItemAtPosition(pos).toString());
             }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent){

    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.edit_product:
                //startActivity(new Intent(ProductDetailActivity.this,AddProductActivity.class));
                break;
            case R.id.save_product:
                updateProduct();
                //finish();
                break;
            case R.id.cancel_edit:
                finish();
                break;
            default:

        }
    }



    private void updateProduct(){
        String error="";
        name = nameEdit.getText().toString();
        description = descriptionEdit.getText().toString();
        updateWebPrices = webPrices.getText().toString();
        price = itemPrice.getText().toString();

        String tempQty = qtyEdit.getText().toString();
        if (tempQty!=null && tempQty!="")
                qty = Integer.parseInt(tempQty);

        if (category.isEmpty()){
            error = "Category";
        }else if (subCategory.isEmpty()){
            subCategory="0";
        }

        if (name.isEmpty()){
            error = "Title";
        }
//        else if (description.isEmpty()){
//            error = "Description";
//        }
        else if (qty<0){
            error = "Quantity";
        }

        if (updateWebPrices.isEmpty()){
            // store 0 if empty from saving exception during string to int converion
            updateWebPrices = "0";
        }

        if (price.isEmpty()){
            // store 0 if empty from saving exception during string to int converion
            price="0";
        }
//        else if (subCategory.isEmpty()){
//            error = "Sub Category";
//        }
        else {
           // new MyToast().createToast(ProductDetailActivity.this,"input validation pass");
            //if every input is okay then before sending request first
            NetworkConnectionThread networkConnectionThread = new NetworkConnectionThread(ProductDetailActivity.this,"update");
            if(networkConnectionThread.isNetworkAvailable()){
                networkConnectionThread.execute();
            }else {
                //if not available
                new MyToast().createToast(ProductDetailActivity.this,"Not Connected to network");
            }
        }
        //this will show toast of error if any filed is empty
        if (!error.isEmpty()){
            new MyToast().createToast(ProductDetailActivity.this,"error "+error);
        }

    }

    public void execute(){
        //convert data to json & execute thread if all fields have value
        JSONObject json = convertToJson(name,description,qty);
        //new MyToast().createToast(ProductDetailActivity.this,"json model ready for request");
        Log.v("JSON",json.toString());
       // new MyToast().createToast(ProductDetailActivity.this,json.toString());
        AsyncThread thread = new AsyncThread(Urls.Type.UPDATE,ProductDetailActivity.this,json.toString());
        thread.execute();
    }


    private JSONObject convertToJson(String name,String description,int qty){
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Id",Integer.parseInt(id));
            jsonObject.put("CategoryId",Integer.parseInt(category));
            jsonObject.put("SubCategoryId",Integer.parseInt(subCategory));
            jsonObject.put("ItemName",name);
            jsonObject.put("Description",description);
            jsonObject.put("ItemQuantity",qty);
            jsonObject.put("IsShowOnTill",showOnTill);
            jsonObject.put("WebPrices",Double.parseDouble(updateWebPrices));
            jsonObject.put("ItemPrice",Double.parseDouble(price));

            return  jsonObject;
        }catch (JSONException e){
            new MyToast().createToast(ProductDetailActivity.this,"error"+e.getMessage());
        }
        return null;
    }

    //this method will call from thread on post execute method
    public void result(String data){
       // new MyToast().createToast(ProductDetailActivity.this,"response of server");
        Log.v("JSON",data);
        if (data!=null){
            //new MyToast().createToast(ProductDetailActivity.this,"response not null");
            try {

                JSONObject jsonObject = new JSONObject(data);
                //toast
                if (jsonObject.getString("Message").equals("Updated successfully")){
                    //product found so open product edit page
                    //toast
                    new MyToast().createToast(ProductDetailActivity.this,"Updated successfully");
                    finish();
                }else {
                    //product not updated, show toast
                    new MyToast().createToast(ProductDetailActivity.this,"Not updated");
                }
            }catch (JSONException e){
                //toast
                new MyToast().createToast(ProductDetailActivity.this,"Error:"+e.getMessage());
            }
        }else{
            new MyToast().createToast(ProductDetailActivity.this,"error: "+"Api link down or don't exist");
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        screenLife++;
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
                startActivity(new Intent(ProductDetailActivity.this,LoginActivity.class));
                break;
            case R.id.settings:
                startActivity(new Intent(ProductDetailActivity.this,ScannerSelectionActivity.class));
                finish();
                break;
            default:
        }
        return true;
    }
}