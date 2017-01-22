package me.lightspeed7.fireflies.video

import java.io.File

import com.xuggle.mediatool.MediaToolAdapter
import com.xuggle.mediatool.event.IVideoPictureEvent
import javax.imageio.ImageIO

class FrameFetcherTool(outputBaseDir: File, baseFileName: String, desiredRanges: (Int, Int)*) extends MediaToolAdapter {

  var frameNumber = 0;
  val ranges: Seq[Range] = desiredRanges.map { case (s, e) => Range.inclusive(s, e) }

  private def toFile(frame: Int): File = new File(outputBaseDir, f"${baseFileName}-${frame}%04d.png")

  @annotation.tailrec
  private def keep(frame: Int, ranges: Seq[Range] = ranges): Boolean = ranges.size match {
    case 0 => false
    case _ =>
      ranges.head.contains(frame) match {
        case true  => true
        case false => if (ranges.size == 0) false else keep(frame, ranges.tail)
      }
  }

  override def onVideoPicture(event: IVideoPictureEvent): Unit = {
    frameNumber += 1
    if (keep(frameNumber)) {
      ImageIO.write(event.getImage(), "png", toFile(frameNumber))
    }
  }
}
