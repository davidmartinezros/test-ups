(function (window, angular, $) {
    'use strict';

    var app = angular
        .module('FileManagerApp', [
            'pascalprecht.translate',
            'ngCookies',
            'ngResource',
            'ngCacheBuster',
            'flow',
            'ngclipboard'
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

            flowFactoryProvider.on('catchAll', function (event) {
                console.log('catchAll', arguments);
                if (arguments[0] === "complete") {
                    // send a message to the server?
                    console.log("completed the file upload");
                }

                /**
                 averageSpeed: 0
                 chunks: Array[24]
                 currentSpeed: 0
                 error: false
                 file: File
                 flowObj: d
                 name: "ClamXav_2.8.9.dmg"
                 paused: false
                 relativePath: "ClamXav_2.8.9.dmg"
                 size: 24476585
                 uniqueIdentifier: "4d13475c-9bcd-49e4-938d-03a3a54b2144"
                 */
                if (arguments[0] === "fileSuccess") {
                    var flowObj = arguments[1];
                    var password = $('#passwordField').val();
                    var expiration = $('#expiration').val();

                    $.ajax({
                        url: "importer/uploadSuccess",
                        type: "get",
                        data: {
                            fileName: flowObj.file.name,
                            uniqueIdentifier: flowObj.uniqueIdentifier,
                            paused: flowObj.paused,
                            password: password,
                            expiration: expiration
                        },
                        success: function(response) {
                            console.log("success");
                            location.href= '/uploadComplete?publicId=' + response.response.publicId;
                        },
                        error: function(xhr) {
                            console.log("error sending uploadSuccess");
                        }
                    });
                }
            });
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