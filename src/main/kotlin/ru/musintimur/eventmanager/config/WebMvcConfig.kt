package ru.musintimur.eventmanager.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfig(
    @Value("\${app.upload.path}") private val uploadPath: String,
) : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        // Все файлы из папки uploads доступны по URL /uploads/**
        // file: prefix требует абсолютный путь с trailing slash
        val absolutePath =
            java.nio.file.Paths
                .get(uploadPath)
                .toAbsolutePath()
                .normalize()
                .toString()
                .replace("\\", "/") // Windows-совместимость

        registry
            .addResourceHandler("/uploads/**")
            .addResourceLocations("file:$absolutePath/")
    }
}
