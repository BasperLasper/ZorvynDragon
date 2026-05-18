# ZorvynDragon
Support: 
A Paper 26.1.2 plugin that automatically respawns the Ender Dragon whenever a player enters the End. Supports group fights, configurable health, dragon egg drops, reward items, broadcast control, and full message customisation.

## Features
- Automatically spawns the dragon when a player enters the End
- Group wait window — gives other players time to join before the dragon spawns
- Prevents two dragons from being active at once
- Respawns end crystals and towers as part of the ritual (optional)
- Drops a dragon egg on death (in addition to or instead of vanilla behaviour)
- Configurable dragon health and XP drop
- Extra reward item drops on death
- Auto-respawn the dragon X minutes after it's killed
- Broadcast to the whole server or just End players
- All messages fully configurable with & color codes
- Admin commands to force spawn, kill, reset, and check status
- Config reload without restarting the server

## Commands
| Command | Alias | Permission | Description |
|---|---|---|---|
| `/zorvyndragon spawn` | `/zd spawn` | `zorvyndragon.spawn` | Force spawn the dragon |
| `/zorvyndragon kill` | `/zd kill` | `zorvyndragon.kill` | Remove the dragon |
| `/zorvyndragon reset` | `/zd reset` | `zorvyndragon.reset` | Reset dragon state so next player triggers a spawn |
| `/zorvyndragon status` | `/zd status` | `zorvyndragon.status` | Check if the dragon is currently active |
| `/zorvyndragon reload` | `/zd reload` | `zorvyndragon.reload` | Reload config without restarting |

All commands default to OP only.

## Configuration
```yaml
world:
  end-world-name: "world_the_end"       # Name of your End world

spawn:
  trigger-on-enter: true                 # Spawn dragon when player enters End
  group-wait-seconds: 5                  # Seconds to wait for others to join
  only-first-entry-per-session: true     # Only trigger once per player per visit
  reset-trigger-on-leave: true           # Reset trigger when player leaves End
  respawn-crystals: true                 # Respawn towers as part of the ritual
  auto-reset-minutes: 0                  # Auto respawn after X mins (0 = off)

dragon:
  max-health: 200.0                      # Dragon max health (vanilla = 200)
  drop-egg: true                         # Drop dragon egg on death
  egg-drop-at-exit-portal: true          # Drop egg at portal instead of death location
  egg-drop-delay-ticks: 40              # Delay before egg drops (lets portal generate)
  drop-vanilla-xp: true                  # Drop XP on death
  custom-xp: -1                          # Override XP amount (-1 = vanilla)

broadcast:
  on-spawn: true                         # Broadcast when dragon spawns
  on-death: true                         # Broadcast when dragon dies
  scope: "server"                        # "server" or "end" players only

rewards:
  enabled: false                         # Drop extra items on death
  items:
    - DIAMOND
    - ELYTRA
    - NETHER_STAR
```

## Messages
All messages are editable in `config.yml` under `messages:`. Supports `&` color codes.

| Key | Placeholder | Description |
|---|---|---|
| `dragon-spawned` | — | Broadcast when dragon spawns |
| `dragon-defeated` | — | Broadcast when dragon dies |
| `dragon-spawning-soon` | `{seconds}` | Countdown message when first player enters |
| `player-joined-fight` | `{player}` | When another player joins during countdown |
| `dragon-already-active` | — | Sent to player if dragon is already up |
| `spawn-forced` | — | Confirmation for /zd spawn |
| `dragon-killed-admin` | `{count}` | Confirmation for /zd kill |
| `dragon-reset` | — | Confirmation for /zd reset |
| `status-active` | — | /zd status when dragon is alive |
| `status-inactive` | — | /zd status when dragon is dead |
| `config-reloaded` | — | Confirmation for /zd reload |

## How It Works
1. A player steps into the End for the first time that visit
2. A countdown begins (configurable, default 5 seconds)
3. Any other players who enter during the countdown join the same fight
4. The dragon spawns once — no duplicates possible
5. When the dragon dies, a dragon egg drops and rewards are given
6. The state resets, ready for the next player to trigger it

## Installation
1. Drop `ZorvynDragon.jar` into your `plugins/` folder
2. Restart the server
3. Edit `plugins/ZorvynDragon/config.yml` to your liking
4. Use `/zd reload` to apply changes without restarting

## Requirements
- Paper 26.1.2
- Java 25
