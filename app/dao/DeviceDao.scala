/*
 * Copyright (c) 2016 morpheby
 */

package dao

import javax.inject.Inject

import models.Device

/**
  * Created by morpheby on 12.6.16.
  */
class DeviceDAO @Inject()() {
  def get(id: String): Device = {
    Device("ntcu-xdevice")
  }
}
