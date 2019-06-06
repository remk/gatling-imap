package com.linagora.gatling.imap

import java.net.URI
import java.util.Properties

import com.lafaspot.imapnio.async.client.{ImapAsyncClient, ImapAsyncSession}
import com.lafaspot.logfast.logging
import com.lafaspot.logfast.logging.LogManager
import com.lafaspot.logfast.logging.internal.LogPage
import org.slf4j.Logger

import scala.concurrent.{Future, Promise}

trait ImapTestUtils {


  protected def logger: Logger

  val threadNumber = 4
  val config = new Properties()
  val imapClient = new ImapAsyncClient(threadNumber)

  def connect(port: Int): Future[ImapAsyncSession] = {
    val promise = Promise[ImapAsyncSession]
    withConnectedSession(port)(promise.success(_))
    promise.future
  }

  def withConnectedSession(port: Int)(f: ImapAsyncSession => Unit): ImapAsyncSession = {
    val uri = new URI(s"imap://localhost:$port")
    val logManager = new LogManager(logging.Logger.Level.TRACE, LogPage.DEFAULT_SIZE)
    logManager.setLegacy(true)
    val session = imapClient.createSession(uri, config, null, null).get()
    logger.trace(s"connection to $uri")
    f(session)
    session
  }
}
