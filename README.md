# Addon Creator

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Java](https://img.shields.io/badge/Java-21-orange.svg)
![JavaFX](https://img.shields.io/badge/JavaFX-21-green.svg)
![Version](https://img.shields.io/badge/version-1.0.2-purple.svg)

**Desktop application for creating Minecraft Bedrock Edition addons**

An integrated development environment (IDE) specifically designed to streamline the creation of Minecraft Bedrock addons. Whether you prefer coding directly or using visual tools, Addon Creator has you covered.

Created by **Agustín Benítez**

---

## 🎯 Features

### 🖥️ Dual Editing Modes
- **Code Mode**: A full-featured code editor with file tree navigation, syntax highlighting, and git integration.
- **Easy Mode (Ez Mode)**: Visual interface to manage your addon elements without touching code.
  - **Entities**: Visual editor for entity behaviors and properties.
  - **Items**: Create custom items with easy-to-use forms.
  - **Blocks**: Define custom blocks and their textures.
  - **Textures**: Manage your project's textures.
  - **Models**: Import and manage 3D models.
  - **Sounds**: Import and preview sound files.

### 🎨 Integrated Tools
- **Blockbench Integration**: 
  - Open 3D models (`.json`, `.geo.json`) directly in Blockbench.
  - Create new models from within the app.
  - Configurable Blockbench executable path.
- **Pixel Art Editor**: Built-in editor for creating and modifying textures.
- **Audio Player**: Preview `.ogg` and `.wav` sound files directly in the editor.
- **Markdown Preview**: View `README.md` and `CHANGELOG.md` files with live rendering.

### 🛠️ Project Management
- **Project Generator**: Automatically creates valid Behavior Pack (BP) and Resource Pack (RP) structures.
- **Manifest Editor**: Visual editor for `manifest.json` with automatic UUID generation.
- **Git Integration**: Initialize repositories, track changes, view diffs, and commit directly from the UI.
- **Todo Manager**: Keep track of your project tasks with a built-in todo list.
- **Project Search**: Quickly find and filter your projects on the home screen.

### ⚙️ Customization
- **Settings**:
  - **Language**: Support for English and Spanish.
  - **Window Size**: Customizable window dimensions.
  - **External Tools**: Configure paths for external tools like Blockbench.

---

## 🚀 Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.6+

### Installation

1. Clone the repository:
```bash
git clone https://github.com/AgustinBeniteez/addon-creator.git
cd addon-creator
```

2. Build the project:
```bash
mvn clean package
```

3. Run the application:
```bash
mvn javafx:run
```

---

## 📖 Usage

1. **Create or Open a Project**: Use the Home Screen to start a new project or open an existing one.
2. **Switch Modes**: Use the toggle button in the toolbar to switch between Code Mode and Easy Mode.
3. **Manage Elements**:
   - In **Easy Mode**, use the sidebar categories to add/edit elements.
   - In **Code Mode**, edit files directly in the `src` folder.
4. **Use External Tools**:
   - Configure your Blockbench path in **Settings**.
   - Right-click models or use the "Open in Blockbench" button to edit 3D assets.

---

## 🛠️ Building from Source

```bash
# Compile
mvn clean compile

# Run tests
mvn test

# Create executable JAR
mvn clean package
```

The executable JAR will be located in `target/addon-creator-1.0.2.jar`.

---

## 🤝 Contributing

Issues and Pull Requests are welcome! 

- Install Maven: `scoop install main/maven`
- Install Java: `scoop install java/openjdk`

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👤 Author

**Agustín Benítez**

- GitHub: [@AgustinBeniteez](https://github.com/AgustinBeniteez)

---

## 🙏 Acknowledgments

- Minecraft Bedrock Edition documentation
- JavaFX community
- JGit and CommonMark libraries
- All contributors who help improve this tool

---

**Made with ❤️ for the Minecraft Bedrock community**
