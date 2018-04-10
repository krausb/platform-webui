addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.13")
addSbtPlugin("com.dwijnand"      % "sbt-dynver"      % "2.0.0")
addSbtPlugin("com.dwijnand"      % "sbt-travisci"    % "1.1.1")
addSbtPlugin("com.geirsson"      % "sbt-scalafmt"    % "1.4.0")
addSbtPlugin("org.wartremover"   % "sbt-wartremover" % "2.2.1")

addSbtPlugin("com.github.gseitz" % "sbt-release"     % "1.0.7")

libraryDependencies += "org.slf4j" % "slf4j-nop" % "1.7.25" // Needed by sbt-git
