# LeafON API

Backend da LeafON desenvolvido em Kotlin com Spring Boot. A API concentra autenticacao via JWT, cadastro de usuarios, gerenciamento de smart pots, rotinas, registro de telemetria e consulta de alertas.

## Integrantes

- Rafael Ferreira dos Santos
- Miguel Gomes de Lima Coyado Vieira

## Stack

- Kotlin 2.2.21
- Spring Boot 4.0.3
- Java 21
- Gradle Wrapper 9.3.1
- Spring Web MVC
- Spring Data JPA
- Spring Security
- Bean Validation
- PostgreSQL
- OAuth2 Resource Server com JWT

## Estrutura

```text
.
|-- build.gradle.kts
|-- settings.gradle.kts
|-- gradlew
|-- gradlew.bat
|-- gradle/
|   `-- wrapper/
|-- src/
|   |-- main/
|   |   |-- kotlin/
|   |   |   `-- com/
|   |   |       `-- leafon/
|   |   |           |-- LeafonApplication.kt
|   |   |           |-- alert/
|   |   |           |-- auth/
|   |   |           |-- common/
|   |   |           |-- irrigation/
|   |   |           |-- routine/
|   |   |           |-- smartpot/
|   |   |           |-- telemetry/
|   |   |           `-- user/
|   |   `-- resources/
|   |       `-- application.properties
|   `-- test/
|       `-- kotlin/
|           `-- com/
|               `-- leafon/
`-- README.md
```

Pacotes principais:

- `auth`: autenticacao e integracao com JWT do Supabase.
- `user`: CRUD de usuarios.
- `smartpot`: cadastro e ownership dos vasos.
- `routine`: configuracao logica de rotinas de irrigacao e luminosidade.
- `telemetry`: registro e consulta de leituras.
- `alert`: alertas gerados a partir da telemetria.
- `common`: seguranca, excecoes e configuracoes compartilhadas.

Padrao interno dos dominios:

- `controller`: endpoints HTTP.
- `service`: regras de negocio.
- `repository`: acesso ao banco via Spring Data JPA.
- `entity`: entidades persistidas.
- `dto`: contratos de entrada e saida.
- `mapper`: conversao entre entidade e DTO.

## Requisitos

- JDK 21
- PostgreSQL acessivel pela aplicacao
- Variavel `SUPABASE_DATABASE_PASSWORD` configurada quando o `application.properties` usa placeholder

O projeto usa Gradle Wrapper, entao nao precisa de instalacao manual do Gradle.

## Configuracao

Arquivo principal:

```text
src/main/resources/application.properties
```

Exemplo de variavel de ambiente:

Windows PowerShell:

```powershell
$env:SUPABASE_DATABASE_PASSWORD="sua_senha"
```

Linux/macOS:

```bash
export SUPABASE_DATABASE_PASSWORD="sua_senha"
```

Exemplo minimo de configuracao para desenvolvimento:

```properties
spring.application.name=leafon-api

spring.datasource.url=jdbc:postgresql://localhost:5432/leafon
spring.datasource.username=postgres
spring.datasource.password=${SUPABASE_DATABASE_PASSWORD:}
spring.datasource.driverClassName=org.postgresql.Driver

