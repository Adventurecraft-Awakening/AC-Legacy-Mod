# Adventurecraft Legacy Mod

[![Build Status](https://github.com/Adventurecraft-Awakening/AC-Legacy/workflows/build/badge.svg)](https://github.com/Adventurecraft-Awakening/AC-Legacy/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Minecraft Version](https://img.shields.io/badge/Minecraft-Beta%201.7.3-green.svg)](https://minecraft.wiki/w/Java_Edition_Beta_1.7.3)
[![Fabric](https://img.shields.io/badge/Mod%20Loader-Fabric-blue.svg)](https://fabricmc.net/)

A comprehensive revival of the classic Adventurecraft mod for Minecraft Beta 1.7.3, 
bringing advanced scripting, map editing, and adventure creation tools to the modern Fabric ecosystem.

## 🎮 What is Adventurecraft?

Adventurecraft Legacy is a powerful mod that transforms Minecraft into a comprehensive adventure creation platform. 
This version brings all the beloved features to Beta 1.7.3 with modern improvements and optimizations.

### Key Features

#### 🛠️ **Advanced Map Editing**
- **In-game Level Editor**: Create and modify worlds directly within Minecraft
- **Block Copy/Paste System**: Efficiently duplicate structures and regions
- **Undo/Redo Functionality**: Never lose progress with comprehensive history tracking
- **Selection Tools**: Precise block selection and manipulation tools
- **Debug Visualization**: Visual aids for collision detection, pathfinding, and more

#### 📜 **JavaScript Scripting Engine**
- **Mozilla Rhino Integration**: Full JavaScript support for complex game logic
- **Event-Driven Programming**: React to player actions, block interactions, and world events
- **Entity Control**: Script custom mob behaviors, pathfinding, and AI
- **World Manipulation**: Dynamically modify blocks, spawn entities, and control game state
- **Custom GUI Creation**: Build interactive interfaces and HUDs

#### 🧱 **Custom Blocks & Items**
- **Script Blocks**: Execute JavaScript code when triggered
- **Camera Blocks**: Create cinematic cutscenes and camera movements
- **Message Blocks**: Display text and dialogue to players
- **Music Blocks**: Control background music and sound effects
- **Effect Blocks**: Trigger visual effects and particle systems
- **Trigger Blocks**: Redstone-compatible logic blocks
- **Spawn Points**: Control player and entity spawning
- **Interactive Elements**: Chairs, pushable blocks, locked doors, and more

#### 🎬 **Cinematic Tools**
- **Camera System**: Create smooth camera movements and cutscenes
- **Camera Blending**: Smooth transitions between camera positions
- **Debug Camera**: Free-roam camera for testing and development

#### 🎨 **Enhanced Graphics**
- **LWJGL3 Integration**: Modern OpenGL support with improved performance
- **Debug Rendering**: Visual debugging tools for development
- **Custom Textures**: Support for animated and dynamic textures
- **Particle Effects**: Optimized batching and interaction with wind

## 🚀 Installation

### Prerequisites
- **Minecraft Beta 1.7.3**
- **Fabric Loader 0.16.10+**
- **Java 21+** (JDK recommended for development)

### For Players

**Recommended: Use the Official Launcher**
- Download and install the [Adventurecraft Launcher](https://adventurecraft.dev/launcher)
- The launcher automatically handles mod installation, updates, and map downloads
- Simply launch and start creating adventures!

### For Developers

1. **Clone the Repository**
   ```bash
   git clone https://github.com/Adventurecraft-Awakening/AC-Legacy.git
   cd AC-Legacy
   ```

2. **Setup Development Environment**
   ```bash
   ./gradlew build
   ```

3. **Import into IDE**
   - For setup instructions, see the [Fabric wiki](https://fabricmc.net/wiki/tutorial:setup)
   - Recommended IDEs: IntelliJ IDEA

## 📖 Usage Guide

### Getting Started

1. **Enable Debug Mode**
   - Press `F4` to toggle debug mode
   - This enables access to editing tools and special blocks

2. **Access Special Blocks**
   - In debug mode, you'll have access to all Adventurecraft blocks
   - Use the creative inventory (F7) to obtain them

3. **Basic Scripting**
   ```javascript
   // Example script for a Script Block
   function onTrigger() {
       player.sendMessage("Hello, Adventurecraft!");
       world.setBlock(x + 1, y, z, "torch");
   }
   ```

### Map Editing Controls

- **F4**: Toggle debug/edit mode
- **F5**: Toggle level editing mode
- **Mouse Controls**: Select and manipulate blocks
- **Keyboard Shortcuts**: Copy, paste, undo, redo operations

### Scripting API

The mod provides extensive JavaScript APIs for:
- **World Manipulation**: `world.setBlock()`, `world.getBlock()`, etc.
- **Player Interaction**: `player.sendMessage()`, `player.teleport()`, etc.
- **Entity Control**: `entity.setTarget()`, `entity.pathTo()`, etc.
- **GUI Creation**: Custom interfaces and HUDs
- **Event Handling**: Block triggers, player actions, world events

## 🏗️ Project Structure

<details>

<summary>Directory overview</summary>

```
src/main/java/dev/adventurecraft/awakening/
├── ACMod.java                 # Main mod class
├── ACMainThread.java          # Extended Minecraft client
├── common/                    # Core game logic
│   ├── AC_DebugMode.java     # Debug mode functionality
│   ├── AC_MapEditing.java    # Map editing system
│   └── gui/                  # User interface components
├── script/                    # JavaScript integration
│   ├── Script.java           # Main scripting engine
│   ├── ScriptEntity.java     # Entity scripting API
│   └── ScriptKeyboard.java   # Input handling
├── tile/                      # Custom blocks
│   ├── AC_BlockScript.java   # Script execution blocks
│   ├── AC_BlockCamera.java   # Camera control blocks
│   └── entity/               # Block entities
├── item/                      # Custom items and tools
├── client/                    # Client-side rendering
└── extension/                 # Minecraft class extensions
```

</details>

## 🤝 Contributing

We welcome contributions from the community! Here's how you can help:

### Development Setup

1. **Fork the Repository**
2. **Create a Feature Branch**
   ```bash
   git checkout -b feature/amazing-feature
   ```
3. **Make Your Changes**
4. **Test Thoroughly**
5. **Submit a Pull Request**

### Areas for Contribution

- 🐛 **Bug Fixes**: Help resolve issues and improve stability
- ✨ **New Features**: Add new blocks, scripting APIs, or tools
- 📚 **Documentation**: Improve guides and API documentation
- 🧪 **Testing**: Help test new features and report bugs

For a complete list of known issues, see our [GitHub Issues](https://github.com/Adventurecraft-Awakening/AC-Legacy/issues).

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Original Adventurecraft Developer Cryect**: For creating the original mod
- **Adventurecraft Awakening Team**: For resurrecting and improving the mod
- **Fabric Team**: For the excellent modding framework
- **Mozilla Rhino**: For JavaScript engine integration
- **Community Contributors**: For bug reports, features, and support

## 📞 Support & Community

- **Website**: [adventurecraft.dev](https://adventurecraft.dev/)
- **GitHub Issues**: [Report bugs and request features](https://github.com/Adventurecraft-Awakening/AC-Legacy/issues)
- **Discord**: Join our community server for real-time support


---

**Made with ❤️ by the Adventurecraft Awakening Team**
