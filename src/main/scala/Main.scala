import cats.syntax.all.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import zio.*
import customercore.{Routes, CustomerService}
import cats.syntax.semigroupk.*
import zio.interop.catz.*

object Main extends ZIOAppDefault {

  val program: ZIO[Routes, Throwable, Unit] = for {
    // Obter o Routes do ambiente ZIO
    routesService <- ZIO.service[Routes]

    // Obter os HttpRoutes
    apiRoutes = routesService.httpRoutes

    // Obter os endpoints para o Swagger
    endpoints = routesService.endpoints

    // Gerar os SwaggerRoutes
//    swaggerRoutes = ZHttp4sServerInterpreter()
//      .from(SwaggerInterpreter().fromEndpoints[Task](endpoints, "Customer API", "1.0"))
//      .toRoutes

    // Combinar os routes
    // httpApp = Router("/" -> (apiRoutes <+> swaggerRoutes)).orNotFound
    httpApp = Router("/" -> (apiRoutes)).orNotFound

    // Iniciar o servidor
    _ <- BlazeServerBuilder[Task]
      .bindHttp(8080, "localhost")
      .withHttpApp(httpApp)
      .serve
      .compile
      .drain

  } yield ()

  override def run: URIO[Any, ExitCode] =
    program
      .provide(
        CustomerService.live, // Fornece o CustomerService
        Routes.live            // Fornece o Routes que depende do CustomerService
      )
      .exitCode
}
