package me.lightspeed7.fireflies.video

import java.awt.image.BufferedImage

import com.xuggle.mediatool.MediaToolAdapter
import com.xuggle.mediatool.event.{ IAudioSamplesEvent, IVideoPictureEvent }
import me.lightspeed7.fireflies.Color

class DeltaFilterTool(desired: java.awt.Color, threshold: Double, boxSide: Int) extends MediaToolAdapter {

  import DeltaFilterTool._

  var frameCount = 0

  val desiredRGB = desired.getRGB

  override def onVideoPicture(event: IVideoPictureEvent): Unit = {
    frameCount += 1
    convert(Image(event.getImage, frameCount), desiredRGB, threshold, boxSide)
    super.onVideoPicture(event)
  }

  override def onAudioSamples(event: IAudioSamplesEvent) = super.onAudioSamples(event)
}

object DeltaFilterTool {

  val outputRGB = java.awt.Color.MAGENTA

  def drawBox(image: Image, centerX: Int, centerY: Int, boxSide: Int) = {
    val offset: Int = boxSide / 2

    image.gr.setColor(outputRGB)
    image.gr.drawRect(centerX - offset, centerY - offset, boxSide, boxSide);
  }

  def convert(image: Image, desired: Int, threshold: Double, boxSide: Int): Image = {
    for (
      x <- 0 to (image.image.getWidth() - 1);
      y <- 0 to (image.image.getHeight() - 1);
      color: Int = image.image.getRGB(x, y);
      distance: Double = Color.CIE76Distance(color, desired);
      isGray: Boolean = Color.classify(color) == "Gray";
      diff: (Int, Int) = (x, y) if isGray && distance < threshold
    ) yield {
      //println(s"Drawing - ${diff} - ${boxSide} - distance ${distance}") //  - distance ${distance}
      drawBox(image, diff._1, diff._2, boxSide)
    }
    image
  }
}