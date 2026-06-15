package com.leafon.telemetry.service

import com.leafon.alert.service.AlertService
import com.leafon.common.exception.ForbiddenException
import com.leafon.smartpot.entity.SmartPot
import com.leafon.smartpot.exception.SmartPotNotFoundException
import com.leafon.smartpot.repository.SmartPotRepository
import com.leafon.telemetry.dto.TelemetryCreateRequest
import com.leafon.telemetry.entity.TelemetryReading
import com.leafon.telemetry.enums.Luminosity
import com.leafon.telemetry.repository.TelemetryRepository
import jakarta.validation.ConstraintViolationException
import jakarta.validation.Validation
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

@ExtendWith(MockitoExtension::class)
class TelemetryServiceTest {

    @Mock
    private lateinit var telemetryRepository: TelemetryRepository

    @Mock
    private lateinit var smartPotRepository: SmartPotRepository

    @Mock
    private lateinit var alertService: AlertService

    @Captor
    private lateinit var readingCaptor: ArgumentCaptor<TelemetryReading>

    private val validator = Validation.buildDefaultValidatorFactory().validator
    private lateinit var service: TelemetryService

    private val authenticatedUserId = UUID.randomUUID()
    private val smartPotId = UUID.randomUUID()
    private lateinit var smartPot: SmartPot

    @BeforeEach
    fun setUp() {
        service = TelemetryService(
            telemetryRepository = telemetryRepository,
            smartPotRepository = smartPotRepository,
            alertService = alertService,
            validator = validator,
        )
        smartPot = SmartPot(
            id = smartPotId,
            userId = authenticatedUserId,
            plantName = "Manjericao",
            humidityMin = 30,
        )
    }

    @Test
    fun `deve registrar leitura valida sem gerar alerta`() {
        givenAccessibleSmartPot()
        givenRepositoryReturnsSavedReading()

        val result = service.create(smartPotId, validRequest(), authenticatedUserId)

        verify(telemetryRepository).save(readingCaptor.capture())
        assertEquals(50, readingCaptor.value.soilHumidityPercent)
        assertEquals(smartPot, readingCaptor.value.smartPot)
        assertSame(readingCaptor.value, result)
        verifyNoInteractions(alertService)
    }

    @Test
    fun `deve registrar leitura valida e gerar alerta quando umidade do solo estiver abaixo do limite`() {
        givenAccessibleSmartPot()
        givenRepositoryReturnsSavedReading()

        val result = service.create(
            smartPotId,
            validRequest(soilHumidity = 29),
            authenticatedUserId,
        )

        verify(alertService).createLowSoilHumidityAlert(eq(smartPot), eq(result))
        verify(alertService, never()).createLowAirHumidityAlert(any(), any())
        verify(alertService, never()).createHighTemperatureAlert(any(), any())
    }

    @Test
    fun `deve registrar leitura MQTT com horario informado pelo dispositivo`() {
        val readAt = Instant.parse("2026-05-30T12:00:00Z")
        givenAccessibleSmartPot()
        givenRepositoryReturnsSavedReading()

        service.createFromDevice(
            smartPotId = smartPotId,
            request = validRequest(),
            readAt = readAt,
        )

        verify(telemetryRepository).save(readingCaptor.capture())
        assertEquals(readAt, readingCaptor.value.readAt)
        assertEquals(smartPot, readingCaptor.value.smartPot)
    }

    @Test
    fun `deve aceitar umidade do solo igual a zero`() {
        smartPot.humidityMin = 0
        givenAccessibleSmartPot()
        givenRepositoryReturnsSavedReading()

        val result = service.create(
            smartPotId,
            validRequest(soilHumidity = 0),
            authenticatedUserId,
        )

        assertEquals(0, result.soilHumidityPercent)
        verify(telemetryRepository).save(any())
        verifyNoInteractions(alertService)
    }

