package com.mcu.util

import com.mcu.model.Mail
import com.mcu.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket
import java.util.*
import javax.naming.NamingException
import javax.naming.directory.InitialDirContext

@Component
class MailSendUtil {

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    @Autowired
    private lateinit var userService : UserService

    @Value("\${smtp.sendMail}")
    private lateinit var sendMail : String
    @Value("\${smtp.localhost}")
    private lateinit var smtp : String

    fun sendEmail(mail : Mail) : HashMap<String, String> {
        val resultMap = HashMap<String, String>()
        val domain = this.getDomain(mail.toMail)
        mail.smtp = smtp
        mail.fromMail = sendMail
        if (domain == null) {
            resultMap["result"] = "fail"
            resultMap["message"] = "잘못된 메일 형식입니다."
            return resultMap
        }
        val mxList =
                try {
                    this.searchMxRecord(domain)
                } catch (exception : Exception) {
                    resultMap["result"] = "fail"
                    resultMap["message"] = "해당 메일서버가 존재하지 않습니다."
                    return resultMap
                }

        var isSendSuccess = false
        var errorCode : String = ""
        mxList.forEach {
            if(isSendSuccess) {
                return@forEach
            }
            val socketAddress = InetSocketAddress(it, 25)
            val socket = Socket()
            socket.soTimeout = 10000
            try {
                socket.connect(socketAddress, 5000)
            } catch (e: Exception) {
                resultMap["result"] = "fail"
                errorCode = e.message.toString()
                logger.error(e.message)
                return@forEach
            }


            val br = BufferedReader(InputStreamReader(socket.getInputStream()))
            val pw = PrintWriter(socket.getOutputStream(), true)
            var currentMailStep = MailSendStep.CONNECT

            try{
                while (currentMailStep != MailSendStep.END && currentMailStep != MailSendStep.ERROR) {
                    currentMailStep = this.goStep(currentMailStep, mail, br, pw)
                }
            } catch (e: Exception) {
                resultMap["result"] = "fail"
                errorCode = e.message.toString()
                logger.error(e.message)
                return@forEach
            }

            if(currentMailStep == MailSendStep.END) {
                isSendSuccess = true
            } else {
                errorCode = currentMailStep.next
            }
            socket.close()
        }

        if(isSendSuccess) {
            resultMap["result"] = "success"
        } else {
            resultMap["result"] = "fail"
            resultMap["message"] = errorCode
        }

        return resultMap
    }

    fun getDomain(userEmail : String) : String? {
        if (userEmail.count { it == '@' } == 1) {
            return userEmail.split('@')[1]
        }
        return null
    }



    fun goStep(currentMailStep: MailSendStep, mail : Mail, reader : BufferedReader, writer : PrintWriter) : MailSendStep {
        var result : String?
        when (currentMailStep) {
            MailSendStep.CONNECT -> {
                result = reader.readLine()?:""
            }
            MailSendStep.HELO -> {
                writer.println("HELO ${mail.smtp}")
                result = reader.readLine()?:""
            }
            MailSendStep.MAIL_FROM -> {
                writer.println("MAIL FROM:<${mail.fromMail}>")
                result = reader.readLine()?:""
            }
            MailSendStep.RCPT_TO -> {
                writer.println("RCPT TO:<${mail.toMail}>")
                result = reader.readLine()?:""

            }
            MailSendStep.DATA -> {
                writer.println("DATA")
                result = reader.readLine()?:""
            }
            MailSendStep.DATA_BODY -> {
                mail.makeHeader()
                writer.println(mail.header)
                writer.println("")
                writer.println(mail.content)
                writer.println(".")
                result = reader.readLine()?:""
            }
            MailSendStep.QUIT -> {
                writer.println("QUIT")
                result = reader.readLine()?:""
            }
            else -> {
                result = ""
            }
        }
        logger.info(currentMailStep.step)
        logger.info("응답 : $result")
        if (result.startsWith(currentMailStep.success)) {
            return MailSendStep.valueOf(currentMailStep.next)
        }
        val nextStep = MailSendStep.ERROR
        nextStep.next = result
        return nextStep
    }

    fun searchMxRecord(domain : String): ArrayList<String> {
        val env = Hashtable<String, String>();
        env["java.naming.factory.initial"] = "com.sun.jndi.dns.DnsContextFactory";
        val ictx = InitialDirContext(env)
        // mx record 조회
        val attrs = ictx.getAttributes(domain, arrayOf("MX") );
        var attr = attrs.get("MX")

        //mx record 가 없을경우 a record 로 조회
        if (attr == null || attr.size() == 0) {
            ictx.getAttributes(domain,arrayOf("A"))
            attr = attrs.get("A")
            if (attr == null) {
                throw NamingException()
            }
        }
        val mxList = attr.all
        val result = ArrayList<String>()
        while(mxList.hasMore()) {
            val rows = mxList.next() as String
            val row = rows.split(" ") as ArrayList
            if(row[1].endsWith(".")) {
                row[1] = row[1].substring(0, row[1].length-1)
            }
            result.add(row[1])
        }
        return result
    }
}

enum class MailSendStep(val step : String, val success : String, var next : String) {
    CONNECT("CONNECT", "220", "HELO"),
    HELO("HELO", "250", "MAIL_FROM"),
    MAIL_FROM("MAIL_FROM", "250", "RCPT_TO"),
    RCPT_TO("RCPT_TO", "250", "DATA"),
    DATA("DATA", "354", "DATA_BODY"),
    DATA_BODY("DATA_BODY", "250", "QUIT"),
    QUIT("QUIT","221","END"),
    END("END", "", ""),
    ERROR("ERROR", "", "")
}
