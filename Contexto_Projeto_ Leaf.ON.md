# Contexto do Projeto: Leaf.ON

Este documento serve como base de conhecimento para IAs sobre o projeto **Leaf.ON**, um sistema inteligente de monitoramento ambiental para estufas e hortas urbanas.

---

## 1. Visão Geral
O **Leaf.ON** integra dispositivos de Internet das Coisas (IoT) com aplicações multiplataforma para automatizar a coleta de dados ambientais (temperatura, umidade, luminosidade) e o controle de irrigação.

- **Objetivo Geral:** Desenvolver um ecossistema integrado para monitoramento, armazenamento e análise de dados de estufas inteligentes.
- **Público-alvo:** Pequenos produtores e entusiastas de agricultura urbana.
- **Diferencial:** Solução acessível que une hardware (ESP32) e software multiplataforma (Mobile, Web, Desktop) com análise preditiva básica.

---

## 2. Stack Tecnológica

### Backend
- **Linguagem:** Kotlin
- **Framework:** Spring Boot
- **Segurança:** Spring Security com autenticação baseada em **JWT (JSON Web Token)**.
- **Persistência:** Spring Data JPA/MongoDB.
- **Comunicação IoT:** Protocolo **MQTT** via Broker para mensagens assíncronas.
- **Documentação:** OpenAPI/Swagger.

### Frontend (Multiplataforma)
- **Mobile (Android) & Desktop:** **Kotlin Multiplatform (KMP)** com Jetpack Compose.
- **Web:** **React** (interface moderna e responsiva).
- **Arquitetura:** MVVM ou MVI.
- **Comunicação API:** Ktor Client.

### IoT (Hardware)
- **Microcontrolador:** **ESP32** (programado em C++).
- **Sensores:** DHT22 (Temperatura e Umidade), Sensores de Umidade do Solo e Luminosidade.
- **Atuadores:** Bomba de irrigação.
- **Protocolo:** MQTT para envio de telemetria e recebimento de comandos.

---

## 3. Requisitos Funcionais (RF)

| ID | Título | Descrição Resumida |
|:---|:---|:---|
| **RF01** | Cadastro/Login | Registro de usuário e autenticação via JWT. |
| **RF04** | Configuração | Definir limites de umidade, duração de irrigação e cooldown. |
| **RF06** | Telemetria | Recebimento de dados dos sensores via MQTT. |
| **RF07** | Registro | Armazenamento de umidade, temperatura e luminosidade com timestamp. |
| **RF09** | Irrigação Manual | Acionamento de irrigação via comando manual no app. |
| **RF10** | Irrigação Auto | Disparo automático quando a umidade cai abaixo do limite. |
| **RF11** | Rotinas | Agendamento de irrigação por horário e dias da semana. |
| **RF14** | Alertas | Geração de notificações para níveis críticos de umidade. |
| **RF18** | Conectividade | Monitoramento do status online/offline do dispositivo IoT. |

---

## 4. Requisitos Não Funcionais (RNF)

- **Segurança:** Senhas criptografadas com BCrypt; isolamento de dados por usuário.
- **Resiliência:** Reconexão automática ao Broker MQTT; validação de payloads para evitar crashes.
- **Performance:** Resposta da API < 1s; paginação em históricos de telemetria.
- **Arquitetura:** Separação clara em camadas (Controller, Service, Repository).
- **Padronização:** Comunicação via JSON; versionamento de API (`/api/v1/...`).

---

## 5. Arquitetura e Fluxo de Dados

### Fluxo de Telemetria (IoT -> Usuário)
1. O **ESP32** lê os sensores e publica no tópico MQTT.
2. O **Backend (Spring Boot)**, inscrito no tópico, recebe e persiste os dados no banco.
3. As **Aplicações (KMP/React)** consomem os dados via API REST para exibição em tempo real e históricos.

### Fluxo de Comando (Usuário -> IoT)
1. O usuário solicita "Irrigação Manual" no App.
2. O **Backend** valida a permissão e publica um comando no tópico MQTT de controle.
3. O **ESP32** recebe o comando e aciona o relé da bomba de água.

---

## 6. Entidades Principais (Modelo de Dados)
- **Usuário:** Nome, Email (único), Senha (hash).
- **SmartPot (Vaso):** Nome da planta, Configurações de limites, Status da bomba.
- **Leitura (Telemetria):** Valor, Tipo (Umidade/Temp/Luz), Timestamp, ID do SmartPot.
- **Evento de Irrigação:** Tipo (Manual/Auto/Rotina), Duração, Timestamp.
- **Alerta:** Mensagem, Status (Lido/Pendente), Timestamp.

---

## 7. Instruções para IA
Ao atuar como assistente para este projeto:
1. Priorize o uso de **Kotlin** tanto no Backend quanto no Frontend (KMP).
2. Garanta que todas as rotas de API verifiquem o **Token JWT**.
3. Siga o padrão de arquitetura em camadas no Spring Boot.
4. Ao sugerir código para o ESP32, utilize a biblioteca **PubSubClient** para MQTT.
5. Considere a técnica de **Regressão Linear** para futuras implementações de análise preditiva de dados ambientais.
