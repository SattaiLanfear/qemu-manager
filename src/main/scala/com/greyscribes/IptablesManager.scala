package com.greyscribes

import collection.JavaConversions._
import scalax.file.Path
import scalax.io._
import scalax.io.managed._
import scalax.io.Resource._
import java.net.NetworkInterface
import play.api.libs.json._
import play.api.data.validation.ValidationError

object IptablesManager extends App {

  val tmpfile = Path("/tmp/qemuout", '/')
  tmpfile.writeStrings(args, "\n")
  tmpfile.append("\n")

  val domain = args(0)
  val action = args(1)

  // Forward all non-loopback addresses
  val settings = Json.parse(Path("qemu.json").inputStream.reader(Codec.UTF8).string).as[Map[String, Entry]]

  // Only matters if the settings map contains the domain
  settings.get(domain).foreach { entry ⇒
    import IptableSupport._
    action match {
      case "stopped" ⇒ destroyRules(entry)
      case "start"   ⇒ setupRules(entry)
      case "reconnect" ⇒ {
        destroyRules(entry)
        setupRules(entry)
      }
    }
  }

  //System.exit(0)
}

object IptableSupport {
  
  val iptables = "/sbin/iptables"

  def setupRules(entry: Entry) = doRules(entry, "-I")

  def destroyRules(entry: Entry) = doRules(entry, "-D")

  def doRules(entry: Entry, tag: String) = {
    import scala.sys.process.Process
    import scala.language.postfixOps

    for {
      protocols ← entry.port_map
      protocol = protocols._1
      portpair ← protocols._2
      (publicPort, privatePort) = portpair
    } {
      // Determine Network Addresses
      def enableNatAndForwarding(publicIp: String) = {
        // Enable NAT
        Process(iptables, Seq[String](
          "-t", "nat", tag, entry.prerouting_chain.getOrElse("PREROUTING"),
          "-p", protocol,
          "-d", publicIp,
          "--dport", publicPort.toString,
          "-j", "DNAT", "--to", entry.private_ip + ":" + privatePort
        ))!
      }

      entry.public_ip.fold(
      for {
        interface ← NetworkInterface.getNetworkInterfaces
        if(entry.interface.map(_ != interface.getDisplayName).getOrElse(true))
        addr ← interface.getInetAddresses()
        if (!addr.isLoopbackAddress && addr.getAddress.length == 4)
      } {
        System.out.println("Adding Interface: " + interface.getDisplayName)
        enableNatAndForwarding(addr.getAddress.map(byte => (byte.intValue & 0xFF)).mkString("."))
      })(enableNatAndForwarding)

      // Enable Forwarding
      Process(iptables, Seq[String](
        "-t", "filter", tag, entry.forward_chain.getOrElse("FORWARD"),
        "-p", protocol,
        "--dport", privatePort.toString,
        "-j", "ACCEPT"
      ) ++ entry.interface.map(iface => Seq("-o", iface)).getOrElse(Seq.empty))!

      entry.input_chain.foreach { inputChain ⇒
        Process(iptables, Seq[String](
          "-t", "filter", tag, inputChain,
          "-p", protocol,
          "--dport", publicPort.toString,
          "-j", "ACCEPT"
        ))!
      }
    }

  }

}
