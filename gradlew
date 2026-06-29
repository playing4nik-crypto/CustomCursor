#!/bin/sh
APP_HOME="$(cd "$(dirname "$0")" && pwd)"
exec java -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" \
  org.gradle.wrapper.GradleWrapperMain "$@"
