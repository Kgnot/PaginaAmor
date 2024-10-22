package com.web.PaginaTY.rest;

import com.web.PaginaTY.service.AlbumService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/albums")
public class GooglePhotosController {

    private final OAuth2AuthorizedClientService authorizedClientService;
    @Autowired
    private AlbumService albumService;

    public GooglePhotosController(OAuth2AuthorizedClientService authorizedClientService, @Autowired AlbumService albumService) {
        this.authorizedClientService = authorizedClientService;
        this.albumService = albumService;
    }


    @GetMapping("/photos/{date}")
    public List<Map<String, Object>> getPhotosByDate(
            @PathVariable String date,
            HttpSession session
    ) {
        String accessToken = (String) session.getAttribute("accessToken");
        if (accessToken != null) {
            return albumService.getPhotos(date);
        }
        return List.of();
    }
}
