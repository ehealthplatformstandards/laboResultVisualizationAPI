package be.fgov.ehealth.fhir.visualization.doc

object ExampleBodies {
    const val EXAMPLE_REQUEST_JSON = """
    {
      "resourceType": "Immunization",
      "id": "covid-jan-2-discarded",
      "meta": {
        "profile": [
          "https://www.ehealth.fgov.be/standards/fhir/vaccination/StructureDefinition/be-vaccination"
        ]
      },
      "text": {
        "status": "generated",
        "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><p><b>Generated Narrative</b></p><div style=\"display: inline-block; background-color: #d9e0e7; padding: 6px; margin: 4px; border: 1px solid #8da1b4; border-radius: 5px; line-height: 60%\"><p style=\"margin-bottom: 0px\">Resource \"covid-jan-2-discarded\" </p><p style=\"margin-bottom: 0px\">Profile: <a href=\"StructureDefinition-be-vaccination.html\">BeVaccination</a></p></div><p><b>BeExtRecorder</b>: <a href=\"Organization-org-kind-en-gezin.html\">Organization/org-kind-en-gezin</a> \"Kind en Gezin\"</p><blockquote><p><b>BeAdministeredProduct</b></p><p><b>value</b>: B037471</p><p><b>value</b>: <a name=\"pfizer-s0002\"> </a></p><blockquote><div style=\"display: inline-block; background-color: #d9e0e7; padding: 6px; margin: 4px; border: 1px solid #8da1b4; border-radius: 5px; line-height: 60%\"><p style=\"margin-bottom: 0px\">Resource \"pfizer-s0002\" </p></div><p><b>identifier</b>: id: ?ngen-9?</p><p><b>code</b>: 19013168 <span style=\"background: LightGoldenRodYellow; margin: 4px; border: 1px solid khaki\"> (be-ns-cnk-codes#19013168)</span></p><h3>Batches</h3><table class=\"grid\"><tr><td>-</td><td><b>LotNumber</b></td><td><b>ExpirationDate</b></td></tr><tr><td>*</td><td>B037471</td><td>2020-08-31</td></tr></table></blockquote></blockquote><p><b>BeVaccinationConfirmationStatus</b>: confirmed</p><p><b>identifier</b>: id: 134c357c-745b-4a55-43b5-1248340bc711</p><p><b>status</b>: not-done</p><p><b>statusReason</b>: Other <span style=\"background: LightGoldenRodYellow; margin: 4px; border: 1px solid khaki\"> (<a href=\"CodeSystem-be-cs-vaccination-status-reason.html\">BeCSStatusReason</a>#OTHER)</span></p><p><b>vaccineCode</b>: COVID-19 vaccine <span style=\"background: LightGoldenRodYellow; margin: 4px; border: 1px solid khaki\"> (<a href=\"https://browser.ihtsdotools.org/\">SNOMED CT</a>#28531000087107)</span></p><p><b>patient</b>: <span></span></p><p><b>encounter</b>: <a name=\"demo-encounter-2\"> </a></p><blockquote><div style=\"display: inline-block; background-color: #d9e0e7; padding: 6px; margin: 4px; border: 1px solid #8da1b4; border-radius: 5px; line-height: 60%\"><p style=\"margin-bottom: 0px\">Resource \"demo-encounter-2\" </p></div><p><b>status</b>: finished</p><p><b>class</b>: AMB (Details: [not stated] code AMB = 'AMB', stated as 'null')</p><p><b>subject</b>: <span></span></p><h3>Locations</h3><table class=\"grid\"><tr><td>-</td><td><b>Location</b></td></tr><tr><td>*</td><td><a href=\"#demo-location\">#demo-location</a></td></tr></table></blockquote><p><b>occurrence</b>: 2020-03-22</p><p><b>recorded</b>: 2020-03-22</p><p><b>manufacturer</b>: <a href=\"Organization-org-pfizer.html\">Organization/org-pfizer</a> \"Pfizer - Belgium\"</p><p><b>doseQuantity</b>: 1</p><h3>Performers</h3><table class=\"grid\"><tr><td>-</td><td><b>Actor</b></td></tr><tr><td>*</td><td><span>: Huisarts 1</span></td></tr></table></div>"
      },
      "contained": [
        {
          "resourceType": "Medication",
          "id": "pfizer-s0002",
          "identifier": [
            {
              "system": "https://covid-vaccine-tracking.be/serialnumbers#034753633002"
            }
          ],
          "code": {
            "coding": [
              {
                "system": "http://www.ehealth.fgov.be/standards/fhir/medication/NamingSystem/be-ns-cnk-codes",
                "code": "19013168"
              }
            ]
          },
          "batch": {
            "lotNumber": "B037471",
            "expirationDate": "2020-08-31"
          }
        },
        {
          "resourceType": "Encounter",
          "id": "demo-encounter-2",
          "status": "finished",
          "class": {
            "code": "AMB"
          },
          "subject": {
            "identifier": {
              "system": "https://www.ehealth.fgov.be/standards/fhir/core/NamingSystem/ssin",
              "value": "16032376921"
            }
          },
          "location": [
            {
              "location": {
                "reference": "#demo-location"
              }
            }
          ]
        },
        {
          "resourceType": "Location",
          "id": "demo-location",
          "type": [
            {
              "coding": [
                {
                  "system": "https://www.ehealth.fgov.be/standards/fhir/vaccination/CodeSystem/be-cs-care-location",
                  "code": "kind-gezin"
                }
              ]
            }
          ]
        }
      ],
      "extension": [
        {
          "url": "https://www.ehealth.fgov.be/standards/fhir/core/StructureDefinition/be-ext-recorder",
          "valueReference": {
            "reference": "Organization/org-kind-en-gezin"
          }
        },
        {
          "extension": [
            {
              "url": "lotNumber",
              "valueString": "B037471"
            },
            {
              "url": "reference",
              "valueReference": {
                "reference": "#pfizer-s0002"
              }
            }
          ],
          "url": "https://www.ehealth.fgov.be/standards/fhir/vaccination/StructureDefinition/be-ext-administeredProduct"
        },
        {
          "url": "https://www.ehealth.fgov.be/standards/fhir/vaccination/StructureDefinition/be-ext-vaccination-confirmationStatus",
          "valueCode": "confirmed"
        }
      ],
      "identifier": [
        {
          "system": "https://www.ehealth.fgov.be/covid-vaccination/vaccination-register",
          "value": "134c357c-745b-4a55-43b5-1248340bc711"
        }
      ],
      "status": "not-done",
      "statusReason": {
        "coding": [
          {
            "system": "https://www.ehealth.fgov.be/standards/fhir/vaccination/CodeSystem/be-cs-vaccination-status-reason",
            "code": "OTHER"
          }
        ]
      },
      "vaccineCode": {
        "coding": [
          {
            "system": "http://snomed.info/sct",
            "code": "28531000087107"
          }
        ]
      },
      "patient": {
        "identifier": {
          "system": "https://www.ehealth.fgov.be/standards/fhir/core/NamingSystem/ssin",
          "value": "70072376921"
        }
      },
      "encounter": {
        "reference": "#demo-encounter-2"
      },
      "occurrenceDateTime": "2020-03-22",
      "recorded": "2020-03-22",
      "manufacturer": {
        "reference": "Organization/org-pfizer"
      },
      "doseQuantity": {
        "value": 1
      },
      "performer": [
        {
          "actor": {
            "identifier": {
              "use": "official",
              "system": "https://www.ehealth.fgov.be/standards/fhir/core/NamingSystem/nihdi-organization",
              "value": "4605123"
            },
            "display": "Huisarts 1"
          }
        }
      ]
    }
    """

    const val EXAMPLE_VALIDATION_RESPONSE_JSON = """
        {
          "errors": 0
        }
    """

    const val EXAMPLE_HTML_RESPONSE_JSON = """
        <html>...</html>
    """

    const val EXAMPLE_HTML_WITH_VALIDATION_RESPONSE_JSON = """
        {
          "html": "<html>htmlRepresentation</html>",
          "errors": 0,
          "validation": "<html>validationHtml</html>"
        }
    """
}
