package be.fgov.ehealth.fhir.visualization.rest.fhir.r4

import org.springframework.http.server.reactive.ServerHttpResponse
import reactor.core.publisher.Mono
import kotlin.reflect.KClass

class ResponseBuilder {
    companion object {
        fun <T : Any> makeResponse(
                fhirData: ByteArray,
                response: ServerHttpResponse,
                embedInHtml: Boolean,
                resource: KClass<T>
        ): Mono<Void> {
            response.headers.set("Content-Type", "text/html")
            return response.writeWith(Mono.just(response.bufferFactory().wrap(NarrativeBuilder.makeNarrative(fhirData, embedInHtml, resource))))
        }
    }

}