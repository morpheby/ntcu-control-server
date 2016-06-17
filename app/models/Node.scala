/*
 * Copyright (c) 2016 morpheby
 */

package models

/**
  * Created by morpheby on 12.6.16.
  */
case class Node(
               nodeId: String,
               group: Option[String],
               devices: Seq[String]
               )
