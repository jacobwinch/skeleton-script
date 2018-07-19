import java.time.LocalDate
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

  case class Subscription(name: String, currentTerm: Int, currentTermPeriodType: String, status: String, ratePlans: List[RatePlan])

  case class RatePlan(productName: String, ratePlanCharges: List[RatePlanCharge])

  case class RatePlanCharge(name: String, chargedThroughDate: Option[LocalDate])

  case class UpdateResult(success: Boolean, subscriptionId: String)

  implicit val ratePlanChargeReads: Reads[RatePlanCharge] = (
    (JsPath \ "name").read[String] and
    (JsPath \ "chargedThroughDate").readNullable[LocalDate]
  )(RatePlanCharge.apply _)

  implicit val ratePlanReads: Reads[RatePlan] = (
    (JsPath \ "productName").read[String] and
    (JsPath \ "ratePlanCharges").read[List[RatePlanCharge]]
  )(RatePlan.apply _)

  implicit val subscriptionReads: Reads[Subscription] = (
    (JsPath \ "subscriptionNumber").read[String] and
    (JsPath \ "currentTerm").read[Int] and
    (JsPath \ "currentTermPeriodType").read[String] and
    (JsPath \ "status").read[String] and
    (JsPath \ "ratePlans").read[List[RatePlan]]
  )(Subscription.apply _)

  implicit val amendmentResultReads: Reads[UpdateResult] = (
    (JsPath \ "success").read[Boolean] and
    (JsPath \ "subscriptionId").read[String]
  )(UpdateResult.apply _)

  def convertResponseToCaseClass[T](response: Response)(implicit r: Reads[T]): String \/ T = {
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

  def getSubscription(subscriptionName: String): String \/ Subscription = {
    logInfo(subscriptionName, s"getting subscripton from Zuora")
    val request = buildRequest(config, s"subscriptions/$subscriptionName").get().build()
    val call = restClient.newCall(request)
    val response = call.execute
    convertResponseToCaseClass[Subscription](response)
  }

  def createAmendment(subscriptionName: String, numberOfDays: Long): String \/ UpdateResult = {
    logInfo(subscriptionName, s"creating amendment in Zuora")
    val body = Json.obj("currentTermPeriodType" -> "Day", "currentTerm" -> numberOfDays)
    val requestBody = RequestBody.create(MediaType.parse("application/json"), body.toString)
    val request = buildRequest(config, s"subscriptions/$subscriptionName").put(requestBody).build()
    val call = restClient.newCall(request)
    val response = call.execute
    convertResponseToCaseClass[UpdateResult](response)
  }

}
