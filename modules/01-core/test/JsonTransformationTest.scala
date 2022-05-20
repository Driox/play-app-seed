package core

import play.api.libs.json._
import utils.json.JsonTransformation

import org.scalatest.PrivateMethodTester
import org.scalatestplus.play._

class JsonTransformationTest extends PlaySpec with PrivateMethodTester {

  "JsonTransformation" should {

    "dot mapping should be idempotent" in {
      val expected_result = Json.parse(
        """{
           "user":{
             "first_name": "Jean",
             "age": "12"
           },
           "tools": [12, 14, 98, 59]
          }"""
      ).as[JsObject]

      val data = Json.parse(
        """{
          "user.first_name": "Jean",
          "user.age": "12",
          "tools[0]": 12,
          "tools[1]": 14,
          "tools[2]": 98,
          "tools[3]": 59
          }"""
      ).as[JsObject]

      val transform = JsonTransformation.jsObject_mapping _ andThen JsonTransformation.jsArray_mapping _
      val data1     = transform(data)
      data1 mustBe expected_result

      val data2 = transform(data1)
      data1 mustBe data2
    }

    "dot mapping should be reversible" in {
      val data = Json.parse(
        """{
          "user.first_name": "Jean",
          "user.age": "12",
          "tools[0]": 12,
          "tools[1]": 14,
          "tools[2]": 98,
          "tools[3]": 59
          }"""
      ).as[JsObject]

      val transform = JsonTransformation.jsObject_mapping _ andThen JsonTransformation.jsObject_flatten _
      val data1     = transform(data)
      data1 mustBe data

      val data_reversed = Json.parse(
        """{
           "user":{
             "first_name": "Jean",
             "age": "12"
           },
           "tools": [12, 14, 98, 59]
          }"""
      ).as[JsObject]

      val transform2 = JsonTransformation.jsObject_flatten _ andThen JsonTransformation.jsObject_mapping
      val data2      = transform2(data_reversed)
      data2 mustBe data_reversed
    }

    "object mapping should be recursive" in {
      val expected_result = Json.parse("""{
        "user" : {
          "address" : {
            "city": {
              "name": "Paris",
              "lat": "43.12",
              "long": "98.34"
            },
            "zip": "75012",
            "number": "3"
          },
          "age" : "12",
          "first_name" : "Jean"
        }
      }""").as[JsObject]

      val data = Json.parse("""{
        "user.first_name": "Jean",
        "user.age": "12",
        "user.address.city.name": "Paris",
        "user.address.city.lat": "43.12",
        "user.address.city.long": "98.34",
        "user.address.zip": "75012",
        "user.address.number": "3"
      }""").as[JsObject]

      val data1 = JsonTransformation.jsObject_mapping(data)
      data1 mustBe expected_result
    }

    "array mapping should be recursive" in {
      val expected_result = Json.parse("""{
        "own_a_house" : "true",
        "zop" : [ "Blue", "Red" ],
        "job" : "Teacher",
        "user" : {
          "address" : [ {
            "city" : "Paris",
            "colors" : [ "Blue", "Red" ]
          }, {
            "city" : "London"
          }, {
            "city" : "Miami"
          } ],
          "last_name" : "Dupont",
          "first_name" : "Jean"
        },
        "colors" : [ "Blue", "Red" ],
        "age" : "45"
      }""").as[JsObject]

      val data = Json.parse("""{
        "own_a_house" : "true",
        "colors[1]" : "Red",
        "colors[0]" : "Blue",
        "zop[1]" : "Red",
        "zop[0]" : "Blue",
        "job" : "Teacher",
        "user" : {
          "last_name" : "Dupont",
          "address[0]" : {
            "city" : "Paris",
            "colors[1]" : "Red",
            "colors[0]" : "Blue"
          },
          "address[2]" : {
            "city" : "Miami"
          },
          "first_name" : "Jean",
          "address[1]" : {
            "city" : "London"
          }
        },
        "age" : "45"
      }""").as[JsObject]

      val data1 = JsonTransformation.jsArray_mapping(data)
      data1 mustBe expected_result
    }

    "array mapping should be reversible" in {
      val data = Json.parse("""{
        "own_a_house" : "true",
        "zop" : [ "Blue", "Red" ],
        "job" : "Teacher",
        "user" : {
          "address" : [ {
            "city" : "Paris",
            "colors" : [ "Blue", "Red" ]
          }, {
            "city" : "London"
          }, {
            "city" : "Miami"
          } ],
          "last_name" : "Dupont",
          "first_name" : "Jean"
        },
        "colors" : [ "Blue", "Red" ],
        "age" : "45"
      }""").as[JsObject]

      val expected_result = Json.parse("""{
        "own_a_house" : "true",
        "colors[1]" : "Red",
        "colors[0]" : "Blue",
        "zop[1]" : "Red",
        "zop[0]" : "Blue",
        "job" : "Teacher",
        "user" : {
          "last_name" : "Dupont",
          "address[0]" : {
            "city" : "Paris",
            "colors[1]" : "Red",
            "colors[0]" : "Blue"
          },
          "address[2]" : {
            "city" : "Miami"
          },
          "first_name" : "Jean",
          "address[1]" : {
            "city" : "London"
          }
        },
        "age" : "45"
      }""").as[JsObject]

      val data1 = JsonTransformation.jsArray_flatten(data)
      data1 mustBe expected_result
    }

    "combined mapping should be flat" in {
      val data = Json.parse("""{
        "own_a_house" : "true",
        "zop" : [ "Blue", "Red" ],
        "job" : "Teacher",
        "user" : {
          "address" : [ {
            "city" : "Paris",
            "colors" : [ "Blue", "Red" ]
          }, {
            "city" : "London"
          }, {
            "city" : "Miami"
          } ],
          "last_name" : "Dupont",
          "first_name" : "Jean"
        },
        "colors" : [ "Blue", "Red" ],
        "age" : "45"
      }""").as[JsObject]

      val expected_result = Json.parse("""{
        "colors[0]" : "Blue",
        "colors[1]" : "Red",
        "job" : "Teacher",
        "age" : "45",
        "own_a_house" : "true",
        "zop[0]" : "Blue",
        "zop[1]" : "Red",
        "user.address[0].city" : "Paris",
        "user.address[0].colors[0]" : "Blue",
        "user.address[0].colors[1]" : "Red",
        "user.address[1].city" : "London",
        "user.address[2].city" : "Miami",
        "user.first_name" : "Jean",
        "user.last_name" : "Dupont"
      }""").as[JsObject]

      val data1 = (JsonTransformation.jsArray_flatten _ andThen JsonTransformation.jsObject_flatten _)(data)
      data1 mustBe expected_result
    }
  }
}
