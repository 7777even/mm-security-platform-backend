# =============================================================================
# 后端服务镜像（Spring Boot 3 / JDK 17）
#
# 构建（仓库根目录 backend-scaffold/）：
#   docker build -t mm-security-backend .
#
# 运行：
#   docker run --rm -p 8787:8787 \
#     -e SPRING_PROFILES_ACTIVE=dev \
#     -e JWT_SECRET=... -e SIGNATURE_SECRET=... \
#     mm-security-backend
#
# 密钥说明：base application.yml 的 jwt.secret / signature.secret 均为纯 ${...}
# 无默认值，缺失即启动失败（fail-fast）。生产务必通过环境变量注入，切勿打进镜像。
#
# 达梦 DM8 驱动：pom 的 dm profile 依赖 com.dameng:DmJdbcDriver18，需先
# install-file 到本地仓库（达梦 JDBC 驱动受授权限制，不随镜像分发）：
#   mvn install:install-file -Dfile=DmJdbcDriver18.jar -DgroupId=com.dameng \
#       -DartifactId=DmJdbcDriver18 -Dversion=8.1.3 -Dpackaging=jar
# 随后用 --build-arg BUILD_PROFILE=dm 构建，镜像方含达梦驱动。
# =============================================================================

# ---------- 阶段 1：构建 ----------
FROM maven:3.9-eclipse-temurin-17 AS build

ARG BUILD_PROFILE=""

WORKDIR /build

# 先只复制 pom，利用 Docker 层缓存拉取依赖
COPY pom.xml ./
RUN mvn -B -ntp dependency:go-offline ${BUILD_PROFILE:+-P $BUILD_PROFILE}

COPY src ./src
# 跳过测试：测试由 CI 门禁负责；镜像构建只产出产物
RUN mvn -B -ntp package -DskipTests ${BUILD_PROFILE:+-P $BUILD_PROFILE}

# ---------- 阶段 2：运行 ----------
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app
COPY --from=build /build/target/*.jar /app/app.jar

ENV SERVER_PORT=8787
EXPOSE 8787

# 运行期可覆盖：SPRING_PROFILES_ACTIVE=dm|prod|dev、数据源与密钥等
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
