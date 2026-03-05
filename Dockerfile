# ==========================================
# ESTÁGIO 1: Build (Compilação do código)
# ==========================================
# Usamos uma imagem com o Maven e Java (estou assumindo o Java 21, altere se usar o 17)
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

# Define o diretório de trabalho dentro do container
WORKDIR /app

# Copia apenas o pom.xml primeiro (isso otimiza o cache do Docker)
COPY pom.xml .

# Baixa as dependências do projeto
RUN mvn dependency:go-offline

# Copia o código-fonte
COPY src ./src

# Compila o projeto gerando o .jar (pulando os testes para ser mais rápido)
RUN mvn clean package -DskipTests

# ==========================================
# ESTÁGIO 2: Execução (Imagem final leve)
# ==========================================
# Usamos apenas o JRE (Java Runtime Environment) para rodar, economizando muita memória
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copia o arquivo .jar que foi gerado no Estágio 1
COPY --from=builder /app/target/*.jar app.jar

# Expõe a porta que a sua API Spring Boot usa
EXPOSE 8080

# Comando que o container vai rodar ao iniciar
ENTRYPOINT ["java", "-jar", "app.jar"]