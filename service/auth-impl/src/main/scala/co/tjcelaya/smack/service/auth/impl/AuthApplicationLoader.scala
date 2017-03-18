package co.tjcelaya.smack.service.auth.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import co.tjcelaya.smack.service.auth.api.AuthService
import com.softwaremill.macwire._
import play.api.LoggerConfigurator
import scredis._

class AuthApplicationLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new AuthApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication = {
    val environment = context.playContext.environment
    LoggerConfigurator(environment.classLoader).foreach {
      _.configure(environment)
    }
    new AuthApplication(context) with LagomDevModeComponents
  }

  override def describeServices = List(
    readDescriptor[AuthService]
  )
}

abstract class AuthApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents
    with HikariCPComponents
    with JdbcPersistenceComponents {

  // private lazy val redisClient = new Client()(this.actorSystem)

  // (this.actorSystem)
  // lazy val accessTokenRepository = new AccessTokenRepository(redisClient)

  protected lazy val accessTokenRepository: AccessTokenRepository = wire[MySQLAccessTokenRepository]
  protected lazy val clientRepository: ClientRepository = wire[ClientRepository]
  protected lazy val userOAuth2Provider: UserOAuth2Provider = wire[UserOAuth2Provider]

  // Bind the services that this server provides
  override lazy val lagomServer = LagomServer.forServices(
    bindService[AuthService].to(wire[AuthServiceImpl])
  )

  // Register the JSON serializer registry
   override lazy val jsonSerializerRegistry = AuthSerializerRegistry

  // Register the Service persistent entity
  // persistentEntityRegistry.register(wire[AuthEntity])
}
