package com.example.test;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class GameInfoService {

    private final SteamApiClient steamApiClient;
    private final String defaultSteamId;

    public GameInfoService(SteamApiClient steamApiClient,
                           @Value("${steam.default.user.id}") String defaultSteamId) {
        this.steamApiClient = steamApiClient;
        this.defaultSteamId = defaultSteamId;
    }

    private Optional<SteamApiClient.AppData> fetchDetails(String gameName) {
        return steamApiClient.searchAppId(gameName)
                .flatMap(steamApiClient::getAppDetails);
    }

    @Tool(description = "Returns the developer of a given Steam game by name")
    public String getGameDeveloper(String gameName) {
        return fetchDetails(gameName)
                .filter(d -> d.developers() != null && !d.developers().isEmpty())
                .map(d -> String.join(", ", d.developers()))
                .orElse("Developer not found for: " + gameName);
    }

    @Tool(description = "Returns the current price of a given Steam game, including any active discount")
    public String getGamePrice(String gameName) {
        return fetchDetails(gameName)
                .map(d -> {
                    if (d.priceOverview() == null) return "Free to Play";
                    int discount = d.priceOverview().discountPercent();
                    return discount > 0
                            ? d.priceOverview().finalFormatted() + " (" + discount + "% off)"
                            : d.priceOverview().finalFormatted();
                })
                .orElse("Price not found for: " + gameName);
    }

    @Tool(description = "Returns the genres of a given Steam game")
    public String getGameGenres(String gameName) {
        return fetchDetails(gameName)
                .filter(d -> d.genres() != null && !d.genres().isEmpty())
                .map(d -> d.genres().stream()
                        .map(SteamApiClient.Genre::description)
                        .collect(Collectors.joining(", ")))
                .orElse("Genres not found for: " + gameName);
    }

    @Tool(description = "Returns a short description of a given Steam game")
    public String getGameDescription(String gameName) {
        return fetchDetails(gameName)
                .map(d -> d.shortDescription() != null ? d.shortDescription() : "No description available")
                .orElse("Description not found for: " + gameName);
    }

    @Tool(description = "Returns the release date of a given Steam game")
    public String getGameLaunchDate(String gameName) {
        return fetchDetails(gameName)
                .map(d -> {
                    if (d.releaseDate() == null) return "Release date unknown";
                    return d.releaseDate().comingSoon() ? "Coming soon" : d.releaseDate().date();
                })
                .orElse("Release date not found for: " + gameName);
    }

    @Tool(description = "Returns the current number of players online for a given Steam game")
    public String getCurrentPlayerCount(String gameName) {
        return steamApiClient.searchAppId(gameName)
                .flatMap(steamApiClient::getCurrentPlayerCount)
                .map(count -> String.format("%,d players currently online in %s", count, gameName))
                .orElse("Player count not available for: " + gameName);
    }

    @Tool(description = "Returns the 3 most recent news articles or patch notes for a given Steam game")
    public String getLatestNews(String gameName) {
        return steamApiClient.searchAppId(gameName)
                .flatMap(steamApiClient::getLatestNews)
                .filter(items -> !items.isEmpty())
                .map(items -> items.stream()
                        .map(n -> "• " + n.title() + "\n  " + n.url())
                        .collect(Collectors.joining("\n\n")))
                .orElse("No news found for: " + gameName);
    }

    @Tool(description = "Returns hours played for a specific game in the user's Steam library")
    public String getHoursPlayed(String gameName) {
        return steamApiClient.getOwnedGames(defaultSteamId)
                .flatMap(games -> games.stream()
                        .filter(g -> g.name() != null && g.name().equalsIgnoreCase(gameName))
                        .findFirst())
                .map(g -> gameName + ": " + (g.playtimeForever() / 60) + " hours and " + (g.playtimeForever() % 60) + " minutes played")
                .orElse("Game not found in library: " + gameName);
    }

    @Tool(description = "Returns the unlocked achievements for a specific game in the user's Steam library")
    public String getGameAchievements(String gameName) {
        return steamApiClient.searchAppId(gameName)
                .flatMap(appId -> steamApiClient.getPlayerAchievements(defaultSteamId, appId))
                .map(achievements -> {
                    long unlocked = achievements.stream().filter(a -> a.achieved() == 1).count();
                    String unlockedNames = achievements.stream()
                            .filter(a -> a.achieved() == 1)
                            .map(SteamDtos.Achievement::name)
                            .collect(Collectors.joining(", "));
                    return unlocked + "/" + achievements.size() + " achievements unlocked"
                            + (unlockedNames.isEmpty() ? "" : "\nUnlocked: " + unlockedNames);
                })
                .orElse("Could not retrieve achievements for: " + gameName);
    }

    @Tool(description = "Returns all games in the user's Steam library, sorted alphabetically")
    public String getAllGames() {
        return steamApiClient.getOwnedGames(defaultSteamId)
                .map(games -> games.stream()
                        .filter(g -> g.name() != null)
                        .map(SteamDtos.OwnedGame::name)
                        .sorted(Comparator.comparing(GameInfoService::sortKey))
                        .collect(Collectors.joining("\n")))
                .orElse("Could not retrieve game library");
    }

    private static String sortKey(String name) {
        return name.replaceFirst("(?i)^(the|a|an)\\s+", "");
    }

    @Tool(description = "Returns the total playtime across all games in the user's Steam library")
    public String getTotalPlaytime() {
        return steamApiClient.getOwnedGames(defaultSteamId)
                .map(games -> {
                    int total = games.stream().mapToInt(SteamDtos.OwnedGame::playtimeForever).sum();
                    return String.format("Total playtime: %d hours and %d minutes", total / 60, total % 60);
                })
                .orElse("Could not retrieve game library");
    }

    @Tool(description = "Returns the most played Steam game of the configured user")
    public String getMostPlayedGame() {
        return steamApiClient.getMostPlayedGame(defaultSteamId)
                .map(g -> g.name() + " with " + (g.playtimeForever() / 60) + " hours played")
                .orElse("Could not retrieve game library");
    }

    @Tool(description = "Returns the top X most played games from the user's Steam library, sorted by hours played descending. Includes free-to-play games.")
    public String getTopGamesByHoursPlayed(int count) {
        AtomicInteger rank = new AtomicInteger(1);
        return steamApiClient.getOwnedGames(defaultSteamId)
                .map(games -> games.stream()
                        .filter(g -> g.name() != null && g.playtimeForever() > 0)
                        .sorted(Comparator.comparingInt(SteamDtos.OwnedGame::playtimeForever).reversed())
                        .limit(count)
                        .map(g -> String.format("%d. %s — %dh %dm",
                                rank.getAndIncrement(),
                                g.name(),
                                g.playtimeForever() / 60,
                                g.playtimeForever() % 60))
                        .collect(Collectors.joining("\n")))
                .filter(result -> !result.isBlank())
                .orElse("Could not retrieve game library");
    }

    @Tool(description = "Returns all available info about a Steam game: developer, price, genres, description, and release date")
    public String getGameInfo(String gameName) {
        return fetchDetails(gameName)
                .map(d -> {
                    String developer = d.developers() != null ? String.join(", ", d.developers()) : "Unknown";
                    String price = d.priceOverview() == null ? "Free to Play"
                            : d.priceOverview().finalFormatted()
                            + (d.priceOverview().discountPercent() > 0 ? " (" + d.priceOverview().discountPercent() + "% off)" : "");
                    String genres = d.genres() != null ? d.genres().stream()
                            .map(SteamApiClient.Genre::description)
                            .collect(Collectors.joining(", ")) : "Unknown";
                    String description = d.shortDescription() != null ? d.shortDescription() : "No description";
                    String releaseDate = d.releaseDate() != null
                            ? (d.releaseDate().comingSoon() ? "Coming soon" : d.releaseDate().date())
                            : "Unknown";
                    return "Developer: " + developer + "\n"
                            + "Price: " + price + "\n"
                            + "Genres: " + genres + "\n"
                            + "Release Date: " + releaseDate + "\n"
                            + "Description: " + description;
                })
                .orElse("Game not found: " + gameName);
    }
}