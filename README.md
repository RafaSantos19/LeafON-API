# LeafON API

Backend da LeafON desenvolvido em Kotlin com Spring Boot. A API concentra os modulos de usuarios, vasos inteligentes, telemetria, irrigacao e alertas, usando PostgreSQL como banco de dados.

Projeto Kotlin Multiplatform com Compose Multiplatform. O app Leaf.ON centraliza telas de autenticacao, home, perfil e gerenciamento de Smart Pots, incluindo listagem, cadastro, edicao, detalhe, rotinas e alertas.

projeto **Leaf.ON**, um sistema inteligente de monitoramento ambiental para estufas e hortas urbanas.

## Integrates
- Rafael Ferreira dos Santos
- Miguel Gomes de Lima Coyado Vieira

## Tecnologias

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

## Estrutura do projeto

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

### Pacotes principais

- `com.leafon.LeafonApplication.kt`: ponto de entrada da aplicacao Spring Boot.
- `auth`: estrutura de autenticacao, DTOs, mappers e servicos de token/autenticacao.
- `user`: CRUD de usuarios, com controller, service, repository, entity, DTOs e mapper.
- `smartpot`: estrutura para configuracao e persistencia dos vasos inteligentes.
- `telemetry`: registro e consulta de leituras de sensores, com validacao de ownership por `SmartPot`.
- `irrigation`: regras, eventos e comandos de irrigacao.
- `alert`: estrutura para alertas gerados pela aplicacao.
- `common`: configuracoes compartilhadas, tratamento de excecoes e utilitarios.

Dentro dos modulos de dominio, a organizacao segue este padrao:

- `controller`: endpoints HTTP.
- `service`: regras de negocio.
- `repository`: acesso ao banco via Spring Data JPA.
- `entity`: entidades persistidas no banco.
- `dto`: objetos de entrada e saida da API.
- `mapper`: conversao entre entidades e DTOs.
- `mqtt`: publicacao ou leitura de mensagens MQTT, quando aplicavel.

## Requisitos

- JDK 21 instalado.
- PostgreSQL acessivel pela aplicacao.
- Variavel de ambiente `SUPABASE_DATABASE_PASSWORD` configurada, conforme usada em `src/main/resources/application.properties`.

O projeto usa Gradle Wrapper, entao nao e necessario instalar o Gradle manualmente.

## Configuracao

As configuracoes principais ficam em:

```text
src/main/resources/application.properties
```

Antes de rodar a aplicacao, configure a senha do banco:

Windows PowerShell:

```powershell
$env:SUPABASE_DATABASE_PASSWORD="sua_senha"
```

Linux/macOS:

```bash
export SUPABASE_DATABASE_PASSWORD="sua_senha"
```

Se for usar um banco local, ajuste as propriedades `spring.datasource.url`, `spring.datasource.username` e `spring.datasource.password` no arquivo `application.properties`.

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

Importante:

- Os valores de `issuer-uri`, `jwk-set-uri` e o JWT usado nas requisicoes precisam pertencer ao mesmo projeto Supabase.
- A tabela `smartpots` precisa usar `uuid` em `id` e `user_id`. Se a tabela existir com `bigint`, os inserts vao falhar.

### Tabela de usuarios

A entidade `User` esta mapeada no projeto para a tabela `users`. O nome `users` e recomendado porque `user` pode ser palavra reservada em alguns bancos SQL.

| Campo | Tipo Kotlin | Tipo PostgreSQL sugerido | Obrigatorio | Observacoes |
| --- | --- | --- | --- | --- |
| `id` | `UUID?` | `uuid` | Sim | Chave primaria. Deve receber o mesmo UUID do claim `sub` do Supabase Auth. |
| `email` | `String` | `varchar(255)` | Sim | Deve ser unico. |
| `name` | `String?` | `varchar(255)` | Nao | Nome opcional do usuario. |
| `phone` | `String?` | `varchar(255)` | Nao | Telefone do usuario. No fluxo atual de criacao ele e enviado no cadastro. |
| `created_at` | `OffsetDateTime?` | `timestamp with time zone` | Nao | Data de criacao preenchida pelo Hibernate. |
| `updated_at` | `OffsetDateTime?` | `timestamp with time zone` | Nao | Data da ultima atualizacao preenchida pelo Hibernate. |

Exemplo para criar a tabela manualmente no PostgreSQL:

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

### Tabela de smart pots

A entidade `SmartPot` esta mapeada para a tabela `smartpots` e pertence a um usuario autenticado.

