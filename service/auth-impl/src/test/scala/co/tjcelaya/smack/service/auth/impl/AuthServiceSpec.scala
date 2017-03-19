package co.tjcelaya.smack.service.auth.impl

import co.tjcelaya.smack.service.auth.api._
import com.lightbend.lagom.scaladsl.api.transport.TransportException
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalatest._

import scala.concurrent.Future
import scalaoauth2.provider.OAuthGrantType

class AuthServiceSpec extends AsyncFlatSpec with Matchers with BeforeAndAfterAll with BeforeAndAfterEach {

  trait ConfigurableServer {
    def getAccessTokenRepository: AccessTokenRepository

    def getClientRepository: StubClientRepository

    def getUserRepository: StubUserRepository
  }

  def testWithServer(block: (ConfigurableServer, AuthService) => Future[Assertion]): Future[Assertion] = {
    ServiceTest.withServer(
      ServiceTest.defaultSetup // .withCassandra(true)
    ) { ctx =>
      new AuthApplication(ctx) with LocalServiceLocator with ConfigurableServer {
        override lazy val accessTokenRepository = new StubAccessTokenRepository
        override lazy val clientRepository = new StubClientRepository
        override lazy val userRepository = new StubUserRepository

        override lazy val oauth2Provider =
          new OAuth2Provider(accessTokenRepository, clientRepository, userRepository)

        override def getAccessTokenRepository = accessTokenRepository

        override def getClientRepository = clientRepository

        override def getUserRepository = userRepository
      }
    } { server =>
      block(server.application, server.serviceClient.implement[AuthService])
    }
  }

  it should "remember tokens it has created and check the client secret" in {
    testWithServer { (server: ConfigurableServer, client: AuthService) =>
      server.getClientRepository.clients = List(Client(new ClientId("best_client1"), "", "best_secret1"))

      client.token(OAuthGrantType.CLIENT_CREDENTIALS, "best_client1", Some("best_secret1"))
        .invoke()
        .flatMap {
          response =>
            response.access_token.length should be > 0
            response.access_token should include regex "\\S+"

            client.checkToken.invoke(response.access_token).flatMap {
              checkResponse =>
                checkResponse shouldEqual true
            }
        }
    }
  }

  it should "reject unknown clients" in {
    testWithServer { (server: ConfigurableServer, client: AuthService) =>
      recoverToSucceededIf[TransportException] {
        client.token(OAuthGrantType.CLIENT_CREDENTIALS, "best_client").invoke()
      }
    }
  }

  it should "handle user auth" in {
    testWithServer { (server: ConfigurableServer, client: AuthService) =>

      val (cid, csecret) = server.getClientRepository.seed
      val (uname, passwd) = server.getUserRepository.seed

      client.token(OAuthGrantType.PASSWORD, cid, Some(csecret), Some(uname), Some(passwd))
        .invoke()
        .flatMap {
          response =>
            response.params("auth_type") shouldEqual "user"
        }
    }
  }

  it should "support extra params" in {
    testWithServer { (server: ConfigurableServer, client: AuthService) =>
      val (cid, csecret) = server.getClientRepository.seed
      val (uname, passwd) = server.getUserRepository.seed

      client.token(OAuthGrantType.PASSWORD, cid, Some(csecret), Some(uname), Some(passwd))
        .invoke()
        .flatMap {
          response =>
            response.params("auth_type") shouldEqual "user"
        }

      client.token(OAuthGrantType.CLIENT_CREDENTIALS, cid, Some(csecret))
        .invoke()
        .flatMap {
          response =>
            response.params("auth_type") shouldEqual "client"
        }
    }
  }
}