package com.leafon.telemetry.mqtt

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "mqtt")
data class MqttProperties(
    var enabled: Boolean = true,
    var brokerUrl: String = "tcp://localhost:1883",
    var clientId: String = "leafon-backend-listener",
    var topic: String = "leafon/telemetry",
    var username: String = "",
    var password: String = "",
)
