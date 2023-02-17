package cleverbase.login2gether.service

import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import io.circe.syntax._
import io.circe.generic.auto._
import cleverbase.login2gether.storage.Session
import pdi.jwt.JwtCirce
import cleverbase.login2gether.domain._
import cats.syntax.all._
import sttp.model.StatusCode
import java.util.UUID
import cleverbase.login2gether.response._
import org.scalatest.EitherValues

class ServiceSpec extends AsyncWordSpec with AsyncIOSpec with Matchers with EitherValues {
  val usernameString   = "username"
  val username         = Username(usernameString)
  val (session, token) = Session.initalize(username)

  "securityLogic" should {
    val service = new Service(session)

    "return the username if the decoding of the jwt is succesfull" in {
      val jwtEncoded = JwtCirce.encode(username.asJson)
      service.securityLogic(jwtEncoded).asserting(_ shouldBe username.asRight)
    }
    "return an error if the decoding of the jwt is failing" in {
      val jwtEncoded = JwtCirce.encode("notAnEncodedUsername".asJson)
      service.securityLogic(jwtEncoded).asserting(_ shouldBe StatusCode.Unauthorized.asLeft)
    }
  }

  "authentication" should {
    val service = new Service(session)
    "create a new session if user is the first to login" in {
      val authInput          = AuthInput(usernameString, none[UUID])
      val justStardedSession = new Service(Session(List.empty[User]))
      justStardedSession.authentication(authInput).asserting(_.value shouldBe a[AuthResponse])
    }
    "return an error if the user is not registered" in {
      val authInput = AuthInput(usernameString, UUID.randomUUID().some)
      service.authentication(authInput).asserting(_ shouldBe StatusCode.Unauthorized.asLeft)
    }
    "return an  authentication if the user is registered" in {
      val putPermission = PermissionInput(username = "newUsername")
      service
        .putPermission(username)(putPermission)
        .flatMap { res =>
          val authInput = AuthInput(putPermission.username, res.value.token.some)
          service.authentication(authInput).asserting(_.value shouldBe a[AuthResponse])
        }
    }
  }

  "putPermission" should {
    val service = new Service(session)
    "put a permission if the user exist" in {
      val putPermission = PermissionInput(username = "newUsername")
      service.putPermission(username)(putPermission).asserting(_.value shouldBe a[PermissionOutput])
    }
    "return an error if the user does not exist" in {
      val putPermission = PermissionInput(username = "newUsername")
      service
        .putPermission(username)(putPermission)
        .flatMap(_ => service.putPermission(username)(putPermission))
        .asserting(_ shouldBe StatusCode.NotFound.asLeft)
    }
  }

  "putSecret" should {
    val service = new Service(session)
    "put a secret if the user exist" in {
      val secretInput = SecretInput("secret", List.empty[String])
      service
        .putSecret(username)(secretInput)
        .asserting(_.value shouldBe StatusCode.Ok)
    }
    "return an error if the user does not exist" in {
      val secretInput = SecretInput("secret", List.empty[String])
      service.putSecret(Username("newUser"))(secretInput).asserting(_ shouldBe StatusCode.NotFound.asLeft)
    }
  }

  "getMySecrets" should {
    val service = new Service(session)
    "put a secret if the user exist" in {
      val secretInput = SecretInput("secret", List.empty[String])
      val result = for {
        _   <- service.putSecret(username)(secretInput)
        res <- service.getMySecrets(username)(())
      } yield res
      result.asserting(_.value shouldBe SecretOutput(List(secretInput.secret)))
    }
    "return an error if the user does not exist" in {
      service
        .getMySecrets(Username("newUser"))(())
        .asserting(_ shouldBe StatusCode.NotFound.asLeft)
    }
  }

  "getSecrets" should {
    val service = new Service(session)
    "put a secret if the user exist" in {
      val newUser       = "newUsername"
      val newUsername   = Username(newUser)
      val putPermission = PermissionInput(username = "newUsername")
      val secretInput   = SecretInput("secret", List(newUser))
      val result = for {
        _   <- service.putPermission(username)(putPermission)
        _   <- service.putSecret(username)(secretInput)
        res <- service.getSecrets(newUsername)(())
      } yield res

      result.asserting(_.value shouldBe SecretOutput(List(secretInput.secret)))
    }
    "return an error if the user does not exist" in {
      service
        .getSecrets(Username("newUser"))(())
        .asserting(_ shouldBe StatusCode.NotFound.asLeft)
    }
  }

}
