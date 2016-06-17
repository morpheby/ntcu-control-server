/*
 * Copyright (c) 2016 morpheby
 */

package models

import java.time.Instant

/**
  * Created by morpheby on 12.6.16.
  */
case class Resource[T] (name: String, data: T, created: Option[Instant])

