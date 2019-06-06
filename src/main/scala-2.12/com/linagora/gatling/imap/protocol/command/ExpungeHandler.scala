package com.linagora.gatling.imap.protocol.command

import akka.actor.{ActorRef, Props}
import com.lafaspot.imapnio.async.client.ImapAsyncSession
import com.lafaspot.imapnio.async.request.ExpungeCommand
import com.linagora.gatling.imap.protocol.{Response, _}
import io.gatling.core.akka.BaseActor

object ExpungeHandler {
  def props(session: ImapAsyncSession, tag: Tag) = Props(new ExpungeHandler(session, tag))
}

class ExpungeHandler(session: ImapAsyncSession, tag: Tag) extends BaseActor {

  override def receive: Receive = {
    case Command.Expunge(userId) =>
      val listener = new RespondToActorIMAPCommandListener(self, userId, Response.Expunged)(logger)
      context.become(waitCallback(sender()))
      val cmd = new ExpungeCommand
      session.execute(cmd)
  }

  def waitCallback(sender: ActorRef): Receive = {
    case msg@Response.Expunged(_) =>
      sender ! msg
      context.stop(self)
  }

}
