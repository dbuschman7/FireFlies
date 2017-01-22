package me.lightspeed7.fireflies

import org.scalatest.FunSuite
import org.scalatest.Matchers.{ be, convertToAnyShouldWrapper }

class ColorDifferenceTest extends FunSuite {

  case class AwtColor(red: Int, green: Int, blue: Int, name: Option[String] = None) {
    val color: java.awt.Color = new java.awt.Color(red, green, blue)
    override def toString: String = f"${name.getOrElse("Unknown")}%15s - ${red}%4d ${green}%4d ${blue}%4d"
  }

  def printHSL(hsl: Array[Double]): String = f"Hue = ${hsl(0)}%3.8f Saturation = ${hsl(1)}%3.8f  Luminance = ${hsl(2)}%3.8f"

  val desired = new AwtColor(250, 250, 250)
  val output = new AwtColor(8, 8, 8)

  val knownColors = Seq(desired, output, //
    new AwtColor(188, 216, 235, Some("BlueSky")), // blue of the sky
    new AwtColor(112, 129, 142, Some("Firefly1")), // firefly
    new AwtColor(98, 96, 97, Some("Firefly Weak1")), // weak firefly
    new AwtColor(105, 102, 106, Some("Firefly Weak2")), // weak firefly
    new AwtColor(200, 196, 199, Some("Firefly2")), // another firefly
    new AwtColor(240, 230, 140, Some("khaki")), // khaki 
    new AwtColor(255, 218, 185, Some("peachpuff")), // peachpuff
    new AwtColor(255, 250, 205, Some("lemon chiffon")), // lemon chiffon
    new AwtColor(255, 0, 0, Some("red")), // red
    new AwtColor(255, 255, 0, Some("yellow")), // yellow
    new AwtColor(0, 255, 0, Some("green")), // green
    new AwtColor(0, 255, 255, Some("cyan")), // 
    new AwtColor(0, 0, 255, Some("blue")), // 
    new AwtColor(255, 0, 255, Some("magenta")), // magenta
    new AwtColor(71, 70, 70, Some("Firefly3")) //
    )

  test("Determine correct threshold") {

    val threshold = 20.0

    val results = knownColors.map { color =>
      val distance = Color.CIE76Distance(color.color, desired.color)
      val category = Color.classify(color.color)
      println(f"Color(${category}%8s) - ${color} - ${distance}%3.8f")
      if (distance > threshold) None else Some(distance)
    }.flatten

    results.size should be(1)
    results.head should be(0.0)
  }

  test("Test RGB -> HSL -> RGB") {

    val yellow = new AwtColor(255, 255, 0)
    println(s"Yellow(RGB) = ${yellow}")

    val hsl = Color.rgb2hsl(yellow.color)
    println(s"Yellow(HSL) = ${printHSL(hsl)}")

    println(s"Yellow(cls) = ${Color.classify(hsl)}")

    val rgb = Color.hsl2rgb(hsl)
    println(s"Yellow(RGB) = ${rgb}")
  }

  test("RGB -> HSL - RGB roundtrips") {
    import Color._

    def print(c: AwtColor, hsl: Array[Double]): Array[Double] = {
      println(c.toString + "  " + printHSL(hsl))
      hsl
    }

    def test(c: AwtColor) = hsl2rgb(print(c, rgb2hsl(c.color))) should be(c.color)

    knownColors.map(test)
  }
}