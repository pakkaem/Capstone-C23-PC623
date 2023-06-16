package com.example.billboardinsight.response

import com.google.gson.annotations.SerializedName

data class AuthResponse(


	@field:SerializedName("error")
	val error: Boolean,

	@field:SerializedName("user")
	val loginUser: User? = null
)

data class User(

	@field:SerializedName("password")
	val password: String,

	@field:SerializedName("nama")
	val nama: String,

	@field:SerializedName("id")
	val id: String,

	@field:SerializedName("email")
	val email: String,

	@field:SerializedName("token")
	val token: String? = null

)