leafon.security.supabase.jwt.issuer-uri=https://SEU_PROJETO.supabase.co/auth/v1
leafon.security.supabase.jwt.jwk-set-uri=https://SEU_PROJETO.supabase.co/auth/v1/.well-known/jwks.json
leafon.security.supabase.jwt.audience=authenticated
leafon.supabase.admin.project-url=https://SEU_PROJETO.supabase.co
leafon.supabase.admin.service-role-key=${SUPABASE_SERVICE_ROLE_KEY:}
leafon.supabase.admin.email-confirm=true
```

Observacoes importantes:

- `issuer-uri`, `jwk-set-uri` e o JWT das requisicoes precisam pertencer ao mesmo projeto Supabase.
- O projeto nao possui migracoes versionadas neste momento. As tabelas precisam existir no banco.
- A tabela `smartpots` precisa usar `uuid` em `id` e `user_id`.

## Banco de dados

### users

A entidade `User` esta mapeada para a tabela `users`.

```sql
CREATE TABLE IF NOT EXISTS users (
    id uuid PRIMARY KEY,
    email varchar(255) NOT NULL UNIQUE,
    name varchar(255),
    phone varchar(255),
    created_at timestamp with time zone,
    updated_at timestamp with time zone
);
```

### smartpots

A entidade `SmartPot` esta mapeada para a tabela `smartpots`.

```sql
CREATE TABLE IF NOT EXISTS smartpots (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL REFERENCES users(id),
    plant_name varchar(255) NOT NULL,
    humidity_min integer NOT NULL CHECK (humidity_min BETWEEN 0 AND 100),
    device_id varchar(255) UNIQUE,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL
);
```

### telemetry_readings

A entidade `TelemetryReading` esta mapeada para a tabela `telemetry_readings`.

```sql
CREATE TABLE IF NOT EXISTS telemetry_readings (
    id uuid PRIMARY KEY,
    smart_pot_id uuid NOT NULL REFERENCES smartpots(id),
    soil_humidity integer NOT NULL CHECK (soil_humidity BETWEEN 0 AND 100),
    temperature double precision NOT NULL,
    luminosity double precision NOT NULL,
    read_at timestamp with time zone NOT NULL,
    created_at timestamp with time zone NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_telemetry_readings_smart_pot_read_at
    ON telemetry_readings (smart_pot_id, read_at DESC);
```

### routines

A entidade `Routine` esta mapeada para a tabela `routines`.

No MVP atual, a rotina e apenas uma configuracao logica do sistema. Nao ha scheduler automatico nem acionamento real de hardware.

```sql
CREATE TABLE IF NOT EXISTS routines (
    id uuid PRIMARY KEY,
    smart_pot_id uuid NOT NULL REFERENCES smartpots(id),
    type varchar(30) NOT NULL CHECK (type IN ('IRRIGATION', 'LIGHTING')),
    name varchar(255) NOT NULL,
    scheduled_time time NOT NULL,
    days_of_week varchar(255) NOT NULL,
    duration_sec integer NOT NULL CHECK (duration_sec > 0),
    active boolean NOT NULL,
    last_executed_at timestamp with time zone,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_routines_smart_pot_created_at
    ON routines (smart_pot_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_routines_smart_pot_active
    ON routines (smart_pot_id, active);
```

### alerts

A entidade `Alert` esta mapeada para a tabela `alerts`.

No MVP atual, um alerta e criado automaticamente quando uma `TelemetryReading` chega com `soil_humidity < smartpots.humidity_min`.

```sql
CREATE TABLE IF NOT EXISTS alerts (
    id uuid PRIMARY KEY,
    smart_pot_id uuid NOT NULL REFERENCES smartpots(id),
    telemetry_reading_id uuid REFERENCES telemetry_readings(id),
    type varchar(50) NOT NULL CHECK (type IN ('LOW_SOIL_HUMIDITY')),
    message text NOT NULL,
    status varchar(20) NOT NULL CHECK (status IN ('PENDING', 'READ')),
    created_at timestamp with time zone NOT NULL,
    read_at timestamp with time zone
);

CREATE INDEX IF NOT EXISTS idx_alerts_smart_pot_created_at
    ON alerts (smart_pot_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_alerts_smart_pot_status_created_at
    ON alerts (smart_pot_id, status, created_at DESC);
```

## Como rodar

Windows:

```powershell
.\gradlew.bat bootRun
```

Linux/macOS:

```bash
./gradlew bootRun
```

URL padrao:

```text
http://localhost:8080
```

## Comandos uteis

```powershell
.\gradlew.bat tasks
.\gradlew.bat test
.\gradlew.bat build
.\gradlew.bat bootJar
.\gradlew.bat clean
```

Executar o jar:

```powershell
java -jar build/libs/leafon-api-0.0.1-SNAPSHOT.jar
```

Em Linux/macOS, substitua `.\gradlew.bat` por `./gradlew`.

## Endpoints

Rotas implementadas:

```text
GET    /users
GET    /users/me
GET    /users/{id}
POST   /users
PUT    /users/me
PUT    /users/{id}
DELETE /users/me
DELETE /users/{id}

POST   /smart-pots
GET    /smart-pots
GET    /smart-pots/{id}
PUT    /smart-pots/{id}
DELETE /smart-pots/{id}

POST   /routines
GET    /routines
GET    /routines/{id}
PUT    /routines/{id}
PATCH  /routines/{id}/activate
PATCH  /routines/{id}/deactivate
PATCH  /routines/{id}/simulate-execution
DELETE /routines/{id}

POST   /telemetry
GET    /telemetry
GET    /telemetry/latest

GET    /alerts
GET    /alerts/unread
PATCH  /alerts/{id}/read
```

Regras relevantes:

- O `userId` nao vem no body. Ele e extraido do JWT autenticado.
- Um usuario so pode acessar `smart-pots`, rotinas, leituras e alertas dos proprios recursos.
- `POST /routines` exige um `smartPotId` pertencente ao usuario autenticado.
- `GET /routines` ordena por `createdAt DESC`.
- `PATCH /routines/{id}/activate` e `PATCH /routines/{id}/deactivate` apenas alteram o status logico da rotina.
- `PATCH /routines/{id}/simulate-execution` apenas preenche `lastExecutedAt`.
- `POST /telemetry` ainda recebe `smartPotId` no body.
- `GET /telemetry` ordena por `readAt DESC`.
- `GET /alerts` e `GET /alerts/unread` ordenam por `createdAt DESC`.
- `PATCH /alerts/{id}/read` marca o alerta como `READ` e preenche `readAt`.
- `soilHumidity` abaixo de `humidityMin` cria automaticamente um alerta `LOW_SOIL_HUMIDITY`.

Respostas esperadas mais comuns:

- `400 Bad Request`: validacao invalida
- `401 Unauthorized`: token ausente ou invalido
- `403 Forbidden`: recurso acessivel, mas fora do ownership do usuario
- `404 Not Found`: recurso inexistente ou, no caso de alertas, nao pertencente ao usuario
- `409 Conflict`: conflito de dados, como `deviceId` duplicado

## Exemplos

Criar telemetria:

```bash
curl -X POST http://localhost:8080/telemetry \
  -H "Authorization: Bearer SEU_JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "smartPotId": "273f9192-d2c1-467d-9855-3f0e502e9f42",
    "soilHumidity": 67,
    "temperature": 24.5,
    "luminosity": 812.3,
    "readAt": "2026-05-12T14:30:00Z"
  }'
```

Listar telemetria de um vaso:

```bash
curl -X GET "http://localhost:8080/telemetry?smartPotId=273f9192-d2c1-467d-9855-3f0e502e9f42" \
  -H "Authorization: Bearer SEU_JWT"
```

Buscar a ultima leitura:

```bash
curl -X GET "http://localhost:8080/telemetry/latest?smartPotId=273f9192-d2c1-467d-9855-3f0e502e9f42" \
  -H "Authorization: Bearer SEU_JWT"
```

Criar rotina:

```bash
curl -X POST http://localhost:8080/routines \
  -H "Authorization: Bearer SEU_JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "smartPotId": "273f9192-d2c1-467d-9855-3f0e502e9f42",
    "type": "IRRIGATION",
    "name": "Irrigacao da manha",
    "scheduledTime": "08:30:00",
    "daysOfWeek": "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY",
    "durationSec": 120,
    "active": true
  }'
```

Listar rotinas:

```bash
curl -X GET http://localhost:8080/routines \
  -H "Authorization: Bearer SEU_JWT"
```

Simular execucao de rotina:

```bash
curl -X PATCH http://localhost:8080/routines/0f6b5c77-84c4-4ad5-8b69-8a87d9f7d2e1/simulate-execution \
  -H "Authorization: Bearer SEU_JWT"
```

Listar alertas:

```bash
curl -X GET http://localhost:8080/alerts \
  -H "Authorization: Bearer SEU_JWT"
```

Listar alertas nao lidos:

```bash
curl -X GET http://localhost:8080/alerts/unread \
  -H "Authorization: Bearer SEU_JWT"
```

Marcar alerta como lido:

```bash
curl -X PATCH http://localhost:8080/alerts/0f6b5c77-84c4-4ad5-8b69-8a87d9f7d2e1/read \
  -H "Authorization: Bearer SEU_JWT"
```

## Links

- Frontend: https://github.com/RafaSantos19/LeafON-KMP
- Backend: https://github.com/RafaSantos19/LeafON-API
- Documento parcial do projeto: https://docs.google.com/document/d/1GGbEGgVE6KhAxyz87omWVD5X1HY0fGU79IRKmRMV-Ec/edit?usp=sharing
