package be.fgov.ehealth.fhir.visualization.dto

data class HtmlWithValidation(val html: String, val errors: Int, val validation: String)
