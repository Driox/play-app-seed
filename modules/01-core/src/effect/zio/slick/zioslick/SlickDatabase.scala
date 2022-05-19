package effect.zio.slick.zioslick

import slick.basic.BasicBackend

trait SlickDatabase {
  val database: BasicBackend#DatabaseDef
}

object SlickDatabase {

  object Live {
    def apply(db: BasicBackend#DatabaseDef) = new SlickDatabase {
      override val database = db
    }
  }
}
