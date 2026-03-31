
Estrutura de pastas do projeto

leafon-backend/
├── src/main/kotlin/com/leafon/
│   ├── LeafonApplication.kt
│   │
│   ├── config/
│   │   ├── security/
│   │   │   ├── SecurityConfig.kt
│   │   │   ├── JwtAuthenticationFilter.kt
│   │   │   ├── JwtService.kt
│   │   │   ├── CustomUserDetailsService.kt
│   │   │   └── AuthenticationEntryPoint.kt
│   │   ├── mqtt/
│   │   │   ├── MqttConfig.kt
│   │   │   ├── MqttTopics.kt
│   │   │   └── MqttProperties.kt
│   │   ├── openapi/
│   │   │   └── OpenApiConfig.kt
│   │   ├── jackson/
│   │   │   └── JacksonConfig.kt
│   │   └── scheduling/
│   │       └── SchedulingConfig.kt
│   │
│   ├── common/
│   │   ├── exception/
│   │   │   ├── GlobalExceptionHandler.kt
│   │   │   ├── BusinessException.kt
│   │   │   ├── NotFoundException.kt
│   │   │   ├── ForbiddenException.kt
│   │   │   └── UnauthorizedException.kt
│   │   ├── response/
│   │   │   ├── ApiResponse.kt
│   │   │   └── PageResponse.kt
│   │   ├── util/
│   │   │   ├── DateTimeUtils.kt
│   │   │   └── ValidationUtils.kt
│   │   └── enums/
│   │       ├── AlertStatus.kt
│   │       ├── AlertType.kt
│   │       ├── CommandStatus.kt
│   │       ├── CommandType.kt
│   │       ├── IrrigationType.kt
│   │       ├── PumpStatus.kt
│   │       └── DayOfWeek.kt
│   │
│   ├── auth/
│   │   ├── controller/
│   │   │   └── AuthController.kt
│   │   ├── service/
│   │   │   ├── AuthService.kt
│   │   │   └── TokenService.kt
│   │   ├── dto/
│   │   │   ├── LoginRequest.kt
│   │   │   ├── RegisterRequest.kt
│   │   │   └── AuthResponse.kt
│   │   └── mapper/
│   │       └── AuthMapper.kt
│   │
│   ├── user/
│   │   ├── controller/
│   │   │   └── UserController.kt
│   │   ├── service/
│   │   │   └── UserService.kt
│   │   ├── repository/
│   │   │   └── UserRepository.kt
│   │   ├── entity/
│   │   │   └── User.kt
│   │   ├── dto/
│   │   │   ├── UserResponse.kt
│   │   │   └── UpdateUserRequest.kt
│   │   └── mapper/
│   │       └── UserMapper.kt
│   │
│   ├── smartpot/
│   │   ├── controller/
│   │   │   └── SmartPotController.kt
│   │   ├── service/
│   │   │   └── SmartPotService.kt
│   │   ├── repository/
│   │   │   └── SmartPotRepository.kt
│   │   ├── entity/
│   │   │   └── SmartPot.kt
│   │   ├── dto/
│   │   │   ├── SmartPotResponse.kt
│   │   │   ├── SmartPotConfigRequest.kt
│   │   │   └── SmartPotConfigPatch.kt
│   │   └── mapper/
│   │       └── SmartPotMapper.kt
│   │
│   ├── telemetry/
│   │   ├── controller/
│   │   │   └── TelemetryController.kt
│   │   ├── service/
│   │   │   └── TelemetryService.kt
│   │   ├── repository/
│   │   │   └── TelemetryReadingRepository.kt
│   │   ├── entity/
│   │   │   └── TelemetryReading.kt
│   │   ├── dto/
│   │   │   ├── TelemetryPayload.kt
│   │   │   ├── TelemetryResponse.kt
│   │   │   └── TelemetryQuery.kt
│   │   ├── mqtt/
│   │   │   └── TelemetryMqttListener.kt
│   │   └── mapper/
│   │       └── TelemetryMapper.kt
│   │
│   ├── irrigation/
│   │   ├── controller/
│   │   │   └── IrrigationController.kt
│   │   ├── service/
│   │   │   ├── IrrigationService.kt
│   │   │   ├── RuleEngine.kt
│   │   │   └── CooldownPolicy.kt
│   │   ├── repository/
│   │   │   └── IrrigationEventRepository.kt
│   │   ├── entity/
│   │   │   └── IrrigationEvent.kt
│   │   ├── dto/
│   │   │   ├── ManualIrrigationRequest.kt
│   │   │   ├── IrrigationEventResponse.kt
│   │   │   └── IrrigationQuery.kt
│   │   ├── mqtt/
│   │   │   ├── DeviceCommandPublisher.kt
│   │   │   └── CommandAckListener.kt
│   │   └── mapper/
│   │       └── IrrigationMapper.kt
│   │
│   ├── routine/
│   │   ├── controller/
│   │   │   └── RoutineController.kt
│   │   ├── service/
│   │   │   ├── RoutineService.kt
│   │   │   └── RoutineScheduler.kt
│   │   ├── repository/
│   │   │   └── RoutineRepository.kt
│   │   ├── entity/
│   │   │   └── Routine.kt
│   │   ├── dto/
│   │   │   ├── RoutineCreateRequest.kt
│   │   │   ├── RoutineResponse.kt
│   │   │   └── RoutineUpdateRequest.kt
│   │   └── mapper/
│   │       └── RoutineMapper.kt
│   │
│   ├── alert/
│   │   ├── controller/
│   │   │   └── AlertController.kt
│   │   ├── service/
│   │   │   └── AlertService.kt
│   │   ├── repository/
│   │   │   └── AlertRepository.kt
│   │   ├── entity/
│   │   │   └── Alert.kt
│   │   ├── dto/
│   │   │   ├── AlertResponse.kt
│   │   │   └── AlertStatusUpdateRequest.kt
│   │   └── mapper/
│   │       └── AlertMapper.kt
│   │
│   ├── command/
│   │   ├── repository/
│   │   │   └── MqttCommandRepository.kt
│   │   ├── entity/
│   │   │   └── MqttCommand.kt
│   │   └── service/
│   │       └── CommandService.kt
│   │
│   └── prediction/
│       ├── controller/
│       │   └── PredictionController.kt
│       ├── service/
│       │   ├── PredictionService.kt
│       │   └── LinearRegressionService.kt
│       ├── dto/
│       │   └── PredictionResponse.kt
│       └── repository/
│           └── PredictionRepository.kt
│
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   ├── application-prod.yml
│   └── db/migration/
│       ├── V1__create_users.sql
│       ├── V2__create_smart_pots.sql
│       ├── V3__create_telemetry_readings.sql
│       ├── V4__create_irrigation_events.sql
│       ├── V5__create_routines.sql
│       ├── V6__create_alerts.sql
│       └── V7__create_mqtt_commands.sql
│
└── src/test/kotlin/com/leafon/
├── unit/
├── integration/
└── fixture/