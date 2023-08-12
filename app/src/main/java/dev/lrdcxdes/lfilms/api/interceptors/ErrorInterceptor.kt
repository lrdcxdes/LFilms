package dev.lrdcxdes.lfilms.api.interceptors

import dev.lrdcxdes.lfilms.api.ApiError
import okhttp3.Interceptor
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class ErrorInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (!response.isSuccessful) {
            val errorTxt = "${chain.request().method} ${chain.request().url}\n" +
                    "Response code: ${response.code}\n" +
                    "Response body: ${response.body?.string()}"
            if (response.body != null) {
                val body = response.body!!.string()
                val json: JSONObject
                try {
                    json = JSONObject(body)
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