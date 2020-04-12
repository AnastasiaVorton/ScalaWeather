import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import java.util.Locale

object WeatherApp {
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

      case Stop => Behaviors.stopped

      case _ => Behaviors.same
    }
  }

}
