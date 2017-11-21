import java.util.concurrent.TimeUnit
import Config.SalesforceConfig
import okhttp3._
import play.api.libs.json.{JsPath, JsSuccess, Json, Reads}
import play.api.libs.functional.syntax._
import scalaz.{-\/, \/, \/-}

object SalesforceService extends Logging {

  val config = Config.salesforce

  case class SalesforceAuth(accessToken: String, instanceUrl: String)

  object SalesforceAuth {

    implicit val salesforceAuthReads: Reads[SalesforceAuth] = (
      (JsPath \ "access_token").read[String] and
      (JsPath \ "instance_url").read[String]
    ) (SalesforceAuth.apply _)

  }

  val restClient = new OkHttpClient().newBuilder()
    .readTimeout(15, TimeUnit.SECONDS)
    .build()

  def requestBuilder(config: SalesforceConfig, route: String): Request.Builder = {
    new Request.Builder()
      .url(s"${config.salesforceUrl}/$route")
  }

  def withSfAuth(requestBuilder: Request.Builder, salesforceAuth: SalesforceAuth): Request.Builder = {
    requestBuilder.addHeader("Authorization", s"Bearer ${salesforceAuth.accessToken}")
  }

  def authenticate(config: SalesforceConfig): String \/ SalesforceAuth = {
    val builder = requestBuilder(config, "/services/oauth2/token")
    val formBody = new FormBody.Builder()
      .add("client_id", config.salesforceClientId)
      .add("client_secret", config.salesforceClientSecret)
      .add("username", config.salesforceUsername)
      .add("password", config.salesforcePassword + config.salesforceToken)
      .add("grant_type", "password")
      .build()
    val request = builder.post(formBody).build()
    logger.info(s"Attempting to perform Salesforce Authentication")
    val response = restClient.newCall(request).execute()
    val responseBody = Json.parse(response.body().string())
    responseBody.validate[SalesforceAuth] match {
      case JsSuccess(result, _) =>
        logger.info(s"Successful Salesforce authentication.")
        \/-(result)
      case _ =>
        -\/(s"Failed to authenticate with Salesforce | body was: ${responseBody.toString}")
    }
  }

}