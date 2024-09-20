package customercore

import zio.*

object ApenasConsulta extends ZIOAppDefault {

  case class User(name: String, email: String)

  class UserSubscription(emailService: EmailService, userDataBase: UserRepository) {
    def subscribeUser(user: User): Task[Unit] =
      for {
        _ <- emailService.sendEmail(user)
        _ <- userDataBase.insert(user)
      } yield ()
  }

  // Companion Object (Factory for UserSubscription, to instantiate UserSubscription, like a static in Java)
  object UserSubscription {
    def create(emailService: EmailService, userRepository: UserRepository) =
      new UserSubscription(emailService, userRepository)

    val layer: ZLayer[EmailService with UserRepository, Nothing, UserSubscription] =
      ZLayer.fromFunction(create _)
  }

  class EmailService {
    def sendEmail(user: User): Task[Unit] =
      ZIO.succeed(println(s"Welcome ${user.name} to my site."))
  }

  object EmailService {
    def create(): EmailService = new EmailService

    val layer: ZLayer[Any, Nothing, EmailService] =
      ZLayer.succeed(create())
  }

  class UserRepository(connectionPool: ConnectionPool) {
    def insert(user: User): Task[Unit] =
    for {
      conn <- connectionPool.get
      _ <- conn.runQuery(s"insert into subscribers(name, email) values (${user.name}, ${user.email})")
    } yield ()
  }

  object UserRepository {
    def create(connectionPool: ConnectionPool) =
      new UserRepository(connectionPool)

    val layer: ZLayer[ConnectionPool, Nothing, UserRepository] =
      ZLayer.fromFunction(create _)
  }

  class ConnectionPool(nConnections: Int) {
    def get: Task[Connection] =
      ZIO.succeed(println("Acquired connection")) *> ZIO.succeed(Connection())
  }

  object ConnectionPool {
    def create(nConnections: Int) =
      new ConnectionPool(nConnections)

    def layer(nConnections: Int): ZLayer[Any, Nothing, ConnectionPool] =
      ZLayer.succeed(create(nConnections))
  }

  case class Connection() {
    def runQuery(query: String): Task[Unit] =
      ZIO.succeed(println(s"Executing query $query"))
  }

  // This example doesn't use Dependency Injection
  /*
    val subscriptionService = ZIO.succeed(
      new UserSubscription(
        new EmailService,
        new UserRepository(
          new ConnectionPool(10)
        )
      )
    )
  */

  /* This is a Clean Dependency Injection but has disadvantages

  - does not scale for many service, is very complicated to debug
  - This DI can be 100x worse, developer has responsibility to organize this services
  - Every time this service is called in a comprehensions (for { ...Aeh paraben), it will instantiate all the services and consume much more resources.

    val subscriptionService = ZIO.succeed(
      UserSubscription.create(
        EmailService.create(),
        UserRepository.create(
          ConnectionPool.create(10)
        )
      )
    )
  */

  /**
   * ZLayers
   */
//  val connectionPoolLayer: ZLayer[Any, Nothing, ConnectionPool] = ZLayer.succeed(ConnectionPool.create(10))
//  val userRepositoryLayer: ZLayer[ConnectionPool, Nothing, UserRepository] = ZLayer.fromFunction(UserRepository.create _)
//  val emailServiceLayer = ZLayer.succeed(EmailService.create())
//  val userSubscriptionLayer = ZLayer.fromFunction(UserSubscription.create _)

  // composing layers

  // vertical composition example using '>>>'
  // val userRepositoryLayerFull: ZLayer[Any, Nothing, UserRepository] = connectionPoolLayer >>> userRepositoryLayer

  // horizontal composition: combine dependencies of both layers and Values of both layers
  //val subscriptionRequirementsLayer: ZLayer[Any, Nothing, UserRepository & EmailService] = userRepositoryLayerFull ++ emailServiceLayer

  // best practice: write "factory" methods exposing layers ih the companion objects of the services

  def subscribe(user: User) = for {
      sub <- ZIO.service[UserSubscription]
      _ <- sub.subscribeUser(user)
    } yield ()

  val program = for {
    _ <- subscribe(User("Rodrigo", "rodrigo.pepper@teste.com"))
    _ <- subscribe(User("Ludmila", "ludmila.pepper@teste.com"))
  } yield ()

  // Magic ZLayer
  val userSubscriptionLayer: ZLayer[Any, Nothing, UserSubscription] = ZLayer.make[UserSubscription](
    UserSubscription.layer,
    EmailService.layer,
    UserRepository.layer,
    ConnectionPool.layer(10)
  )

  def run = program.provideLayer(userSubscriptionLayer)
}
