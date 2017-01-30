package me.lightspeed7.fireflies.video

import java.io.File

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration.Duration

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.Matchers.{ be, convertToAnyShouldWrapper }

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Sink, Source }
import org.scalatest.junit.JUnitRunner
import scala.concurrent.ExecutionContext

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

    import ExecutionContext.Implicits.global
    import scala.collection.JavaConverters._

    val fileList = dir.listFiles()
      .filter(_.isFile)
      .filter(_.getCanonicalPath.contains("MP4"))
    //      .filter { f => findInt(f) > 96 }

    implicit val system = ActorSystem("QuickStart")
    implicit val materializer = ActorMaterializer()

    val source = Source.fromIterator(() => fileList.toIterator)
    val sink = Sink.foreach { f: Any => println("Completed") }

    val flow = source.mapAsyncUnordered(3) { f =>
      println(s"Processing file - ${f.getCanonicalPath}")
      //      Future { new FileProcessor(f, output, true, "detection").processVideo }
      Future { new FileProcessor(f, output, false, "grids").processVideo }
    }.runWith(sink)

    Await.result(flow, Duration.Inf)

    //    val files = Seq( //
    //      "/Users/david/Documents/fireflies/GOPR0018.MP4" //
    //      , "/Users/david/Documents/fireflies/GOPR0019.MP4" //
    //      , "/Users/david/Documents/fireflies/GOPR0020.MP4" //
    //      )
    //
    //    val futures = files
    //      .map { f => new File(f) }
    //      .map { f =>
    //        println(s"Processing file - ${f.getCanonicalPath}")
    //        Future { new FileProcessor(f, output).processVideo }
    //      }
    //
    //    Await.result(Future.sequence(futures), Duration.Inf)

    //     process files
    //    fileList.map(loadVideoFile(_)).map { video =>
    //      println(s"VideoFile = ${video}")
    //    }
  }

}