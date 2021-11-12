/*
 * Copyright (c) 2020. Taktik SA, All rights reserved.
 */

package be.fgov.ehealth.fhir.visualization.config

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.netty.channel.ChannelOption
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer
import org.springframework.boot.web.reactive.server.ReactiveWebServerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.ResourceHandlerRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.server.session.CookieWebSessionIdResolver
import java.util.*


@Configuration
@EnableWebFlux
class WebConfig : WebFluxConfigurer {
    private val CLASSPATH_RESOURCE_LOCATIONS = arrayOf("classpath:/META-INF/resources/", "classpath:/resources/", "classpath:/static/", "classpath:/public/")
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/**")
                .addResourceLocations(*CLASSPATH_RESOURCE_LOCATIONS)
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**").allowCredentials(true).allowedOriginPatterns("*").allowedMethods("*").allowedHeaders("*")
    }

    override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
        configurer.defaultCodecs().maxInMemorySize(128*1024*1024)

        configurer.defaultCodecs().jackson2JsonEncoder(Jackson2JsonEncoder(ObjectMapper().registerModule(KotlinModule()).apply { setSerializationInclusion(JsonInclude.Include.NON_NULL) }))
        configurer.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(ObjectMapper().registerModule(KotlinModule())).apply { maxInMemorySize = 128 * 1024 * 1024 })
    }

    @Bean
    fun objectMapper(): ObjectMapper = ObjectMapper().registerModule(KotlinModule()).apply { setSerializationInclusion(JsonInclude.Include.NON_NULL) }

    @Bean
    fun webSessionIdResolver() = CookieWebSessionIdResolver().apply { addCookieInitializer { cb -> cb.sameSite("None").secure(true) } }

   @Bean
    fun reactiveWebServerFactory(): ReactiveWebServerFactory? {
        val factory = NettyReactiveWebServerFactory()
        factory.addServerCustomizers(nettyCustomizer())
        return factory
    }

    fun nettyCustomizer() = NettyServerCustomizer { httpServer ->
        if (System.getProperty("os.name").lowercase(Locale.getDefault()).contains("linux")) {
            httpServer.option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.SO_BACKLOG, 2048)
        } else {
            httpServer
        }
    }
}
