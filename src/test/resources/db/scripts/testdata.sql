INSERT INTO flow (id, version, name, description, content)
VALUES ('tjansteskrivelse', 1, 'Tjänsteskrivelse', 'Ett Intric AI-flöde för tjänsteskrivelser', '{
  "id" : "tjansteskrivelse",
  "version" : 1,
  "name" : "Tjänsteskrivelse",
  "description" : "Ett Intric AI-flöde för tjänsteskrivelser",
  "helptext" : "",
  "spaceId" : "",
  "visible" : true,
  "defaultTemplateId" : "ai-mvp.tjansteskrivelse",
  "input" : [ {
    "id" : "arendenummer",
    "name" : "Ärendenummer",
    "type" : "STRING",
    "cardinality" : "SINGLE_VALUED",
    "passthrough" : true
  }, {
    "id" : "uppdraget-till-tjansten",
    "name" : "Uppdrag",
    "type" : "TEXT",
    "cardinality" : "SINGLE_VALUED",
    "passthrough" : false
  }, {
    "id" : "forvaltningens-input",
    "name" : "Förvaltningens input",
    "type" : "TEXT",
    "cardinality" : "SINGLE_VALUED",
    "passthrough" : false
  }, {
    "id" : "bakgrundsmaterial",
    "name" : "Bakgrundsmaterial",
    "type" : "DOCUMENT",
    "cardinality" : "MULTIPLE_VALUED",
    "passthrough" : false
  }, {
    "id" : "relaterade-styrdokument",
    "name" : "Relaterade styrdokument",
    "type" : "DOCUMENT",
    "cardinality" : "MULTIPLE_VALUED",
    "passthrough" : false
  } ],
  "steps" : [ {
    "id" : "arendet",
    "order" : 1,
    "name" : "Ärendet",
    "target" : { "type" : "SERVICE", "id" : "9dda859f-f7cf-4961-9616-cdcb1c8b3d85" },
    "input" : [ { "use-flow-input" : "uppdraget-till-tjansten" }, { "use-flow-input" : "forvaltningens-input" }, { "use-flow-input" : "bakgrundsmaterial" } ]
  }, {
    "id" : "bakgrund",
    "order" : 2,
    "name" : "Bakgrund",
    "target" : { "type" : "SERVICE", "id" : "127dd187-b010-42db-a0b4-f413de22963f" },
    "input" : [ { "use-flow-input" : "uppdraget-till-tjansten" }, { "use-flow-input" : "forvaltningens-input" }, { "use-flow-input" : "bakgrundsmaterial" } ]
  }, {
    "id" : "forvaltningens-overvaganden",
    "order" : 3,
    "name" : "Förvaltningens överväganden",
    "target" : { "type" : "SERVICE", "id" : "714e598a-7a73-4870-81e5-1b8c9e3897a3" },
    "input" : [ { "use-flow-input" : "uppdraget-till-tjansten" }, { "use-flow-input" : "forvaltningens-input" }, { "use-flow-input" : "bakgrundsmaterial" }, { "use-output-from-step" : "bakgrund", "use-as" : "Bakgrund" } ]
  }, {
    "id" : "styrdokument-och-juridik",
    "order" : 4,
    "name" : "Styrdokument och juridik",
    "target" : { "type" : "SERVICE", "id" : "7b0aaa43-74e9-4b46-9546-eb29f6ee8419" },
    "input" : [ { "use-flow-input" : "uppdraget-till-tjansten" }, { "use-flow-input" : "forvaltningens-input" }, { "use-flow-input" : "bakgrundsmaterial" }, { "use-flow-input" : "relaterade-styrdokument" } ]
  }, {
    "id" : "ekonomisk-hallbarhet",
    "order" : 5,
    "name" : "Ekonomisk hållbarhet",
    "target" : { "type" : "SERVICE", "id" : "4acb5405-3526-4c31-b7a7-c50d444c57f2" },
    "input" : [ { "use-flow-input" : "uppdraget-till-tjansten" }, { "use-flow-input" : "forvaltningens-input" }, { "use-flow-input" : "bakgrundsmaterial" } ]
  }, {
    "id" : "ekologisk-hallbarhet",
    "order" : 6,
    "name" : "Ekologisk hållbarhet",
    "target" : { "type" : "SERVICE", "id" : "f646c831-8424-494f-a833-68d64f452c22" },
    "input" : [ { "use-flow-input" : "uppdraget-till-tjansten" }, { "use-flow-input" : "forvaltningens-input" }, { "use-flow-input" : "bakgrundsmaterial" } ]
  }, {
    "id" : "social-hallbarhet",
    "order" : 7,
    "name" : "Social hållbarhet",
    "target" : { "type" : "SERVICE", "id" : "a9c350c9-0e63-4e57-bc6a-7cf6a2a5f8c7" },
    "input" : [ { "use-flow-input" : "uppdraget-till-tjansten" }, { "use-flow-input" : "forvaltningens-input" }, { "use-flow-input" : "bakgrundsmaterial" } ]
  }, {
    "id" : "landsbygdssakring",
    "order" : 8,
    "name" : "Landsbygdssäkring",
    "intricServiceId" : "12b64b34-dec5-44d9-a758-beb55af81c56",
    "input" : [ { "use-flow-input" : "uppdraget-till-tjansten"}, {  "use-flow-input" : "forvaltningens-input"}, {"use-flow-input" : "bakgrundsmaterial" } ]
  } ]
}');
