package cleverbase.login2gether.route

import io.circe.generic.auto._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import cleverbase.login2gether.response.AuthInput
import cleverbase.login2gether.response.AuthResponse
import cleverbase.login2gether.response.SecretInput
import sttp.tapir._
import sttp.model.StatusCode
import cleverbase.login2gether.response.PermissionInput
import cleverbase.login2gether.response.PermissionOutput
import cleverbase.login2gether.response.SecretOutput

object Endpoint {

  private def secureEnedpoint = endpoint.securityIn(auth.bearer[String]())

  def postPermission: Endpoint[String, PermissionInput, StatusCode, PermissionOutput, Any] =
    secureEnedpoint.post
      .in("permissions" / jsonBody[PermissionInput])
      .out(jsonBody[PermissionOutput])
      .errorOut(statusCode)

  def postSecrtet: Endpoint[String, SecretInput, StatusCode, StatusCode, Any] =
    secureEnedpoint.post
      .in("secrets" / jsonBody[SecretInput])
      .out(statusCode)
      .errorOut(statusCode)

  def getSecrets: Endpoint[String, Unit, StatusCode, SecretOutput, Any] =
    secureEnedpoint.get
      .in("secrets")
      .out(jsonBody[SecretOutput])
      .errorOut(statusCode)

  def getMySecrets: Endpoint[String, Unit, StatusCode, SecretOutput, Any] =
    secureEnedpoint.get
      .in("mysecrets")
      .out(jsonBody[SecretOutput])
      .errorOut(statusCode)

  def authenticationEndpoin: PublicEndpoint[AuthInput, StatusCode, AuthResponse, Any] =
    endpoint.post.in("auth" / jsonBody[AuthInput]).out(jsonBody[AuthResponse]).errorOut(statusCode)

}
