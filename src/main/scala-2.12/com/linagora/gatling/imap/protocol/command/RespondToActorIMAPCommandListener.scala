package com.linagora.gatling.imap.protocol.command

import java.util

import akka.actor.ActorRef
import com.linagora.gatling.imap.protocol.{ImapResponses, Response, UserId}
import com.sun.mail.imap.protocol.IMAPResponse
import com.typesafe.scalalogging.Logger

import scala.collection.immutable.Seq

private[command] class RespondToActorIMAPCommandListener(self: ActorRef,
                                                         userId: UserId,
                                                         getResponse: ImapResponses => Response)(logger: Logger) {

  import collection.JavaConverters._

  def onResponse(responses: util.Collection[IMAPResponse]): Unit = {
    val response = ImapResponses(responses.asScala.to[Seq])
    logsOnResponse(response)
    self ! getResponse(response)
  }

  protected def logsOnResponse(response: ImapResponses): Unit = {
    logger.trace(s"On response for $userId :\n ${response.mkString("\n")}")
  }
}
