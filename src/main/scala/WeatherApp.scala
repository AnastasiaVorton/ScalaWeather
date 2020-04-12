import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.osinka.i18n.{Lang, Messages}
import play.api.libs.json.{JsValue, Json}
import scalaj.http.{Http, HttpResponse}
import java.util.Locale

object WeatherApp {
  sealed trait Command
  case object Start extends Command
  case object Stop extends Command

  val lang: String = Locale.getDefault.getLanguage

  implicit val userLang: Lang = Lang(lang)

  def apply(): Behavior[Command] = Behaviors.receive { (ctx, msg) =>
    msg match {
      case Start =>

        val printer = ctx.spawn(Printer(ctx.self), "Printer")
        printer ! Printer.Greet

//        val city = scala.io.StdIn.readLine()
//
//        val locationResponse: HttpResponse[String] = Http("https://www.metaweather.com/api/location/search/")
//          .param("query", city)
//          .asString
//
//        val locationJson: JsValue = Json.parse(locationResponse.body.toString().replaceAll("(\\[|\\])", ""))
//
//        val woeid = (locationJson \ "woeid").get.toString().replaceAll("\"", "")
//
//        val weatherResponse: HttpResponse[String] = Http(s"https://www.metaweather.com/api/location/$woeid/")
//          .asString
//
//        val weatherJson: JsValue = Json.parse(weatherResponse.body.toString())
//
//        val weatherToday = (weatherJson \ "consolidated_weather").get(0)
//
//        val weatherState = (weatherToday \ "weather_state_name").get.toString().replaceAll("\"", "").toLowerCase
//        val lowestTemp = (weatherToday \ "min_temp").get
//        val highestTemp = (weatherToday \ "max_temp").get
//
//        println(Messages("weather.summary", city, weatherState))
//        println(Messages("weather.temperature", lowestTemp, highestTemp))

        Behaviors.same

      case Stop => Behaviors.stopped

      case _ => Behaviors.same
    }
  }

}
