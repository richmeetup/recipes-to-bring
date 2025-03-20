package lambda

import sttp.client3.{SimpleHttpClient, UriContext, basicRequest}
import com.amazonaws.services.lambda.runtime.LambdaLogger
import java.net.{URL, HttpURLConnection}

// use companion class to make testing methods that use SimpleHttpClient easier
class WebPageFetcher(val client: SimpleHttpClient) {
  def fetchBody(
      url: String,
      logger: Option[LambdaLogger]
  ): Either[Error, String] = {
    logger.foreach(_.log(s"Fetching body from $url"))

    try {
      val response = client.send(basicRequest.get(uri"$url"))

      response.body.fold(
        error => Left(new Error(error)),
        body => Right(body)
      )
    } catch {
      case e: Exception => Left(new Error(e))
    }
  }
}

object WebPageFetcher {
  private lazy val instance = new WebPageFetcher(SimpleHttpClient())

  def getInstance() = instance
}
