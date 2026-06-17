package com.juanpablo0612.tucargo.core.time

import java.time.LocalTime

actual fun currentHour(): Int = LocalTime.now().hour
