package com.mapic.backend.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
@Slf4j
public class ImageDownloader {
    
    private static final String UPLOAD_DIR = "uploads/moments/";
    private static final int MAX_REDIRECTS = 5;
    
    /**
     * Download image from URL and save to local storage.
     * Follows HTTP redirects and sets a browser-like User-Agent.
     * Returns the filename if successful, null otherwise.
     */
    public String downloadImage(String imageUrl, String filename) {
        try {
            // Create upload directory if not exists
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            Path filePath = uploadPath.resolve(filename);
            
            // Skip if file already exists
            if (Files.exists(filePath)) {
                log.debug("Image already exists: {}", filename);
                return filename;
            }
            
            // Download with redirect following
            String downloadUrl = imageUrl;
            int redirectCount = 0;
            
            while (redirectCount < MAX_REDIRECTS) {
                HttpURLConnection connection = (HttpURLConnection) new URL(downloadUrl).openConnection();
                connection.setRequestProperty("User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120 Safari/537.36");
                connection.setRequestProperty("Accept", "image/webp,image/apng,image/*,*/*");
                connection.setConnectTimeout(10_000);
                connection.setReadTimeout(30_000);
                connection.setInstanceFollowRedirects(false); // handle manually for http->https

                int responseCode = connection.getResponseCode();
                
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (InputStream in = connection.getInputStream()) {
                        Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
                        log.info("Downloaded image: {} (from {})", filename, downloadUrl);
                        return filename;
                    }
                } else if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP
                        || responseCode == HttpURLConnection.HTTP_MOVED_PERM
                        || responseCode == 307 || responseCode == 308) {
                    String location = connection.getHeaderField("Location");
                    if (location == null) {
                        log.error("Redirect with no Location header for {}", filename);
                        return null;
                    }
                    downloadUrl = location;
                    redirectCount++;
                    log.debug("Following redirect ({}) to {}", redirectCount, downloadUrl);
                } else {
                    log.error("HTTP {} downloading image {}", responseCode, filename);
                    return null;
                }
            }
            
            log.error("Too many redirects downloading image {}", filename);
            return null;
            
        } catch (IOException e) {
            log.error("Failed to download image {}: {}", filename, e.getMessage());
            return null;
        }
    }
    
    /**
     * Get a Vietnam-themed image URL using loremflickr.com.
     * Uses Vietnam-related keywords to return relevant photos.
     * The 'lock' parameter ensures the same image for the same index.
     */
    public String getPlaceholderImageUrl(int seed) {
        // Keywords cycle through Vietnam locations/themes for varied results
        String[] vietnamKeywords = {
            "vietnam,landscape",
            "hanoi,vietnam",
            "hochiminh,vietnam",
            "halong,bay",
            "danang,vietnam",
            "hoi-an,vietnam",
            "sapa,vietnam",
            "mekong,vietnam",
            "hue,vietnam",
            "nhatrang,beach",
            "dalat,vietnam",
            "phuquoc,island",
            "vietnam,temple",
            "vietnam,pagoda",
            "vietnam,river",
            "vietnam,mountain",
            "vietnam,beach",
            "vietnam,street",
            "vietnam,market",
            "vietnam,food"
        };
        String keyword = vietnamKeywords[seed % vietnamKeywords.length];
        // loremflickr returns real Flickr photos matching the tag
        // /lock/{seed} makes it deterministic per seed
        return String.format("https://loremflickr.com/800/800/%s/all?lock=%d", keyword, seed);
    }
}
