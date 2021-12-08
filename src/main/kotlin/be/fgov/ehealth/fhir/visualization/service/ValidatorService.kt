package be.fgov.ehealth.fhir.visualization.service

import be.fgov.ehealth.fhir.narrative.option.FhirValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.io.PrintStream

@Service
class ValidatorService {
    @Cacheable(cacheNames = ["Validator"])
    fun getValidator(implementationGuideUrls: List<String>): Deferred<FhirValidator> = CoroutineScope(Dispatchers.IO).async {
        FhirValidator(PrintStream(System.out), implementationGuideUrls)
    }

}
