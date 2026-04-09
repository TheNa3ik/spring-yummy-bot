<div align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="assets/banner-dark.png">
    <img alt="YummyBot Banner" src="assets/banner-light.png" width="100%">
  </picture>

  <br />
  
  [![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://www.oracle.com/java/)
  [![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
  [![TelegramBots API](https://img.shields.io/badge/TelegramBots-API-blue.svg)](https://github.com/rubenlagus/TelegramBots)
  [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
  
  <p align="center">
    <strong>Your multilingual cooking companion on Telegram.</strong>
  </p>
</div>

## About The Project

Yummy Bot is a Telegram bot designed to streamline recipe discovery. Built with Java and Spring Boot, it allows users to search for delicious meals based on available ingredients, specific dietary requirements, and food intolerances.

The application integrates the **Spoonacular API** for comprehensive recipe data, alongside the **Google Gemini API** to provide dynamic, on-the-fly translation. This ensures a fully localized user experience across 8 supported languages.

## Getting Started

### Prerequisites

* Java 25 or higher
* Maven
* MySQL Server
* Telegram Bot Token (via [@BotFather](https://t.me/BotFather))
* Spoonacular API Key
* Google Gemini API Key

### Installation

1. Clone the repository:
```bash
git clone https://github.com/thena3ik/spring-yummy-bot.git
cd spring-yummy-bot
```

2. Configure application properties:
rename `application.properties.example` to `application.properties` in the `src/main/resources/` directory and apply your credentials:

```properties
# Telegram Bot
bot.token=YOUR_TELEGRAM_BOT_TOKEN

# External APIs
spoonacular.api.key=YOUR_SPOONACULAR_API_KEY
gemini.api.key=YOUR_GEMINI_API_KEY

# Database Connection
spring.datasource.url=jdbc:mysql://localhost:3306/YOUR_DB_NAME
spring.datasource.username=YOUR_DB_USER
spring.datasource.password=YOUR_DB_PASSWORD
```

3. Build and execute the application:

```bash
./mvnw clean install
./mvnw spring-boot:run
```

## Contributing

Contributions, issues, and feature requests are welcome. Feel free to check the [issues page](https://github.com/thena3ik/spring-yummy-bot/issues) if you want to contribute.
