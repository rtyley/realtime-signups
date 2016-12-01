package shared

import enumeratum._

import scala.collection.immutable.IndexedSeq

sealed trait Checkpoints extends EnumEntry

object Checkpoints extends Enum[Checkpoints] {

  val values: IndexedSeq[Checkpoints] = findValues

  case object ExternalCta extends Checkpoints
  case object LandingPage extends Checkpoints
  case object Checkout extends Checkpoints
  case object ThankYou extends Checkpoints

}