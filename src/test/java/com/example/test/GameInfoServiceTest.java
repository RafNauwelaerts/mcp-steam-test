package com.example.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GameInfoServiceTest {

    @Mock
    SteamApiClient steamApiClient;

    @Mock
    AnthropicClient anthropicClient;

    SteamApiClient.AppData appData;
    SteamApiClient.AppData badAppData;
    SteamDtos.OwnedGamesData ownedGamesData;
    GameInfoService service;

    @BeforeEach
    void setUp() {
        service = new GameInfoService(steamApiClient, anthropicClient, "default_steam_id");
        appData = new SteamApiClient.AppData(
                "Elden Ring",
                List.of("FromSoftware"),
                "The critically acclaimed fantasy action RPG — rise as the Tarnished, wield the power of the Elden Ring, and become an Elden Lord in the Lands Between.",
                new SteamApiClient.ReleaseDate(false, "February 24, 2022"),
                new SteamApiClient.PriceOverview("€59.99", 50),
                List.of(new SteamApiClient.Genre("1","Action"), new SteamApiClient.Genre("1","RPG"))
        );

        badAppData = new SteamApiClient.AppData(
                "Bad game",
                List.of(),
                null,
                new SteamApiClient.ReleaseDate(true, "February 24, 3000"),
                null,
                null
        );

        ownedGamesData = new SteamDtos.OwnedGamesData(3, List.of(
                new SteamDtos.OwnedGame(123, "Elden Ring", 680),
                new SteamDtos.OwnedGame(456, "Bad game", 0),
                new SteamDtos.OwnedGame(789, "A short hike", 236)
        ));
    }

    //successfull getGameDeveloper
    @Test
    void getGameDeveloper_returnsDevName(){
        when(steamApiClient.searchAppId("Elden Ring")).thenReturn(Optional.of(1234567890));
        when(steamApiClient.getAppDetails(1234567890)).thenReturn(Optional.of(appData));

        String result = service.getGameDeveloper("Elden Ring");

        assertThat(result).contains("FromSoftware");
    }

    //failed getGameDeveloper for game without dev
    @Test
    void getGameDeveloper_gameWithoutDeveloper_returnsNotFound(){
        when(steamApiClient.searchAppId("Bad game")).thenReturn(Optional.of(987654321));
        when(steamApiClient.getAppDetails(987654321)).thenReturn(Optional.of(badAppData));

        String result = service.getGameDeveloper("Bad game");

        assertThat(result).contains("Developer not found for: Bad game");
    }

    //failed getGameDeveloper for nonexistent game
    @Test
    void getGameDeveloper_gameNotOnSteam_returnsNotFound() {
        when(steamApiClient.searchAppId("Fake game")).thenReturn(Optional.empty());

        String result = service.getGameDeveloper("Fake game");

        assertThat(result).contains("Developer not found for: Fake game");
    }

    //successfully show price and discount getGamePrice
    @Test
    void getGamePrice_returnsGamePriceAndDiscount(){
        when(steamApiClient.searchAppId("Elden Ring")).thenReturn(Optional.of(1234567890));
        when(steamApiClient.getAppDetails(1234567890)).thenReturn(Optional.of(appData));

        String result = service.getGamePrice("Elden Ring");

        assertThat(result).contains("€59.99 (50% off)");
    }

    //successfully show only price getGamePrice
    @Test
    void getGamePrice_returnsGamePrice(){
        appData = new SteamApiClient.AppData(
                "Elden Ring",
                List.of("FromSoftware"),
                "The critically acclaimed fantasy action RPG — rise as the Tarnished, wield the power of the Elden Ring, and become an Elden Lord in the Lands Between.",
                new SteamApiClient.ReleaseDate(false, "February 24, 2022"),
                new SteamApiClient.PriceOverview("€59.99", 0),
                List.of(new SteamApiClient.Genre("1","Action"), new SteamApiClient.Genre("1","RPG"))
        );

        when(steamApiClient.searchAppId("Elden Ring")).thenReturn(Optional.of(1234567890));
        when(steamApiClient.getAppDetails(1234567890)).thenReturn(Optional.of(appData));

        String result = service.getGamePrice("Elden Ring");

        assertThat(result).contains("€59.99");
    }

    //succesfully show game is free to play
    @Test
    void getGamePrice_returnsFreeToPlay(){
        when(steamApiClient.searchAppId("Bad game")).thenReturn(Optional.of(987654321));
        when(steamApiClient.getAppDetails(987654321)).thenReturn(Optional.of(badAppData));

        String result = service.getGamePrice("Bad game");

        assertThat(result).contains("Free to Play");
    }

    //fail to show price for nonexistant game
    @Test
    void getGamePrice_returnsNotFound(){
        when(steamApiClient.searchAppId("Fake game")).thenReturn(Optional.empty());

        String result = service.getGamePrice("Fake game");

        assertThat(result).contains("Price not found for: Fake game");
    }

    //successfully show genres of game
    @Test
    void getGameGenres_returnsGenres(){
        when(steamApiClient.searchAppId("Elden Ring")).thenReturn(Optional.of(1234567890));
        when(steamApiClient.getAppDetails(1234567890)).thenReturn(Optional.of(appData));

        String result = service.getGameGenres("Elden Ring");

        assertThat(result).contains("Action, RPG");

    }

    //fail to show genres of game without genres
    @Test
    void getGameGenres_gameWithoutGenres_returnsNotFound(){
        when(steamApiClient.searchAppId("Bad game")).thenReturn(Optional.of(987654321));
        when(steamApiClient.getAppDetails(987654321)).thenReturn(Optional.of(badAppData));

        String result = service.getGameGenres("Bad game");

        assertThat(result).contains("Genres not found for: Bad game");
    }

    //fail to show genres of non-existent game
    @Test
    void getGameGenres_gameNotOnSteam_returnsNotFound(){
        when(steamApiClient.searchAppId("Fake game")).thenReturn(Optional.empty());

        String result = service.getGameGenres("Fake game");

        assertThat(result).contains("Genres not found for: Fake game");
    }

    //successfully show a short description of the game
    @Test
    void getGameDescription_returnsDescription(){
        when(steamApiClient.searchAppId("Elden Ring")).thenReturn(Optional.of(1234567890));
        when(steamApiClient.getAppDetails(1234567890)).thenReturn(Optional.of(appData));

        String result = service.getGameDescription("Elden Ring");

        assertThat(result).contains("The critically acclaimed fantasy action RPG — rise as the Tarnished, wield the power of the Elden Ring, and become an Elden Lord in the Lands Between.");
    }

    //fail to show Description of game without Description
    @Test
    void getGameDescription_gameWithoutDescription_returnsNotAvailable(){
        when(steamApiClient.searchAppId("Bad game")).thenReturn(Optional.of(987654321));
        when(steamApiClient.getAppDetails(987654321)).thenReturn(Optional.of(badAppData));

        String result = service.getGameDescription("Bad game");

        assertThat(result).contains("No description available");
    }

    //fail to show Description of non-existent game
    @Test
    void getGameDescription_gameNotOnSteam_returnsNotFound(){
        when(steamApiClient.searchAppId("Fake game")).thenReturn(Optional.empty());

        String result = service.getGameDescription("Fake game");

        assertThat(result).contains("Description not found for: Fake game");
    }

    //successfully show the release date of a game
    @Test
    void getGameLaunchDate_returnsLaunchDate(){
        when(steamApiClient.searchAppId("Elden Ring")).thenReturn(Optional.of(1234567890));
        when(steamApiClient.getAppDetails(1234567890)).thenReturn(Optional.of(appData));

        String result = service.getGameLaunchDate("Elden Ring");

        assertThat(result).contains("February 24, 2022");
    }

    //successfully show coming soon
    @Test
    void getGameLaunchDate_returnComingSoon(){
        when(steamApiClient.searchAppId("Bad game")).thenReturn(Optional.of(987654321));
        when(steamApiClient.getAppDetails(987654321)).thenReturn(Optional.of(badAppData));

        String result = service.getGameLaunchDate("Bad game");

        assertThat(result).contains("Coming soon");
    }

    //fail to show release date when release date not found
    @Test
    void getGameLaunchDate_detailsNotFound_returnNotFound(){
        when(steamApiClient.searchAppId("Bad game")).thenReturn(Optional.of(987654321));
        when(steamApiClient.getAppDetails(987654321)).thenReturn(Optional.empty());

        String result = service.getGameLaunchDate("Bad game");

        assertThat(result).contains("Release date not found for: Bad game");
    }

    //fail to show Launch date of non-existent game
    @Test
    void getGameLaunchDate_gameNotOnSteam_returnsNotFound(){
        when(steamApiClient.searchAppId("Fake game")).thenReturn(Optional.empty());

        String result = service.getGameLaunchDate("Fake game");

        assertThat(result).contains("Release date not found for: Fake game");
    }

    //successfully show player count of a game
    @Test
    void getCurrentPlayerCount_returnsPlayerCount(){
        when(steamApiClient.searchAppId("Elden Ring")).thenReturn(Optional.of(1234567890));
        when(steamApiClient.getCurrentPlayerCount(1234567890)).thenReturn(Optional.of(7500));

        String result = service.getCurrentPlayerCount("Elden Ring");

        assertThat(result).contains("7,500 players currently online in Elden Ring");
    }

    //fail to show player count game not on steam
    @Test
    void getCurrentPlayerCount_gameNotOnSteam_returnsNotFound(){
        when(steamApiClient.searchAppId("Fake game")).thenReturn(Optional.empty());

        String result = service.getCurrentPlayerCount("Fake game");

        assertThat(result).contains("Player count not available for: Fake game");
    }

    //fail to show player count game player count empty
    @Test
    void getCurrentPlayerCount_playerCountEmpty_returnsNotFound(){
        when(steamApiClient.searchAppId("Bad game")).thenReturn(Optional.of(987654321));
        when(steamApiClient.getCurrentPlayerCount(987654321)).thenReturn(Optional.empty());

        String result = service.getCurrentPlayerCount("Bad game");

        assertThat(result).contains("Player count not available for: Bad game");
    }

    //successfully show latest news
    @Test
    void getLatestNews_returnsNewsTitlesAndUrls(){
        List<SteamApiClient.NewsItem> news = List.of(
                new SteamApiClient.NewsItem("Patch 1.10 Released", "https://store.steampowered.com/news/1", "content"),
                new SteamApiClient.NewsItem("Balance Update", "https://store.steampowered.com/news/2", "content"),
                new SteamApiClient.NewsItem("New DLC Announced", "https://store.steampowered.com/news/3", "content")
        );
        when(steamApiClient.searchAppId("Elden Ring")).thenReturn(Optional.of(1234567890));
        when(steamApiClient.getLatestNews(1234567890)).thenReturn(Optional.of(news));

        String result = service.getLatestNews("Elden Ring");

        assertThat(result).contains("Patch 1.10 Released");
        assertThat(result).contains("https://store.steampowered.com/news/1");
        assertThat(result).contains("Balance Update");
        assertThat(result).contains("https://store.steampowered.com/news/2");
        assertThat(result).contains("New DLC Announced");
        assertThat(result).contains("https://store.steampowered.com/news/3");
    }

    //fails to show news because of empty news list
    @Test
    void getLatestNews_emptyList_returnsNotFound(){
        when(steamApiClient.searchAppId("Bad game")).thenReturn(Optional.of(1234567890));
        when(steamApiClient.getLatestNews(1234567890)).thenReturn(Optional.empty());
        String result = service.getLatestNews("Bad game");
        assertThat(result).contains("No news found for: Bad game");
    }

    //fails to show news because of non-existent game
    @Test
    void getLatestNews_gameNotOnSteam_returnsNotFound(){
        when(steamApiClient.searchAppId("Fake game")).thenReturn(Optional.empty());

        String result = service.getLatestNews("Fake game");
        assertThat(result).contains("No news found for: Fake game");
    }

    //successfully show playtime for a specific game
    @Test
    void getHoursPlayed_returnsHoursPlayed(){
        when(steamApiClient.getOwnedGames("default_steam_id")).thenReturn(Optional.of(ownedGamesData.games()));

        String result = service.getHoursPlayed("Elden Ring");
        assertThat(result).contains("Elden Ring: 11 hours and 20 minutes played");
    }

    //fail to show playtime for specific game not in library
    @Test
    void getHoursPlayed_gameNotInLibrary_returnsNotFound(){
        when(steamApiClient.getOwnedGames("default_steam_id")).thenReturn(Optional.of(ownedGamesData.games()));

        String result = service.getHoursPlayed("Fake game");
        assertThat(result).contains("Game not found in library: Fake game");
    }

    //fail to show playtime for unavailable library
    @Test
    void getHoursPlayed_gameLibraryUnavailable_returnsNotFound(){
        when(steamApiClient.getOwnedGames("default_steam_id")).thenReturn(Optional.empty());

        String result = service.getHoursPlayed("Bad game");
        assertThat(result).contains("Game not found in library: Bad game");
    }

    //successfully show the gained achievements of a game
    @Test
    void getGameAchievements_returnsGainedAchievements() {
        when(steamApiClient.searchAppId("Elden Ring")).thenReturn(Optional.of(1234567890));
        when(steamApiClient.getPlayerAchievements("default_steam_id", 1234567890)).thenReturn(Optional.of(List.of(
                new SteamDtos.Achievement("apiname", 1, "beat boss", "beat the boss"),
                new SteamDtos.Achievement("apiname", 0, "finish game", "beat the game"),
                new SteamDtos.Achievement("apiname", 1, "start game", "start the game"))));

        String result = service.getGameAchievements("Elden Ring");
        assertThat(result).contains("""
                2/3 achievements unlocked
                Unlocked: beat boss, start game
                locked: finish game""");
    }

    //fail to show gained achievements of a game without achievements
    @Test
    void getGameAchievements_achievementsNotAvailable_returnsNotFound() {
        when(steamApiClient.searchAppId("Bad game")).thenReturn(Optional.of(987654321));
        when(steamApiClient.getPlayerAchievements("default_steam_id", 987654321)).thenReturn(Optional.empty());

        String result = service.getGameAchievements("Bad game");
        assertThat(result).contains("Could not retrieve achievements for: Bad game");
    }

    //fail to show gained achievements of a game without achievements
    @Test
    void getGameAchievements_gameNotOnSteam_returnsNotFound() {
        when(steamApiClient.searchAppId("Bad game")).thenReturn(Optional.empty());

        String result = service.getGameAchievements("Bad game");
        assertThat(result).contains("Could not retrieve achievements for: Bad game");
    }

    //Successfully show a list of owned games
    @Test
    void getAllGames_returnsAllGames(){
        when(steamApiClient.getOwnedGames("default_steam_id")).thenReturn(Optional.of(ownedGamesData.games()));

        String result = service.getAllGames();
        assertThat(result).contains("""
                Bad game
                Elden Ring
                A short hike""");
    }

    //fail to show a list of owned games
    @Test
    void getAllGames_returnsNotFound(){
        when(steamApiClient.getOwnedGames("default_steam_id")).thenReturn(Optional.empty());

        String result = service.getAllGames();
        assertThat(result).contains("Could not retrieve game library");
    }


    //successfully show total play time
    @Test
    void getTotalPlaytime_returnsTotalPlayTime(){
        when(steamApiClient.getOwnedGames("default_steam_id")).thenReturn(Optional.of(ownedGamesData.games()));

        String result = service.getTotalPlaytime();
        assertThat(result).contains("Total playtime: 15 hours and 16 minutes");
    }

    //fail to show total play time
    @Test
    void getTotalPlaytime_returnsTotalNotFound(){
        when(steamApiClient.getOwnedGames("default_steam_id")).thenReturn(Optional.empty());

        String result = service.getTotalPlaytime();
        assertThat(result).contains("Could not retrieve game library");
    }

    //successfully show most played game
    @Test
    void getMostPlayedGame_returnsMostPlayedGame(){
        when(steamApiClient.getMostPlayedGame("default_steam_id")).thenReturn(Optional.of(ownedGamesData.games().get(0)));

        String result = service.getMostPlayedGame();
        assertThat(result).contains("Elden Ring with 11 hours and 20 minutes played");
    }

    //fail to show most played game
    @Test
    void getMostPlayedGame_returnsNotFound(){
        when(steamApiClient.getMostPlayedGame("default_steam_id")).thenReturn(Optional.empty());

        String result = service.getMostPlayedGame();
        assertThat(result).contains("Could not retrieve game library");
    }

    //successfully show top x played games
    @Test
    void getTopGamesByHoursPlayed_returnsTopGames(){
        when(steamApiClient.getOwnedGames("default_steam_id")).thenReturn(Optional.of(ownedGamesData.games()));

        String result = service.getTopGamesByHoursPlayed(5);
        assertThat(result).contains("1. Elden Ring — 11h 20m\n" +
                "2. A short hike — 3h 56m");
    }

    //fail to top x played games
    @Test
    void getTopGamesByHoursPlayed_returnsNotFound(){
        when(steamApiClient.getOwnedGames("default_steam_id")).thenReturn(Optional.empty());

        String result = service.getTopGamesByHoursPlayed(5);
        assertThat(result).contains("Could not retrieve game library");
    }

    //fail to show top x played games for library with only unplayed games
    @Test
    void getTopGamesByHoursPlayed_emptyLibrary_returnsEmpty(){
        ownedGamesData = new SteamDtos.OwnedGamesData(0, List.of());
        when(steamApiClient.getOwnedGames("default_steam_id")).thenReturn(Optional.of(ownedGamesData.games()));

        String result = service.getTopGamesByHoursPlayed(5);
        assertThat(result).contains("No played games in library");
    }

    //successfully show all the details of a game
    @Test
    void getGameInfo_returnsGameInfo(){
        when(steamApiClient.searchAppId("Elden Ring")).thenReturn(Optional.of(1234567890));
        when(steamApiClient.getAppDetails(1234567890)).thenReturn(Optional.of(appData));

        String result = service.getGameInfo("Elden Ring");
        assertThat(result).contains("""
                Developer: FromSoftware
                Price: €59.99 (50% off)
                Genres: Action, RPG
                Release Date: February 24, 2022
                Description: The critically acclaimed fantasy action RPG — rise as the Tarnished, wield the power of the Elden Ring, and become an Elden Lord in the Lands Between.""");
    }

    //successfully show the unavailable details of a game
    @Test
    void getGameInfo_returnsEmptyGameInfo(){
        when(steamApiClient.searchAppId("Bad game")).thenReturn(Optional.of(987654321));
        when(steamApiClient.getAppDetails(987654321)).thenReturn(Optional.of(badAppData));

        String result = service.getGameInfo("Bad game");
        assertThat(result).contains("""
                Developer:\s
                Price: Free to Play
                Genres: Unknown
                Release Date: Coming soon
                Description: No description""");
    }

    //fail to show the details of a game
    @Test
    void getGameInfo_gameNotOnSteam_returnsNotFound(){
        when(steamApiClient.searchAppId("Fake game")).thenReturn(Optional.of(987654321));
        when(steamApiClient.getAppDetails(987654321)).thenReturn(Optional.empty());

        String result = service.getGameInfo("Fake game");
        assertThat(result).contains("Game not found");
    }

    //successfully show recommended on sale games
    @Test
    void getSaleRecommendations_returnsOnSaleGames() {
        SteamApiClient.AppData hollowKnightData = new SteamApiClient.AppData(
                "Hollow Knight",
                List.of("Team Cherry"),
                "A challenging 2D action-adventure.",
                new SteamApiClient.ReleaseDate(false, "February 24, 2017"),
                new SteamApiClient.PriceOverview("€7.49", 50),
                List.of(new SteamApiClient.Genre("1", "Action"))
        );
        SteamApiClient.AppData hadesData = new SteamApiClient.AppData(
                "Hades",
                List.of("Supergiant Games"),
                "A rogue-like dungeon crawler.",
                new SteamApiClient.ReleaseDate(false, "September 17, 2020"),
                new SteamApiClient.PriceOverview("€12.49", 30),
                List.of(new SteamApiClient.Genre("1", "Action"))
        );
        SteamApiClient.AppData celesteData = new SteamApiClient.AppData(
                "Celeste",
                List.of("Maddy Makes Games"),
                "A precise platformer about climbing a mountain.",
                new SteamApiClient.ReleaseDate(false, "January 25, 2018"),
                new SteamApiClient.PriceOverview("€19.99", 0),
                List.of(new SteamApiClient.Genre("1", "Platformer"))
        );

        when(anthropicClient.complete(anyString())).thenReturn("Hollow Knight\nHades\nCeleste");
        when(steamApiClient.searchAppId("Hollow Knight")).thenReturn(Optional.of(367520));
        when(steamApiClient.getAppDetails(367520)).thenReturn(Optional.of(hollowKnightData));
        when(steamApiClient.searchAppId("Hades")).thenReturn(Optional.of(1145360));
        when(steamApiClient.getAppDetails(1145360)).thenReturn(Optional.of(hadesData));
        when(steamApiClient.searchAppId("Celeste")).thenReturn(Optional.of(504230));
        when(steamApiClient.getAppDetails(504230)).thenReturn(Optional.of(celesteData));

        String result = service.getSaleRecommendations("fun indie games");

        assertThat(result).contains("Hollow Knight");
        assertThat(result).contains("€7.49");
        assertThat(result).contains("50% off");
        assertThat(result).contains("Hades");
        assertThat(result).contains("€12.49");
        assertThat(result).contains("30% off");
        assertThat(result).doesNotContain("Celeste");
    }

    //no games matching the request are on sale
    @Test
    void getSaleRecommendations_noGamesOnSale_returnsNoSalesMessage() {
        SteamApiClient.AppData celesteData = new SteamApiClient.AppData(
                "Celeste",
                List.of("Maddy Makes Games"),
                "A precise platformer about climbing a mountain.",
                new SteamApiClient.ReleaseDate(false, "January 25, 2018"),
                new SteamApiClient.PriceOverview("€19.99", 0),
                List.of(new SteamApiClient.Genre("1", "Platformer"))
        );

        when(anthropicClient.complete(anyString())).thenReturn("Celeste");
        when(steamApiClient.searchAppId("Celeste")).thenReturn(Optional.of(504230));
        when(steamApiClient.getAppDetails(504230)).thenReturn(Optional.of(celesteData));

        String result = service.getSaleRecommendations("fun platformers");

        assertThat(result).contains("No games matching your request are currently on sale");
    }

    //claude returns empty response
    @Test
    void getSaleRecommendations_claudeReturnsEmpty_returnsErrorMessage() {
        when(anthropicClient.complete(anyString())).thenReturn("");

        String result = service.getSaleRecommendations("anything");

        assertThat(result).contains("could not find any recommendations from claude");
    }

    //successfully show recommendations including games not on sale
    @Test
    void getRecommendations_returnsAllFoundGames() {
        SteamApiClient.AppData hollowKnightData = new SteamApiClient.AppData(
                "Hollow Knight",
                List.of("Team Cherry"),
                "A challenging 2D action-adventure.",
                new SteamApiClient.ReleaseDate(false, "February 24, 2017"),
                new SteamApiClient.PriceOverview("€7.49", 50),
                List.of(new SteamApiClient.Genre("1", "Action"))
        );
        SteamApiClient.AppData celesteData = new SteamApiClient.AppData(
                "Celeste",
                List.of("Maddy Makes Games"),
                "A precise platformer about climbing a mountain.",
                new SteamApiClient.ReleaseDate(false, "January 25, 2018"),
                new SteamApiClient.PriceOverview("€19.99", 0),
                List.of(new SteamApiClient.Genre("1", "Platformer"))
        );

        when(anthropicClient.complete(anyString())).thenReturn("Hollow Knight\nCeleste");
        when(steamApiClient.searchAppId("Hollow Knight")).thenReturn(Optional.of(367520));
        when(steamApiClient.getAppDetails(367520)).thenReturn(Optional.of(hollowKnightData));
        when(steamApiClient.searchAppId("Celeste")).thenReturn(Optional.of(504230));
        when(steamApiClient.getAppDetails(504230)).thenReturn(Optional.of(celesteData));

        String result = service.getRecommendations("fun indie games");

        assertThat(result).contains("Hollow Knight");
        assertThat(result).contains("€7.49");
        assertThat(result).contains("50% off");
        assertThat(result).contains("Celeste");
        assertThat(result).contains("€19.99");
        assertThat(result).contains("0% off");
    }

    //games not found on steam are excluded from recommendations
    @Test
    void getRecommendations_gamesNotOnSteam_areExcluded() {
        SteamApiClient.AppData hadesData = new SteamApiClient.AppData(
                "Hades",
                List.of("Supergiant Games"),
                "A rogue-like dungeon crawler.",
                new SteamApiClient.ReleaseDate(false, "September 17, 2020"),
                new SteamApiClient.PriceOverview("€12.49", 30),
                List.of(new SteamApiClient.Genre("1", "Action"))
        );

        when(anthropicClient.complete(anyString())).thenReturn("Hades\nFake game");
        when(steamApiClient.searchAppId("Hades")).thenReturn(Optional.of(1145360));
        when(steamApiClient.getAppDetails(1145360)).thenReturn(Optional.of(hadesData));
        when(steamApiClient.searchAppId("Fake game")).thenReturn(Optional.empty());

        String result = service.getRecommendations("action games");

        assertThat(result).contains("Hades");
        assertThat(result).doesNotContain("Fake game");
    }

    //all recommended games not found on steam returns no games message
    @Test
    void getRecommendations_noGamesFoundOnSteam_returnsNoGamesMessage() {
        when(anthropicClient.complete(anyString())).thenReturn("Fake game 1\nFake game 2");
        when(steamApiClient.searchAppId("Fake game 1")).thenReturn(Optional.empty());
        when(steamApiClient.searchAppId("Fake game 2")).thenReturn(Optional.empty());

        String result = service.getRecommendations("anything");

        assertThat(result).contains("No games matching your request are currently on steam");
    }

    //claude returns empty response for getRecommendations
    @Test
    void getRecommendations_claudeReturnsEmpty_returnsErrorMessage() {
        when(anthropicClient.complete(anyString())).thenReturn("");

        String result = service.getRecommendations("anything");

        assertThat(result).contains("could not find any recommendations from claude");
    }
}