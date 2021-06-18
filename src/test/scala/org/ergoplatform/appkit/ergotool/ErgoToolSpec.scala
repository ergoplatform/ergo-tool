package org.ergoplatform.appkit.ergotool

import org.scalatest.{PropSpec, Matchers}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import java.nio.file.{Files, Paths}
import org.ergoplatform.appkit.cli.ConsoleTesting
import org.ergoplatform.appkit.cli.CommandsTesting

class ErgoToolSpec
  extends PropSpec
  with Matchers
  with ScalaCheckDrivenPropertyChecks
  with ConsoleTesting
  with CommandsTesting {

  // test values which correspond to each other (see also addr.json storage file, which is obtained using this values)
  val addrStr = "3WzR39tWQ5cxxWWX6ys7wNdJKLijPeyaKgx72uqg9FJRBCdZPovL"
  val mnemonic = "slow silly start wash bundle suffer bulb ancient height spin express remind today effort helmet"
  val mnemonicPassword = ""
  val storagePassword = "def"
  val publicKey = "03f56b14197c1d0f9bf8418ed8c57a3179d12d9af98745fbd0ab3b9dd6883d24a8"
  val secretKey = "18258e98ea87256806275b71cb203dc234752488e01985d405426e5c6f4ea1d1"
  val masterKey = "18258e98ea87256806275b71cb203dc234752488e01985d405426e5c6f4ea1d1efe92e5adfcaa6f61173108305f7e3ba4ec9643a81dffa347879cf4d58d2a10006000200000000"

  val responsesDir = "src/test/resources/mockwebserver"

  // NOTE, mainnet data is used for testing
  val testConfigFile = "ergo_tool_config.json"

  property("address command") {
    testCommand(ErgoTool, "address", Seq("testnet"),
      expectedConsoleScenario =
        s"""Enter Mnemonic>::$mnemonic;
           |Mnemonic password>::$mnemonicPassword;
           |$addrStr::;
           |""".stripMargin)
  }

  property("mnemonic command") {
    val res = runCommand(ErgoTool, "mnemonic", Nil, "")
    res.split(" ").length shouldBe 15
  }

  property("checkAddress command") {
    testCommand(ErgoTool, "checkAddress", Seq("testnet", addrStr),
      expectedConsoleScenario =
        s"""Enter Mnemonic>::$mnemonic;
           |Mnemonic password>::$mnemonicPassword;
           |Ok::;
           |""".stripMargin)
  }

  property("checkAddress command validates address format") {
    val res = runCommand(ErgoTool, "checkAddress", Seq("testnet", "someaddress"),
      expectedConsoleScenario =
        s"""Enter Mnemonic>::$mnemonic;
          |Mnemonic password>::$mnemonicPassword;
          |""".stripMargin)
    res should include ("Invalid address encoding, expected base58 string: someaddress")
  }

  property("checkAddress command validates network type") {
    val res = runCommand(ErgoTool, "checkAddress",
      args = Seq("testnet", "9f4QF8AD1nQ3nJahQVkMj8hFSVVzVom77b52JU7EW71Zexg6N8v"),
      expectedConsoleScenario =
        s"""Enter Mnemonic>::$mnemonic;
          |Mnemonic password> ::$mnemonicPassword;
          |""".stripMargin)
    res should include ("Network type of the address MAINNET don't match expected TESTNET")
  }

  property("listAddressBoxes command") {
    val data = MockData(
      Seq(
        loadNodeResponse("response_Box1.json"),
        loadNodeResponse("response_Box2.json"),
        loadNodeResponse("response_Box3.json"),
        loadNodeResponse("response_Box4.json")),
    Seq(
        loadExplorerResponse("response_boxesByAddressUnspent.json")))
    val res = runCommand(ErgoTool, "listAddressBoxes", Seq("9hHDQb26AjnJUXxcqriqY1mnhpLuUeC81C4pggtK7tupr92Ea1K"),
      expectedConsoleScenario = "", data)
    res should include ("d47f958b201dc7162f641f7eb055e9fa7a9cb65cc24d4447a10f86675fc58328")
    res should include ("e050a3af38241ce444c34eb25c0ab880674fc23a0e63632633ae14f547141c37")
    res should include ("26d6e08027e005270b38e5c5f4a73ffdb6d65a3289efb51ac37f98ad395d887c")
  }

  property("createStorage and extractStorage commands") {
    import ExtractStorageCmd._
    val storageDir = "storage"
    val storageFileName = "secret.json"
    val filePath = Paths.get(storageDir, storageFileName)
    try {
      // create a storage file
      testCommand(ErgoTool, "createStorage", Seq(storageDir, storageFileName),
        expectedConsoleScenario =
            s"""Enter Mnemonic Phrase>::$mnemonic;
              |Mnemonic password>::$mnemonicPassword;
              |Repeat Mnemonic password>::$mnemonicPassword;
              |Storage password>::$storagePassword;
              |Repeat Storage password>::$storagePassword;
              |Storage File: $filePath\n::;
              |""".stripMargin)

      // extract properties from the storage file
      Seq(
        PropAddress -> addrStr,
        PropPublicKey -> publicKey,
        PropMasterKey -> masterKey,
        PropSecretKey -> secretKey).foreach { case (propName, expectedValue) =>
        testCommand(ErgoTool, "extractStorage", Seq(filePath.toString, propName, "testnet"),
          expectedConsoleScenario =
            s"""Storage password>::$storagePassword;
              |$expectedValue\n::;
              |""".stripMargin)
        println(s"$propName: ok")
      }

      // try extract invalid property
      val res = runCommand(ErgoTool, "extractStorage", Seq(filePath.toString, "invalidProp", "testnet"),
        expectedConsoleScenario = s"ignored")
      res should include ("Please specify one of the supported properties")
    } finally {
      if (Files.exists(filePath)) Files.delete(filePath)
    }
  }

  property("send command") {
    val data = MockData(
      Seq(
        loadNodeResponse("response_Box1.json"),
        loadNodeResponse("response_Box2.json"),
        loadNodeResponse("response_Box3.json"),
        "21f84cf457802e66fb5930fb5d45fbe955933dc16a72089bf8980797f24e2fa1"),
      Seq(
        loadExplorerResponse("response_boxesByAddressUnspent.json")))
    val res = runCommand(ErgoTool, "send",
      args = Seq("storage/E2.json", "9f4QF8AD1nQ3nJahQVkMj8hFSVVzVom77b52JU7EW71Zexg6N8v", "1000000"),
      expectedConsoleScenario =
        s"""Storage password> ::abc;
          |""".stripMargin, data)
    println(res)
    res should include ("Server returned tx id: 21f84cf457802e66fb5930fb5d45fbe955933dc16a72089bf8980797f24e2fa1")
  }

}

