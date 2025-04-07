# 1. Maven orqali build qilamiz
FROM maven:3.8.6-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package

# 2. Endi faqat .jar faylni ishga tushiramiz
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/telegram-bot-1.0-SNAPSHOT.jar bot.jar
CMD ["java", "-jar", "bot.jar"]
