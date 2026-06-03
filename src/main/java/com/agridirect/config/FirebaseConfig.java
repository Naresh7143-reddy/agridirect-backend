package com.agridirect.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initFirebase() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            InputStream serviceAccount = resolveServiceAccount();
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            FirebaseApp.initializeApp(options);
        }
    }

    /**
     * Loads Firebase credentials in this priority order:
     * 1. FIREBASE_SERVICE_ACCOUNT_BASE64 env var  (used on Render / production)
     * 2. firebase-service-account.json in classpath (used locally)
     */
    private InputStream resolveServiceAccount() throws IOException {
        String base64 = System.getenv("FIREBASE_SERVICE_ACCOUNT_BASE64");
        if (base64 != null && !base64.isBlank()) {
            byte[] decoded = Base64.getDecoder().decode(base64.trim());
            return new ByteArrayInputStream(decoded);
        }
        return new ClassPathResource("firebase-service-account.json").getInputStream();
    }
}