    @Test
    fun `deve aceitar umidade do solo igual a cem`() {
        givenAccessibleSmartPot()
        givenRepositoryReturnsSavedReading()

        val result = service.create(
            smartPotId,
            validRequest(soilHumidity = 100),
            authenticatedUserId,
        )

        assertEquals(100, result.soilHumidityPercent)
        verify(telemetryRepository).save(any())
        verifyNoInteractions(alertService)
    }

    @Test
    fun `deve rejeitar umidade do solo menor que zero sem persistir`() {
        assertFailsWith<ConstraintViolationException> {
            service.create(
                smartPotId,
                validRequest(soilHumidity = -1),
                authenticatedUserId,
            )
        }

        verifyNoInteractions(smartPotRepository, telemetryRepository, alertService)
    }

    @Test
    fun `deve rejeitar umidade do solo maior que cem sem persistir`() {
        assertFailsWith<ConstraintViolationException> {
            service.create(
                smartPotId,
                validRequest(soilHumidity = 101),
                authenticatedUserId,
            )
        }

        verifyNoInteractions(smartPotRepository, telemetryRepository, alertService)
    }

    @Test
    fun `deve rejeitar SmartPot inexistente sem persistir`() {
        whenever(smartPotRepository.findById(smartPotId)).thenReturn(Optional.empty())

        assertFailsWith<SmartPotNotFoundException> {
            service.create(smartPotId, validRequest(), authenticatedUserId)
        }

        verify(telemetryRepository, never()).save(any())
        verifyNoInteractions(alertService)
    }

    @Test
    fun `deve rejeitar SmartPot pertencente a outro usuario sem persistir`() {
        smartPot.userId = UUID.randomUUID()
        givenAccessibleSmartPot()

        assertFailsWith<ForbiddenException> {
            service.create(smartPotId, validRequest(), authenticatedUserId)
        }

        verify(telemetryRepository, never()).save(any())
        verifyNoInteractions(alertService)
    }

    @Test
    fun `deve listar leituras de SmartPot do usuario autenticado`() {
        val olderReading = reading(readAt = Instant.parse("2026-06-08T10:00:00Z"))
        val latestReading = reading(readAt = Instant.parse("2026-06-09T10:00:00Z"))
        val expected = listOf(latestReading, olderReading)
        givenAccessibleSmartPot()
        whenever(telemetryRepository.findAllBySmartPotIdOrderByReadAtDesc(smartPotId))
            .thenReturn(expected)

        val result = service.findAll(smartPotId, authenticatedUserId)

        assertEquals(expected, result)
        verify(telemetryRepository).findAllBySmartPotIdOrderByReadAtDesc(smartPotId)
    }

    @Test
    fun `deve retornar a leitura mais recente do SmartPot`() {
        val latestReading = reading(readAt = Instant.parse("2026-06-09T10:00:00Z"))
        givenAccessibleSmartPot()
        whenever(telemetryRepository.findTopBySmartPotIdOrderByReadAtDesc(smartPotId))
            .thenReturn(latestReading)

        val result = service.findLatest(smartPotId, authenticatedUserId)

        assertSame(latestReading, result)
        verify(telemetryRepository).findTopBySmartPotIdOrderByReadAtDesc(smartPotId)
    }

    private fun givenAccessibleSmartPot() {
        whenever(smartPotRepository.findById(smartPotId)).thenReturn(Optional.of(smartPot))
    }

    private fun givenRepositoryReturnsSavedReading() {
        whenever(telemetryRepository.save(any())).thenAnswer { invocation ->
            invocation.getArgument<TelemetryReading>(0)
        }
    }

    private fun validRequest(soilHumidity: Int = 50) = TelemetryCreateRequest(
        soilHumidity = soilHumidity,
        airHumidity = 60.0,
        temperature = 25.0,
        luminosityStatus = Luminosity.CLARO,
    )

    private fun reading(readAt: Instant) = TelemetryReading(
        id = UUID.randomUUID(),
        smartPot = smartPot,
        soilHumidityPercent = 50,
        airHumidity = 60.0,
        temperature = 25.0,
        luminosity = Luminosity.CLARO,
        readAt = readAt,
    )
}
