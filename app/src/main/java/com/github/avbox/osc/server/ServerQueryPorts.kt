package com.github.avbox.osc.server

interface ServerQueryPort {

    fun dashData(): String?

    fun proxyLocal(params: MutableMap<String, String>): Array<Any?>?

    fun pushSubscribe(mode: String, name: String, url: String): Boolean = false
}

object ServerQueryPorts {

    @Volatile
    var ports: ServerQueryPort? = null
}
