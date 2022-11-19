package com.intelicle.inteliclepos.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.intelicle.inteliclepos.AddProductActivity;
import com.intelicle.inteliclepos.MainActivity;
import com.intelicle.inteliclepos.model.Category;
import com.intelicle.inteliclepos.model.Subcategory;
import com.intelicle.inteliclepos.model.TaxId;
import com.intelicle.inteliclepos.thread.AsyncThread;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class ItemsLookup {

    public static int reader=0;
    private static int[] unitOfSaleIds,taxIds,categoryIds;
    private static String[] unitOfSaleNames,taxNames,categoryNames;
    //create list for holding company objects
    public static List<Category> categoryList = new ArrayList<Category>();
    //this will use when user select category and subcat will initalize
    private static Integer[] tempSubCatIds;
    private static String[] tempSubCatNames;

    public static void load(Context context){
        if (reader==0) {
            Log.v("search","load call");
            SharedPreferences preferences = context.getSharedPreferences("loginState", MODE_PRIVATE);
            String companyId = preferences.getString("CompanyId", "");
            String outletId = preferences.getString("OutletId", "");
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("CompanyId", Integer.parseInt(companyId));
                jsonObject.put("OutletId", Integer.parseInt(outletId));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            AsyncThread thread = new AsyncThread(Urls.Type.SHOW, context, jsonObject.toString());
            thread.execute();
            reader++;
        }
    }


    public static void resultCategory(String data){
        Log.v("search","resultCategory");
        Log.v("search","result"+data);
        if (data!=null){
            try {
                JSONObject jsonObject = new JSONObject(data);
                //json object contain category,tax and unitofslae data
                JSONObject actionData = jsonObject.optJSONObject("ActionData");
                //json array from action data property
                JSONArray unitOfSale = actionData.optJSONArray("UnitOfSale");
                JSONArray taxObject = actionData.optJSONArray("TaxObject");
                JSONArray categories = actionData.optJSONArray("Categories");
                Log.v("search","unitofsale"+unitOfSale.toString());
                Log.v("search","taxObject"+taxObject.toString());
                Log.v("search","categories"+categories.toString());

                //call loadUnitOfSale for unit of sale dropdown
                loadUnitOfSale(unitOfSale);
                //call loadTaxObject for tax dropdown
                loadTaxObject(taxObject);
                //call l loadCategory for cat and sub cat dropdown
                loadCategory(categories);


            }catch (JSONException e){
                //toast
                //new MyToast().createToast(MainActivity.this,"json errors:"+e.getMessage());

            }
        }
    }

    private static void loadUnitOfSale(JSONArray unitOfSale){
        unitOfSaleIds = new int[unitOfSale.length()+1];
        unitOfSaleNames = new String[unitOfSale.length()+1];
        for (int a=0;a<unitOfSale.length();a++){
            if (a==0){
                unitOfSaleIds[a] = -1;
                unitOfSaleNames[a] = "Select Unit Of Sale";
            }
            int index = a+1;
            JSONObject unitOfSaleJson = unitOfSale.optJSONObject(a);
            unitOfSaleIds[index] = unitOfSaleJson.optInt("Id");
            unitOfSaleNames[index] = unitOfSaleJson.optString("Name");
        }
        Log.v("search","UnitOfSale id size"+unitOfSaleIds.length);
        Log.v("search","UnitOfSale name size"+unitOfSaleNames.length);
    }

    private static void loadTaxObject(JSONArray taxArray){
        taxIds = new int[taxArray.length()+1];
        taxNames = new String[taxArray.length()+1];
        for (int a=0;a<taxArray.length();a++){
            if (a==0){
                taxIds[a] = -1;
                taxNames[a] =  "Select Tax Id";
            }
            int index = a+1;
            JSONObject taxIdJson = taxArray.optJSONObject(a);
            taxIds[index] = taxIdJson.optInt("Id");
            taxNames[index] = taxIdJson.optString("TaxName");

        }
        Log.v("search","TaxObject id size"+taxIds.length);
        Log.v("search","TaxObject name size"+taxNames.length);
    }

    private static void loadCategory(JSONArray categories){
        categoryIds = new int[categories.length()+1];
        categoryNames = new String[categories.length()+1];
        for (int a=0;a<categories.length();a++){
            //first index will use for demo value or spinner type
            if (a==0){
                categoryIds[a] = -1;
                categoryNames[a] = "Select Category";

            }
            int index = a+1;
            JSONObject categoriesJson = categories.optJSONObject(a);
            int id = categoriesJson.optInt("Id");
            categoryIds[index] = id;
            categoryNames[index] =  categoriesJson.optString("CategoryName");
            //subcategory size
            JSONArray subCategoryArray = categoriesJson.optJSONArray("subCategories");
            //create category object
            Category category = new Category();
            category.setId(id);
            Subcategory subcategory = category.getSubcategory();
            List<Integer> subIds = subcategory.getSubCategoryIds();
            List<String> subNames = subcategory.getSubCategoryNames();
            //inner loop for subcategory
            for (int i=0;i<subCategoryArray.length();i++){
                if (i==0){
                    subIds.add(-1);
                    subNames.add("Select Subcategory");
                }
                JSONObject subCat = subCategoryArray.optJSONObject(i);
                subIds.add(subCat.optInt("Id"));
                subNames.add(subCat.optString("CategoryName"));
            }
            //after creating subcategory obj put in category for later use

            categoryList.add(category);
        }
        Log.v("search","Category id size"+categoryIds.length);
        Log.v("search","Category name size"+categoryNames.length);
    }


    public static String[] getUnitOfSaleNames(){
        return unitOfSaleNames;
    }


    public static String[] getTaxNames(){
        return taxNames;
    }


    public static String[] getCategoryNames(){
        return categoryNames;
    }

    public static String[] getSubCategoryNames(int id){
        //this will find category then return subcategories of parent cat.
        for (int a=0;a<categoryList.size();a++){
            Category category = categoryList.get(a);
            int tempId = category.getId();
            if (tempId==id){
                Subcategory subcategory = category.getSubcategory();
                //convert arraylist of names and ids of subcat into array bcz spinner require array.
                Integer[] subCatIds = new Integer[subcategory.getSubCategoryIds().size()];
                subCatIds = subcategory.getSubCategoryIds().toArray(subCatIds);
                String[] subCatNames = new String[subcategory.getSubCategoryNames().size()];
                subCatNames = subcategory.getSubCategoryNames().toArray(subCatNames);
                tempSubCatIds = subCatIds;
                tempSubCatNames = subCatNames;
                return subCatNames;
            }
        }
        return new String[]{"11","12","13","14","15"};
    }

    //it will return id of matching value of UnitOfSaleName
    public static String getUnitOfSaleId(String name){
        String id="";
        for (int a=0;a<unitOfSaleIds.length;a++){
            String n = unitOfSaleNames[a];
            if (n.equals(name)){
                id = String.valueOf(unitOfSaleIds[a]);
                break;
            }
        }
        return id;
    }

    //it will return id of matching value of UnitOfSaleName
    public static String getTaxId(String name){
        String id="";
        for (int a=0;a<taxIds.length;a++){
            String n = taxNames[a];
            if (n.equals(name)){
                id = String.valueOf(taxIds[a]);
                break;
            }
        }
        return id;
    }

    //it will return id of matching value of UnitOfSaleName
    public static String getCategoryId(String name){
        String id="";
        for (int a=0;a<categoryIds.length;a++){
            String n = categoryNames[a];
            if (n.equals(name)){
                id = String.valueOf(categoryIds[a]);
                break;
            }
        }
        return id;
    }


    public static String getSubCategoryId(String name){
        String id="";
        for (int a=0;a<tempSubCatIds.length;a++){
            String n = tempSubCatNames[a];
            if (n.equals(name)){
                id = String.valueOf(tempSubCatIds[a]);
                break;
            }
        }
        return id;
    }


    public static int getCategoryIndex(int id){
        int index=0;
        if (categoryIds.length>0){
            for (int i=0;i<categoryIds.length;i++){
                int tempId = categoryIds[i];
                if (tempId==id) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }

    public static int getSubCategoryIndex(int catId,int subCatId){
        int index=0;
        //getSubCategoryNames(catId);
        Log.v("sub","cat: "+catId);
        Log.v("sub","cat: "+subCatId);
        if (tempSubCatIds.length>0){
            Log.v("sub","length >0 : "+tempSubCatIds.length);
            for (int i=0;i<tempSubCatIds.length;i++){
                int tempId = tempSubCatIds[i];
                if (tempId==subCatId) {
                    Log.v("sub","item found index"+ i);
                    index = i;
                    break;
                }
            }
        }
        return index;
    }

}
