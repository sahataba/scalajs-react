package olog

import japgolly.scalajs.react._, vdom.prefix_<^._
import chandu0101.scalajs.react.components.fascades.{LatLng, Marker}
import chandu0101.scalajs.react.components.maps.GoogleMap

object AboutPage {

  val center = LatLng(45.800403, 15.721538)
  val markers = List(
    Marker( position = center , title = "Olog" )
  )
  val map = GoogleMap(width = 600 ,height = 500 ,center = center , markers = markers, zoom = 12)

  val component = ReactComponentB[Unit]("About")
    .render(P => {
      <.div(<.h1("Olog"), map)})
    .buildU

  def apply() = component()

}
