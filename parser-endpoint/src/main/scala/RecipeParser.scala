package lambda

import com.amazonaws.services.lambda.runtime.LambdaLogger
import sttp.client3.{SimpleHttpClient, basicRequest}
import sttp.model.Uri._

import org.json4s.jackson.JsonMethods._
import org.json4s.JsonDSL._
import org.json4s._
import lambda.util.HtmlUtils.getContentFromOpenAiResponse
import lambda.util.HtmlUtils

sealed trait RecipeParser {
  def extractSchemaOrgRecipeTypeJson(
      htmlBody: String,
      logger: Option[LambdaLogger]
  ): Either[Error, String]
}

class OpenAIRecipeParser(val client: SimpleHttpClient, val openAiApiKey: String)
    extends RecipeParser {
  val openAiChatCompletionsUrl = uri"https://api.openai.com/v1/chat/completions"

  def extractSchemaOrgRecipeTypeJson(
      htmlBody: String,
      logger: Option[LambdaLogger]
  ): Either[Error, String] = {
    logger.foreach(_.log(s"Sending html to OpenAI"))

    val response = client
      .send(
        basicRequest
          .header("Content-Type", "application/json")
          .header("Authorization", s"Bearer ${openAiApiKey}")
          .body(generateOpenAiRequestJson(htmlBody))
          .post(openAiChatCompletionsUrl)
      );

    // XXX - could probably do better error handling here
    response.body match {
      case Left(error) => Left(new Error(error))
      case Right(responseBody) => {
        logger.foreach(
          _.log(s"Received recipe JSON from OpenAI: $responseBody")
        )
        getContentFromOpenAiResponse(responseBody)
      }
    }
  }

  private def generateOpenAiRequestJson(htmlBody: String): String = {
    // only need top ass along htmlBody that is visible to users
    val cleanerHtml = HtmlUtils.getCleanerHtml(htmlBody)

    val agent =
      "You are an assistant that extracts structured recipe data from websites and produces the result in schema.org JSON format.";
    val prompt = s"""
      |Extract the recipe from the following HTML and put it in the recipe schema.org structured data format: https://schema.org/Recipe.
      |Make sure that there are "recipeIngredient" and "image" JSON fields.
      |Return only the JSON and nothing else:
      |
      |${cleanerHtml}
      """.stripMargin

    val jsonRequest = ("model" -> "gpt-4o-mini") ~
      ("messages" -> List(
        Map(
          "role" -> "system",
          "content" -> agent
        ),
        Map(
          "role" -> "user",
          "content" -> prompt
        )
      ))

    compact(render(jsonRequest))
  }
}

object OpenAIRecipeParser {
  private lazy val client = SimpleHttpClient()
  private val openAiApiKey =
    sys.env("OPENAI_API_KEY") // XXX - throw an error if can't fetch this

  private lazy val instance = new OpenAIRecipeParser(client, openAiApiKey)

  def getInstance() = instance
}
