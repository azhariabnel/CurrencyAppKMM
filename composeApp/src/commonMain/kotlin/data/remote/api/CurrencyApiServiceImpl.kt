package data.remote.api

import domain.model.ApiResponse
import domain.model.Currency
import domain.model.CurrencyApiService
import domain.model.RequestState
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class CurrencyApiService : CurrencyApiService {
    companion object{
        const val ENDPOINT = "https://api.currencyapi.com/v3/latest"
        const val API_KEY = "cur_live_HolfXxybAaxrkXeTaT9nmU9dfJAZ01KBOWWcnFol"
    }

    private val httpClient = HttpClient {
        install(ContentNegotiation){
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(HttpTimeout){
            requestTimeoutMillis = 15000
        }
        install(DefaultRequest){
            headers {
                append("apiKey", API_KEY)
            }
        }
    }

    override suspend fun getLatestExchangeRates(): RequestState<List<Currency>> {
        return try {
            val response = httpClient.get(ENDPOINT)
            if (response.status.value == 200){
                println("API_RESPONSE: ${response.body<String>()}")
                val apiResponse = Json.decodeFromString<ApiResponse>(response.body())
                RequestState.Success(data = apiResponse.data.values.toList())
            } else {
                RequestState.Error(message = "HTTP Error Code : ${response.status}")
            }
        } catch (e: Exception){
            RequestState.Error(message = e.message.toString())
        }
    }
}