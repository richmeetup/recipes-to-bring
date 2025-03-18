package utils

import org.scalatest._
import lambda.util.HtmlUtils
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class HtmlUtilsSpec extends AnyFunSpec with Matchers {
  val schemaJsonRecipe =
    "{\"@context\":\"http://schema.org\",\"@type\":\"Recipe\"}";
  val schemaJsonSomething =
    "{\"@context\":\"http://schema.org\",\"@type\":\"Something\"}";

  describe("insertScriptTag") {
    it("should insert a script tag into the html head tag if it exists") {
      val htmlBody = "<html><head></head><body></body></html>"

      val result =
        HtmlUtils.insertScriptTagWithSchemaJsonLD(htmlBody, schemaJsonRecipe)

      result should include(
        s"<script type=\"application/ld+json\">${schemaJsonRecipe}</script>"
      )
    }

    it(
      "should insert a script tag into the html and add a head tag if it doesn't exist"
    ) {
      val htmlBody = "<html><body></body></html>"

      val result =
        HtmlUtils.insertScriptTagWithSchemaJsonLD(htmlBody, schemaJsonRecipe)

      result should include("<head>")
      result should include(
        s"<script type=\"application/ld+json\">${schemaJsonRecipe}</script>"
      )
    }
  }

  describe("containsSchemaJsonLD") {
    it(
      "should return true if the html body contains a schema.org JSON-LD script tag with specific type"
    ) {
      val htmlBody = s"""
|<html>
|    <head>
|        <script type=\"application/ld+json\">${schemaJsonRecipe}</script>
|    </head>
|    <body></body>
|</html>
        """.stripMargin

      val result = HtmlUtils.containsSchemaJsonLD(htmlBody, "Recipe")
      result shouldBe true
    }

    it(
      "should return false if the JSON-LD doesn't contain the specific type"
    ) {
      val htmlBody = s"""
|<html>
|    <head>
|        <script type=\"application/ld+json\">${schemaJsonSomething}</script>
|    </head>
|    <body></body>
|</html>
        """.stripMargin

      val result = HtmlUtils.containsSchemaJsonLD(htmlBody, "Recipe")
      result shouldBe false
    }

    it(
      "should return true if the html body contains a graph JSON-LD script tag with a recipe"
    ) {
      val htmlBody = """
|<html>
|    <head>
|        <script type="application/ld+json">{
  "@context": "https://schema.org",
  "@graph": [
    {
      "@type": "Recipe",
      "name": "Chocolate Cake",
    },
    {
      "@type": "Person",
      "name": "Chef John"
    },
    {
      "@type": "WebPage",
      "name": "Best Recipes",
      "url": "https://example.com/recipe"
    }
  ]
}</script>
|    </head>
|    <body></body>
|</html>
        """.stripMargin

      val result = HtmlUtils.containsSchemaJsonLD(htmlBody, "Recipe")
      result shouldBe true
    }

    it(
      "should return false if the html body contains a graph JSON-LD script tag without a recipe"
    ) {
      val htmlBody = """
|<html>
|    <head>
|        <script type="application/ld+json">{
  "@context": "https://schema.org",
  "@graph": [
    {
      "@type": "NotARecipe",
      "name": "Chocolate Cake",
    },
    {
      "@type": "Person",
      "name": "Chef John"
    },
    {
      "@type": "WebPage",
      "name": "Best Recipes",
      "url": "https://example.com/recipe"
    }
  ]
}</script>
|    </head>
|    <body></body>
|</html>
        """.stripMargin

      val result = HtmlUtils.containsSchemaJsonLD(htmlBody, "Recipe")
      result shouldBe false
    }

    it(
      "should return false if the html body doesn't contain any JSON-LD script tag"
    ) {
      val htmlBody = s"""
|<html>
|    <head>
|        <script type=\"application/json\"></script>
|    </head>
|    <body></body>
|</html>
        """.stripMargin

      val result = HtmlUtils.containsSchemaJsonLD(htmlBody, "Recipe")
      result shouldBe false
    }
  }

  describe("getContentFromOpenAiResponse") {
    it("should extract the content from the OpenAI response") {
      val response =
        "{\"choices\":[{\"message\":{\"content\":\"```json\\n{\\n  \\\"@context\\\": \\\"http://schema.org\\\",\\n  \\\"@type\\\": \\\"Recipe\\\"\\n}\\n```\"}}]}"

      val result = HtmlUtils.getContentFromOpenAiResponse(response)
      result shouldBe Right("""{
|  "@context": "http://schema.org",
|  "@type": "Recipe"
|}""".stripMargin)
    }
  }
}
