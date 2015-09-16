package interface

import java.awt.{GraphicsEnvironment, Dimension}

object SizeUtils
{
  val std_height = 600
  val std_width = 800

  /**
   * @return the display size of the main screen at this computer.
   */
  def getDisplaySize: Dimension =
  {
    val ge = GraphicsEnvironment.getLocalGraphicsEnvironment
    val gs = ge.getDefaultScreenDevice
    val dm = gs.getDisplayMode
    new Dimension(dm.getWidth, dm.getHeight)
  }
}