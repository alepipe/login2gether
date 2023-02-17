package cleverbase.login2gether.response

import java.util.UUID

case class AuthInput(username: String, token: Option[UUID])
