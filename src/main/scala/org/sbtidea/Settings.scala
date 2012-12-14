package org.sbtidea

import sbt._
import sbt.Load.BuildStructure
import scala.Some

case class Settings(projectRef: ProjectRef, buildStruct: BuildStructure, state: State) {
  def optionalSetting[A](key: SettingKey[A], pr: ProjectRef = projectRef, bs: BuildStructure = buildStruct) : Option[A] = key in pr get bs.data

  def logErrorAndFail(errorMessage: String): Nothing = {
    state.log.error(errorMessage)
    throw new IllegalArgumentException()
  }

  def setting[A](key: SettingKey[A], errorMessage: => String, pr: ProjectRef = projectRef) : A = {
    optionalSetting(key, pr) getOrElse {
      logErrorAndFail(errorMessage)
    }
  }

  def settingWithDefault[A](key: SettingKey[A], defaultValue: => A) : A = {
    optionalSetting(key) getOrElse defaultValue
  }

  def task[A](key: TaskKey[A]): A = optionalTask(key).getOrElse(logErrorAndFail("Missing task key: " + key.key.label))

  def optionalTask[A](key: TaskKey[A]): Option[A] = EvaluateTask(buildStruct, key, state, projectRef).map(_._2) match {
    case Some(Value(v)) => Some(v)
    case _ => None
  }
}
