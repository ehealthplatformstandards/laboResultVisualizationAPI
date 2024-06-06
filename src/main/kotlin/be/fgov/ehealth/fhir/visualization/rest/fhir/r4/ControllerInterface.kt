package be.fgov.ehealth.fhir.visualization.rest.fhir.r4

import be.fgov.ehealth.fhir.narrative.option.FhirValidator
import be.fgov.ehealth.fhir.visualization.dto.HtmlWithValidation
import be.fgov.ehealth.fhir.visualization.dto.Validation
import kotlinx.coroutines.Deferred
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.bind.annotation.RequestBody
import reactor.core.publisher.Mono

interface ControllerInterface {

    fun fhirValidatorAsync(): Deferred<FhirValidator>

    fun html(@RequestBody fhirFile: ByteArray, response: ServerHttpResponse): Mono<Void>

    fun htmlAndValidate(@RequestBody fhirFile: ByteArray): Mono<HtmlWithValidation>

    fun validate(@RequestBody fhirFile: ByteArray): Mono<Validation>
}