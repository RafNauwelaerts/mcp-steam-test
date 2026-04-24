# Steam MCP Server

A Spring Boot MCP (Model Context Protocol) server that exposes Steam game and library data as tools for AI assistants like Claude.

## Prerequisites

- Java 17+
- Maven
- A [Steam Web API key](https://steamcommunity.com/dev/apikey) (free)
- Your [Steam ID](https://www.steamidfinder.com/)

## Setup

1. Clone the repository
2. Copy the example properties file and fill in your credentials:
   ```
   cp src/main/resources/application.properties.example src/main/resources/application.properties
   ```
3. Edit `application.properties`:
   ```properties
   steam.api.key=YOUR_STEAM_API_KEY
   steam.default.user.id=YOUR_STEAM_ID
   ```
  you can get an api key on https://steamcommunity.com/dev/apikey

4. Run the application:
   ```
   ./mvnw spring-boot:run
   ```
   The server starts on `http://localhost:8080`.

## Connecting to Claude Code

Register the server once:
```
claude mcp add --transport sse steam-games http://localhost:8080/sse
```

Then start Claude Code (`claude`) and ask anything — for example:
- *"Who developed Hades?"*
- *"What's my most played game?"*
- *"Show me all my Steam games"*

## Available Tools

### Game info (no account needed)
| Tool | Description |
|------|-------------|
| `getGameDeveloper` | Developer of a game |
| `getGamePrice` | Current price and discount |
| `getGameGenres` | Genres |
| `getGameDescription` | Short description |
| `getGameLaunchDate` | Release date |
| `getCurrentPlayerCount` | Live concurrent player count |
| `getLatestNews` | 3 most recent news articles / patch notes |
| `getGameInfo` | All of the above in one call |

### User library (requires API key + Steam ID)
| Tool | Description |
|------|-------------|
| `getMostPlayedGame` | Your most played game by total hours |
| `getHoursPlayed` | Hours played for a specific game |
| `getGameAchievements` | Unlocked achievements for a game |
| `getAllGames` | Full list of owned games |