package be.fgov.ehealth.fhir.visualization.rest.fhir.r4

import be.fgov.ehealth.fhir.narrative.gen.DiagnosticReportHtmlGenerator
import be.fgov.ehealth.fhir.narrative.option.FhirValidator
import be.fgov.ehealth.fhir.narrative.utils.FhirNarrativeUtils.stripNarratives
import be.fgov.ehealth.fhir.visualization.dto.HtmlWithValidation
import ca.uhn.fhir.context.FhirContext
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.ExperimentalCoroutinesApi
import mergeDataBuffers
import org.hl7.fhir.r4.model.Bundle
import org.springframework.http.codec.multipart.FilePart
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.io.PrintStream

@ExperimentalStdlibApi
@ExperimentalCoroutinesApi
@RestController
@RequestMapping("/rest/fhir/r4/viz")
@Tag(name = "fhir")
class VisualizationController(val diagnosticReportHtmlGenerator: DiagnosticReportHtmlGenerator) {
    val fhirValidator = FhirValidator(
        PrintStream(System.out),
        listOf("https://build.fhir.org/ig/hl7-be/hl7-be-fhir-laboratory-report")
    )

    @PostMapping("/html", consumes = ["application/json", "application/xml"])
    @Operation(summary = "Convert FHIR file to html")
    fun html(@RequestBody fhirFile: ByteArray, response: ServerHttpResponse): Mono<Void> =
        makeResponse(fhirFile, response, true)

    @PostMapping("/html/validate", consumes = ["application/json", "application/xml"])
    @Operation(summary = "Validate and convert FHIR file to object including validation information and html representation")
    fun htmlAndValidate(@RequestBody fhirFile: ByteArray) =
        fhirValidator.validate(fhirFile).let { (errors, validationReport) ->
            HtmlWithValidation(
                html = makeNarrative(fhirFile, true).toString(Charsets.UTF_8),
                errors = errors,
                validation = validationReport
            )
        }

    @PostMapping("/div", consumes = ["application/json", "application/xml"])
    @Operation(summary = "Convert FHIR file to embeddable div")
    fun div(@RequestBody fhirFile: ByteArray, response: ServerHttpResponse): Mono<Void> =
        makeResponse(fhirFile, response, false)

    @PostMapping("/div/validate", consumes = ["application/json", "application/xml"])
    @Operation(summary = "Validate and convert FHIR file to object including validation information and embeddable div")
    fun divAndValidate(@RequestBody fhirFile: ByteArray) =
        fhirValidator.validate(fhirFile).let { (errors, validationReport) ->
            HtmlWithValidation(
                html = makeNarrative(fhirFile, false).toString(Charsets.UTF_8),
                errors = errors,
                validation = validationReport
            )
        }

    @PostMapping("/html/single")
    @Operation(summary = "Convert FHIR file provided as multipart request to html")
    fun htmlMultipartSingle(
        @RequestPart("file") fhirFilePart: Mono<FilePart>,
        response: ServerHttpResponse
    ): Mono<Void> =
        fhirFilePart.flatMap { it.content().mergeDataBuffers() }
            .flatMap { fhirFile -> makeResponse(fhirFile, response, true) }

    @PostMapping("/html/single/validate")
    @Operation(summary = "Convert FHIR file provided as multipart request to html")
    fun htmlAndValidateMultipartSingle(
        @RequestPart("file") fhirFilePart: Mono<FilePart>
    ) = fhirFilePart.flatMap { it.content().mergeDataBuffers() }
        .map { fhirFile ->
            fhirValidator.validate(fhirFile).let { (errors, validationReport) ->
                HtmlWithValidation(
                    html = makeNarrative(fhirFile, true).toString(Charsets.UTF_8),
                    errors = errors,
                    validation = validationReport
                )
            }
        }

    @PostMapping("/div/single")
    @Operation(summary = "Convert FHIR file provided as multipart request to embeddable div")
    fun divMultipartSingle(@RequestPart("file") fhirFilePart: Mono<FilePart>, response: ServerHttpResponse) =
        fhirFilePart.flatMap { it.content().mergeDataBuffers() }
            .flatMap { fhirFile -> makeResponse(fhirFile, response, false) }

    @PostMapping("/div/single/validate")
    @Operation(summary = "Convert FHIR file provided as multipart request to object comprised of validation information and embeddable div")
    fun divAndValidateMultipartSingle(@RequestPart("file") fhirFilePart: Mono<FilePart>) =
        fhirFilePart.flatMap { it.content().mergeDataBuffers() }.map { fhirFile ->
            fhirValidator.validate(fhirFile).let { (errors, validationReport) ->
                HtmlWithValidation(
                    html = makeNarrative(fhirFile, false).toString(Charsets.UTF_8),
                    errors = errors,
                    validation = validationReport
                )
            }
        }

    private fun makeResponse(
        fhirData: ByteArray,
        response: ServerHttpResponse,
        embedInHtml: Boolean
    ): Mono<Void> {
        response.headers.set("Content-Type", "text/html")
        return response.writeWith(Mono.just(response.bufferFactory().wrap(makeNarrative(fhirData, embedInHtml))))
    }

    private fun makeNarrative(fhirData: ByteArray, embedInHtml: Boolean): ByteArray {
        val ctx = FhirContext.forR4()
        val parser = ctx.newJsonParser()
        val bundle = stripNarratives(ctx, parser.parseResource(Bundle::class.java, fhirData.toString(Charsets.UTF_8)))
        return if (embedInHtml) {
            diagnosticReportHtmlGenerator.generateHtmlRepresentation(ctx, bundle, null)
        } else {
            diagnosticReportHtmlGenerator.generateDivRepresentation(ctx, bundle, null).toByteArray(Charsets.UTF_8)
        }
    }
}
