package model

import com.gu.media.logging.Logging
import com.gu.media.model.{SelfHostedAsset, VideoSource, YouTubeAsset}
import com.gu.media.upload.model._
import com.gu.media.youtube.{YouTubeAccess, YouTubeProcessingStatus, YouTubeVideos}
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{FunSuite, MustMatchers}

class ClientAssetTest extends FunSuite with MustMatchers {
  val ytAsset = Asset(AssetType.Video, 1, "12345", Platform.Youtube, None)
  val ytProcessing = YouTubeProcessingStatus("1", "processing", 0, 0, 0, None)

  val mp4 = Asset(AssetType.Video, 1, "test.mp4", Platform.Url, Some("video/mp4"))
  val m3u8 = Asset(AssetType.Video, 1, "test.m3u8", Platform.Url, Some("video/m3u8"))

  val parts = List(UploadPart("1", 0, 10), UploadPart("2", 10, 20))

  test("no assets") {
    withoutYouTubeStatus { youTube =>
      ClientAsset(List.empty, youTube) mustBe empty
    }
  }

  test("self hosted asset") {
    withoutYouTubeStatus { youTube =>
      val asset = SelfHostedAsset(List(
        VideoSource(mp4.id, mp4.mimeType.get), VideoSource(m3u8.id, m3u8.mimeType.get)
      ))

      val expected = ClientAsset(mp4.version.toString, Some(asset), processing = None)
      val actual = ClientAsset(List(mp4, m3u8), youTube)

      actual must contain(expected)
    }
  }

  test("YouTube asset") {
    withYouTubeStatus(processing = None) { youTube =>
      val asset = YouTubeAsset(ytAsset.id)

      val expected = ClientAsset(ytAsset.version.toString, Some(asset), processing = None)
      val actual = ClientAsset(List(ytAsset), youTube)

      actual must contain(expected)
    }
  }

  test("YouTube asset (succeeded)") {
    withYouTubeStatus(processing = Some(ytProcessing.copy(status = "succeeded"))) { youTube =>
      val asset = YouTubeAsset(ytAsset.id)

      val expected = ClientAsset(ytAsset.version.toString, Some(asset), processing = None)
      val actual = ClientAsset(List(ytAsset), youTube)

      actual must contain(expected)
    }
  }

  test("YouTube asset (processing)") {
    withYouTubeStatus(processing = Some(ytProcessing)) { youTube =>
      val processing = ClientAssetProcessing("YouTube Processing", failed = false, None, None)

      val expected = ClientAsset(ytAsset.version.toString, asset = None, Some(processing))
      val actual = ClientAsset(List(ytAsset), youTube)

      actual must contain(expected)
    }
  }

  test("Group assets by version (newest first)") {
    withYouTubeStatus(processing = None) { youTube =>
      val input = List(mp4.copy(version = 1), m3u8.copy(version = 1), ytAsset.copy(version = 2))
      val output = ClientAsset.byVersion(input, youTube)

      output match {
        case first :: second :: Nil =>
          first.asset.get mustBe a[YouTubeAsset]
          second.asset.get mustBe a[SelfHostedAsset]

        case _ =>
          fail(s"Expected two entries, got $output")
      }
    }
  }

  test("Self hosted upload") {
    val upload = Upload("test", List.empty, blank(selfHost = true), null)
    val processing = ClientAssetProcessing("Reticulating Splines", failed = false, None, None)

    val expected = ClientAsset("test", asset = None, Some(processing))
    val actual = ClientAsset("Reticulating Splines", upload, error = None)

    actual must be(expected)
  }

  test("Self hosted upload (error)") {
    val upload = Upload("test", List.empty, blank(selfHost = true), null)
    val processing = ClientAssetProcessing("Failed due to electric boogaloo", failed = true, None, None)

    val expected = ClientAsset("test", asset = None, Some(processing))
    val actual = ClientAsset("test", upload, error = Some("Failed due to electric boogaloo"))

    actual must be(expected)
  }

  test("YouTube fully uploaded") {
    val progress = UploadProgress(chunksInS3 = 2, chunksInYouTube = 2, fullyUploaded = true, fullyTranscoded = false, retries = 0)
    val metadata = blank(selfHost = false).copy(asset = Some(YouTubeAsset("test")))
    val upload = Upload("test", parts, metadata, progress)

    val processing = ClientAssetProcessing("Uploading", failed = false, None, None)
    val expected = ClientAsset("test", asset = None, Some(processing))
    val actual = ClientAsset("test", upload, error = None)

    actual must be(expected)
  }

  test("YouTube partially uploaded") {
    val progress = UploadProgress(chunksInS3 = 2, chunksInYouTube = 1, fullyUploaded = false, fullyTranscoded = false, retries = 0)
    val metadata = blank(selfHost = false).copy(asset = Some(YouTubeAsset("test")))
    val upload = Upload("test", parts, metadata, progress)

    val processing = ClientAssetProcessing("Uploading to YouTube", failed = false, current = Some(1), total = Some(2))
    val expected = ClientAsset("test", asset = None, Some(processing))
    val actual = ClientAsset("test", upload, error = None)

    actual must be(expected)
  }

  test("YouTube upload (error)") {
    val metadata = blank(selfHost = false).copy(asset = Some(YouTubeAsset("test")))
    val upload = Upload("test", parts, metadata, null)

    val processing = ClientAssetProcessing("It Didn't Work!", failed = true, current = None, total = None)
    val expected = ClientAsset("test", asset = None, Some(processing))
    val actual = ClientAsset("test", upload, error = Some("It Didn't Work!"))

    actual must be(expected)
  }

  test("YouTube processing (indeterminate)") {
    val status = YouTubeProcessingStatus("", "processing", 0, 0, 0, None)
    val expected = ClientAssetProcessing("YouTube Processing", failed = false, None, None)
    val actual = ClientAssetProcessing(status)

    actual must be(expected)
  }

  test("YouTube processing (time left)") {
    val status = YouTubeProcessingStatus("", "processing", 10, 5, 10000, None)
    val expected = ClientAssetProcessing("YouTube Processing (10s left)", failed = false, Some(5), Some(10))
    val actual = ClientAssetProcessing(status)

    actual must be(expected)
  }

  test("YouTube processing (error)") {
    val status = YouTubeProcessingStatus("", "failed", 0, 0, 0, Some("burp"))
    val expected = ClientAssetProcessing("burp", failed = true, None, None)
    val actual = ClientAssetProcessing(status)

    actual must be(expected)
  }

  test("YouTube processing (something else)") {
    val status = YouTubeProcessingStatus("", "reticulating", 0, 0, 0, None)
    val expected = ClientAssetProcessing("reticulating", failed = false, None, None)
    val actual = ClientAssetProcessing(status)

    actual must be(expected)
  }

  private def blank(selfHost: Boolean): UploadMetadata = {
    UploadMetadata("", "", "", "", null, selfHost, null, None)
  }

  private def withoutYouTubeStatus(fn: (String => Option[YouTubeProcessingStatus]) => Unit): Unit = {
    fn((_: String) => {
      fail("Unexpected call to YouTube status")
    })
  }

  private def withYouTubeStatus(processing: Option[YouTubeProcessingStatus])(fn: (String => Option[YouTubeProcessingStatus]) => Unit): Unit = {
    fn((_: String) => {
      processing
    })
  }
}
