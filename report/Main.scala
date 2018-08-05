package blobstoreBenchmark.report

import com.cibo.evilplot._
import com.cibo.evilplot.colors.HTMLNamedColors
import com.cibo.evilplot.geometry._
import com.cibo.evilplot.numeric.Point
import com.cibo.evilplot.plot._
import com.cibo.evilplot.plot.aesthetics.DefaultTheme.DefaultTheme
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.renderers.PointRenderer
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import scala.math

import blobstoreBenchmark.core.DiscardNonUnitValue.discard
import blobstoreBenchmark.core.Result

object Main {
  def main(args: Array[String]): Unit = {
    discard(args)
    val results = Seq.tabulate(3, 100) {
      case (i, j) =>
        Result(
          i match {
            case 0 => "simpleFileStream"
            case 1 => "fs2"
            case _ => "rocksDB"
          },
          math.pow(10, 1 + 4 * j.toDouble / 99).toInt,
          5 * scala.util.Random.nextDouble(),
          10 * scala.util.Random.nextDouble(),
          20 * scala.util.Random.nextDouble()
        )
    }
    val plot = combinePlots(
      createPlot(
        results.flatten,
        "user seconds (s)",
        _.userSeconds),
      createPlot(
        results.flatten,
        "total seconds (s)",
        _.totalSeconds))
    val drawable = renderPlot(plot)
    writePlot(drawable)
  }

  implicit val theme: Theme = DefaultTheme

  def combinePlots(plots: Plot*): Plot =
    Facets(plots.map(Seq(_)))
      .xLabel("key count")
      .padRight(40)
      .rightLegend()

  def createPlot(
    results: Seq[Result],
    yLabel: String,
    yValue: Result => Double
  ): Plot = {
    val data = results.map(result =>
      Point(result.keyCount.toDouble, yValue(result)))
    val dataLog10 = data.map { point =>
      Point(math.log10(point.x), point.y)
    }
    val maxLog10 = math.ceil(dataLog10.map(_.x).max)
    val countLog10 = maxLog10.toInt + 1

    ScatterPlot(
      dataLog10,
      pointRenderer = Some(PointRenderer.colorByCategory(
        results.map(_.blobstore))))
      .xAxis(
        tickCount = Some(countLog10),
        labelFormatter =
          Some(value => math.pow(10, value).toInt.toString))
      .xGrid(lineCount = Some(countLog10))
      .yAxis(tickCount = Some(6))
      .yGrid(lineCount = Some(6))
      .padLeft(10)
      .yLabel(yLabel)
      .frame()
      .padTop(20)
  }

  def renderPlot(plot: Plot): Drawable = {
    val plotDrawable = plot
      .render(Extent(1600, 600))
      .padAll(80)
      .translate(-50, 20)
    val extent = plotDrawable.extent
    val background = Style(
      Rect(extent.width, extent.height),
      HTMLNamedColors.white)
    plotDrawable.inFrontOf(background)
  }

  def writePlot(drawable: Drawable): Unit = {
    val writePath = Paths.get("out.png")
    drawable.write(writePath.toFile)
    val finalPath = Paths.get("..", "r.png")
    discard(
      Files.move(
        writePath,
        finalPath,
        StandardCopyOption.REPLACE_EXISTING))
  }
}
