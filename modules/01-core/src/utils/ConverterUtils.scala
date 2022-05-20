package utils

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Try
import scala.xml.NodeSeq

import com.ibm.icu.util.Currency

object ConverterUtils {

  def stringToLong(s: String): Option[Long] = {
    if(s == "inf") {
      Some(Long.MaxValue)
    } else {
      Try(s.toLong).toOption
    }
  }

  def stringToInt(s: String): Option[Int] = {
    if(s == "inf") {
      Some(Int.MaxValue)
    } else {
      Try(s.toInt).toOption
    }
  }

  def stringToDouble(s: String): Option[Double] = {
    if(s == "inf") {
      Some(Double.MaxValue)
    } else {
      Try(BigDecimal(s).toDouble).toOption
    }
  }

  def stringToCurrency(s: String): Option[Currency] = {
    Try {
      Currency.getInstance(s)
    }.toOption
  }

  def prettyPrintXml(xml: NodeSeq) = {
    xml.toList.map(_.toString).fold("")((x, y) => x + y)
  }

  def scalaMapToJavaMap[A, B](smap: Map[A, B]): java.util.Map[A, B] = {
    val jmap = new java.util.HashMap[A, B]
    smap.foreach(p => jmap.put(p._1, p._2.asInstanceOf[B]))

    jmap
  }

  def transform[A](o: Option[Future[A]])(implicit exec: ExecutionContext): Future[Option[A]] =
    o.map(f => f.map(Option(_))).getOrElse(Future.successful(None))
}
