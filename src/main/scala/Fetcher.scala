import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import play.api.libs.json.{JsValue, Json}
import scalaj.http.{Http, HttpResponse}

object Fetcher {
  sealed trait Command
  case class FetchWoeid(city: String, replyTo: ActorRef[String]) extends Command
  case class FetchWeather(woeid: String, replyTp: ActorRef[Array[String]]) extends Command

  def apply(printer: ActorRef[Printer.Command]): Behavior[Command] =
    Behaviors.receive { (ctx, msg) =>
      msg match {
        case FetchWoeid(city, replyTo) =>
          val locationResponse: HttpResponse[String] = Http("https://www.metaweather.com/api/location/search/")
            .param("query", city)
            .asString

          val locationJson: JsValue = Json.parse(locationResponse.body.toString().replaceAll("(\\[|\\])", ""))

          val woeid = (locationJson \ "woeid").get.toString().replaceAll("\"", "")

          replyTo ! woeid

          Behaviors.same

        case FetchWeather(woeid, replyTo) =>
          val weatherResponse: HttpResponse[String] = Http(s"https://www.metaweather.com/api/location/$woeid/")
            .asString

          val weatherJson: JsValue = Json.parse(weatherResponse.body.toString())

          val weatherToday = (weatherJson \ "consolidated_weather").get(0)

          val weatherState = (weatherToday \ "weather_state_name").get.toString().replaceAll("\"", "").toLowerCase
          val lowestTemp = (weatherToday \ "min_temp").get.toString()
          val highestTemp = (weatherToday \ "max_temp").get.toString()

          replyTo ! Array(weatherState, lowestTemp, highestTemp)

          Behaviors.same

        case _ => Behaviors.same
      }
    }
}
