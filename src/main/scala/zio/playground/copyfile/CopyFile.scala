package zio.playground.copyfile

import zio._
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.io.FileInputStream
import java.io.FileOutputStream

object CopyFile extends ZIOAppDefault {

  def run: ZIO[Environment with ZIOAppArgs with Scope, Any, Any] =
    for {
      _   <- Console.printLine("Read file:")
      in  <- Console.readLine
      _   <- Console.printLine("Write to file:")
      out <- Console.readLine

      orig = new File(in)
      dest = new File(out)

      _      <- checkIdentical(in, out)
      destOk <- checkDestination(dest)
      _ <-
        if (destOk)
          runCopy(orig, dest, 1024 * 10)
        else
          ZIO.fail(new RuntimeException("Terminated"))

    } yield ()

  def runCopy(origin: File, destination: File, bufferSize: Int) =
    for {
      count <- copy(origin, destination, bufferSize)
      _ <- Console.printLine(
        s"$count bytes copied from ${origin.getPath} to ${destination.getPath}"
      )
    } yield ()

  def checkIdentical(in: String, out: String): Task[Unit] =
    if (in == out) {
      ZIO.fail(new RuntimeException("Origin and destination are the same"))
    } else {
      ZIO.unit
    }

  def checkDestination(dest: File) =
    if (dest.isFile()) {
      for {
        _  <- Console.printLine("Destination exists, overwrite y/n?")
        in <- Console.readLine
      } yield (in == "y")
    } else {
      ZIO.succeed(true)
    }

  def copy(
    origin: File,
    destination: File,
    bufferSize: Int,
  ): Task[Long] =
    inputStream(origin) { inputStream =>
      outputStream(destination) { outputStream =>
        transfer(inputStream, outputStream, bufferSize)
      }
    }

  def transfer(
    origin: InputStream,
    destination: OutputStream,
    bufferSize: Int,
  ): Task[Long] = transmit(origin, destination, new Array[Byte](bufferSize), 0L)

  def transmit(
    origin: InputStream,
    destination: OutputStream,
    buffer: Array[Byte],
    acc: Long,
  ): Task[Long] =
    for {
      // !!!!!!!!!!!!
      // Introduced artificial delay when reading a file
      // !!!!!!!!!!!!
      amount <-
        ZIO.sleep(10.millisecond) *> ZIO.attemptBlocking(origin.read(buffer, 0, buffer.length))
      count <-
        if (amount > -1)
          ZIO.attemptBlocking(destination.write(buffer, 0, amount)) *> transmit(
            origin,
            destination,
            buffer,
            acc + amount,
          )
        // End of read stream reached (by java.io.InputStream contract), nothing to write
        else
          ZIO.succeed(acc)
    } yield count // Returns the actual amount of bytes transmitted

  def inputStream(f: File) =
    ZIO.acquireReleaseWith(ZIO.attemptBlocking(new FileInputStream(f)))(stream =>
      ZIO.attemptBlocking(stream.close()).catchAll { throwable =>
        throwable match {
          case _ => ZIO.unit
        }
      }
    )

  def outputStream(f: File) =
    ZIO.acquireReleaseWith(ZIO.attemptBlocking(new FileOutputStream(f)))(stream =>
      ZIO.attemptBlocking(stream.close()).catchAll { throwable =>
        throwable match {
          case _ => ZIO.unit
        }
      }
    )

}
