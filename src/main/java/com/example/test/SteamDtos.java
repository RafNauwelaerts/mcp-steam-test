package com.example.test;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SteamDtos {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SearchResponse(int total, List<SearchItem> items) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SearchItem(int id, String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AppDetailEntry(boolean success, AppData data) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AppData(
            List<String> developers,
            @JsonProperty("short_description") String shortDescription,
            @JsonProperty("release_date") ReleaseDate releaseDate,
            @JsonProperty("price_overview") PriceOverview priceOverview,
            List<Genre> genres
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ReleaseDate(
            @JsonProperty("coming_soon") boolean comingSoon,
            String date
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PriceOverview(
            @JsonProperty("final_formatted") String finalFormatted,
            @JsonProperty("discount_percent") int discountPercent
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Genre(String id, String description) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PlayerCountResponse(PlayerCountData response) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PlayerCountData(@JsonProperty("player_count") int playerCount) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NewsResponse(AppNews appnews) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AppNews(List<NewsItem> newsitems) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NewsItem(String title, String url, String contents) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OwnedGamesResponse(OwnedGamesData response) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OwnedGamesData(
            @JsonProperty("game_count") int gameCount,
            List<OwnedGame> games
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OwnedGame(
            int appid,
            String name,
            @JsonProperty("playtime_forever") int playtimeForever
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PlayerAchievementsResponse(PlayerStats playerstats) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PlayerStats(List<Achievement> achievements, boolean success) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Achievement(String apiname, int achieved, String name, String description) {}
}