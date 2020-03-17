package org.ergoplatform.appkit.ergotool

import org.ergoplatform.appkit.commands._
import org.ergoplatform.appkit.RestApiErgoClient
import org.ergoplatform.appkit.cli.{Console, CliApplication}



/** ErgoTool implementation, contains main entry point of the console application.
  *
  * @see instructions in README to generate native executable
  */
object ErgoTool extends CliApplication {
  /** Commands supported by this application. */
  override def commands: Seq[CmdDescriptor] = super.commands ++ Array(
    AddressCmd, MnemonicCmd, CheckAddressCmd,
    ListAddressBoxesCmd,
    CreateStorageCmd, ExtractStorageCmd, SendCmd
  )

  /** Main entry point of console application. */
  def main(args: Array[String]): Unit = {
    val console = Console.instance
    run(args, console, clientFactory = { ctx =>
      RestApiErgoClient.create(ctx.apiUrl, ctx.networkType, ctx.apiKey)
    })
  }

}

