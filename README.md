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
- `routine`: cadastro e simulacao logica de rotinas de irrigacao e luminosidade.
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
- Variaveis de banco e JWT configuradas no ambiente

O projeto usa Gradle Wrapper, entao nao precisa de instalacao manual do Gradle.

## Configuracao

Arquivo principal:

```text
src/main/resources/application.properties
```

Exemplo de variavel de ambiente:

Windows PowerShell:

```powershell
$env:DATABASE_URL="jdbc:postgresql://localhost:5432/leafon"
$env:DATABASE_USERNAME="postgres"
$env:SUPABASE_DATABASE_PASSWORD="sua_senha"
$env:SUPABASE_JWT_ISSUER_URI="https://SEU_PROJETO.supabase.co/auth/v1"
$env:SUPABASE_JWK_SET_URI="https://SEU_PROJETO.supabase.co/auth/v1/.well-known/jwks.json"
```

Linux/macOS:

```bash
export DATABASE_URL="jdbc:postgresql://localhost:5432/leafon"
export DATABASE_USERNAME="postgres"
export SUPABASE_DATABASE_PASSWORD="sua_senha"
export SUPABASE_JWT_ISSUER_URI="https://SEU_PROJETO.supabase.co/auth/v1"
export SUPABASE_JWK_SET_URI="https://SEU_PROJETO.supabase.co/auth/v1/.well-known/jwks.json"
```

Exemplo minimo de configuracao para desenvolvimento:

```properties
spring.application.name=leafon-api

spring.datasource.url=${DATABASE_URL:jdbc:postgresql://aws-0-us-west-2.pooler.supabase.com:5432/postgres?sslmode=require}
spring.datasource.username=${DATABASE_USERNAME:postgres.cieqfhwerpxomfvelojq}
spring.datasource.password=${SUPABASE_DATABASE_PASSWORD:}
spring.datasource.driver-class-name=org.postgresql.Driver

leafon.security.supabase.jwt.issuer-uri=${SUPABASE_JWT_ISSUER_URI:https://cieqfhwerpxomfvelojq.supabase.co/auth/v1}
leafon.security.supabase.jwt.jwk-set-uri=${SUPABASE_JWK_SET_URI:https://cieqfhwerpxomfvelojq.supabase.co/auth/v1/.well-known/jwks.json}
leafon.security.supabase.jwt.audience=${SUPABASE_JWT_AUDIENCE:authenticated}
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
    air_humidity double precision NOT NULL CHECK (air_humidity BETWEEN 0 AND 100),
    temperature double precision NOT NULL,
    luminosity varchar(10) NOT NULL CHECK (luminosity IN ('CLARO', 'ESCURO')),
    read_at timestamp with time zone NOT NULL,
    created_at timestamp with time zone NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_telemetry_readings_smart_pot_read_at
    ON telemetry_readings (smart_pot_id, read_at DESC);
```

### routines

A entidade `Routine` esta mapeada para a tabela `routines`.

No MVP atual, a rotina e apenas uma configuracao logica do sistema. O backend permite cadastrar, consultar, alterar, ativar, desativar e simular a execucao de uma rotina. Nao ha scheduler automatico, comunicacao MQTT nem acionamento real de bomba, iluminacao ou outro hardware.

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

No MVP atual, alertas sao criados automaticamente quando uma `TelemetryReading` viola limites configurados ou fixos do sistema.

```sql
CREATE TABLE IF NOT EXISTS alerts (
    id uuid PRIMARY KEY,
    smart_pot_id uuid NOT NULL REFERENCES smartpots(id),
    telemetry_reading_id uuid REFERENCES telemetry_readings(id),
    type varchar(50) NOT NULL CHECK (type IN ('LOW_SOIL_HUMIDITY', 'LOW_AIR_HUMIDITY', 'HIGH_TEMPERATURE')),
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

Se a tabela `alerts` ja existir com a constraint antiga aceitando apenas `LOW_SOIL_HUMIDITY`, atualize o `CHECK`:

```sql
ALTER TABLE alerts DROP CONSTRAINT IF EXISTS ck_alerts_type;

ALTER TABLE alerts ADD CONSTRAINT ck_alerts_type
    CHECK (type IN ('LOW_SOIL_HUMIDITY', 'LOW_AIR_HUMIDITY', 'HIGH_TEMPERATURE'));
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

Os testes automatizados usam H2 em memoria e nao exigem credenciais externas.

Para executar `system-tests-telemetry.ps1`, configure dados validos no ambiente:

```powershell
$env:LEAFON_TEST_TOKEN="jwt_valido"
$env:LEAFON_TEST_SMART_POT_ID="uuid_do_smart_pot"
$env:LEAFON_TEST_OTHER_USER_SMART_POT_ID="uuid_de_outro_usuario"
.\system-tests-telemetry.ps1
```

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

