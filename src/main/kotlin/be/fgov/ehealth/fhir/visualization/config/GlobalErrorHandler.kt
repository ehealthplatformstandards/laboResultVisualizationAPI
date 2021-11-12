package be.fgov.ehealth.fhir.visualization.config

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.core.io.buffer.DataBufferFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.ServerWebInputException
import reactor.core.publisher.Mono
import java.io.IOException

@Configuration
class GlobalErrorHandler(private val objectMapper: ObjectMapper): ErrorWebExceptionHandler {

    val log = LoggerFactory.getLogger(this::class.java)

    override fun handle(exchange: ServerWebExchange, ex: Throwable) = exchange.response.let { r ->
        log.error("Error caught in handler", ex)
        val bufferFactory = r.bufferFactory()

        r.headers.contentType = MediaType.APPLICATION_JSON
        r.writeWith(Mono.just(when (ex) {
            is IOException -> bufferFactory.toBuffer(ex.message).also { r.statusCode = HttpStatus.BAD_REQUEST }
            is HttpClientErrorException.Unauthorized -> bufferFactory.toBuffer(ex.message).also { r.statusCode = HttpStatus.UNAUTHORIZED }
            is IllegalArgumentException -> bufferFactory.toBuffer(ex.message).also { r.statusCode = HttpStatus.BAD_REQUEST }
            is ServerWebInputException -> bufferFactory.toBuffer(ex.reason).also { r.statusCode = HttpStatus.BAD_REQUEST }
            else -> {
                val rsAnnotation = AnnotationUtils.findAnnotation(ex::class.java, ResponseStatus::class.java)
                if (rsAnnotation != null) {
                    r.statusCode = rsAnnotation.code
                    rsAnnotation.reason.takeIf { it.isNotBlank() }?.let { bufferFactory.toBuffer(it) } ?: bufferFactory.toBuffer(ex.message)
                } else {
                    bufferFactory.toBuffer(ex.message).also { r.statusCode = HttpStatus.INTERNAL_SERVER_ERROR }
                }
            }
        }))
    }

    private fun DataBufferFactory.toBuffer(info: String?) = try {
        val error = info?.let { HttpError(it) } ?: "Unknown error".toByteArray()
        this.wrap(objectMapper.writeValueAsBytes(error))
    } catch (e: JsonProcessingException) {
        this.wrap("".toByteArray())
    }

    class HttpError internal constructor(val message: String)
}
