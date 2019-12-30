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
        }
    },
    mounted: function () {
        const url = window.location.href;
        const index = url.lastIndexOf("/");
        const current = url.substring(index + 1);
        try {
            const nav = document.getElementById(current + "Header").parentElement;
            nav.classList.add("active");
        } catch (e) {

        }
    }
});