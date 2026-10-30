# Armour Stats HUD

A Minecraft mod to show the status of your equipped armour on the HUD.

Two display modes are available (toggle with `/armourstats`):

- **panel** (default) – a compact panel in the top-left listing each equipped
  armour piece (head, chest, legs, feet), its remaining durability
  (colour-coded) and your total armour points.
- **hotbar** – a row of hotbar-sized slots to the left of the hotbar showing
  just the armour icons and their durability bars.

## Screenshot

![Armour Stats HUD showing equipped armour, colour-coded durability and total armour points](docs/armour-stats-panel.png)

The durability of each piece is colour-coded — green when healthy, yellow when worn and red when nearly broken.

## Requirements

- Minecraft **26.2** (Fabric)
- [Fabric Loader](https://fabricmc.net/) `>= 0.19.0`
- [Fabric API](https://modrinth.com/mod/fabric-api) `0.156.0+26.2`
- **Java 25** (required by Minecraft 26.2)

## Commands

Client-side commands (work in singleplayer and on multiplayer):

| Command | Effect |
| --- | --- |
| `/armourstats` | Show the current display mode |
| `/armourstats toggle` | Cycle between `panel` and `hotbar` |
| `/armourstats mode` | Same as `toggle` |
| `/armourstats mode panel` | Use the detailed top-left panel |
| `/armourstats mode hotbar` | Use the hotbar icon row |

The chosen mode is saved to `config/armourstats.properties`.

## Development

This is a [Fabric](https://fabricmc.net/) mod built with Gradle + Fabric Loom.

```bash
# Build the mod jar (output in build/libs/)
./gradlew build

# Launch a development Minecraft client with the mod loaded
./gradlew runClient
```

The built mod jar is written to `build/libs/armourstats-<version>.jar`.

## License

[MIT](LICENSE)

---

Built with ❤️ for mstrspence ⛏️💎⛏️
