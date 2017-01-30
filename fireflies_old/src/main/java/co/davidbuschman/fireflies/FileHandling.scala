package co.davidbuschman.fireflies

import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

object FileHandling {

  def findAllFiles(filterFunc: File => Boolean)(baseDir: File): Seq[File] = baseDir.isDirectory() match {
    case false => Seq(baseDir).filter(filterFunc)
    case true => {
      val these = baseDir.listFiles
      val currentDirFiles = these.filter(!_.isDirectory()).filter(filterFunc(_))
      val recursedFiles = these.filter(_.isDirectory()).flatMap(findAllFiles(filterFunc))
      currentDirFiles ++ recursedFiles
    }
  }

  def findAllPaths(filterFunc: File => Boolean)(baseDir: Path): Seq[Path] = {

    def toPath(in: File): Path = Paths.get(in.getCanonicalPath)

    val f: File => Boolean = (in: File) => filterFunc(in)

    val raw = baseDir.toFile.isDirectory() match {
      case false => Seq(baseDir).map(_.toFile).filter(filterFunc)
      case true  => findAllFiles(filterFunc)(baseDir.toFile)
    }
    raw.map(toPath)
  }

}