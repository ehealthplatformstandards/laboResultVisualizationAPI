package be.fgov.ehealth.fhir.visualization.rest.fhir.r4

import be.fgov.ehealth.fhir.narrative.gen.ResourceHtmlGenerator
import be.fgov.ehealth.fhir.visualization.dto.HtmlWithValidation
import be.fgov.ehealth.fhir.visualization.dto.Validation
import be.fgov.ehealth.fhir.visualization.service.ValidatorService
import ca.uhn.fhir.context.FhirContext
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.runBlocking
import mergeDataBuffers
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.FhirNarrativeUtils
import org.hl7.fhir.r4.model.Immunization
import org.springframework.http.codec.multipart.FilePart
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.config.CorsRegistry
import reactor.core.publisher.Mono
import java.util.*


@ExperimentalStdlibApi
@ExperimentalCoroutinesApi
@RestController
@RequestMapping("/rest/fhir/r4/viz")
@Tag(name = "fhir")
class VisualizationController(val resourceHtmlGenerator: ResourceHtmlGenerator, val validatorService: ValidatorService) {
    fun fhirValidator() = validatorService.getValidatorAsync(listOf("https://www.ehealth.fgov.be/standards/fhir/lab", "https://www.ehealth.fgov.be/standards/fhir/allergy"))

    fun getMimeType(@RequestBody fhirFile: ByteArray): Char {
        return String(fhirFile)[0]
    }

