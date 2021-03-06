/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.spark.sql.hive.llap

import org.apache.spark.SparkContext
import java.sql.ResultSet
import java.sql.Statement

object TestUtils {
  lazy val sparkContext = new SparkContext("local", "test")

  // Query HS2 for the necessary settings
  def updateConfWithMiniClusterSettings(connectionUrl: String, userName: String): Unit = {
    val conn = DefaultJDBCWrapper.getConnector(None, url = connectionUrl, userName)
    val settings = Seq(
      "hive.llap.daemon.service.hosts",
      "hive.zookeeper.quorum",
      "hive.zookeeper.client.port"
    )

    val stmt = conn.createStatement()
    settings.foreach { setting =>
      val value = getConfSetting(stmt, setting)
      println("Setting " + setting + " to " + value)
      sparkContext.hadoopConfiguration.set(setting, value)
    }
    stmt.close()
  }

  private def getConfSetting(stmt: Statement, confSetting: String): String = {
    val res = stmt.executeQuery("set " + confSetting)
    res.next()
    val fields: Array[String] = res.getString(1).split("=", 2)
    fields(1)
  }
}