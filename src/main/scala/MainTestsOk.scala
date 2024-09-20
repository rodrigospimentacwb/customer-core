import cats.syntax.all.*
import customercore.Routes.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import sttp.tapir.PublicEndpoint
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir.*
import zio.interop.catz.*
import zio.{ExitCode, Task, URIO, ZIO, ZIOAppDefault}

object MainTestsOk extends ZIOAppDefault:

  case class Pet(species: String, url: String)

  // Sample endpoint, with the logic implemented directly using .toRoutes
  val petEndpoint: PublicEndpoint[Int, String, Pet, Any] =
    endpoint.get.in("pet" / path[Int]("petId")).errorOut(stringBody).out(jsonBody[Pet])

  val petRoutes: HttpRoutes[Task] = ZHttp4sServerInterpreter()
    .from(petEndpoint.zServerLogic { petId =>
      if (petId == 35) {
        ZIO.succeed(Pet("Tapirus terrestris", "https://en.wikipedia.org/wiki/Tapir"))
      } else {
        ZIO.fail("Unknown pet id")
      }
    })
    .toRoutes

  val swaggerRoutes: HttpRoutes[Task] =
    ZHttp4sServerInterpreter()
      .from(SwaggerInterpreter().fromEndpoints[Task](List(petEndpoint), "Our pets", "1.0"))
      .toRoutes

  // Starting the server
  val serve: Task[Unit] =
    ZIO.executor.flatMap(executor =>
      BlazeServerBuilder[Task]
        .withExecutionContext(executor.asExecutionContext)
        .bindHttp(8080, "localhost")
        .withHttpApp(Router("/" -> (petRoutes <+> swaggerRoutes)).orNotFound)
        .serve
        .compile
        .drain
    )

  override def run: URIO[Any, ExitCode] = serve.exitCode

  /*
    Only MysqlConfig em HttpConfig

    val myProgram: ZIO[MysqlConfig & HttpConfig, Throwable, Unit] =
    for
      mysqlConfig <- ZIO.service[MysqlConfig]
      httpConfig  <- ZIO.service[HttpConfig]
      _           <- ZIO.succeed(println(s"MySQL Config: $mysqlConfig"))
      _           <- ZIO.succeed(println(s"HTTP Config: $httpConfig"))
      // Continue com a lógica do seu aplicativo
    yield ()

    override def run: ZIO[Any, Throwable, Unit] =
    myProgram.provide(
      ConfigLayer.mysqlConfigLayer,
      ConfigLayer.httpConfigLayer
    )
  */