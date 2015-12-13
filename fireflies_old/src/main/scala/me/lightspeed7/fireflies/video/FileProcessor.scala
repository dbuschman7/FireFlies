package me.lightspeed7.fireflies.video

import java.io.File
import com.xuggle.mediatool.ToolFactory
import co.davidbuschman.fireflies.writers.FileImageWriter
import co.davidbuschman.fireflies.adapters.GridDrawingTool
import co.davidbuschman.fireflies.adapters.TimeStampTool
import java.awt.image.BufferedImage
import scala.util.Try

class FileProcessor(file: File, outputBase: File) {

  val parts = file.getCanonicalPath.split('/').toSeq
  val fileName = parts.last
  val fileNameBase = fileName.split("\\.").apply(0)
  val outputDirectory: File = {
    val out = new File(outputBase, fileNameBase)
    if (!out.exists) out.mkdirs
    out
  }

  val outputFile = s"${outputDirectory.getCanonicalPath}/${fileNameBase}-output.mp4"
  //  val videoFile: VideoFile = loadVideoFile(file)

  val reader = {
    val r = ToolFactory.makeReader(file.getCanonicalPath)
    r.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
    r
  }

  val writer = ToolFactory.makeWriter(outputFile, reader);

  val pngWriter = new FileImageWriter(outputDirectory.getCanonicalPath + "/" + fileNameBase, "png")

  val grid = new GridDrawingTool
  val time = new TimeStampTool

  def processVideo = {
    val outCopy = new File(outputDirectory, fileName).getCanonicalPath
    copyOriginal(file.getCanonicalPath, outCopy)

    // wire up the listener chain 
    reader.addListener(grid)
    grid.addListener(time)
    time.addListener(writer)
    writer.addListener(pngWriter)

    // read all the frames
    try {
      while (reader.readPacket() == null)
        do {
        } while (false)
      writer.flush()
    } catch {
      case ex: Throwable => println("Exception - Writing file"); ex.printStackTrace()
    } finally {
      if (writer != null)
        Try(writer.close()).recover { case ex => println("Exception - closing file"); ex.printStackTrace() }
    }

  }

}