    init {
        val fhirData = Base64.getDecoder().decode(
            "ewogICJyZXNvdXJjZVR5cGUiOiAiQnVuZGxlIiwKICAiaWQiOiAiYW50aWJpb2dyYW1Jc05lZ2F0aXZlQXNCdW5kbGVDb2xsZWN0aW9uIiwKICAiaWRlbnRpZmllciI6IHsKICAgICJzeXN0ZW0iOiAidXJuOmlldGY6cmZjOjM5ODYiLAogICAgInZhbHVlIjogInVybjp1dWlkOjBjMzIwMWJkLTFjYmYtNGQ2NC1iMDRkLWNkOTE4N2E0YzZlMCIKICB9LAogICJ0eXBlIjogImNvbGxlY3Rpb24iLAogICJlbnRyeSI6IFsKICAgIHsKICAgICAgImZ1bGxVcmwiOiAidXJuOnV1aWQ6N2MxNmM5YzAtYzQ3MS00MDk4LWFlMTgtNDhhYmNhZGQ4ZDkwIiwKICAgICAgInJlc291cmNlIjogewogICAgICAgICJyZXNvdXJjZVR5cGUiOiAiRGlhZ25vc3RpY1JlcG9ydCIsCiAgICAgICAgImlkIjogImRpYWdub3N0aWNyZXBvcnQ2MCIsCiAgICAgICAgIm1ldGEiOiB7CiAgICAgICAgICAidmVyc2lvbklkIjogIjEiLAogICAgICAgICAgInByb2ZpbGUiOiBbCiAgICAgICAgICAgICJodHRwczovL3d3dy5laGVhbHRoLmZnb3YuYmUvc3RhbmRhcmRzL2ZoaXIvU3RydWN0dXJlRGVmaW5pdGlvbi9iZS1sYWJvcmF0b3J5LXJlcG9ydCIKICAgICAgICAgIF0KICAgICAgICB9LAogICAgICAgICJsYW5ndWFnZSI6ICJlbiIsCiAgICAgICAgInRleHQiOiB7CiAgICAgICAgICAic3RhdHVzIjogImdlbmVyYXRlZCIsCiAgICAgICAgICAiZGl2IjogIjxkaXYgeG1sbnM9XCJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hodG1sXCIgeG1sOmxhbmc9XCJlblwiIGxhbmc9XCJlblwiPlxuXHRcdFx0XHRcdFx0PHA+VGhpcyBleGFtcGxlIGlzIHByZXNlbnRlZCBhcyBhIENvbGxlY3Rpb24gYnVuZGxlIGZvciBlYXNlIG9mIHVuZGVyc3RhbmRpbmcuIEl0IG1ha2VzIG5vIGFzc3VtcHRpb24gdG93YXJkcyBhbnkgZmluYWwgaW1wbGVtZW50YXRpb24gb2YgdXNpbmcgRkhJUiB0ZWNobmljYWxseS4gPC9wPlxuXHRcdFx0XHRcdFx0PHA+RG93bmxvYWQgYW5kIG9wZW4gdGhpcyBhcyBYTUwgaW4gYW4gZWRpdG9yIHRvIHZpZXcgWE1MIGFubm90YXRpb25zLjwvcD5cblx0XHRcdFx0XHRcdDxwPkFMTCBMT0lOQywgU05PTUVELUNUIENPREVTIEVUQy4gQVJFIFVTRUQgRk9SIElMTFVTVFJBVElWRSBQVVJQT1NFUyBBTkQgRE8gTk9UIE5FQ0VTU0FSSUxZIFBSRVNFTlQgQSBDTElOSUNBTExZIENPUlJFQ1QgUkVBTCBMSUZFIExBQk9SQVRPUlkgUkVQT1JUPC9wPlxuXHRcdFx0XHRcdFx0PGgyPkEgbmFycmF0aXZlIFNIQUxMIGJlIGluY2x1ZGVkPC9oMj5cblx0XHRcdFx0XHRcdDxwPlNwZWNpZmljYWxseSwgdGhlIHN0cm9uZyByZWNvbW1lbmRhdGlvbiBvZiBITDcgY29uY2VybmluZyB0aGUgdXNlIG9mIHRoZSBuYXJyYXRpdmUgJnF1b3Q7dG8gc3VwcG9ydCBodW1hbi1jb25zdW1wdGlvbiBhcyBhIGZhbGxiYWNrJnF1b3Q7IGlzIGltcG9ydGFudCBpbiB0aGUgY29udGV4dCBvZiB0aGUgbGFib3JhdG9yeSByZXBvcnQuIEluZGVlZCwgdGhpcyAmcXVvdDtodW1hbi1jb25zdW1wdGlvbiZxdW90OyBhc3BlY3QgYWN0dWFsbHkgY29uY2VybnMgdGhlIGluY2x1c2lvbiBvZiBsaW1pdGVkIHhodG1sIGNvbnRlbnQgYW5kIGFzIHN1Y2ggYW4gZWZmb3J0bGVzcyBpbXBsZW1lbnRhdGlvbiB0byB2aXN1YWxpemUgdGhlIGNvbnRlbnQgb2YgdGhlIHJlcG9ydCBieSBjb25zdW1pbmcgc3lzdGVtcyBpcyBndWFyYW50dWVlZC4gQWxzbywgZm9yIHN5c3RlbXMgdGhhdCBtaWdodCBub3QgaW5pdGlhbGx5IGNob29zZSB0byBjb25zdW1lIHJlcG9ydHMgaW4gYSBzdHJ1Y3R1cmVkIHdheSwgdGhpcyBndWFyYW50dWVlcyBhIHdheSB0byBhdCBsZWFzdCB2aXN1YWxpemUgYW5kIHByZXNlbnQgdGhlIGNvbnRlbnQgdG8gdGhlIHVzZXIuPC9wPlxuXHRcdFx0XHRcdFx0PHA+VGhlIGV4YWN0IHByZXNlbnRhdGlvbiBpcyBsZWZ0IGF0IHRoZSBkaXNjcmV0aW9uIG9mIHRoZSBwcm92aWRpbmcgcGFydHkuPC9wPlxuXHRcdFx0XHRcdFx0PHByZT5TcGVjaW1lblx0XHRcdEludHViYXRpb24gQXNwaXJhdGVcbk1hY3Jvc2NvcGljIG9ic2VydmF0aW9uXHRcdE1hdGlnIHB1cnVsZW50XG5DdWx0dXJlXHRcdFx0XHRDb21tZW5zYWxlbiArLTwvcHJlPlxuXHRcdFx0XHRcdDwvZGl2PiIKICAgICAgICB9LAogICAgICAgICJpZGVudGlmaWVyIjogWwogICAgICAgICAgewogICAgICAgICAgICAic3lzdGVtIjogImh0dHA6Ly9hY21lLmNvbS9sYWIvcmVwb3J0cyIsCiAgICAgICAgICAgICJ2YWx1ZSI6ICIxMTUzMDIzMTAwMy4yMDE1MTEwNDEzMTgwMDAwMDAwIgogICAgICAgICAgfQogICAgICAgIF0sCiAgICAgICAgImJhc2VkT24iOiBbCiAgICAgICAgICB7CiAgICAgICAgICAgICJyZWZlcmVuY2UiOiAiU2VydmljZVJlcXVlc3Qvc2VydmljZXJlcXVlc3Q2MCIKICAgICAgICAgIH0KICAgICAgICBdLAogICAgICAgICJzdGF0dXMiOiAiZmluYWwiLAogICAgICAgICJjYXRlZ29yeSI6IFsKICAgICAgICAgIHsKICAgICAgICAgICAgImNvZGluZyI6IFsKICAgICAgICAgICAgICB7CiAgICAgICAgICAgICAgICAic3lzdGVtIjogImh0dHA6Ly90ZXJtaW5vbG9neS5obDcub3JnL0NvZGVTeXN0ZW0vdjItMDA3NCIsCiAgICAgICAgICAgICAgICAiY29kZSI6ICJMQUIiLAogICAgICAgICAgICAgICAgImRpc3BsYXkiOiAiTGFib3JhdG9yeSIKICAgICAgICAgICAgICB9LAogICAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgICJzeXN0ZW0iOiAiaHR0cDovL3Rlcm1pbm9sb2d5LmhsNy5vcmcvQ29kZVN5c3RlbS92Mi0wMDc0IiwKICAgICAgICAgICAgICAgICJjb2RlIjogIk1CIiwKICAgICAgICAgICAgICAgICJkaXNwbGF5IjogIk1pY3JvYmlvbG9neSIKICAgICAgICAgICAgICB9CiAgICAgICAgICAgIF0KICAgICAgICAgIH0KICAgICAgICBdLAogICAgICAgICJjb2RlIjogewogICAgICAgICAgImNvZGluZyI6IFsKICAgICAgICAgICAgewogICAgICAgICAgICAgICJzeXN0ZW0iOiAiaHR0cDovL2xvaW5jLm9yZyIsCiAgICAgICAgICAgICAgImNvZGUiOiAiMTg3NjktMCIsCiAgICAgICAgICAgICAgImRpc3BsYXkiOiAiTWljcm9iaWFsIHN1c2NlcHRpYmlsaXR5IHRlc3RzIFNldCIKICAgICAgICAgICAgfQogICAgICAgICAgXSwKICAgICAgICAgICJ0ZXh0IjogIk1pY3JvYmlhbCBzdXNjZXB0aWJpbGl0eSB0ZXN0cyIKICAgICAgICB9LAogICAgICAgICJzdWJqZWN0IjogewogICAgICAgICAgInJlZmVyZW5jZSI6ICJ1cm46dXVpZDo3YzE2YzljMC1jNDcxLTQwOTgtYWUxOC00OGFiY2FkZDhkOTYiCiAgICAgICAgfSwKICAgICAgICAiZWZmZWN0aXZlRGF0ZVRpbWUiOiAiMjAxNS0xMS0wM1QxMzoxODowMCswMTowMCIsCiAgICAgICAgImlzc3VlZCI6ICIyMDE1LTExLTA0VDEzOjE4OjAwKzAxOjAwIiwKICAgICAgICAicGVyZm9ybWVyIjogWwogICAgICAgICAgewogICAgICAgICAgICAicmVmZXJlbmNlIjogIk9yZ2FuaXphdGlvbi9vcmdhbml6YXRpb24xMCIKICAgICAgICAgIH0KICAgICAgICBdLAogICAgICAgICJyZXN1bHRzSW50ZXJwcmV0ZXIiOiBbCiAgICAgICAgICB7CiAgICAgICAgICAgICJyZWZlcmVuY2UiOiAiUHJhY3RpdGlvbmVyL3ByYWN0aXRpb25lcjExIgogICAgICAgICAgfQogICAgICAgIF0sCiAgICAgICAgInNwZWNpbWVuIjogWwogICAgICAgICAgewogICAgICAgICAgICAicmVmZXJlbmNlIjogIlNwZWNpbWVuL3NwZWNpbWVuNjAiCiAgICAgICAgICB9CiAgICAgICAgXSwKICAgICAgICAicmVzdWx0IjogWwogICAgICAgICAgewogICAgICAgICAgICAicmVmZXJlbmNlIjogInVybjp1dWlkOjdjMTZjOWMwLWM0NzEtNDA5OC1hZTA5LTQ4YWJjYWRkOWQ5MiIKICAgICAgICAgIH0sCiAgICAgICAgICB7CiAgICAgICAgICAgICJyZWZlcmVuY2UiOiAidXJuOnV1aWQ6N2MxNmM5YzAtYzQ3MS00MDk4LWFlMDktNDhhYmNhZGQ5ZDkzIgogICAgICAgICAgfQogICAgICAgIF0sCiAgICAgICAgImNvbmNsdXNpb24iOiAiVGhlIGNvbmNsdXN0aW9uIG9mIHRoaXMgZXhhbXBsZSIKICAgICAgfQogICAgfSwKICAgIHsKICAgICAgImZ1bGxVcmwiOiAidXJuOnV1aWQ6N2MxNmM5YzAtYzQ3MS00MDk4LWFlMTgtNDhhYmNhZGQ4ZDkxIiwKICAgICAgInJlc291cmNlIjogewogICAgICAgICJyZXNvdXJjZVR5cGUiOiAiU3BlY2ltZW4iLAogICAgICAgICJpZCI6ICJzcGVjaW1lbjYwIiwKICAgICAgICAibWV0YSI6IHsKICAgICAgICAgICJ2ZXJzaW9uSWQiOiAiMSIsCiAgICAgICAgICAicHJvZmlsZSI6IFsKICAgICAgICAgICAgImh0dHBzOi8vd3d3LmVoZWFsdGguZmdvdi5iZS9zdGFuZGFyZHMvZmhpci9TdHJ1Y3R1cmVEZWZpbml0aW9uL2JlLXNwZWNpbWVuLWxhYm9yYXRvcnkiCiAgICAgICAgICBdCiAgICAgICAgfSwKICAgICAgICAidGV4dCI6IHsKICAgICAgICAgICJzdGF0dXMiOiAiZ2VuZXJhdGVkIiwKICAgICAgICAgICJkaXYiOiAiPGRpdiB4bWxucz1cImh0dHA6Ly93d3cudzMub3JnLzE5OTkveGh0bWxcIj48cD48Yj5HZW5lcmF0ZWQgTmFycmF0aXZlPC9iPjwvcD48cD48Yj5pZGVudGlmaWVyPC9iPjogaWQ6IDE8L3A+PHA+PGI+c3RhdHVzPC9iPjogYXZhaWxhYmxlPC9wPjxwPjxiPnR5cGU8L2I+OiA8c3BhbiB0aXRsZT1cIkNvZGVzOiB7aHR0cDovL3Nub21lZC5pbmZvL3NjdCAxMTkyOTUwMDh9XCI+QSBzdHJpbmcgY2FuIGJlIGFkZGVkIHRvIG51YW5jZSBvciBleHBsYWluLjwvc3Bhbj48L3A+PHA+PGI+cmVjZWl2ZWRUaW1lPC9iPjogMjAxNS0xMS0wNDwvcD48cD48Yj5ub3RlPC9iPjogU29tZSBleHRyYSByZWxldmFudCBpbmZvcm1hdGlvbiBjb25jZXJuaW5nIHRoZSBzcGVjaW1lbjwvcD48L2Rpdj4iCiAgICAgICAgfSwKICAgICAgICAiaWRlbnRpZmllciI6IFsKICAgICAgICAgIHsKICAgICAgICAgICAgInN5c3RlbSI6ICJodHRwczovL3d3dy5HVEwtTEFCTy5iZS8iLAogICAgICAgICAgICAidmFsdWUiOiAiMSIKICAgICAgICAgIH0KICAgICAgICBdLAogICAgICAgICJzdGF0dXMiOiAiYXZhaWxhYmxlIiwKICAgICAgICAidHlwZSI6IHsKICAgICAgICAgICJjb2RpbmciOiBbCiAgICAgICAgICAgIHsKICAgICAgICAgICAgICAic3lzdGVtIjogImh0dHA6Ly9zbm9tZWQuaW5mby9zY3QiLAogICAgICAgICAgICAgICJjb2RlIjogIjExOTI5NTAwOCIsCiAgICAgICAgICAgICAgImRpc3BsYXkiOiAiQXNwaXJhdGUiCiAgICAgICAgICAgIH0KICAgICAgICAgIF0sCiAgICAgICAgICAidGV4dCI6ICJBIHN0cmluZyBjYW4gYmUgYWRkZWQgdG8gbnVhbmNlIG9yIGV4cGxhaW4uIgogICAgICAgIH0sCiAgICAgICAgInJlY2VpdmVkVGltZSI6ICIyMDE1LTExLTA0IiwKICAgICAgICAibm90ZSI6IFsKICAgICAgICAgIHsKICAgICAgICAgICAgInRleHQiOiAiU29tZSBleHRyYSByZWxldmFudCBpbmZvcm1hdGlvbiBjb25jZXJuaW5nIHRoZSBzcGVjaW1lbiIKICAgICAgICAgIH0KICAgICAgICBdCiAgICAgIH0KICAgIH0sCiAgICB7CiAgICAgICJmdWxsVXJsIjogInVybjp1dWlkOjdjMTZjOWMwLWM0NzEtNDA5OC1hZTA5LTQ4YWJjYWRkOWQ5MiIsCiAgICAgICJyZXNvdXJjZSI6IHsKICAgICAgICAicmVzb3VyY2VUeXBlIjogIk9ic2VydmF0aW9uIiwKICAgICAgICAiaWQiOiAibWFjNjAiLAogICAgICAgICJtZXRhIjogewogICAgICAgICAgInByb2ZpbGUiOiBbCiAgICAgICAgICAgICJodHRwczovL3d3dy5laGVhbHRoLmZnb3YuYmUvc3RhbmRhcmRzL2ZoaXIvU3RydWN0dXJlRGVmaW5pdGlvbi9iZS1vYnNlcnZhdGlvbi1sYWJvcmF0b3J5IgogICAgICAgICAgXQogICAgICAgIH0sCiAgICAgICAgInRleHQiOiB7CiAgICAgICAgICAic3RhdHVzIjogImdlbmVyYXRlZCIsCiAgICAgICAgICAiZGl2IjogIjxkaXYgeG1sbnM9XCJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hodG1sXCI+PHA+PGI+R2VuZXJhdGVkIE5hcnJhdGl2ZTwvYj48L3A+PHA+PC9wPjxwPjxiPmNvZGU8L2I+OiA8c3BhbiB0aXRsZT1cIkNvZGVzOiB7aHR0cDovL2xvaW5jLm9yZyA3NDU3NC01fVwiPk1hY3Jvc2NvcGljIG9ic2VydmF0aW9uPC9zcGFuPjwvcD48cD48Yj5zdWJqZWN0PC9iPjogPGEgaHJlZj1cIiNQYXRpZW50X3BhdGllbnQxXCI+U2VlIGFib3ZlIChQYXRpZW50L3BhdGllbnQxKTwvYT48L3A+PHA+PGI+ZWZmZWN0aXZlPC9iPjogTm92IDQsIDIwMTUsIDI6MTY6MDAgUE08L3A+PHA+PGI+aXNzdWVkPC9iPjogTm92IDQsIDIwMTUsIDI6MTY6MDAgUE08L3A+PHA+PGI+cGVyZm9ybWVyPC9iPjogPGEgaHJlZj1cIiNQcmFjdGl0aW9uZXJfcHJhY3RpdGlvbmVyMTFcIj5TZWUgYWJvdmUgKFByYWN0aXRpb25lci9wcmFjdGl0aW9uZXIxMSk8L2E+PC9wPjxwPjxiPnZhbHVlPC9iPjogbWF0aWcgcHVydWxlbnQ8L3A+PC9kaXY+IgogICAgICAgIH0sCiAgICAgICAgInN0YXR1cyI6ICJmaW5hbCIsCiAgICAgICAgImNvZGUiOiB7CiAgICAgICAgICAiY29kaW5nIjogWwogICAgICAgICAgICB7CiAgICAgICAgICAgICAgInN5c3RlbSI6ICJodHRwOi8vbG9pbmMub3JnIiwKICAgICAgICAgICAgICAiY29kZSI6ICI3NDU3NC01IiwKICAgICAgICAgICAgICAiZGlzcGxheSI6ICJNYWNyb3Njb3BpYyBvYnNlcnZhdGlvbiBbSW50ZXJwcmV0YXRpb25dIGluIFVuc3BlY2lmaWVkIHNwZWNpbWVuIE5hcnJhdGl2ZSIKICAgICAgICAgICAgfQogICAgICAgICAgXSwKICAgICAgICAgICJ0ZXh0IjogIk1hY3Jvc2NvcGljIG9ic2VydmF0aW9uIgogICAgICAgIH0sCiAgICAgICAgInN1YmplY3QiOiB7CiAgICAgICAgICAicmVmZXJlbmNlIjogIlBhdGllbnQvcGF0aWVudDEiCiAgICAgICAgfSwKICAgICAgICAiZWZmZWN0aXZlRGF0ZVRpbWUiOiAiMjAxNS0xMS0wNFQwOToxNjowMC0wNTowMCIsCiAgICAgICAgImlzc3VlZCI6ICIyMDE1LTExLTA0VDA5OjE2OjAwLTA1OjAwIiwKICAgICAgICAicGVyZm9ybWVyIjogWwogICAgICAgICAgewogICAgICAgICAgICAicmVmZXJlbmNlIjogIlByYWN0aXRpb25lci9wcmFjdGl0aW9uZXIxMSIKICAgICAgICAgIH0KICAgICAgICBdLAogICAgICAgICJ2YWx1ZVN0cmluZyI6ICJtYXRpZyBwdXJ1bGVudCIKICAgICAgfQogICAgfSwKICAgIHsKICAgICAgImZ1bGxVcmwiOiAidXJuOnV1aWQ6N2MxNmM5YzAtYzQ3MS00MDk4LWFlMDktNDhhYmNhZGQ5ZDkzIiwKICAgICAgInJlc291cmNlIjogewogICAgICAgICJyZXNvdXJjZVR5cGUiOiAiT2JzZXJ2YXRpb24iLAogICAgICAgICJpZCI6ICJjdWx0NjAiLAogICAgICAgICJtZXRhIjogewogICAgICAgICAgInByb2ZpbGUiOiBbCiAgICAgICAgICAgICJodHRwczovL3d3dy5laGVhbHRoLmZnb3YuYmUvc3RhbmRhcmRzL2ZoaXIvU3RydWN0dXJlRGVmaW5pdGlvbi9iZS1vYnNlcnZhdGlvbi1sYWJvcmF0b3J5IgogICAgICAgICAgXQogICAgICAgIH0sCiAgICAgICAgInRleHQiOiB7CiAgICAgICAgICAic3RhdHVzIjogImdlbmVyYXRlZCIsCiAgICAgICAgICAiZGl2IjogIjxkaXYgeG1sbnM9XCJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hodG1sXCI+PHA+PGI+R2VuZXJhdGVkIE5hcnJhdGl2ZTwvYj48L3A+PHA+PC9wPjxwPjxiPmNvZGU8L2I+OiA8c3BhbiB0aXRsZT1cIkNvZGVzOiB7aHR0cDovL2xvaW5jLm9yZyA0MzQxMS04fVwiPkN1bHR1cmU8L3NwYW4+PC9wPjxwPjxiPnN1YmplY3Q8L2I+OiA8YSBocmVmPVwiI1BhdGllbnRfcGF0aWVudDFcIj5TZWUgYWJvdmUgKFBhdGllbnQvcGF0aWVudDEpPC9hPjwvcD48cD48Yj5lZmZlY3RpdmU8L2I+OiBOb3YgNCwgMjAxNSwgMjoxNjowMCBQTTwvcD48cD48Yj5pc3N1ZWQ8L2I+OiBOb3YgNCwgMjAxNSwgMjoxNjowMCBQTTwvcD48cD48Yj5wZXJmb3JtZXI8L2I+OiA8YSBocmVmPVwiI1ByYWN0aXRpb25lcl9wcmFjdGl0aW9uZXIxMVwiPlNlZSBhYm92ZSAoUHJhY3RpdGlvbmVyL3ByYWN0aXRpb25lcjExKTwvYT48L3A+PHA+PGI+dmFsdWU8L2I+OiBDb21tZW5zYWxlbiArLTwvcD48L2Rpdj4iCiAgICAgICAgfSwKICAgICAgICAic3RhdHVzIjogImZpbmFsIiwKICAgICAgICAiY29kZSI6IHsKICAgICAgICAgICJjb2RpbmciOiBbCiAgICAgICAgICAgIHsKICAgICAgICAgICAgICAic3lzdGVtIjogImh0dHA6Ly9sb2luYy5vcmciLAogICAgICAgICAgICAgICJjb2RlIjogIjQzNDExLTgiLAogICAgICAgICAgICAgICJkaXNwbGF5IjogIkJhY3RlcmlhIGlkZW50aWZpZWQgaW4gQXNwaXJhdGUgYnkgQ3VsdHVyZSIKICAgICAgICAgICAgfQogICAgICAgICAgXSwKICAgICAgICAgICJ0ZXh0IjogIkN1bHR1cmUiCiAgICAgICAgfSwKICAgICAgICAic3ViamVjdCI6IHsKICAgICAgICAgICJyZWZlcmVuY2UiOiAiUGF0aWVudC9wYXRpZW50MSIKICAgICAgICB9LAogICAgICAgICJlZmZlY3RpdmVEYXRlVGltZSI6ICIyMDE1LTExLTA0VDA5OjE2OjAwLTA1OjAwIiwKICAgICAgICAiaXNzdWVkIjogIjIwMTUtMTEtMDRUMDk6MTY6MDAtMDU6MDAiLAogICAgICAgICJwZXJmb3JtZXIiOiBbCiAgICAgICAgICB7CiAgICAgICAgICAgICJyZWZlcmVuY2UiOiAiUHJhY3RpdGlvbmVyL3ByYWN0aXRpb25lcjExIgogICAgICAgICAgfQogICAgICAgIF0sCiAgICAgICAgInZhbHVlU3RyaW5nIjogIkNvbW1lbnNhbGVuICstIgogICAgICB9CiAgICB9LAogICAgewogICAgICAiZnVsbFVybCI6ICJ1cm46dXVpZDo3YzE2YzljMC1jNDcxLTQwOTgtYWUxOC00OGFiY2FkZDhkOTQiLAogICAgICAicmVzb3VyY2UiOiB7CiAgICAgICAgInJlc291cmNlVHlwZSI6ICJPcmdhbml6YXRpb24iLAogICAgICAgICJpZCI6ICJvcmdhbml6YXRpb24xMCIsCiAgICAgICAgIm1ldGEiOiB7CiAgICAgICAgICAicHJvZmlsZSI6IFsKICAgICAgICAgICAgImh0dHBzOi8vd3d3LmVoZWFsdGguZmdvdi5iZS9zdGFuZGFyZHMvZmhpci9TdHJ1Y3R1cmVEZWZpbml0aW9uL2JlLW9yZ2FuaXphdGlvbiIKICAgICAgICAgIF0KICAgICAgICB9LAogICAgICAgICJ0ZXh0IjogewogICAgICAgICAgInN0YXR1cyI6ICJnZW5lcmF0ZWQiLAogICAgICAgICAgImRpdiI6ICI8ZGl2IHhtbG5zPVwiaHR0cDovL3d3dy53My5vcmcvMTk5OS94aHRtbFwiPjxwPjxiPkdlbmVyYXRlZCBOYXJyYXRpdmU8L2I+PC9wPjxwPjxiPmlkZW50aWZpZXI8L2I+OiBNZWRpY2FsIExpY2Vuc2UgbnVtYmVyOiA4MTE2NTM0Mzk5OCAoT0ZGSUNJQUwpPC9wPjxwPjxiPmFjdGl2ZTwvYj46IHRydWU8L3A+PHA+PGI+dHlwZTwvYj46IDxzcGFuIHRpdGxlPVwiQ29kZXM6IHtodHRwczovL3d3dy5laGVhbHRoLmZnb3YuYmUvc3RhbmRhcmRzL2ZoaXIvQ29kZVN5c3RlbS9jZC1oY3BhcnR5IG9yZ2xhYm9yYXRvcnl9XCI+SW5kZXBlbmRlbnQgbGFib3JhdG9yeTwvc3Bhbj48L3A+PHA+PGI+bmFtZTwvYj46IEdUTCAtIEdlbmVyYWwgVGVzdGluZyBMYWJvcmF0b3J5PC9wPjxwPjxiPnRlbGVjb208L2I+OiBwaDogMzIyNjc1MTk5KFdPUkspPC9wPjxwPjxiPmFkZHJlc3M8L2I+OiBWdWxjYW5zc3RyYWF0IDEyMCwgMTAwMCBCcnVzc2VsKFdPUkspPC9wPjwvZGl2PiIKICAgICAgICB9LAogICAgICAgICJpZGVudGlmaWVyIjogWwogICAgICAgICAgewogICAgICAgICAgICAidXNlIjogIm9mZmljaWFsIiwKICAgICAgICAgICAgInR5cGUiOiB7CiAgICAgICAgICAgICAgImNvZGluZyI6IFsKICAgICAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgICAgInN5c3RlbSI6ICJodHRwOi8vdGVybWlub2xvZ3kuaGw3Lm9yZy9Db2RlU3lzdGVtL3YyLTAyMDMiLAogICAgICAgICAgICAgICAgICAiY29kZSI6ICJNRCIsCiAgICAgICAgICAgICAgICAgICJkaXNwbGF5IjogIk1lZGljYWwgTGljZW5zZSBudW1iZXIiCiAgICAgICAgICAgICAgICB9CiAgICAgICAgICAgICAgXQogICAgICAgICAgICB9LAogICAgICAgICAgICAic3lzdGVtIjogImh0dHBzOi8vd3d3LmVoZWFsdGguZmdvdi5iZS9zdGFuZGFyZHMvZmhpci9OYW1pbmdTeXN0ZW0vbmloZGkiLAogICAgICAgICAgICAidmFsdWUiOiAiODExNjUzNDM5OTgiCiAgICAgICAgICB9CiAgICAgICAgXSwKICAgICAgICAiYWN0aXZlIjogdHJ1ZSwKICAgICAgICAidHlwZSI6IFsKICAgICAgICAgIHsKICAgICAgICAgICAgImNvZGluZyI6IFsKICAgICAgICAgICAgICB7CiAgICAgICAgICAgICAgICAic3lzdGVtIjogImh0dHBzOi8vd3d3LmVoZWFsdGguZmdvdi5iZS9zdGFuZGFyZHMvZmhpci9Db2RlU3lzdGVtL2NkLWhjcGFydHkiLAogICAgICAgICAgICAgICAgImNvZGUiOiAib3JnbGFib3JhdG9yeSIKICAgICAgICAgICAgICB9CiAgICAgICAgICAgIF0KICAgICAgICAgIH0KICAgICAgICBdLAogICAgICAgICJuYW1lIjogIkdUTCAtIEdlbmVyYWwgVGVzdGluZyBMYWJvcmF0b3J5IiwKCiAgICAgICAgImFkZHJlc3MiOiBbCiAgICAgICAgICB7CiAgICAgICAgICAgICJleHRlbnNpb24iOiBbCiAgICAgICAgICAgICAgewogICAgICAgICAgICAgICAgInVybCI6ICJodHRwOi8vaGw3Lm9yZy9maGlyL1N0cnVjdHVyZURlZmluaXRpb24vbGFuZ3VhZ2UiLAogICAgICAgICAgICAgICAgInZhbHVlQ29kZSI6ICJubCIKICAgICAgICAgICAgICB9CiAgICAgICAgICAgIF0sCiAgICAgICAgICAgICJ1c2UiOiAid29yayIsCiAgICAgICAgICAgICJ0eXBlIjogImJvdGgiLAogICAgICAgICAgICAidGV4dCI6ICJWdWxjYW5zc3RyYWF0IDEyMCwgMTAwMCBCcnVzc2VsIiwKICAgICAgICAgICAgImxpbmUiOiBbCiAgICAgICAgICAgICAgIlZ1bGNhbnNzdHJhYXQgMTIwIgogICAgICAgICAgICBdLAogICAgICAgICAgICAiX2xpbmUiOiBbCiAgICAgICAgICAgICAgewogICAgICAgICAgICAgICAgImV4dGVuc2lvbiI6IFsKICAgICAgICAgICAgICAgICAgewogICAgICAgICAgICAgICAgICAgICJ1cmwiOiAiaHR0cDovL2hsNy5vcmcvZmhpci9TdHJ1Y3R1cmVEZWZpbml0aW9uL2lzbzIxMDkwLUFEWFAtc3RyZWV0TmFtZSIsCiAgICAgICAgICAgICAgICAgICAgInZhbHVlU3RyaW5nIjogIlZ1bGNhbnNzdHJhYXQiCiAgICAgICAgICAgICAgICAgIH0sCiAgICAgICAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgICAgICAidXJsIjogImh0dHA6Ly9obDcub3JnL2ZoaXIvU3RydWN0dXJlRGVmaW5pdGlvbi9pc28yMTA5MC1BRFhQLWhvdXNlTnVtYmVyIiwKICAgICAgICAgICAgICAgICAgICAidmFsdWVTdHJpbmciOiAiMTIwIgogICAgICAgICAgICAgICAgICB9CiAgICAgICAgICAgICAgICBdCiAgICAgICAgICAgICAgfQogICAgICAgICAgICBdLAogICAgICAgICAgICAiY2l0eSI6ICJCcnVzc2VsIiwKICAgICAgICAgICAgInBvc3RhbENvZGUiOiAiMTAwMCIsCiAgICAgICAgICAgICJjb3VudHJ5IjogIkJFIgogICAgICAgICAgfQogICAgICAgIF0KICAgICAgfQogICAgfSwKICAgIHsKICAgICAgImZ1bGxVcmwiOiAidXJuOnV1aWQ6N2MxNmM5YzAtYzQ3MS00MDk4LWFlMTgtNDhhYmNhZGQ4ZDk1IiwKICAgICAgInJlc291cmNlIjogewogICAgICAgICJyZXNvdXJjZVR5cGUiOiAiUHJhY3RpdGlvbmVyIiwKICAgICAgICAiaWQiOiAicHJhY3RpdGlvbmVyMTEiLAogICAgICAgICJtZXRhIjogewogICAgICAgICAgInByb2ZpbGUiOiBbCiAgICAgICAgICAgICJodHRwczovL3d3dy5laGVhbHRoLmZnb3YuYmUvc3RhbmRhcmRzL2ZoaXIvU3RydWN0dXJlRGVmaW5pdGlvbi9iZS1wcmFjdGl0aW9uZXIiCiAgICAgICAgICBdCiAgICAgICAgfSwKICAgICAgICAidGV4dCI6IHsKICAgICAgICAgICJzdGF0dXMiOiAiZ2VuZXJhdGVkIiwKICAgICAgICAgICJkaXYiOiAiPGRpdiB4bWxucz1cImh0dHA6Ly93d3cudzMub3JnLzE5OTkveGh0bWxcIj48cD48Yj5HZW5lcmF0ZWQgTmFycmF0aXZlPC9iPjwvcD48cD48Yj5pZGVudGlmaWVyPC9iPjogaWQ6IDU1NDQ4ODk5NyAoT0ZGSUNJQUwpPC9wPjxwPjxiPm5hbWU8L2I+OiBOaWNvbGFzIERhdm91c3QgKE9GRklDSUFMKTwvcD48cD48Yj50ZWxlY29tPC9iPjogPGEgaHJlZj1cIm1haWx0bzpuaWNvbGFzLmRhdm91c3RAZ3RsLmJlXCI+bmljb2xhcy5kYXZvdXN0QGd0bC5iZTwvYT4sIHBoOiAwMjI2NzUxOTgoV09SSyksIGZheDogMDIyNjc1MjA5KFdPUkspPC9wPjwvZGl2PiIKICAgICAgICB9LAogICAgICAgICJpZGVudGlmaWVyIjogWwogICAgICAgICAgewogICAgICAgICAgICAidXNlIjogIm9mZmljaWFsIiwKICAgICAgICAgICAgInN5c3RlbSI6ICJodHRwczovL3d3dy5laGVhbHRoLmZnb3YuYmUvc3RhbmRhcmRzL2ZoaXIvTmFtaW5nU3lzdGVtL25paGRpIiwKICAgICAgICAgICAgInZhbHVlIjogIjU1NDQ4ODk5NyIKICAgICAgICAgIH0KICAgICAgICBdLAogICAgICAgICJuYW1lIjogWwogICAgICAgICAgewogICAgICAgICAgICAidXNlIjogIm9mZmljaWFsIiwKICAgICAgICAgICAgImZhbWlseSI6ICJEYXZvdXN0IiwKICAgICAgICAgICAgImdpdmVuIjogWwogICAgICAgICAgICAgICJOaWNvbGFzIgogICAgICAgICAgICBdCiAgICAgICAgICB9CiAgICAgICAgXSwKICAgICAgICAidGVsZWNvbSI6IFsKICAgICAgICAgIHsKICAgICAgICAgICAgInN5c3RlbSI6ICJlbWFpbCIsCiAgICAgICAgICAgICJ2YWx1ZSI6ICJuaWNvbGFzLmRhdm91c3RAZ3RsLmJlIiwKICAgICAgICAgICAgInVzZSI6ICJ3b3JrIgogICAgICAgICAgfSwKICAgICAgICAgIHsKICAgICAgICAgICAgInN5c3RlbSI6ICJwaG9uZSIsCiAgICAgICAgICAgICJ2YWx1ZSI6ICIwMjI2NzUxOTgiLAogICAgICAgICAgICAidXNlIjogIndvcmsiCiAgICAgICAgICB9LAogICAgICAgICAgewogICAgICAgICAgICAic3lzdGVtIjogImZheCIsCiAgICAgICAgICAgICJ2YWx1ZSI6ICIwMjI2NzUyMDkiLAogICAgICAgICAgICAidXNlIjogIndvcmsiCiAgICAgICAgICB9CiAgICAgICAgXQogICAgICB9CiAgICB9LAogICAgewogICAgICAiZnVsbFVybCI6ICJ1cm46dXVpZDo3YzE2YzljMC1jNDcxLTQwOTgtYWUxOC00OGFiY2FkZDhkOTYiLAogICAgICAicmVzb3VyY2UiOiB7CiAgICAgICAgInJlc291cmNlVHlwZSI6ICJQYXRpZW50IiwKICAgICAgICAiaWQiOiAicGF0aWVudDEiLAogICAgICAgICJtZXRhIjogewogICAgICAgICAgInByb2ZpbGUiOiBbCiAgICAgICAgICAgICJodHRwczovL3d3dy5laGVhbHRoLmZnb3YuYmUvc3RhbmRhcmRzL2ZoaXIvU3RydWN0dXJlRGVmaW5pdGlvbi9iZS1wYXRpZW50IgogICAgICAgICAgXQogICAgICAgIH0sCiAgICAgICAgInRleHQiOiB7CiAgICAgICAgICAic3RhdHVzIjogImdlbmVyYXRlZCIsCiAgICAgICAgICAiZGl2IjogIjxkaXYgeG1sbnM9XCJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hodG1sXCI+PHA+PGI+R2VuZXJhdGVkIE5hcnJhdGl2ZTwvYj48L3A+PHA+PGI+aWRlbnRpZmllcjwvYj46IGlkOiA3OTEyMTEzNzc0MCAoT0ZGSUNJQUwpPC9wPjxwPjxiPmFjdGl2ZTwvYj46IHRydWU8L3A+PHA+PGI+bmFtZTwvYj46IEpvc2VwaGluZSBOZXNzYSBMYSBQYXJhZGlzaW8gPC9wPjxwPjxiPnRlbGVjb208L2I+OiA8YSBocmVmPVwibWFpbHRvOm5lc3NhLmxhcGFyYWRpc2lvQGJlbGdpdW0uYmVcIj5uZXNzYS5sYXBhcmFkaXNpb0BiZWxnaXVtLmJlPC9hPiwgPGEgaHJlZj1cInRlbDorMzIyNDc2NzkyOTc5XCI+KzMyMjQ3Njc5Mjk3OTwvYT4sIDxhIGhyZWY9XCJ0ZWw6KzMyMjY3MTg2NTVcIj4rMzIyNjcxODY1NTwvYT4sIDxhIGhyZWY9XCJ0ZWw6KzMyMjQ3Njc5OVwiPiszMjI0NzY3OTk8L2E+PC9wPjxwPjxiPmdlbmRlcjwvYj46IGZlbWFsZTwvcD48cD48Yj5iaXJ0aERhdGU8L2I+OiAxOTc5LTEyLTExPC9wPjxwPjxiPmFkZHJlc3M8L2I+OiBTbG9vcmRlbGxlIDQyLCAxMTYwIE91ZGVyZ2VtKEhPTUUpPC9wPjwvZGl2PiIKICAgICAgICB9LAogICAgICAgICJpZGVudGlmaWVyIjogWwogICAgICAgICAgewogICAgICAgICAgICAidXNlIjogIm9mZmljaWFsIiwKICAgICAgICAgICAgInN5c3RlbSI6ICJodHRwczovL3d3dy5laGVhbHRoLmZnb3YuYmUvc3RhbmRhcmRzL2ZoaXIvTmFtaW5nU3lzdGVtL3NzaW4iLAogICAgICAgICAgICAidmFsdWUiOiAiNzkxMjExMzc3NDAiCiAgICAgICAgICB9CiAgICAgICAgXSwKICAgICAgICAiYWN0aXZlIjogdHJ1ZSwKICAgICAgICAibmFtZSI6IFsKICAgICAgICAgIHsKICAgICAgICAgICAgImZhbWlseSI6ICJMYSBQYXJhZGlzaW8iLAogICAgICAgICAgICAiZ2l2ZW4iOiBbCiAgICAgICAgICAgICAgIkpvc2VwaGluZSIsCiAgICAgICAgICAgICAgIk5lc3NhIgogICAgICAgICAgICBdCiAgICAgICAgICB9CiAgICAgICAgXSwKICAgICAgICAidGVsZWNvbSI6IFsKICAgICAgICAgIHsKICAgICAgICAgICAgInN5c3RlbSI6ICJlbWFpbCIsCiAgICAgICAgICAgICJ2YWx1ZSI6ICJuZXNzYS5sYXBhcmFkaXNpb0BiZWxnaXVtLmJlIgogICAgICAgICAgfSwKICAgICAgICAgIHsKICAgICAgICAgICAgInN5c3RlbSI6ICJwaG9uZSIsCiAgICAgICAgICAgICJ2YWx1ZSI6ICIrMzIyNDc2NzkyOTc5IiwKICAgICAgICAgICAgInVzZSI6ICJtb2JpbGUiCiAgICAgICAgICB9LAogICAgICAgICAgewogICAgICAgICAgICAic3lzdGVtIjogInBob25lIiwKICAgICAgICAgICAgInZhbHVlIjogIiszMjI2NzE4NjU1IiwKICAgICAgICAgICAgInVzZSI6ICJob21lIgogICAgICAgICAgfSwKICAgICAgICAgIHsKICAgICAgICAgICAgInN5c3RlbSI6ICJwaG9uZSIsCiAgICAgICAgICAgICJ2YWx1ZSI6ICIrMzIyNDc2Nzk5IiwKICAgICAgICAgICAgInVzZSI6ICJ3b3JrIgogICAgICAgICAgfQogICAgICAgIF0sCiAgICAgICAgImdlbmRlciI6ICJmZW1hbGUiLAogICAgICAgICJiaXJ0aERhdGUiOiAiMTk3OS0xMi0xMSIsCiAgICAgICAgIl9iaXJ0aERhdGUiOiB7CiAgICAgICAgICAiZXh0ZW5zaW9uIjogWwogICAgICAgICAgICB7CiAgICAgICAgICAgICAgInVybCI6ICJodHRwOi8vaGw3Lm9yZy9maGlyL1N0cnVjdHVyZURlZmluaXRpb24vcGF0aWVudC1iaXJ0aFRpbWUiLAogICAgICAgICAgICAgICJ2YWx1ZURhdGVUaW1lIjogIjE5NzktMTItMTFUMTM6Mjg6MTctMDU6MDAiCiAgICAgICAgICAgIH0KICAgICAgICAgIF0KICAgICAgICB9LAogICAgICAgICJhZGRyZXNzIjogWwogICAgICAgICAgewogICAgICAgICAgICAidXNlIjogImhvbWUiLAogICAgICAgICAgICAidHlwZSI6ICJib3RoIiwKICAgICAgICAgICAgInRleHQiOiAiU2xvb3JkZWxsZSA0MiwgMTE2MCBPdWRlcmdlbSIsCiAgICAgICAgICAgICJsaW5lIjogWwogICAgICAgICAgICAgICJTbG9vcmRlbGxlIDQyIgogICAgICAgICAgICBdLAogICAgICAgICAgICAiY2l0eSI6ICJPdWRlcmdlbSIsCiAgICAgICAgICAgICJwb3N0YWxDb2RlIjogIjExNjAiLAogICAgICAgICAgICAiY291bnRyeSI6ICJCRSIKICAgICAgICAgIH0KICAgICAgICBdCiAgICAgIH0KICAgIH0sCiAgICB7CiAgICAgICJmdWxsVXJsIjogInVybjp1dWlkOjdjMTZjOWMwLWM0NzEtNDA5OC1hZTE4LTQ4YWJjYWRkOGQ5NyIsCiAgICAgICJyZXNvdXJjZSI6IHsKICAgICAgICAicmVzb3VyY2VUeXBlIjogIlNlcnZpY2VSZXF1ZXN0IiwKICAgICAgICAiaWQiOiAic2VydmljZXJlcXVlc3Q2MCIsCiAgICAgICAgInRleHQiOiB7CiAgICAgICAgICAic3RhdHVzIjogImdlbmVyYXRlZCIsCiAgICAgICAgICAiZGl2IjogIjxkaXYgeG1sbnM9XCJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hodG1sXCI+PHA+PGI+R2VuZXJhdGVkIE5hcnJhdGl2ZTwvYj48L3A+PHA+PGI+aWRlbnRpZmllcjwvYj46IGlkOiAxMjMzNDU0NjwvcD48cD48Yj5zdGF0dXM8L2I+OiBhY3RpdmU8L3A+PHA+PGI+aW50ZW50PC9iPjogb3JkZXI8L3A+PHA+PGI+Y2F0ZWdvcnk8L2I+OiA8c3BhbiB0aXRsZT1cIkNvZGVzOiB7aHR0cDovL3Nub21lZC5pbmZvL3NjdCAxMDgyNTIwMDd9XCI+TGFib3JhdG9yeSBwcm9jZWR1cmU8L3NwYW4+PC9wPjxwPjxiPnN1YmplY3Q8L2I+OiA8YSBocmVmPVwiI1BhdGllbnRfcGF0aWVudDFcIj5TZWUgYWJvdmUgKFBhdGllbnQvcGF0aWVudDEpPC9hPjwvcD48cD48Yj5hdXRob3JlZE9uPC9iPjogTm92IDEsIDIwMTUsIDE6NDE6MDAgUE08L3A+PHA+PGI+cmVxdWVzdGVyPC9iPjogPGEgaHJlZj1cIiNQcmFjdGl0aW9uZXJfcHJhY3RpdGlvbmVyMVwiPlNlZSBhYm92ZSAoUHJhY3RpdGlvbmVyL3ByYWN0aXRpb25lcjEpPC9hPjwvcD48cD48Yj5zcGVjaW1lbjwvYj46IDxhIGhyZWY9XCIjU3BlY2ltZW5fc3BlY2ltZW42MFwiPlNlZSBhYm92ZSAoU3BlY2ltZW4vc3BlY2ltZW42MCk8L2E+PC9wPjwvZGl2PiIKICAgICAgICB9LAogICAgICAgICJpZGVudGlmaWVyIjogWwogICAgICAgICAgewogICAgICAgICAgICAic3lzdGVtIjogImh0dHBzOi8vd3d3LkdUTC1MQUJPLmJlL29yZGVyaW5nc3lzdGVtIiwKICAgICAgICAgICAgInZhbHVlIjogIjEyMzM0NTQ2IgogICAgICAgICAgfQogICAgICAgIF0sCiAgICAgICAgInN0YXR1cyI6ICJhY3RpdmUiLAogICAgICAgICJpbnRlbnQiOiAib3JkZXIiLAogICAgICAgICJjYXRlZ29yeSI6IFsKICAgICAgICAgIHsKICAgICAgICAgICAgImNvZGluZyI6IFsKICAgICAgICAgICAgICB7CiAgICAgICAgICAgICAgICAic3lzdGVtIjogImh0dHA6Ly9zbm9tZWQuaW5mby9zY3QiLAogICAgICAgICAgICAgICAgImNvZGUiOiAiMTA4MjUyMDA3IiwKICAgICAgICAgICAgICAgICJkaXNwbGF5IjogIkxhYm9yYXRvcnkgcHJvY2VkdXJlIgogICAgICAgICAgICAgIH0KICAgICAgICAgICAgXQogICAgICAgICAgfQogICAgICAgIF0sCiAgICAgICAgInN1YmplY3QiOiB7CiAgICAgICAgICAicmVmZXJlbmNlIjogIlBhdGllbnQvcGF0aWVudDEiCiAgICAgICAgfSwKICAgICAgICAiYXV0aG9yZWRPbiI6ICIyMDE1LTExLTAxVDE0OjQxOjAwKzAxOjAwIiwKICAgICAgICAicmVxdWVzdGVyIjogewogICAgICAgICAgInJlZmVyZW5jZSI6ICJQcmFjdGl0aW9uZXIvcHJhY3RpdGlvbmVyMSIKICAgICAgICB9LAogICAgICAgICJzcGVjaW1lbiI6IFsKICAgICAgICAgIHsKICAgICAgICAgICAgInJlZmVyZW5jZSI6ICJTcGVjaW1lbi9zcGVjaW1lbjYwIgogICAgICAgICAgfQogICAgICAgIF0KICAgICAgfQogICAgfSwKICAgIHsKICAgICAgImZ1bGxVcmwiOiAidXJuOnV1aWQ6N2MxNmM5YzAtYzQ3MS00MDk4LWFlMTgtNDhhYmNhZGQ4ZDk4IiwKICAgICAgInJlc291cmNlIjogewogICAgICAgICJyZXNvdXJjZVR5cGUiOiAiUHJhY3RpdGlvbmVyIiwKICAgICAgICAiaWQiOiAicHJhY3RpdGlvbmVyMSIsCiAgICAgICAgIm1ldGEiOiB7CiAgICAgICAgICAicHJvZmlsZSI6IFsKICAgICAgICAgICAgImh0dHBzOi8vd3d3LmVoZWFsdGguZmdvdi5iZS9zdGFuZGFyZHMvZmhpci9TdHJ1Y3R1cmVEZWZpbml0aW9uL2JlLXByYWN0aXRpb25lciIKICAgICAgICAgIF0KICAgICAgICB9LAogICAgICAgICJ0ZXh0IjogewogICAgICAgICAgInN0YXR1cyI6ICJnZW5lcmF0ZWQiLAogICAgICAgICAgImRpdiI6ICI8ZGl2IHhtbG5zPVwiaHR0cDovL3d3dy53My5vcmcvMTk5OS94aHRtbFwiPjxwPjxiPkdlbmVyYXRlZCBOYXJyYXRpdmU8L2I+PC9wPjxwPjxiPmlkZW50aWZpZXI8L2I+OiBpZDogMTg3NDk3MDQ0NzcgKE9GRklDSUFMKTwvcD48cD48Yj5uYW1lPC9iPjogS2F0aGVyaW5lIFB1bGFza2kgKE9GRklDSUFMKTwvcD48cD48Yj50ZWxlY29tPC9iPjogPGEgaHJlZj1cIm1haWx0bzprYXRoZXJpbmUucHVsYXNraUBlbnRlcnByaXNlaG9zcGl0YWwuYmVcIj5rYXRoZXJpbmUucHVsYXNraUBlbnRlcnByaXNlaG9zcGl0YWwuYmU8L2E+LCBwaDogMDIyNjc1MTk4KFdPUkspLCBmYXg6IDAyMjY3NTIwOShXT1JLKTwvcD48cD48Yj5hZGRyZXNzPC9iPjogSnVwaXRlcmxhYW4gNSwgMTg1MyBHcmltYmVyZ2VuKEhPTUUpPC9wPjwvZGl2PiIKICAgICAgICB9LAogICAgICAgICJpZGVudGlmaWVyIjogWwogICAgICAgICAgewogICAgICAgICAgICAidXNlIjogIm9mZmljaWFsIiwKICAgICAgICAgICAgInN5c3RlbSI6ICJodHRwczovL3d3dy5laGVhbHRoLmZnb3YuYmUvc3RhbmRhcmRzL2ZoaXIvTmFtaW5nU3lzdGVtL25paGRpIiwKICAgICAgICAgICAgInZhbHVlIjogIjE4NzQ5NzA0NDc3IgogICAgICAgICAgfQogICAgICAgIF0sCiAgICAgICAgIm5hbWUiOiBbCiAgICAgICAgICB7CiAgICAgICAgICAgICJ1c2UiOiAib2ZmaWNpYWwiLAogICAgICAgICAgICAiZmFtaWx5IjogIlB1bGFza2kiLAogICAgICAgICAgICAiZ2l2ZW4iOiBbCiAgICAgICAgICAgICAgIkthdGhlcmluZSIKICAgICAgICAgICAgXSwKICAgICAgICAgICAgInN1ZmZpeCI6IFsKICAgICAgICAgICAgICAiTUQiCiAgICAgICAgICAgIF0KICAgICAgICAgIH0KICAgICAgICBdLAogICAgICAgICJ0ZWxlY29tIjogWwogICAgICAgICAgewogICAgICAgICAgICAic3lzdGVtIjogImVtYWlsIiwKICAgICAgICAgICAgInZhbHVlIjogImthdGhlcmluZS5wdWxhc2tpQGVudGVycHJpc2Vob3NwaXRhbC5iZSIsCiAgICAgICAgICAgICJ1c2UiOiAid29yayIKICAgICAgICAgIH0sCiAgICAgICAgICB7CiAgICAgICAgICAgICJzeXN0ZW0iOiAicGhvbmUiLAogICAgICAgICAgICAidmFsdWUiOiAiMDIyNjc1MTk4IiwKICAgICAgICAgICAgInVzZSI6ICJ3b3JrIgogICAgICAgICAgfSwKICAgICAgICAgIHsKICAgICAgICAgICAgInN5c3RlbSI6ICJmYXgiLAogICAgICAgICAgICAidmFsdWUiOiAiMDIyNjc1MjA5IiwKICAgICAgICAgICAgInVzZSI6ICJ3b3JrIgogICAgICAgICAgfQogICAgICAgIF0KICAgICAgfQogICAgfQogIF0KfQo="
        )

        runBlocking {
            makeNarrative(fhirData, false, "lab", "{".toCharArray()[0])
            fhirValidator().await().validate(fhirData)
        }
    }

