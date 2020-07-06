package me.lovesasuna.bot.function

import me.lovesasuna.bot.util.interfaces.FunctionListener
import me.lovesasuna.bot.util.network.NetWorkUtil
import me.lovesasuna.bot.util.photo.ImageUtil
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.*
import java.util.*
import javax.imageio.ImageIO

/**
 * @author LovesAsuna
 * @date 2020/4/22 23:50
 */
class RepeatDetect : FunctionListener {
    private val maps: MutableMap<Long, MutableList<MessageChain>> = HashMap()
    override suspend fun execute(event: MessageEvent, message: String, image: Image?, face: Face?): Boolean {
        val groupID = (event as GroupMessageEvent).group.id
        maps.putIfAbsent(groupID, ArrayList())
        val messageList = maps[groupID]!!

        if (messageList.size >= 3) {
            messageList.removeAt(0)
        }

        operate(event, messageList)

        if (messageList.size < 3) {
            return false
        }

        if (isRepeat(messageList)) {
            val messageChain = event.message
            when (messageChain.size) {
                2 -> {
                    when (messageChain[1]) {
                        is PlainText -> {
                            ArrayList<Char>().apply {
                                message.forEach {
                                    this.add(it)
                                }
                                this.shuffle()
                                val builder = StringBuilder()
                                this.forEach { builder.append(it) }
                                event.reply(builder.toString())
                            }
                        }
                        is Image -> {
                            val bufferedImage = ImageIO.read(NetWorkUtil.get(image!!.queryUrl())?.first).let {
                                when (Random().nextInt(2)) {
                                    0 -> ImageUtil.rotateImage(it, 180)
                                    1-> ImageUtil.reverseImage(it)
                                    else -> it
                                }
                            }
                            event.reply(event.uploadImage(bufferedImage))
                        }
                    }
                }
                else -> event.reply(messageList[2])
            }


            messageList.clear()
        }
        return true
    }

    private fun operate(event: MessageEvent, messageList: MutableList<MessageChain>) {
        messageList.add(event.message)
    }

    private fun isRepeat(messageList: MutableList<MessageChain>): Boolean {
        val first = messageList.first()
        val second = messageList[1]
        val third = messageList[2]
        if (first.contentEquals(second) && second.contentEquals(third)) {
            return true
        }
        return false
    }
}