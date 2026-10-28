# Personal Welcome

Server-side Fabric mod for Minecraft 26.3.

Sends a private welcome message when a player joins and provides `/myping`.

## Placeholders

- `%player%` — player name
- `%ping%` — server-measured player latency in milliseconds
- `%online%` — current online player count
- `%max_players%` — configured maximum player count
- `%server_address%` — value from the configuration

## Commands

- `/myping` — shows the player their current ping
- `/personalwelcome reload` — console-only configuration reload

## Configuration

Created at `config/personal-welcome.json` on first startup.

```json
{
  "welcomeEnabled": true,
  "serverAddress": "dinokebab.duckdns.org",
  "welcomeMessage": "..."
}
```

## Build

```bash
./gradlew build
```

The server JAR is created in `build/libs/`.
