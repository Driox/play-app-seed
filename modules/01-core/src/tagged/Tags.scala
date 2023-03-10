package tagged

import helpers.sorus.Fail
import pl.iterators.kebs.macros.CaseClass1Rep
import pl.iterators.kebs.tagged._
import utils.StringUtils

// tag String Id
// https://github.com/theiterators/kebs#tagged-types
object Tags {
  trait IdTag[+A]
  type Id[A] = String @@ IdTag[A]

  object Id {
    def apply[A](arg: String) = from[A](arg)
    def from[A](arg: String)  = arg.taggedWith[IdTag[A]]
  }

  object IdTag {
    implicit def IdCaseClass1Rep[A] = new CaseClass1Rep[Id[A], String](Id.apply(_), identity)
  }

  trait IdCapabilities[A] {
    protected def prefix: String

    def generate()      = Id[A](prefix + StringUtils.generateUuid())
    def from(s: String) = Id[A](s)

    def validate(s: String): Either[Fail, Id[A]] = {
      s.startsWith(prefix) match {
        case true  => Right(Id[A](s))
        case false => Left(Fail(s"Invalid format for ${this.getClass().getSimpleName()}, $s should begin with $prefix"))
      }
    }
  }
}
