
package com.movie_booking.util;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.movie_booking.config.AppConfig;

public class GoogleTokenVerifier {

    private static final String CLIENT_ID = AppConfig.getRequired("GOOGLE_CLIENT_ID");
    private static final GoogleIdTokenVerifier VERIFIER =
            new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(CLIENT_ID))
                    .build();

    public static String getClientId() {
        return CLIENT_ID;
    }

    public static GoogleIdToken verify(String credential)
            throws GeneralSecurityException, IOException {

        if (credential == null || credential.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Google credential cannot be empty.");
        }

        GoogleIdToken idToken = VERIFIER.verify(credential);

        if (idToken == null) {
            throw new IllegalArgumentException(
                    "Invalid Google ID token.");
        }

        return idToken;
    }
}

