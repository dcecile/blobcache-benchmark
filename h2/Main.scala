package blobstoreBenchmark.h2

import java.io.File
import java.nio.ByteBuffer
import java.sql.Connection
import org.h2.jdbcx.JdbcConnectionPool
import org.h2.tools.Console

import blobstoreBenchmark.core.Blob
import blobstoreBenchmark.core.Harness
import blobstoreBenchmark.core.Key
import blobstoreBenchmark.core.Plan
import blobstoreBenchmark.core.Step
import blobstoreBenchmark.core.Verify

object Main extends Harness {
  def init(plan: Plan): Unit = {
    withConnection(plan.dbDir, connection => {
      createTable(connection)
      plan.keys.grouped(100).foreach(group => {
        group.foreach(write(connection, plan.blobSize, _))
        connection.commit()
      })
    })
  }

  def run(plan: Plan): Unit = {
    withConnection(plan.dbDir, connection => {
      val sum = plan.steps.toStream
        .map(runStep(connection, plan, _))
        .sum
      Verify.sum("total", sum, plan.expectedSum)
    })
  }

  def runStep(
    connection: Connection,
    plan: Plan,
    step: Step
  ): Long = {
    val sum = step.queries.toStream
      .map(read(connection, plan.blobSize, _))
      .sum
    step.updates
      .foreach(write(connection, plan.blobSize, _))
    step.queries
      .foreach(delete(connection, _))
    connection.commit()
    sum
  }

  def debug(): Unit =
    Console.main()

  def withConnection[T](dbDir: File, block: Connection => T): T = {
    val pool = JdbcConnectionPool.create(
      connectionString(dbDir),
      "sa",
      "sa")
    val connection = pool.getConnection()
    connection.setAutoCommit(false)
    val result = block(connection)
    connection.close()
    pool.dispose()
    result
  }

  def connectionString(dbDir: File): String =
    s"jdbc:h2:./${dbDir.getPath()}/store"

  def createTable(connection: Connection): Unit = {
    val statement = connection.prepareStatement("create table pairs(id bigint primary key, data binary)")
    val _ = statement.execute()
  }

  def write(
    connection: Connection,
    blobSize: Int,
    key: Key
  ): Unit = {
    val blob = Blob.generate(key, blobSize)
    val statement = connection.prepareStatement(
      "insert into pairs (id, data) values (?, ?)")
    statement.setLong(1, key.value)
    statement.setBytes(2, blob.array)
    val _ = statement.execute()
  }

  def read(
    connection: Connection,
    blobSize: Int,
    key: Key
  ): Long = {
    val statement = connection.prepareStatement(
      "select data from pairs where id = ?")
    statement.setLong(1, key.value)
    val results = statement.executeQuery()
    val _ = results.first()
    val buffer = results.getBytes(1)
    results.close()
    Verify.blobSum(ByteBuffer.wrap(buffer), key, blobSize)
  }

  def delete(connection: Connection, key: Key): Unit = {
    val statement = connection.prepareStatement(
      "delete from pairs where id = ?")
    statement.setLong(1, key.value)
    val _ = statement.execute()
  }
}
