package com.linagora.gatling.imap.protocol.command

import akka.actor.{ActorRef, Props}
import com.lafaspot.imapnio.async.client.{ImapAsyncClient, ImapAsyncSession}
import com.lafaspot.imapnio.async.request.FetchCommand
import com.linagora.gatling.imap.protocol._
import com.sun.mail.imap.protocol.MessageSet
import io.gatling.core.akka.BaseActor

object UIDFetchHandler {
  def props(session: ImapAsyncSession, tag: Tag) = Props(new UIDFetchHandler(session, tag))
}

class UIDFetchHandler(session: ImapAsyncSession, tag: Tag) extends BaseActor {

  override def receive: Receive = {
    case Command.UIDFetch(userId, sequence, attributes) =>
      val listener = new RespondToActorIMAPCommandListener(self, userId, Response.Fetched)(logger)
      context.become(waitCallback(sender()))
      listener.onResponse(session.execute(new FetchCommand(true,  sequence.asString, attributes.asString)).get().getResponseLines);
  }

  def waitCallback(sender: ActorRef): Receive = {
    case msg@Response.Fetched(response) =>
      sender ! msg
      context.stop(self)
  }

}
