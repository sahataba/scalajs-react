package olog

object Views{
  import scalatags.Text.all._
  import scalatags.Text.tags2.{title, style}

  val homepage =
    "<!DOCTYPE html>" +
      html(
        head(
          title("Example Scala.js application"),
          meta(httpEquiv:="Content-Type", content:="text/html; charset=UTF-8"),
          script(`type`:="text/javascript", src:="/olog-fastopt.js"),
          script(`type`:="text/javascript", src:="//localhost:12345/workbench.js"),
          script(`type` := "text/javascript", src:="http://fb.me/react-with-addons-0.12.1.js"),
          link(rel:="stylesheet",`type`:="text/css",href:="https://cdnjs.cloudflare.com/ajax/libs/materialize/0.97.0/css/materialize.min.css"),
          script(`type`:="text/javascript",src:="https://code.jquery.com/jquery-2.1.1.min.js"),
          script(`type`:="text/javascript",src:="https://cdnjs.cloudflare.com/ajax/libs/materialize/0.97.0/js/materialize.min.js"),
          style("""
          .example-enter {
            opacity: 0.01;
            transition: opacity .5s ease-in;
          }
          .example-enter.example-enter-active {
            opacity: 1;
          }
          .example-leave {
            opacity: 1;
            transition: opacity .5s ease-in;
          }
          .example-leave.example-leave-active {
            opacity: 0.01;
          }
                """)
        ),
        body(margin:=0)(
          script("olog.ReactApp().main()")
        )
      )
}