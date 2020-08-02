package me.lightspeed7.fireflies.video

import java.io.File

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.Matchers.{be, convertToAnyShouldWrapper}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import org.scalatest.junit.JUnitRunner
import scala.concurrent.ExecutionContext

@RunWith(classOf[JUnitRunner])
class ProcessVideoFilesTest extends FunSuite {

//  val inDir = new File("/Volumes/Backups/fireflies")
  val inDir = new File("/Users/david/Documents/fireflies")

  val outDir = new File("/Users/david/Documents/fireflies")
  val output = new File(outDir, "processed")

  def findInt(in: File): Int = {

    try {
      val name = in.getName
      val end = name.lastIndexOf('.')
      name.substring(4, end).toInt
    } catch {
      case ex:Exception =>
        println("Unable to find int id in filename")
        -1
    }
  }

  test("get file info for all files in directory") {

    inDir.isDirectory should be(true)
    inDir.exists() should be(true)

    outDir.isDirectory should be(true)
    outDir.exists() should be(true)

    import ExecutionContext.Implicits.global
    import scala.collection.JavaConverters._

    val fileList = inDir.listFiles()
      .filter(_.isFile)
      .filter(_.getCanonicalPath.toLowerCase.contains("mp4"))
      .filter { f => Seq.range(486, 513).contains(findInt(f)) }
    //      .filter { f => println(f); f.getName.contains("Fossil")}

    implicit val system: ActorSystem = ActorSystem("Processing")
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    val source = Source.fromIterator(() => fileList.toIterator)
    val sink = Sink.foreach { f: Any => println("Completed") }

    val flow = source.mapAsyncUnordered(3) { f =>
      println(s"Processing file - ${f.getCanonicalPath}")
      //      Future { new FileProcessor(f, output, true, "detection").processVideo }
      Future {
        new FileProcessor(f, output, false, "grids").processVideo()
      }
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