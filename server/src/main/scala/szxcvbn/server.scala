package szxcvbn

import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._

import scala.Some
import spray.json._
import ZxcvbnJsonProtocol._

object App extends cycle.Plan with cycle.SynchronousExecution with ServerErrorResponse {

  import QParams._
  import szxcvbn._

  def intent = {
    case POST(Path(p) & Params(params)) =>
      println("POST %s" format p)

      val expected = for {
        password <- lookup("password") is required("password is required")
        userData <- lookup("userData") is optional[String, String]
      } yield {
        println("-> " + password.get)
        val z = Zxcvbn(password.get, matchers(userData.get))

        val response = z.toJson.compactPrint
        println("<- " + response)
        JsonContent ~> ResponseString(response)
      }
      expected(params) orFail {
        fails =>
          JsonContent ~> ResponseString(Map("error" -> fails.mkString(" ")).toJson.compactPrint)
      }
  }

  private def matchers(userData: Option[String]): Seq[Matcher[Match]] = userData match {
    case Some(s) if (!s.isEmpty) =>
      Zxcvbn.createMatcher("userData", s.toLowerCase.split(",").toSeq) +: Zxcvbn.defaultMatchers
    case _ => Zxcvbn.defaultMatchers
  }
}

object Server {
  val resources = new java.net.URL(getClass.getResource("/web/robots.txt"), ".")

  def main(args: Array[String]) {
    println("Port is: " + System.getenv("VCAP_APP_PORT"))
    val http = unfiltered.netty.Http(Option(System.getenv("VCAP_APP_PORT")).getOrElse("8080").toInt)
      .resources(resources)
      .handler(App).run()
  }
}
