package com.intelicle.inteliclepos.model;

import java.util.ArrayList;
import java.util.List;

public class Category {
    public int id=0;
    Subcategory subcategory = new Subcategory();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Subcategory getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(Subcategory subcategory) {
        this.subcategory = subcategory;
    }
}
