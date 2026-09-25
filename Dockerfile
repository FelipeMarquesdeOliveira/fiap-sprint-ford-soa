# ---------- build ----------
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw -B -q dependency:go-offline
COPY src src
RUN ./mvnw -B -q package -DskipTests

# ---------- runtime ----------
FROM eclipse-temurin:25-jre
WORKDIR /app
RUN useradd --system --uid 1001 vinshare
COPY --from=build /app/target/vinshare-*.jar app.jar
USER vinshare
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
