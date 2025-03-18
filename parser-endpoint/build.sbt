enablePlugins(JavaAppPackaging)

scalaVersion := "2.13.14"

val AwsSdkVersion = "2.31.1"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-lambda-java-core" % "1.2.2",
  "com.amazonaws" % "aws-lambda-java-events" % "3.11.1",
  "com.amazonaws" % "aws-lambda-java-log4j2" % "1.5.1",
  "software.amazon.awssdk" % "s3" % AwsSdkVersion,
  "software.amazon.awssdk" % "apache-client" % AwsSdkVersion,
  "com.softwaremill.sttp.client3" %% "core" % "3.10.3"
)

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}