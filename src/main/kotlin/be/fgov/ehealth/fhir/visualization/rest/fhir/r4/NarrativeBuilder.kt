package be.fgov.ehealth.fhir.visualization.rest.fhir.r4

import be.fgov.ehealth.fhir.narrative.gen.ResourceHtmlGenerator
import ca.uhn.fhir.context.FhirContext
import org.hl7.fhir.r4.model.*

class NarrativeBuilder {

    companion object {
        fun makeNarrative(fhirData: ByteArray, embedInHtml: Boolean): ByteArray {

            val fhirContext  = FhirContext.forR4()

            val content = fhirData.toString(Charsets.UTF_8).trimStart()
            val parser = when {
                content.startsWith("{") || content.startsWith("[")  -> fhirContext.newJsonParser()
                content.startsWith("<") || content.startsWith("<?xml") -> fhirContext.newXmlParser()
                else -> fhirContext.newJsonParser()
            }

            val resource = parser.parseResource(fhirData.inputStream()) as Resource

            val narrative = FhirNarrativeUtils.stripNarratives(resource)

            return if (embedInHtml) {
                ResourceHtmlGenerator().generateHtmlRepresentation(fhirContext , narrative, null)
            } else {
                ResourceHtmlGenerator().generateDivRepresentation(fhirContext , narrative, null).toByteArray(Charsets.UTF_8)
            }

        }
    }
}