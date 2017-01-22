package me.lightspeed7.fireflies.video

import java.io.File
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.Matchers._
import org.scalatest.junit.JUnitRunner
import scala.util._
import java.awt.image.BufferedImage

@RunWith(classOf[JUnitRunner])
class VideoProcessingTest extends FunSuite {

  val dir = new File("/Users/david/Documents/fireflies")
  //    val image1 = loadImage("/Users/david/Pictures/tigger.jpeg").get
  //    val image2 = loadImage("/Users/david/Pictures/tigger2.jpeg").get
  val i1 = loadImage("/Users/david/Documents/fireflies/processed/GOPR0014/GOPR0014-0390.png").get
  val i2 = loadImage("/Users/david/Documents/fireflies/processed/GOPR0014/GOPR0014-0391.png").get
  val i3 = loadImage("/Users/david/Documents/fireflies/processed/GOPR0014/GOPR0014-0392.png").get
  val i4 = loadImage("/Users/david/Documents/fireflies/processed/GOPR0014/GOPR0014-0393.png").get
  val i5 = loadImage("/Users/david/Documents/fireflies/processed/GOPR0014/GOPR0014-0394.png").get
  val i6 = loadImage("/Users/david/Documents/fireflies/processed/GOPR0014/GOPR0014-0395.png").get
  val i7 = loadImage("/Users/david/Documents/fireflies/processed/GOPR0014/GOPR0014-0396.png").get
  val i8 = loadImage("/Users/david/Documents/fireflies/processed/GOPR0014/GOPR0014-0397.png").get

  ignore("get file info for all files in directory") {

    dir.isDirectory() should be(true)
    dir.exists() should be(true)

    import scala.collection.JavaConverters._

    val fileList = dir.listFiles().filter(_.getCanonicalPath.contains("MP4"))

    fileList.filter(_.isFile).toList.map { f =>
      println(s"File = ${f.getCanonicalPath}")

      f
    }.map(loadVideoFile(_)).map { video =>
      println(s"VideoFile = ${video}")
    }
  }

  ignore("image diff outputs diff in Magenta, alpha is dark gray") {

    val slice1 = ImageSlice(i1, Image(i1, 0), 0, 0, 0, 0)
    val slice2 = ImageSlice(i2, Image(i2, 0), 0, 0, 0, 0)
    val slice3 = ImageSlice(i3, Image(i3, 0), 0, 0, 0, 0)

    val diff = generateImageDiff(slice1, slice2, 0.1f, 100)
    diff should not be (None)

    val file = new File("/Users/david/Pictures/diff1.jpeg")
    diff.map { slice => saveImage(slice.slice, "jpeg", file) }

    val diff2 = generateImageDiff(slice2, slice3, 0.1f, 100)
    diff2 should not be (None)

    val file2 = new File("/Users/david/Pictures/diff2.jpeg")
    diff2.map { slice => saveImage(slice.slice, "jpeg", file2) }
  }

  ignore("image with combined diff") {

    val slices = Seq(i1, i2, i3, i4, i5, i6, i7, i8).map { i => ImageSlice(i, Image(i, 0), 0, 0, 0, 0) }

    val file = new File("/Users/david/Pictures/combinedDiff.jpeg")
    val image = Image(new BufferedImage(slices.head.w, slices.head.h, BufferedImage.TYPE_INT_RGB), 0)
    grayOutSlice(image, ImageSlice(image.image, image, 0, 0, 0, 0))
//    println(s"Slices size = ${slices.size}")
    slices.sliding(2).toSeq.map { pair =>
//      println(s"Processing pair - ${pair.size}")
      val diff = generateImageDiff(pair.head, pair.last, 0.1f, 100)
//      println("Processing diff")
      diff.map { diff => mergeDiff(image, diff) }
    }

    saveImage(image.image, "jpeg", file)
  }

  ignore("Split images into slices") {
    val img = loadImage("/Users/david/Pictures/tigger.jpeg").get
    val image = Image(img, 0)

    val w = img.getWidth
    val h = img.getHeight

    val sliceSide = 100
    val cols: Int = w / sliceSide
    val rows: Int = h / sliceSide

    val slices = imageSlicer(image, rows, cols)

    slices.map { slice =>
      saveImage(slice.slice, "jpeg", new File(s"/Users/david/Pictures/tigger_slice${slice.x}_${slice.y}.jpeg"))
    }
    image.dispose // not needed but safe
  }

  ignore("replace diff splice with real slice ") {
    val image1 = Image(i1, 0)
    val image2 = Image(i2, 0)

    val w = image1.image.getWidth
    val h = image1.image.getHeight

    val sliceSide = 20
    val cols: Int = w / sliceSide
    val rows: Int = h / sliceSide

    val slices1 = imageSlicer(image1, rows, cols)
    val slices2 = imageSlicer(image2, rows, cols)

    // generate all diffs
    val diff = (slices1 zip slices2) flatMap { case (l, r) => generateImageDiff(l, r, 0.1f, 100) }

    // find all non diffs 
    val nonDiffs = slices2.filterNot { s =>
      diff.toSeq.map { ds =>
        ds.isSamePosition(s)
      }.foldLeft(false) { case (prev, cur) => prev || cur }
    }

    println(s"Total - ${slices2.size}  Diffs = ${diff.size}  Non = ${nonDiffs.size}")

    // update image with non-empty diffs
    diff.foreach { slice => borderSlice(image2, slice) }
    nonDiffs.foreach { slice => grayOutSlice(image2, slice) }

    saveImage(image2.image, "jpeg", new File(s"/Users/david/Pictures/tigger_updated.jpeg"))

    image2.dispose
    image1.dispose
  }

}