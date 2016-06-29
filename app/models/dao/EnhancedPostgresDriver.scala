package models.dao

import com.github.tminglei.slickpg.PgJsonSupport
import com.github.tminglei.slickpg._

trait EnhancedPostgresDriver extends ExPostgresDriver
    with PgArraySupport
    with PgDateSupportJoda
    with PgJsonSupport
    with PgNetSupport
    with PgLTreeSupport
    with PgRangeSupport
    with PgHStoreSupport
    with PgSearchSupport {

  override val pgjson = "jsonb"
  ///
  override val api = new API with ArrayImplicits with DateTimeImplicits with JsonImplicits with NetImplicits with LTreeImplicits with RangeImplicits with HStoreImplicits with SearchImplicits with SearchAssistants {}
}

object EnhancedPostgresDriver extends EnhancedPostgresDriver
