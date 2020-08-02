package me.lightspeed7.fireflies.video

import java.awt.image.BufferedImage
import java.io.File

import scala.util.Try
import com.xuggle.mediatool.{IMediaReader, IMediaWriter, ToolFactory}
import co.davidbuschman.fireflies.adapters.TimeStampTool

class FileProcessor(file: File, outputBase: File, detection:Boolean, tag: String) {

  import java.awt.{ Color => AwtColor }

  val parts: Seq[String] = file.getCanonicalPath.split('/').toSeq
  val fileName: String = parts.last
  val fileNameBase: String = fileName.split("\\.").apply(0)
  val outputDirectory: File = {
    val out = new File(outputBase, fileNameBase)
    if (!out.exists) out.mkdirs
    out
  }

  val outputFile = s"${outputDirectory.getCanonicalPath}/$fileNameBase-$tag.mp4"
  val diffOutFile = s"${outputDirectory.getCanonicalPath}/$fileNameBase-diffs.mp4"

  //  val videoFile: VideoFile = loadVideoFile(file)

  val reader: IMediaReader = {
    ToolFactory.setTurboCharged(true)

    val r = ToolFactory.makeReader(file.getCanonicalPath)
    r.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR)
    r
  }

  val writer: IMediaWriter = ToolFactory.makeWriter(outputFile, reader)
  //  val diffWriter = ToolFactory.makeWriter(diffOutFile, reader);

//  val grid = new GridDrawingTool
  val time = new TimeStampTool

  def processVideo(): Unit = {
    val outCopy = new File(outputDirectory, fileName).getCanonicalPath
    copyOriginal(file.getCanonicalPath, outCopy)

    // wire up the listener chain 
    val processing = if (detection) {
      val desired = new AwtColor(180, 180, 180)
      //      val output = new AwtColor(8, 8, 8)
      val threshold = 10.0
      val boxSide = 30
      val filter = new DeltaFilterTool(desired, threshold, boxSide)
      reader.addListener(filter)
      filter
    } else {
      reader
    }

    processing.addListener(time)
//    time.addListener(grid)
    time.addListener(writer)
//    grid.addListener(writer)

    // read all the frames
    try {
      while (reader.readPacket() == null)
        do {
        } while (false)
      reader.close()
      writer.flush()
    } catch {
      case ex: Throwable => println("Exception - Writing file"); ex.printStackTrace()
    } finally {
      Thread.sleep(1000)
      if (writer != null)
        Try(writer.close()).recover { case ex => println("Exception - closing file"); ex.printStackTrace() }
    }

  }

}