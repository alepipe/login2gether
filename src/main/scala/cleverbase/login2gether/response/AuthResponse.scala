package cleverbase.login2gether.response

import java.util.UUID

case class AuthResponse(bearer: String, token: Option[UUID])
