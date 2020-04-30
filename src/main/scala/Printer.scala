import java.util.Locale

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.util.Timeout
import com.osinka.i18n.{Lang, Messages}

import scala.util.{Failure, Success}
import scala.concurrent.duration._

object Printer {
  sealed trait Command
  case object Greet extends Command
  case object ReadAndProcess extends Command
  case object PrintHelp extends Command
  case object Continue extends Command

  case class AskForWoeid(city: String) extends Command
  case class AskForWeather(woeid: String) extends Command
  case class PrintWeather(weatherData: WeatherData) extends Command
  case class ReceiveFetcher(fetcher: ActorRef[Fetcher.Command]) extends Command

  // language settings
  val lang: String = Locale.getDefault.getLanguage
  implicit val userLang: Lang = Lang(lang)
  implicit val responceTimeout: Timeout = 10.second

  var fetcher: Option[ActorRef[Fetcher.Command]] = None
  var city: Option[String] = None

  def apply(app: ActorRef[WeatherApp.Command]): Behavior[Command] =
    Behaviors.receive { (ctx, msg) =>
      msg match {
        // greet the user and start reading commands
        case Greet =>
          println(Messages("greeting"))
          ctx.self ! ReadAndProcess
          Behaviors.same

        case ReceiveFetcher(f) =>
          fetcher = Option(f)
          Behaviors.same

        // read and process users input
        case ReadAndProcess =>
          println(Messages("type.command"))
          val city = scala.io.StdIn.readLine()

          city match {
            case "help" => ctx.self ! PrintHelp
            case "stop" =>
              app ! WeatherApp.Stop
              Behaviors.stopped
            case _ =>
              this.city = Option(city)
              ctx.self ! AskForWoeid(city)
          }

          Behaviors.same

        // print help and continue reading users commands
        case PrintHelp =>
          println(Messages("help"))
          ctx.self ! ReadAndProcess
          Behaviors.same

        // fetch cities woeid
        case AskForWoeid(city) =>
          ctx.ask(fetcher.get, (ref: ActorRef[String]) => Fetcher.FetchWoeid(city, ref)) {
            case Success(woeid) =>
              ctx.self ! AskForWeather(woeid)
              Continue
            case Failure(_) =>
              println(Messages("error"))
              ctx.self ! ReadAndProcess
              Continue
            case _ => Continue
          }
          Behaviors.same

        // fetch weather data by woeid
        case AskForWeather(woeid) =>
          ctx.ask(fetcher.get, (ref: ActorRef[WeatherData]) => Fetcher.FetchWeather(woeid, ref)) {
            case Success(weatherParams) =>
              ctx.self ! PrintWeather(weatherParams)
              Continue
            case Failure(_) =>
              println(Messages("error"))
              ctx.self ! ReadAndProcess
              Continue
            case _ => Continue
          }
          Behaviors.same

        // print weather data
        case PrintWeather(weatherData) =>
          println(Messages("weather.summary", city.get, weatherData.weatherState.toLowerCase()))
          println(Messages("weather.temperature", f"${weatherData.lowestTemp}%1.1f", f"${weatherData.highestTemp}%1.1f"))
          ctx.self ! ReadAndProcess
          Behaviors.same

        case _ => Behaviors.same
      }
    }
}
