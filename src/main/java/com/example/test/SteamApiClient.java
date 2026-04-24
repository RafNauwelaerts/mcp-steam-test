package com.example.test;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class SteamApiClient {

    private final RestClient storeClient;
    private final RestClient apiClient;
    private final String apiKey;

    public SteamApiClient(@Value("${steam.api.key}") String apiKey) {
        this.storeClient = RestClient.create("https://store.steampowered.com");
        this.apiClient = RestClient.create("https://api.steampowered.com");
        this.apiKey = apiKey;
    }

    public Optional<Integer> searchAppId(String gameName) {
        try {
            SearchResponse response = storeClient.get()
                    .uri("/api/storesearch/?term={name}&l=english&cc=US", gameName)
                    .retrieve()
                    .body(SearchResponse.class);
            if (response == null || response.items() == null || response.items().isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(response.items().get(0).id());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<AppData> getAppDetails(int appId) {
        try {
            Map<String, AppDetailEntry> response = storeClient.get()
                    .uri("/api/appdetails?appids={appId}", appId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            if (response == null) return Optional.empty();
            AppDetailEntry entry = response.get(String.valueOf(appId));
            if (entry == null || !entry.success() || entry.data() == null) {
                return Optional.empty();
            }
            return Optional.of(entry.data());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<Integer> getCurrentPlayerCount(int appId) {
        try {
            PlayerCountResponse response = apiClient.get()
                    .uri("/ISteamUserStats/GetNumberOfCurrentPlayers/v1/?appid={appId}", appId)
                    .retrieve()
                    .body(PlayerCountResponse.class);
            if (response == null || response.response() == null) return Optional.empty();
            return Optional.of(response.response().playerCount());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<List<NewsItem>> getLatestNews(int appId) {
        try {
            NewsResponse response = apiClient.get()
                    .uri("/ISteamNews/GetNewsForApp/v2/?appid={appId}&count=3&maxlength=300&format=json", appId)
                    .retrieve()
                    .body(NewsResponse.class);
            if (response == null || response.appnews() == null || response.appnews().newsitems() == null) {
                return Optional.empty();
            }
            return Optional.of(response.appnews().newsitems());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<List<SteamDtos.OwnedGame>> getOwnedGames(String steamId) {
        try {
            SteamDtos.OwnedGamesResponse response = apiClient.get()
                    .uri("/IPlayerService/GetOwnedGames/v1/?key={key}&steamid={steamId}&include_appinfo=true",
                            apiKey, steamId)
                    .retrieve()
                    .body(SteamDtos.OwnedGamesResponse.class);
            if (response == null || response.response() == null || response.response().games() == null) {
                return Optional.empty();
            }
            return Optional.of(response.response().games());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<SteamDtos.OwnedGame> getMostPlayedGame(String steamId) {
        return getOwnedGames(steamId)
                .flatMap(games -> games.stream()
                        .max(Comparator.comparingInt(SteamDtos.OwnedGame::playtimeForever)));
    }

    public Optional<List<SteamDtos.Achievement>> getPlayerAchievements(String steamId, int appId) {
        try {
            SteamDtos.PlayerAchievementsResponse response = apiClient.get()
                    .uri("/ISteamUserStats/GetPlayerAchievements/v1/?key={key}&steamid={steamId}&appid={appId}",
                            apiKey, steamId, appId)
                    .retrieve()
                    .body(SteamDtos.PlayerAchievementsResponse.class);
            if (response == null || response.playerstats() == null
                    || !response.playerstats().success()
                    || response.playerstats().achievements() == null) {
                return Optional.empty();
            }
            return Optional.of(response.playerstats().achievements());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // --- Store API records ---

    @JsonIgnoreProperties(ignoreUnknown = true)
    record SearchResponse(int total, List<SearchItem> items) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record SearchItem(int id, String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record AppDetailEntry(boolean success, AppData data) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record AppData(
            List<String> developers,
            @JsonProperty("short_description") String shortDescription,
            @JsonProperty("release_date") ReleaseDate releaseDate,
            @JsonProperty("price_overview") PriceOverview priceOverview,
            List<Genre> genres
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ReleaseDate(
            @JsonProperty("coming_soon") boolean comingSoon,
            String date
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record PriceOverview(
            @JsonProperty("final_formatted") String finalFormatted,
            @JsonProperty("discount_percent") int discountPercent
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Genre(String id, String description) {}

    // --- Steam Web API records ---

    @JsonIgnoreProperties(ignoreUnknown = true)
    record PlayerCountResponse(PlayerCountData response) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record PlayerCountData(@JsonProperty("player_count") int playerCount) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record NewsResponse(AppNews appnews) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record AppNews(List<NewsItem> newsitems) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record NewsItem(String title, String url, String contents) {}
}