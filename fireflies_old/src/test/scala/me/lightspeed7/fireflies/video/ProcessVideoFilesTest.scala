package me.lightspeed7.fireflies.video

import java.io.File

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.Matchers._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ProcessVideoFilesTest extends FunSuite {

  val dir = new File("/Users/david/Documents/fireflies")
  val output = new File(dir, "processed")

  def findInt(in: File): Int = {
    val name = in.getName
    val end = name.lastIndexOf('.')
    name.substring(4, end).toInt
  }

  test("get file info for all files in directory") {

    dir.isDirectory() should be(true)
    dir.exists() should be(true)

    import scala.collection.JavaConverters._

    val fileList = dir.listFiles()
      .filter(_.isFile)
      .filter(_.getCanonicalPath.contains("MP4"))
      .filter { f => findInt(f) == 14 }
      .map { f =>
        println(s"Processing file - ${f.getCanonicalPath}")
        new FileProcessor(f, output).processVideo
        f
      }

    //     process files
    //    fileList.map(loadVideoFile(_)).map { video =>
    //      println(s"VideoFile = ${video}")
    //    }
  }

}