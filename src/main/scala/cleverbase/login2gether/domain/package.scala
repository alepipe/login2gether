package cleverbase.login2gether.domain

import java.util.UUID

case class Token(value: UUID) extends AnyVal
object Token {
  def apply(): Token = Token(UUID.randomUUID())
}

case class Username(value: String) extends AnyVal

case class User(username: Username, secret: List[Secret], issuedPermissions: List[Permission])
object User {
  def apply(username: Username): User = User(username, List.empty[Secret], List.empty[Permission])
}

case class Secret(secret: String, sharedWith: List[Username])

case class Permission(token: Token)
