package me.lightspeed7.fireflies.video

import com.xuggle.mediatool.MediaToolAdapter
import com.xuggle.mediatool.event.IVideoPictureEvent
import java.awt.image.BufferedImage
import java.io.File
import com.xuggle.mediatool.event.ICloseEvent

class DiffProcessorTool(outputBaseDir: File) extends MediaToolAdapter {

  var frameCount = 0
  val sliceSide = 20
  val file = new File(outputBaseDir, "diffMap.jpeg")

  var previous: Option[Image] = None
  var diffMap: Option[BufferedImage] = None

  override def onVideoPicture(event: IVideoPictureEvent): Unit = {
    frameCount = frameCount + 1

    val current = Image(event.getImage, frameCount)
    val height = event.getImage.getHeight()
    val width = event.getImage.getWidth()

    previous match {
      case None =>
      case Some(prev) => {
        val cols: Int = width / sliceSide
        val rows: Int = height / sliceSide

        val slices1 = imageSlicer(prev, rows, cols)
        val slices2 = imageSlicer(current, rows, cols)

        // generate all diffs
        val diff = (slices1 zip slices2) flatMap { case (l, r) => generateImageDiff(l, r, 0.1f, 100) }

        // find all non diffs 
        val nonDiffs = slices2.filterNot { s =>
          diff.map { ds =>
            ds.isSamePosition(s)
          }.foldLeft(false) { case (prev, cur) => prev || cur }
        }

        println(s"Total - ${slices2.size}  Diffs = ${diff.size}  Non = ${nonDiffs.size}")

        // update image with non-empty diffs
        diff.foreach { slice => borderSlice(current, slice) }
        nonDiffs.foreach { slice => grayOutSlice(current, slice) }

        // update the diff map 
        diff.foreach { slice => mergeDiff(current, slice) }
      }

    }

    // update the diff map 
    diffMap match {
      case None      => new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
      case Some(img) =>
    }

    // setup for next image  
    previous = Some(current)

    // call parent which will pass the video onto next tool in chain
    super.onVideoPicture(event)
  }

  override def onClose(event: ICloseEvent) = diffMap match {
    case None      =>
    case Some(img) => saveImage(img, "jpeg", file)
  }
}