package com.greyscribes

import play.api.libs.json._
import play.api.data.validation.ValidationError

case class Entry(
  interface: Option[String],
  private_ip: String,
  public_ip: Option[String],
  input_chain: Option[String],
  forward_chain: Option[String],
  prerouting_chain: Option[String],
  port_map: Map[String, Seq[(Int, Int)]])

object Entry {

  implicit def tuple2Reads[A, B](implicit aReads: Reads[A], bReads: Reads[B]) = Reads[Tuple2[A, B]] {
    case JsArray(arr) if (arr.size == 2) ⇒ for {
      a ← aReads.reads(arr(0))
      b ← bReads.reads(arr(1))
    } yield (a, b)
    case _ ⇒ JsError(Seq(JsPath() -> Seq(ValidationError("Expected array of two elements"))))
  }

  implicit val reader = Json.reads[Entry]

}