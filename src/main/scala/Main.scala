import akka.actor.typed.ActorSystem

object Main extends App {
  val system: ActorSystem[WeatherApp.Command] = ActorSystem(WeatherApp(), "WeatherApp")
  system ! WeatherApp.Start
}