var CookieLanguages = ["bg", "br", "ca", "cs", "da", "de", "el", "en", "es", "fi", "fr", "hr", "hu", "it", "nl", "no", "oc", "pl", "pt", "ro", "ru", "se", "sk", "sl", "tr"],
    cookieLawStates = ["AT", "BE", "BG", "BR", "CY", "CZ", "DE", "DK", "EE", "EL", "ES", "FI", "FR", "GB", "HR", "HU", "IE", "IT", "LT", "LU", "LV", "MT", "NL", "NO", "PL", "PT", "RO", "SE", "SI", "SK"];
function setupCookieBar() {
    var e,
        t,
        o,
        n,
        a,
        c,
        r,
        l = (function () {
            var e = document.getElementsByTagName("script");
            for (i = 0; i < e.length; i += 1) if (e[i].hasAttribute("src") && ((path = e[i].src), path.indexOf("cookiebar") > -1)) return path;
        })(),
        d = !1,
        s = !1,
        m = k();
    if (("CookieDisallowed" == m && (f(), b("cookiebar", "CookieDisallowed")), void 0 === m))
        if (I("noGeoIp")) (d = !0), p();
        else {
            var u = new XMLHttpRequest();
            u.open("GET", "https://freegeoip.app/json/", !0),
                (u.onreadystatechange = function () {
                    if (4 === u.readyState) {
                        if ((clearTimeout(y), 200 === u.status)) {
                            var e = JSON.parse(u.responseText).country_code;
                            cookieLawStates.indexOf(e) > -1 ? (d = !0) : ((s = !0), b("cookiebar", "CookieAllowed"), I("refreshPage") && window.location.reload());
                        } else d = !0;
                        p();
                    }
                });
            var y = setTimeout(function () {
                console.log("cookieBAR - Timeout for ip geolocation"), (u.onreadystatechange = function () {}), u.abort(), (d = !0), p();
            }, 1500);
            u.send();
        }
    function p() {
        document.cookie.length > 0 || (null !== window.localStorage && window.localStorage.length > 0) ? (void 0 === k() ? (d = !0) : (s = !0)) : (d = !1),
            I("always") && (d = !0),
            !0 === d &&
                !1 === s &&
                (function () {
                    var i = (function () {
                            var e = I("forceLang");
                            return !1 === e && (e = navigator.language || navigator.userLanguage), (e = e.substr(0, 2)), CookieLanguages.indexOf(e) < 0 && (e = "en"), e;
                        })(),
                        d = "";
                    I("theme") && (d = "-" + I("theme"));
                    var s = l.replace(/[^\/]*$/, ""),
                        m = l.indexOf(".min") > -1 ? ".min" : "",
                        u = document.createElement("link");
                    u.setAttribute("rel", "stylesheet"), u.setAttribute("href", s + "themes/cookiebar" + d + m + ".css"), document.head.appendChild(u);
                    var y = new XMLHttpRequest();
                    y.open("GET", s + "lang/" + i + ".html", !0),
                        (y.onreadystatechange = function () {
                            if (4 === y.readyState && 200 === y.status) {
                                var i = document.createElement("div");
                                (i.innerHTML = y.responseText),
                                    document.getElementsByTagName("body")[0].appendChild(i),
                                    (e = document.getElementById("cookie-bar")),
                                    (t = document.getElementById("cookie-bar-button")),
                                    (o = document.getElementById("cookie-bar-button-no")),
                                    (n = document.getElementById("cookie-bar-prompt")),
                                    (a = document.getElementById("cookie-bar-prompt-button")),
                                    (c = document.getElementById("cookie-bar-prompt-close")),
                                    (promptContent = document.getElementById("cookie-bar-prompt-content"));
                                const l = I("logoUrl");
                                l && (document.getElementById("cookie-bar-prompt-logo").style.cssText = `background:url('${decodeURIComponent(l)}') no-repeat;height:70px;background-size:contain;cursor:default`),
                                    (r = document.getElementById("cookie-bar-no-consent")),
                                    (thirdparty = document.getElementById("cookie-bar-thirdparty")),
                                    (tracking = document.getElementById("cookie-bar-tracking")),
                                    (customize = document.getElementById("cookie-bar-customize-block")),
                                    (buttonCustomize = document.getElementById("cookie-bar-button-customize")),
                                    (buttonSaveCustomized = document.getElementById("cookiebar-save-customized")),
                                    (customizeBlock = document.getElementById("cookie-bar-customize-block")),
                                    (customizeTracking = document.getElementById("cookiebar-tracking-input")),
                                    (customizeThirdParty = document.getElementById("cookiebar-third-party-input")),
                                    (scrolling = document.getElementById("cookie-bar-scrolling")),
                                    (privacyPage = document.getElementById("cookie-bar-privacy-page")),
                                    (privacyLink = document.getElementById("cookie-bar-privacy-link")),
                                    (mainBarPrivacyLink = document.getElementById("cookie-bar-main-privacy-link")),
                                    I("showNoConsent") || ((r.style.display = "none"), (o.style.display = "none")),
                                    I("showCustomConsent") && (buttonCustomize.style.display = "none"),
                                    I("blocking") && (v(n, 500), (c.style.display = "none")),
                                    I("thirdparty") ? ((thirdparty.style.display = "block"), (customizeThirdParty.style.display = "block")) : ((thirdparty.style.display = "none"), (customizeThirdParty.style.display = "none")),
                                    I("tracking") ? ((tracking.style.display = "block"), (customizeTracking.style.display = "block")) : ((tracking.style.display = "none"), (customizeTracking.style.display = "none")),
                                    I("hideDetailsBtn") && (a.style.display = "none"),
                                    I("scrolling") && (scrolling.style.display = "inline-block"),
                                    I("top") ? ((e.style.top = 0), E("top")) : ((e.style.bottom = 0), E("bottom")),
                                    I("privacyPage") && ((privacyLink.href = g()), (privacyPage.style.display = "inline-block")),
                                    I("showPolicyLink") && I("privacyPage") && ((mainBarPrivacyLink.href = g()), (mainBarPrivacyLink.style.display = "inline-block")),
                                    I("customize") ? ((customizeBlock.style.display = "block"), (buttonCustomize.style.display = "block")) : ((customizeBlock.style.display = "none"), (buttonCustomize.style.display = "none")),
                                    (function () {
                                        if (
                                            (t.addEventListener("click", function () {
                                                b("cookiebar", "CookieAllowed"), h(), B(n, 250), B(e, 250), I("refreshPage") && window.location.reload();
                                            }),
                                            o.addEventListener("click", function () {
                                                var t = r.textContent.trim(),
                                                    o = !0;
                                                I("noConfirm") || (o = window.confirm(t)), !0 === o && (f(), b("cookiebar", "CookieDisallowed"), h(), B(n, 250), B(e, 250));
                                            }),
                                            buttonSaveCustomized.addEventListener("click", function () {
                                                b("cookiebar", "CookieCustomized"),
                                                    b("cookiebar-tracking", document.getElementById("cookiebar-tracking").checked),
                                                    b("cookiebar-third-party", document.getElementById("cookiebar-third-party").checked),
                                                    h(),
                                                    B(n, 250),
                                                    B(e, 250),
                                                    I("refreshPage") && window.location.reload();
                                            }),
                                            a.addEventListener("click", function () {
                                                v(n, 250);
                                            }),
                                            c.addEventListener("click", function () {
                                                B(customize, 0), B(n, 250);
                                            }),
                                            buttonCustomize.addEventListener("click", function () {
                                                v(customize, 0), v(n, 250);
                                            }),
                                            I("scrolling"))
                                        ) {
                                            var i = document.body.getBoundingClientRect().top,
                                                l = !1;
                                            window.addEventListener("scroll", function () {
                                                !1 === l &&
                                                    (document.body.getBoundingClientRect().top - i > 250 || document.body.getBoundingClientRect().top - i < -250) &&
                                                    (b("cookiebar", "CookieAllowed"), h(), B(n, 250), B(e, 250), (l = !0), I("refreshPage") && window.location.reload());
                                            });
                                        }
                                    })(),
                                    v(e, 250),
                                    E();
                            }
                        }),
                        y.send();
                })();
    }
    function g() {
        return decodeURIComponent(I("privacyPage"));
    }
    function k() {
        var e = document.cookie.match(/(;)?cookiebar=([^;]*);?/);
        return null == e ? void 0 : decodeURI(e[2]);
    }
    function b(e, t) {
        var o = 30;
        I("remember") && (o = I("remember"));
        var n = new Date();
        n.setDate(n.getDate() + parseInt(o));
        var i = encodeURI(t) + (null === o ? "" : "; expires=" + n.toUTCString() + ";path=/");
        document.cookie = e + "=" + i;
    }
    function f() {
        document.cookie.split(";").forEach(function (e) {
            document.cookie = e.replace(/^\ +/, "").replace(/\=.*/, "=;expires=" + new Date().toUTCString() + ";path=/");
        }),
            null !== localStorage && localStorage.clear();
    }
    function v(e, t) {
        var o = e.style;
        (o.opacity = 0),
            (o.display = "block"),
            (function e() {
                !((o.opacity -= -0.1) > 0.9) && setTimeout(e, t / 10);
            })();
    }
    function B(e, t) {
        var o = e.style;
        (o.opacity = 1),
            (function e() {
                (o.opacity -= 0.1) < 0.1 ? (o.display = "none") : setTimeout(e, t / 10);
            })();
    }
    function E(e) {
        setTimeout(function () {
            var t = document.getElementById("cookie-bar").clientHeight,
                o = document.getElementsByTagName("body")[0],
                n = o.currentStyle || window.getComputedStyle(o);
            switch (e) {
                case "top":
                    o.style.marginTop = parseInt(n.marginTop) + t + "px";
                    break;
                case "bottom":
                    o.style.marginBottom = parseInt(n.marginBottom) + t + "px";
            }
        }, 300);
    }
    function h() {
        var e = document.getElementById("cookie-bar").clientHeight;
        if (I("top")) {
            var t = parseInt(document.getElementsByTagName("body")[0].style.marginTop);
            document.getElementsByTagName("body")[0].style.marginTop = t - e + "px";
        } else {
            var o = parseInt(document.getElementsByTagName("body")[0].style.marginBottom);
            document.getElementsByTagName("body")[0].style.marginBottom = o - e + "px";
        }
    }
    function I(e) {
        var t = l.split(e + "=");
        return !!t[1] && t[1].split(/[&?]+/)[0];
    }
}
document.addEventListener("DOMContentLoaded", function () {
    setupCookieBar();
});
