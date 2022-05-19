package test

import org.scalatest._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

abstract class AppSpec extends AnyWordSpec with Matchers with OptionValues {
  protected class PrivateMethodCaller(x: AnyRef, methodName: String) {
    def apply(_args: Any*): Any = {
      val args                         = _args.map(_.asInstanceOf[AnyRef])
      def _parents: LazyList[Class[_]] = LazyList(x.getClass) #::: _parents.map(_.getSuperclass)
      val parents                      = _parents.takeWhile(_ != null).toList
      val methods                      = parents.flatMap(_.getDeclaredMethods)
      val method                       = methods.find(_.getName == methodName)
        .getOrElse(throw new IllegalArgumentException(
          s"Method $methodName not found"
        ))
      method.setAccessible(true)
      method.invoke(x, args: _*)
    }
  }

  protected class PrivateMethodExposer(x: AnyRef) {
    def apply(method: scala.Symbol): PrivateMethodCaller = new PrivateMethodCaller(x, method.name)
  }

  /**
   * usage :
   *  invokePrivate(instance | object)('method_name)(input1, input2, input3)
   */
  protected def invokePrivate[T](x: AnyRef)(method: scala.Symbol)(_args: Any*): T = {
    new PrivateMethodExposer(x)(method)(_args: _*).asInstanceOf[T]
  }

}
