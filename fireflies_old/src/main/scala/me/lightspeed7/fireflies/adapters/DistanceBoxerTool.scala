package me.lightspeed7.fireflies.adapters

import java.awt.image.BufferedImage

import com.xuggle.mediatool.MediaToolAdapter
import com.xuggle.mediatool.event.{ IAudioSamplesEvent, IVideoPictureEvent }

import me.lightspeed7.fireflies.video.Image
import me.lightspeed7.fireflies.Color

class DistanceBoxerTool(desired: java.awt.Color, threshold: Double, boxSide: Int = 20) extends MediaToolAdapter {

  import DistanceBoxerTool._

  var frameCount = 0

  val desiredRGB = desired.getRGB

  override def onVideoPicture(event: IVideoPictureEvent): Unit = {
    frameCount += 1
    convert(Image(event.getImage, frameCount), desiredRGB, threshold, boxSide)
    super.onVideoPicture(event)
  }

  override def onAudioSamples(event: IAudioSamplesEvent) = super.onAudioSamples(event)

}
object DistanceBoxerTool {

  val outputRGB = java.awt.Color.MAGENTA

  def drawBox(image: Image, centerX: Int, centerY: Int, boxSide: Int) = {
    val offset: Int = boxSide / 2

    image.gr.setColor(outputRGB)
    image.gr.drawRect(centerX - offset, centerY - offset, boxSide, boxSide);
  }

  def convert(image: Image, baseLine: Int, threshold: Double, boxSide: Int): Image = {
    val height = image.image.getHeight()
    val width = image.image.getWidth()

    for (
      x <- 0 to (width - 1);
      y <- 0 to (height - 1);
      color: Int = image.image.getRGB(x, y);
      isGray: Boolean = Color.classify(color) == "Gray";
      distance: Double = if (isGray) Color.CIE76Distance(color, baseLine) else threshold;
      diff: (Int, Int) = (x, y) if distance > threshold
    ) yield {
      println(s"Drawing - ${diff} - ${boxSide} - distance ${distance}") //  - distance ${distance}
      drawBox(image, diff._1, diff._2, boxSide)
    }
    image
  }
}