package models.dao

import play.api.Play.current

/**
 * We use it for now to allow slick-pg support for specifiq postgreSql type
 *
 * We will switch to EnhancedPostgresDriver for full support. But for now the test don't work so we need this hack
 */
trait WithMapSupport { self: com.github.tototoshi.slick.GenericJodaSupport =>

  implicit val mapTypeMapper = EnhancedPostgresDriver.api.simpleHStoreTypeMapper
}

/**
 * This is needed to support joda time for all driver
 *
 * https://github.com/tototoshi/slick-joda-mapper/issues/8
 */
object PortableJodaSupport
  extends com.github.tototoshi.slick.GenericJodaSupport(play.api.db.slick.DatabaseConfigProvider.get.driver)
  with WithMapSupport
