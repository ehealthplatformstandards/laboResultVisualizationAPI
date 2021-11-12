package be.fgov.ehealth.fhir.visualization.rest.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(code = HttpStatus.UNAUTHORIZED)
open class UnauthorizedException(msg: String) : RuntimeException(msg)