    @PostMapping("lab/html", consumes = ["application/json", "application/xml"])
    @Operation(summary = "Convert FHIR file to html")
    fun htmlLab(@RequestBody fhirFile: ByteArray, response: ServerHttpResponse): Mono<Void> =
        makeResponseLab(fhirFile, response, true, String(fhirFile)[0])

    @PostMapping("vaccination/html", consumes = ["application/json", "application/xml"])
    @Operation(summary = "Convert FHIR file to html")
    fun htmlVaccination(@RequestBody fhirFile: ByteArray, response: ServerHttpResponse): Mono<Void> =
            makeResponseVaccination(fhirFile, response, true, String(fhirFile)[0])


    @PostMapping("lab/html/validate", consumes = ["application/json", "application/xml"])
    @Operation(summary = "Validate and convert FHIR file to object including validation information and html representation")
    fun htmlAndValidateLab(@RequestBody fhirFile: ByteArray) = mono {

        fhirValidator().await().validate(fhirFile).let { (errors, validationReport) ->
            HtmlWithValidation(
                html = makeNarrative(fhirFile, true,"lab", getMimeType(fhirFile)).toString(Charsets.UTF_8),
                errors = errors,
                validation = validationReport
            )
        }
    }

    @PostMapping("vaccination/html/validate", consumes = ["application/json", "application/xml"])
    @Operation(summary = "Validate and convert FHIR file to object including validation information and html representation")
    fun htmlAndValidateVaccination(@RequestBody fhirFile: ByteArray) = mono {

        fhirValidator().await().validate(fhirFile).let { (errors, validationReport) ->
            HtmlWithValidation(
                    html = makeNarrative(fhirFile, true,"vaccination", getMimeType(fhirFile)).toString(Charsets.UTF_8),
                    errors = errors,
                    validation = validationReport
            )
        }

    }

