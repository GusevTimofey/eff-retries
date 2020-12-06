name := "eff-retries"

version := "0.1.0"

scalaVersion := "2.13.3"

scalacOptions ++= Seq(
  "-Ymacro-annotations",
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:postfixOps",
  "-feature",
  "-Xfatal-warnings"
)

libraryDependencies ++= Seq(
  "ru.tinkoff" %% "tofu-core"           % "0.8.0",
  "ru.tinkoff" %% "tofu-optics-macro"   % "0.8.0",
  "ru.tinkoff" %% "tofu-optics-interop" % "0.8.0",
  "ru.tinkoff" %% "tofu-logging"        % "0.8.0",
  "org.specs2" %% "specs2-core"         % "4.10.0",
  compilerPlugin("org.typelevel" %% "kind-projector"     % "0.11.0" cross CrossVersion.patch),
  compilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1")
)
