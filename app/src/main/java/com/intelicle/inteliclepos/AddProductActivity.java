package com.intelicle.inteliclepos;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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

import java.util.regex.Pattern;

public class AddProductActivity extends AppCompatActivity implements View.OnClickListener,
        AdapterView.OnItemSelectedListener, RadioGroup.OnCheckedChangeListener{

    //widgets reference
    TextView barcodeTextView;
    Spinner addCompany,addStore,addCategory,addSubCategory,addUnitOfSale,addTax;
    EditText addName,addDescription,addBrand,addProductCode,addPurchasePrice,
            addSalePrice,addMinStock,addQuantity,addProductPoints,addWebPrice;
    Button takePhoto,addSubmit,addCancel;
    RadioGroup tillRadioGroup, webRadioGroup;
    //values reference
    String barcode="",company="",store="",category="",subCategory="",unitOfSale="",tax="",name,description,brand,
           purchasePrice,salePrice,minStock,quantity,points,error,productCode,webPrice;
    boolean showOnTill = false,showOnWeb = false;
   // String[] country = {"select one", "1", "2", "3", "4", "5","","",""};
    public static String[] companyId = {"select company", ""};
    public static String[] outletId = {"select outlet",""};

   String[] subcategory = {"select subcategory", ""};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        if (getIntent()!=null)
            barcode = getIntent().getStringExtra("barcode");
        //init views
        initViews();
        //set onclick listener
        attachOnClick();
        //attach data to spinner
        createAdaptersForSpinner();
    }

    public void initViews(){
         //textView
         barcodeTextView = findViewById(R.id.barcode_text);barcodeTextView.setText(barcode);
         //spinner
         addCompany = findViewById(R.id.add_company);
         addStore = findViewById(R.id.add_store);
         addCategory = findViewById(R.id.add_category);
         addSubCategory = findViewById(R.id.add_sub_category);
         addUnitOfSale = findViewById(R.id.add_unit_of_sale);
         addTax = findViewById(R.id.add_tax);
         //EditText
         addName = findViewById(R.id.add_name);
         addDescription = findViewById(R.id.add_description);
         addBrand = findViewById(R.id.add_brand);
         addProductCode = findViewById(R.id.add_product_code);
         addPurchasePrice = findViewById(R.id.add_purchase_price);
         addSalePrice = findViewById(R.id.add_sale_price);
         addMinStock = findViewById(R.id.add_minimum_stock);
         addQuantity = findViewById(R.id.add_quantity);
         addProductPoints = findViewById(R.id.add_product_points);
         addWebPrice = findViewById(R.id.add_web_price);
         //buttons
         takePhoto = findViewById(R.id.take_photo);
         addSubmit = findViewById(R.id.add_submit);
         addCancel = findViewById(R.id.add_cancel);
         //radio groups
         tillRadioGroup = findViewById(R.id.show_on_till);
         webRadioGroup = findViewById(R.id.show_on_web);

    }

    public void attachOnClick(){
        //button onclick
      //  takePhoto.setOnClickListener(this);
        addSubmit.setOnClickListener(this);
        addCancel.setOnClickListener(this);

        //spinner onItemSelected
        addCompany.setOnItemSelectedListener(this);
        addStore.setOnItemSelectedListener(this);
        addCategory.setOnItemSelectedListener(this);
        addSubCategory.setOnItemSelectedListener(this);
        addUnitOfSale.setOnItemSelectedListener(this);
        addTax.setOnItemSelectedListener(this);

        //radio group onChecked changed listener
        tillRadioGroup.setOnCheckedChangeListener(this);
        webRadioGroup.setOnCheckedChangeListener(this);
    }

    //view.onClickListener method
    @Override
    public void onClick(View v){
        switch (v.getId()){
//            case R.id.take_photo:
//
//                break;
            case R.id.add_submit:
                //validate user input
                //if all inputs will fill then validateUserInput() will return null mean no error
                if (validateUserInput()==null){
                    NetworkConnectionThread networkConnectionThread = new NetworkConnectionThread(AddProductActivity.this,"add");
                    if(networkConnectionThread.isNetworkAvailable()){
                        networkConnectionThread.execute();
                    }else {
                        //if not available
                        new MyToast().createToast(AddProductActivity.this,"Not Connected to network");
                    }
                }else {
                    new MyToast().createToast(AddProductActivity.this,"Please check "+error);
                    //reset error ot empty string
                    error = null;
                }
                break;
            case R.id.add_cancel:
                finish();
                break;
            default:

        }
    }


    public void execute(){
        //convertTO JSON
        JSONObject json = convertToJson();
        //new MyToast().createToast(AddProductActivity.this,json.toString());
        Log.v("JSON",json.toString());
        AsyncThread thread = new AsyncThread(Urls.Type.ADD,AddProductActivity.this,json.toString());
        thread.execute();
    }

    //spinner callback
    @Override
    public void onItemSelected(AdapterView<?> parent,View child,int pos,long id){
        if (pos>0){
            String item =  parent.getItemAtPosition(pos).toString();
            if (parent.getId() == R.id.add_company){
                company = item;
               // new MyToast().createToast(AddProductActivity.this,"company");
            }else if (parent.getId() == R.id.add_store){
               // new MyToast().createToast(AddProductActivity.this,"store");
                store = item;
            }else if (parent.getId() == R.id.add_category){
                category = ItemsLookup.getCategoryId(item);
               // new MyToast().createToast(AddProductActivity.this,"Category Id "+category);
                String[] array = ItemsLookup.getSubCategoryNames(Integer.parseInt(category));
                addSubCategory.setAdapter(new ArrayAdapter(AddProductActivity.this, android.R.layout.simple_spinner_item,array));
               // category = item;
            }else if (parent.getId() == R.id.add_sub_category){
                subCategory = ItemsLookup.getSubCategoryId(item);
               // new MyToast().createToast(AddProductActivity.this,"sub category "+subCategory);
            }else if (parent.getId() == R.id.add_unit_of_sale){
                unitOfSale = ItemsLookup.getUnitOfSaleId(item);
               // new MyToast().createToast(AddProductActivity.this,"unit of sale id "+unitOfSale);
                //unitOfSale = item;
            }else if (parent.getId() == R.id.add_tax){
                tax = ItemsLookup.getTaxId(item);
                //new MyToast().createToast(AddProductActivity.this,"tax id "+tax);
                //tax = item;
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView){

    }

    //radio group callback
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        RadioButton btn = findViewById(group.getCheckedRadioButtonId());
        String checkedValue = btn.getText().toString();
        Log.v("Test",checkedValue);
        boolean isChecked=false;
        if (checkedValue.equals("yes")){
            isChecked = true;
        }

        if (group.getId() == R.id.show_on_web){
            showOnWeb = isChecked;
        }else if (group.getId() == R.id.show_on_till){
            showOnTill = isChecked;
        }
        Log.v("Test",""+isChecked);
    }

    private void createAdaptersForSpinner(){
        ArrayAdapter companyAdapter,storeAdapter,categoryAdapter,subCategoryAdapter,
                unitOfSaleAdapter,taxAdapter;
        //init array adapter
        companyAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item,companyId);
        storeAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item,outletId);
        categoryAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item,ItemsLookup.getCategoryNames());
        subCategoryAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item,subcategory);
        unitOfSaleAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item,ItemsLookup.getUnitOfSaleNames());
        taxAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, ItemsLookup.getTaxNames());

        //attach array adapter to corresponding spinners
        addCompany.setAdapter(companyAdapter);//addCompany.setSelection(1);
        addStore.setAdapter(storeAdapter);//addStore.setSelection(1);
        addCategory.setAdapter(categoryAdapter);
        addSubCategory.setAdapter(subCategoryAdapter);
        addUnitOfSale.setAdapter(unitOfSaleAdapter);
        addTax.setAdapter(taxAdapter);
    }

