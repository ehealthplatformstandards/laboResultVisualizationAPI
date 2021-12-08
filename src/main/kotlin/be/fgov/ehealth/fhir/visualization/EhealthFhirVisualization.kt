package be.fgov.ehealth.fhir.visualization

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableCaching
class EhealthFhirVisualization

fun main(args: Array<String>) {
    runApplication<EhealthFhirVisualization>(*args)
}
