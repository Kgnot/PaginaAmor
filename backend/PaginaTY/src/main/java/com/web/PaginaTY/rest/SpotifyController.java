package com.web.PaginaTY.rest;


import com.nimbusds.oauth2.sdk.AuthorizationCode;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;

@RestController
@RequestMapping("/spotify")
public class SpotifyController {

    private final String albumCode = "6huCR9DJ8nCe7TGEyZguXM"; // Código del Album
    private String code ="";
    private static final URI redirectUri = SpotifyHttpManager.makeUri("http://localhost:3000/spotify/get-user-code");

    private static final SpotifyApi spotifyApi = new SpotifyApi.Builder()
            .setClientId("b13bc2eb276040da8334a89b2f1060be")
            .setClientSecret("0afa9ae5a92649f4b324d2815a497102")
            .setRedirectUri(redirectUri)
            .build();

    @GetMapping("login")
    @ResponseBody
    public RedirectView spotifyLogin() {
        AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
                .scope("user-read-private, user-read-email, user-top-read, user-modify-playback-state, user-read-playback-state, streaming")
                .show_dialog(true)
                .build();
        final URI uri = authorizationCodeUriRequest.execute();
       return new RedirectView(uri.toString());
    }

        @GetMapping(value = "get-user-code")
    public String getSpotifyUserCode(@RequestParam("code") String userCode, HttpServletResponse response, HttpSession session) throws IOException {
        code = userCode;
        AuthorizationCodeRequest authorizationCodeRequest = spotifyApi.authorizationCode(code).build();

        try {
            final AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRequest.execute();
            //To token
            spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
            spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
            session.setAttribute("accessTokenSpotify",authorizationCodeCredentials.getAccessToken());


        } catch (IOException | SpotifyWebApiException | org.apache.hc.core5.http.ParseException e) {
            System.out.println("error todo largo de spotify: " + e);
        }
        response.sendRedirect("http://localhost:8080/calendar");
        return spotifyApi.getAccessToken();
    }
    @GetMapping("api/check-spotify-login")
    public ResponseEntity<Object> checkSpotifyLogin(HttpSession session){
        Object accessToken = session.getAttribute("accessTokenSpotify");
        if (accessToken != null) {
            return ResponseEntity.ok(Collections.singletonMap("loggedIn", true));
        } else {
            return ResponseEntity.ok(Collections.singletonMap("loggedIn", false));
        }
    }

    @GetMapping("api/token")
    public ResponseEntity<Object> accessTokenSpotify(HttpSession session){
        return ResponseEntity.ok(session.getAttribute("accessTokenSpotify"));
    }

    // To get an album:
    @GetMapping("api/playlist")
    public ResponseEntity<Object> getPlaylistDetails(HttpSession session) {
        String accessToken = (String) session.getAttribute("accessTokenSpotify");

        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated with Spotify");
        }
        spotifyApi.setAccessToken(accessToken); // Set the access token to the API instance
        System.out.println(accessToken);
        try {
            final Playlist playlist = spotifyApi.getPlaylist(albumCode).build().execute(); // Cambia aquí a getPlaylist
            return ResponseEntity.ok(playlist);
        } catch (IOException | SpotifyWebApiException | org.apache.hc.core5.http.ParseException e) {
            System.out.println("Error getting playlist details: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving playlist details");
        }
    }


}
