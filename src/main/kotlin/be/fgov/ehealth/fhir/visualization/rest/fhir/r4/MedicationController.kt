package be.fgov.ehealth.fhir.visualization.rest.fhir.r4

import be.fgov.ehealth.fhir.visualization.dto.HtmlWithValidation
import be.fgov.ehealth.fhir.visualization.dto.Validation
import be.fgov.ehealth.fhir.visualization.service.ValidatorService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.reactor.mono
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping(
    value = [
        "/rest/fhir/r4/viz/recipe-prescriber",
        "/rest/fhir/r4/viz/recipe-patient",
        "/rest/fhir/r4/viz/recipe-executer"
    ]
)
@Tag(name = "Recip-e")
class MedicationController(val validatorService: ValidatorService) : ControllerInterface {

    override fun fhirValidatorAsync() = validatorService.getValidatorAsync(listOf(
        "hl7.fhir.be.medication#1.1.0",
        "hl7.fhir.be.infsec#1.2.0"
    ))

    @PostMapping("html", consumes = ["application/json", "application/xml"])
    @Operation(
        summary = "Convert FHIR file to html",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(required = true)
    )
    override fun html(@RequestBody fhirFile: ByteArray, response: ServerHttpResponse): Mono<Void> =
            ResponseBuilder.makeResponse(fhirFile, response, true)

    @PostMapping("html/validate", consumes = ["application/json", "application/xml"])
    @Operation(
        summary = "Validate and convert FHIR file to object including validation information and html representation",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(required = true)
    )
    override fun htmlAndValidate(@RequestBody fhirFile: ByteArray) = mono {
        fhirValidatorAsync().await().validate(fhirFile).let { (errors, validationReport) ->
            HtmlWithValidation(
                    html = NarrativeBuilder.makeNarrative(fhirFile, true).toString(Charsets.UTF_8),
                    errors = errors,
                    validation = validationReport
            )
        }
    }

    @PostMapping("validate", consumes = ["application/json", "application/xml"])
    @Operation(
        summary = "Validate vaccination report",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(required = true)
    )
    override fun validate(@RequestBody fhirFile: ByteArray) = mono {

        fhirValidatorAsync().await().validate(fhirFile).let { (errors) ->
            Validation(
                    errors = errors
            )
        }
    }

}