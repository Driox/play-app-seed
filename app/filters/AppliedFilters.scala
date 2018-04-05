package filters

import javax.inject.Inject

import play.api.http.HttpFilters
import play.filters.gzip.GzipFilter
import play.filters.cors.CORSFilter
import play.filters.csrf.CSRFFilter

class AppliedFilters @Inject() (
  gzip:                  GzipFilter,
  cors:                  CORSFilter,
  csrf:                  CSRFFilter,
  https:                 HttpsFilter,
  expireSessionFilter:   ExpireSessionFilter,
  blackListCookieFilter: BlackListCookieFilter
) extends HttpFilters {

  val filters = Seq(https, /* SecurityHeadersFilter,*/ cors, csrf, gzip, expireSessionFilter, blackListCookieFilter)
}
