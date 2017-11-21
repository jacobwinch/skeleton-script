import java.util.concurrent.TimeUnit
import Config.ZuoraConfig
import okhttp3._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import scalaz.\/
import scalaz.Scalaz._

object ZuoraService extends Logging {

  val config = Config.zuora

  val restClient = new OkHttpClient().newBuilder()
    .readTimeout(15, TimeUnit.SECONDS)
    .build()

  def buildRequest(config: ZuoraConfig, route: String): Request.Builder = {
    new Request.Builder()
      .addHeader("apiSecretAccessKey", config.password)
      .addHeader("apiAccessKeyId", config.username)
      .url(s"${config.baseUrl}/$route")
  }

  case class DefaultPaymentMethod(id: String, paymentMethodType: String)

  case class BasicAccountInfo(id: String, balance: Double, defaultPaymentMethod: DefaultPaymentMethod)

  case class AccountSummary(basicInfo: BasicAccountInfo, success: Boolean)

  implicit val defaultPaymentMethodReads: Reads[DefaultPaymentMethod] = (
    (JsPath \ "id").read[String] and
    (JsPath \ "paymentMethodType").read[String]
  )(DefaultPaymentMethod.apply _)

  implicit val basicAccountInfoReads: Reads[BasicAccountInfo] = (
    (JsPath \ "id").read[String] and
    (JsPath \ "balance").read[Double] and
    (JsPath \ "defaultPaymentMethod").read[DefaultPaymentMethod]
  )(BasicAccountInfo.apply _)

  implicit val accountSummaryReads: Reads[AccountSummary] = (
    (JsPath \ "basicInfo").read[BasicAccountInfo] and
    (JsPath \ "success").read[Boolean]
  )(AccountSummary.apply _)

  def convertResponseToCaseClass[T](accountId: String, response: Response)(implicit r: Reads[T]): String \/ T = {
    if (response.isSuccessful) {
      val bodyAsJson = Json.parse(response.body.string)
      bodyAsJson.validate[T] match {
        case success: JsSuccess[T] => success.get.right
        case error: JsError => {
          s"failed to convert Zuora response to case case. Response body was: \n ${bodyAsJson}".left
        }
      }
    } else {
      s"request to Zuora was unsuccessful, the response was: \n $response | body was: \n ${response.body.string}".left
    }
  }

  def getAccountSummary(accountId: String): String \/ AccountSummary = {
    logger.info(s"Getting account summary from Zuora for Account Id: $accountId")
    val request = buildRequest(config, s"accounts/$accountId/summary").get().build()
    val call = restClient.newCall(request)
    val response = call.execute
    convertResponseToCaseClass[AccountSummary](accountId, response)
  }

}
