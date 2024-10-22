package com.web.PaginaTY.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AlbumService {

    private final CacheAlbumService cacheAlbumService;

    @Autowired
    public AlbumService( CacheAlbumService cacheAlbumService){
        this.cacheAlbumService = cacheAlbumService;
    }

    public List<Map<String, Object>> getPhotos(String date) {
        List<Map<String, Object>> album = cacheAlbumService.getFullPhotosAlbum(null, null); // Cache ya cargada, no se necesita token

        return album.stream()
                .filter(photo -> {
                    String creationDate = (String) ((Map<String, Object>) photo.get("mediaMetadata")).get("creationTime");
                    return creationDate.startsWith(date); // Compara solo la parte "yyyy-MM-dd"
                })
                .toList();
    }
}
