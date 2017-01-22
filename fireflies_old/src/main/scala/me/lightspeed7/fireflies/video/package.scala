package me.lightspeed7.fireflies

import java.io.File
import com.xuggle.xuggler.IContainer
import com.xuggle.xuggler.ICodec
import com.xuggle.xuggler.IAudioSamples
import com.xuggle.xuggler.ICodec.ID
import com.xuggle.xuggler.IPixelFormat
import java.awt.image.BufferedImage
import java.io.FileInputStream
import scala.util.Try
import javax.imageio.ImageIO
import com.xuggle.xuggler.IStreamCoder

package object video {

  import scala.concurrent.ExecutionContext.global

  // Raw data classes
  case class Metadata(streams: Int, fileSize: Long, duration: Long, bitRate: Long)
  case class VideoStream(codec: ID, width: Int, height: Int, format: IPixelFormat.Type, frameRate: BigDecimal, stream: IStreamCoder)
  case class AudioStream(codec: ID, sampleRate: Int, channels: Int, format: IAudioSamples.Format)
  case class VideoFile(val filename: String, meta: Metadata, video: Option[VideoStream], audio: Option[AudioStream])

  // Process Image classes
  case class Image(image: BufferedImage, frameNumber: Int) {
    private var grActive = false
    lazy val gr = {
      grActive = true
      image.createGraphics()
    }
    def dispose = if (grActive) gr.dispose()
  }
  case class ImageSlice(slice: BufferedImage, source: Image, x: Int, y: Int, xPos: Int, yPos: Int) {
    val w: Int = slice.getWidth
    val h: Int = slice.getHeight

    def sliceAsImage: Image = new Image(slice, source.frameNumber)
    def isSamePosition(s: ImageSlice): Boolean = (x == s.x) && (y == s.y) && (xPos == s.xPos) && (yPos == s.yPos)
  }

  def loadImage(filename: String): Try[BufferedImage] = Try(ImageIO.read(new FileInputStream(new File(filename))))
  def saveImage(img: BufferedImage, formatName: String, file: File): File = { ImageIO.write(img, formatName, file); file }

 
  def loadVideoFile(file: File): VideoFile = {

    val filename = file.getCanonicalPath

    // first we create a Xuggler container object
    val container = IContainer.make();

    // we attempt to open up the container
    val result = container.open(filename, IContainer.Type.READ, null);

    // check if the operation was successful
    if (result < 0)
      throw new RuntimeException("Failed to open media file");

    val numStreams = container.getNumStreams();

    var video: VideoStream = null
    var audio: AudioStream = null

    for (streamIndex <- 0 to numStreams - 1) {
      val stream = container.getStream(streamIndex)
      val coder = stream.getStreamCoder()

      val codecId = coder.getCodecID()

      coder.getCodecType() match {
        case ICodec.Type.CODEC_TYPE_VIDEO => video = VideoStream(codecId, coder.getWidth(), coder.getHeight(), coder.getPixelType(), coder.getFrameRate().getDouble(), coder);
        case ICodec.Type.CODEC_TYPE_AUDIO => audio = AudioStream(codecId, coder.getSampleRate(), coder.getChannels(), coder.getSampleFormat())
        case _                            =>
      }
    }

    val meta = Metadata(numStreams, container.getFileSize(), container.getDuration(), container.getBitRate())
    VideoFile(filename, meta, Option(video), Option(audio))
  }

  def imageSlicer(image: Image, rows: Int, cols: Int): Stream[ImageSlice] = {

    val chunks = rows * cols;
    val chunkWidth = image.image.getWidth() / cols; // determines the chunk width and height  
    val chunkHeight = image.image.getHeight() / rows;

    val pairs = for (
      x <- 0 to (rows - 1);
      y <- 0 to (cols - 1)
    ) yield (x, y)

    val slices = pairs.par.map {
      case (x, y) =>
        val slice = new BufferedImage(chunkWidth, chunkHeight, image.image.getType())
        val gr = slice.createGraphics()
        gr.drawImage(image.image, 0, 0, chunkWidth, chunkHeight, chunkWidth * y, chunkHeight * x, chunkWidth * y + chunkWidth, chunkHeight * x + chunkHeight, null)
        gr.dispose()
        ImageSlice(slice, image, x, y, chunkWidth * y, chunkHeight * x)
    }

    slices.toStream
  }

  val diffColor = java.awt.Color.MAGENTA.getRGB

  def generateImageDiff(first: ImageSlice, second: ImageSlice, translucent: Double, threshold: Int): Option[ImageSlice] = {
    // convert images to pixel arrays...
    val w = first.slice.getWidth()
    val h = first.slice.getHeight()

    val diffs = for (
      x <- 0 to (w - 1);
      y <- 0 to (h - 1);
      xVal = first.slice.getRGB(x, y);
      yVal = second.slice.getRGB(x, y);
      diff = (x, y) if Color.rgbDiff(xVal, yVal) > threshold
    ) yield {
      //      val cDiff = rgbDiff(xVal, yVal)
      //      val cDist = colorDistance(xVal, yVal)
      //      val cTrans = Option(calcTranlucency(xVal, yVal, translucent))
      //      println(f"Diff - ${cDiff}%6d  Dist - ${cDist}%2.5f Trans - ${cTrans}")
      diff
    }

    if (diffs.size == 0) None
    else {
      val slice = new BufferedImage(second.w, second.h, BufferedImage.TYPE_INT_RGB)
      diffs.foreach { diff =>
        val color = second.slice.getRGB(diff._1, diff._2)
        println(s"Diff color = ${color}")
        slice.setRGB(diff._1, diff._2, color)
      }
      Some(second.copy(slice = slice))
    }

  }

  def grayOutSlice(image: Image, slice: ImageSlice) = {
    image.gr.setColor(java.awt.Color.DARK_GRAY)
    image.gr.fillRect(slice.xPos, slice.yPos, slice.w, slice.h)
  }

  def borderSlice(image: Image, slice: ImageSlice) = {
    image.gr.setColor(java.awt.Color.MAGENTA)
    image.gr.drawRect(slice.xPos, slice.yPos, slice.w, slice.h);
  }

  def mergeDiff(image: Image, slice: ImageSlice) = {
    for {
      x <- 1 to slice.w - 1
      y <- 1 to slice.h - 1
      if (slice.slice.getRGB(x, y) == diffColor)
    } yield {
      //      println(s"Diff x= ${x} y = ${y}") 
      image.image.setRGB(slice.xPos + x - 1, slice.yPos + y - 1, diffColor)
    }
    //
    //    image.gr.drawImage(slice.slice, slice.xPos, slice.yPos, slice.w, slice.h, null)
  }

  def copyOriginal(from: String, to: String) = {
    import java.nio.file.StandardCopyOption.REPLACE_EXISTING
    import java.nio.file.Files.copy
    import java.nio.file.Paths.get

    implicit def toPath(filename: String) = get(filename)

    copy(from, to, REPLACE_EXISTING)
  }

}