Implemente o domínio Routines no backend Leaf.ON.

Contexto:
- Backend em Kotlin + Spring Boot.
- JWT Authentication já implementado.
- Domínios existentes:
 - SmartPot
 - TelemetryReading
 - Alerts
- O SmartPot pertence a um usuário.
- O usuário só pode criar, listar, editar, ativar/desativar ou deletar rotinas dos próprios SmartPots.
- Não implementar controle físico real de bomba, relé, luminária, MQTT ou IoT nesta etapa.
- A execução da rotina será lógica/simulada.

Objetivo:
Criar o domínio Routine para representar rotinas agendadas de irrigação ou luminosidade.

Entidade Routine:
- id: UUID
- smartPot: SmartPot
- type: RoutineType
- name: String
- scheduledTime: LocalTime
- daysOfWeek: String
- durationSec: Int
- active: Boolean
- lastExecutedAt: Instant? nullable
- createdAt: Instant
- updatedAt: Instant

Enums:
RoutineType:
- IRRIGATION
- LIGHTING

Estrutura:
routines/
├── controller/
│    └── RoutineController.kt
├── service/
│    └── RoutineService.kt
├── repository/
│    └── RoutineRepository.kt
├── entity/
│    └── Routine.kt
├── dto/
│    ├── RoutineCreateRequest.kt
│    ├── RoutineUpdateRequest.kt
│    └── RoutineResponse.kt
├── enums/
     └── RoutineType.kt


exception
RoutineNotFoundException.kt

Endpoints:
Base path:
/routines

Rotas:
POST   /routines
GET    /routines
GET    /routines/{id}
PUT    /routines/{id}
PATCH  /routines/{id}/activate
PATCH  /routines/{id}/deactivate
PATCH  /routines/{id}/simulate-execution
DELETE /routines/{id}

DTOs:

RoutineCreateRequest:
- smartPotId: UUID
- type: RoutineType
- name: String
- scheduledTime: LocalTime
- daysOfWeek: String
- durationSec: Int
- active: Boolean

RoutineUpdateRequest:
- type: RoutineType
- name: String
- scheduledTime: LocalTime
- daysOfWeek: String
- durationSec: Int
- active: Boolean

RoutineResponse:
- id
- smartPotId
- type
- name
- scheduledTime
- daysOfWeek
- durationSec
- active
- lastExecutedAt
- createdAt
- updatedAt

Regras:
- smartPotId deve existir.
- SmartPot deve pertencer ao usuário autenticado.
- name obrigatório.
- durationSec deve ser maior que 0.
- scheduledTime obrigatório.
- daysOfWeek obrigatório.
- active indica se a rotina está habilitada.
- Não aceitar userId no body.
- Não executar hardware real.
- simulate-execution apenas atualiza lastExecutedAt com Instant.now().
- Rotina deve ser tratada como configuração lógica do sistema.

Repository:
Criar métodos:
- findAllBySmartPotUserIdOrderByCreatedAtDesc(userId: UUID)
- findByIdAndSmartPotUserId(id: UUID, userId: UUID)
- findAllBySmartPotIdAndActiveTrue(smartPotId: UUID)

Service:
- create(request, authenticatedUserId)
- findAll(authenticatedUserId)
- findById(id, authenticatedUserId)
- update(id, request, authenticatedUserId)
- activate(id, authenticatedUserId)
- deactivate(id, authenticatedUserId)
- simulateExecution(id, authenticatedUserId)
- delete(id, authenticatedUserId)

HTTP:
- POST -> 201 Created
- GET -> 200 OK
- PUT -> 200 OK
- PATCH -> 200 OK
- DELETE -> 204 No Content
- 404 -> rotina não encontrada ou não pertence ao usuário
- 400 -> validação inválida

Importante:
- Não criar scheduler automático agora.
- Não criar integração com MQTT.
- Não publicar comandos para ESP32.
- Não implementar acionamento real de bomba ou luminária.
- A rotina é apenas uma configuração lógica e simulável.
- Seguir o padrão atual do projeto.
- Ao final gerar resumo dos arquivos criados/modificados e testes recomendados no Postman/Insomnia.