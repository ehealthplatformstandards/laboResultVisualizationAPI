package be.fgov.ehealth.fhir.visualization.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "be.fgov.ehealth")
@Component
class AppProperties {}
