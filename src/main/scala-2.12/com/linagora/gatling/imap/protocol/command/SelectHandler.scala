package com.linagora.gatling.imap.protocol.command

import akka.actor.{ActorRef, Props}
import com.lafaspot.imapnio.async.client.ImapAsyncSession
import com.lafaspot.imapnio.async.request.SelectFolderCommand
import com.linagora.gatling.imap.protocol._
import io.gatling.core.akka.BaseActor

object SelectHandler {
  def props(session: ImapAsyncSession, tag: Tag) = Props(new SelectHandler(session, tag))
}

class SelectHandler(session: ImapAsyncSession, tag: Tag) extends BaseActor {

  override def receive: Receive = {
    case Command.Select(userId, mailbox) =>
      val listener = new RespondToActorIMAPCommandListener(self, userId, Response.Selected)(logger)
      context.become(waitCallback(sender()))
      listener.onResponse(session.execute(new SelectFolderCommand(mailbox)).get().getResponseLines)
  }

  def waitCallback(sender: ActorRef): Receive = {
    case msg@Response.Selected(response) =>
      sender ! msg
      context.stop(self)
  }

}
