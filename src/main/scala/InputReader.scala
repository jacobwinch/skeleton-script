import purecsv.unsafe.CSVReader

object InputReader extends Logging {

  case class Example(accountId: String)

  def readFile(filename: String): List[Example] = {
    val examples = CSVReader[Example].readCSVFromFileName(filename, skipHeader = true)
    logger.info(s"Reading ${examples.size} from $filename")
    examples
  }

}
