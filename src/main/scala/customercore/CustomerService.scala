package customercore
import zio.*

trait CustomerService {
  def get(id: Long): Task[Customer]

  def list(): Task[List[Customer]]
}

object CustomerService {
  val live: ZLayer[Any, Nothing, CustomerService] = ZLayer.succeed(new CustomerService {

    override def get(customerId: Long): Task[Customer] =
      if (customerId == 1) ZIO.succeed(Customer(1, "John Doo"))
      else ZIO.fail(new RuntimeException("Unknown customer id"))

    override def list(): Task[List[Customer]] =
      ZIO.succeed(List(Customer(1, "John Doo"), Customer(2, "Lily Foo")))
  })
}