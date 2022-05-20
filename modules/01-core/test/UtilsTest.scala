import play.api.i18n.Lang
import utils.{ NumberUtils, StringUtils, TimeUtils }

import java.time.Month
import java.util.Locale

import org.scalatestplus.play.PlaySpec

import com.ibm.icu.util.Currency

class UtilsTest extends PlaySpec {

  "TimeUtils" should {
    "handle empty zero" in {
      val d      = "2019-06-24T09:39:00Z"
      val a_date = TimeUtils.parse(d)
      a_date.isDefined mustBe true

      val d_iso = TimeUtils.toIso(a_date.get)
      d_iso mustBe d
    }

    "parse a date in iso format" in {
      val maybe_date = TimeUtils.parse("2017-04-26T15:13:12Z")
      maybe_date.isDefined mustBe true

      maybe_date.map { date =>
        date.getYear mustBe 2017
        date.getMonth.getValue mustBe 4
        date.getDayOfMonth mustBe 26
        date.getHour mustBe 15
        date.getMinute mustBe 13
        date.getSecond mustBe 12
      }
    }

    "print a date in iso format" in {
      val date = TimeUtils.now
        .withHour(14)
        .withMinute(23)
        .withSecond(5)
        .withDayOfMonth(15)
        .withMonth(8)
        .withYear(1984)

      TimeUtils.toIso(date) mustBe "1984-08-15T14:23:05Z"
    }

    "return None on wrong format" in {
      TimeUtils.parse("lkjld98729") mustBe None
    }

    "format with locale" in {
      val d = TimeUtils.from(2019, Month.JANUARY, 24)

      TimeUtils.format(d, new Locale("fr_FR")) mustBe "24/01/2019"
      TimeUtils.format(d, new Locale("en_US")) mustBe "01/24/2019"
    }
  }

  "StringUtils" should {

    "validate an email" in {
      val email_1 = "toto@gmail.com"
      val email_2 = "toto.gmail@com"

      StringUtils.isEmail(email_1) mustBe true
      StringUtils.isEmail(email_2) mustBe false
      StringUtils.isEmail(null) mustBe false
      StringUtils.isEmail("") mustBe false
    }

    "validate an uuid" in {
      val uuid_1 = "a6075fc2-07b7-4a71-9f99-e648daeda35e"
      val uuid_2 = "toto.gmail@com"
      val uuid_3 = StringUtils.generateUuid()

      StringUtils.isUuid(uuid_1) mustBe true
      StringUtils.isUuid(uuid_2) mustBe false
      StringUtils.isUuid(uuid_3) mustBe true
      StringUtils.isUuid(null) mustBe false
      StringUtils.isUuid("") mustBe false
    }

    "validate an ip" in {
      val ip_1 = "192.128.1.1"
      val ip_2 = "567.98.3.12"
      val ip_3 = "ldfkjlgkjlgdjfgl"

      StringUtils.isIp(ip_1) mustBe true
      StringUtils.isIp(ip_2) mustBe false
      StringUtils.isIp(ip_3) mustBe false
      StringUtils.isIp(null) mustBe false
      StringUtils.isIp("") mustBe false
    }

    "validate an url" in {
      val url_1 = "https://www.dlfkgj.com/lksdjf?x=12&b=LKJ"
      val url_2 = "www.lolilol.com"
      val url_3 = "ldfkjlgkjlgdjfgl"

      StringUtils.isUrl(url_1) mustBe true
      StringUtils.isUrl(url_2) mustBe false
      StringUtils.isUrl(url_3) mustBe false
      StringUtils.isUrl(null) mustBe false
      StringUtils.isUrl("") mustBe false
    }
  }

  "NumberUtils" should {
    "format amount for fr lng" in {
      NumberUtils.displayAmountFromCentime(12030040L, Lang("fr")) mustBe "120 300,40 ¤"
      NumberUtils.displayAmountFromCentime(12030040L, Lang("fr"), Currency.getInstance("EUR")) mustBe "120 300,40 €"
      NumberUtils.displayAmountFromCentime(12030040L, Lang("fr"), Currency.getInstance("USD")) mustBe "120 300,40 $US"
    }
    "format amount in EUR" in {
      NumberUtils.displayAmountFromCentime(12030040L, Lang("fr-FR")) mustBe "120 300,40 €"
    }
    "format amount in USD" in {
      NumberUtils.displayAmountFromCentime(12030040L, Lang("en-US")) mustBe "$120,300.40"
    }
  }
}
