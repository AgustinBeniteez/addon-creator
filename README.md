# Addon Creator

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Java](https://img.shields.io/badge/Java-21-orange.svg)
![JavaFX](https://img.shields.io/badge/JavaFX-21-green.svg)

**Desktop application for creating Minecraft Bedrock Edition addons**

Created by **Agustín Benítez**

---

## 🎯 Features

- ✅ **Automatic Project Structure**: Generates BP (Behavior Pack) and RP (Resource Pack) folders
- ✅ **Visual Manifest Editor**: Edit manifest.json files with an intuitive UI
- ✅ **UUID Generation**: Automatic UUID generation for manifests
- ✅ **Presets System**: Quick templates for items, blocks, and entities
- ✅ **Validation**: Ensures your addon structure is correct

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

1. **Select a folder** where you want to create your addon
2. **Configure the manifest** using the visual editor:
   - Addon name
   - Description
   - Version
   - Minimum engine version
3. **Choose presets** to add custom items, blocks, or entities
4. **Generate** your addon structure

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

The executable JAR will be in `target/addon-creator-1.0.0.jar`

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
- All contributors who help improve this tool

---

## 📚 Resources

- [Minecraft Bedrock Addon Documentation](https://learn.microsoft.com/en-us/minecraft/creator/)
- [JavaFX Documentation](https://openjfx.io/)

---

**Made with ❤️ for the Minecraft Bedrock community**
