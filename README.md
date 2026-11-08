# Tower of Life

A RuneLite plugin for Creature Creation in the Tower of Life basement.

Each Symbol of life altar needs two items before you can activate it to create a creature. This plugin labels every altar with:

- the creature it creates
- the two items it needs, and whether each one is **placed** (green), **in your inventory** (yellow), or still **needed** (red)
- an outline around the altar: cyan when it is ready to activate, yellow when you are carrying everything it still needs, red otherwise

| Ready to activate | One placed, one carried |
|---|---|
| ![Ready to activate](docs/ready.png) | ![One item placed](docs/in-progress.png) |
| **Carrying both items** | **Nothing placed or carried** |
| ![Carrying both items](docs/carrying.png) | ![Items needed](docs/needed.png) |

Clicking Activate while carrying both items also works: the altar takes them from your inventory and creates the creature in one step.

| Creature | Items |
|---|---|
| Newtroost | Eye of newt + Feather |
| Unicow | Unicorn horn + Cowhide |
| Spidine | Red spiders' eggs + Raw sardine |
| Swordchick | Raw swordfish + Raw chicken |
| Jubster | Raw jubbly + Raw lobster |
| Frogeel | Giant frog legs + Raw cave eel |

## How placed items are tracked

The game stores which items are on each altar in 12 varbits (`TOL_SYMBOL_OBJ1`–`12`), one per item. The plugin reads them directly, so the overlay matches the game from the moment you log in, including after a relog.

Inspecting an altar also updates the overlay from what the dialogue says, as a backup.

## Configuration

- Show creature name, required items, and item icons
- Hide items that are already placed
- Highlight altars
- Colors for placed, in inventory, missing, and ready
