package models.dao

import com.github.tototoshi.slick.GenericJodaSupport
import slick.jdbc.{H2Profile, JdbcProfile, PostgresProfile}

object DbDriver {

  trait DbProfile extends GenericJodaSupport with JdbcProfile

  class PostgreSqlDriver
    extends GenericJodaSupport(PostgresProfile)
    with EnhancedPostgresDriver
    with DbProfile

  //  with PostGreEnumSupport
  //  with JsonSupport
  //  with CollectionSupport

  class H2Driver extends GenericJodaSupport(H2Profile)
    with DbProfile

  //  with H2EnumSupport
  //  with JsonSupport
  //  with CollectionSupport

}
