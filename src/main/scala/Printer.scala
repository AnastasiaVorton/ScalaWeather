import java.util.Locale

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import com.osinka.i18n.{Lang, Messages}

object Printer {
  sealed trait Command
  case object Greet extends Command
  case object ReadAndProcess extends Command
  case class AskForWoeid(city: String) extends Command
  case class AskForWeather(wieid: String) extends Command
  case class PrintWeather(state: String, temperatures: Array[Int]) extends Command
  case object PrintHelp extends Command

  // language settings
  val lang: String = Locale.getDefault.getLanguage
  implicit val userLang: Lang = Lang(lang)

  def apply(app: ActorRef[WeatherApp.Command]): Behavior[Command] =
    Behaviors.receive { (ctx, msg) =>
      msg match {
        // greet the user and start reading commands
        case Greet =>
          println(Messages("greeting"))
          ctx.self ! ReadAndProcess
          Behaviors.same

        // read and process users input
        case ReadAndProcess =>
          val city = scala.io.StdIn.readLine()

          city match {
            case "help" => ctx.self ! PrintHelp
            case "stop" =>
              app ! WeatherApp.Stop
              Behaviors.stopped
            case _ => ctx.self ! AskForWoeid(city)
          }

          Behaviors.same

        // print help and continue reading users commands
        case PrintHelp =>
          println(Messages("help"))
          ctx.self ! ReadAndProcess
          Behaviors.same

        // fetch cities woeid
        case AskForWoeid(city) =>
          Behaviors.same

        case _ => Behaviors.same
      }
    }
}
