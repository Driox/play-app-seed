package utils

import java.io.File

import com.typesafe.config._

/**
 * A very thin wrapper for Typesafe Config's `ConfigFactory`.
 * Loads a '''HOCON''' file without merging it with other configuration,
 * encapsulating the returned `Config` object and providing a more idiomatic
 * Scala interface to it.
 */
private[utils] final class Resource(resource: String) {

  private[this] val cfg = ConfigFactory.parseFile(new File("conf/" + resource))
    .resolve(ConfigResolveOptions.noSystem)

  def apply(key: String): Option[String]  = if(has(key)) Some(cfg.getString(key)) else None
  def int(key: String): Option[Int]       = if(has(key)) Some(cfg.getInt(key)) else None
  def bool(key: String): Option[Boolean]  = if(has(key)) Some(cfg.getBoolean(key)) else None
  def double(key: String): Option[Double] = if(has(key)) Some(cfg.getDouble(key)) else None
  def has(key: String): Boolean           = cfg.hasPath(key)
}

private[utils] object Resource {
  def apply(resource: String): Resource = new Resource(resource)
}
