package org.ergoplatform.appkit.ergotool

import org.ergoplatform.appkit.cli.AppContext
import org.ergoplatform.appkit.commands.{AddressPType, Cmd, CmdDescriptor, CmdParameter, RunWithErgoClient}
import org.ergoplatform.appkit.config.ErgoToolConfig
import org.ergoplatform.appkit.{Address, ErgoClient, InputBox}

import scala.collection.convert.ImplicitConversions.`iterable AsScalaIterable`

/** Lists tokens (can be NFTs or Tokens) belonging to the given address.
  *
  * @param address string encoding of the address
  */
case class ListAddressTokensCmd(toolConf: ErgoToolConfig, name: String, address: Address) extends Cmd with RunWithErgoClient {
  override def runWithClient(ergoClient: ErgoClient, runCtx: AppContext): Unit = {
    val res: String = ergoClient.execute(ctx => {
      val boxes = ctx.getUnspentBoxesFor(address)
      val boxesWithTokens: Iterable[InputBox] = boxes.filter(_.getTokens.size() > 0)
      val lines = if (runCtx.isPrintJson) {
        val ret = for (b <- boxesWithTokens) yield {
          val tokens = b.getTokens
          tokens.map(t => "{\"tokenId\":\"" + t.getId + "\",\"amount\":" + t.getValue + "}" ).mkString("", ",\n", "")
        }
        ret.mkString("[", ",\n", "]")
      } else {
        val ret = for (b <- boxesWithTokens) yield {
                val tokens = b.getTokens
                tokens.map(t => s"${t.getId} ${t.getValue}").mkString("", "\n", "")
             }
        "TokenId                                                          Amount          \n" +
        ret.mkString("", "\n", "")
      }
      lines
    })
    runCtx.console.print(res)
  }
}
object ListAddressTokensCmd extends CmdDescriptor(
  name = "listAddressTokens", cmdParamSyntax = "<address>",
  description = "list tokens owned by the given <address>") {

  override val parameters: Seq[CmdParameter] = Array(
    CmdParameter("address", AddressPType, "string encoding of the address")
  )

  override def createCmd(ctx: AppContext): Cmd = {
    val Seq(address: Address) = ctx.cmdParameters
    ListAddressTokensCmd(ctx.toolConf, name, address)
  }
}
