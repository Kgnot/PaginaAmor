package com.web.PaginaTY.rest;

import com.web.PaginaTY.service.CacheAlbumService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/")
public class AuthGoogleController {

    private final OAuth2AuthorizedClientService authorizedClientService;
    //Tenemos que cargar el album al iniciar la pagina:
    private final CacheAlbumService cacheAlbumService;
    private final String album = "AD47FgzYIO4mgCR--6NxHOMr7KKZlxG2ZXNVh1sCGYKbyeKD-LlJKYCvqluaauwyMfD2RtCunohh"; // este es el album asociado

    public AuthGoogleController(OAuth2AuthorizedClientService authorizedClientService, @Autowired CacheAlbumService cacheAlbumService){
        this.authorizedClientService = authorizedClientService;
        this.cacheAlbumService = cacheAlbumService;
    }

    @GetMapping
    public RedirectView authUser(OAuth2AuthenticationToken oAuth2AuthenticationToken, HttpSession session) {
        String token =  getTokenValue(oAuth2AuthenticationToken);
        session.setAttribute("accessToken",token);
        // Luego con el token:
        cacheAlbumService.getFullPhotosAlbum(token,album);
        return new RedirectView( "http://localhost:8080/inicio");
    }

    private String getTokenValue(OAuth2AuthenticationToken oAuth2AuthenticationToken) {
        OAuth2AuthorizedClient client = authorizedClientService
                .loadAuthorizedClient(
                        oAuth2AuthenticationToken.getAuthorizedClientRegistrationId(),
                        oAuth2AuthenticationToken.getName());

        return client.getAccessToken().getTokenValue();
    }

}
