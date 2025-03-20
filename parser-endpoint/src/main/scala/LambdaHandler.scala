package lambda

import com.amazonaws.services.lambda.runtime.{
  Context,
  RequestHandler,
  LambdaLogger
}
import com.amazonaws.services.lambda.runtime.events.CloudFrontEvent.Request

import org.json4s.jackson.JsonMethods.{compact, render}
import org.json4s.JsonDSL._

import scala.jdk.CollectionConverters._
import lambda.util.HtmlUtils

case class UserError(message: String) extends Error(message)

class LambdaHandler
    extends RequestHandler[
      java.util.Map[String, String],
      java.util.Map[String, Any]
    ] {
  override def handleRequest(
      event: java.util.Map[String, String],
      context: Context
  ): java.util.Map[String, Any] = {
    val logger = context.getLogger

    val result = for {
      uri <- getUri(event, Some(logger))
      body <- WebPageFetcher.getInstance.fetchBody(uri, Some(logger))
      bodyWithRecipe <- getBodyWithRecipe(body, Some(logger))
      bucketUrl <- BucketUploader.getInstance.upload(
        bodyWithRecipe,
        Some(logger)
      )
    } yield bucketUrl

    // maybe return something more useful to the lambda
    result match {
      case Left(error) => {
        error match {
          case UserError(_) => {
            logger.log(s"User error: ${error.getMessage}")
            getLambdaErrorResponse(error, context, 400)
          }
          case _ => {
            logger.log(s"Internal error: ${error.getMessage}")
            getLambdaErrorResponse(error, context)
          }
        }
      }
      case Right(url) => {
        logger.log(s"Uploaded: $url")
        getLambdaSuccessResponse(url)
      }
    }
  }

  def getUri(
      event: java.util.Map[String, String],
      logger: Option[LambdaLogger]
  ): Either[Error, String] = {
    logger.foreach(_.log(s"Parsing event for URI: $event"))
    event.asScala
      .get("uri")
      .filter(_.nonEmpty)
      .toRight(new UserError("Missing URI in input"))
  }

  def getBodyWithRecipe(
      body: String,
      logger: Option[LambdaLogger]
  ): Either[Error, String] = {
    if (HtmlUtils.containsSchemaJsonLD(body, "Recipe")) {
      Right(body)
    } else {
      for {
        recipeJson <- OpenAIRecipeParser.getInstance
          .extractSchemaOrgRecipeTypeJson(
            body,
            logger
          )
        bodyWithRecipe = HtmlUtils.insertScriptTagWithSchemaJsonLD(
          body,
          recipeJson
        )
      } yield bodyWithRecipe
    }
  }

  def getLambdaErrorResponse(
      error: Error,
      context: Context,
      statusCode: Number = 500
  ): java.util.Map[String, Any] = {
    val jsonBody = (
      ("errorType" -> "InternalError") ~
        ("statusCode" -> 500) ~
        ("requestId" -> context.getAwsRequestId) ~
        ("message" -> error.getMessage)
    )
    val response: Map[String, Any] =
      Map("body" -> compact(render(jsonBody)), "statusCode" -> statusCode)
    response.asJava
  }

  def getLambdaSuccessResponse(url: String): java.util.Map[String, Any] = {
    val jsonBody = ("url" -> url)
    val response: Map[String, Any] =
      Map("body" -> compact(render(jsonBody)), "statusCode" -> 200)
    response.asJava
  }
}
