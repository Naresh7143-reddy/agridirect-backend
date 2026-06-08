package com.agridirect.ai;

import java.time.Instant;
import java.util.List;

public class DiseaseDetectionResult {
    private String diseaseName;
    private String scientificName;
    private double confidence;
    private String severity; // low | medium | high | critical
    private List<String> affectedCrops;
    private List<String> symptoms;
    private List<String> causes;
    private List<TreatmentStep> treatment;
    private List<String> preventionTips;
    private String imageAnalyzed;
    private String detectedAt;

    public DiseaseDetectionResult() {
        this.detectedAt = Instant.now().toString();
    }

    public String getDiseaseName() { return diseaseName; }
    public void setDiseaseName(String diseaseName) { this.diseaseName = diseaseName; }

    public String getScientificName() { return scientificName; }
    public void setScientificName(String scientificName) { this.scientificName = scientificName; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public List<String> getAffectedCrops() { return affectedCrops; }
    public void setAffectedCrops(List<String> affectedCrops) { this.affectedCrops = affectedCrops; }

    public List<String> getSymptoms() { return symptoms; }
    public void setSymptoms(List<String> symptoms) { this.symptoms = symptoms; }

    public List<String> getCauses() { return causes; }
    public void setCauses(List<String> causes) { this.causes = causes; }

    public List<TreatmentStep> getTreatment() { return treatment; }
    public void setTreatment(List<TreatmentStep> treatment) { this.treatment = treatment; }

    public List<String> getPreventionTips() { return preventionTips; }
    public void setPreventionTips(List<String> preventionTips) { this.preventionTips = preventionTips; }

    public String getImageAnalyzed() { return imageAnalyzed; }
    public void setImageAnalyzed(String imageAnalyzed) { this.imageAnalyzed = imageAnalyzed; }

    public String getDetectedAt() { return detectedAt; }
    public void setDetectedAt(String detectedAt) { this.detectedAt = detectedAt; }
}
