object Script extends App with Logging {

  def processExample(id: String): Unit = {
    println(s"Processed $id")
  }

  // Script starts here
  val inputFile = "test.csv"
  logger.info(s"Starting script: using input file $inputFile")
  val examples = InputReader.readFile(inputFile)
  examples.foreach(example => processExample(example.accountId))

}
