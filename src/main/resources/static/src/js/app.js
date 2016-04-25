(function (window, angular, $) {
    'use strict';

    var app = angular
        .module('FileManagerApp', [
            'pascalprecht.translate',
            'ngCookies',
            'ngResource',
            'ngCacheBuster',
            'flow',
            'ngclipboard',
            'oitozero.ngSweetAlert',
            'ui.bootstrap'
        ])

        .config(['flowFactoryProvider', function (flowFactoryProvider) {

            flowFactoryProvider.defaults = {
                target: '/importer',
                testChunks: true,
                forceChunkSize: true,
                simultaneousUploads: 8,
                singleFile: true,
                withCredentials: true,
                permanentErrors: [415, 500, 501],
                maxChunkRetries: 1,
                chunkRetryInterval: 5000,
                generateUniqueIdentifier: function () {
                    var request = new XMLHttpRequest();
                    request.open("GET", "importer/getUniqueIdentifier", false);
                    request.send();
                    return request.responseText;
                }
            };


            // Can be used with different implementations of Flow.js
            //flowFactoryProvider.factory = fustyFlowFactory;
        }])
        .config(function ($httpProvider, httpRequestInterceptorCacheBusterProvider) {
            //enable CSRF
            $httpProvider.defaults.xsrfCookieName = 'CSRF-TOKEN';
            $httpProvider.defaults.xsrfHeaderName = 'X-CSRF-TOKEN';

            //Cache everything except rest api requests
            httpRequestInterceptorCacheBusterProvider.setMatchlist([/.*api.*/, /.*protected.*/], true);
        })
        .config(function ($translateProvider) {
            $translateProvider.translations('en', {
                "ribbon": {
                    "dev": "Development",
                    "test": "Test",
                    "uat": "User Acceptance"
                }
            });
            $translateProvider.preferredLanguage('en');
        })
        .constant('moment', moment);  // register moment.js data/time library


    /**
     * jQuery inits
     */
    $(window.document).on('shown.bs.modal', '.modal', function () {
        window.setTimeout(function () {
            $('[autofocus]', this).focus();
        }.bind(this), 100);
    });

    $(window.document).on('click', function () {
        $('#context-menu').hide();
    });

    $(window.document).on('contextmenu', '.main-navigation .table-files td:first-child, .iconset a.thumbnail', function (e) {
        $('#context-menu').hide().css({
            left: e.pageX,
            top: e.pageY
        }).show();
        e.preventDefault();
    });

})(window, angular, jQuery);