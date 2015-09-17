package interface

import java.awt.event.MouseEvent
import java.awt.{Cursor, Polygon, Color}
import java.awt.geom.Area
import java.io.File
import javax.imageio.ImageIO
import javax.swing.border.{TitledBorder, BevelBorder}
import javax.swing.{ToolTipManager, Box, WindowConstants, UIManager}

import scala.swing.event._
import scala.swing._

class Interface extends SimpleSwingApplication {
  private val statusPanel = new Label() {
    horizontalAlignment = Alignment.Right
  }

  private val roomPanel = new Label() {
    horizontalAlignment = Alignment.Left
  }

  def top: Frame = new MainFrame {
    //Look and Feel
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)
    } catch {
      case e: Throwable => // gets ignored, look and feel is not that important
    }

    val width = SizeUtils.std_width + 123
    val height = SizeUtils.std_height
    val screenSize = SizeUtils.getDisplaySize
    val dim = new java.awt.Dimension(width, height)
    val pos = new java.awt.Point((screenSize.width - width) / 2, (screenSize.height - height) / 2)
    location = pos
    minimumSize = dim
    preferredSize = dim
    peer.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)
    peer.setResizable(false)
    title = "CPSDemo"

    val drawPanel = new DrawPanel()
    val content = new BorderPanel() {
      add(new BorderPanel {
        border = new BevelBorder(BevelBorder.LOWERED)
        add(statusPanel, BorderPanel.Position.East)
        add(roomPanel, BorderPanel.Position.West)
      }, BorderPanel.Position.South)
      add(new BorderPanel() {
        border = new TitledBorder("Sensors")
        add(new GridPanel(4, 1) {
          contents += new ComboBox(List("Sensor A", "Sensor B", "..."))
          peer.add(Box.createVerticalBox())
          contents += new Button("Add") {
            listenTo(mouse.clicks)
            reactions += {
              case e: MouseClicked => drawPanel.peer.setCursor(new Cursor(Cursor.HAND_CURSOR))
            }
          }
          contents += new Button("Remove") {
            listenTo(mouse.clicks)
            reactions += {
              case e: MouseClicked => drawPanel.peer.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR))
            }
          }
        }, BorderPanel.Position.North)
      }, BorderPanel.Position.West)
      add(drawPanel, BorderPanel.Position.Center)
    }
    contents = content
    drawPanel.requestFocus()

    override def closeOperation() {
      if (Dialogs.confirmation("Do you really want to exit the demo?")) {
        super.closeOperation()
      }
    }
  }

  private class DrawPanel extends Panel {
    private val floor = ImageIO.read(new File("src/main/resources/floor.jpg"))
    private val grandma = new Grandma()
    private val rooms = Seq(new Room("Bathroom", List((17, 133), (232, 133), (232, 335), (17, 335))),
      new Room("Livingroom", List((247, 133), (788, 133), (788, 500), (17, 500), (17, 353), (247, 353))),
      new Room("Balcony", List((385, 0), (385, 114), (800, 114), (800, 0))),
      new Room("Bed", List((450, 300), (630, 300), (630, 500), (450, 500))))

    private val borders = Seq(new Border(List((0, 114), (508, 114), (508, 133), (0, 133))),
      new Border(List((580, 114), (800, 114), (800, 133), (580, 133))),
      new Border(List((0, 500), (800, 500), (800, 514), (0, 515))),

      new Border(List((0, 133), (17, 133), (17, 500), (0, 500))),
      new Border(List((788, 133), (800, 133), (800, 500), (788, 500))),
      new Border(List((17, 335), (75, 335), (75, 353), (17, 353))),
      new Border(List((155, 335), (232, 335), (232, 133), (247, 133), (247, 353), (155, 353))),

      new Border(List((370, 0), (385, 0), (385, 114), (370, 114))),
      new Border(List((788, 0), (800, 0), (800, 114), (788, 114))))

    background = Color.WHITE
    listenTo(mouse.clicks)
    listenTo(mouse.moves)
    listenTo(keys)
    focusable = true

    reactions += {
      case KeyPressed(_, Key.Up, _, _) => grandma.moveUp()
      case KeyPressed(_, Key.Down, _, _) => grandma.moveDown()
      case KeyPressed(_, Key.Left, _, _) => grandma.moveLeft()
      case KeyPressed(_, Key.Right, _, _) => grandma.moveRight()
      case KeyPressed(_, Key.Space, _, _) => grandma.passedOut match {
        case true => grandma.recover()
        case false => grandma.passOut()
      }
      case e: MouseClicked => peer.setCursor(Cursor.getDefaultCursor)
      case MouseMoved(src, pt, mod) =>
        statusPanel.text = s"Position: x = ${pt.x}, y = ${pt.y}"
        rooms.foreach(_.handleHover(pt.x, pt.y))
      case MouseExited(src, pt, mod) =>
        rooms.foreach(_.handleHover(-1, -1))
    }

    def showTooltip(x: Int, y: Int) {
      ToolTipManager.sharedInstance().mouseMoved(new MouseEvent(this.peer, 0, 0, 0, x, y, 0, false))
    }

    def hideTooltip() {
      ToolTipManager.sharedInstance().mouseExited(new MouseEvent(this.peer, 0, 0, 0, 0, 0, 0, 0, 0, false, 0))
    }

    override def paint(g: Graphics2D) {
      super.paintComponent(g)
      // background image
      // g.drawImage(floor.getScaledInstance(peer.getWidth, peer.getHeight, java.awt.Image.SCALE_FAST), 0, 0, null)
      // character
      grandma.repaint(g)
      // rooms
      rooms.foreach(_.repaint(g))
      // borders
      borders.foreach(_.repaint(g))
    }

    private sealed abstract class Tile {
      var x, y, width, height = 0
      var hover = false

      def getArea: Area

      def collide(other: Tile): Boolean = {
        var collide = false

        val collide1 = new Area(getArea)
        collide1.subtract(other.getArea)
        if (!collide1.equals(getArea)) {
          collide = true
        }

        val collide2 = new Area(other.getArea)
        collide2.subtract(getArea)
        if (!collide2.equals(other.getArea)) {
          collide = true
        }

        collide
      }

      def repaint(g: Graphics2D)

      def handleClick(x: Int, y: Int) {
        DrawPanel.this.repaint()
        ???
      }

      def handleHover(vx: Int, vy: Int) {
        hover = getArea.contains(vx, vy)
        DrawPanel.this.repaint()
      }

      def updateModel() {
        DrawPanel.this.repaint()
        ???
      }
    }

    private object Room {
      val HOVER_COLOR = new Color(255, 0, 0, 50)
      val COLOR = new Color(255, 255, 255, 0)
      val MAX_SENSORS = 2
      val MAX_ACTORS = 2
    }

    private class Room(name: String, points: List[(Int, Int)]) extends Tile {
      protected val poly = new Polygon(points.map(_._1).toArray, points.map(_._2).toArray, points.size)
      private val area = new Area(poly)

      private val sensors = List(
        new Sensor("SA", List((points.head._1 + 5, points.head._2 + 25))),
        new Sensor("SB", List((points.head._1 + 5, points.head._2 + 50)))
      )

      private val actors = List(
        new Actor("AA", List((points.head._1 + 35, points.head._2 + 25))),
        new Actor("AB", List((points.head._1 + 35, points.head._2 + 50)))
      )

      y = area.getBounds2D.getX.toInt
      x = area.getBounds2D.getX.toInt
      width = area.getBounds2D.getWidth.toInt
      height = area.getBounds2D.getHeight.toInt

      override def getArea = area

      def getName = name

      override def repaint(g: Graphics2D) {
        if (hover)
          g.setColor(Room.HOVER_COLOR)
        else
          g.setColor(Room.COLOR)
        g.fillPolygon(poly)

        g.setColor(Color.RED)
        g.drawPolygon(poly)
        // render room name
        g.drawString(name, points.head._1 + 5, points.head._2 + 15)
        // render sensors
        sensors.foreach(_.repaint(g))
        // render actors
        actors.foreach(_.repaint(g))
      }

      override def handleHover(vx: Int, vy: Int) {
        super.handleHover(vx, vy)
        (sensors ++ actors).find(_.getArea.contains(vx, vy)).foreach {
          case Sensor(n, _) =>
            DrawPanel.this.peer.setToolTipText("Sensor: " + n)
            showTooltip(vx, vy)
            return
          case Actor(n, _) =>
            DrawPanel.this.peer.setToolTipText("Actor: " + n)
            showTooltip(vx, vy)
            return
        }
      }
    }

    private abstract class Installable(name: String, points: List[(Int, Int)]) extends Tile {
      width = 25
      height = 25

      private val ROUNDED = 6

      protected var color: Color = _

      x = points.head._1
      y = points.head._2

      override def getArea: Area = new Area(new Polygon(Array(x, x + width, x + width, x), Array(y, y, y + height, y + height), 4))

      override def repaint(g: Graphics2D) {
        g.setColor(color)
        g.drawRoundRect(x, y, width, height, ROUNDED, ROUNDED)
        g.drawString(name, x + 5, y + 17)
      }
    }

    private case class Sensor(name: String, points: List[(Int, Int)]) extends Installable(name, points) {
      color = Color.GREEN
    }

    private case class Actor(name: String, points: List[(Int, Int)]) extends Installable(name, points) {
      color = Color.BLUE
    }

    private class Border(points: List[(Int, Int)]) extends Room("", points) {
      override def repaint(g: Graphics2D) {
        g.setColor(Color.BLACK)
        g.fillPolygon(poly)
      }
    }

    private object Grandma {
      val LEFT = ImageIO.read(new File("src/main/resources/grandma_l.png"))
      val RIGHT = ImageIO.read(new File("src/main/resources/grandma_r.png"))
      val PASSEDOUT_RIGHT = ImageIO.read(new File("src/main/resources/grandma_passedout_r.png"))
      val PASSEDOUT_LEFT = ImageIO.read(new File("src/main/resources/grandma_passedout_l.png"))
    }

    private class Grandma extends Tile {
      var passedOut = false
      var image = Grandma.LEFT

      width = SizeUtils.std_width / 15
      height = SizeUtils.std_height / 7

      x = SizeUtils.std_width / 2 - (width / 2)
      y = SizeUtils.std_height / 2 - (height / 2)

      override def getArea = new Area(new Polygon(Array(x, x + width, x + width, x), Array(y, y, y + height, y + height), 4))

      def isInRoom(room: Room): Boolean = {
        val bounds = getArea.getBounds2D
        room.getArea.contains(bounds.getCenterX, bounds.getCenterY)
      }

      private def moveAllowed = !borders.exists(collide)

      def passOut() {
        passedOut = true
        image match {
          case Grandma.LEFT => image = Grandma.PASSEDOUT_LEFT
          case _ => image = Grandma.PASSEDOUT_RIGHT
        }
        DrawPanel.this.repaint()
      }

      def recover() {
        passedOut = false
        image match {
          case Grandma.PASSEDOUT_LEFT => image = Grandma.LEFT
          case _ => image = Grandma.RIGHT
        }
        DrawPanel.this.repaint()
      }

      def flipRight() {
        image = Grandma.RIGHT
      }

      def flipLeft() {
        image = Grandma.LEFT
      }

      def moveUp() {
        y = y - 10
        if (!moveAllowed)
          y = y + 10
        DrawPanel.this.repaint()
      }

      def moveDown() {
        y = y + 10
        if (!moveAllowed)
          y = y - 10
        DrawPanel.this.repaint()
      }

      def moveLeft() {
        flipLeft()
        x = x - 10
        if (!moveAllowed)
          x = x + 10
        DrawPanel.this.repaint()
      }

      def moveRight() {
        flipRight()
        x = x + 10
        if (!moveAllowed)
          x = x - 10
        DrawPanel.this.repaint()
      }

      override def repaint(g: Graphics2D) {
        width = peer.getWidth / 15
        height = peer.getHeight / 7
        g.drawImage(image.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH), x, y, null)
        // draw bounding box
        rooms.foreach(room => {
          if (isInRoom(room)) {
            g.setColor(Color.RED)
            roomPanel.text = "Currently in: " + room.getName
          }
        })
        g.drawPolygon(new Polygon(Array(x, x + width, x + width, x), Array(y, y, y + height, y + height), 4))
      }
    }

  }

}