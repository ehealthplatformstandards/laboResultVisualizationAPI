package be.fgov.ehealth.fhir.visualization.rest.fhir.r4

import be.fgov.ehealth.fhir.narrative.gen.DiagnosticReportHtmlGenerator
import be.fgov.ehealth.fhir.narrative.utils.FhirNarrativeUtils.stripNarratives
import ca.uhn.fhir.context.FhirContext
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hl7.fhir.r4.model.Bundle
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@ExperimentalStdlibApi
@ExperimentalCoroutinesApi
@RestController
@RequestMapping("/rest/fhir/r4/viz")
@Tag(name = "fhir")
class VisualizationController(val diagnosticReportHtmlGenerator: DiagnosticReportHtmlGenerator) {
    @PostMapping("/html", consumes = ["application/json", "application/xml"])
    @Operation(summary = "Convert FHIR file to html")
    fun html(@RequestBody fhirFile: ByteArray, response: ServerHttpResponse): Mono<Void> {
        val ctx = FhirContext.forR4()
        val parser = ctx.newJsonParser()
        val bundle = stripNarratives(ctx, parser.parseResource(Bundle::class.java, fhirFile.toString(Charsets.UTF_8)))
        val html = diagnosticReportHtmlGenerator.generateHtmlRepresentation(ctx, bundle, null)
        response.headers.set("Content-Type", "text/html")
        return response.writeWith(Mono.just(response.bufferFactory().wrap(html)))
    }

    @PostMapping("/div", consumes = ["application/json", "application/xml"])
    @Operation(summary = "Convert FHIR file to embeddable div")
    fun div(@RequestBody fhirFile: ByteArray, response: ServerHttpResponse): Mono<Void> {
        val ctx = FhirContext.forR4()
        val parser = ctx.newJsonParser()
        val bundle = stripNarratives(ctx, parser.parseResource(Bundle::class.java, fhirFile.toString(Charsets.UTF_8)))
        val div = diagnosticReportHtmlGenerator.generateDivRepresentation(ctx, bundle, null).toByteArray(Charsets.UTF_8)
        response.headers.set("Content-Type", "text/html")
        return response.writeWith(Mono.just(response.bufferFactory().wrap(div)))
    }

}
