package me.lightspeed7.fireflies

import java.io.File

import scala.util.{ Failure, Success }

import org.scalatest.FunSuite
import me.lightspeed7.fireflies.adapters.DistanceBoxerTool
import co.davidbuschman.fireflies.adapters.GridDrawingTool

class FrameDifferTest extends FunSuite {

  import me.lightspeed7.fireflies.video._

  val dir = new File("/Users/david/Documents/fireflies")
  val outputDir = new File(dir, "processed/GOPR0014")

  val frames: Seq[Int] = Seq(460 -> 469, 1114 -> 1119, 1170 -> 1179).flatMap { case (s, e) => (s to e).map(identity) }

  def genSlice(frame: Int): ImageSlice = {
    val buff1 = loadImage(new File(outputDir, f"GOPR0014-${frame}%04d.png").getCanonicalPath).map { bi => Image(bi, frame) }
    buff1 match {
      case Failure(ex) => fail(ex.getMessage)
      case Success(b1) => imageSlicer(b1, 1, 1).head
    }
  }

  def genDiff(first: ImageSlice, second: ImageSlice): Option[ImageSlice] = generateImageDiff(first, second, 0.3f, 300)

  def writeFile(slice: ImageSlice, output: File) = saveImage(slice.slice, "png", output)

  test("Distance Images") {

    import java.awt.{ Color => AwtColor }

    val desired = new AwtColor(90, 90, 90)
    //    val baseLine = new AwtColor(8, 8, 8)
    val boxSide = 30
    val threshold = 20.0

    val list = frames.map { frame => (frame, genSlice(frame)) }
    list.map {
      case (frame, slice) =>
        //        val filtered = DistanceBoxerTool.convert(slice.sliceAsImage, baseLine.getRGB, 100.0, boxSide)
        val filtered = DeltaFilterTool.convert(slice.sliceAsImage, desired.getRGB, threshold, boxSide)
        val gridded = GridDrawingTool.convert(slice.slice)
        val file = new File(outputDir, f"GOPR0014-${frame}%04d-diff.png")
        writeFile(slice, file)
    }

  }

}