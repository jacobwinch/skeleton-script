
object Config {

  case class ZuoraConfig(username: String, password: String, baseUrl: String)

  val zuora = ZuoraConfig(
      System.getenv("zuoraUser"),
      System.getenv("zuoraPass"),
      System.getenv("zuoraUrl")
    )

}

