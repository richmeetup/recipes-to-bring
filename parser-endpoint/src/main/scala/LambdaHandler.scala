package lambda

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler, LambdaLogger}
import com.amazonaws.services.lambda.runtime.events.CloudFrontEvent.Request

import scala.jdk.CollectionConverters._

class LambdaHandler extends RequestHandler[java.util.Map[String, String], String] {
  override def handleRequest(event: java.util.Map[String, String], context: Context): String = {
    val logger = context.getLogger

    val result = for {
      uri <- getUri(event, Some(logger))
      body <- WebPageFetcher.fetchBody(uri, Some(logger))
      publicUrl <- BucketUploader.upload(body, Some(logger))
    } yield publicUrl

    result match {
      case Left(error) => logger.log(s"Error: $error")
      case Right(_) => logger.log(s"Uploaded to $result")
    }

    s"Hello world! Received event: ${event.toString}"
  }

  def getUri(event: java.util.Map[String, String], logger: Option[LambdaLogger]): Either[Error, String] = {
    logger.foreach(_.log(s"Parsing event for URI: $event"))
    event.asScala.get("uri").toRight(new Error("Missing URI"))
  }
}
