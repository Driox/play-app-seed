package modules

import net.sf.ehcache.CacheManager
import play.api.Logging
import play.api.inject.ApplicationLifecycle

import javax.inject._
import scala.concurrent.Future

@Singleton
class OnStop @Inject() (
  applicationLifecycle: ApplicationLifecycle
) extends Logging {
  applicationLifecycle.addStopHook(onStop())

  private[this] def onStop() = () => {
    CacheManager.getInstance().shutdown()
    Future.successful {
      logger.info("--- App stoped : Plug ---")
    }
  }
}
