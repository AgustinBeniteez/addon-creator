package com.agustinbenitez.addoncreator.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Represents the metadata section of a manifest.json
 * 
 * @author Agustín Benítez
 */
public class Metadata {
    
    private List<String> authors;
    
    private String license;
    
    @SerializedName("product_type")
    private String productType;
    
    public Metadata(List<String> authors, String license, String productType) {
        this.authors = authors;
        this.license = license;
        this.productType = productType;
    }
    
    public List<String> getAuthors() {
        return authors;
    }
    
    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }
    
    public String getLicense() {
        return license;
    }
    
    public void setLicense(String license) {
        this.license = license;
    }
    
    public String getProductType() {
        return productType;
    }
    
    public void setProductType(String productType) {
        this.productType = productType;
    }
}
