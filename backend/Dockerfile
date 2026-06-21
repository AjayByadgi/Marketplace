FROM gradle:8.14-jdk21 AS builder
# pulls a prebuilt image from docker hub that has gradle and java 21 installed

WORKDIR /app
# sets working directory inside container, same as cd / app it doenst exist it creates it

COPY . .
# copies everything from project root ( left . ) into /app ( right . ) in the container
# so src/ build.gradle.kkts, settings.gradle.kts , all of it is in in ontinaer

RUN gradle bootJar --no-daemon
# runs the gradle build inside the container, into one jar file

FROM eclipse-temurin:21-jre-jammy AS runner
# starts a new stage with a clean image that only has java 21 runtime, this is the image that will be used to run the application

# Sets /app as the workign director in the new clean image
WORKDIR /app

# copy the jar files from the build stage

COPY --from=builder /app/build/libs/*.jar app.jar
# handoff the jar file from hte builder stage to the run stage, only grabs the compiled jar, everything else from the build stage is discarded

EXPOSE 8080
# documents that this container listens on port 8080, this doesnt opent he port , the actual ports:"8080:8080" in copose.yml does this, just meta data

ENTRYPOINT ["java", "-jar", "app.jar"]
