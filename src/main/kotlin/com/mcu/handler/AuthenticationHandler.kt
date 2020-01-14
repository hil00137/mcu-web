package com.mcu.handler

import com.mcu.model.User
import com.mcu.service.UserService
import com.mcu.util.HashUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class AuthFailureHandler : SimpleUrlAuthenticationFailureHandler() {

    @Autowired
    private lateinit var userService: UserService

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    override fun onAuthenticationFailure(request: HttpServletRequest?, response: HttpServletResponse?, exception: AuthenticationException?) {
        response!!.status = HttpServletResponse.SC_UNAUTHORIZED
        logger.info("Login Fail")
        val user = userService.getUserByUserId(request?.getParameter("userId")?:"")
        if (user == null) {
            request?.setAttribute("errorMessage", "존재하지 않는 사용자입니다.")
        } else if (user.mailAuth == "wait") {
            request?.setAttribute("errorMessage", "메일인증이 되지않았습니다.")
        } else if (user.mailAuth == "fail") {
            request?.setAttribute("errorMessage", user.mailAuthFailReason)
        } else {
            request?.setAttribute("errorMessage", "비밀번호가 틀렸습니다.")
        }
        request?.getRequestDispatcher("/user/login")?.forward(request,response)
    }
}

@Component
class AuthSuccessHandler : SimpleUrlAuthenticationSuccessHandler() {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    override fun onAuthenticationSuccess(request: HttpServletRequest?, response: HttpServletResponse?, authentication: Authentication?) {
        response!!.status = HttpServletResponse.SC_OK
        logger.info("Login Success")
        redirectStrategy.sendRedirect(request, response, "/home")
    }
}

@Component("authProvider")
class AuthProvider : AuthenticationProvider {

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    @Autowired
    lateinit var userService : UserService

    override fun authenticate(authentication: Authentication?): Authentication? {
        val userId = authentication?.name?:""
        val password = HashUtil.sha512(password = authentication?.credentials.toString())
        logger.info("Try Login : $userId")

        val user = userService.getUserByUserId(userId)
        if ( user == null || user.password != password || user.mailAuth != "success") {
            return null
        }

        val grantedAuthorityList = ArrayList<GrantedAuthority>()

        if (user.auth == "admin") {
           grantedAuthorityList.add(SimpleGrantedAuthority("ROLE_ADMIN"))
        } else {
            grantedAuthorityList.add(SimpleGrantedAuthority("ROLE_USER"))
        }

        return McuAuthentication(userId, password, grantedAuthorityList, user)
    }

    override fun supports(authentication: Class<*>?): Boolean {
        return authentication?.name == "org.springframework.security.authentication.UsernamePasswordAuthenticationToken"
    }

}

class McuAuthentication(principal: String, credentials: String, authorities: ArrayList<GrantedAuthority>, var user: User) : UsernamePasswordAuthenticationToken(principal, credentials, authorities)