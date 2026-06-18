package com.agridirect.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for DiseaseResultParser — verifies the labelled-text parser
 * correctly extracts all fields and maps severity/urgency values.
 */
class DiseaseResultParserTest {

    private static final String TYPICAL_RESPONSE =
            "ISSUE: Tomato Late Blight\n" +
            "SEVERITY: Severe\n" +
            "CAUSE: Phytophthora infestans fungus spreading in humid conditions\n" +
            "SYMPTOMS: Dark brown water-soaked lesions on leaves\nWhite mould on underside\n" +
            "TREATMENT: Remove affected leaves\nSpray copper fungicide\nImprove drainage\n" +
            "PREVENTION: Use resistant varieties\nAvoid overhead irrigation\n" +
            "URGENCY: Act immediately";

    // ── Happy path ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("parse returns correct disease name from ISSUE field")
    void parse_extractsDiseaseName() {
        DiseaseDetectionResult r = DiseaseResultParser.parse(TYPICAL_RESPONSE, "tomato");
        assertThat(r.getDiseaseName()).isEqualTo("Tomato Late Blight");
    }

    @Test
    @DisplayName("parse maps SEVERITY=Severe + URGENCY=Act immediately to 'critical'")
    void parse_severeCritical() {
        DiseaseDetectionResult r = DiseaseResultParser.parse(TYPICAL_RESPONSE, "tomato");
        assertThat(r.getSeverity()).isEqualTo("critical");
    }

    @Test
    @DisplayName("parse maps SEVERITY=Moderate to 'high'")
    void parse_moderateHigh() {
        String text = TYPICAL_RESPONSE.replace("Severe", "Moderate").replace("Act immediately", "Within a week");
        DiseaseDetectionResult r = DiseaseResultParser.parse(text, "tomato");
        assertThat(r.getSeverity()).isEqualTo("high");
    }

    @Test
    @DisplayName("parse maps SEVERITY=Mild to 'medium'")
    void parse_mildMedium() {
        String text = TYPICAL_RESPONSE.replace("Severe", "Mild").replace("Act immediately", "Monitor closely");
        DiseaseDetectionResult r = DiseaseResultParser.parse(text, "tomato");
        assertThat(r.getSeverity()).isEqualTo("medium");
    }

    @Test
    @DisplayName("parse sets confidence to 0.75 for non-healthy results")
    void parse_confidence() {
        DiseaseDetectionResult r = DiseaseResultParser.parse(TYPICAL_RESPONSE, "tomato");
        assertThat(r.getConfidence()).isEqualTo(0.75);
    }

    @Test
    @DisplayName("parse populates causes list from CAUSE field")
    void parse_causesNotEmpty() {
        DiseaseDetectionResult r = DiseaseResultParser.parse(TYPICAL_RESPONSE, "tomato");
        assertThat(r.getCauses()).isNotEmpty();
    }

    @Test
    @DisplayName("parse populates symptoms list from SYMPTOMS field")
    void parse_symptomsNotEmpty() {
        DiseaseDetectionResult r = DiseaseResultParser.parse(TYPICAL_RESPONSE, "tomato");
        assertThat(r.getSymptoms()).isNotEmpty();
    }

    @Test
    @DisplayName("parse converts TREATMENT steps into numbered TreatmentStep objects")
    void parse_treatmentStepsNumbered() {
        DiseaseDetectionResult r = DiseaseResultParser.parse(TYPICAL_RESPONSE, "tomato");
        assertThat(r.getTreatment()).isNotEmpty();
        assertThat(r.getTreatment().get(0).getStep()).isEqualTo(1);
    }

    @Test
    @DisplayName("parse sets affectedCrops from the cropName argument")
    void parse_affectedCrops() {
        DiseaseDetectionResult r = DiseaseResultParser.parse(TYPICAL_RESPONSE, "tomato");
        assertThat(r.getAffectedCrops()).containsExactly("tomato");
    }

    // ── Healthy plant detection ────────────────────────────────────────────

    @Test
    @DisplayName("parse detects healthy plant via 'no disease' in ISSUE field")
    void parse_healthyPlant_noDisease() {
        String healthy = "ISSUE: No disease detected\nSEVERITY: Mild\nCAUSE: N/A\nSYMPTOMS: None\n"
                + "TREATMENT: Continue routine care\nPREVENTION: Good practices\nURGENCY: Monitor closely";
        DiseaseDetectionResult r = DiseaseResultParser.parse(healthy, "wheat");
        assertThat(r.getDiseaseName()).isNull();
        assertThat(r.getConfidence()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("parse detects healthy plant via 'healthy' keyword in text")
    void parse_healthyKeyword() {
        String healthy = "The plant looks healthy. No signs of disease.";
        DiseaseDetectionResult r = DiseaseResultParser.parse(healthy, "rice");
        assertThat(r.getDiseaseName()).isNull();
    }

    // ── Edge cases / robustness ────────────────────────────────────────────

    @Test
    @DisplayName("parse handles null rawText without throwing")
    void parse_nullText_noThrow() {
        assertThatCode(() -> DiseaseResultParser.parse(null, "tomato"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("parse handles empty rawText gracefully")
    void parse_emptyText_returnsDefaultSeverity() {
        DiseaseDetectionResult r = DiseaseResultParser.parse("", "corn");
        assertThat(r.getSeverity()).isEqualTo("low");
    }

    @Test
    @DisplayName("parse handles null cropName — affectedCrops is empty")
    void parse_nullCropName_emptyList() {
        DiseaseDetectionResult r = DiseaseResultParser.parse(TYPICAL_RESPONSE, null);
        assertThat(r.getAffectedCrops()).isEmpty();
    }

    @Test
    @DisplayName("parse handles missing ISSUE label — sets fallback disease name")
    void parse_missingIssueLabel() {
        String partial = "SEVERITY: Moderate\nCAUSE: Unknown\nSYMPTOMS: Yellowing\n"
                + "TREATMENT: Spray neem oil\nPREVENTION: Crop rotation\nURGENCY: Within a week";
        DiseaseDetectionResult r = DiseaseResultParser.parse(partial, "paddy");
        assertThat(r.getDiseaseName()).isEqualTo("Unidentified issue");
    }

    @Test
    @DisplayName("parse case-insensitively matches 'HEALTHY' keyword")
    void parse_healthyKeyword_caseInsensitive() {
        String text = "ISSUE: HEALTHY plant, no problems";
        DiseaseDetectionResult r = DiseaseResultParser.parse(text, "okra");
        assertThat(r.getDiseaseName()).isNull();
    }

    @Test
    @DisplayName("imageAnalyzed field is initialized to empty string")
    void parse_imageAnalyzed_initialized() {
        DiseaseDetectionResult r = DiseaseResultParser.parse(TYPICAL_RESPONSE, "tomato");
        assertThat(r.getImageAnalyzed()).isNotNull();
    }
}
