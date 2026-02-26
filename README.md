# Time Trial API — Relatório Completo do Sistema

## Visão Geral

A **Time Trial API** é uma aplicação back-end construída com **Spring Boot 3** e **Java 21**. Seu propósito é gerenciar a cronometragem de voltas de carrinhos Hot Wheels em uma pista instrumentada com sensores RFID. A aplicação recebe eventos de passagem de carrinho via MQTT, calcula o tempo de volta, valida se ele é aceitável, mantém um pódio global com os 10 melhores tempos e notifica os clientes em tempo real via WebSocket.

---

## Stack Tecnológica

| Tecnologia | Versão | Papel |
|---|---|---|
| Java | 21 | Linguagem principal |
| Spring Boot | 3.5.11 | Framework de aplicação |
| Spring Data Cassandra | (gerenciada pelo Boot) | Persistência de dados |
| Spring WebSocket / STOMP | (gerenciada pelo Boot) | Notificações em tempo real para o front-end |
| Spring Web | (gerenciada pelo Boot) | Suporte HTTP / SockJS |
| Eclipse Paho MQTT | 1.2.5 | Recepção de eventos dos sensores IoT |
| Lombok | (gerenciada pelo Boot) | Redução de boilerplate Java |
| Maven | 3.x | Gerenciamento de build e dependências |

---

## Arquitetura

A aplicação segue uma arquitetura orientada a eventos (*event-driven*) com quatro camadas principais:

```
[ Sensor RFID ]
      ↓ (mensagem MQTT com payload = RFID)
[ MqttReceiver ] → publica CarroPassouNoSensorEvent(rfid, timestampMs)
      ↓
[ CalculadoraDeVoltaService ] → valida tempo → publica VoltaValidaCalculadaEvent(rfid, tempoVoltaMs)
      ↓
[ GerenciadorPodioService ] → atualiza pódio no Cassandra → publica PainelPrecisaAtualizarEvent
      ↓
[ NotificadorWebSocket ] → envia PainelSaidaDTO para /topic/painel via STOMP
      ↓
[ Clientes front-end conectados via WebSocket ]
```

Os eventos são publicados de forma **assíncrona** (`@Async`) via `ApplicationEventPublisher` do Spring, usando um pool de threads dedicado (`AsyncConfig`), desacoplando completamente as etapas do fluxo.

---

## Estrutura de Pacotes

```
com.centroweg.iot.time_trial_api
│
├── TimeTrialApiApplication.java          ← Ponto de entrada Spring Boot
│
├── config/                               ← Configurações de infraestrutura
│   ├── AsyncConfig.java                  ← Pool de threads para @Async
│   ├── CassandraConfig.java              ← Keyspace e conexão Cassandra
│   ├── MqttConfig.java                   ← Bean MqttClient (Eclipse Paho)
│   ├── MqttProperties.java               ← Propriedades MQTT (broker, clientId, topic)
│   └── WebSocketConfig.java              ← Endpoint STOMP /ws-time-trial
│
├── core/
│   ├── domain/                           ← Entidades Cassandra (tabelas)
│   │   ├── PodioGlobal.java
│   │   ├── HistoricoCarro.java
│   │   └── FeedRecente.java
│   │
│   ├── event/                            ← Eventos de domínio (Spring Events)
│   │   ├── CarroPassouNoSensorEvent.java
│   │   ├── VoltaValidaCalculadaEvent.java
│   │   └── PainelPrecisaAtualizarEvent.java
│   │
│   ├── repository/                       ← Repositórios Cassandra
│   │   ├── JpaPodioGlobalRepository.java
│   │   ├── JpaHistoricoCarroRepository.java
│   │   └── JpaFeedRecenteRepository.java
│   │
│   └── service/                          ← Lógica de negócio
│       ├── CalculadoraDeVoltaService.java
│       └── GerenciadorPodioService.java
│
├── inbound/
│   └── mqtt/
│       └── MqttReceiver.java             ← Subscrição MQTT e publicação de eventos
│
└── outbound/
    ├── dto/
    │   └── PainelSaidaDTO.java           ← DTO de resposta do painel
    └── websocket/
        └── NotificadorWebSocket.java     ← Push STOMP para /topic/painel
```

---

## Configurações de Infraestrutura (`config/`)

### `AsyncConfig`

Configura o pool de threads utilizado pelo `@Async` do Spring para processar os eventos de domínio de forma assíncrona.

