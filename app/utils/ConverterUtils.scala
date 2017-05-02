package utils

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq
import org.joda.time.DateTime

object ConverterUtils {

  def stringToDate(s: String, pattern: String): Option[DateTime] = {
    scala.util.Try(DateUtils.stringToDate(s, pattern)).toOption
  }

  def stringToInt(s: String): Option[Int] = {
    if (s == "inf") {
      Some(Integer.MAX_VALUE)
    } else {
      scala.util.Try(s.toInt).toOption
    }
  }

  def prettyPrintXml(xml: NodeSeq) = {
    val printer = new scala.xml.PrettyPrinter(80, 2)
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
