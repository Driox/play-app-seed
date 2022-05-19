package tagging

import pl.iterators.kebs.tagged._

/**
 * We use kebs (https://github.com/theiterators/kebs) for tagging
 * But it miss TypeclassTaggingCompat from https://github.com/softwaremill/scala-common
 *
 * scala-common alone don't work well
 */
trait TypeclassTaggingCompat[Typeclass[_]] {

  implicit def liftTypeclass[T, Tag](implicit tc: Typeclass[T]): Typeclass[T @@ Tag] =
    tc.asInstanceOf[Typeclass[T @@ Tag]]

}

trait AnyTypeclassTaggingCompat {

  implicit def liftAnyTypeclass[Typeclass[_], T, Tag](implicit tc: Typeclass[T]): Typeclass[T @@ Tag] =
    tc.asInstanceOf[Typeclass[T @@ Tag]]

}

object AnyTypeclassTaggingCompat extends AnyTypeclassTaggingCompat
