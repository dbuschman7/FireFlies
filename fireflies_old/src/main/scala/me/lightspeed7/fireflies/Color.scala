package me.lightspeed7.fireflies

object Color {

  def toTuple(rgb: Int): (Int, Int, Int) = ((rgb >> 16) & 0xff, (rgb >> 8) & 0xff, (rgb) & 0xff)
  def fromTuple(r: Int, g: Int, b: Int): Int = (r << 16) + (g << 8) + b

  def rgbDiff(left: Int, right: Int): Int = {
    val (r1, g1, b1) = toTuple(left)
    val (r2, g2, b2) = toTuple(right)
    Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2)
  }

  def euclideanDistance(left: Int, right: Int): Double = {
    val (r1, g1, b1) = toTuple(left)
    val (r2, g2, b2) = toTuple(right)
    Math.sqrt((r2 - r1) ^ 2 + (g2 - g1) ^ 2 + (b2 - b1) ^ 2)
  }

  def CIE76Distance(first: Int, second: Int): Double = CIE76Distance(new java.awt.Color(first), new java.awt.Color(second))

  def CIE76Distance(first: java.awt.Color, second: java.awt.Color): Double = {
    val lab1 = rgb2lab(first)
    val lab2 = rgb2lab(second)
    val p1 = Math.pow(lab2(0) - lab1(0), 2)
    val p2 = Math.pow(lab2(1) - lab1(1), 2)
    val p3 = Math.pow(lab2(2) - lab1(2), 2)
    Math.sqrt(p1 + p2 + p3)
  }

  def calcTranlucency(left: Int, right: Int, tolerance: Double): (Double, Double, Double) = {
    val (r1, g1, b1) = toTuple(left)
    val (r2, g2, b2) = toTuple(right)

    val lHSB = Seq(java.awt.Color.RGBtoHSB(r1, g1, b1, null): _*)
    val rHSB = Seq(java.awt.Color.RGBtoHSB(r2, g2, b2, null): _*)
    val deltas = (lHSB zip rHSB).map { case (l, r) => Math.abs(l - r) }.map(_ / tolerance) //.map(Math.min(1f, _))

    Tuple3(deltas(0), deltas(1), deltas(2))
  }

  def rgb2lab(rgb: java.awt.Color): Array[Int] = {
    // http://www.brucelindbloom.com

    val eps = 216.0 / 24389.0;
    val k = 24389.0 / 27.0;

    val Xr = 0.964221; // reference white D50
    val Yr = 1.0;
    val Zr = 0.825211;

    // RGB to XYZ
    var r = rgb.getRed() / 255.0; // R 0..1
    var g = rgb.getGreen() / 255.0; // G 0..1
    var b = rgb.getBlue() / 255.0; // B 0..1

    // assuming sRGB (D65)
    if (r <= 0.04045)
      r = r / 12;
    else
      r = Math.pow((r + 0.055) / 1.055, 2.4);

    if (g <= 0.04045)
      g = g / 12;
    else
      g = Math.pow((g + 0.055) / 1.055, 2.4);

    if (b <= 0.04045)
      b = b / 12;
    else
      b = Math.pow((b + 0.055) / 1.055, 2.4);

    val X = 0.436052025f * r + 0.385081593f * g + 0.143087414f * b;
    val Y = 0.222491598f * r + 0.71688606f * g + 0.060621486f * b;
    val Z = 0.013929122f * r + 0.097097002f * g + 0.71418547f * b;

    // XYZ to Lab
    val xr = X / Xr;
    val yr = Y / Yr;
    val zr = Z / Zr;

    val fx = if (xr > eps)
      Math.pow(xr, 1 / 3.0)
    else
      ((k * xr + 16.0) / 116.0)

    val fy = if (yr > eps)
      Math.pow(yr, 1 / 3.0);
    else
      ((k * yr + 16.0) / 116.0);

    val fz = if (zr > eps)
      Math.pow(zr, 1 / 3.0);
    else
      ((k * zr + 16.0) / 116.0);

    val Ls = (116 * fy) - 16;
    val as = 500 * (fx - fy);
    val bs = 200 * (fy - fz);

    Array[Int]((2.55 * Ls + .5).toInt, (as + .5).toInt, (bs + .5).toInt)
  }

  def rgb2hsl(rgb: java.awt.Color): Array[Double] = {
    val r = rgb.getRed / 255.0
    val g = rgb.getGreen / 255.0
    val b = rgb.getBlue / 255.0

    val min = Math.min(r, Math.min(g, b))
    val max = Math.max(r, Math.max(g, b))

    // Calc Hue
    val h: Double = //
      if (max == min) {
        0.0
      } else if (max == r) {
        (((g - b) / (max - min) / 6.0) + 1.0) % 1.0
      } else if (max == g) {
        ((b - r) / (max - min) / 6.0) + 1.0 / 3.0
      } else if (max == b) {
        ((r - g) / (max - min) / 6.0) + 2.0 / 3.0
      } else { 0.0 }

    // Calc Luminance
    val l: Double = (max + min) / 2.0

    // Calc Saturation
    val s: Double = //
      if (max == min) {
        0.0
      } else if (l <= 0.5) {
        (max - min) / (max + min)
      } else {
        (max - min) / (2.0 - max - min)
      }

    Array[Double](h, s, l)
  }

  def hsl2rgb(hsl: Array[Double], alpha: Double = 1.0): java.awt.Color = {

    def hueToRGB(p: Double, q: Double, preH: Double) = {
      val h = preH match {
        case h if h < 0 => h + 1
        case h if h > 1 => h - 1
        case h          => h
      }

      if (6 * h < 1) {
        p + ((q - p) * 6 * h)
      } else {
        if (2 * h < 1) {
          q
        } else {
          if (3 * h < 2) {
            p + ((q - p) * 6 * ((2.0f / 3.0f) - h))
          } else {
            p
          }
        }
      }
    }

    val h = hsl(0)
    val s = hsl(1)
    val l = hsl(2)

    val q = //
      if (l < 0.5) {
        l * (1.0 + s)
      } else {
        (l + s) - (s * l);
      }

    val p = 2.0 * l - q

    val r = (255 * Math.max(0, hueToRGB(p, q, h + (1.0 / 3.0))) + 0.5).toInt
    val g = (255 * Math.max(0, hueToRGB(p, q, h)) + 0.5).toInt
    val b = (255 * Math.max(0, hueToRGB(p, q, h - (1.0 / 3.0))) + 0.5).toInt

    new java.awt.Color(((255 * alpha).toInt << 24) + (r << 16) + (g << 8) + (b))
  }

  def classify(rgb: Int): String = classify(new java.awt.Color(rgb))
  def classify(rgb: java.awt.Color): String = classify(rgb2hsl(rgb))
  def classify(hsl: Array[Double]): String = {
    (hsl(0), hsl(1), hsl(2)) match {
      case (_, _, l) if l < 0.20 => "Black"
      case (_, _, l) if l > 0.80 => "White"
      case (_, s, _) if s < 0.25 => "Gray"
      case (h, _, _) if h < 0.10 => "Red"
      case (h, _, _) if h < 0.22 => "Yellow"
      case (h, _, _) if h < 0.41 => "Green"
      case (h, _, _) if h < 0.58 => "Cyan"
      case (h, _, _) if h < 0.72 => "Blue"
      case (h, _, _) if h < 0.91 => "Magenta"
      case (_, _, _)             => "Red"
    }
  }

}