package customercore

import pureconfig.generic.derivation.default.*
import pureconfig.ConfigReader

case class MysqlConfig(
                        host: String,
                        port: Int,
                        user: String,
                        password: String,
                        database: String
                      ) derives ConfigReader
