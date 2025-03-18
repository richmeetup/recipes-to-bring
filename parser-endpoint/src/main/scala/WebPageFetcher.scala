package lambda

import sttp.client3.{SimpleHttpClient, UriContext, basicRequest}
import com.amazonaws.services.lambda.runtime.LambdaLogger
import java.net.{URL, HttpURLConnection}

object WebPageFetcher {
  val client = SimpleHttpClient()

  def fetchBody(
      url: String,
      logger: Option[LambdaLogger]
  ): Either[Error, String] = {
    logger.foreach(_.log(s"Fetching body from $url"))
    val response = client.send(basicRequest.get(uri"$url"))
    response.body.fold(
      error => Left(new Error(error)),
      body => Right(body)
    )
  }
}
