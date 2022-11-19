package com.intelicle.inteliclepos.model;

import java.util.ArrayList;
import java.util.List;

public class Subcategory {

    List<Integer> subCategoryIds = new ArrayList<Integer>();
    List<String> subCategoryNames = new ArrayList<String>();

    public List<Integer> getSubCategoryIds() {
        return subCategoryIds;
    }

    public void setSubCategoryIds(List<Integer> subCategoryIds) {
        this.subCategoryIds = subCategoryIds;
    }

    public List<String> getSubCategoryNames() {
        return subCategoryNames;
    }

    public void setSubCategoryNames(List<String> subCategoryNames) {
        this.subCategoryNames = subCategoryNames;
    }
}
