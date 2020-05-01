import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import java.util.Locale

import com.typesafe.scalalogging.LazyLogging

object WeatherApp extends LazyLogging{
  sealed trait Command
  case object Start extends Command
  case object Stop extends Command

  val lang: String = Locale.getDefault.getLanguage

  def apply(): Behavior[Command] = Behaviors.receive { (ctx, msg) =>
    msg match {
      case Start =>

        val printer = ctx.spawn(Printer(ctx.self), "Printer")
        val fetcher = ctx.spawn(Fetcher(printer), "Fetcher")

        printer ! Printer.ReceiveFetcher(fetcher)
        printer ! Printer.Greet

        Behaviors.same

      case Stop =>
        logger.info(s"Stoping Weather app ${ctx.self}")
        Behaviors.stopped

      case _ => Behaviors.same
    }
  }

}
