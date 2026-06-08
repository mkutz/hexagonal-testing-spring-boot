package io.github.mkutz.hexagonaltesting

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class HexagonaltestingApplication

fun main(args: Array<String>) {
  runApplication<HexagonaltestingApplication>(*args)
}
