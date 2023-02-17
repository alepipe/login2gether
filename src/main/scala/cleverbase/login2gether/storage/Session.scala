package cleverbase.login2gether.storage

import cleverbase.login2gether.domain._
import cats.syntax.all._

case class Session(users: List[User]) {

  import Session.SessionUpdated

  def authenticate(username: Username, token: Token): Boolean =
    users
      .find(_.username == username)
      .flatMap(_ => users.map(_.issuedPermissions).flatten.find(_.token == token))
      .isDefined

  def garantPermission(issuer: Username, newUser: Username): SessionUpdated[Option[Token]] = {
    val token = Token()
    users
      .find(_.username == issuer)
      .flatMap(_ => users.find(_.username == newUser).fold(().some)(_ => none[Unit]))
      .map(_ =>
        (users :+ User(newUser)).map {
          case User(username, secrets, permissions) if username == issuer =>
            User(username, secrets, permissions :+ Permission(token))
          case other => other
        }
      )
      .fold((this, none[Token]))(newUsersList => (Session(newUsersList), token.some))
  }

  def createSecret(username: Username, secret: Secret): SessionUpdated[Boolean] =
    users
      .find(_.username == username)
      .map(_ =>
        users.map {
          case User(user, secrets, permissions) if username == user =>
            User(username, secrets :+ secret, permissions)
          case other => other
        }
      )
      .fold((this, false))(newUserList => (Session(newUserList), true))

  def getSecretForUser(username: Username): Option[List[Secret]] = users.find(_.username == username).map(_.secret)

  def getSecretsSharedWithUser(username: Username): Option[List[Secret]] =
    users.find(_.username == username).map(_ => users.flatMap(_.secret).filter(_.sharedWith.contains(username)))

}

object Session {

  type SessionUpdated[T] = (Session, T)

  def initalize(username: Username): SessionUpdated[Token] = {
    val token = Token()
    (Session(List(User(username, List.empty[Secret], List(Permission(token))))), token)
  }
}
