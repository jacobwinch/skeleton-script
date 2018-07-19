import purecsv.unsafe.CSVReader

object InputReader extends Logging {

  case class SubInput(subscriptionName: String)

  def readFile(filename: String): List[SubInput] = {
    val examples = CSVReader[SubInput].readCSVFromFileName(filename, skipHeader = true)
    logger.info(s"Reading ${examples.size} from $filename")
    examples
  }

}
