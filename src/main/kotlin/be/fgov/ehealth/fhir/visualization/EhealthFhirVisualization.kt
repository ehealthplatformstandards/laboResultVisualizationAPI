package be.fgov.ehealth.fhir.visualization

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class EhealthFhirVisualization

fun main(args: Array<String>) {
    runApplication<EhealthFhirVisualization>(*args)
}
