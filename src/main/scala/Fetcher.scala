import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

case class WeatherData(weatherState: String, lowestTemp: Double, highestTemp: Double)

object Fetcher {
  sealed trait Command
  case class FetchWoeid(city: String, replyTo: ActorRef[String]) extends Command
  case class FetchWeather(woeid: String, replyTo: ActorRef[WeatherData]) extends Command

  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  def apply(printer: ActorRef[Printer.Command]): Behavior[Command] =
    Behaviors.receive { (ctx, msg) =>
      msg match {
        case FetchWoeid(city, replyTo) =>
          val query = s"https://www.metaweather.com/api/location/search/?query=$city"

          val locationResponse: Future[HttpResponse] =
            Http().singleRequest(HttpRequest(uri = query))

          val result: Future[String] = locationResponse.flatMap {
            case HttpResponse(StatusCodes.OK, _, e, _) =>
              Unmarshal(e).to[String]
            case HttpResponse(status, _, e, _) =>
              e.discardBytes()
              Future.failed(sys.error("something wrong"))
          }

          result
            .onComplete {
              case Success(res) =>
                val locationJson: JsValue = Json.parse(res.replaceAll("(\\[|\\])", ""))
                val woeid = (locationJson \ "woeid").get.toString()

                replyTo ! woeid
              case Failure(_)   => sys.error("something wrong")
            }

          Behaviors.same

        case FetchWeather(woeid, replyTo) =>
          val query = s"https://www.metaweather.com/api/location/$woeid/"

          val weatherResponse: Future[HttpResponse] =
            Http().singleRequest(HttpRequest(uri = query))

          val result: Future[String] = weatherResponse.flatMap {
            case HttpResponse(StatusCodes.OK, _, e, _) =>
              Unmarshal(e).to[String]
            case HttpResponse(status, _, e, _) =>
              e.discardBytes()
              Future.failed(sys.error("something wrong"))
          }

          result
            .onComplete {
              case Success(res) =>
                val locationJson: JsValue = Json.parse(res)
                val weatherToday = (locationJson \ "consolidated_weather").get(0)

                implicit val weatherDataReads: Reads[WeatherData] = (
                  (JsPath \ "weather_state_name").read[String] and
                  (JsPath \ "min_temp").read[Double] and
                    (JsPath \ "max_temp").read[Double]
                  )(WeatherData.apply _)

                val weatherResult: JsResult[WeatherData] = weatherToday.validate[WeatherData](weatherDataReads)

                weatherResult match {
                  case data: JsSuccess[WeatherData] => replyTo ! data.get
                  case e: JsError           => println("Errors: " + JsError.toJson(e).toString())
                }

              case Failure(_)   => sys.error("something wrong")
            }

          Behaviors.same

        case _ => Behaviors.same
      }
    }
}
