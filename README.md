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
- JWT (`jjwt`)

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
- `telemetry`: estrutura para leituras de telemetria e integracao MQTT.
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

Atualmente, o controller de usuarios expoe:

```text
GET    /users
GET    /users/me
GET    /users/{id}
POST   /users
PUT    /users/me
PUT    /users/{id}
DELETE /users/me
DELETE /users/{id}
```

Os demais pacotes ja existem como base de organizacao do dominio e podem ser evoluidos com seus respectivos controllers, services e repositories.

## Links

- Repositórios do projeto: [Frontend](https://github.com/RafaSantos19/LeafON-KMP)
- Repositórios do projeto: [Backend](https://github.com/RafaSantos19/LeafON-API)
- Documentação do Projeto (Parcial): [Link do Docs](https://docs.google.com/document/d/1GGbEGgVE6KhAxyz87omWVD5X1HY0fGU79IRKmRMV-Ec/edit?usp=sharing)
