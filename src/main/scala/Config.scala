
object Config {

  case class ZuoraConfig(username: String, password: String, baseUrl: String)

  case class SalesforceConfig(salesforceUrl: String,
                              salesforceClientId: String,
                              salesforceClientSecret: String,
                              salesforceUsername: String,
                              salesforcePassword: String,
                              salesforceToken: String
                             )

  val zuora = ZuoraConfig(
      System.getenv("zuoraUser"),
      System.getenv("zuoraPass"),
      System.getenv("zuoraUrl")
    )

  val salesforce = SalesforceConfig(
      salesforceUrl = System.getenv("salesforceUrl"),
      salesforceClientId = System.getenv("salesforceClientId"),
      salesforceClientSecret = System.getenv("salesforceClientSecret"),
      salesforceUsername = System.getenv("salesforceUsername"),
      salesforcePassword = System.getenv("salesforcePassword"),
      salesforceToken = System.getenv("salesforceToken")
    )

}

