import java.time.LocalDate
import java.time.temporal.ChronoUnit
import ZuoraService.Subscription
import scalaz.{-\/, \/, \/-}

object Script extends App with Logging {

  def processSubscription(subName: String): Unit = {
    logger.info(s"${subName}: started processing")
    val attempt = for {
      subscription <- ZuoraService.getSubscription(subName)
      termLengthCheck <- validateTermLength(subscription)
      chargedThroughDate <- getChargedThroughDate(subscription)
      amendment <- ZuoraService.createAmendment(subName, calculateDays(chargedThroughDate))
    } yield amendment
    attempt match {
      case \/-(_) => logSuccessfulResult(subName)
      case -\/(error) => logFailureResult(subName, error)
    }
  }

  def getChargedThroughDate(subscription: Subscription): String \/ LocalDate = {
    val voucherPlans = subscription.ratePlans.filter(ratePlan => ratePlan.productName == "Newspaper Voucher")
    val maybeChargedThroughDate = voucherPlans.headOption.flatMap(_.ratePlanCharges.map(_.chargedThroughDate).headOption.flatten)
    maybeChargedThroughDate match {
      case Some(date) =>
        logInfo(subscription.name, s"charged through date is: $date")
        \/-(date)
      case None =>
        logError(subscription.name, s"failed to identify charged through date for sub: $subscription")
        -\/("unable to identify charged through date")
    }
  }

  def validateTermLength(subscription: Subscription): String \/ Unit = {
    if (subscription.currentTerm == 12 && subscription.currentTermPeriodType == "Month")
      \/-(logInfo(subscription.name, "passed term validation"))
    else
      -\/(s"${subscription.name}: current term length was non-standard: $subscription")
  }

  def calculateDays(chargedThroughDate: LocalDate): Long = {
    val renewalDate = LocalDate.of(2018, 8, 14)
    if (chargedThroughDate.isAfter(renewalDate)) 365 + ChronoUnit.DAYS.between(renewalDate, chargedThroughDate)
    else 365 + ChronoUnit.DAYS.between(renewalDate.minusMonths(1), chargedThroughDate)
  }

  // Script starts here
  val inputFile = "test.csv"
  logger.info(s"Starting script: using input file $inputFile")
  val subscriptons = InputReader.readFile(inputFile)
  subscriptons.foreach(sub => processSubscription(sub.subscriptionName))

}
