package domain

import tagged.Tags.{ Id, IdCapabilities }

object UserId extends IdCapabilities[User] {
  protected val prefix: String = "usr-"
}

case class User(
  id:         Id[User],
  email:      String,
  first_name: String,
  last_name:  String
)
