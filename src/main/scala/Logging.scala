import com.typesafe.scalalogging.StrictLogging

trait Logging extends StrictLogging {

  def logInfo(subscriptionName: String, message: String): Unit = {
    logger.info(s"${subscriptionName}: $message")
  }

  def logError(subscriptionName: String, message: String): Unit = {
    logger.error(s"${subscriptionName}: $message")
  }

  def logSuccessfulResult(subscriptionName: String): Unit = {
    logInfo(subscriptionName, s"SUCCESSFUL processing")
  }

  def logFailureResult(subscriptionName: String, errorMessage: String): Unit = {
    logError(subscriptionName, s"FAILURE during processing: $errorMessage")
  }

}
