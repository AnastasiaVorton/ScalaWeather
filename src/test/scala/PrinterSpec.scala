import java.io.{ByteArrayInputStream, StringReader}

import Printer.Command
import akka.actor.testkit.typed.scaladsl.{BehaviorTestKit, TestInbox}
import akka.actor.typed.Behavior
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class PrinterSpec extends AnyFlatSpec with Matchers {
  val testApp: TestInbox[WeatherApp.Command] = TestInbox[WeatherApp.Command]()
  val testKit = BehaviorTestKit(Printer(testApp.ref))
  val inbox: TestInbox[Command] = TestInbox[Command]()

  "Printer" should "correctly handle help command" in {
    val input = "help".stripMargin
    val in = new StringReader(input)

    Console.withIn(in) {
      testKit.run(Printer.ReadAndProcess)
    }

    inbox.expectMessage(Printer.PrintHelp)
  }
}