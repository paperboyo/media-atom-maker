import java.io.{FilterInputStream, InputStream}
import java.time.Instant
import java.util.UUID

import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicSessionCredentials}
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ObjectMetadata
import com.google.common.io.ByteStreams
import com.gu.media.util.TestFilters
import com.squareup.okhttp.Response
import integration.IntegrationTestBase
import integration.services.Config
import org.scalatest.CancelAfterFailure
import org.scalatest.time.{Minutes, Seconds, Span}
import play.api.Logger
import play.api.libs.json.{JsArray, JsValue, Json}
import services.AWS

class VideoUploadTests extends IntegrationTestBase with CancelAfterFailure {
  val s3 = accountCredentialsS3Client()

  var atomId: String = ""
  var apiEndpoint: String = ""

  var uploadId: String = ""
  var uploadBucket: String = ""
  var uploadParts = List.empty[(String, Long, Long)]
  var completeKey: String = ""

  override def beforeAll(): Unit = {
    atomId = createAtom()
    apiEndpoint = apiUri(atomId)

    eventually {
      gutoolsGet(apiEndpoint).code() should be(200)
    }
  }

  test("Create an upload") {
    val source = s3.getObject(Config.testVideoBucket, Config.testVideo)
    val json = generateUploadRequest(atomId, source.getObjectMetadata.getContentLength)

    val response = gutoolsPost(s"$targetBaseUrl/api2/uploads?atomId=$atomId", jsonBody(json))
    val responseJson = Json.parse(response.body().string())

    response.code() should be(200)

    uploadId = (responseJson \ "id").as[String]
    uploadBucket = (responseJson \ "metadata" \ "bucket").as[String]
    uploadParts = (responseJson \ "parts").as[JsArray].value.map(parseUploadPart).toList
    completeKey = (responseJson \ "metadata" \ "pluto" \ "s3Key").as[String]
  }

  test("Upload parts") {
    val source = s3.getObject(Config.testVideoBucket, Config.testVideo).getObjectContent

    uploadParts.foreach { case(uploadKey, start, end) =>
      Logger.info(s"Getting credentials for $uploadKey")
      val credentials = gutoolsPost(s"$targetBaseUrl/api2/uploads/$uploadId/credentials?key=$uploadKey", emptyBody)
      val temporaryClient = stsCredentialsS3Client(credentials)

      val length = end - start
      val part = slice(source, length)

      val metadata = new ObjectMetadata()
      metadata.setContentLength(length)

      Logger.info(s"Uploading $uploadKey")
      temporaryClient.putObject(uploadBucket, uploadKey, part, metadata)
    }
  }

  test("Add asset to atom") {
    implicit val patienceConfig = PatienceConfig(
      timeout = Span(10, Minutes),
      interval = Span(10, Seconds)
    )

    eventually {
      val assets = (Json.parse(gutoolsGet(apiEndpoint).body().string()) \ "data" \ "assets").as[JsArray].value
      assets should not be empty

      val asset = assets.head

      (asset \ "platform").as[String] should be("Youtube")
      addYouTubeVideoToStore((asset \ "id").as[String])
    }
  }

  test("Create complete video in S3") {
    completeKey should not be empty

    eventually {
      s3.doesObjectExist(uploadBucket, completeKey) should be(true)
      s3.deleteObject(uploadBucket, completeKey)
    }
  }

  private def parseUploadPart(part: JsValue): (String, Long, Long) =
    ((part \ "key").as[String], (part \ "start").as[Long], (part \ "end").as[Long])

  private def slice(input: InputStream, size: Long): InputStream = {
    new FilterInputStream(ByteStreams.limit(input, size)) {
      override def close(): Unit = {
        // nah keep it open :)
      }
    }
  }

  private def accountCredentialsS3Client() = {
    Region.getRegion(Regions.EU_WEST_1).createClient(classOf[AmazonS3Client], AWS.credentialsProvider, null)
  }

  private def stsCredentialsS3Client(credentials: Response) = {
    val json = Json.parse(credentials.body().string())

    val accessId = (json \ "temporaryAccessId").as[String]
    val secret = (json \ "temporarySecretKey").as[String]
    val token = (json \ "sessionToken").as[String]

    val awsCredentials = new BasicSessionCredentials(accessId, secret, token)
    val awsCredentialsProvider = new AWSStaticCredentialsProvider(awsCredentials)

    Region.getRegion(Regions.EU_WEST_1).createClient(classOf[AmazonS3Client], awsCredentialsProvider, null)
  }
}