    @PostMapping("lab/validate", consumes = ["application/json", "application/xml"])
    @Operation(summary = "Validate laboratory report")
    fun validateLab(@RequestBody fhirFile: ByteArray) = mono {

        fhirValidator().await().validate(fhirFile).let { (errors) ->
            Validation(
                    errors = errors
            )
        }
    }

    @PostMapping("lab/div", consumes = ["application/json", "application/xml"])
    @Operation(summary = "Convert FHIR file to embeddable div")
    fun divLab(@RequestBody fhirFile: ByteArray, response: ServerHttpResponse): Mono<Void> =
        makeResponseLab(fhirFile, response, false, String(fhirFile)[0])

    @PostMapping("vaccination/div", consumes = ["application/json", "application/xml"])
    @Operation(summary = "Convert FHIR file to embeddable div")
    fun divVaccination(@RequestBody fhirFile: ByteArray, response: ServerHttpResponse): Mono<Void> =
            makeResponseVaccination(fhirFile, response, false, String(fhirFile)[0])

    @PostMapping("lab/div/validate", consumes = ["application/json", "application/xml"])
    @Operation(summary = "Validate and convert FHIR file to object including validation information and embeddable div")
    fun divAndValidateLab(@RequestBody fhirFile: ByteArray) = mono {

        fhirValidator().await().validate(fhirFile).let { (errors, validationReport) ->
            HtmlWithValidation(
                html = makeNarrative(fhirFile, false,"lab", getMimeType(fhirFile)).toString(Charsets.UTF_8),
                errors = errors,
                validation = validationReport
            )
        }
    }

