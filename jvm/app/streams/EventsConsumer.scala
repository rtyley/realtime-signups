package streams

import java.util.UUID

import com.amazonaws.services.kinesis.clientlibrary.lib.worker.{InitialPositionInStream, KinesisClientLibConfiguration, Worker}
import controllers.AWSCredentialsProvider

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object EventsConsumer {

  // This application name is used by KCL to store the checkpoint data about how much of the stream you have consumed.
  val applicationName = "roberto-foolin" // s"${Config.stack}-${Config.app}-kinesis-${Config.stage}"

  val initialPosition = InitialPositionInStream.LATEST


  // Unique ID for the worker thread
  val workerId = UUID.randomUUID().toString

  val config = new KinesisClientLibConfiguration(
    applicationName,
    AnalyticsStream.name,
    AWSCredentialsProvider.Chain,
    workerId)
    .withInitialPositionInStream(initialPosition)
    .withRegionName(AnalyticsStream.region)

  // Create a worker, which will in turn create one or more EventProcessors
  val worker = new Worker(EventProcessorFactory, config)

  def start() = Future {
    worker.run()
  }

  def stop() = {
    worker.shutdown()
  }

}
