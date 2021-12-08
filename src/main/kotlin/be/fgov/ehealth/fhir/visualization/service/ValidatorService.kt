package be.fgov.ehealth.fhir.visualization.service

import be.fgov.ehealth.fhir.narrative.option.FhirValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.io.PrintStream

@Service
class ValidatorService {
    val mutex = Mutex()

    @Cacheable(cacheNames = ["Validator"])
    fun getValidatorAsync(implementationGuideUrls: List<String>): Deferred<FhirValidator> = CoroutineScope(Dispatchers.IO).async {
        //Concurrent access exceptions can happen when filesystem is used concurrently by two different validators being created at the same time
        mutex.withLock {
            FhirValidator(PrintStream(System.out), implementationGuideUrls)
        }
    }

}
