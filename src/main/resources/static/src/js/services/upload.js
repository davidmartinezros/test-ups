/*global angular */
'use strict';

/**
 * The main app module
 *
 * @name upload
 * @type {angular.Module}
 */
(function () {
    /*
     * You can use CORS changing the target be sure to specify the domain in the UploadServlet file (java)
     */
    var upload = angular.module('UploadModule', ['flow']).config(
        ['flowFactoryProvider', function (flowFactoryProvider) {
            flowFactoryProvider.defaults = {
                target: '/upload',
                permanentErrors: [500, 501],
                maxChunkRetries: 1,
                chunkRetryInterval: 5000,
                simultaneousUploads: 4,
                progressCallbacksInterval: 1,
                withCredentials: true,
                method: "octet"
            };
            flowFactoryProvider.on('catchAll', function (event) {
                console.log('catchAll', arguments);
            });
            // Can be used with different implementations of Flow.js
            // flowFactoryProvider.factory = fustyFlowFactory;
        }]);

    upload.controller('ButtonController', function () {
        this.pause = false;
        this.cancel = false;
        this.stop = false;

        this.startPause = function (file) {
            if (file.paused || file.isComplete()) {
                this.pause = false;
                return this.pause;
            }

            if (file.isUploading()) {
                this.pause = true;
                return this.pause;
            }

        };

        this.startCancel = function (file) {
            file.isUploading();
            this.stop = true;
            return this.stop;
        };

    });

})();