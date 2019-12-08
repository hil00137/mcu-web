package com.mcu.util

class StringUtil {
    companion object {
        private const val scriptInjection = "[Script_Injection]"
        /**
         * 스크립트 인젝션 체크
         */
        fun checkScriptInjection(str : String) : String {
            var result = str
            if (result.contains("<script") || result.contains("script>")) {
                result = result.replace("<script","<")
                result = result.replace("script>",">")
                result += scriptInjection
            }
            return result
        }

        /**
         * 스크립트 인젝션 확인
         */
        fun isScriptInjection(str : String) = str.contains(scriptInjection)

        /**
         * 라벨제거
         */
        fun removeLabel(str : String) = str.replace(scriptInjection, "")
    }
}