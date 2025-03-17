package lambda

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler, LambdaLogger}
import com.amazonaws.services.lambda.runtime.events.CloudFrontEvent.Request

class LambdaHandler extends RequestHandler[java.util.Map[String, String], String] {
  override def handleRequest(event: java.util.Map[String, String], context: Context): String = {
    val logger = context.getLogger

    logger.log("Hello log!")
    s"Hello world! Received event: ${event.toString}"
  }
}
