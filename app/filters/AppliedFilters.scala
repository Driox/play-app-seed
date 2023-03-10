package filters

import play.api.http.HttpFilters
import play.filters.cors.CORSFilter
import play.filters.csrf.CSRFFilter
import play.filters.gzip.GzipFilter
import play.filters.headers.SecurityHeadersFilter

import javax.inject.Inject

class AppliedFilters @Inject() (
  gzip:                  GzipFilter,
  cors:                  CORSFilter,
  csrf:                  CSRFFilter,
  https:                 HttpsFilter,
  expireSessionFilter:   ExpireSessionFilter,
  blackListCookieFilter: BlackListCookieFilter,
  security:              SecurityHeadersFilter
) extends HttpFilters {

  val filters = Seq(https, /* SecurityHeadersFilter,*/ cors, csrf, gzip, expireSessionFilter, blackListCookieFilter)
}
