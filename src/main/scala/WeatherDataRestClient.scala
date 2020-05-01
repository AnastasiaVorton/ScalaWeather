import akka.actor.ActorSystem
import akka.http.scaladsl.{Http, HttpExt}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}

class WeatherDataRestClient(implicit actorSystem: ActorSystem, mat: Materializer, executionContext: ExecutionContext) extends LazyLogging {
  private val path = "https://www.metaweather.com/api/location/"

  def http: HttpExt = Http()

  def fetchWeatherDataByWoeid(woeid: String): Future[String] = {
    val request = HttpRequest(uri = s"$path$woeid/")

    http.singleRequest(request).flatMap {
      case HttpResponse(StatusCodes.OK, _, e, _) =>
        logger.info("Weather data successfully fetched")

        Unmarshal(e).to[String]
      case HttpResponse(status, _, e, _) =>
        logger.error(s"""Request failed with status $status""")
        logger.error(s"Error: $e")

        e.discardBytes()

        Future.failed(sys.error("Request failed, see log.txt"))
    }
  }

  def fetchWoeidDataByCity(city: String): Future[String] = {
    val request = HttpRequest(uri = s"${path}search/?query=$city")

    http.singleRequest(request).flatMap {
      case HttpResponse(StatusCodes.OK, _, e, _) =>
        logger.info("Woeid data successfully fetched")

        Unmarshal(e).to[String]
      case HttpResponse(status, _, e, _) =>
        logger.error(s"""Request failed with status $status""")
        logger.error(s"Error: $e")

        e.discardBytes()

        Future.failed(sys.error("Request failed, see log.txt"))
    }
  }
}
