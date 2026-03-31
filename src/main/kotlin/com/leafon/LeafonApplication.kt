package com.leafon

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LeafonApplication

fun main(args: Array<String>) {
    runApplication<LeafonApplication>(*args)
}
