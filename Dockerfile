# Maven + JDK 17 mavjud bo‘lgan rasm
FROM maven:3.8.6-eclipse-temurin-17 AS build

WORKDIR /app
COPY . .
RUN mvn clean package

# Faqat runtime uchun
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /app/target/telegram-bot-1.0-SNAPSHOT.jar bot.jar
CMD ["java", "-jar", "bot.jar"]
