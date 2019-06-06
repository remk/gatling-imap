package com.linagora.gatling.imap.protocol.command

import akka.actor.{ActorRef, Props}
import com.lafaspot.imapnio.async.client.ImapAsyncSession
import com.lafaspot.imapnio.async.request.FetchCommand
import com.linagora.gatling.imap.protocol._
import io.gatling.core.akka.BaseActor


abstract class FetchAttributes {
  def asString: String
}

object FetchAttributes {

  case class ALL() extends FetchAttributes {
    override def asString = "ALL"
  }

  case class FULL() extends FetchAttributes {
    override def asString = "FULL"
  }

  case class FAST() extends FetchAttributes {
    override def asString = "FAST"
  }

  case class AttributeList(fetchAttributes: String*) extends FetchAttributes {
    override def asString = fetchAttributes.mkString( " ")
  }

}

object FetchHandler {
  def props(session: ImapAsyncSession, tag: Tag) = Props(new FetchHandler(session, tag))
}

class FetchHandler(session: ImapAsyncSession, tag: Tag) extends BaseActor {

  override def receive: Receive = {
    case Command.Fetch(userId, sequence, attributes) =>
      val listener = new RespondToActorIMAPCommandListener(self, userId, Response.Fetched)(logger)
      context.become(waitCallback(sender()))
      listener.onResponse(session.execute(new FetchCommand(false,  sequence.asString, attributes.asString)).get().getResponseLines);
  }

  def waitCallback(sender: ActorRef): Receive = {
    case msg@Response.Fetched(response) =>
      sender ! msg
      context.stop(self)
  }

}