    @PostMapping("vaccination/div/validate", consumes = ["application/json", "application/xml"])
    @Operation(summary = "Validate and convert FHIR file to object including validation information and embeddable div")
    fun divAndValidateVaccination(@RequestBody fhirFile: ByteArray) = mono {

        fhirValidator().await().validate(fhirFile).let { (errors, validationReport) ->
            HtmlWithValidation(
                    html = makeNarrative(fhirFile, false,"vaccination", getMimeType(fhirFile)).toString(Charsets.UTF_8),
                    errors = errors,
                    validation = validationReport
            )
        }
    }

    /*@PostMapping("html/single")
    @Operation(summary = "Convert FHIR file provided as multipart request to html")
    fun htmlMultipartSingleLab(
        @RequestPart("file") fhirFilePart: Mono<FilePart>,
        response: ServerHttpResponse
    ): Mono<Void> =
        fhirFilePart.flatMap { it.content().mergeDataBuffers() }
            .flatMap { fhirFile -> makeResponseLab(fhirFile, response, true) }

    @PostMapping("vaccination/html/single")
    @Operation(summary = "Convert FHIR file provided as multipart request to html")
    fun htmlMultipartSingleVaccination(
            @RequestPart("file") fhirFilePart: Mono<FilePart>,
            response: ServerHttpResponse
    ): Mono<Void> =
            fhirFilePart.flatMap { it.content().mergeDataBuffers() }
                    .flatMap { fhirFile -> makeResponseVaccination(fhirFile, response, true) }*/

