package customercore

import io.circe.generic.auto.*
import org.http4s.HttpRoutes
import sttp.tapir.*
import sttp.tapir.ztapir.RichZEndpoint
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.http4s.ztapir.*
import zio.*
import cats.syntax.semigroupk.*
import zio.interop.catz.*

trait Routes {
  def httpRoutes: HttpRoutes[Task]
  def endpoints: List[PublicEndpoint[_, _, _, _]]
}

object Routes {

  val live: ZLayer[CustomerService, Nothing, Routes] = ZLayer.fromFunction { (service: CustomerService) =>
    new Routes {

      val getCustomerEndpoint: PublicEndpoint[Long, String, Customer, Any] =
        endpoint.get.in("customers" / path[Long]("customerId")).errorOut(stringBody).out(jsonBody[Customer])

      val listCustomerEndpoint: PublicEndpoint[Unit, String, List[Customer], Any] =
        endpoint.get.in("customers").errorOut(stringBody).out(jsonBody[List[Customer]])

      override val httpRoutes: HttpRoutes[Task] = {
        val getCustomerRoute: HttpRoutes[Task] = ZHttp4sServerInterpreter()
          .from(
            getCustomerEndpoint.zServerLogic { id =>
              service.get(id).mapError(_.getMessage)
            }
          )
          .toRoutes

        val listCustomerRoute: HttpRoutes[Task] = ZHttp4sServerInterpreter()
          .from(
            listCustomerEndpoint.zServerLogic(_ => service.list().mapError(_.getMessage))
          )
          .toRoutes

        getCustomerRoute <+> listCustomerRoute
      }

      override val endpoints: List[PublicEndpoint[_, _, _, _]] = List(getCustomerEndpoint, listCustomerEndpoint)
    }
  }
}