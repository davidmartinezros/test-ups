(function () {
    'use strict';

    angular
        .module('FileManagerApp')
        .service('DateUtils', DateUtils);

    function DateUtils() {

        var service = {
            convertLocaleDateToServer: convertLocaleDateToServer,
            convertLocaleDateFromServer: convertLocaleDateFromServer,
            convertDateTimeFromServer: convertDateTimeFromServer
        };

        return service;

        /////////////

        function convertLocaleDateToServer(date) {
            if (date) {
                var utcDate = new Date();
                utcDate.setUTCDate(date.getDate());
                utcDate.setUTCMonth(date.getMonth());
                utcDate.setUTCFullYear(date.getFullYear());
                return utcDate;
            } else {
                return null;
            }
        }

        function convertLocaleDateFromServer(date) {
            if (date) {
                var dateString = date.split("-");
                return new Date(dateString[0], dateString[1] - 1, dateString[2]);
            }
            return null;
        }

        function convertDateTimeFromServer(date) {
            if (date) {
                return new Date(date);
            } else {
                return null;
            }
        }
    }
})();