    @PostMapping("lab/html/single/validate")
    @Operation(summary = "Convert FHIR file provided as multipart request to html")
    fun htmlAndValidateMultipartSingleLab(
        @RequestPart("file") fhirFilePart: Mono<FilePart>
    ) = fhirFilePart.flatMap { it.content().mergeDataBuffers() }
            .flatMap { fhirFile ->
                mono {

                    fhirValidator().await().validate(fhirFile).let { (errors, validationReport) ->
                        HtmlWithValidation(
                            html = makeNarrative(fhirFile, true,"lab", getMimeType(fhirFile)).toString(Charsets.UTF_8),
                            errors = errors,
                            validation = validationReport
                        )
                    }
                }
            }

    @PostMapping("vaccination/html/single/validate")
    @Operation(summary = "Convert FHIR file provided as multipart request to html")
    fun htmlAndValidateMultipartSingleVaccination(
            @RequestPart("file") fhirFilePart: Mono<FilePart>
    ) = fhirFilePart.flatMap { it.content().mergeDataBuffers() }
            .flatMap { fhirFile ->
                mono {

                    fhirValidator().await().validate(fhirFile).let { (errors, validationReport) ->
                        HtmlWithValidation(
                                html = makeNarrative(fhirFile, true,"vaccination", getMimeType(fhirFile)).toString(Charsets.UTF_8),
                                errors = errors,
                                validation = validationReport
                        )
                    }
                }
            }


