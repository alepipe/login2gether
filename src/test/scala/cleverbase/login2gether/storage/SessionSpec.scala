package cleverbase.login2gether.storage

import org.scalatest.wordspec.AsyncWordSpec
import org.scalatest.matchers.should.Matchers
import cleverbase.login2gether.domain._
import java.util.UUID
import org.scalatest.OptionValues
import cats.syntax.all._

class SessionSpec extends AsyncWordSpec with Matchers with OptionValues {

  val username         = Username("Username")
  val (session, token) = Session.initalize(username)

  "a session" should {
    "contain only one user whit is own token when initialized " in {
      val (resultSession, resultToken) = (session, token)
      resultSession.users should have size 1
      resultSession.users.head.username shouldBe username
      resultToken shouldBe resultSession.users.head.issuedPermissions.head.token
    }

    "return true when user can authenticate" in {
      session.authenticate(username, token) shouldBe true
    }

    "return false when receive wrongs credential" in {
      val (wrongUser, wrongToken) = (username.copy(value = "wrong"), token.copy(UUID.randomUUID()))
      session.authenticate(wrongUser, token) shouldBe false
      session.authenticate(username, wrongToken) shouldBe false
      session.authenticate(wrongUser, wrongToken) shouldBe false
    }

    "register the permission of a new user and make them login if the user is not present" in {
      val newUsername = Username("newUsername")

      val (newSession, newToken) = session.garantPermission(username, newUsername)
      newSession.authenticate(newUsername, newToken.value) shouldBe true

      val (anotherNewSession, anotherNewToken) = newSession.garantPermission(username, newUsername)
      (anotherNewSession should be).equals(newSession)
      anotherNewToken shouldBe none[Token]
    }

    "create secret for a user if the user exits otherwise return false" in {
      val secret               = Secret("secrete!", List.empty[Username])
      val (newSession, result) = session.createSecret(username, secret)
      newSession.users.map(_.secret).flatten should contain(secret)
      result shouldBe true

      val newUser = Username("newUser")
      session.createSecret(newUser, secret)._2 shouldBe false
    }

    "return the secret for a user, if any. if user do not exist return None" in {
      session.getSecretForUser(username).value shouldBe List.empty[Secret]
      val secret = Secret("secrete!", List.empty[Username])

      val (newSession, result) = session.createSecret(username, secret)
      result shouldBe true

      newSession.getSecretForUser(username).value shouldBe List(secret)

      val newUser = Username("newUser")
      session.getSecretForUser(newUser) shouldBe none[Secret]
    }

    "return all secret shared with user, if any. If user do not exist return None" in {
      val newUser = Username("newUser")
      session.getSecretForUser(newUser) shouldBe none[Secret]

      val (newSession, _) = session.garantPermission(username, newUser)
      newSession.getSecretForUser(newUser).value shouldBe List.empty[Secret]

      val secret1 = Secret("secrete!", List(newUser))
      val secret2 = Secret("an another secrete!", List(newUser))
      val secret3 = Secret("my secret!", List.empty[Username])
      val (anotherNewSession, result) =
        newSession.createSecret(username, secret1)._1.createSecret(username, secret2)._1.createSecret(newUser, secret3)

      result shouldBe true

      anotherNewSession.getSecretsSharedWithUser(newUser).value shouldBe List(secret1, secret2)
    }
  }

}
