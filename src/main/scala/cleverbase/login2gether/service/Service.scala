package cleverbase.login2gether.service

import cleverbase.login2gether.storage.Session
import cleverbase.login2gether.response.AuthInput
import cleverbase.login2gether.response.AuthResponse
import cats.syntax.all._
import cleverbase.login2gether.response.PermissionInput
import cleverbase.login2gether.response.PermissionOutput
import cleverbase.login2gether.domain.Username
import cleverbase.login2gether.response.SecretOutput
import sttp.model.StatusCode
import cleverbase.login2gether.response.SecretInput
import cats.effect.IO
import pdi.jwt.JwtCirce
import io.circe.syntax._
import io.circe.generic.auto._
import cleverbase.login2gether.domain.Secret
import scala.annotation.unused
import cleverbase.login2gether.domain.Token

class Service(var session: Session) {

  def authentication(authInput: AuthInput): IO[Either[StatusCode, AuthResponse]] = IO {
    val userToAuth = Username(authInput.username)
    val claims     = userToAuth.asJson
    if (session.users.isEmpty) {
      val (newSession, token) = Session.initalize(userToAuth)
      this.session = newSession
      AuthResponse(JwtCirce.encode(claims), token.value.some).asRight
    } else {
      authInput.token
        .filter(token => session.authenticate(userToAuth, Token(token)))
        .fold[Either[StatusCode, AuthResponse]](StatusCode.Unauthorized.asLeft) { token =>
          AuthResponse(JwtCirce.encode(claims), token.some).asRight
        }
    }
  }

  def putPermission(username: Username)(permissionInput: PermissionInput): IO[Either[StatusCode, PermissionOutput]] =
    IO {
      val newUser              = Username(permissionInput.username)
      val (newSession, result) = session.garantPermission(username, newUser)
      result.fold[Either[StatusCode, PermissionOutput]](StatusCode.NotFound.asLeft) { token =>
        this.session = newSession; PermissionOutput(token.value).asRight
      }
    }

  def putSecret(username: Username)(secretInput: SecretInput): IO[Either[StatusCode, StatusCode]] = IO {
    val sharedWith           = secretInput.accessFor.map(Username)
    val (newSession, result) = session.createSecret(username, Secret(secretInput.secret, sharedWith))
    if (result) { this.session = newSession; StatusCode.Ok.asRight }
    else StatusCode.NotFound.asLeft
  }

  def getMySecrets(requestIssuer: Username)(@unused unused: Unit): IO[Either[StatusCode, SecretOutput]] = IO {
    session
      .getSecretForUser(requestIssuer)
      .fold[Either[StatusCode, SecretOutput]](StatusCode.NotFound.asLeft) { secrets =>
        SecretOutput(secrets.map(_.secret)).asRight
      }
  }
  def getSecrets(requestIssuer: Username)(@unused unused: Unit): IO[Either[StatusCode, SecretOutput]] = IO {
    session
      .getSecretsSharedWithUser(requestIssuer)
      .fold[Either[StatusCode, SecretOutput]](StatusCode.NotFound.asLeft) { secrets =>
        SecretOutput(secrets.map(_.secret)).asRight
      }
  }

  import io.circe.parser.decode
  def securityLogic(jwt: String): IO[Either[StatusCode, Username]] =
    IO(
      JwtCirce
        .decode(jwt)
        .toEither
        .flatMap(jwtClaims => decode[Username](jwtClaims.content))
        .fold[Either[StatusCode, Username]](_ => StatusCode.Unauthorized.asLeft, username => username.asRight)
    )

}
