package io.github.mkutz.hexagonaltesting

import org.springframework.boot.fromApplication
import org.springframework.boot.with

fun main(args: Array<String>) {
  fromApplication<HexagonaltestingApplication>().with(TestcontainersConfiguration::class).run(*args)
}
