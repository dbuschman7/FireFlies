package me.lightspeed7.fireflies.video

import org.scalatest.FunSuite
import java.io.File
import co.davidbuschman.fireflies.FileHandling

class RenameProcessedFilesTest extends FunSuite {

  val dir = new File("/Users/david/Documents/fireflies")
  val output = new File(dir, "processed")

  ignore("Rename Processed output files") {

    val source = "grids"
    val sink = "detection"

    FileHandling.findAllFiles((in) => in.getCanonicalPath contains source)(output)
      .map { file =>
        val fileName = file.getCanonicalPath
        if (fileName contains source) {
          val renamedFile = fileName.replace(source, sink)
          println(fileName)
          println(renamedFile)
          println
          file.renameTo(new File(renamedFile)) match {
            case true  => println(s"Rename successful -${fileName}")
            case false => println(s"Rename failed     -${fileName}")
          }
        }
      }
  }
}