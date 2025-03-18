package lambda.util

import org.jsoup.nodes.Document
import org.jsoup.Jsoup

import org.json4s.native.JsonMethods
import org.json4s.DefaultFormats
import org.json4s.JsonAST.JValue

import scala.jdk.CollectionConverters._

object HtmlUtils {
  def insertScriptTagWithSchemaJsonLD(
      htmlBody: String,
      schemaJson: String
  ): String = {
    val doc: Document = Jsoup.parse(htmlBody)

    Option(doc.head()).foreach { head =>
      val script = doc.createElement("script")
      script.attr("type", "application/ld+json")
      script.text(schemaJson)
      head.appendChild(script)
    }

    doc.outerHtml()
  }

  // checks if the html body already contains a schema.org JSON-LD script tag with specific type
  def containsSchemaJsonLD(htmlBody: String, schemaType: String): Boolean = {
    implicit val formats: DefaultFormats.type = DefaultFormats
    val doc: Document = Jsoup.parse(htmlBody)

    doc.select("script[type=application/ld+json]").asScala.foldLeft(false) {
      case (acc, script) =>
        acc || isRecipeSchemaOrgJson(script.data())
    }
  }

  def isRecipeSchemaOrgJson(
      jsonString: String,
      schemaType: String = "Recipe"
  ): Boolean = {
    implicit val formats: DefaultFormats.type = DefaultFormats
    val json = JsonMethods.parse(jsonString)

    (json \ "@type").extractOpt[String].exists(_ == schemaType) ||
    (json \ "@graph").extractOpt[List[JValue]].exists { graphNodes =>
      graphNodes.exists(graphNode =>
        (graphNode \ "@type").extractOpt[String].exists(_ == schemaType)
      )
    }
  }

  def getContentFromOpenAiResponse(response: String): Either[Error, String] = {
    implicit val formats: DefaultFormats.type = DefaultFormats
    val openAiJson = JsonMethods.parse(response)

    val maybeRecipeString =
      ((openAiJson \ "choices")(0) \ "message" \ "content")
        .extract[String]
        .replaceAll("""(?s)^[`'"]{3}json\s*|\s*[`'"]{3}$""", "")

    if (isRecipeSchemaOrgJson(maybeRecipeString)) {
      // woohoo! it's a recipe!
      Right(maybeRecipeString)
    } else {
      Left(
        new Error(
          s"OpenAI response is not a recipe schema.org JSON: $maybeRecipeString"
        )
      )
    }
  }

  def getVisibleHtml(htmlBody: String): String = {
    val doc: Document = Jsoup.parse(htmlBody)
    doc.text()
  }
}
