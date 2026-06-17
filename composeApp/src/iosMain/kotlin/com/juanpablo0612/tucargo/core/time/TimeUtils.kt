package com.juanpablo0612.tucargo.core.time

import platform.Foundation.NSCalendar
import platform.Foundation.NSDate

actual fun currentHour(): Int =
    NSCalendar.currentCalendar.component(
        platform.Foundation.NSCalendarUnitHour,
        fromDate = NSDate()
    ).toInt()
