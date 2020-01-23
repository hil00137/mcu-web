const headerVue = new Vue({
    el: '#header',
    methods: {
        logout: function () {
            const check = confirm("로그아웃 하시겠습니까?");
            if (check) {
                location.href = "/user/logout";
            }
        },
        goHome: function () {
            location.href = "/home";
        },
        goGuide: function () {
            location.href = "/guide";
        },
        goBoard: function () {
            location.href = "/board";
        },
        goUserInfo : function () {
            location.href = "/user/info"
        }
    },
    mounted: function () {
        const headerList = ["home","guide","board","user/info"];
        const url = window.location.href;
        try {
            for(let i=0; i<headerList.length; i++) {
                if(url.match(headerList[i])) {
                    const nav = document.getElementById(headerList[i] + "Header").parentElement;
                    nav.classList.add("active");
                    break;
                }
            }
        } catch (e) {

        }
    }
});