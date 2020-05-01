import akka.actor.testkit.typed.CapturedLogEvent
import akka.actor.testkit.typed.Effect._
import akka.actor.testkit.typed.scaladsl.BehaviorTestKit
import akka.actor.testkit.typed.scaladsl.TestInbox
import akka.actor.typed._
import akka.actor.typed.scaladsl._
import akka.http.scaladsl.{HttpExt, HttpsConnectionContext}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.util.FastFuture
import akka.stream.ActorMaterializer
import org.mockito.{Mockito, MockitoSugar}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.fixture._
import org.scalatest.flatspec.AnyFlatSpec
import org.slf4j.event.Level
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.settings.ConnectionPoolSettings
import akka.util.ByteString
import org.mockito.ArgumentMatchers._
import org.scalatest.funspec.AnyFunSpec

//class FetcherSpec extends AnyFlatSpec with Matchers {
//  val testPrinter: TestInbox[Printer.Command] = TestInbox[Printer.Command]()
//
//  "Fetcher" should "handle woeid fetch" in {
//    val testKit = BehaviorTestKit(Fetcher(testPrinter.ref))
//    val inbox: TestInbox[String] = TestInbox[String]()
//
//    testKit.run(Fetcher.FetchWoeid("moscow", inbox.ref))
//
//  }
//
//}