    /*@PostMapping("div/single")
    @Operation(summary = "Convert FHIR file provided as multipart request to embeddable div")
    fun divMultipartSingleLab(@RequestPart("file") fhirFilePart: Mono<FilePart>, response: ServerHttpResponse) =
        fhirFilePart.flatMap { it.content().mergeDataBuffers() }
            .flatMap { fhirFile -> makeResponseLab(fhirFile, response, false) }

    @PostMapping("vaccination/div/single")
    @Operation(summary = "Convert FHIR file provided as multipart request to embeddable div")
    fun divMultipartSingleVaccination(@RequestPart("file") fhirFilePart: Mono<FilePart>, response: ServerHttpResponse) =
            fhirFilePart.flatMap { it.content().mergeDataBuffers() }
                    .flatMap { fhirFile -> makeResponseVaccination(fhirFile, response, false) }
*/

    @PostMapping("lab/div/single/validate")
    @Operation(summary = "Convert FHIR file provided as multipart request to object comprised of validation information and embeddable div")
    fun divAndValidateMultipartSingleLab(@RequestPart("file") fhirFilePart: Mono<FilePart>) =
        fhirFilePart.flatMap { it.content().mergeDataBuffers() }.flatMap { fhirFile ->
            mono {

                fhirValidator().await().validate(fhirFile).let { (errors, validationReport) ->
                    HtmlWithValidation(
                        html = makeNarrative(fhirFile, false, "lab", getMimeType(fhirFile)).toString(Charsets.UTF_8),
                        errors = errors,
                        validation = validationReport
                    )
                }
            }
        }

