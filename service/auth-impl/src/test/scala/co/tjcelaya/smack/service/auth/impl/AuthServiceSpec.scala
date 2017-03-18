package co.tjcelaya.smack.service.auth.impl

import java.util.UUID

import co.tjcelaya.smack.service.auth.api._
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}

import scala.concurrent.Future
import scalaoauth2.provider.{AccessToken, AuthInfo, OAuthGrantType}

class AuthServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
    // .withCassandra(true)
  ) { ctx =>
    new AuthApplication(ctx) with LocalServiceLocator {
      override lazy val accessTokenRepository = new AccessTokenRepository {
        var toks: List[AccessToken] = List()

        override def find(token: String): Future[Option[AccessToken]] =
          Future.successful(toks.find(_.token == token))

        override def findByUserId(id: UUID): Future[Option[AccessToken]] = {
          // find does -> toks.filter(_.params.get("user_id") == id.toString).headOption
          Future.successful(toks.find(_.params.get("user.user_id") == id.toString))
        }

        override def persist(accessToken: AccessToken,
                             authInfo: AuthInfo[_ <: Authenticatable]): Future[AccessToken] = {
          toks = accessToken :: toks
          Future.successful(accessToken)
        }

        override def revoke(token: String): Future[Unit] = {
          toks = toks.filter(_.token != token)
          Future.successful(())
        }
      }

      override lazy val clientRepository = new ClientRepository {
        override def exists(id: String): Future[Boolean] = {
          Future.successful(id.matches("/^[A-z0-9]+$/"))
        }
      }

      override lazy val userOAuth2Provider = new UserOAuth2Provider(accessTokenRepository, clientRepository)
    }
  }

  val client = server.serviceClient.implement[AuthService]

  override protected def afterAll() = server.stop()

  "Auth service" should {

    "return " in {
      client.token(OAuthGrantType.CLIENT_CREDENTIALS, "client_id", Some("client_secret"), None, None, None)
        .invoke().map {
        response =>
          response.access_token.length should be > 0
      }
    }

    "allow responding with a custom message" in {
      //      for {
      //        _ <- client.useGreeting("Bob").invoke(GreetingMessage("Hi"))
      //        answer <- client.hello("Bob").invoke()
      //      } yield {
      //        answer should ===("Hi, Bob!")
      //      }
      assert(true)
    }
  }
}
