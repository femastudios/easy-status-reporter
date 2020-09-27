FROM gradle:jdk14 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle shadowJar --no-daemon


FROM openjdk:14-alpine
EXPOSE 7828
RUN mkdir /app
RUN mkdir /config
VOLUME /config

COPY --from=build /home/gradle/src/build/libs/esr.jar /app/esr.jar
WORKDIR /app

CMD ["java", "-jar", "-Dcom.femastudios.esr.configDir=/config", "esr.jar"]