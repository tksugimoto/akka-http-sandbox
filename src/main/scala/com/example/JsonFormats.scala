package com.example

import com.example.UserRegistry.ActionPerformed

//#json-formats
import spray.json._

object JsonFormats  {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._

  implicit private val userNameJsonFormat: JsonFormat[UserName] = new JsonFormat[UserName] {
    override def read(json: JsValue): UserName = json match {
      case JsString(value) => UserName(value)
      case _ => deserializationError("String expected")
    }
    override def write(userName: UserName): JsValue = JsString(userName.value)
  }
  implicit val userJsonFormat = jsonFormat3(User)
  implicit val usersJsonFormat = jsonFormat1(Users)

  implicit val actionPerformedJsonFormat = jsonFormat1(ActionPerformed)
}
//#json-formats
