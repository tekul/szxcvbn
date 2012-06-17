
import unfiltered.request._
import unfiltered.response._

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

class App extends unfiltered.filter.Plan {
  import QParams._
  implicit val formats = Serialization.formats(NoTypeHints)

  def intent = {
    case POST(Path(p) & Params(params)) =>
      println("POST %s" format p)

      val expected = for {
        password <- lookup("password") is required("password is required")
        verbose  <- lookup("verbose") is optional[String,String]
      } yield {
        println("-> " + password.get)
        val z = szxcvbn.Zxcvbn(password.get)

        val result = Map[String,Any](
          "score" -> z.score,
          "entropy" -> z.entropy,
          "crack_time_s" -> z.crackTime,
          "crack_time" -> z.crackTimeDisplay,
          "match_sequence" -> z.matches
        )

        val response = Serialization.write(result)
        println("<- " + response)
        JsonContent ~> ResponseString(response)
      }
      expected(params) orFail { fails =>
        Json(("error" -> fails.mkString(" ")))
      }
  }

}

object SzxcvbnServer {
  val resources = new java.net.URL(getClass.getResource("/web/robots.txt"), ".")

  def main(args: Array[String]) {
    println("Port is: " + System.getenv("VCAP_APP_PORT"))
    val http = unfiltered.jetty.Http(Option(System.getenv("VCAP_APP_PORT")).getOrElse("8080").toInt)
      .resources(resources)
      .filter(new App).run()
  }
}