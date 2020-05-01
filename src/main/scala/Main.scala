import akka.actor.typed.ActorSystem
import com.typesafe.scalalogging.LazyLogging

object Main extends App with LazyLogging {
  logger.info("Weather app started")
  val system: ActorSystem[WeatherApp.Command] = ActorSystem(WeatherApp(), "WeatherApp")
  system ! WeatherApp.Start
}