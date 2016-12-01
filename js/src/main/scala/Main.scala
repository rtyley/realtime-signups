import autowire._
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.html.Canvas
import shared.AutowiredApi
import upickle.Js
import upickle.default.{readJs, writeJs, _}

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import scala.util.Random

object Ajaxer extends autowire.Client[Js.Value, Reader, Writer]{
  override def doCall(req: Request): Future[Js.Value] = {
    val url = "/api/" + req.path.mkString("/")
    val jsonPayload = upickle.json.write(Js.Obj(req.args.toSeq: _*))

    Ajax.post(url, jsonPayload, 0,
      Map("X-Requested-With" -> "XMLHttpRequest",
        "Content-Type" -> "application/json"), withCredentials = true)
      .map(_.responseText)
      .map(upickle.json.read)
  }

  def read[Result: Reader](p: Js.Value) = readJs[Result](p)
  def write[Result: Writer](r: Result) = writeJs(r)
}

object Main extends js.JSApp {
  def main(): Unit = {
    import org.scalajs.dom

    type Ctx2D =
      dom.CanvasRenderingContext2D

    val c = dom.document.createElement("canvas").asInstanceOf[Canvas]
    val ctx = c.getContext("2d").asInstanceOf[Ctx2D]

    c.width = (0.95 * dom.window.innerWidth).toInt
    c.height = (0.95 * dom.window.innerHeight).toInt
    dom.document.body.appendChild(c)

    val w = c.width / 2
//    c.width = w
//    c.height = w

    ctx.strokeStyle = "red"
    ctx.lineWidth = 3
    ctx.beginPath()
    ctx.moveTo(w / 3, 0)
    ctx.lineTo(w / 3, w / 3)
    ctx.moveTo(w * 2 / 3, 0)
    ctx.lineTo(w * 2 / 3, w / 3)
    ctx.moveTo(w, w / 2)
    ctx.arc(w / 2, w / 2, w / 2, 0, 3.14)

    ctx.stroke()

    val eventSource = new dom.EventSource("/boom")

    eventSource.onmessage = {
      (message: dom.MessageEvent) =>
        ctx.fillText(message.data.toString, Random.nextInt(300), Random.nextInt(300))
        println(message.data)
    }
    val lib = new MyLibrary
    println(lib.sq(2))
  }

  @JSExport
  def addClickedMessage() {
    Ajaxer[AutowiredApi].makeAHit().call()
  }
}
