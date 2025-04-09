# Simple Text Adventure Game (STAG)

## Project Overview

This is a Java-based text adventure game engine, inspired by the classic game Zork. Players interact with the game world by entering natural language commands to explore, collect items, interact with characters, and solve tasks.

The goal of the project is to build a generic socket-based server engine that loads different game worlds from configuration files (DOT + XML), supports both built-in and custom commands, and provides a dynamic game experience.



## How to Play

This is a text-based adventure game. All operations are performed through commands.  
Each player starts from an initial location and explores different places, picks up items, and unlocks new areas.

### Supported Built-in Commands

| Command              | Description                                                  |
| -------------------- | ------------------------------------------------------------ |
| `inventory` or `inv` | Lists the items in your inventory                            |
| `get <item>`         | Picks up an item from the current location                   |
| `drop <item>`        | Drops an item from your inventory into the current location  |
| `goto <location>`    | Moves to a connected location                                |
| `look`               | Shows details of the current location, including items, furniture, characters, and available paths |
| `health`             | Displays your current health level (max 3)                   |

### Custom Actions (Defined in XML)

Using configuration files such as `basic-actions.xml` or `extended-actions.xml`, players can perform custom actions like:

- `open trapdoor with key`: unlocks a trapdoor
- `chop tree with axe`: cuts down a tree and obtains a log
- `fight elf`: attacks the elf and loses health
- `drink potion`: restores health
- `dig ground with shovel`: digs up hidden treasure
- `blow horn`: summons the lumberjack

Custom commands support flexible natural expressions like `please cut down the tree using the axe`.



## How to Run

1. Start the game server:

```
mvnw exec:java@server
```

2. In a new terminal window, start the client:

```
mvnw exec:java@client -Dexec.args="playername"
```

- Each player is identified by their username. New players are placed in the start location.
- Multiple players are supported. Each player's state is tracked independently.



## Configuration Files

| Filename                                       | Type | Description                                                  |
| ---------------------------------------------- | ---- | ------------------------------------------------------------ |
| `basic-entities.dot` / `extended-entities.dot` | DOT  | Defines locations, furniture, artefacts, characters, and paths |
| `basic-actions.xml` / `extended-actions.xml`   | XML  | Defines custom actions, triggers, effects, and narration     |

Entity descriptions appear in the output of the `look` command.



## Notes

- The game supports **flexible natural language parsing**, including:
  - **Decorated Commands**: Extra words like `please chop the tree using the axe` are accepted as `chop tree with axe`
  - **Word Ordering**: Different word orders like `use axe to chop tree` are valid
  - **Partial Commands**: Commands like `unlock trapdoor` or `unlock with key` are acceptable as long as a trigger and one subject are present
  - **Ambiguity Handling**: If a command matches multiple actions, the game refuses to execute and returns a warning
  - **No Composite Commands**: Multi-action commands like `get axe and coin` or `open door and go cellar` are not supported
  - **Extraneous Entities**: Commands with unrelated entities will be rejected, such as `open potion with hammer`

- Commands are case-insensitive but must include at least one **trigger phrase** and one **subject entity** to be valid.

- When a player's health reaches zero:
  - All items in inventory are dropped at the current location
  - The player is respawned at the start location with full health



## Example Commands

```
simon: look
simon: get key
simon: goto cabin
simon: unlock trapdoor with key
simon: goto cellar
simon: fight elf
simon: health
```



## Features

- Multi-player support
- Configurable game world with DOT/XML
- Health system and revival mechanism
- Dynamic production and consumption of entities
- Clean, modular design suitable for education and testing



## Tech Stack

- Java 17+
- Maven
- JPGD Parser (for DOT parsing)
- JAXP + DOM (for XML parsing)
- Socket-based client-server architecture
