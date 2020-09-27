package com.femastudios.esr.datastruct.agents

import com.femastudios.debouncerthread.DebouncerThread
import com.femastudios.esr.Main
import com.femastudios.esr.availablity.AvailabilityHolder
import com.femastudios.esr.availablity.AvailabilityState
import com.femastudios.esr.availablity.GlobalAvailability
import com.femastudios.esr.availablity.MultiAvailabilityHolder
import com.femastudios.esr.datastruct.Agent
import com.femastudios.esr.datastruct.DebouncingInfo
import com.femastudios.esr.datastruct.Service
import com.femastudios.esr.datastruct.WebServerConfig
import mu.KotlinLogging
import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.db.MapDBContext
import org.telegram.abilitybots.api.objects.Ability
import org.telegram.abilitybots.api.objects.Locality
import org.telegram.abilitybots.api.objects.Privacy
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.io.File
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


private val logger = KotlinLogging.logger {}

open class A(val i : Int)

data class TelegramAgent(
    override val debounce: DebouncingInfo? = null,
    val token: String,
    val username: String,
    val userAdminId: Int,
    val chatIds: Set<Long>
) : Agent {

    private val telegramBotsApi = TelegramBotsApi()
    private var lastSentAvailability: GlobalAvailability? = null
    private lateinit var debouncer: DebouncerThread<GlobalAvailability>
    private val bot = object : AbilityBot(
        token,
        username,
        MapDBContext.onlineInstance(
            File(File(Main.CONFIG_DIR, "agents" + File.separator + "telegram" + File.separator + "bot").also {
                it.mkdirs()
            }, username).absolutePath
        )
    ) {
        override fun creatorId(): Int = userAdminId

        override fun onUpdateReceived(update: Update) {
            if (update.hasCallbackQuery() && update.callbackQuery.data == "/status") {
                sendCurrentStatus(listOf(update.callbackQuery.message.chatId))
            } else {
                super.onUpdateReceived(update)
            }
        }

        @Suppress("unused")
        fun sayCurrentStatus(): Ability {
            return Ability
                .builder()
                .name("status")
                .info("Returns the current status")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action { ctx ->
                    sendCurrentStatus(listOf(ctx.chatId()))
                }
                .build()
        }
    }

    override fun start() {
        ApiContextInitializer.init()
        try {
            telegramBotsApi.registerBot(bot)
        } catch (e: TelegramApiException) {
            logger.error(e) { "Cannot register bot '$username'" }
        }
        debouncer = (debounce ?: Main.global.debounce).newDebouncerThread<GlobalAvailability> {
            val lastAvailability = it.last()
            val lsa = lastSentAvailability
            // Skip if message is the same as the last sent one
            if (lsa == null || !lastAvailability.isTheSame(lsa)) {
                sendMessage(getAvailabilityMessage(lastAvailability))
            }
            lastSentAvailability = lastAvailability
        }
    }

    override fun onShutdownAnomaly(estimateCrashTime: Instant) {
        sendMessage(
            "An instance of ESR wasn't closed properly at " + estimateCrashTime.atZone(Main.global.timezone)
                .format(
                    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                )
        )
    }

    override fun onAvailabilityChanged(previous: GlobalAvailability?, current: GlobalAvailability) {
        if (previous == null || !current.isTheSame(previous)) {
            debouncer.debounce(current)
        }
    }

    private fun sendMessage(message: String, chatIds: Iterable<Long> = this@TelegramAgent.chatIds) {
        for (chatId in chatIds) {
            val command = SendMessage()
                .setChatId(chatId)
                .enableHtml(true)
                .disableWebPagePreview()
                .setText(message)
            val commands = ArrayList<InlineKeyboardButton>()
            commands.add(InlineKeyboardButton("Open").apply {
                url = Main.global.webServer.url.toString()
            })
            commands.add(InlineKeyboardButton("Get current status").apply {
                callbackData = "/status"
            })
            command.replyMarkup = InlineKeyboardMarkup().apply {
                keyboard = listOf(commands)
            }
            try {
                bot.execute(command)
            } catch (e: TelegramApiException) {
                logger.error(e) { "Cannot send message from bot '$username'" }
            }
        }
    }

    private fun getAvailabilityMessage(availability: GlobalAvailability, onlyUnavailable: Boolean = true): String {
        return availability.state.symbol + " <b>Global state</b>" + getStatusString(
            1,
            availability,
            onlyUnavailable,
            { group -> group.name }) { groupState ->
            getStatusString(2, groupState, onlyUnavailable, { server -> server.displayName }) { serverState ->
                getStatusString(
                    3,
                    serverState,
                    onlyUnavailable,
                    { service: Service -> service.test.displayName }) { serviceState ->
                    ": " + serviceState.message
                }
            }
        }
    }


    private fun <K, A : AvailabilityHolder> getStatusString(
        depth: Int,
        availability: MultiAvailabilityHolder<K, A>,
        onlyUnavailable: Boolean,
        childNamer: (K) -> String,
        childPrinter: (A) -> String
    ): String {
        if (availability.children.size == 1) {
            return childPrinter(availability.children.values.single())
        }
        val childMessages = LinkedHashMap<String, LinkedHashSet<Pair<K, A>>>()
        for ((service, serviceState) in availability.children) {
            if (!onlyUnavailable || serviceState.state != AvailabilityState.AVAILABLE) {
                childMessages.getOrPut(childPrinter(serviceState)) { LinkedHashSet() }
                    .add(service to serviceState)
            }
        }
        if (childMessages.isEmpty()) {
            return ""
        }
        var message = ":\n"
        for ((status, children) in childMessages) {
            message += "    ".repeat(depth)
            message += children.first().second.state.symbol + " "
            message += if (children.size == 1) {
                "<b>" + childNamer(children.single().first) + "</b>" + status + "\n"
            } else {
                "[" + children.joinToString(
                    ", ",
                    transform = { c -> "<b>" + childNamer(c.first) + "</b>" }) + "]" + status + "\n"
            }
        }
        return message.trimEnd()
    }

    private fun sendCurrentStatus(chatIds: Iterable<Long> = this@TelegramAgent.chatIds) {
        val availability = Main.globalAvailabilityComputer.getCurrentGlobalState(true)
        if (availability == null) {
            sendMessage("Status is not available yet", chatIds)
        } else {
            sendMessage(getAvailabilityMessage(availability, false), chatIds)
        }
    }
}