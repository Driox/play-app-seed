package test.mock

import play.api.cache.AsyncCacheApi

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.reflect.ClassTag
import akka.Done
import scala.concurrent.ExecutionContext.Implicits.global

class CacheMock extends AsyncCacheApi {

  private[this] var data: mutable.Map[String, Any] = scala.collection.mutable.Map()

  /**
   * Set a value into the cache.
   *
   * @param key Item key.
   * @param value Item value.
   * @param expiration Expiration time.
   */
  def set(key: String, value: Any, expiration: Duration = Duration.Inf) = Future.successful {
    data.put(key, value)
    Done
  }

  /**
   * Remove a value from the cache
   */
  def remove(key: String) = Future.successful {
    data.remove(key)
    Done
  }

  /**
   * Retrieve a value from the cache, or set it from a default function.
   *
   * @param key Item key.
   * @param expiration expiration period in seconds.
   * @param orElse The default function to invoke if the value was not found in cache.
   */
  def getOrElseUpdate[A: ClassTag](
    key:        String,
    expiration: Duration = Duration.Inf
  )(orElse:     => Future[A]): Future[A] = {
    get[A](key).flatMap {
      case Some(x) => Future.successful(x)
      case None    => {
        val value = orElse
        set(key, value, expiration)
        value
      }
    }
  }

  /**
   * Retrieve a value from the cache for the given type
   *
   * @param key Item key.
   * @return result as Option[T]
   */
  def get[T: ClassTag](key: String): Future[Option[T]] = Future.successful {
    data.get(key).map(_.asInstanceOf[T])
  }

  def removeAll(): Future[Done] = Future.successful {
    data.clear()
    Done
  }
}
