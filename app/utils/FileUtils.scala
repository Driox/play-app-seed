package utils

import java.io._

class FileUtils(file: File) {

  private[this] def using[A <: { def close(): Unit }, B](param: A)(f: A => B): B = try { f(param) } finally { param.close() }

  def read(): String = read(0)

  def read(from_line: Int): String = {
    val source = scala.io.Source.fromFile(file)
    val lines = try source.getLines().drop(from_line).mkString("\n") finally source.close()
    lines
  }

  def write(text: String): Unit = {
    file.getParentFile.mkdirs()
    val fw = new FileWriter(file)
    try { fw.write(text) }
    finally { fw.close }
  }

  def append(textData: String) = {
    using(new FileWriter(file, true)) {
      fileWriter =>
        using(new PrintWriter(fileWriter)) {
          printWriter => printWriter.println(textData)
        }
    }
  }

  def foreachLine(proc: String => Unit): Unit = {
    val br = new BufferedReader(new FileReader(file))
    try { while (br.ready) proc(br.readLine) }
    finally { br.close }
  }

  def deleteAll(): Unit = {
    def deleteFile(dfile: File): Unit = {
      if (dfile.isDirectory) {
        dfile.listFiles.foreach { f => deleteFile(f) }
      }
      dfile.delete
    }
    deleteFile(file)
  }
}

object FileUtils {
  implicit def enhancedFile(file: File) = new FileUtils(file)
}