POST   /telemetry?smartPotId={uuid}
GET    /telemetry?smartPotId={uuid}
GET    /telemetry/latest?smartPotId={uuid}

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
- `POST /telemetry` recebe `smartPotId` como query parameter.
- `GET /telemetry` ordena por `readAt DESC`.
- `GET /alerts` e `GET /alerts/unread` ordenam por `createdAt DESC`.
- `PATCH /alerts/{id}/read` marca o alerta como `READ` e preenche `readAt`.
- `soilHumidity` abaixo de `humidityMin` cria automaticamente um alerta `LOW_SOIL_HUMIDITY`.
- `airHumidity` abaixo de `40.0` cria automaticamente um alerta `LOW_AIR_HUMIDITY`.
- `temperature` acima de `35.0` cria automaticamente um alerta `HIGH_TEMPERATURE`.

### Contrato de telemetria

O `POST /telemetry` exige o `smartPotId` como query parameter e um JWT valido. O horario da leitura (`readAt`) e gerado automaticamente pelo servidor.

Payload:

```json
{
  "soilHumidity": 92,
  "airHumidity": 68.4,
  "temperature": 21.4,
  "luminosityStatus": "CLARO"
}
```

Campos:

| Campo | Tipo | Regra |
|---|---|---|
| `soilHumidity` | inteiro | Valor entre 0 e 100 |
| `airHumidity` | decimal | Valor entre 0 e 100 |
| `temperature` | decimal | Temperatura coletada pelo sensor |
| `luminosityStatus` | texto | Aceita somente `CLARO` ou `ESCURO` |

Campos adicionais enviados pelo sensor, como `soilHumidityRaw`, `luminosityDigital` e `readAt`, sao aceitos e ignorados.

As respostas do `POST /telemetry`, `GET /telemetry` e `GET /telemetry/latest` usam o mesmo formato:

```json
{
  "soilHumidity": 92,
  "airHumidity": 68.4,
  "temperature": 21.4,
  "luminosityStatus": "CLARO"
}
```

Respostas esperadas mais comuns:

- `400 Bad Request`: validacao invalida
- `401 Unauthorized`: token ausente ou invalido
- `403 Forbidden`: recurso acessivel, mas fora do ownership do usuario
- `404 Not Found`: recurso inexistente ou, no caso de alertas, nao pertencente ao usuario
- `409 Conflict`: conflito de dados, como `deviceId` duplicado

## Exemplos

### Testar no Insomnia

Crie uma requisicao com as seguintes configuracoes:

- Metodo: `POST`
- URL: `http://localhost:8080/telemetry?smartPotId=SEU_SMART_POT_ID`
- Auth: `Bearer Token`
- Token: JWT valido do usuario proprietario do vaso
- Body: `JSON`

Headers:

```text
Content-Type: application/json
Authorization: Bearer SEU_JWT
```

Body:

```json
{
  "soilHumidity": 92,
  "soilHumidityRaw": 352,
  "airHumidity": 68.4,
  "temperature": 21.4,
  "luminosityStatus": "CLARO",
  "luminosityDigital": 0,
  "readAt": "2026-05-30T12:00:00Z"
}
```

Os campos `soilHumidityRaw`, `luminosityDigital` e `readAt` nao sao persistidos. O servidor gera o horario da leitura automaticamente.

Resposta esperada (`201 Created`):

```json
{
  "soilHumidity": 92,
  "airHumidity": 68.4,
  "temperature": 21.4,
  "luminosityStatus": "CLARO"
}
```

### Exemplos com curl

Criar telemetria:

```bash
curl -X POST "http://localhost:8080/telemetry?smartPotId=273f9192-d2c1-467d-9855-3f0e502e9f42" \
  -H "Authorization: Bearer SEU_JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "soilHumidity": 67,
    "soilHumidityRaw": 352,
    "airHumidity": 68.4,
    "temperature": 24.5,
    "luminosityStatus": "CLARO",
    "luminosityDigital": 0,
    "readAt": "2026-05-30T12:00:00Z"
  }'
```

Resposta (`201 Created`):

```json
{
  "soilHumidity": 67,
  "airHumidity": 68.4,
  "temperature": 24.5,
  "luminosityStatus": "CLARO"
}
```

Listar telemetria de um vaso:

```bash
curl -X GET "http://localhost:8080/telemetry?smartPotId=273f9192-d2c1-467d-9855-3f0e502e9f42" \
  -H "Authorization: Bearer SEU_JWT"
```

Resposta (`200 OK`):

```json
[
  {
    "soilHumidity": 67,
    "airHumidity": 68.4,
    "temperature": 24.5,
    "luminosityStatus": "CLARO"
  }
]
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

## Simulacao MQTT

O backend possui ingestao de telemetria por MQTT, desativavel por configuracao e reutilizando o dominio existente. Consulte [MQTT_SIMULATION.md](MQTT_SIMULATION.md) para subir o broker local, publicar uma leitura simulada e verificar a persistencia.
