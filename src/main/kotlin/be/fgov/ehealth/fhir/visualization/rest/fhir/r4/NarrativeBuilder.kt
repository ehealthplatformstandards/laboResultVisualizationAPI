package be.fgov.ehealth.fhir.visualization.rest.fhir.r4

import be.fgov.ehealth.fhir.narrative.gen.ResourceHtmlGenerator
import ca.uhn.fhir.context.FhirContext
import org.hl7.fhir.r4.model.*
import kotlin.reflect.KClass

class NarrativeBuilder {

    companion object {
        fun <T : Any> makeNarrative(fhirData: ByteArray, embedInHtml: Boolean, resource: KClass<T>): ByteArray {

            val ctx = FhirContext.forR4()

            val tag = "<".toCharArray()

            var parser = ctx.newJsonParser()
            when (String(fhirData)[0]) {
                tag[0] -> parser = ctx.newXmlParser()
            }

            val unstrippedResource: Resource = when (resource) {
                Bundle::class -> parser.parseResource(Bundle::class.java, fhirData.toString(Charsets.UTF_8))
                Immunization::class -> parser.parseResource(Immunization::class.java, fhirData.toString(Charsets.UTF_8))
                AllergyIntolerance::class -> parser.parseResource(AllergyIntolerance::class.java, fhirData.toString(Charsets.UTF_8))
                ServiceRequest::class -> parser.parseResource(ServiceRequest::class.java, fhirData.toString(Charsets.UTF_8))
                else -> parser.parseResource(Bundle::class.java, fhirData.toString(Charsets.UTF_8))
            }

            val narrative = FhirNarrativeUtils.stripNarratives(unstrippedResource)

            return if (embedInHtml) {
                ResourceHtmlGenerator().generateHtmlRepresentation(ctx, narrative, null)
            } else {
                ResourceHtmlGenerator().generateDivRepresentation(ctx, narrative, null).toByteArray(Charsets.UTF_8)
            }

        }
    }
}