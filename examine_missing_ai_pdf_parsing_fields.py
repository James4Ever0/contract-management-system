extractResultFields = ["attachmentId", "contractId", "extractTime", "errorMessage"]

extractDataFields = [
    "contractName",
    "contractNo",
    "contractType",
    "contractAmount",
    "partyA",
    "partyB",
    "signDate",
    "startDate",
    "endDate",
    "servicePeriod",
    "paymentTerms",
    "deliveryTerms",
    "breachClause",
    "otherTerms",
]


sqlTableSchema = [
    "contractId",
    "attachmentId",
    "extractStatus",
    "contractNo",
    "contractName",
    "contractType",
    "customerName",
    "contractAmount",
    "signDate",
    "startDate",
    "endDate",
    "paymentTerms",
    "deliveryTerms",
    "qualityTerms",
    "breachClause",
    "otherTerms",
    "rawData",
    "extractStartTime",
    "extractEndTime",
    "errorMessage",
    "retryCount",
    "pdfContent",
    "extractData",
    "extractTime",
]

javaFields = [
    "extractStatus",
    "extractData",
    "errorMessage",
    "contractId",
    "attachmentId",
    "pdfContent",
]

print("Fields in extractResultFields but not in sqlTableSchema")
for field in extractResultFields:
    if field not in sqlTableSchema:
        print(field)

print("Fields in extractDataFields but not in sqlTableSchema")
for field in extractDataFields:
    if field not in sqlTableSchema:
        print(field)
# partyA
# partyB
# servicePeriod

print("Fields in sqlTableSchema but not in javaFields")
for field in sqlTableSchema:
    if field not in javaFields:
        print(field)

# contractNo
# contractName
# contractType
# customerName
# contractAmount
# signDate
# startDate
# endDate
# paymentTerms
# deliveryTerms
# qualityTerms
# breachClause
# otherTerms
# rawData
# extractStartTime
# extractEndTime
# retryCount
# extractTime
