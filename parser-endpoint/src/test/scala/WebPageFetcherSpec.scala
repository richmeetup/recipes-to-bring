import org.scalatest._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.ArgumentMatchers._

import sttp.client3.{SimpleHttpClient, UriContext, basicRequest, Response}
import sttp.client3.Response

import lambda.WebPageFetcher
import scala.Error

class WebPageFetcherSpec extends AnyFunSpec with Matchers with MockitoSugar {
  it(
    "should call send on the client with a basicRequest to the url specified"
  ) {
    val mockClient = mock[SimpleHttpClient]

    val fetcher = new WebPageFetcher(mockClient)
    val url = "http://example.com"

    fetcher.fetchBody(url, None)
    // fetcher.simpleTest()
    verify(mockClient, times(1)).send(
      basicRequest.get(uri"http://example.com")
    )
  }

  it(
    "should return the response body when the client returns a successful response"
  ) {
    val mockClient = mock[SimpleHttpClient]

    val fetcher = new WebPageFetcher(mockClient)
    val url = "http://example.com"

    val mockResponse = mock[Response[Nothing]]
    when(mockResponse.body).thenReturn(Right("some body"))

    when(mockClient.send(any())).thenReturn(
      mockResponse
    )

    val result = fetcher.fetchBody(url, None)
    result shouldBe Right("some body")
  }

  it("should return an error when the client throws an exception") {
    val mockClient = mock[SimpleHttpClient]

    val fetcher = new WebPageFetcher(mockClient)
    val url = "http://example.com"

    when(mockClient.send(any())).thenThrow(new RuntimeException("some error"))

    val result = fetcher.fetchBody(url, None)
    result.isLeft shouldBe true
  }

  it("should reutrn an error when the client returns an error") {
    val mockClient = mock[SimpleHttpClient]

    val fetcher = new WebPageFetcher(mockClient)
    val url = "http://example.com"

    val mockResponse = mock[Response[Nothing]]
    when(mockResponse.body).thenReturn(Left("some error"))

    when(mockClient.send(any())).thenReturn(
      mockResponse
    )

    val result = fetcher.fetchBody(url, None)
    result.isLeft shouldBe true
  }

}