//String barcode,company,store,category,subCategory,name,description,brand,unitOfSale,tax,
//           purchasePrice,salePrice,minStock,quantity,points,showOnTill,showOnWeb;
//
    public String validateUserInput(){
        //GET edit text value and store in data members

        productCode = addProductCode.getText().toString().trim();
        name = addName.getText().toString();
        description = addDescription.getText().toString();
        brand = addBrand.getText().toString().trim();
        purchasePrice = addPurchasePrice.getText().toString().trim();
        salePrice = addSalePrice.getText().toString().trim();
        minStock = addMinStock.getText().toString().trim();
        quantity = addQuantity.getText().toString().trim();
        points = addProductPoints.getText().toString().trim();
        webPrice = addWebPrice.getText().toString().trim();
//
//        Log.v("input","category"+category);
//        Log.v("input","subCategory"+subCategory);
//        Log.v("input","tax"+tax);
//        Log.v("input","unitOfSale"+unitOfSale);


        //temporary hold values of company and outlet
        company = companyId[1];
        store = outletId[1];

        for (int i=0; i<barcode.length(); i++){
            char c = barcode.charAt(i);
            if (!Character.isDigit(c))
                return error = "Barcode format is not vaild";

        }

        if (name.isEmpty()){
            error = "Title";
        }else if (barcode.isEmpty()){
            error = "barcode";
        }else {
            barcode.length();
            if (purchasePrice.isEmpty()){
                error = "Purchase price";
            }else if (salePrice.isEmpty()){
                error = "Sale Price";
            }else if (minStock.isEmpty()){
                error = "Min Stock";
            }else if (quantity.isEmpty()){
                error = "Quantity";
            }else if (company.isEmpty()){
                error = "Company";
            }else if (store.isEmpty()){
                error = "Store";
            }else if (category.isEmpty()){
                error = "Category";
            }else if (unitOfSale.isEmpty()){
                error = "Unit Of Sale";
            }else if (tax.isEmpty()){
                error = "Tax";
            }else if (webPrice.isEmpty()){
                //Optional input so no error
                webPrice = "0";
            }
            if (points.isEmpty()){
                //Optional input so no error
                points = "0";
            }
        }

        //optional input removed
//         else if (brand.isEmpty()){
//            error = "Brand";
//        }        else if (productCode.isEmpty()){
//            error = "Product code";
//       }        else if (productCode.isEmpty()){
//            error = "Product code";
//        }else if (description.isEmpty()){
//            error = "Description";
//        }else if (subCategory.isEmpty()){
//            error = "Sub Category";
//        }
//

        //check if error has value then return error otherwise null
        if (error!=null){
            return error;
        }
      return null;
    }

    public JSONObject convertToJson(){
        try {
            JSONObject jsonObject = new JSONObject();
            //edittext values
            jsonObject.put("ItemName",name);
            jsonObject.put("ItemPrice",Double.parseDouble(salePrice));
            jsonObject.put("ItemPurchasePrice",Double.parseDouble(purchasePrice));
            jsonObject.put("ItemBarCode",Long.parseLong(barcode));
            jsonObject.put("ItemQuantity",Integer.parseInt(quantity));
            jsonObject.put("MinimumStockLevel",Integer.parseInt(minStock));
            jsonObject.put("Description",description);
            //three edittext optional value
            jsonObject.putOpt("Brand",brand);
            jsonObject.putOpt("ProductPoint",Integer.parseInt(points));
            jsonObject.putOpt("ProductCode",productCode);
            jsonObject.putOpt("WebPrices",Double.parseDouble(webPrice));
            //spinner values
            jsonObject.put("CompanyId",Integer.parseInt(company));
            jsonObject.put("OutletId",Integer.parseInt(store));
            jsonObject.put("CategoryId",Integer.parseInt(category));
            if (subCategory.isEmpty())
                subCategory = "0";
            jsonObject.put("SubCategoryId",Integer.parseInt(subCategory));
            jsonObject.put("UnitOfSaleId",Integer.parseInt(unitOfSale));
            jsonObject.put("TaxId",Integer.parseInt(tax));
            //radio button values
            jsonObject.put("IsShowOnTill",showOnTill);
            jsonObject.put("IsShowOnWeb",showOnWeb);

            return jsonObject;
        }catch (JSONException e){
            Log.v("exception",e.getMessage());
        }
        return null;
    }

    //this method will call from thread on post execute method
    public void result(String data){
        if (data!=null){
            try {
                JSONObject jsonObject = new JSONObject(data);
                if (jsonObject.getString("Message").equals("Item added")){
                    //clear input and spinner after successful input
                    clearInput();
                    //toast for success
                    new MyToast().createToast(AddProductActivity.this,"Item added");
                    finish();
                }else {
                    //product not found so show toast
                    new MyToast().createToast(AddProductActivity.this,"Item not added");
                }
            }catch (JSONException e){
                //toast
                new MyToast().createToast(AddProductActivity.this,"Error:"+e.getMessage());
                Log.v("exception",e.getMessage());
            }
        }else{
            new MyToast().createToast(AddProductActivity.this,"error: "+"Api link down or don't exist");
        }
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
                        .edit().putBoolean("isLogin",false).apply();
                startActivity(new Intent(AddProductActivity.this,LoginActivity.class));
                break;
            case R.id.settings:
                startActivity(new Intent(AddProductActivity.this,ScannerSelectionActivity.class));
                finish();
                break;
            default:
        }
        return true;
    }

    private void clearInput(){
        //textview barcode clear
        barcodeTextView.setText("");
        barcode="";
        //clear edittext after item added
        addProductCode.setText("");
        addName.setText("");
        addDescription.setText("");
        addBrand.setText("");
        addPurchasePrice.setText("");
        addSalePrice.setText("");
        addMinStock.setText("");
        addQuantity.setText("");
        addProductPoints.setText("");
        addProductPoints.setText("");
        addBrand.setText("");
        //clear spinner to default value after item added
        addCompany.setSelection(0);
        addStore.setSelection(0);
        addCategory.setSelection(0);
        addSubCategory.setSelection(0);
        addUnitOfSale.setSelection(0);
        addTax.setSelection(0);
        //clear radiobutton after item added
        RadioButton webYes,webNo,tillYes,tillNo;
        webYes = findViewById(R.id.show_on_web_yes);
        webNo = findViewById(R.id.show_on_web_no);
        tillYes = findViewById(R.id.show_on_till_yes);
        tillNo = findViewById(R.id.show_on_till_no);
        webYes.setChecked(false);
        webNo.setChecked(false);
        tillYes.setChecked(false);
        tillNo.setChecked(false);

    }
}