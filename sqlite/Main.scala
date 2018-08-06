package blobstoreBenchmark.sqlite

import java.io.File
import java.sql.Connection
import java.sql.DriverManager

import blobstoreBenchmark.core.DiscardNonUnitValue.discard
import blobstoreBenchmark.core.Harness
import blobstoreBenchmark.core.Key
import blobstoreBenchmark.core.Pair
import blobstoreBenchmark.core.Plan
import blobstoreBenchmark.core.Step
import blobstoreBenchmark.core.Sum

object Main extends Harness {
  def init(plan: Plan): Unit =
    withConnection(
      plan.dbDir,
      connection => {
        createTable(connection)
        plan.pairs.foreach(
          write(connection, plan.blobSize, _))
      }
    )

  def run(plan: Plan): Long =
    withConnection(plan.dbDir, connection => {
      plan.steps.toStream
        .map(runStep(connection, plan, _))
        .sum
    })

  def runStep(
    connection: Connection,
    plan: Plan,
    step: Step
  ): Long = {
    val sum = step.queries.toStream
      .map(read(connection, _))
      .sum
    step.updates
      .foreach(write(connection, plan.blobSize, _))
    sum
  }

  def withConnection[T](
    dbDir: File,
    block: Connection => T
  ): T = {
    val connection =
      DriverManager.getConnection(connectionString(dbDir))
    connection.setAutoCommit(false)
    val result = block(connection)
    connection.commit()
    connection.close()
    result
  }

  def connectionString(dbDir: File): String =
    s"jdbc:sqlite:./${dbDir.getPath()}/store.db"

  def createTable(connection: Connection): Unit = {
    val statement = connection.prepareStatement(
      "create table pairs(id integer primary key, data blob)")
    discard(statement.execute())
  }

  def write(
    connection: Connection,
    blobSize: Int,
    pair: Pair
  ): Unit = {
    val statement = connection.prepareStatement(
      "insert or replace into pairs (id, data) values (?, ?)")
    statement.setLong(1, pair.key.value)
    statement.setBytes(
      2,
      pair.blobStub.generateArray(blobSize))
    discard(statement.execute())
  }

  def read(
    connection: Connection,
    key: Key
  ): Long = {
    val statement = connection.prepareStatement(
      "select data from pairs where id = ?")
    statement.setLong(1, key.value)
    val results = statement.executeQuery()
    discard(results.next())
    val array = results.getBytes(1)
    results.close()
    Sum.fromArray(array)
  }
}
