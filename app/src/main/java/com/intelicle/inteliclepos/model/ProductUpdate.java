package com.intelicle.inteliclepos.model;

public class ProductUpdate {

    String name,description;
    int qty;
    public ProductUpdate(String name, String description, int qty){
        this.name = name;
        this.description = description;
        this.qty = qty;
    }

    public String getName(){
        if (name!=null){
            return name;
        }
        return null;
    }

    public String getDescription(){
        if (description!=null){
            return description;
        }
        return null;
    }


}
