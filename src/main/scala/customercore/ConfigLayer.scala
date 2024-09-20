package customercore

import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderException
import zio.*

object ConfigLayer {

  val mysqlConfigLayer: Layer[Throwable, MysqlConfig] = ZLayer.fromZIO(
    ZIO
      .fromEither(ConfigSource.default.at("mysql").load[MysqlConfig])
      .mapError(failures => ConfigReaderException[MysqlConfig](failures))
  )

  val httpConfigLayer: Layer[Throwable, HttpConfig] = ZLayer.fromZIO(
    ZIO
      .fromEither(ConfigSource.default.at("http").load[HttpConfig])
      .mapError(failures => ConfigReaderException[HttpConfig](failures))
  )

}
