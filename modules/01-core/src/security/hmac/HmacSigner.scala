package security.hmac

import play.api.Configuration
import play.api.libs.ws.{ StandaloneWSRequest, WSRequestExecutor, WSRequestFilter }
import play.shaded.ahc.org.asynchttpclient.BoundRequestBuilder
import utils.{ StringUtils, TimeUtils }

import scala.jdk.CollectionConverters._

class HmacSigner(configuration: Configuration) extends HmacSignerSecurity {
  override lazy val config = HmacSecurity.parse_config(configuration)
}

trait HmacSignerSecurity {

  def config(): HmacSecurityConfig

  val filter: WSRequestFilter = WSRequestFilter { e =>
    WSRequestExecutor(r => e.apply(secure(r)))
  }

  def secure(req: StandaloneWSRequest): StandaloneWSRequest = {
    val headers = List(
      ("Date"  -> TimeUtils.now_iso()),
      ("nonce" -> StringUtils.randomAlphanumericString(40))
    )

    val req_with_header = req.addHttpHeaders(headers: _*)

    val custom_headers = req_with_header.headers
      .filter(h => config().custom_headers.exists(_.equalsIgnoreCase(h._1)))
      .view.mapValues(_.mkString(","))
      .toList

    val hmac_req = HmacSecurity.wsRequest2HmacRequest(req_with_header, custom_headers)
    val auth     = new HmacCoreSecurity(config()).authorization_header(hmac_req)

    req_with_header.addHttpHeaders("Authorization" -> auth)
  }

  def secure(builder: BoundRequestBuilder): BoundRequestBuilder = {
    // we need to build a request to access data into BoundRequestBuilder
    val req = builder.build()

    val date_header = TimeUtils.now_iso()
    val nonce       = StringUtils.randomAlphanumericString(40)

    val custom_headers = req.getHeaders().entries().asScala.map(x => (x.getKey, x.getValue))
      .filter(h => config().custom_headers.exists(_.equalsIgnoreCase(h._1)))
      .toList :+ ("nonce" -> nonce)

    val req_with_header = builder
      .addHeader("Date", date_header)
      .addHeader("nonce", nonce)

    val hmac_req = HmacSecurityRequest(
      req.getMethod,
      None,
      Some(req.getHeaders.get("Content-Type")),
      date_header,
      nonce,
      custom_headers
    )
    val auth     = new HmacCoreSecurity(config()).authorization_header(hmac_req)

    req_with_header.addHeader("Authorization", auth)
  }
}
