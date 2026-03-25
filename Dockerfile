# ============================================
# Stage 1: Builder - Construcción y extracción de capas
# ============================================
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copiar archivos esenciales para la construcción
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Dar permisos al wrapper y descargar dependencias (aprovecha caché de capas)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Construir la aplicación
RUN ./mvnw clean package -DskipTests -B

# Extraer las capas del JAR (optimización para Spring Boot)
RUN java -Djarmode=layertools -jar target/*.jar extract

# ============================================
# Stage 2: Runtime - Imagen final ligera y segura
# ============================================
FROM eclipse-temurin:17-jre-alpine

# Metadatos
LABEL maintainer="Valle Grande"
LABEL version="1.0.0"

# Variables de entorno para optimizar JVM en contenedores
ENV SPRING_PROFILES_ACTIVE=prod \
    TZ=America/Lima \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseSerialGC"

# Instalar herramientas necesarias y crear usuario no root
RUN apk add --no-cache tzdata dumb-init && \
    addgroup -g 1001 -S appuser && \
    adduser -u 1001 -S appuser -G appuser && \
    mkdir -p /app && \
    chown -R appuser:appuser /app

WORKDIR /app

# Copiar capas extraídas desde el builder (ordenadas de menos a más propensas a cambios)
COPY --from=builder --chown=appuser:appuser /app/dependencies/ ./
COPY --from=builder --chown=appuser:appuser /app/spring-boot-loader/ ./
COPY --from=builder --chown=appuser:appuser /app/snapshot-dependencies/ ./
COPY --from=builder --chown=appuser:appuser /app/application/ ./

# Usuario no root por seguridad
USER appuser

# Exponer puerto del Gateway
EXPOSE 5000

# Healthcheck interno
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:5000/actuator/health || exit 1

# Entrypoint usando dumb-init y el Launcher oficial de Spring Boot
ENTRYPOINT ["dumb-init", "java", "org.springframework.boot.loader.launch.JarLauncher"]
