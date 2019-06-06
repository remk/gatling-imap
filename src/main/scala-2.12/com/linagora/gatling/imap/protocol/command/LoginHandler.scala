package com.linagora.gatling.imap.protocol.command

import akka.actor.{ActorRef, Props}
import com.lafaspot.imapnio.async.client.ImapAsyncSession
import com.lafaspot.imapnio.async.request.LoginCommand
import com.linagora.gatling.imap.protocol._
import io.gatling.core.akka.BaseActor

object LoginHandler {
  def props(session: ImapAsyncSession, tag: Tag) = Props(new LoginHandler(session, tag))
}

class LoginHandler(session: ImapAsyncSession, tag: Tag) extends BaseActor {

  override def receive: Receive = {
    case Command.Login(userId, user, password) =>
      val listener = new RespondToActorIMAPCommandListener(self, userId, Response.LoggedIn)(logger)
      logger.trace(s"LoginHandler for user : ${userId.value}, on actor ${self.path} responding to ${sender.path}")
      context.become(waitForLoggedIn(sender()))

      listener.onResponse(session.execute(new LoginCommand(user, "\"" + password + "\"")).get().getResponseLines)
  }

  def waitForLoggedIn(sender: ActorRef): Receive = {
    case msg@Response.LoggedIn(response) =>
      logger.trace(s"LoginHandler respond to ${sender.path} with $msg")
      sender ! msg
      context.stop(self)
  }

}

