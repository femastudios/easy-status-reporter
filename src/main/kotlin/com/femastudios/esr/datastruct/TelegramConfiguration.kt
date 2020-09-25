package com.femastudios.esr.datastruct

data class TelegramConfiguration(
		val botToken: String,
		val botUsername: String,
		val userAdminId: Long,
		val chatIds: Set<Long>
)