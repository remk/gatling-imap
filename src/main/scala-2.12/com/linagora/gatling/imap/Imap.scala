package com.linagora.gatling.imap

import java.util

import com.lafaspot.imapnio.async.client.{ImapAsyncSession, ImapFuture}
import com.lafaspot.imapnio.async.request.{CreateFolderCommand, LoginCommand}
import com.sun.mail.imap.protocol.IMAPResponse

import scala.concurrent.{ExecutionContext, Future, Promise}

object Imap {

  case class TaggedResponse(tag: String, responses: List[IMAPResponse])

  def login(login: String, password: String)(implicit session: ImapAsyncSession, executionContext: ExecutionContext): Future[Any] =
    executeCommand(() => session.execute(new LoginCommand(login, password)))

  def createMailbox(name: String)(implicit session: ImapAsyncSession, executionContext: ExecutionContext): Future[Any] =

    executeCommand(() => session.execute(new CreateFolderCommand(name)))

  def disconnect()(implicit session: ImapAsyncSession, executionContext: ExecutionContext): Future[Unit] =
    Future.successful(session.close().get())

  private def executeCommand(command: () => ImapFuture[_])(implicit executionContext: ExecutionContext) = {
    val taggedResponse = Promise[Any]()
    command.apply().get()
    taggedResponse.success()
    taggedResponse.future
  }
}
