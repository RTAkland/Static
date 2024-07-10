/*
 * Copyright 2023 RTAkland
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import kotlin.system.exitProcess


if (args.isEmpty() || args.size != 2) {
    println("请按照以下格式输入参数以启动反向代理: <LOCAL:PORT> <REMOTE:PORT>")
    exitProcess(-1)
}

val local = args[0]
val remote = args[1]
val lHost = local.split(":").first()
var lPort = 80
val rHost = remote.split(":").first()
var rPort = 80
if (local.contains(":")) {
    lPort = local.split(":").last().toInt()
}

if (remote.contains(":")) {
    rPort = remote.split(":").last().toInt()
}

if (lHost == rHost && lPort == rPort) {
    println("请勿反向代理相同的地址!")
    exitProcess(-1)
}

val serverSocket = ServerSocket(lPort)

println("开始转发 $lHost:$lPort => $rHost:$rPort \n")
while (true) {
    var client = Socket()
    try {
        client = Socket(rHost, rPort)
        val connection = serverSocket.accept()
        Thread { forward(connection, client) }.start()
        Thread { forward(client, connection) }.start()
        println(
            "新的连接: ${connection.inetAddress.hostAddress}:${connection.port}"
        )
    } catch (_: SocketException) {
        println("无法连接至后端服务器: $rHost:$rPort , 请检查后端服务器是否正常!")
        client.close()
    }
}

fun forward(reader: Socket, writer: Socket) {
    val buffer = ByteArray(2048)
    val inputStream = reader.getInputStream()
    val outputStream = writer.getOutputStream()
    while (!reader.isClosed && !writer.isClosed) {
        try {
            val length = inputStream.read(buffer)
            if (length == -1) break
            outputStream.write(buffer, 0, length)
            outputStream.flush()
        } catch (e: SocketException) {
            println("断开连接: ${writer.inetAddress}")
            reader.close()
            writer.close()
            return
        }
    }
}

