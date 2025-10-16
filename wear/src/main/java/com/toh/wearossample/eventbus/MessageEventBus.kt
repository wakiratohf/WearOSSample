package com.toh.wearossample.eventbus

data class MessageEventBus(val event: Event, val extraValue: Any? = null)
