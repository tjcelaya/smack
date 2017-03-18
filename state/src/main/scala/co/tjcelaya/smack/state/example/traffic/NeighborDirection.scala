package co.tjcelaya.smack.state.example.traffic

/**
  * Created by tj on 2/12/17.
  */
sealed trait NeighborDirection {
  def inverse: NeighborDirection = {
    this match {
      case Self => Self
      case Opposite => Opposite
      case Tail => Tail
      case Left => Right
      case Right => Left
      case Indirect => Indirect
    }
  }
}

case object Self extends NeighborDirection
case object Opposite extends NeighborDirection
case object Tail extends NeighborDirection
case object Left extends NeighborDirection
case object Right extends NeighborDirection
case object Indirect extends NeighborDirection