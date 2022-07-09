import zio.ZIOAppDefault
import zio.{Scope, ZIO, ZIOAppArgs}
import zio.Console

object Main extends ZIOAppDefault {

  def run: ZIO[Environment with ZIOAppArgs with Scope, Any, Any] = Console.printLine("Hello from ZIO app")

}