    @PostMapping("vaccination/div/single/validate")
    @Operation(summary = "Convert FHIR file provided as multipart request to object comprised of validation information and embeddable div")
    fun divAndValidateMultipartSingleVaccination(@RequestPart("file") fhirFilePart: Mono<FilePart>) =
            fhirFilePart.flatMap { it.content().mergeDataBuffers() }.flatMap { fhirFile ->
                mono {

                    fhirValidator().await().validate(fhirFile).let { (errors, validationReport) ->
                        HtmlWithValidation(
                                html = makeNarrative(fhirFile, false, "vaccination", getMimeType(fhirFile)).toString(Charsets.UTF_8),
                                errors = errors,
                                validation = validationReport
                        )
                    }
                }
            }



    private fun makeResponseLab(
        fhirData: ByteArray,
        response: ServerHttpResponse,
        embedInHtml: Boolean,
        fileType: Char
    ): Mono<Void> {
        response.headers.set("Content-Type", "text/html")
        return response.writeWith(Mono.just(response.bufferFactory().wrap(makeNarrative(fhirData, embedInHtml, "lab", fileType))))
    }

    private fun makeResponseVaccination(
            fhirData: ByteArray,
            response: ServerHttpResponse,
            embedInHtml: Boolean,
            fileType: Char
    ): Mono<Void> {
        response.headers.set("Content-Type", "text/html")
        return response.writeWith(Mono.just(response.bufferFactory().wrap(makeNarrative(fhirData, embedInHtml, "vaccination", fileType))))
    }


    private fun makeNarrative(fhirData: ByteArray, embedInHtml: Boolean, resourceType: String, fileType: Char): ByteArray {

        val ctx = FhirContext.forR4()

        val tag = "<".toCharArray()

        var parser = ctx.newJsonParser()
        when (fileType) {
            tag[0] -> parser = ctx.newXmlParser()
        }

        when (resourceType) {
            "lab" -> {
                val unstrippedBundle = parser.parseResource(Bundle::class.java, fhirData.toString(Charsets.UTF_8))
                val bundle = FhirNarrativeUtils.stripNarratives(unstrippedBundle)
                return if (embedInHtml) {
                    resourceHtmlGenerator.generateHtmlRepresentation(ctx, bundle, null)
                } else {
                    resourceHtmlGenerator.generateDivRepresentation(ctx, bundle, null).toByteArray(Charsets.UTF_8)
                }
            }
            "vaccination" -> {
                val unstrippedImmunization = parser.parseResource(Immunization::class.java, fhirData.toString(Charsets.UTF_8))
                val immunization = FhirNarrativeUtils.stripNarratives(unstrippedImmunization)
                return if (embedInHtml) {
                    resourceHtmlGenerator.generateHtmlRepresentation(ctx, immunization, null)
                } else {
                    resourceHtmlGenerator.generateDivRepresentation(ctx, immunization, null).toByteArray(Charsets.UTF_8)
                }
            }
            else -> {
                val byteArray = byteArrayOf(0x48, 101, 108, 108, 111)
                return byteArray
            }
        }

    }


}
