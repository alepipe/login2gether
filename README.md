# Login2Gether

This is a simple solution that uses in-memory session, tapir to generate the
endpoints, http4s ember as a server, jwt-scala to manage a simple jwt, that is
used as authentication as bearer token, and circe for json
serialization/deserialization.

## Usage
It's a simple sbt project and we can run it with
```
sbt run
```

## How the system works
A unique Username and an issued Token are used as credentials to authenticate
the users. The first user, in order to attempt a login, does not need a token,
just the username. Then a randomly generated token is issued for subsequent
authentication.

To register a new User inside the system, an already authenticated user must
issue a permission with the new user Username, then inside the response we can
find the token that can be exploited by the new user to authenticate
themselves.

The private endpoints are accessible only with a bearer token that is
deserialized by the system. Inside the token we can find the Username of the
issuer of the request.

A user can register a secret and share it with a pool of users during the
secret creation. Then a user has the possibility both to get their own secrets,
and to get the secret that other users shared with them.

## What can be easily improved
The storage and the service layer have unit test, due to time constraint, the
solution lacks some endpoint testings and a good logging system, by now only
the http request are logged. Moreover a proper storage layer should be used,
and we might use the expiration attribute of the jwt to manage user sessions.
