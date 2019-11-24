package com.mcu.handler

import org.slf4j.LoggerFactory
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class WebInterceptor : HandlerInterceptorAdapter() {

    companion object {
        val logger = LoggerFactory.getLogger(this.javaClass)!!
    }

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val requestPath = request.servletPath
        logger.info("[$requestPath]")

        return super.preHandle(request, response, handler)
    }
}