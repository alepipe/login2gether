package cleverbase.login2gether

import cats.effect.{ExitCode, IO, IOApp}
import org.http4s.ember.server.EmberServerBuilder
import com.comcast.ip4s.IpLiteralSyntax
import org.http4s.HttpApp
import sttp.tapir.server.http4s.Http4sServerInterpreter
import cleverbase.login2gether.storage.Session
import cleverbase.login2gether.service.Service
import cleverbase.login2gether.route.Endpoint
import org.http4s.HttpRoutes
import org.http4s.server.middleware.Logger

object Main extends IOApp {

  val session = Session(List())
  val service = new Service(session)
  val httpApp: HttpRoutes[IO] = Http4sServerInterpreter[IO]()
    .toRoutes(
      List(
        Endpoint.authenticationEndpoin.serverLogic(service.authentication),
        Endpoint.postPermission.serverSecurityLogic(service.securityLogic).serverLogic(service.putPermission),
        Endpoint.postSecrtet.serverSecurityLogic(service.securityLogic).serverLogic(service.putSecret),
        Endpoint.getSecrets.serverSecurityLogic(service.securityLogic).serverLogic(service.getSecrets),
        Endpoint.getMySecrets.serverSecurityLogic(service.securityLogic).serverLogic(service.getMySecrets)
      )
    )

  private val finalHttpApp: HttpApp[IO] =
    Logger.httpApp(true, true)(httpApp.orNotFound)

  def run(args: List[String]): IO[ExitCode] =
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(finalHttpApp)
      .build
      .use { server =>
        for {
          _ <- IO.println(
            s"Server running on http://localhost:${server.address.getPort}. Press ENTER key to exit."
          )
          _ <- IO.readLine
        } yield ()
      }
      .as(ExitCode.Success)
}
