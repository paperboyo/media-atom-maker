import integration.IntegrationTestBase
import integration.services.Config
import org.scalatest.CancelAfterFailure
import org.scalatest.exceptions.TestFailedException
import play.api.libs.json.Json

class AtomCreationTests extends IntegrationTestBase with CancelAfterFailure {
  var atomId = ""
  var apiEndpoint = ""

  test(s"$targetBaseUrl is up") {
    val response = gutoolsGet(targetBaseUrl)
    response.code() should be (200)
    response.body().string() should include ("video")
  }

  test("Create a new atom") {
    atomId = createAtom()
    apiEndpoint = apiUri(atomId)

    eventually {
      gutoolsGet(apiEndpoint).code() should be(200)
    }

    Json.parse(gutoolsGet(s"$targetBaseUrl/api2/atoms/$atomId/published").body().string()).toString() should be("{}")
  }


  test("Add an asset to an existing atom") {
    val assetResponse = gutoolsPost(s"$targetBaseUrl/api2/atoms/$atomId/assets", jsonBody(s"""{"uri":"${Config.asset}"}"""))

    assetResponse.code() should be(200)

    eventually {
      val assets = (Json.parse(gutoolsGet(apiEndpoint).body().string()) \ "data" \ "assets") (0)
      (assets \ "id").get.as[String] should be(Config.assetId)
    }
  }

  test("Make an asset current for an existing atom") {
    val currentAssetResponse = gutoolsPut(s"$targetBaseUrl/api2/atom/$atomId/asset-active", Some(jsonBody(s"""{"youtubeId":"${Config.assetId}"}""")))

    currentAssetResponse.code() should be(200)

    eventually {
      (Json.parse(gutoolsGet(apiEndpoint).body().string()) \ "defaultHtml").get.as[String] should not be ("<div></div>")
    }
  }

  test("Publishing an existing atom") {
    val response = gutoolsPut(s"$targetBaseUrl/api2/atom/$atomId/publish")

    response.code() match {
      case 200 => eventually {
        (Json.parse(gutoolsGet(apiEndpoint).body().string()) \ "contentChangeDetails" \ "published" \ "user" \ "email").get.as[String] should be (Config.userEmail)
      }

      case 400 =>
        fail(s"Publishing atom returned 400: ${response.body().string()}")

      case 500 =>
        Option(response.header("X-No-Alerts")) match {
          case Some(_) =>
            cancel(s"Publishing atom returned 500: ${response.body().string()}")

          case None =>
            fail(s"Publishing atom returned 500: ${response.body().string()}")
        }
    }
  }
}
