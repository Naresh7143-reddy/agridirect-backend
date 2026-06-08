package com.agridirect.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the labelled plain-text response produced by GeminiService.detectDisease()
 * (format: "ISSUE: ...\nSEVERITY: ...\nCAUSE: ...\nSYMPTOMS: ...\nTREATMENT: ...\n
 * PREVENTION: ...\nURGENCY: ...") into a structured DiseaseDetectionResult that
 * matches the shape the mobile app expects.
 */
public final class DiseaseResultParser {

    private DiseaseResultParser() {}

    private static final List<String> NO_DISEASE_HINTS = Arrays.asList(
            "no disease", "healthy", "no issue", "looks healthy", "no pest", "no problem detected"
    );

    public static DiseaseDetectionResult parse(String rawText, String cropName) {
        DiseaseDetectionResult result = new DiseaseDetectionResult();
        result.setAffectedCrops(cropName != null ? List.of(cropName) : List.of());
        result.setImageAnalyzed("");

        String issue = extract(rawText, "ISSUE");
        String severityRaw = extract(rawText, "SEVERITY");
        String cause = extract(rawText, "CAUSE");
        String symptoms = extract(rawText, "SYMPTOMS");
        String treatment = extract(rawText, "TREATMENT");
        String prevention = extract(rawText, "PREVENTION");
        String urgency = extract(rawText, "URGENCY");

        boolean healthy = issue != null && containsAny(issue.toLowerCase(), NO_DISEASE_HINTS)
                || (issue == null && containsAny(rawText.toLowerCase(), NO_DISEASE_HINTS));

        result.setDiseaseName(healthy ? null : (issue != null ? issue : "Unidentified issue"));
        result.setSeverity(mapSeverity(severityRaw, urgency));
        result.setConfidence(healthy ? 0.0 : 0.75);
        result.setCauses(toBulletList(cause));
        result.setSymptoms(toBulletList(symptoms));
        result.setTreatment(toTreatmentSteps(treatment));
        result.setPreventionTips(toBulletList(prevention));

        return result;
    }

    private static String extract(String text, String label) {
        if (text == null) return null;
        // Matches "LABEL:" up to the next known label or end of string
        Pattern p = Pattern.compile(
                label + "\\s*:\\s*(.*?)(?=\\n\\s*(?:ISSUE|SEVERITY|CAUSE|SYMPTOMS|TREATMENT|PREVENTION|URGENCY)\\s*:|$)",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(text);
        if (m.find()) {
            String value = m.group(1).trim();
            return value.isEmpty() ? null : value;
        }
        return null;
    }

    private static boolean containsAny(String haystack, List<String> needles) {
        for (String n : needles) {
            if (haystack.contains(n)) return true;
        }
        return false;
    }

    private static String mapSeverity(String severityRaw, String urgencyRaw) {
        String s = (severityRaw == null ? "" : severityRaw).toLowerCase();
        String u = (urgencyRaw == null ? "" : urgencyRaw).toLowerCase();
        if (s.contains("severe") || u.contains("immediately")) return "critical";
        if (s.contains("moderate") || u.contains("within a week")) return "high";
        if (s.contains("mild")) return "medium";
        if (s.isEmpty() && u.isEmpty()) return "low";
        return "low";
    }

    private static List<String> toBulletList(String text) {
        if (text == null || text.isBlank()) return new ArrayList<>();
        List<String> items = new ArrayList<>();
        // Split on newlines, bullet markers, or sentence-ending punctuation followed by space
        String[] lines = text.split("\\r?\\n|•|‣|^[\\s]*[-*]\\s+");
        for (String line : lines) {
            String trimmed = line.replaceFirst("^[\\s\\-*•‣]+", "").trim();
            if (!trimmed.isEmpty()) items.add(trimmed);
        }
        if (items.size() <= 1) {
            // Fall back to splitting on sentences if it's one big paragraph
            items.clear();
            for (String sentence : text.split("(?<=[.!?])\\s+")) {
                String trimmed = sentence.trim();
                if (!trimmed.isEmpty()) items.add(trimmed);
            }
        }
        return items;
    }

    private static List<TreatmentStep> toTreatmentSteps(String text) {
        List<String> bullets = toBulletList(text);
        List<TreatmentStep> steps = new ArrayList<>();
        int i = 1;
        for (String bullet : bullets) {
            steps.add(new TreatmentStep(i++, bullet));
        }
        return steps;
    }
}
