package org.ergoplatform.appkit.ergotool

import org.ergoplatform.appkit.cli.AppContext
import org.ergoplatform.appkit.commands.{Cmd, CmdDescriptor, CmdParameter, RunWithErgoClient, StringPType}
import org.ergoplatform.appkit.config.ErgoToolConfig
import org.ergoplatform.appkit.ErgoClient
import scala.collection.convert.ImplicitConversions.`iterable AsScalaIterable`

/** Get the details about a boxId (value, tokens, registers and the ergotree)
  *
  * @param boxId string encoding of the address
  */
case class GetBoxInfoCmd(toolConf: ErgoToolConfig, name: String, boxId: String) extends Cmd with RunWithErgoClient {
  override def runWithClient(ergoClient: ErgoClient, runCtx: AppContext): Unit = {
    val res: String = ergoClient.execute(ctx => {
      val boxes = ctx.getBoxesById(boxId)
      val box = boxes(0)
      val lines = if (runCtx.isPrintJson) {
        box.toJson(false)
      } else {
        "BoxId: " + box.getId +
          "\nNanoERGs: " + box.getValue +
          "\nTokens: \n" + box.getTokens.map( e => s"    TokenId: ${e.getId} Amount: ${e.getValue}").mkString("\n") +
          "\nRegisters: \n" + box.getRegisters.map( r => s"    Type: ${r.getType} Value: ${r.getValue}").mkString("\n") +
          "\nErgoTree: " + box.getErgoTree
      }
      lines
    })
    runCtx.console.print(res)
  }
}
object GetBoxInfoCmd extends CmdDescriptor(
  name = "getBoxInfo", cmdParamSyntax = "<boxId>",
  description = "list box content with tokens and register details") {

  override val parameters: Seq[CmdParameter] = Array(
    CmdParameter("boxId", StringPType, "id of the box")
  )

  override def createCmd(ctx: AppContext): Cmd = {
    val Seq(boxId: String) = ctx.cmdParameters
    GetBoxInfoCmd(ctx.toolConf, name, boxId)
  }
}
