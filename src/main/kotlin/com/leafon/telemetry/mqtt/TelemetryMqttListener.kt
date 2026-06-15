package com.leafon.telemetry.mqtt

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

@Component
@ConditionalOnProperty(prefix = "mqtt", name = ["enabled"], havingValue = "true")
@EnableConfigurationProperties(MqttProperties::class)
class TelemetryMqttListener(
    private val properties: MqttProperties,
    private val messageHandler: TelemetryMqttMessageHandler,
) {
    private val logger = LoggerFactory.getLogger(TelemetryMqttListener::class.java)
    private val client = MqttAsyncClient(
        properties.brokerUrl,
        properties.clientId,
        MemoryPersistence(),
    )
    private val reconnectExecutor = Executors.newSingleThreadScheduledExecutor { runnable ->
        Thread(runnable, "mqtt-connect-retry").apply { isDaemon = true }
    }
    private val connectInProgress = AtomicBoolean(false)
    private val stopping = AtomicBoolean(false)
    private val connectOptions = MqttConnectOptions().apply {
        isAutomaticReconnect = true
        isCleanSession = true
        connectionTimeout = CONNECTION_TIMEOUT_SECONDS
        keepAliveInterval = KEEP_ALIVE_SECONDS

        if (properties.username.isNotBlank()) {
            userName = properties.username
        }
        if (properties.password.isNotBlank()) {
            password = properties.password.toCharArray()
        }
    }

    @PostConstruct
    fun start() {
        client.setCallback(
            object : MqttCallbackExtended {
                override fun connectComplete(reconnect: Boolean, serverURI: String) {
                    subscribe(reconnect)
                }

                override fun connectionLost(cause: Throwable?) {
                    logger.warn(
                        "MQTT connection lost brokerUrl={}: {}",
                        properties.brokerUrl,
                        cause?.message ?: "unknown cause",
                    )
                }

                override fun messageArrived(topic: String, message: MqttMessage) {
                    val payload = message.payload.toString(Charsets.UTF_8)
                    logger.debug(
                        "MQTT message received topic={} qos={} retained={}",
                        topic,
                        message.qos,
                        message.isRetained,
                    )
                    messageHandler.handle(payload)
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) = Unit
            },
        )

        connect()
    }

    private fun connect() {
        if (stopping.get() || client.isConnected || !connectInProgress.compareAndSet(false, true)) {
            return
        }
        try {
            client.connect(
                connectOptions,
                null,
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        connectInProgress.set(false)
                        logger.info("MQTT connection established brokerUrl={}", properties.brokerUrl)
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        connectInProgress.set(false)
                        logger.warn(
                            "MQTT broker unavailable at startup brokerUrl={}: {}",
                            properties.brokerUrl,
                            exception?.message ?: "connection failed",
                        )
                        scheduleReconnect()
                    }
                },
            )
        } catch (ex: Exception) {
            connectInProgress.set(false)
            logger.warn(
                "MQTT listener could not start brokerUrl={}: {}",
                properties.brokerUrl,
                ex.message,
            )
            scheduleReconnect()
        }
    }

    @PreDestroy
    fun stop() {
        stopping.set(true)
        reconnectExecutor.shutdownNow()
        runCatching {
            if (client.isConnected) {
                client.disconnect().waitForCompletion(DISCONNECT_TIMEOUT_MILLIS)
            }
            client.close()
        }.onFailure { ex ->
            logger.warn("MQTT client shutdown failed: {}", ex.message)
        }
    }

    private fun scheduleReconnect() {
        if (stopping.get()) {
            return
        }

        logger.info(
            "MQTT connection retry scheduled brokerUrl={} delaySeconds={}",
            properties.brokerUrl,
            RECONNECT_DELAY_SECONDS,
        )
        reconnectExecutor.schedule(
            ::connect,
            RECONNECT_DELAY_SECONDS,
            TimeUnit.SECONDS,
        )
    }

    private fun subscribe(reconnect: Boolean) {
        try {
            client.subscribe(properties.topic, QOS).waitForCompletion(SUBSCRIBE_TIMEOUT_MILLIS)
            logger.info(
                "MQTT topic subscribed topic={} qos={} reconnect={}",
                properties.topic,
                QOS,
                reconnect,
            )
        } catch (ex: Exception) {
            logger.warn(
                "MQTT topic subscription failed topic={}: {}",
                properties.topic,
                ex.message,
            )
        }
    }

    companion object {
        private const val QOS = 1
        private const val CONNECTION_TIMEOUT_SECONDS = 5
        private const val KEEP_ALIVE_SECONDS = 30
        private const val RECONNECT_DELAY_SECONDS = 10L
        private const val SUBSCRIBE_TIMEOUT_MILLIS = 5_000L
        private const val DISCONNECT_TIMEOUT_MILLIS = 2_000L
    }
}
