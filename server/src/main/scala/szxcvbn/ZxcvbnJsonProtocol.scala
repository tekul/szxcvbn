package szxcvbn

import spray.json._

/**
 * Spray JSON serializer for Zxcvbn objects
 */
object ZxcvbnJsonProtocol extends DefaultJsonProtocol {

  implicit object ZxcvbnJsonFormat extends RootJsonFormat[Zxcvbn] {
    def write(z: Zxcvbn) = JsObject(
      "score" -> JsNumber(z.score),
      "entropy" -> JsNumber(z.entropy),
      "crack_time_s" -> JsNumber(z.crackTime),
      "crack_time" -> JsNumber(z.crackTimeDisplay),
      "match_sequence" -> JsArray(z.matches map { m =>
         JsObject(
           "start" -> JsNumber(m.start),
           "end"   -> JsNumber(m.end),
           "token" -> JsString(m.token),
           "pattern"  -> JsString(m.pattern)
         )

      })
    )

    def read(value: JsValue) = throw new UnsupportedOperationException


  }

}
