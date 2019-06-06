package com.linagora.gatling.imap.protocol

import javax.mail.Flags

object FlagsUtils {
  def toFlags(flags: Option[Seq[String]]) =  flags.map(flagsStr => flagsStr.foldLeft(new Flags())((acc, elem) => {
    val newFlags = new Flags(acc)
    newFlags.add(elem)
    newFlags
  })).getOrElse(new Flags())
}
