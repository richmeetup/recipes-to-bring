package lambda

import com.amazonaws.services.lambda.runtime.{
  Context,
  RequestHandler,
  LambdaLogger
}
import com.amazonaws.services.lambda.runtime.events.CloudFrontEvent.Request

import scala.jdk.CollectionConverters._
import lambda.util.HtmlUtils

class LambdaHandler
    extends RequestHandler[java.util.Map[String, String], String] {
  override def handleRequest(
      event: java.util.Map[String, String],
      context: Context
  ): String = {
    val logger = context.getLogger

    val result = for {
      uri <- getUri(event, Some(logger))
      body <- WebPageFetcher.fetchBody(uri, Some(logger))
      bodyWithRecipe <- getBodyWithRecipe(body, Some(logger))
      bucketUrl <- BucketUploader.upload(bodyWithRecipe, Some(logger))
    } yield bucketUrl

    // maybe return something more useful to the lambda
    result match {
      case Left(error) => {
        logger.log(s"Error: $error")
        ""
      }
      case Right(url) => {
        logger.log(s"Uploaded to $result")
        url
      }
    }
  }

  def getUri(
      event: java.util.Map[String, String],
      logger: Option[LambdaLogger]
  ): Either[Error, String] = {
    logger.foreach(_.log(s"Parsing event for URI: $event"))
    event.asScala.get("uri").toRight(new Error("Missing URI"))
  }

  def getBodyWithRecipe(
      body: String,
      logger: Option[LambdaLogger]
  ): Either[Error, String] = {
    if (HtmlUtils.containsSchemaJsonLD(body, "Recipe")) {
      Right(body)
    } else {
      for {
        recipeJson <- OpenAIRecipeParser.extractSchemaOrgRecipeTypeJson(
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
}
