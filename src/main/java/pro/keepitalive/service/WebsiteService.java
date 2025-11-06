package pro.keepitalive.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import pro.keepitalive.Website;
import pro.keepitalive.repository.WebsiteRepository;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class WebsiteService {

    private final WebsiteRepository websiteRepository;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public List<Website> getAllWebsites() {
        return websiteRepository.findAll();
    }

    public Website addWebsite(String url) {
        var website = new Website();
        website.setUrl(url);
        website.setStatus("PENDING");
        return websiteRepository.save(website);
    }

    public void checkWebsite(Website website) {
        var url = website.getUrl();

        if (url == null || url.isBlank()) {
            website.setStatus("DOWN - Empty URL");
            website.setLastChecked(LocalDateTime.now());
            websiteRepository.save(website);
            return;
        }

        var fullUrl = url;
        if (!fullUrl.toLowerCase().startsWith("http://") && !fullUrl.toLowerCase().startsWith("https://")) {
            fullUrl = "https://" + fullUrl;
        }

        try {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(fullUrl))
                    .GET()
                    .build();

            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                website.setStatus("UP");
            } else {
                website.setStatus("DOWN - " + response.statusCode());
            }

        } catch (IOException | InterruptedException | IllegalArgumentException e) {
            website.setStatus("DOWN - " + e.getClass().getSimpleName());
        }

        website.setLastChecked(LocalDateTime.now());
        websiteRepository.save(website);
    }

    public void checkAllWebsites() {
        var websites = getAllWebsites();
        for (Website website : websites) {
            checkWebsite(website);
        }
    }

    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.SECONDS, initialDelay = 5)
    public void scheduleAllWebsitesCheck() {
        System.out.println("Running scheduled check...");
        checkAllWebsites();
        System.out.println("Scheduled check finished.");
    }
}