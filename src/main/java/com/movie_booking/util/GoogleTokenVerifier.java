
package com.movie_booking.util;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

public class GoogleTokenVerifier {

    private static final String CLIENT_ID ="272663743548-itpthoqm2u1uik1kfe77aa13ts49omva.apps.googleusercontent.com";

    private static final GoogleIdTokenVerifier VERIFIER =
            new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(CLIENT_ID))
                    .build();

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

