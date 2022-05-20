/*
 * Copyright 2014
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package effect

import java.io.{ PrintWriter, StringWriter }

class Fail(val message: String, val cause: Option[Either[Throwable, Fail]] = None) {

  def withEx(s: String): Fail = new Fail(s, Some(Right(this)))

  def withEx(ex: Throwable): Fail = new Fail(this.message, Some(Left(ex)))

  def withEx(fail: Fail): Fail = new Fail(this.message, Some(Right(fail)))

  private def messages(): List[String] = cause match {
    case None                => List(message)
    case Some(Left(exp))     => message :: List(s"${exp.getMessage} ${getStackTrace(exp)}")
    case Some(Right(parent)) => message :: parent.messages()
  }

  def techMessages(): List[String] = messages()

  def userMessages(): List[String] = (cause match {
    case None                => List(message)
    case Some(Left(_))       => List(message)
    case Some(Right(parent)) => message :: parent.userMessages()
  })

  def userMessage(): String = userMessages().mkString(". ")

  def getRootException(): Option[Throwable] = cause flatMap {
    _ match {
      case Left(exp)     => Some(exp)
      case Right(parent) => parent.getRootException()
    }
  }

  /**
   * from play.libs.exception.ExceptionUtils
   */
  private[this] def getStackTrace(throwable: Throwable): String = {
    val sw = new StringWriter()
    val pw = new PrintWriter(sw, true)
    throwable.printStackTrace(pw)
    sw.getBuffer().toString()
  }

  override def toString(): String = {
    message + cause.map(_.toString).getOrElse("")
  }

  override def equals(obj: Any): Boolean = {
    obj match {
      case f: Fail => f.message == this.message && f.cause == this.cause
      case _       => false
    }
  }

  override def hashCode(): Int = {
    this.message.hashCode + this.cause.hashCode()
  }
}

object Fail {
  def apply(message: String, cause: Option[Either[Throwable, Fail]] = None): Fail = new Fail(message, cause)
}
