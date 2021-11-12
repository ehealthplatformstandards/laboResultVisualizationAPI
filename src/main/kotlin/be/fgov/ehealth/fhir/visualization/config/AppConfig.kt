package be.fgov.ehealth.fhir.visualization.config

import be.fgov.ehealth.fhir.narrative.gen.DiagnosticReportHtmlGenerator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig {
    @Bean
    fun diagnosticReportHtmlGenerator() = DiagnosticReportHtmlGenerator()
}
