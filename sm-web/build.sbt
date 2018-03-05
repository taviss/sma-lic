name := """sm-web"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.12.4"

crossScalaVersions := Seq("2.11.12", "2.12.4")

libraryDependencies += guice

// Test Database
libraryDependencies += "com.h2database" % "h2" % "1.4.196"

// Testing libraries for dealing with CompletionStage...
libraryDependencies += "org.assertj" % "assertj-core" % "3.6.2" % Test
libraryDependencies += "org.awaitility" % "awaitility" % "2.0.0" % Test

libraryDependencies += javaJpa
libraryDependencies += "org.hibernate" % "hibernate-core" % "4.3.10.Final"
libraryDependencies += "org.hibernate" % "hibernate-entitymanager" % "4.3.10.Final"
libraryDependencies += "org.hibernate" % "hibernate-search-orm" % "5.3.0.Final"
libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.36"
libraryDependencies += javaJdbc
//libraryDependencies += cache
libraryDependencies += "org.projectlombok" % "lombok" % "1.16.8"
libraryDependencies += "org.mockito" % "mockito-core" % "1.10.19"
libraryDependencies += evolutions
//libraryDependencies += javaWs
//libraryDependencies += filters
//libraryDependencies += "org.apache.commons" % "commons-io" % "1.3.2"


// Make verbose tests
testOptions in Test := Seq(Tests.Argument(TestFrameworks.JUnit, "-a", "-v"))
