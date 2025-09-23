# SurvivalCore-V2

A comprehensive Minecraft Spigot plugin designed for survival servers, created by MemeMC Network.

## Features

### 🎯 Random Teleportation (RTP)
- Safe random teleportation with configurable boundaries
- Intelligent location finding that avoids unsafe blocks (lava, fire, etc.)
- Cooldown system with bypass permissions
- Multi-world support with configurable allowed worlds
- Admin command to teleport other players

### 📞 Teleport Accept System (TPA)
- Send teleport requests to other players
- TPA and TPA-Here request types
- Request timeout system with automatic cleanup
- Teleportation delays with movement cancellation
- Sound notifications for better user experience

### 🏠 Home Management System
- Set multiple homes with configurable limits
- Permission-based home limits (1, 5, 10, unlimited)
- Database storage with SQLite/MySQL support
- Teleportation delays and movement checks
- Home validation and cleanup

### 🗺️ Warp System
- Admin-managed server warps
- Easy warp creation and deletion
- Teleportation with cooldowns and delays
- Permission-based access control

### 🌟 Player Warp System
- Player-created public warps
- Configurable limits per player
- Economic integration (coming soon)
- Permission-based management

### 🛒 Multi-Page Shop System
- GUI-based shop with multiple categories
- 7 categories: Blocks, Tools, Food, Enchantments, Potions, Decorations, Redstone
- Multiple pages per category with 45+ items each
- Buy and sell functionality with dynamic pricing
- Player balance display with real-time updates
- Search functionality for finding specific items
- Transaction history tracking
- Favorites system for quick access to preferred items
- Category selector with dynamic category loading
- Navigation controls (previous/next page, close)
- Economic integration support (Vault compatible)

### 🎁 Kit System
- Configurable starter kits with customizable items
- Permission-based kit access control
- Cooldown system with bypass permissions
- One-time and repeatable kits
- Cost-based kits (requires economy integration)
- GUI-based kit selection interface
- Multiple predefined kits: Starter, Daily, VIP, PvP, Builder
- Database tracking for one-time kit claims
- Fully customizable kit configurations

### 📊 Player Statistics & Leaderboards
- Comprehensive player activity tracking
- Statistics include: time played, blocks placed/broken, mobs killed, deaths, PvP kills, distance traveled, items crafted, fish caught
- GUI-based statistics viewer with detailed breakdowns
- Server-wide leaderboards for all stat categories
- Automatic data collection and periodic saving
- Player ranking system with visual displays
- Historical data preservation across sessions

### 💬 Advanced Chat Management
- Comprehensive chat moderation and formatting system
- Anti-spam protection with message cooldowns and repeat detection
- Profanity filter with customizable word list and replacement
- URL blocking to prevent unwanted links
- Caps filter to prevent excessive capital letters
- Player muting system with staff commands
- Global chat toggle for server-wide chat control
- Chat clearing functionality for moderators
- Permission-based chat formatting with group support
- Color code support for privileged players
- Configurable chat formats per permission group

### 🛠️ Enhanced Admin Tools
- Comprehensive admin control panel with GUI interface
- Player management with teleportation and monitoring
- God mode with damage immunity for administrators
- Vanish mode with selective visibility controls
- Instant heal and feed commands for player management
- Gamemode switching with permission controls
- Advanced teleportation system (players and coordinates)
- Server backup system with automated scheduling
- Real-time server statistics (TPS, RAM, uptime, player count)
- World management and monitoring
- Plugin status overview and management

## Configuration

### Main Config (`config.yml`)
- Database settings (SQLite/MySQL)
- Feature toggles and cooldowns
- Permission-based limits
- Teleportation delays and safety settings

### Messages (`messages.yml`)
- Fully customizable messages
- Color code support
- Placeholder system
- Multi-language ready

### Shop Config (`shop.yml`)
- Complete shop item configuration
- Price management
- Category organization
- GUI customization

### Kit Config (`kits.yml`)
- Configurable kits with items, permissions, cooldowns
- Support for one-time and repeatable kits
- Cost-based kits for economy integration
- Predefined starter, VIP, PvP, and builder kits

## Permissions

The plugin includes a comprehensive permission system with granular control:

- `survivalcore.*` - All permissions
- `survivalcore.rtp.*` - All RTP permissions
- `survivalcore.tpa.*` - All TPA permissions
- `survivalcore.home.*` - All home permissions
- `survivalcore.warp.*` - All warp permissions
- `survivalcore.pwarp.*` - All player warp permissions
- `survivalcore.shop.*` - All shop permissions
- `survivalcore.kit.*` - All kit permissions
- `survivalcore.stats.*` - All statistics permissions
- `survivalcore.chat.*` - All chat management permissions
- `survivalcore.admin.*` - All administrative permissions
- `survivalcore.bypass.cooldown` - Bypass all cooldowns

## Database Support

- **SQLite**: Built-in, no setup required
- **MySQL**: Full support for larger servers
- Automatic table creation and migration
- Efficient data storage and retrieval

## Commands

### RTP Commands
- `/rtp [player]` - Random teleport

### TPA Commands
- `/tpa <player>` - Send teleport request
- `/tpahere <player>` - Request player to teleport to you
- `/tpaccept` - Accept teleport request
- `/tpdeny` - Deny teleport request

### Home Commands
- `/home [name]` - Teleport to home
- `/sethome [name]` - Set a home
- `/delhome <name>` - Delete a home
- `/homes` - List all homes

### Warp Commands
- `/warp <name>` - Teleport to warp
- `/warps` - List all warps
- `/setwarp <name>` - Create warp (admin)
- `/delwarp <name>` - Delete warp (admin)

### Player Warp Commands
- `/pwarp <name>` - Teleport to player warp
- `/pwarps` - List player warps
- `/setpwarp <name>` - Create player warp
- `/delpwarp <name>` - Delete your player warp

### Shop Commands
- `/shop [category]` - Open shop

### Kit Commands
- `/kit [name]` - Claim a kit or open kit GUI
- `/kits` - List all available kits

### Statistics Commands
- `/stats [player]` - View player statistics
- `/leaderboard [category]` - View server leaderboards

### Chat Management Commands
- `/mute <player>` - Mute a player from chat
- `/unmute <player>` - Unmute a player from chat
- `/clearchat` - Clear chat for all players
- `/mutechat` - Toggle server-wide chat

### Admin Tools Commands
- `/adminpanel` - Open admin control panel GUI
- `/god [player]` - Toggle god mode (damage immunity)
- `/vanish` - Toggle invisibility mode
- `/heal [player]` - Heal yourself or another player
- `/feed [player]` - Feed yourself or another player
- `/gamemode <mode> [player]` - Change gamemode
- `/teleport <player|x y z>` - Teleport to player or coordinates
- `/backup [name]` - Create server backup

### Admin Commands
- `/survivalcore <reload|info>` - Plugin management

## Installation

1. Download the plugin JAR file
2. Place it in your server's `plugins` folder
3. Start/restart your server
4. Configure the plugin using the generated config files
5. Set up permissions using LuckPerms or your preferred permission plugin

## Requirements

- Minecraft 1.20.1+
- Spigot/Paper server
- Java 17+
- (Optional) Economy plugin with Vault support

## Support

For support, feature requests, or bug reports, please visit our GitHub repository:
https://github.com/MemeMC-Network/plugin-1

## License

This plugin is created by MemeMC Network. All rights reserved.