| Parâmetro | Valor |
|---|---|
| Bean name | `eventExecutor` |
| Core pool size | 4 threads |
| Max pool size | 8 threads |
| Queue capacity | 100 tarefas |
| Thread name prefix | `event-` |

### `CassandraConfig`

Configura a conexão com o Apache Cassandra.

| Parâmetro | Valor |
|---|---|
| Keyspace | `corrida` |
| Contact point | `localhost` |
| Porta | `9042` |
| Pacote de entidades | `com.centroweg.iot.time_trial_api.core.domain` |

### `MqttConfig`

Cria e registra o bean `MqttClient` (Eclipse Paho). Conecta ao broker na inicialização e desconecta graciosamente no shutdown (`@PreDestroy`).

| Opção de conexão | Valor |
|---|---|
| Auto-reconnect | `true` |
| Clean session | `true` |
| Connection timeout | `10s` |

As propriedades de conexão (broker URL, client ID, tópico) são injetadas via `MqttProperties`.

### `MqttProperties`

Lê as propriedades com prefixo `mqtt` do `application.yaml` (ou variáveis de ambiente):

| Propriedade | Tipo | Descrição |
|---|---|---|
| `mqtt.broker` | String | URL do broker MQTT (ex.: `tcp://localhost:1883`) |
| `mqtt.clientId` | String | Identificador único do cliente MQTT |
| `mqtt.topic` | String | Tópico ao qual o `MqttReceiver` se inscreve |

### `WebSocketConfig`

Configura o endpoint STOMP para comunicação em tempo real com o front-end.

| Item | Valor |
|---|---|
| Endpoint de conexão | `/ws-time-trial` (com fallback SockJS) |
| CORS | `*` (todos os origens permitidos) |
| Prefixo do broker simples | `/topic` |
| Prefixo de destino de aplicação | `/app` |

---

## Modelo de Dados (Cassandra)

### Tabela: `podio_global`

Armazena o ranking global dos 10 melhores tempos de volta.

| Coluna | Tipo | Papel | Descrição |
|---|---|---|---|
| `agrupador` | text | Partition Key | Sempre `"GERAL"` — agrupa todos os registros em uma única partição |
| `tempo_volta_ms` | bigint | Clustering Key (ASC) | Tempo da volta em milissegundos — ordenação crescente garante os menores tempos primeiro |
| `carro_id` | text | Clustering Key (ASC) | Identificador RFID do carrinho |

> A chave de clustering por `tempo_volta_ms` garante que a query `LIMIT 10` retorne automaticamente os 10 menores tempos (= top 10).
>
> ⚠️ **Limitação conhecida:** usar `"GERAL"` como única chave de partição concentra todas as leituras e escritas em uma única partição Cassandra (*hotspot*), o que não escala horizontalmente. Em um ambiente com alto volume de dados, considere uma estratégia de particionamento baseada em tempo (ex.: por dia ou semana).

### Tabela: `historico_carro`

Registra a **última passagem** de cada carrinho no sensor. Usada para calcular o tempo de volta.

| Coluna | Tipo | Papel | Descrição |
|---|---|---|---|
| `carro_id` | text | Partition Key | Identificador RFID do carrinho |
| `timestamp_ms` | bigint | Clustering Key (DESC) | Timestamp da passagem em ms — ordenação decrescente traz o registro mais recente primeiro |

### Tabela: `feed_recente`

Armazena as últimas 10 voltas completadas para exibição em um feed ao vivo.

| Coluna | Tipo | Papel | Descrição |
|---|---|---|---|
| `agrupador` | text | Partition Key | Sempre `"GERAL"` |
| `timestamp_ms` | bigint | Clustering Key (DESC) | Timestamp da volta — ordenação decrescente traz as mais recentes primeiro |
| `carro_id` | text | Coluna | Identificador RFID do carrinho |
| `tempo_volta_ms` | bigint | Coluna | Tempo da volta em milissegundos |

> ⚠️ **Limitação conhecida:** assim como em `podio_global`, o uso de `"GERAL"` como única chave de partição em `feed_recente` cria um *hotspot* no Cassandra. À medida que o volume de dados crescer, uma estratégia de particionamento por tempo (ex.: por dia) distribuiria melhor a carga entre os nós.

---

## Eventos de Domínio

