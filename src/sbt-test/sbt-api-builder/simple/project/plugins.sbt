sys.props.get("plugin.version") match {
  case Some(pluginVersion) => addSbtPlugin("com.sirocchj" % "sbt-api-builder" % pluginVersion)
  case _                   => sys.error("""|The system property 'plugin.version' is not defined.
                                           |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
}
