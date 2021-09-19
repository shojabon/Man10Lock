package com.shojabon.man10lock.dataClass

import com.shojabon.man10lock.enums.Man10LockPermission
import java.util.*

class Man10LockUser(val name: String, val uuid: UUID, val permission: Man10LockPermission) {
}