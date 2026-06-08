package com.agridirect.ai;

import java.util.List;

public class TreatmentStep {
    private int step;
    private String description;
    private List<String> products;

    public TreatmentStep() {}

    public TreatmentStep(int step, String description) {
        this.step = step;
        this.description = description;
    }

    public int getStep() { return step; }
    public void setStep(int step) { this.step = step; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getProducts() { return products; }
    public void setProducts(List<String> products) { this.products = products; }
}
