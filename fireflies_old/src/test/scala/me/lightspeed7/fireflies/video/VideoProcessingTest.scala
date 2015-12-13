package me.lightspeed7.fireflies.video

import java.io.File
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.Matchers._
import org.scalatest.junit.JUnitRunner
import scala.util._

@RunWith(classOf[JUnitRunner])
class DisplayFileInfoTest extends FunSuite {

  val dir = new File("/Users/david/Documents/firefiles")

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

  ignore("image diff sees nothing in the same image") {

    val i1 = loadImage("/Users/david/Pictures/tigger.jpeg").get
    val i2 = loadImage("/Users/david/Pictures/tigger2.jpeg").get

    val slice1 = ImageSlice(i1, Image(i1, 0), 0, 0, 0, 0)
    val slice2 = ImageSlice(i2, Image(i2, 0), 0, 0, 0, 0)

    val diff = generateImageDiff(slice1, slice2, 0.1f, 100)
    diff should not be (None)

    val file = new File("/Users/david/Pictures/tiggerDiff.jpeg")

    diff.map { slice => saveImage(slice.slice, "jpeg", file) }
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
    val image1 = Image(loadImage("/Users/david/Pictures/tigger.jpeg").get, 0)
    val image2 = Image(loadImage("/Users/david/Pictures/tigger2.jpeg").get, 0)

    val w = image1.image.getWidth
    val h = image1.image.getHeight

    val sliceSide = 100
    val cols: Int = w / sliceSide
    val rows: Int = h / sliceSide

    val slices1 = imageSlicer(image1, rows, cols)
    val slices2 = imageSlicer(image2, rows, cols)

    // generate all diffs
    val diff = (slices1 zip slices2) flatMap { case (l, r) => generateImageDiff(l, r, 0.1f, 100) }

    // update image with non-empty diffs
    diff.foreach { slice => updateImage(image2, slice, 10) }

    saveImage(image2.image, "jpeg", new File(s"/Users/david/Pictures/tigger_updated.jpeg"))

    image2.dispose
    image1.dispose
  }

}