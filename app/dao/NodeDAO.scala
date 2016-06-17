/*
 * Copyright (c) 2016 morpheby
 */

package dao

import models.{Device, Node}
import javax.inject.Inject

/**
  * Created by morpheby on 12.6.16.
  */
class NodeDAO @Inject() () {
  def get(id: String): Node = {
    Node("testNode", None, List("ntcu-xdevice"))
  }
}
