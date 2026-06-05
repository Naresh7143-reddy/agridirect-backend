package com.agridirect.config;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Serves /health, /privacy, and /terms as plain HTML pages.
 * Required for Google OAuth consent screen brand verification.
 */
@RestController
public class LegalController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "app", "AgriDirect"));
    }

    @GetMapping(value = "/privacy", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> privacy() {
        String html = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8"/>
              <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
              <title>Privacy Policy – AgriDirect</title>
              <style>
                body { font-family: Arial, sans-serif; max-width: 800px; margin: 40px auto; padding: 0 20px; color: #333; }
                h1 { color: #2E7D32; } h2 { color: #388E3C; margin-top: 30px; }
                p { line-height: 1.7; } footer { margin-top: 60px; color: #999; font-size: 13px; }
              </style>
            </head>
            <body>
              <h1>Privacy Policy</h1>
              <p><strong>Last updated: June 2025</strong></p>
              <p>AgriDirect ("we", "us", or "our") operates the AgriDirect mobile application. This page informs you of our policies regarding the collection, use, and disclosure of personal data.</p>

              <h2>1. Information We Collect</h2>
              <p>We collect the following types of information:</p>
              <ul>
                <li><strong>Phone number</strong> – for account creation and OTP verification via Firebase Authentication.</li>
                <li><strong>Name and profile details</strong> – provided during registration.</li>
                <li><strong>Location data</strong> – used for delivery address and farm location.</li>
                <li><strong>Payment information</strong> – processed securely by Razorpay. We do not store card details.</li>
                <li><strong>Device token</strong> – for push notifications via Firebase Cloud Messaging.</li>
              </ul>

              <h2>2. How We Use Your Information</h2>
              <ul>
                <li>To create and manage your account</li>
                <li>To process orders and payments</li>
                <li>To send order status notifications</li>
                <li>To connect buyers with farmers</li>
                <li>To improve our services</li>
              </ul>

              <h2>3. Data Sharing</h2>
              <p>We do not sell your personal data. We share data only with:</p>
              <ul>
                <li><strong>Firebase (Google)</strong> – authentication and notifications</li>
                <li><strong>Razorpay</strong> – payment processing</li>
                <li><strong>Cloudinary</strong> – image storage</li>
              </ul>

              <h2>4. Data Security</h2>
              <p>We implement industry-standard security measures including JWT-based authentication, HTTPS encryption, and secure payment processing.</p>

              <h2>5. Your Rights</h2>
              <p>You may request deletion of your account and associated data by contacting us at support@agridirect.app.</p>

              <h2>6. Children's Privacy</h2>
              <p>Our service is not directed to children under 13. We do not knowingly collect data from children.</p>

              <h2>7. Contact Us</h2>
              <p>Email: support@agridirect.app</p>

              <footer>© 2025 AgriDirect. All rights reserved.</footer>
            </body>
            </html>
            """;
        return ResponseEntity.ok(html);
    }

    @GetMapping(value = "/terms", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> terms() {
        String html = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8"/>
              <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
              <title>Terms of Service – AgriDirect</title>
              <style>
                body { font-family: Arial, sans-serif; max-width: 800px; margin: 40px auto; padding: 0 20px; color: #333; }
                h1 { color: #2E7D32; } h2 { color: #388E3C; margin-top: 30px; }
                p { line-height: 1.7; } footer { margin-top: 60px; color: #999; font-size: 13px; }
              </style>
            </head>
            <body>
              <h1>Terms of Service</h1>
              <p><strong>Last updated: June 2025</strong></p>
              <p>By using AgriDirect, you agree to these terms. Please read them carefully.</p>

              <h2>1. Acceptance of Terms</h2>
              <p>By downloading or using the AgriDirect app, you agree to be bound by these Terms of Service.</p>

              <h2>2. Use of Service</h2>
              <ul>
                <li>You must be at least 18 years old to use this service.</li>
                <li>You are responsible for maintaining the security of your account.</li>
                <li>You agree not to misuse the platform for fraudulent transactions.</li>
              </ul>

              <h2>3. For Farmers</h2>
              <ul>
                <li>You must provide accurate product information and pricing.</li>
                <li>You are responsible for fulfilling orders you accept.</li>
                <li>AgriDirect reserves the right to verify your farm credentials.</li>
              </ul>

              <h2>4. For Buyers</h2>
              <ul>
                <li>Payments are processed securely through Razorpay.</li>
                <li>Refunds are subject to our Refund Policy.</li>
                <li>You agree to provide accurate delivery addresses.</li>
              </ul>

              <h2>5. Prohibited Activities</h2>
              <p>You may not use AgriDirect to engage in illegal activities, post false information, or harm other users.</p>

              <h2>6. Limitation of Liability</h2>
              <p>AgriDirect is a platform connecting farmers and buyers. We are not liable for disputes between parties beyond our stated refund policy.</p>

              <h2>7. Changes to Terms</h2>
              <p>We may update these terms at any time. Continued use of the app constitutes acceptance of updated terms.</p>

              <h2>8. Contact</h2>
              <p>Email: support@agridirect.app</p>

              <footer>© 2025 AgriDirect. All rights reserved.</footer>
            </body>
            </html>
            """;
        return ResponseEntity.ok(html);
    }
}
