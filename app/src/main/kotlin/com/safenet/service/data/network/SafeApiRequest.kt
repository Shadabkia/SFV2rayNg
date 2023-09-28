package com.safenet.service.data.network

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber
import java.io.IOException


@Suppress("UNCHECKED_CAST")
abstract class SafeApiRequest {

    fun <T : Any> apiRequest(call: suspend () -> Response<T>): Flow<Result<T>> {
        return flow {
            emit(Result.Loading())
            try {
                // Do call and wait for response
                val response = call()
                if (response.isSuccessful) {
                    emit(Result.Success(response.body()!!))
                } else {

                    response.errorBody()?.string()?.let { errorBody ->
                        when (isJSONValid(errorBody)) {
                            true -> try {
                                // Convert Error or ErrorResponse
                                val message = Gson().fromJson(
                                    errorBody,
                                    ErrorResponse::class.java
                                )?.message?.fold("") { acc, s -> "$acc\n$s" }?.trim()
                                // Convert Error to AuthErrorResponse (for keycloak) when cant convert to ErrorResponse
                                    ?: Gson().fromJson(
                                        errorBody,
                                        AuthErrorResponse::class.java
                                    )?.errorDescription ?: "An unexpected error occurred 1"

                                Timber.e("Error message: $message")

                                emit(Result.Error(message))
                                // Json format is different with kotlin class
                            } catch (e: JSONException) {
                                emit(Result.Error("Message is not compatible with ErrorResponse class!!"))
                            }
                            // Bad gate way error (returns html)
                            false -> emit(Result.Error("Server error"))
                        }
                    }
                }
            } catch (ex: NoInternetException) {
                emit(Result.Error(ex.message!!))
            } catch (ex: HttpException) {
                emit(Result.Error(ex.localizedMessage ?: "An unexpected error occurred 2"))
            } catch (ex: IOException) {
                emit(Result.Error("Couldn't reach server. Check your internet connection."))
            }!!
        }
    }
}

@Suppress("BlockingMethodInNonBlockingContext")
suspend fun ResponseBody.stringSuspending() =
    withContext(Dispatchers.IO) { string() }

// Check input is json or not
fun isJSONValid(input: String): Boolean {
    try {
        JSONObject(input)
    } catch (ex: JSONException) {
        try {
            JSONArray(input)
        } catch (ex: JSONException) {
            return false
        }
    }
    return true
}

// inline fun <reified T> Moshi.jsonToObject(json: String): T? =
//    adapter(T::class.java).fromJson(json)