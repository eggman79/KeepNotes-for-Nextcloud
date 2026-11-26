package com.keepnc.data.remote

import android.util.Base64
import android.util.Log
import com.keepnc.data.auth.TokenStorage
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * OkHttp interceptor that:
 * 1. Attaches HTTP Basic Auth to requests intended for Nextcloud using credentials from [TokenStorage].
 * 2. Dynamically rewrites placeholder URLs to the stored server URL so Retrofit
 *    works even if created before login or if the server URL changes.
 *
 * BEGINNER NOTE: OkHttp interceptors run on the background thread that made the
 * network call.
 */
class AuthInterceptor @Inject constructor(
    private val tokenStorage: TokenStorage
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url
        val requestBuilder = originalRequest.newBuilder()

        val rawServerUrl = tokenStorage.getServerUrl()
        val targetBaseUrl = if (!rawServerUrl.isNullOrBlank()) {
            val serverUrl = ServerUrlUtils.normalizeServerUrl(rawServerUrl)
            "$serverUrl/index.php/apps/notes/api/v1/".toHttpUrlOrNull()
        } else null

        // Only attach Nextcloud credentials if the request is targeting Nextcloud or placeholder
        val isTargetingNextcloud = targetBaseUrl != null && (
            originalUrl.host == "placeholder.invalid" || originalUrl.host == targetBaseUrl.host
        )

        val credentials = tokenStorage.getCredentials()
        if (credentials != null && isTargetingNextcloud) {
            // Build "loginName:appPassword", base64-encode it, attach as Authorization header
            val encoded = Base64.encodeToString(
                "${credentials.loginName}:${credentials.appPassword}".toByteArray(Charsets.UTF_8),
                Base64.NO_WRAP
            )
            requestBuilder.header("Authorization", "Basic $encoded")
            // Required by Nextcloud OCS API endpoints
            requestBuilder.header("OCS-APIREQUEST", "true")
        }

        // Dynamic server URL resolution (only for Nextcloud API / placeholder endpoints)
        if (targetBaseUrl != null && isTargetingNextcloud) {
            val newUrlBuilder = targetBaseUrl.newBuilder()
            // Find path relative to api/v1/
            val v1Index = originalUrl.pathSegments.indexOf("v1")
            val relativeSegments = if (v1Index != -1 && v1Index < originalUrl.pathSegments.size - 1) {
                originalUrl.pathSegments.subList(
                    v1Index + 1,
                    originalUrl.pathSegments.size
                )
            } else if (v1Index == -1) {
                // Fallback if URL had no v1 prefix — extract relative endpoint segments
                val notesIndex = originalUrl.pathSegments.indexOf("notes")
                if (notesIndex != -1) {
                    originalUrl.pathSegments.subList(notesIndex, originalUrl.pathSegments.size)
                } else {
                    originalUrl.pathSegments.filter { it.isNotBlank() }
                }
            } else {
                emptyList()
            }

            for (segment in relativeSegments) {
                newUrlBuilder.addPathSegment(segment)
            }
            originalUrl.encodedQuery?.let { newUrlBuilder.encodedQuery(it) }
            requestBuilder.url(newUrlBuilder.build())
        }

        val finalRequest = requestBuilder.build()
        Log.d("AuthInterceptor", "--> ${finalRequest.method} ${finalRequest.url}")
        val response = chain.proceed(finalRequest)
        Log.d("AuthInterceptor", "<-- ${response.code} ${finalRequest.url}")
        return response
    }
}
