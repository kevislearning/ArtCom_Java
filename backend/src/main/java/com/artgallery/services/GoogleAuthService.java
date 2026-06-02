package com.artgallery.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Collections;

@Service
public class GoogleAuthService {

    @Value("${google.client-id}")
    private String googleClientId;

    public static class GoogleUserInfo {
        public final String email;
        public final String name;
        public final String pictureUrl;

        public GoogleUserInfo(String email, String name, String pictureUrl) {
            this.email = email;
            this.name = name;
            this.pictureUrl = pictureUrl;
        }
    }

    public GoogleUserInfo verifyToken(String idTokenString) {
        try {
            NetHttpTransport transport = new NetHttpTransport();
            GsonFactory jsonFactory = GsonFactory.getDefaultInstance();

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();

                String email = payload.getEmail();
                String name = (String) payload.get("name");
                String pictureUrl = (String) payload.get("picture");

                return new GoogleUserInfo(email, name, pictureUrl);
            }
        } catch (Exception e) {
            System.err.println("[GoogleAuthService] Token verification failed: " + e.getMessage());
        }
        return null;
    }
}
