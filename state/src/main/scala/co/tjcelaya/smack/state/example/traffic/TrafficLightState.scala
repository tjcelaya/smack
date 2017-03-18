package co.tjcelaya.smack.state.example.traffic

/**
  * Created by tj on 2/12/17.
  */
sealed trait TrafficLightState

case object Go extends TrafficLightState
case object Stop extends TrafficLightState

