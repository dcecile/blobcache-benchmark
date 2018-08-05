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
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import org.rogach.scallop._
import scala.math

import blobstoreBenchmark.core.DiscardNonUnitValue.discard
import blobstoreBenchmark.core.Result

object Main {
  def main(args: Array[String]): Unit = {
    val conf = new Conf(args)
    conf.subcommand match {
      case Some(conf.clean) =>
        clean()
      case Some(conf.run) =>
        run()
      case _ =>
        conf.printHelp()
    }
  }

  def clean(): Unit = {
    Result.clearAll()
    discard(Files.deleteIfExists(finalPath))
  }

  val finalPath: Path = Paths.get("..", "results.png")

  def run(): Unit = {
    val results = Result.loadAll()
    val plot = createAndCombinePlots(
      results,
      ("user seconds (s)", _.userSeconds),
      ("total seconds (s)", _.totalSeconds))
    val drawable = renderPlot(plot)
    writePlot(drawable)
  }

  implicit val theme: Theme = DefaultTheme

  def createAndCombinePlots(
    results: Seq[Result],
    plotDetails: (String, Result => Double)*
  ): Plot =
    combinePlots(plotDetails.map({
      case (yLabel, yValue) =>
        createPlot(results, yLabel, yValue)
    }))

  def combinePlots(plots: Seq[Plot]): Plot =
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
    discard(
      Files.move(
        writePath,
        finalPath,
        StandardCopyOption.REPLACE_EXISTING))
  }

  class Conf(arguments: Seq[String])
      extends ScallopConf(arguments) {
    object clean extends Subcommand("clean")
    addSubcommand(clean)

    object run extends Subcommand("run")
    addSubcommand(run)

    verify()
  }
}
