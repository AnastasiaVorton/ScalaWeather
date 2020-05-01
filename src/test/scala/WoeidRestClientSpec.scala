import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.{HttpExt, HttpsConnectionContext}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.settings.ConnectionPoolSettings
import akka.http.scaladsl.util.FastFuture
import akka.stream.ActorMaterializer
import akka.util.ByteString
import org.mockito.ArgumentMatchers.any
import org.mockito.{Mockito, MockitoSugar}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.funspec.AnyFunSpec

import scala.concurrent.ExecutionContextExecutor

class WoeidRestClientSpec extends AnyFunSpec with BeforeAndAfterAll with ScalaFutures with MockitoSugar {
  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val mat: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = actorSystem.dispatcher

  override def afterAll(): Unit = actorSystem.terminate()

  val mockHttp: HttpExt = mock[HttpExt]
  val logger: LoggingAdapter = mock[LoggingAdapter]

  class TestWoeidTestClient extends WeatherDataRestClient {
    override def http: HttpExt = mockHttp
  }

  describe("fetchWoeidDataByCity") {

    it("test case description") {
      val json = """[
        {
          "title": "Moscow",
          "location_type": "City",
          "woeid": 2122265,
          "latt_long": "55.756950,37.614971"
        }
      ]"""
      val response = FastFuture.successful {
        HttpResponse(
          status = StatusCodes.OK,
          entity = HttpEntity(ContentTypes.`application/json`, ByteString(json)))
      }

      // я пыталась, но это не робит(
      Mockito.when(mockHttp.singleRequest(any[HttpRequest], any[HttpsConnectionContext], any[ConnectionPoolSettings], log =logger))
        .thenReturn(response)

      val client = new TestWoeidTestClient
      val handledResponse = client.fetchWoeidDataByCity("moscow").futureValue
      assert(handledResponse === Some("""[
        {
          "title": "Moscow",
          "location_type": "City",
          "woeid": 2122265,
          "latt_long": "55.756950,37.614971"
        }
      ]"""))
    }
  }
}
