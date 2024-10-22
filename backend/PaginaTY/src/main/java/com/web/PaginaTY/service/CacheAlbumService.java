// Documentacion: https://developers.google.com/photos/library/reference/rest/v1/mediaItems/search

package com.web.PaginaTY.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CacheAlbumService {

    private RestTemplate restTemplate;

    @Autowired
    public CacheAlbumService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    // Busca por Dia
    public List<Map<String, Object>> getPhotos(String date) {
        List<Map<String, Object>> album = getFullPhotosAlbum(null, null); // Cache ya cargada, no se necesita token

        return album.stream()
                .filter(photo -> {
                    String creationDate = (String) ((Map<String, Object>) photo.get("mediaMetadata")).get("creationTime");
                    return creationDate.startsWith(date); // Compara solo la parte "yyyy-MM-dd"
                })
                .toList();
    }

    //Que me guarde todos el album :
    @Cacheable(value = "albumCache", key="'fullAlbum'", unless = "#result == null || #result.isEmpty()")
    public List<Map<String, Object>> getFullPhotosAlbum(String accessToken, String album) {
        String url = "https://photoslibrary.googleapis.com/v1/mediaItems:search";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        String pageToken = null;
        List<Map<String, Object>> allMediaItems = new ArrayList<>();
        do {
            HttpEntity<Map<String, Object>> httpEntity = getMapHttpEntity(album, headers, pageToken);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, Map.class);
            List<Map<String, Object>> mediaItems = (List<Map<String, Object>>) response.getBody().get("mediaItems");
            if (mediaItems == null || mediaItems.isEmpty()) {
                break;
            }
            allMediaItems.addAll(mediaItems);
            pageToken = (String) response.getBody().get("nextPageToken");
        } while (pageToken != null);
        System.out.println("Guardando en cache: " + allMediaItems.size() + " elementos");
        return allMediaItems;
    }

    private static HttpEntity<Map<String, Object>> getMapHttpEntity(String album, HttpHeaders headers, String pageToken) {
        //aun no usamos darte pero sera de suma importancia
        Map<String, Object> body = new HashMap<>();
        body.put("albumId", album); // Me toca solo album porque no puedo usar filtros
        body.put("pageSize", 100);
        if (pageToken != null) {
            body.put("pageToken", pageToken);
        }
        // Luego usamos un HTTPEntity para manejar el cabezado y cuerpo de la solicitud:
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(body, headers); // el primer parametros siempre es l body y el segundo headers
        return httpEntity;
    }
}
