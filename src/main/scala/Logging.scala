import InputReader.Example
import com.typesafe.scalalogging.StrictLogging

trait Logging extends StrictLogging {

  def logInfo(example: Example, message: String): Unit = {
    logger.info(s"${example.accountId}: $message")
  }

  def logError(example: Example, message: String): Unit = {
    logger.error(s"${example.accountId}: $message")
  }

  def logSuccessfulResult(example: Example): Unit = {
    logInfo(example, s"SUCCESSFUL processing")
  }

  def logFailureResult(example: Example, errorMessage: String): Unit = {
    logError(example, s"FAILURE during processing: $errorMessage")
  }

}
