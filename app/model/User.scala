package model

import com.gu.contentatom.thrift.{User => ThriftUser}
import org.cvogt.play.json.Jsonx

case class User(email: String, firstName: Option[String], lastName: Option[String]) {
  def asThrift = ThriftUser(email, firstName, lastName)
}

object User {
  implicit val userFormat = Jsonx.formatCaseClassUseDefaults[User]
  def fromThrift(user: ThriftUser) = User(user.email, user.firstName, user.lastName)
}