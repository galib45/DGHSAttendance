package com.galib.dghsattendance.data

import android.content.Context
import android.util.Log
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

sealed class ApiResult {
    data class Success(val data: String) : ApiResult()
    data class Redirect(val location: String) : ApiResult()
    data class Error(val exception: ApiException) : ApiResult()
}

sealed class ApiException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class NoInternet : ApiException("No internet connection. Please check your network.")
    class ServerError : ApiException("Server is temporarily unavailable. Please try again later.")
    class Timeout : ApiException("Request timed out. Please check your connection and try again.")

    data class HttpError(val code: Int) : ApiException("Request failed with code $code")
    data class ParseError(val originalError: String) : ApiException("Failed to parse server response")
    data class UnknownError(val originalError: String) : ApiException("An unexpected error occurred: $originalError")
}

object AttendanceApi {
    private const val TAG = "AttendanceApi"
    private const val TIMEOUT_SECONDS = 60L
    private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/121.0"
    private const val ORIGIN = "http://attendance.dghs.gov.bd"
    private const val HOST = "attendance.dghs.gov.bd"

    const val BASE_URL = "$ORIGIN/biometric"

    lateinit var cookieJar: PersistentCookieJar
    lateinit var client: OkHttpClient

    fun init(context: Context) {
        cookieJar = PersistentCookieJar(
            SetCookieCache(), SharedPrefsCookiePersistor(context)
        )
        client = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .followRedirects(false)
            .cookieJar(cookieJar)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", USER_AGENT)
                    .header("Host", HOST)
                    .header("Origin", ORIGIN)
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    private fun executeRequest(
        request: Request,
        callback: (ApiResult) -> Unit
    ) {
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Request Failed", e)
                val apiException = when (e) {
                    is UnknownHostException -> ApiException.NoInternet()
                    is SocketTimeoutException -> ApiException.Timeout()
                    else -> ApiException.UnknownError(e.message ?: "Unknown network error")
                }
                callback(ApiResult.Error(apiException))
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    try {
                        when {
                            it.isSuccessful -> {
                                val body = it.body.string()
                                if (body.isEmpty()) {
                                    callback(ApiResult.Error(ApiException.ParseError("Empty response from server")))
                                } else {
                                    callback(ApiResult.Success(body))
                                }
                            }
                            it.isRedirect -> {
                                callback(ApiResult.Redirect(it.headers["Location"].toString()))
                            }
                            it.code in 500..599 -> {
                                callback(ApiResult.Error(ApiException.ServerError()))
                            }
                            else -> {
                                println("HTTP code: ${it.code}")
                                callback(ApiResult.Error(ApiException.HttpError(it.code)))
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing response", e)
                        callback(
                            ApiResult.Error(
                                ApiException.ParseError(
                                    e.message ?: "Failed to read response"
                                )
                            )
                        )
                    }
                }
            }
        })
    }

    fun checkIfLoggedIn(callback: (ApiResult) -> Unit) {
        val request = Request.Builder()
            .url("$BASE_URL/home")
            .build()
        executeRequest(request, callback)
    }

    fun login(email: String, password: String, callback: (ApiResult) -> Unit) {
        val url = "$BASE_URL/login"
        val formBody = FormBody.Builder()
            .add("username", email)
            .add("password", password)
            .add("submit", "Submit")
            .build()
        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .header("Referer", url)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .build()
        executeRequest(request, callback)
    }

    fun logout() {
        cookieJar.clear()
    }

    fun searchIndividual(hrisId: String, fromDate: String, toDate: String, callback: (ApiResult) -> Unit) {
        val url = "$BASE_URL/report/individual_attendance"
        val yearReport = toDate.split("/").last()
        val formBody = FormBody.Builder()
            .add("from_date", fromDate)
            .add("to_date", toDate)
            .add("hris_id", hrisId)
            .add("year_report", yearReport)
            .add("search", "Search")
            .build()
        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .header("Referer", url)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .build()
        executeRequest(request, callback)
    }

    fun searchFacility(facilityCode: String, callback: (ApiResult) -> Unit) {
        val url = "$BASE_URL/report/daily_attendance/$facilityCode"
        val request = Request.Builder()
            .url(url)
            .build()
        executeRequest(request, callback)
    }
}