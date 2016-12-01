package streams;

import java.util.{List => JList}

import com.amazonaws.services.kinesis.clientlibrary.interfaces.{IRecordProcessor, IRecordProcessorCheckpointer, IRecordProcessorFactory}
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.ShutdownReason
import com.amazonaws.services.kinesis.model.Record

class EventProcessor() extends IRecordProcessor {

  private[this] var shardId: String = _

  override def initialize(shardId: String): Unit = {
    this.shardId = shardId
    println(s"Initialized an event processor for shard $shardId")
  }

  override def processRecords(records: JList[Record], checkpointer: IRecordProcessorCheckpointer): Unit = {
    println(s"got ${records.size()}")
    checkpointer.checkpoint()
  }

  // This method may be called by KCL, e.g. in case of shard splits/merges
  override def shutdown(checkpointer: IRecordProcessorCheckpointer, reason: ShutdownReason): Unit = {
    if (reason == ShutdownReason.TERMINATE) {
      checkpointer.checkpoint()
    }
    println(s"Shutdown event processor for shard $shardId because $reason")
  }
}


object EventProcessorFactory extends IRecordProcessorFactory {
  override def createProcessor(): EventProcessor = new EventProcessor()
}