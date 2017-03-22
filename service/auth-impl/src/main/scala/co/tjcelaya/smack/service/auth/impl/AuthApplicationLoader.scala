package co.tjcelaya.smack.service.auth.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.persistence.jdbc.{JdbcPersistenceComponents, JdbcSession}
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import co.tjcelaya.smack.service.auth.api.AuthService
import co.tjcelaya.smack.service.auth.impl.entity.{ClientEntity, HelloEntity}
import com.lightbend.lagom.internal.scaladsl.persistence.jdbc.JdbcSessionImpl
import com.softwaremill.macwire._
import play.api.{Environment, LoggerConfigurator}
import play.api.db.HikariCPComponents
import scredis._

class AuthApplicationLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new AuthApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication = {
//    val environment = context.playContext.environment
//    LoggerConfigurator(environment.classLoader).foreach { l =>
//      l.configure(environment)
//    }
    new AuthApplication(context) with LagomDevModeComponents
  }

  override def describeServices = List(
    readDescriptor[AuthService]
  )
}

abstract class AuthApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents
    with CassandraPersistenceComponents
    with HikariCPComponents {

  private val env: Environment = context.playContext.environment

  persistentEntityRegistry.register(wire[ClientEntity])
  persistentEntityRegistry.register(wire[HelloEntity])

  protected lazy val userRepository: UserRepository = wire[MySQLUserRepository]
  protected lazy val accessTokenRepository: AccessTokenRepository = wire[MySQLAccessTokenRepository]
  protected lazy val clientRepository: ClientRepository = wire[MySQLClientRepository]
  protected lazy val oauth2Provider: OAuth2Provider = wire[OAuth2Provider]

  // Bind the services that this server provides
  override lazy val lagomServer: LagomServer = LagomServer.forServices(
    bindService[AuthService].to(wire[AuthServiceImpl])
  )

  // Register the JSON serializer registry
   override lazy val jsonSerializerRegistry = AuthSerializerRegistry

  // Register the Service persistent entity
  // persistentEntityRegistry.register(wire[AuthEntity])
}
