name := """sm-web"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, LauncherJarPlugin)

//val folderName =
//  if (System.getProperty("os.name").startsWith("Windows")) "windows" else "linux"

//val libPath = Seq("D:/study/lic/sma-lic/tf", s"lib/native/$folderName").mkString(java.io.File.pathSeparator)
//javaOptions in run += s"-Djava.library.path=$libPath"
//javaOptions in run += s"-Dorg.tensorflow.NativeLibrary.DEBUG=1"

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
libraryDependencies += "org.tensorflow" % "tensorflow" % "1.6.0"
libraryDependencies += "org.red5" % "red5-client" % "1.0.10-M5"
libraryDependencies += "nu.pattern" % "opencv" % "2.4.9-4"



// Make verbose tests
testOptions in Test := Seq(Tests.Argument(TestFrameworks.JUnit, "-a", "-v"))
