package com.iptv.player.data.model
import com.google.gson.annotations.SerializedName

data class LoginCredentials(val host: String, val username: String, val password: String)
data class UserInfo(
    @SerializedName("username") val username: String = "", @SerializedName("password") val password: String = "",
    @SerializedName("auth") val auth: Int = 0, @SerializedName("status") val status: String = "",
    @SerializedName("exp_date") val expDate: String? = null, @SerializedName("is_trial") val isTrial: String = "0",
    @SerializedName("active_cons") val activeCons: String = "0", @SerializedName("max_connections") val maxConnections: String = "1"
)
data class ServerInfo(
    @SerializedName("url") val url: String = "", @SerializedName("port") val port: String = "",
    @SerializedName("server_protocol") val serverProtocol: String = "http", @SerializedName("timezone") val timezone: String = ""
)
data class AuthResponse(@SerializedName("user_info") val userInfo: UserInfo, @SerializedName("server_info") val serverInfo: ServerInfo)
