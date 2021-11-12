/*
 * Copyright (c) 2020. Taktik SA, All rights reserved.
 */

package be.fgov.ehealth.fhir.visualization.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springdoc.core.GroupedOpenApi
import org.springdoc.core.SpringDocUtils
import org.springdoc.core.customizers.OperationCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.RequestParam


@Configuration
class SwaggerConfig {
    companion object {
        init {
            SpringDocUtils.getConfig().removeRequestWrapperToIgnore(Map::class.java)
        }
    }

    @Bean
    fun visualizationR4Api(springOperationCustomizer: OperationCustomizer): GroupedOpenApi = GroupedOpenApi.builder().group("r4").pathsToMatch("/rest/fhir/r4/**").packagesToScan("be.fgov.ehealth.fhir.visualization.rest.fhir.r4").addOpenApiCustomiser { openApi ->
        openApi.info(
            Info().title("Ehealth FHIR visualization App")
                .description("Ehealth FHIR visualization App formats several FHIR resources to html or pdf")
                .version("r4"))
    }.addOperationCustomizer(springOperationCustomizer).build()

    @Bean
    fun springOperationCustomizer() = OperationCustomizer { operation, handlerMethod ->
        operation.also {
            try {
                if (it.parameters != null) {
                    it.parameters = it.parameters.sortedWith(compareBy { p ->
                        handlerMethod.methodParameters.indexOfFirst { mp -> (mp.parameterAnnotations.find { it is RequestParam }?.let { it as? RequestParam }?.name?.takeIf { it.isNotEmpty() } ?: mp.parameter.name) == p.name }
                    })
                }
            } catch(e:IllegalStateException) {}
        }
    }

    @Bean
    fun customOpenAPI(): OpenAPI = OpenAPI()
            .info(Info().title("Ehealth FHIR visualization App").version("all")
                    .description("Ehealth FHIR visualization App formats several FHIR resources to html or pdf"))

}
