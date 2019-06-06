package com.linagora.gatling.imap.scenario

import com.linagora.gatling.imap.PreDef.{contains, hasFolder, hasRecent, hasUid, imap, ok}
import com.linagora.gatling.imap.protocol.Uid
import com.linagora.gatling.imap.protocol.command.FetchAttributes.AttributeList
import com.linagora.gatling.imap.protocol.command.MessageRange.Last
import com.linagora.gatling.imap.protocol.command.MessageRanges
import io.gatling.core.Predef.{scenario, _}
import io.gatling.core.feeder.FeederBuilder
import io.gatling.core.structure.ScenarioBuilder

import scala.collection.immutable.Seq

object ImapAuthenticationScenario {

  def apply(feeder: FeederBuilder): ScenarioBuilder = scenario("ImapAuthentication")
    .feed(feeder)
    .exec(imap("Connect").connect()).exitHereIfFailed
    .exec(imap("login").login("${username}", "${password}").check(ok))
    .exec(imap("list").list("", "*").check(ok, hasFolder("INBOX")))
    .exec(imap("append").append(mailbox = "INBOX", flags = Some(Seq("\\Flagged")), date = None,
      content =
        """From: expeditor@example.com
        |To: recipient@example.com
        |Subject: test subject
        |
        |Test content
        |abcdefghijklmnopqrstuvwxyz
        |0123456789""".stripMargin.replaceAllLiterally("\n", "\r\n")).check(ok))
    .exec(imap("select").select("INBOX").check(ok, hasRecent(1)))
    .exec(imap("fetch").fetch(MessageRanges(Last()), AttributeList("BODY", "UID")).check(ok, hasUid(Uid(1)), contains("TEXT")))
//    .exec(imap("fetch").fetch(MessageRanges(Last()), AttributeList("BODY[TEXT]", "UID")).check(ok, hasUid(Uid(1)), contains("abcdefghijklmnopqrstuvwxyz")))
    .exec(imap("fetch").fetch(MessageRanges(Last()), AttributeList( "BODY[HEADER]", "UID", "BODY[TEXT]")).check(ok,contains("Test content")))

}