### `CarroPassouNoSensorEvent`
```java
record CarroPassouNoSensorEvent(String rfid, Long timestampMs)
```
Publicado pelo `MqttReceiver` quando uma mensagem chega no tópico MQTT subscrito. O `rfid` é o payload da mensagem (string com o ID do carrinho) e `timestampMs` é o instante da recepção (`System.currentTimeMillis()`).

### `VoltaValidaCalculadaEvent`
```java
record VoltaValidaCalculadaEvent(String rfid, Long tempoVoltaMs)
```
Publicado pela `CalculadoraDeVoltaService` quando uma volta válida é calculada. Carrega o RFID e o tempo da volta em ms.

### `PainelPrecisaAtualizarEvent`
```java
record PainelPrecisaAtualizarEvent()
```
Evento marcador (sem dados) publicado pela `GerenciadorPodioService` após cada atualização do pódio. Aciona o `NotificadorWebSocket` para fazer push do estado atual ao front-end.

---

## Componentes de Entrada (`inbound/`)

### `MqttReceiver`

**Responsabilidade:** Conectar-se ao broker MQTT e transformar mensagens recebidas em eventos de domínio Spring.

**Funcionamento:**
1. Ao inicializar (`@PostConstruct`), inscreve-se no tópico definido em `mqtt.topic`.
2. Para cada mensagem recebida:
   - Lê o payload como string (ID RFID do carrinho).
   - Captura o timestamp atual (`System.currentTimeMillis()`).
   - Publica `CarroPassouNoSensorEvent(rfid, timestamp)` no contexto Spring.

---

## Serviços (`core/service/`)

### `CalculadoraDeVoltaService`

**Responsabilidade:** Receber eventos de passagem de sensor e determinar se uma volta válida foi completada.

**Fluxo:**
1. Escuta `CarroPassouNoSensorEvent` (`@EventListener @Async`).
2. Busca a última passagem do carrinho na tabela `historico_carro`.
3. **Novo carrinho (nunca visto):** salva o timestamp atual como marco zero e encerra.
4. **Calcula `tempoDaVolta = timestampAtual - ultimaPassagem`**.
5. **Bounce/Ruído** (`tempoDaVolta < tempoMinimoVolta`, padrão 2000ms): ignora a passagem (evita leituras duplicadas do sensor).
6. **DNF/Timeout** (`tempoDaVolta > tempoMaximoVolta`, padrão 30000ms): o carrinho saiu da pista ou reiniciou; salva novo marco zero sem registrar volta.
7. **Volta válida:** salva novo marco zero e publica `VoltaValidaCalculadaEvent`.

---

### `GerenciadorPodioService`

**Responsabilidade:** Manter o pódio global atualizado com os 10 menores tempos.

**Fluxo:**
1. Escuta `VoltaValidaCalculadaEvent` (`@EventListener @Async`).
2. Busca o top 10 atual da tabela `podio_global`.
3. **Carrinho já está no pódio:**
   - Se o novo tempo for melhor (menor), remove o registro antigo e insere o novo.
   - Caso contrário, nenhuma alteração.
4. **Carrinho fora do pódio:**
   - Se há menos de 10 registros, insere diretamente.
   - Se o novo tempo for melhor que o 10º colocado, remove o 10º e insere o novo.
5. Publica `PainelPrecisaAtualizarEvent` ao final, independentemente de ter havido alteração.

---

## Componentes de Saída (`outbound/`)

### `NotificadorWebSocket`

**Responsabilidade:** Enviar o estado atual do painel a todos os clientes WebSocket conectados sempre que o pódio for atualizado.

**Funcionamento:**
1. Escuta `PainelPrecisaAtualizarEvent` (`@EventListener @Async`).
2. Busca o top 10 via `JpaPodioGlobalRepository.buscarTop10()`.
3. Busca as últimas 10 voltas via `JpaFeedRecenteRepository.buscarUltimas10()`.
4. Monta um `PainelSaidaDTO` e envia para o tópico STOMP **`/topic/painel`** via `SimpMessagingTemplate`.

### `PainelSaidaDTO`
```java
record PainelSaidaDTO(List<PodioGlobal> podio, List<FeedRecente> recentes)
```
Payload enviado via WebSocket para o front-end:
- `podio`: top 10 tempos do pódio global.
- `recentes`: as últimas 10 voltas completadas (feed ao vivo).

---

## Repositórios

