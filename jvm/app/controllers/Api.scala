package controllers

import java.nio.ByteBuffer
import javax.inject.{Inject, _}

import akka.actor._
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth.{AWSCredentialsProviderChain, InstanceProfileCredentialsProvider}
import com.amazonaws.regions.Regions
import com.amazonaws.services.kinesis.producer.{KinesisProducer, KinesisProducerConfiguration}
import play.api.inject.ApplicationLifecycle
import play.api.libs.EventSource
import play.api.mvc._
import shared.AutowiredApi
import streams.{AnalyticsStream, EventsConsumer}
import upickle._
import upickle.default._

import scala.concurrent.Future

object AWSCredentialsProvider {
  val Dev = new ProfileCredentialsProvider("developerPlayground")
  val Prod = new InstanceProfileCredentialsProvider(false)
  val Chain = new AWSCredentialsProviderChain(Dev, Prod)
}

object AutowireServer extends autowire.Server[Js.Value, Reader, Writer]{
  def read[Result: Reader](p: Js.Value) = upickle.default.readJs[Result](p)
  def write[Result: Writer](r: Result) = upickle.default.writeJs(r)
}

@Singleton
class Api @Inject() (lifecycle: ApplicationLifecycle)(implicit system: ActorSystem) extends Controller with shared.AutowiredApi {

  EventsConsumer.start()
  lifecycle.addStopHook { () =>
    Future.successful(EventsConsumer.stop())
  }

  implicit val mat = ActorMaterializer()
  implicit val ec = system.dispatcher

  val producer = new KinesisProducer(
    new KinesisProducerConfiguration()
        .setRegion(Regions.EU_WEST_1.getName)
        .setCredentialsProvider(AWSCredentialsProvider.Chain)
  )


  val (actorRef, publisher) =
    Source.actorRef[TweetInfo](1000, OverflowStrategy.dropHead).toMat(Sink.asPublisher(true))(Keep.both).run()

  val actorPublisher = Source.fromPublisher(publisher).log("word up dog").map(_.whatever)


  def autowireApi(path: String) = Action.async(parse.json) { implicit request =>
    val autowireRequest = autowire.Core.Request(
      path.split("/"),
      upickle.json.read(request.body.toString()).asInstanceOf[Js.Obj].value.toMap
    )

    AutowireServer.route[AutowiredApi](this)(autowireRequest).map(responseJS => {
      Ok(upickle.json.write(responseJS))
    })
  }


  def mixedStream = Action {
    //Source.fromPublisher()
    // val keywordSources = Source("Hey,boy,you,look,good,when,you,conform".split(",").toList)
    // val responses = keywordSources.flatMapMerge(10, queryToSource)
    Ok.chunked(actorPublisher via EventSource.flow)
  }

  override def makeAHit(): Future[String] = {
    actorRef ! TweetInfo("Whatever")

    producer.addUserRecord(
      AnalyticsStream.name,
      "myReallyPartitionyPartitionKey",
      ByteBuffer.wrap("myData".getBytes("UTF-8"))
    )
    Future.successful("Totally sent it")
  }


}
