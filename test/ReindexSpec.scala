package test

import data._
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers.any
import play.api.test._
import play.api.inject.bind
import play.api.test.Helpers._
import scala.util.Failure
import play.api.libs.json._

import TestData._

class ReindexSpec
    extends MediaAtomSuite
    with MockitoSugar {

  val testAtoms = Map("1" -> testAtom, "2" -> testAtom)
  def initialDataStore = new MemoryStore(testAtoms)


  def reindexTest(
    reindexer: AtomReindexer = mock[AtomReindexer],
    dataStore: DataStore = initialDataStore
  ) = injectedTest(bind[AtomReindexer] toInstance reindexer) _

  "reindexer" should {
    "return error if publisher fails" in reindexTest() { implicit inj =>
      when(reindexPublisher.reindexAtoms(any())).thenReturn(Failure(new Exception("forced failure")))
      val res = call(reindexController.reindexLive(None, None), FakeRequest())
      status(res) mustEqual INTERNAL_SERVER_ERROR
      contentAsJson(res) mustEqual JsObject("error" -> JsString("forced failure") :: Nil)
    }
  }

}