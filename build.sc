import mill._, scalalib._, scalajslib._

object todo extends ScalaModule with ScalaJSModule {
  override def scalaVersion = "2.13.1"
  override def scalaJSVersion = "1.5.0"

  override def ivyDeps = Agg(
    ivy"com.raquo::laminar::0.12.1",
    ivy"com.raquo::airstream::0.12.0"
  )
}