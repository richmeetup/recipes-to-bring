package lambda

import sttp.client3.{SimpleHttpClient, UriContext, basicRequest}
import com.amazonaws.services.lambda.runtime.LambdaLogger

object WebPageFetcher {
    def fetchBody(url: String, logger: LambdaLogger): Either[Error, String] = {
        logger.log(s"Fetching body from $url")
        val client = SimpleHttpClient()
        val response = client.send(basicRequest.get(uri"$url"))
        response.body.fold(
            error => Left(new Error(error)),
            body => Right(body)
        )
    }
}