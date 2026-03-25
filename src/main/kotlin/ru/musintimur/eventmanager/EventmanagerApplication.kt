package ru.musintimur.eventmanager

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class EventmanagerApplication

fun main(args: Array<String>) {
	runApplication<EventmanagerApplication>(*args)
}
