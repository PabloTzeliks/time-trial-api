# Time Trial API — Relatório Completo do Sistema

## Visão Geral

A **Time Trial API** é uma aplicação back-end construída com **Spring Boot 3** e **Java 21**. Seu propósito é gerenciar a cronometragem de voltas de carrinhos Hot Wheels em uma pista instrumentada com sensores RFID. A aplicação recebe eventos de passagem de carrinho pelo sensor, calcula o tempo de volta, valida se ele é aceitável e mantém um pódio global com os 10 melhores tempos.

---

## Stack Tecnológica

| Tecnologia | Versão | Papel |
|---|---|---|
| Java | 21 | Linguagem principal |
| Spring Boot | 3.5.11 | Framework de aplicação |
| Spring Data Cassandra | (gerenciada pelo Boot) | Persistência de dados |
| Spring WebSocket | (gerenciada pelo Boot) | Notificações em tempo real (pendente) |
| Spring Web | (gerenciada pelo Boot) | API REST (pendente) |
| Eclipse Paho MQTT | 1.2.5 | Comunicação com sensores IoT (pendente) |
| Lombok | (gerenciada pelo Boot) | Redução de boilerplate Java |
| Maven | 3.x | Gerenciamento de build e dependências |

---

## Arquitetura

A aplicação segue uma arquitetura orientada a eventos (*event-driven*) com três camadas principais:

```
[ Sensor RFID / MQTT ] → (CarroPassouNoSensorEvent)
         ↓
[ CalculadoraDeVoltaService ] → valida tempo → (VoltaValidaCalculadaEvent)
         ↓
[ GerenciadorPodioService ] → atualiza pódio → (PainelPrecisaAtualizarEvent)
         ↓
[ NotificadorWebSocket ] → envia update para o front-end via WebSocket (pendente)
```

Os eventos são publicados de forma **assíncrona** (`@Async`) via `ApplicationEventPublisher` do Spring, desacoplando completamente as etapas do fluxo.

---

## Estrutura de Pacotes

```
com.centroweg.iot.time_trial_api
│
├── TimeTrialApiApplication.java          ← Ponto de entrada Spring Boot
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
└── outbound/
    ├── dto/
    │   └── PainelSaidaDTO.java           ← DTO de resposta do painel
    └── websocket/
        └── NotificadorWebSocket.java     ← Notificador WebSocket (pendente)
```

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
Publicado quando um sensor detecta a passagem de um carrinho. Carrega o ID RFID do carrinho e o timestamp exato da passagem.

### `VoltaValidaCalculadaEvent`
```java
record VoltaValidaCalculadaEvent(String rfid, Long tempoVoltaMs)
```
Publicado pela `CalculadoraDeVoltaService` quando uma volta válida é calculada. Carrega o RFID e o tempo da volta em ms.

### `PainelPrecisaAtualizarEvent`
```java
record PainelPrecisaAtualizarEvent()
```
Evento marcador (sem dados) publicado pela `GerenciadorPodioService` após cada atualização do pódio. Destina-se a acionar o envio de um update via WebSocket ao front-end.

---

## Serviços

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

**Configurações relevantes (`application.yaml`):**
```yaml
time-trial:
  secret-keys:
    tempo-minimo-volta: 2000   # ms — limiar anti-bounce
    tempo-maximo-volta: 30000  # ms — limiar de timeout/DNF
```

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

## Repositórios

| Repositório | Tabela | Métodos Customizados |
|---|---|---|
| `JpaPodioGlobalRepository` | `podio_global` | `buscarTop10()` — `SELECT * FROM podio_global WHERE agrupador = 'GERAL' LIMIT 10` |
| `JpaHistoricoCarroRepository` | `historico_carro` | `findFirstByCarroId(String carroId)` — retorna o registro mais recente do carrinho |
| `JpaFeedRecenteRepository` | `feed_recente` | `buscarUltimas10()` — `SELECT * FROM feed_recente WHERE agrupador = 'GERAL' LIMIT 10` |

---

## DTOs de Saída

### `PainelSaidaDTO`
```java
record PainelSaidaDTO(List<PodioGlobal> podio, List<FeedRecente> recentes)
```
Estrutura de resposta destinada a serializar o estado completo do painel:
- `podio`: top 10 tempos do pódio global.
- `recentes`: as últimas 10 voltas completadas (feed ao vivo).

---

## Funcionalidades Pendentes de Implementação

| Componente | Status | Descrição |
|---|---|---|
| `NotificadorWebSocket` | ⚠️ Stub vazio | Deve escutar `PainelPrecisaAtualizarEvent` e fazer *push* do `PainelSaidaDTO` para os clientes conectados via WebSocket/STOMP |
| Integração MQTT | ⚠️ Não implementada | Dependência Eclipse Paho presente no `pom.xml`, mas nenhum cliente MQTT foi configurado para receber mensagens dos sensores RFID e publicar `CarroPassouNoSensorEvent` |
| API REST | ⚠️ Não implementada | Nenhum `@RestController` existe; endpoints HTTP para consulta do painel não foram criados |
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
```

> **Nota:** a configuração de conexão com o Cassandra (host, porta, keyspace, usuário, senha) não está presente no arquivo. Ela deve ser fornecida via variáveis de ambiente ou perfis adicionais ao executar a aplicação.

---

## Como Executar

### Pré-requisitos
- Java 21+
- Apache Cassandra acessível (local ou remoto)
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

## Fluxo Completo de uma Volta (Resumo)

```
1. Carrinho passa no sensor RFID
       ↓
2. [MQTT - pendente] mensagem chega → publica CarroPassouNoSensorEvent(rfid, timestampMs)
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
5. [WebSocket - pendente] NotificadorWebSocket → push PainelSaidaDTO para clientes conectados
```
