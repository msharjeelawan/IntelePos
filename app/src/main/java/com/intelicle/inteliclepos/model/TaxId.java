package com.intelicle.inteliclepos.model;

public class TaxId {
    private int id;
    private String taxName;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setTaxName(String taxName) {
        this.taxName = taxName;
    }

    public String getTaxName() {
        return taxName;
    }
}
