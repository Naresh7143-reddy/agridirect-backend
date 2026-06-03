package com.agridirect;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Initialises Firebase using the real service-account JSON during tests,
 * but only if it hasn't been initialised already.
 * This replaces the main FirebaseConfig @PostConstruct to avoid double-init.
 */
@TestConfiguration
public class TestFirebaseConfig {

    @Bean(name = "testFirebaseInit")
    public String initFirebase() {
        if (FirebaseApp.getApps().isEmpty()) {
            try {
                InputStream sa = new ClassPathResource("firebase-service-account.json").getInputStream();
                FirebaseOptions opts = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(sa))
                        .build();
                FirebaseApp.initializeApp(opts);
            } catch (IOException e) {
                // If service account not available in test classpath, skip init gracefully
                System.out.println("[TestFirebaseConfig] Firebase init skipped: " + e.getMessage());
            }
        }
        return "firebase-initialised";
    }
}
