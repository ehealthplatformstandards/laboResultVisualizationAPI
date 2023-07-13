package be.fgov.ehealth.fhir.visualization.config

import be.fgov.ehealth.fhir.narrative.gen.ResourceHtmlGenerator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig {
    @Bean
    fun resourceHtmlGenerator() = ResourceHtmlGenerator() 
}
