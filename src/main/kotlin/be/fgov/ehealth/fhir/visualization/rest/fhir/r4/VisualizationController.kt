package be.fgov.ehealth.fhir.visualization.rest.fhir.r4

import be.fgov.ehealth.fhir.narrative.gen.DiagnosticReportHtmlGenerator
import be.fgov.ehealth.fhir.narrative.utils.FhirNarrativeUtils.stripNarratives
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

@ExperimentalStdlibApi
@ExperimentalCoroutinesApi
@RestController
@RequestMapping("/rest/fhir/r4/viz")
@Tag(name = "fhir")
class VisualizationController(val diagnosticReportHtmlGenerator: DiagnosticReportHtmlGenerator) {
    @PostMapping("/html", consumes = ["application/json", "application/xml"])
    @Operation(summary = "Convert FHIR file to html")
    fun html(@RequestBody fhirFile: ByteArray, response: ServerHttpResponse): Mono<Void> =
        makeResponse(fhirFile, response, true)

    @PostMapping("/div", consumes = ["application/json", "application/xml"])
    @Operation(summary = "Convert FHIR file to embeddable div")
    fun div(@RequestBody fhirFile: ByteArray, response: ServerHttpResponse): Mono<Void> =
        makeResponse(fhirFile, response, false)

    @PostMapping("/html/single")
    @Operation(summary = "Convert FHIR file to html")
    fun htmlMultipartSingle(@RequestPart("file") fhirFilePart: Mono<FilePart>, response: ServerHttpResponse): Mono<Void> =
        fhirFilePart.flatMap { it.content().mergeDataBuffers() }.flatMap { fhirFile -> makeResponse(fhirFile, response, true) }

    @PostMapping("/div/single")
    @Operation(summary = "Convert FHIR file provided as multipart request to embeddable div")
    fun divMultipartSingle(@RequestPart("file") fhirFilePart: Mono<FilePart>, response: ServerHttpResponse) =
        fhirFilePart.flatMap { it.content().mergeDataBuffers() }.flatMap { fhirFile -> makeResponse(fhirFile, response, false) }

    private fun makeResponse(
        fhirData: ByteArray,
        response: ServerHttpResponse,
        embedInHtml: Boolean
    ): Mono<Void> {
        val ctx = FhirContext.forR4()
        val parser = ctx.newJsonParser()
        val bundle = stripNarratives(ctx, parser.parseResource(Bundle::class.java, fhirData.toString(Charsets.UTF_8)))
        val htmlOrDiv = if (embedInHtml) {
            diagnosticReportHtmlGenerator.generateHtmlRepresentation(ctx, bundle, null)
        } else {
            diagnosticReportHtmlGenerator.generateDivRepresentation(ctx, bundle, null).toByteArray(Charsets.UTF_8)
        }
        response.headers.set("Content-Type", "text/html")
        return response.writeWith(Mono.just(response.bufferFactory().wrap(htmlOrDiv)))
    }
}
