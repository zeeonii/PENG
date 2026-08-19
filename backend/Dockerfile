FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN ./gradlew --version
COPY src src
RUN ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app

# 배포된 인스턴스가 어느 커밋인지 /actuator/info 로 바로 확인하기 위해 이미지에 박아 둔다.
# 런타임 환경변수로 받으면 컨테이너를 다시 띄울 때 실제 이미지와 값이 어긋날 수 있어,
# 이미지에 고정되는 빌드 인자로 받는다.
ARG GIT_COMMIT=unknown
ARG GIT_BRANCH=unknown
ENV DEPLOY_COMMIT=${GIT_COMMIT} \
    DEPLOY_BRANCH=${GIT_BRANCH}

COPY --from=build /app/build/libs/*-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
