package lambda

import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.core.sync.RequestBody
import java.util.UUID
import com.amazonaws.services.lambda.runtime.LambdaLogger

object BucketUploader {
    private val s3 = S3Client.create()
    private val bucketName = sys.env.get("PARSED_PAGES_BUCKET").getOrElse("default-bucket")

    def upload(body: String, logger: LambdaLogger): Either[Error, String] = {
        val key = s"processed/${UUID.randomUUID()}.html"

        logger.log(s"Uploading to $key")

        val request = PutObjectRequest.builder()
            .contentType("text/html")
            .bucket(bucketName)
            .key(key)
            .acl("public-read")
            .build()

        val response = s3.putObject(request, RequestBody.fromString(body))
        Right(s"https://$bucketName.s3.amazonaws.com/$key")
    }
}
