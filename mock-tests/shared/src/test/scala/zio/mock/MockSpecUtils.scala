package zio.mock

import zio._
import zio.mock.module.T22
import zio.test.{Assertion, Live, ZSpec, assertM, test}
import testing._

trait MockSpecUtils[R] {

  lazy val intTuple22: T22[Int] =
    (1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22)

  private[mock] def testValue[E, A](name: String)(
      mock: ULayer[R],
      app: ZIO[R, E, A],
      check: Assertion[A]
  ): ZSpec[Any, E] = test(name) {
    val result = ZIO.scoped[Any](mock.build.flatMap(app.provideEnvironment(_)))
    assertM(result)(check)
  }

  private[mock] def testError[E, A](name: String)(
      mock: ULayer[R],
      app: ZIO[R, E, A],
      check: Assertion[E]
  ): ZSpec[Any, A] = test(name) {
    val result = ZIO.scoped[Any](mock.build.flatMap(app.flip.provideEnvironment(_)))
    assertM(result)(check)
  }

  private[mock] def testValueTimeboxed[E, A](name: String)(duration: Duration)(
      mock: ULayer[R],
      app: ZIO[R, E, A],
      check: Assertion[Option[A]]
  ): ZSpec[Live, E] = test(name) {
    val result =
      Live.live {
        ZIO
          .scoped {
            mock.build.flatMap(app.provideEnvironment(_))
          }
          .timeout(duration)
      }

    assertM(result)(check)
  }

  private[mock] def testDied[E, A](name: String)(
      mock: ULayer[R],
      app: ZIO[R, E, A],
      check: Assertion[Throwable]
  ): ZSpec[Any, Any] = test(name) {

    val result: IO[Any, Throwable] =
      swapFailure(
        ZIO
          .scoped {
            mock.build
              .flatMap(app.provideEnvironment(_))
          }
      )

    assertM(result)(check)
  }

}
