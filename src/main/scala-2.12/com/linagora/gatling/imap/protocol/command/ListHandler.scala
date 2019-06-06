package com.linagora.gatling.imap.protocol.command

import akka.actor.{ActorRef, Props}
import com.lafaspot.imapnio.async.client.ImapAsyncSession
import com.lafaspot.imapnio.async.request.ListCommand
import com.linagora.gatling.imap.protocol._
import io.gatling.core.akka.BaseActor

import scala.util.{Failure, Success, Try}

object ListHandler {
  def props(session: ImapAsyncSession, tag: Tag) = Props(new ListHandler(session, tag))
}

class ListHandler(session: ImapAsyncSession, tag: Tag) extends BaseActor {

  override def receive: Receive = {
    case Command.List(userId, reference, name) =>
      val listener = new RespondToActorIMAPCommandListener(self, userId, Response.Listed)(logger)
      context.become(waitCallback(sender()))

      listener.onResponse(session.execute(   new ListCommand(reference, name)).get().getResponseLines)
  }

  def waitCallback(sender: ActorRef): Receive = {
    case msg@Response.Listed(response) =>
      sender ! msg
      context.stop(self)
  }

}
