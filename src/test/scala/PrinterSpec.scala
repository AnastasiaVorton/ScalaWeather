import java.io.{ByteArrayOutputStream, StringReader}

import Printer.Command
import akka.actor.testkit.typed.scaladsl.{BehaviorTestKit, TestInbox}
import data.WeatherData
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class PrinterSpec extends AnyFlatSpec with Matchers {
  val testApp: TestInbox[WeatherApp.Command] = TestInbox[WeatherApp.Command]()

  "Printer" should "correctly handle help command" in {
    val testKit = BehaviorTestKit(Printer(testApp.ref))

    val input = "help".stripMargin
    val in = new StringReader(input)

    Console.withIn(in) {
      testKit.run(Printer.ReadAndProcess)
    }

    testKit.selfInbox().expectMessage(Printer.PrintHelp)
  }

  "Printer" should "correctly handle stop command" in {
    val testKit = BehaviorTestKit(Printer(testApp.ref))

    val input = "stop".stripMargin
    val in = new StringReader(input)

    Console.withIn(in) {
      testKit.run(Printer.ReadAndProcess)
    }

    testApp.expectMessage(WeatherApp.Stop)
  }

  "Printer" should "correctly handle city command" in {
    val testKit = BehaviorTestKit(Printer(testApp.ref))

    val input = "tokyo".stripMargin
    val in = new StringReader(input)

    Console.withIn(in) {
      testKit.run(Printer.ReadAndProcess)
    }

    testKit.selfInbox().expectMessage(Printer. AskForWoeid("tokyo"))
  }

  "Printer" should "print weather data" in {
    val testKit = BehaviorTestKit(Printer(testApp.ref))

    val weatherData = WeatherData("kek", 2.3, 4.20)
    val in = new StringReader("chebuerk".stripMargin)
    val out = new ByteArrayOutputStream()

    Console.withIn(in) {
      testKit.run(Printer.ReadAndProcess)
    }

    Console.withOut(out) {
      testKit.run(Printer.PrintWeather(weatherData))
    }

    out.toString should (include ("kek") and include ("2,3") and include ("4,2"))
  }
}