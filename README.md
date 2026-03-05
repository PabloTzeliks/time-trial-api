# Time Trial API

API para gerenciamento de tempos de volta de carros Hot Wheels, construída com **Spring Boot**, **MQTT**, **WebSocket (STOMP)** e **Apache Cassandra**.

---

## Sumário

1. [Arquitetura do Sistema](#arquitetura-do-sistema)
2. [Tecnologias Utilizadas](#tecnologias-utilizadas)
3. [Configuração](#configuração)
4. [Comunicação MQTT](#comunicação-mqtt)
5. [WebSocket (Tempo Real)](#websocket-tempo-real)
6. [REST API](#rest-api)
7. [Schema do Banco de Dados](#schema-do-banco-de-dados)
8. [Fluxo Completo de Comunicação](#fluxo-completo-de-comunicação)
9. [Como Executar](#como-executar)

---

## Arquitetura do Sistema

```
┌──────────────┐        MQTT          ┌─────────────────────────┐
│ Sensor IoT   │ ─────────────────► │                         │
│ (ESP32 /     │  time_trial/        │     Time Trial API      │
│  Raspberry)  │  lap_completed      │     (Spring Boot)       │
└──────────────┘                     │                         │
                                     │  ┌─────────────────┐   │
                                     │  │  MqttSubscriber  │   │
                                     │  │  (Eclipse Paho)  │   │
                                     │  └────────┬────────┘   │
                                     │           │            │
                                     │  ┌────────▼────────┐   │
                                     │  │   LapService     │   │
                                     │  └────────┬────────┘   │
                                     │      ┌────┴────┐       │
                                     │      ▼         ▼       │
                                     │  Cassandra  WebSocket  │
                                     │  (persist)  /topic/..  │
                                     └─────────────────────────┘
                                                  │ WebSocket
                                                  ▼
                                     ┌─────────────────────────┐
                                     │      Frontend            │
                                     │  (React / Angular / Vue) │
                                     └─────────────────────────┘
```

O fluxo é:
1. Um **sensor IoT** detecta a passagem de um carro e publica no MQTT.
2. O **backend** consome a mensagem, salva no Cassandra e retransmite pelo WebSocket.
3. O **frontend** recebe a atualização em tempo real via WebSocket STOMP.
4. O **frontend** também pode chamar a REST API para buscar históricos e gerenciar corridas/carros.

---

## Tecnologias Utilizadas

| Tecnologia           | Papel                                               |
|----------------------|-----------------------------------------------------|
| Spring Boot 3.5      | Framework principal                                 |
| Eclipse Paho MQTT    | Cliente MQTT para receber dados dos sensores       |
| Spring WebSocket / STOMP | Push em tempo real para o frontend             |
| Apache Cassandra     | Banco de dados NoSQL para persistência              |
| Lombok               | Redução de boilerplate                              |

---

## Configuração

Arquivo `src/main/resources/application.yaml`:

```yaml
spring:
  application:
    name: Time Trial API
  cassandra:
    keyspace-name: time_trial
    contact-points: localhost
    port: 9042
    local-datacenter: datacenter1
    schema-action: create_if_not_exists

server:
  port: 8080

mqtt:
  broker-url: tcp://localhost:1883   # URL do broker MQTT
  client-id: time-trial-api          # ID único do cliente
  username:                           # (opcional) usuário do broker
  password:                           # (opcional) senha do broker
  topics:
    lap-completed: time_trial/lap_completed
```

---

## Comunicação MQTT

### Tópico

| Tópico                      | Direção             | Quem publica          |
|-----------------------------|---------------------|-----------------------|
| `time_trial/lap_completed`  | Sensor → Backend   | Sensor IoT (ESP32 etc.) |

> O backend **subscreve** este tópico. O sensor IoT **publica** neste tópico.

### Payload (JSON)

Quando um carro completa uma volta, o sensor deve publicar a seguinte mensagem JSON no tópico `time_trial/lap_completed`:

```json
{
  "carId": "550e8400-e29b-41d4-a716-446655440000",
  "raceId": "660e8400-e29b-41d4-a716-446655440001",
  "lapNumber": 3,
  "timeMillis": 4521
}
```

| Campo        | Tipo    | Descrição                                        |
|--------------|---------|--------------------------------------------------|
| `carId`      | UUID    | ID do carro (obtido via REST API `/api/v1/cars`) |
| `raceId`     | UUID    | ID da corrida (obtido via REST API `/api/v1/races`) |
| `lapNumber`  | Integer | Número da volta (começa em 1)                    |
| `timeMillis` | Long    | Tempo da volta em milissegundos                  |

### QoS

O backend subscreve com **QoS 1** (entrega pelo menos uma vez), garantindo que nenhuma volta seja perdida.

---

## WebSocket (Tempo Real)

O backend usa **STOMP sobre WebSocket** para enviar atualizações em tempo real ao frontend.

### Endpoint de Conexão

```
ws://localhost:8080/ws
```

Com fallback SockJS:
```
http://localhost:8080/ws
```

### Tópicos WebSocket (Destinos STOMP)

| Destino STOMP                        | Descrição                                              |
|--------------------------------------|--------------------------------------------------------|
| `/topic/laps`                        | Recebe **todas** as voltas de todas as corridas        |
| `/topic/race/{raceId}/laps`          | Recebe as voltas de uma corrida específica             |

### Mensagem Recebida (LapTimeResponse)

Sempre que uma volta é registrada via MQTT, o backend envia a seguinte mensagem aos tópicos STOMP:

```json
{
  "raceId": "660e8400-e29b-41d4-a716-446655440001",
  "carId": "550e8400-e29b-41d4-a716-446655440000",
  "lapNumber": 3,
  "timeMillis": 4521,
  "recordedAt": "2024-01-15T14:30:00Z"
}
```

### Exemplo de Conexão no Frontend (JavaScript/TypeScript)

```javascript
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const client = new Client({
  webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
  onConnect: () => {
    // Subscreve todas as voltas
    client.subscribe('/topic/laps', (message) => {
      const lap = JSON.parse(message.body);
      console.log('Nova volta:', lap);
    });

    // Subscreve voltas de uma corrida específica
    const raceId = '660e8400-e29b-41d4-a716-446655440001';
    client.subscribe(`/topic/race/${raceId}/laps`, (message) => {
      const lap = JSON.parse(message.body);
      console.log('Volta na corrida:', lap);
    });
  },
});

client.activate();
```

> **Dependências npm necessárias:**
> ```bash
> npm install @stomp/stompjs sockjs-client
> ```

---

## REST API

Base URL: `http://localhost:8080/api/v1`

### Carros (`/api/v1/cars`)

#### Cadastrar carro
```http
POST /api/v1/cars
Content-Type: application/json

{
  "name": "Camaro Amarelo",
  "driver": "João Silva"
}
```

**Resposta 201:**
```json
{
  "carId": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Camaro Amarelo",
  "driver": "João Silva",
  "createdAt": "2024-01-15T14:00:00Z"
}
```

#### Listar todos os carros
```http
GET /api/v1/cars
```

#### Buscar carro por ID
```http
GET /api/v1/cars/{carId}
```

---

### Corridas (`/api/v1/races`)

#### Criar corrida
```http
POST /api/v1/races
Content-Type: application/json

{
  "name": "Grande Prêmio 2024",
  "totalLaps": 5
}
```

**Resposta 201:**
```json
{
  "raceId": "660e8400-e29b-41d4-a716-446655440001",
  "name": "Grande Prêmio 2024",
  "totalLaps": 5,
  "status": "SCHEDULED",
  "startTime": null,
  "endTime": null,
  "createdAt": "2024-01-15T14:00:00Z"
}
```

**Status possíveis:** `SCHEDULED` | `ONGOING` | `FINISHED`

#### Listar todas as corridas
```http
GET /api/v1/races
```

#### Buscar corrida por ID
```http
GET /api/v1/races/{raceId}
```

#### Iniciar corrida
```http
PUT /api/v1/races/{raceId}/start
```

Sets status to `ONGOING` and records `startTime`.

#### Finalizar corrida
```http
PUT /api/v1/races/{raceId}/finish
```

Sets status to `FINISHED` and records `endTime`.

---

### Voltas (`/api/v1/races/{raceId}/laps`)

#### Listar todas as voltas de uma corrida
```http
GET /api/v1/races/{raceId}/laps
```

**Resposta 200:**
```json
[
  {
    "raceId": "660e8400-e29b-41d4-a716-446655440001",
    "carId": "550e8400-e29b-41d4-a716-446655440000",
    "lapNumber": 1,
    "timeMillis": 4521,
    "recordedAt": "2024-01-15T14:30:01Z"
  },
  {
    "raceId": "660e8400-e29b-41d4-a716-446655440001",
    "carId": "550e8400-e29b-41d4-a716-446655440000",
    "lapNumber": 2,
    "timeMillis": 4398,
    "recordedAt": "2024-01-15T14:30:06Z"
  }
]
```

#### Listar voltas de um carro específico em uma corrida
```http
GET /api/v1/races/{raceId}/laps/car/{carId}
```

---

## Schema do Banco de Dados

### Cassandra Keyspace

```cql
CREATE KEYSPACE IF NOT EXISTS time_trial
  WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};
```

> O Spring Data Cassandra cria as tabelas automaticamente com `schema-action: create_if_not_exists`.

### Tabela: `cars`

```cql
CREATE TABLE IF NOT EXISTS time_trial.cars (
  car_id    UUID PRIMARY KEY,
  name      TEXT,
  driver    TEXT,
  created_at TIMESTAMP
);
```

### Tabela: `races`

```cql
CREATE TABLE IF NOT EXISTS time_trial.races (
  race_id    UUID PRIMARY KEY,
  name       TEXT,
  total_laps INT,
  status     TEXT,
  start_time TIMESTAMP,
  end_time   TIMESTAMP,
  created_at TIMESTAMP
);
```

### Tabela: `lap_times`

```cql
CREATE TABLE IF NOT EXISTS time_trial.lap_times (
  race_id    UUID,
  car_id     UUID,
  lap_number INT,
  time_millis BIGINT,
  recorded_at TIMESTAMP,
  PRIMARY KEY ((race_id), car_id, lap_number)
);
```

---

## Fluxo Completo de Comunicação

```
[SENSOR IoT]                [BACKEND]                  [FRONTEND]
     │                          │                           │
     │  PUBLISH MQTT             │                           │
     │  topic: time_trial/       │                           │
     │         lap_completed     │                           │
     │  payload: {carId, raceId, │                           │
     │   lapNumber, timeMillis}  │                           │
     │ ─────────────────────── ► │                           │
     │                          │  1. Parse JSON             │
     │                          │  2. Save to Cassandra      │
     │                          │  3. Broadcast WebSocket    │
     │                          │ ──────────────────────── ► │
     │                          │  /topic/laps               │
     │                          │  /topic/race/{id}/laps     │
     │                          │                           │
     │                          │    REST API calls          │
     │                          │ ◄──────────────────────── │
     │                          │  GET /api/v1/races         │
     │                          │  GET /api/v1/cars          │
     │                          │  GET /api/v1/races/{id}/laps│
     │                          │ ──────────────────────── ► │
```

### Passo a Passo para o Frontend

1. **Conectar ao WebSocket** em `http://localhost:8080/ws` usando STOMP/SockJS.
2. **Buscar corridas disponíveis** via `GET /api/v1/races`.
3. **Subscrever ao tópico WebSocket** `/topic/race/{raceId}/laps` da corrida selecionada.
4. **Ao receber mensagem WebSocket**, atualizar o placar em tempo real.
5. **Para buscar histórico**, chamar `GET /api/v1/races/{raceId}/laps`.

---

## Como Executar

### Pré-requisitos

- Java 17+
- Apache Cassandra rodando na porta `9042`
- Broker MQTT (ex: Mosquitto) rodando na porta `1883`

### Iniciando o Cassandra (Docker)

```bash
docker run -d --name cassandra -p 9042:9042 cassandra:4.1
```

### Criando o Keyspace no Cassandra

```bash
docker exec -it cassandra cqlsh -e "
  CREATE KEYSPACE IF NOT EXISTS time_trial
  WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};
"
```

### Iniciando o Mosquitto MQTT (Docker)

```bash
docker run -d --name mosquitto -p 1883:1883 eclipse-mosquitto
```

### Executando a API

```bash
./mvnw spring-boot:run
```

A API estará disponível em: `http://localhost:8080`

### Testando com MQTT (mosquitto_pub)

```bash
# Publicar uma volta de teste
mosquitto_pub -h localhost -t "time_trial/lap_completed" -m '{
  "carId": "550e8400-e29b-41d4-a716-446655440000",
  "raceId": "660e8400-e29b-41d4-a716-446655440001",
  "lapNumber": 1,
  "timeMillis": 4521
}'
```
