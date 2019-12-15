package com.example

//#user-registry-actor
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

import scala.collection.immutable
import scala.util.Try

//#user-case-classes
final case class UserName(value: String) {
  require(value.matches("[a-zA-Z ]+"), s"invalid name (アルファベットと空白のみ可): '$value'")
}
object UserName {
  def validateAndApply(rawName: String): Either[String, UserName] = {
    Try(UserName(rawName)).toEither.left.map { throwable =>
      Option(throwable.getMessage)
          // require が付与するメッセージを除去 (FIXME)
          .map(_.replaceFirst("requirement failed: ", ""))
          .getOrElse("unexpected error")
    }
  }
}
final case class User(name: UserName, age: Int, countryOfResidence: String)
final case class Users(users: immutable.Seq[User])
//#user-case-classes

object UserRegistry {
  // actor protocol
  sealed trait Command
  final case class GetUsers(replyTo: ActorRef[Users]) extends Command
  final case class CreateUser(user: User, replyTo: ActorRef[ActionPerformed]) extends Command
  final case class GetUser(name: UserName, replyTo: ActorRef[GetUserResponse]) extends Command
  final case class DeleteUser(name: UserName, replyTo: ActorRef[ActionPerformed]) extends Command

  final case class GetUserResponse(maybeUser: Option[User])
  final case class ActionPerformed(description: String)

  def apply(): Behavior[Command] = registry(Set.empty)

  private def registry(users: Set[User]): Behavior[Command] =
    Behaviors.receiveMessage {
      case GetUsers(replyTo) =>
        replyTo ! Users(users.toSeq)
        Behaviors.same
      case CreateUser(user, replyTo) =>
        replyTo ! ActionPerformed(s"User ${user.name.value} created.")
        registry(users + user)
      case GetUser(name, replyTo) =>
        replyTo ! GetUserResponse(users.find(_.name == name))
        Behaviors.same
      case DeleteUser(name, replyTo) =>
        replyTo ! ActionPerformed(s"User ${name.value} deleted.")
        registry(users.filterNot(_.name == name))
    }
}
//#user-registry-actor
