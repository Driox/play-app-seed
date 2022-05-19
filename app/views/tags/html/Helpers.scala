import _root_.utils.m
import play.api.i18n.Lang
import play.twirl.api._

import scala.jdk.CollectionConverters._

package views.html.tags.html {

  import play.api.data.FormError

  object Enhancer {
    implicit def enhancedFormError(f_err: Option[FormError]) = new EnhancedOptionalFormError(f_err)
    implicit def enhancedFormError(f_err: FormError)         = new EnhancedFormError(f_err)
  }

  /**
   * usage
   *   @credentialsForm.error("password").format()
   */
  class EnhancedFormError(error: FormError) {
    def translate()(implicit lng: Lang): String = {
      m(error.message, error.args: _*).trim
    }

    def html()(implicit lng: Lang): Html = {
      Html(s"""<span class="mdl-textfield__error">${translate()}</span>""")
    }
  }

  class EnhancedOptionalFormError(error: Option[FormError]) {
    def translate()(implicit lng: Lang): String = error.map(e => new EnhancedFormError(e).translate()).getOrElse("")
    def html()(implicit lng:      Lang): Html   = error.map(e => new EnhancedFormError(e).html()).getOrElse(Html(""))
  }

  case class FieldElements(id: String, field: play.api.data.Field, input: Html, args: Map[Symbol, Any], lang: Lang) {

    def infos: Seq[String] = {
      args.get(Symbol("_help")).map(msg => Seq(msg.toString)).getOrElse {
        (if(
           args.get(Symbol("_showConstraints")) match {
             case Some(false) => false
             case _           => true
           }
         ) {
           field.constraints.map(c => m(c._1, c._2: _*)(lang)) ++
             field.format.map(f => m(f._1, f._2: _*)(lang))
         } else { Nil })
      }
    }

    def errors: Seq[String] = {
      (args.get(Symbol("_error")) match {
        case Some(Some(play.api.data.FormError(_, message, args))) => Some(Seq(m(message.head, args: _*)(lang)))
        case _                                                     => None
      }).getOrElse {
        (if(
           args.get(Symbol("_showErrors")) match {
             case Some(false) => false
             case _           => true
           }
         ) {
           field.errors.map(e => m(e.message, e.args: _*)(lang))
         } else { Nil })
      }
    }

    def hasErrors: Boolean = {
      !errors.isEmpty
    }

    def isRequired: Boolean = {
      args.get(Symbol("required")).isDefined || args.get(Symbol("_required")).isDefined || infos.contains(
        "constraint.required"
      )
    }

    def hasLabel: Boolean = args.get(Symbol("_label")).isDefined

    def label: Any = {
      args.get(Symbol("_label")).getOrElse(m(field.label)(lang))
    }

    def hasName: Boolean = args.get(Symbol("_name")).isDefined

    def name: Any = {
      args.get(Symbol("_name")).getOrElse(m(field.label)(lang))
    }

  }

  trait FieldConstructor {
    def apply(elts: FieldElements): Html
  }

  object FieldConstructor {

    implicit val defaultField = FieldConstructor(views.html.tags.html.bootstrapFieldConstructor.f)

    def apply(f: FieldElements => Html): FieldConstructor = new FieldConstructor {
      def apply(elts: FieldElements) = f(elts)
    }

    implicit def inlineFieldConstructor(f:     (FieldElements) => Html)        = FieldConstructor(f)
    implicit def templateAsFieldConstructor(t: Template1[FieldElements, Html]) = FieldConstructor(t.render)
  }

  object Repeat {

    /**
     * Render a field a repeated number of times.
     *
     * Useful for repeated fields in forms.
     *
     * @param field The field to repeat.
     * @param min The minimum number of times the field should be repeated.
     * @param fieldRenderer A function to render the field.
     * @return The sequence of rendered fields.
     */
    def apply(field: play.api.data.Field, min: Int = 1)(fieldRenderer: play.api.data.Field => Html): Seq[Html] = {
      val indexes = field.indexes match {
        case Nil                              => 0 until min
        case complete if complete.size >= min => field.indexes
        case partial                          =>
          // We don't have enough elements, append indexes starting from the largest
          val start  = field.indexes.max + 1
          val needed = min - field.indexes.size
          field.indexes ++ (start until (start + needed))
      }

      indexes.map(i => fieldRenderer(field("[" + i + "]")))
    }
  }

  object Options {

    def apply(options: (String, String)*)             = options.toSeq
    def apply(options: Map[String, String])           = options.toSeq
    def apply(options: java.util.Map[String, String]) = options.asScala.toSeq
    def apply(options: List[String])                  = options.map(v => v -> v)
    def apply(options: java.util.List[String])        = options.asScala.map(v => v -> v)

  }

  object Implicits {
    implicit def toAttributePair(pair: (String, String)): (Symbol, String) = Symbol(pair._1) -> pair._2
  }

}
