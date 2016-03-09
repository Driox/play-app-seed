package utils

import play.api.i18n.Lang
import play.api.Logger

import scala.collection.JavaConverters._
import scala.collection.mutable

import com.ibm.icu.text.MessageFormat
import scala.reflect.ClassTag

/**
 * i18n made with icu : http://site.icu-project.org/
 *
 * Java doc : http://icu-project.org/apiref/icu4j/com/ibm/icu/text/MessageFormat.html
 */
object m {
  private val messagesCache = mutable.Map[Lang, Resource]()
  private def messages(lang: Lang) = messagesCache.getOrElseUpdate(lang, Resource("i18n/messages." + lang.code + ".conf"))

  private val formatCache = mutable.Map[(String, Lang), MessageFormat]()
  private def format(key: String)(implicit lang: Lang) =
    formatCache.getOrElseUpdate((key, lang), new MessageFormat(m(key), lang.toLocale))

  def apply(key: String)(implicit lang: Lang): String = messages(lang)(key).getOrElse {
    Logger.warn(s"Invalid i18n key '$key', locale '${lang.code}'")
    key
  }

  def apply[X: ClassTag](key: String, args: (String, Any)*)(implicit lang: Lang): String =
    format(key).format(args.toMap.asJava)

  /*
   * ClassManifest come from : http://stackoverflow.com/questions/3307427/scala-double-definition-2-methods-have-the-same-type-erasure
   */
  def apply(key: String, args: (Any)*)(implicit lang: Lang): String = {
    format(key).format(args.toArray)
  }
}
