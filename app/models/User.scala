/*
 * Copyright (c) 2016 morpheby
 */

package models

/**
  * Created by morpheby on 27.5.16.
  */

case class User (name: String, passwordHash: Option[String], otpKey: Option[String])
