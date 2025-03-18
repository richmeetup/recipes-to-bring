package lambda

import com.amazonaws.services.lambda.runtime.LambdaLogger
import sttp.client3.{SimpleHttpClient, basicRequest}
import sttp.model.Uri._

import org.json4s.jackson.JsonMethods._
import org.json4s.JsonDSL._
import org.json4s._
import lambda.util.HtmlUtils.getContentFromOpenAiResponse
import lambda.util.HtmlUtils.getVisibleHtml
import lambda.util.HtmlUtils

trait RecipeParser {
  def extractSchemaOrgRecipeTypeJson(
      htmlBody: String,
      logger: Option[LambdaLogger]
  ): Either[Error, String]
}

object OpenAIRecipeParser extends RecipeParser {
  val client = SimpleHttpClient()
  val openAiApiKey =
    sys.env("OPENAI_API_KEY") // XXX - throw an error if can't fetch this
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
      case Left(error)         => Left(new Error(error))
      case Right(responseBody) => getContentFromOpenAiResponse(responseBody)
    }
  }

  private def generateOpenAiRequestJson(htmlBody: String): String = {
    // only need top ass along htmlBody that is visible to users
    val visibleHtml = HtmlUtils.getVisibleHtml(htmlBody)

    val agent =
      "You are an assistant that extracts structured recipe data from websites and produces the result in schema.org JSON format.";
    val prompt = s"""
      |Extract the recipe from the following HTML and put it in the recipe schema.org structured data format: https://schema.org/Recipe.
      |Make sure that there is a "recipeIngredient" JSON field. 
      |Return only the JSON and nothing else:
      |
      |${visibleHtml}
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
