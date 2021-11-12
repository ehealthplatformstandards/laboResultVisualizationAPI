package be.fgov.ehealth.fhir.visualization.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class CacheConfig {
    @Bean
    fun apiCacheManager(): CacheManager = CaffeineCacheManager("User", "LoincCode").apply {
        isAllowNullValues = false
        setCaffeine(
            Caffeine.newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)
        )
    }
}
