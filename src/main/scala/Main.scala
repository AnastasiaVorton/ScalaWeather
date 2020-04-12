import com.osinka.i18n.{Lang, Messages}
import play.api.libs.json.{JsValue, Json}
import scalaj.http._

object Main extends App {
  implicit val userLang = Lang("en")
  val msg = Messages("greeting")

  println(msg)
  val input = scala.io.StdIn.readLine()

  val locationResponse: HttpResponse[String] = Http("https://www.metaweather.com/api/location/search/")
    .param("query", input)
    .asString

  val locationJson: JsValue = Json.parse(locationResponse.body.toString().replaceAll("(\\[|\\])", ""))

  val woeid = (locationJson \ "woeid").get.toString().replaceAll("\"", "")

  val weatherResponse: HttpResponse[String] = Http(s"https://www.metaweather.com/api/location/$woeid/")
    .asString

  val weatherJson: JsValue = Json.parse(weatherResponse.body.toString())

  val weatherToday = (weatherJson \ "consolidated_weather").get(0)

  val weatherState = (weatherToday \ "weather_state_name").get.toString().replaceAll("\"", "").toLowerCase
  val lowestTemp = (weatherToday \ "min_temp").get
  val highestTemp = (weatherToday \ "max_temp").get

  println("The weather in " + input + " today is " + weatherState)
  println("The temperature will vary from " + lowestTemp + " to " + highestTemp)
}