package dev.nixgeek.bliki.lib.test.fixtures.json

const val JSON_RESOURCE_FILE = "/json/test.json"
const val TEST_JSON_RESOURCE_FILE = "/test.json"

const val TEST_JSON_ADDRESS_EXPECTED =
    "{\"id\":1,\"street1\":\"100 West Liberty St\",\"city\":\"Reno\"," +
        "\"state\":\"NV\",\"zip\":\"89501\",\"street2\":\"suite 600\",\"type\":\"HOME\"}"
const val TEST_JSON_ADDRESS_EXPECTED_PRETTY = """{
  "id" : 1,
  "street1" : "100 West Liberty St",
  "city" : "Reno",
  "state" : "NV",
  "zip" : "89501",
  "street2" : "suite 600",
  "type" : "HOME"
}"""

const val TEST_JSON_EMPTY = ""
const val TEST_JSON_EXPECTED = "Jane Doe"
const val TEST_JSON_PATH = "name"
const val TEST_JSON_SPACE = " "

const val TEST_JSON_RANDOM_001 = "{\"key\":12,\"value\":\"abcdefghijklmnopqrstuvwxyz\"}"
const val TEST_JSON_RANDOM_002 = "{\"key\":123,\"value\":\"ABCDEFGHIJKLMNOPQRSTUVWXYZ\"}"
const val TEST_JSON_RANDOM_003 = "a:b"

const val TEST_JSON_USER_001 =
    "{\"name\":\"Jane Doe\",\"email\":\"janinedoe@gmail.com\",\"id\":2,\"active\":true," +
        "\"hire_date\":\"2019-01-01\"}"
const val TEST_JSON_USER_002 =
    "{\"name\":\"Janet Doe\",\"email\":\"janetdoe@yahoo.com\",\"id\":5,\"active\":true," +
        "\"hire_date\":\"2018-01-01\"}"

const val TEST_JSON_WARN_BYTES = "bytes:"
const val TEST_JSON_WARN_DATA = "testData:"
const val TEST_JSON_WARN_NODE = "jsonNode:"
const val TEST_JSON_WARN_STRING = "jsonString:"
