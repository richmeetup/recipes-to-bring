import org.scalatest._
import lambda.LambdaHandler
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import lambda.UserError

class LambdaHandlerSpec extends AnyFunSpec with Matchers {
  describe("getUri") {
    it("should return the uri from the event") {
      val uri =
        new LambdaHandler()
          .getUri(Map("uri" -> "https://example.com"), None)
      uri shouldBe Right("https://example.com")
    }

    it("should return a user error if the uri is not present") {
      val uri = new LambdaHandler().getUri(Map(), None)
      uri shouldBe a[Left[UserError, _]]
    }

    it("should return a user error if the uri is empty") {
      val uri = new LambdaHandler().getUri(Map("uri" -> ""), None)
      uri shouldBe a[Left[UserError, _]]
    }
  }
}
