package lambda

import com.amazonaws.services.lambda.runtime.{
  Context,
  RequestHandler,
  LambdaLogger
}
import com.amazonaws.services.lambda.runtime.events.CloudFrontEvent.Request

import org.json4s.jackson.JsonMethods.{compact, render}
import org.json4s.jackson.JsonMethods
import org.json4s.DefaultFormats
import org.json4s.JsonDSL._

import scala.jdk.CollectionConverters._
import lambda.util.HtmlUtils
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent

case class UserError(message: String) extends Error(message)

class LambdaHandler
    extends RequestHandler[
      APIGatewayProxyRequestEvent,
      APIGatewayProxyResponseEvent
    ] {
  override def handleRequest(
      event: APIGatewayProxyRequestEvent,
      context: Context
  ): APIGatewayProxyResponseEvent = {
    val logger = context.getLogger

    implicit val formats: DefaultFormats.type = DefaultFormats
    val eventBody =
      JsonMethods.parse(event.getBody).extract[Map[String, Any]]

    val result = for {
      uri <- getUri(eventBody, Some(logger))
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
      event: Map[String, Any],
      logger: Option[LambdaLogger]
  ): Either[Error, String] = {
    logger.foreach(_.log(s"Parsing event for URI: $event"))
    event
      .get("uri") match {
      case Some(uri: String) if !uri.isEmpty => Right(uri)
      case _ => Left(new UserError("Missing URI in input"))
    }
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
  ): APIGatewayProxyResponseEvent = {
    val response = new APIGatewayProxyResponseEvent()

    val jsonBody = (
      ("errorType" -> "InternalError") ~
        ("requestId" -> context.getAwsRequestId) ~
        ("message" -> error.getMessage)
    )

    response.setStatusCode(statusCode.intValue())
    response.setBody(compact(render(jsonBody)))
    response
  }

  def getLambdaSuccessResponse(url: String): APIGatewayProxyResponseEvent = {
    val jsonBody = ("url" -> url)

    val response = new APIGatewayProxyResponseEvent()
    response.setStatusCode(200)
    response.setBody(compact(render(jsonBody)))
    response
  }
}
