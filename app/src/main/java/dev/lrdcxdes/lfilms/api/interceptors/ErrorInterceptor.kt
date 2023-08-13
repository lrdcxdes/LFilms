package dev.lrdcxdes.lfilms.api.interceptors

import dev.lrdcxdes.lfilms.api.ApiError
import okhttp3.Interceptor
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ErrorInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val response: Response
        try {
            response = chain.proceed(chain.request())
        } catch (e: SocketTimeoutException) {
            throw ApiError("Connection timed out")
        } catch (e: SocketException) {
            throw ApiError("Connection error")
        } catch (e: UnknownHostException) {
            throw ApiError("Unknown host")
        } catch (e: Exception) {
            throw ApiError("Unknown error")
        }

        if (!response.isSuccessful) {
            val bodyStr = response.body?.string() ?: ""
            val errorTxt = "${chain.request().method} ${chain.request().url}\n" +
                    "Response code: ${response.code}\n" +
                    "Response body: $bodyStr"
            if (bodyStr.isNotEmpty()) {
                val json: JSONObject
                try {
                    json = JSONObject(bodyStr)
                } catch (e: Exception) {
                    throw ApiError(errorTxt)
                }
                if (json.has("message")) {
                    throw ApiError(json.getString("message"))
                }
            } else {
                throw ApiError(errorTxt)
            }
        }
        return response
    }
}