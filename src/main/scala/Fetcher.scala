import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import data.WeatherData

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

object Fetcher extends LazyLogging {
  sealed trait Command
  case class FetchWoeid(city: String, replyTo: ActorRef[String]) extends Command
  case class FetchWeather(woeid: String, replyTo: ActorRef[WeatherData]) extends Command

  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val weatherDataRestClient = new WeatherDataRestClient()

  def apply(printer: ActorRef[Printer.Command]): Behavior[Command] =
    Behaviors.receive { (ctx, msg) =>
      msg match {
        case FetchWoeid(city, replyTo) =>
          logger.info(s"Fetcher ${ctx.self} received FetchWoeid")

          val woeidData = weatherDataRestClient.fetchWoeidDataByCity(city)

          woeidData
            .onComplete {
              case Success(res) =>
                val locationJson: JsValue = Json.parse(res.replaceAll("(\\[|\\])", ""))
                val woeid = (locationJson \ "woeid").get.toString()
                logger.info(s"woeid $woeid successfully fetched")

                replyTo ! woeid
              case Failure(error)   =>
                logger.error(s"Request failed with error: $error")
            }

          Behaviors.same

        case FetchWeather(woeid, replyTo) =>
          logger.info(s"Fetcher ${ctx.self} received FetchWeather")

          val result = weatherDataRestClient.fetchWeatherDataByWoeid(woeid)

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
                  case data: JsSuccess[WeatherData] =>
                    replyTo ! data.get
                    logger.info(s"WeatherData ${data.get} successfully fetched")
                  case e: JsError           =>
                    logger.error(s"An error accured while parsing weather data")
                    logger.error("Errors: " + JsError.toJson(e).toString())
                }

              case Failure(error)   =>
                logger.error(s"Request failed with error: $error")
            }

          Behaviors.same

        case _ => Behaviors.same
      }
    }
}
