package be.fgov.ehealth.fhir.visualization.rest.fhir.r4

import be.fgov.ehealth.fhir.visualization.dto.HtmlWithValidation
import be.fgov.ehealth.fhir.visualization.dto.Validation
import be.fgov.ehealth.fhir.visualization.service.ValidatorService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.reactor.mono
import org.hl7.fhir.r4.model.AllergyIntolerance
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/rest/fhir/r4/viz/allergy")
@Tag(name = "fhir")
class AllergyController(val validatorService: ValidatorService) : ControllerInterface {

    override fun fhirValidatorAsync() = validatorService.getValidatorAsync(listOf(
            "hl7.fhir.be.allergy#1.2.0"
    ))

    @PostMapping("html", consumes = ["application/json", "application/xml"])
    @Operation(summary = "Convert FHIR file to html")
    override fun html(@RequestBody fhirFile: ByteArray, response: ServerHttpResponse): Mono<Void> =
            ResponseBuilder.makeResponse(fhirFile, response, true, AllergyIntolerance::class)

    @PostMapping("html/validate", consumes = ["application/json", "application/xml"])
    @Operation(summary = "Validate and convert FHIR file to object including validation information and html representation")
    override fun htmlAndValidate(@RequestBody fhirFile: ByteArray) = mono {

        fhirValidatorAsync().await().validate(fhirFile).let { (errors, validationReport) ->
            HtmlWithValidation(
                    html = NarrativeBuilder.makeNarrative(fhirFile, true, AllergyIntolerance::class).toString(Charsets.UTF_8),
                    errors = errors,
                    validation = validationReport
            )
        }

    }

    @PostMapping("validate", consumes = ["application/json", "application/xml"])
    @Operation(summary = "Validate vaccination report")
    override fun validate(@RequestBody fhirFile: ByteArray) = mono {

        fhirValidatorAsync().await().validate(fhirFile).let { (errors) ->
            Validation(
                    errors = errors
            )
        }
    }

}