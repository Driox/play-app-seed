package utils

import helpers.sorus.Fail
import play.api.Logger
import scalaz.{ -\/, \/ }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.control.NonFatal

object LoggerUtils {

  val not_handled: String = "warning.event.not.handled"

  private[this] def is_handled(fail: Fail): Boolean =
    fail.message != not_handled || !(fail.userMessage()).startsWith(not_handled)

  /**
   * You can choose your logger and log level like this
   *
   * private[this] val log = LoggerFactory.getLogger(this.getClass)
   * LoggerUtils.log_error(fail, log.info, log.info)
   * or
   * LoggerUtils.log_error(
   *   fail,
   *   (s:String, t:Throwable) => logger.error(s, t),
   *   (s:String) => logger.error(s)
   * )
   */
  def log_error(fail: Fail, log_with_error: (String, Throwable) => Unit, log: String => Unit): Unit = {
    if(is_handled(fail)) {
      fail.getRootException()
        .map(ex => log_with_error(fail.userMessage(), ex))
        .getOrElse(log(fail.userMessage()))
    }
  }

  def log_light(logger: Logger)(fail: Fail): Unit = {
    if(is_handled(fail)) {
      fail.getRootException()
        .map(ex => logger.error(fail.userMessage(), ex))
        .getOrElse(logger.warn(fail.userMessage()))
    }
  }

  def log_error(fail: Fail)(logger: Logger): Unit = {
    if(is_handled(fail)) {
      fail.getRootException()
        .map(ex => logger.error(fail.userMessage(), ex))
        .getOrElse(logger.error(fail.userMessage()))
    }
  }

  def log_error(v: Fail \/ _)(logger: Logger): Unit = {
    v.swap.map(fail => log_error(fail)(logger))
    ()
  }

  def log_errors(v: Seq[Fail \/ _])(logger: Logger): Unit = v.foreach(log_error(_)(logger))

  def log_errors[A](f: Future[Seq[Fail \/ A]], msg: Option[String])(logger: Logger)(implicit
    ec:                ExecutionContext
  ): Future[Seq[Fail \/ A]] = {
    f.map(log_errors(_)(logger))
    f.recoverWith {
      case NonFatal(e) => {
        logger.error(msg.getOrElse("Error in Future"), e)
        f
      }
    }
  }

  def log_error[A](f: Future[Fail \/ A], msg: String)(logger: Logger)(implicit
    ec:               ExecutionContext
  ): Future[Fail \/ A] = log_error(f, Some(msg))(logger)(ec)

  def log_error[A](f: Future[Fail \/ A], msg: Option[String] = None)(logger: Logger)(implicit
    ec:               ExecutionContext
  ): Future[Fail \/ A] = {
    f.map(log_error(_)(logger))

    f.recoverWith {
      case NonFatal(e) => {
        logger.error(msg.getOrElse("Error in Future"), e)
        f
      }
    }
  }

  def log(
    logger:      Logger
  )(
    result:      Future[Fail \/ _]
  )(
    custom_msg:  String
  )(implicit ec: ExecutionContext): Future[Unit] = {
    result
      .collect {
        case -\/(fail) if is_handled(fail) => {
          val msg = s"$custom_msg. ${fail.userMessage()}"
          fail.getRootException().map { exception =>
            logger.error(msg, exception)
          }.getOrElse(
            logger.error(msg)
          )
        }
        case _                             => ()
      }
      .recover {
        case NonFatal(exception) => logger.error(custom_msg, exception)
      }
  }
}
