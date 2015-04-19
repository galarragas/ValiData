import sbt.Keys._
import sbt._
import scoverage.ScoverageSbtPlugin

object ValiDataBuild extends Build {
    import Common._

    val project = Project(
    		id = "ValiData", base = file(".")
    	)
    	.settings(
    			version := "0.1",
    			scalaVersion := "2.11.6",
    			crossScalaVersions := Seq("2.11.6", "2.10.4"),
    			scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-feature"),
    			resolvers ++= commonResolvers,
    			organization := "uk.co.pragmasoft"
    		)
    	.settings(net.virtualvoid.sbt.graph.Plugin.graphSettings:_*)
    	.settings(
    			publishMavenStyle := true,
        		libraryDependencies ++= libraryClasspath
    		)
			.scoverageSettings()
			.settings (
				addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full)
			)


    lazy val libraryClasspath = 
    	scalaz ++
			monocle ++
    	scalaTest.forTest
}