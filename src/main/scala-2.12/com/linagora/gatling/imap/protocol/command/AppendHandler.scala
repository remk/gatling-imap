package com.linagora.gatling.imap.protocol.command

import akka.actor.{ActorRef, Props}
import com.lafaspot.imapnio.async.client.ImapAsyncSession
import com.lafaspot.imapnio.async.request.AppendCommand
import com.linagora.gatling.imap.protocol._
import io.gatling.core.akka.BaseActor
import javax.mail.Flags
import org.apache.commons.compress.utils.Charsets

object AppendHandler {
  def props(session: ImapAsyncSession, tag: Tag) = Props(new AppendHandler(session, tag))
}

class AppendHandler(session: ImapAsyncSession, tag: Tag) extends BaseActor {


  override def receive: Receive = {
    case Command.Append(userId, mailbox, flags, date, content) =>
      if (!date.isEmpty) throw new NotImplementedError("Date parameter for APPEND is still not implemented")
      val listener = new RespondToActorIMAPCommandListener(self, userId, Response.Appended)(logger)

      val length = content.length + content.lines.length - 1
      logger.debug(s"APPEND receive from sender ${sender.path} on ${self.path}")
      context.become(waitCallback(sender()))
      val cmd = new AppendCommand(mailbox, FlagsUtils.toFlags(flags), date.map(_.getTime).orNull, content.getBytes(Charsets.US_ASCII))
      listener.onResponse(session.execute(cmd).get().getResponseLines)
  }

  def waitCallback(sender: ActorRef): Receive = {
    case msg@Response.Appended(response) =>
      logger.debug(s"APPEND reply to sender ${sender.path}")
      sender ! msg
      context.stop(self)
  }

}