| Repositório | Tabela | Métodos Customizados |
|---|---|---|
| `JpaPodioGlobalRepository` | `podio_global` | `buscarTop10()` — `SELECT * FROM podio_global WHERE agrupador = 'GERAL' LIMIT 10` |
| `JpaHistoricoCarroRepository` | `historico_carro` | `findFirstByCarroId(String carroId)` — retorna o registro mais recente do carrinho |
| `JpaFeedRecenteRepository` | `feed_recente` | `buscarUltimas10()` — `SELECT * FROM feed_recente WHERE agrupador = 'GERAL' LIMIT 10` |

---

## Funcionalidades Pendentes de Implementação

| Componente | Status | Descrição |
|---|---|---|
| API REST | ⚠️ Não implementada | Nenhum `@RestController` existe; endpoints HTTP para consulta sob demanda do painel não foram criados |
| Testes unitários | ⚠️ Mínimos | Apenas o teste de carregamento de contexto `TimeTrialApiApplicationTests` existe |

---

## Configuração (`src/main/resources/application.yaml`)

```yaml
spring:
  application:
    name: Time Trial API

time-trial:
  secret-keys:
    tempo-minimo-volta: 2000   # Tempo mínimo de uma volta válida (ms)
    tempo-maximo-volta: 30000  # Tempo máximo antes de considerar DNF (ms)

mqtt:
  broker: tcp://localhost:1883   # URL do broker MQTT
  clientId: time-trial-api       # ID do cliente MQTT
  topic: rfid/sensor             # Tópico a ser subscrito
```

> **Nota:** as configurações de Cassandra (keyspace `corrida`, host `localhost`, porta `9042`) estão fixas em `CassandraConfig.java`. Para ambientes diferentes de desenvolvimento, sobrescreva via subclasse ou propriedades externas.

---

## Como Executar

### Pré-requisitos
- Java 21+
- Apache Cassandra (keyspace `corrida`, porta `9042`)
- Broker MQTT acessível (ex.: Mosquitto na porta `1883`)
- Maven 3.x (ou usar o wrapper `./mvnw`)

### Build
```bash
./mvnw clean package
```

### Execução
```bash
./mvnw spring-boot:run
```

ou, após o build:
```bash
java -jar target/time-trial-api-0.0.1-SNAPSHOT.jar
```

### Testes
```bash
./mvnw test
```

---

## WebSocket — Como Consumir no Front-end

O front-end deve conectar-se ao endpoint SockJS/STOMP e se inscrever no tópico `/topic/painel` para receber atualizações automáticas do painel.

**Exemplo com STOMP.js:**
```javascript
// Em desenvolvimento: usar URL relativa. Em produção, substituir pelo host do servidor.
// Exemplo: new SockJS('http://meu-servidor:8080/ws-time-trial')
const socket = new SockJS('/ws-time-trial');
const stompClient = Stomp.over(socket);

stompClient.connect({}, () => {
  stompClient.subscribe('/topic/painel', (message) => {
    const painel = JSON.parse(message.body);
    // painel.podio  → lista com top 10
    // painel.recentes → lista com últimas 10 voltas
  });
});
```

---

## Fluxo Completo de uma Volta (Resumo)

```
1. Carrinho passa no sensor RFID
       ↓
2. MqttReceiver recebe mensagem MQTT (payload = RFID do carrinho)
   → publica CarroPassouNoSensorEvent(rfid, System.currentTimeMillis())
       ↓
3. CalculadoraDeVoltaService.onCarroPassou()
   ├─ Carrinho novo? → salva marco zero → fim
   ├─ tempoDaVolta < 2000ms? → bounce, ignora → fim
   ├─ tempoDaVolta > 30000ms? → DNF, salva novo marco zero → fim
   └─ Volta válida → salva marco zero + publica VoltaValidaCalculadaEvent(rfid, tempoVoltaMs)
       ↓
4. GerenciadorPodioService.onVoltaValida()
   ├─ Já no pódio e tempo melhor? → remove antigo, insere novo
   ├─ Fora do pódio e há vaga? → insere
   ├─ Fora do pódio e melhor que o 10º? → remove 10º, insere novo
   └─ Publica PainelPrecisaAtualizarEvent
       ↓
5. NotificadorWebSocket.onPainelPrecisaAtualizar()
   → busca top10 + ultimas10 → monta PainelSaidaDTO
   → envia para /topic/painel via SimpMessagingTemplate
       ↓
6. Todos os clientes STOMP inscritos em /topic/painel recebem o painel atualizado
```
