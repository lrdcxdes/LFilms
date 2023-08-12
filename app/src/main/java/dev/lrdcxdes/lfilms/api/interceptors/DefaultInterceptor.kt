package dev.lrdcxdes.lfilms.api.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class DefaultInterceptor(
    private val userAgent: String,
    private val host: String,
    private val scheme: String
) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(
            chain.request()
                .newBuilder()
                .header("User-Agent", userAgent)
                .header("X-Requested-With", "XMLHttpRequest")
                .url(
                    chain.request().url.newBuilder()
                        .host(host)
                        .scheme(scheme)
                        .build()
                )
                .build()
        )
    }
}