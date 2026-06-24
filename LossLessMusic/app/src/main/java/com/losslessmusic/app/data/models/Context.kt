package com.losslessmusic.app.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Context(
    val client: Client,
    val user: User = User(),
    val request: Request = Request(),
)

@Serializable
data class Client(
    val clientName: String,
    val clientVersion: String,
    val androidSdkVersion: Int = 34,
    val osName: String = "Android",
    val osVersion: String = "14",
    val platform: String = "MOBILE",
    val userAgent: String,
    val acceptHeader: String = "application/json",
    val deviceMake: String = "OnePlus",
    val deviceModel: String = "13R",
    val userInterfaceTheme: String = "USER_INTERFACE_THEME_DARK",
)

@Serializable
data class User(
    val lockedSafetyMode: Boolean = false,
)

@Serializable
data class Request(
    val useSsl: Boolean = true,
    val internalExperimentFlags: List<String> = emptyList(),
    val consistencyTokenJars: List<ConsistencyTokenJar> = emptyList(),
)

@Serializable
data class ConsistencyTokenJar(
    val token: String = "",
    val version: String = "",
)

@Serializable
data class YouTubeLocale(
    val gl: String = "US",
    val hl: String = "en-US",
)
