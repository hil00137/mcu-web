new Vue({
    el: '#header',
    methods : {
        logout : function() {
            const check = confirm("로그아웃 하시겠습니까?");
            if(check) {
                location.href = "/user/logout";
            }
        }
    }
});