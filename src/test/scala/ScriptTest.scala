import java.time.LocalDate
import ZuoraService.{RatePlan, RatePlanCharge, Subscription}
import Script._
import org.scalatest.FlatSpec
import scalaz.\/-

class ScriptTest extends FlatSpec {

  val testSat = RatePlan("Newspaper Voucher", List(RatePlanCharge("Saturday", Some(LocalDate.of(2018, 7, 29)))))
  val testSun = RatePlan("Newspaper Voucher", List(RatePlanCharge("Sunday", Some(LocalDate.of(2018, 7, 29)))))
  val newTestSun = RatePlan("Newspaper Voucher", List(RatePlanCharge("Sunday", None)))

  def subscription(ratePlans: List[RatePlan], termLength: Int = 12, termPeriodType: String = "Month") = {
    Subscription("abc", termLength, termPeriodType, "Active", ratePlans)
  }

  "getChargedThroughDate" should "calculate the charged through date correctly for a rate plan with a single charge" in {
    val fakeSaturdaySubscription = subscription(List(testSat))
    val result = getChargedThroughDate(fakeSaturdaySubscription)
    assert(result == \/-(LocalDate.of(2018, 7, 29)))
  }

  "getChargedThroughDate" should "calculate the charged through date correctly for a rate plan with multiple charges" in {
    val fakeWeekendSubscription = subscription(List(testSat, testSun))
    val result = getChargedThroughDate(fakeWeekendSubscription)
    assert(result == \/-(LocalDate.of(2018, 7, 29)))
  }

  "getChargedThroughDate" should "return an error if there is no charged through date" in {
    val fakeNewSubscription = subscription(List(newTestSun))
    val result = getChargedThroughDate(fakeNewSubscription)
    assert(result.isLeft)
  }

  "validateTermLength" should "return an error if the term length is non-standard" in {
    val fakeSaturdaySubscription = subscription(List(testSat), 11, "Month")
    val result = validateTermLength(fakeSaturdaySubscription)
    assert(result.isLeft)
  }

  "validateTermLength" should "return an error if the term type is non-standard" in {
    val fakeSaturdaySubscription = subscription(List(testSat), 12, "Days")
    val result = validateTermLength(fakeSaturdaySubscription)
    assert(result.isLeft)
  }

  "calculateDays" should "calculate the correct number of days for an amendment (where the sub has not yet been invoiced past the renewal date)" in {
    val result = calculateDays(chargedThroughDate = LocalDate.of(2018, 8, 10))
    assert(result == 392)
  }

  "calculateDays" should "calculate the correct number of days for an amendment (where the sub has already been invoiced past the renewal date)" in {
    val result = calculateDays(chargedThroughDate = LocalDate.of(2018, 8, 15))
    assert(result == 366)
  }

}
