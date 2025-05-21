package be.fgov.ehealth.fhir.visualization.rest.fhir.r4

import org.springframework.http.server.reactive.ServerHttpResponse
import reactor.core.publisher.Mono

class ResponseBuilder {
    companion object {
        fun makeResponse(
                fhirData: ByteArray,
                response: ServerHttpResponse,
                embedInHtml: Boolean,
        ): Mono<Void> {
            response.headers.set("Content-Type", "text/html")
            return response.writeWith(Mono.just(response.bufferFactory().wrap(NarrativeBuilder.makeNarrative(fhirData, embedInHtml))))
        }
    }

}