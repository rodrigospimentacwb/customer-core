package customercore

import pureconfig.generic.derivation.default.*
import pureconfig.ConfigReader

case class HttpConfig(host: String, port: Int) derives ConfigReader