package com.arkindustries.gogreen.api

import android.content.Context
import com.arkindustries.gogreen.api.interceptor.AuthInterceptor
import com.arkindustries.gogreen.api.services.CategoryService
import com.arkindustries.gogreen.api.services.ChatService
import com.arkindustries.gogreen.api.services.ContractsService
import com.arkindustries.gogreen.api.services.FileService
import com.arkindustries.gogreen.api.services.JobService
import com.arkindustries.gogreen.api.services.ProfileService
import com.arkindustries.gogreen.api.services.ProposalService
import com.arkindustries.gogreen.api.services.SkillService
import com.arkindustries.gogreen.api.services.UserService
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


object RetrofitClient {
    private const val BASE_URL = "https://absolutely-sharp-llama.ngrok-free.app/api/v1/"
    private fun createOkHttpClient(context: Context): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context))
            .addInterceptor(logging)
            .build()
    }

    private fun createRetrofit(context: Context): Retrofit {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .client(createOkHttpClient(context))
            .build()
    }

    fun createCategoryService(context: Context): CategoryService {
        return createRetrofit(context).create(CategoryService::class.java)
    }

    fun createContractService(context: Context): ContractsService {
        return createRetrofit(context).create(ContractsService::class.java)
    }

    fun createJobService(context: Context): JobService {
        return createRetrofit(context).create(JobService::class.java)
    }

    fun createProposalService(context: Context): ProposalService {
        return createRetrofit(context).create(ProposalService::class.java)
    }

    fun createSkillService(context: Context): SkillService {
        return createRetrofit(context).create(SkillService::class.java)
    }

    fun createUserService(context: Context): UserService {
        return createRetrofit(context).create(UserService::class.java)
    }

    fun createFileService(context: Context): FileService {
        return createRetrofit(context).create(FileService::class.java)
    }

    fun createProfileService(context: Context): ProfileService {
        return createRetrofit(context).create(ProfileService::class.java)
    }

    fun createChatService(context: Context): ChatService {
        return createRetrofit(context).create(ChatService::class.java)
    }
}
