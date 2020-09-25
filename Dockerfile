FROM openjdk:13-alpine

RUN mkdir /app
RUN mkdir /config

COPY ./build/libs/esr.jar /app/esr.jar
WORKDIR /app

CMD ["java", "-jar", "-Dcom.femastudios.esr.configDir=/config", "esr.jar"]