package me.lightspeed7.fireflies

import java.io.File
import com.xuggle.xuggler.IContainer
import com.xuggle.xuggler.ICodec
import com.xuggle.xuggler.IAudioSamples
import com.xuggle.xuggler.ICodec.ID
import com.xuggle.xuggler.IPixelFormat
import java.awt.image.BufferedImage
import java.awt.Color
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
    private[video] lazy val gr = {
      grActive = true
      image.createGraphics()
    }
    def dispose = if (grActive) gr.dispose()
  }
  case class ImageSlice(slice: BufferedImage, source: Image, x: Int, y: Int, xPos: Int, yPos: Int) {
    val w: Int = slice.getWidth
    val h: Int = slice.getHeight
  }

  def loadImage(filename: String): Try[BufferedImage] = Try(ImageIO.read(new FileInputStream(new File(filename))))
  def saveImage(img: BufferedImage, formatName: String, file: File): File = { ImageIO.write(img, formatName, file); file }

  def toTuple(rgb: Int): (Int, Int, Int) = ((rgb >> 16) & 0xff, (rgb >> 8) & 0xff, (rgb) & 0xff)
  def fromTuple(r: Int, g: Int, b: Int): Int = (r << 16) + (g << 8) + b

  def rgbDiff(left: Int, right: Int): Int = {
    val (r1, g1, b1) = toTuple(left)
    val (r2, g2, b2) = toTuple(right)
    Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2)
  }

  def colorDistance(left: Int, right: Int): Double = {
    val (r1, g1, b1) = toTuple(left)
    val (r2, g2, b2) = toTuple(right)
    Math.sqrt((r1 - r2) ^ 2 + (g1 - g2) ^ 2 + (b1 - b2) ^ 2)
  }

  def calcTranlucency(left: Int, right: Int, tolerance: Double): (Double, Double, Double) = {
    val (r1, g1, b1) = toTuple(left)
    val (r2, g2, b2) = toTuple(right)

    val lHSB = Seq(Color.RGBtoHSB(r1, g1, b1, null): _*)
    val rHSB = Seq(Color.RGBtoHSB(r2, g2, b2, null): _*)
    val deltas = (lHSB zip rHSB).map { case (l, r) => Math.abs(l - r) }.map(_ / tolerance).map(Math.min(1f, _))

    Tuple3(deltas(0), deltas(1), deltas(2))
  }

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

  def generateImageDiff(first: ImageSlice, second: ImageSlice, translucent: Double, threshold: Int): Option[ImageSlice] = {
    // convert images to pixel arrays...
    val w = first.slice.getWidth()
    val h = first.slice.getHeight()

    val diffs = for (
      x <- 0 to (w - 1);
      y <- 0 to (h - 1);
      xVal = first.slice.getRGB(x, y);
      yVal = second.slice.getRGB(x, y);
      diff = (x, y) if rgbDiff(xVal, yVal) > threshold
    ) yield {
      val cDiff = rgbDiff(xVal, yVal)
      val cDist = colorDistance(xVal, yVal)
      val cTrans = Option(calcTranlucency(xVal, yVal, translucent))
      println(f"Diff - ${cDiff}%6d  Dist - ${cDist}%2.5f Trans - ${cTrans}")
      diff
    }

    if (diffs.size == 0) None
    else {
      val slice = new BufferedImage(second.w, second.h, BufferedImage.TYPE_INT_RGB)
      val gr = slice.createGraphics()
      gr.setColor(Color.DARK_GRAY)
      gr.fillRect(0, 0, w, h)

      val clr = Color.MAGENTA.getRGB
      diffs.foreach { diff => slice.setRGB(diff._1, diff._2, clr) }

      gr.dispose()
      Some(second.copy(slice = slice))
    }

  }

  def updateImage(image: Image, slice: ImageSlice, offset: Int) = {
    image.gr.drawImage(slice.slice, slice.xPos, slice.yPos, slice.w, slice.h, Color.DARK_GRAY, null)

    image.gr.setColor(Color.MAGENTA)
    image.gr.drawRect(slice.xPos, slice.yPos, slice.w, slice.h);
    image.gr.drawRect(slice.xPos + 1, slice.yPos + 1, slice.w - 2, slice.h - 2);

  }

  def copyOriginal(from: String, to: String) = {
    import java.nio.file.StandardCopyOption.REPLACE_EXISTING
    import java.nio.file.Files.copy
    import java.nio.file.Paths.get

    implicit def toPath(filename: String) = get(filename)

    copy(from, to, REPLACE_EXISTING)
  }
  
  
}