| Campo | Tipo Kotlin | Tipo PostgreSQL sugerido | Obrigatorio | Observacoes |
| --- | --- | --- | --- | --- |
| `id` | `UUID?` | `uuid` | Sim | Chave primaria do vaso. |
| `user_id` | `UUID?` | `uuid` | Sim | Dono do vaso. Deve referenciar `users(id)`. |
| `plant_name` | `String?` | `varchar(255)` | Sim | Nome da planta. |
| `humidity_min` | `Int?` | `integer` | Sim | Valor minimo permitido, entre `0` e `100`. |
| `device_id` | `String?` | `varchar(255)` | Nao | Identificador do dispositivo. Deve ser unico quando informado. |
| `created_at` | `Instant?` | `timestamp with time zone` | Sim | Definido na criacao. |
| `updated_at` | `Instant?` | `timestamp with time zone` | Sim | Atualizado a cada alteracao. |

Exemplo para criar a tabela manualmente no PostgreSQL:

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

### Tabela de telemetria

A entidade `TelemetryReading` esta mapeada para a tabela `telemetry_readings` e cada leitura pertence a um `SmartPot`.

| Campo | Tipo Kotlin | Tipo PostgreSQL sugerido | Obrigatorio | Observacoes |
| --- | --- | --- | --- | --- |
| `id` | `UUID?` | `uuid` | Sim | Chave primaria da leitura. |
| `smart_pot_id` | `UUID?` | `uuid` | Sim | Vaso dono da leitura. Deve referenciar `smartpots(id)`. |
| `soil_humidity` | `Int?` | `integer` | Sim | Umidade do solo entre `0` e `100`. |
| `temperature` | `Double?` | `double precision` | Sim | Temperatura aceita decimal. |
| `luminosity` | `Double?` | `double precision` | Sim | Luminosidade aceita decimal. |
| `read_at` | `Instant?` | `timestamp with time zone` | Sim | Momento em que o sensor realizou a leitura. |
| `created_at` | `Instant?` | `timestamp with time zone` | Sim | Momento em que a API persistiu o registro. |

Exemplo para criar a tabela manualmente no PostgreSQL:

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

## Como rodar

No Windows:

```powershell
.\gradlew.bat bootRun
```

No Linux/macOS:

```bash
./gradlew bootRun
```

Por padrao, a aplicacao sobe em:

```text
http://localhost:8080
```

## Comandos uteis

Listar tarefas disponiveis do Gradle:

```powershell
.\gradlew.bat tasks
```

Rodar a aplicacao:

```powershell
.\gradlew.bat bootRun
```

Rodar os testes:

```powershell
.\gradlew.bat test
```

Gerar build completo:

```powershell
.\gradlew.bat build
```

Gerar o JAR executavel:

```powershell
.\gradlew.bat bootJar
```

Executar o JAR gerado:

```powershell
java -jar build/libs/leafon-api-0.0.1-SNAPSHOT.jar
```

Limpar arquivos gerados:

```powershell
.\gradlew.bat clean
```

Em Linux/macOS, substitua `.\gradlew.bat` por `./gradlew`.

## Rotas implementadas

Atualmente, a API expoe:

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

POST   /telemetry
GET    /telemetry
GET    /telemetry/latest
```

Observacoes sobre `smart-pots`:

- O frontend nao envia `userId`.
- O `userId` e extraido do JWT autenticado.
- Um usuario so pode listar, buscar, atualizar ou deletar os proprios vasos.
- `deviceId` duplicado retorna `409 Conflict`.
- Validacoes invalidas retornam `400 Bad Request`.
- Vaso inexistente ou sem posse do usuario retorna `404 Not Found`.

Observacoes sobre `telemetry`:

- O frontend nao envia `userId`.
- O `userId` e extraido do JWT autenticado.
- O `POST /telemetry` recebe `smartPotId` no body de forma temporaria.
- O usuario autenticado so pode registrar e consultar leituras dos proprios vasos.
- `GET /telemetry` ordena as leituras por `readAt` em ordem decrescente.
- `GET /telemetry/latest` retorna apenas a leitura mais recente do vaso informado.
- `smartPotId` inexistente retorna `404 Not Found`.
- `smartPotId` de outro usuario retorna `403 Forbidden`.
- `soilHumidity` fora do intervalo valido retorna `400 Bad Request`.

## Exemplos de telemetria

Exemplo de `POST`:

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

Exemplo de `GET`:

```bash
curl -X GET "http://localhost:8080/telemetry?smartPotId=273f9192-d2c1-467d-9855-3f0e502e9f42" \
  -H "Authorization: Bearer SEU_JWT"
```

Exemplo de `GET /latest`:

```bash
curl -X GET "http://localhost:8080/telemetry/latest?smartPotId=273f9192-d2c1-467d-9855-3f0e502e9f42" \
  -H "Authorization: Bearer SEU_JWT"
```

## Links

- Repositórios do projeto: [Frontend](https://github.com/RafaSantos19/LeafON-KMP)
- Repositórios do projeto: [Backend](https://github.com/RafaSantos19/LeafON-API)
- Documentação do Projeto (Parcial): [Link do Docs](https://docs.google.com/document/d/1GGbEGgVE6KhAxyz87omWVD5X1HY0fGU79IRKmRMV-Ec/edit?usp=sharing)
