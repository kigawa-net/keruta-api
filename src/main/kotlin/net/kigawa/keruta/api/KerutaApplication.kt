package net.kigawa.keruta.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(scanBasePackages = ["net.kigawa.keruta"])
@EnableScheduling
@ConfigurationPropertiesScan(basePackages = ["net.kigawa.keruta"])
class KerutaApplication

fun main(args: Array<String>) {
    runApplication<KerutaApplication>(*args)
}
