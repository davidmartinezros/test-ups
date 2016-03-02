(function() {
    'use strict';

    angular
        .module('FileManagerApp')
        .factory('dataservice', ['$http', '$location', dataservice]);

    /* @ngInject */
    function dataservice($http, $location) {

        var service = {
            generatePassword: generatePassword,
            generateUniqueIdentifier: generateUniqueIdentifier,
            submitUploadSuccess: submitUploadSuccess
        };

        return service;

        //////////////////////////////

        function generatePassword() {
            return $http.get('/api/generatepwd')
                .then(generatePasswordComplete)
                .catch(function(message) {
                    console.log('XHR Failed for generatePassword ' + message);
                    $location.url('/');
                });

            function generatePasswordComplete(data, status, headers, config) {
                return data.data.response;
            }
        }

        function generateUniqueIdentifier() {
            $http.get('/importer/getUniqueIdentifier')
                .then(generatePasswordComplete)
                .catch(function(message) {
                    console.log('XHR Failed for generateUniqueIdentifier ' + message);
                    $location.url('/');
                });

            function generatePasswordComplete(data, status, headers, config) {
                return data;
            }
        }

        function submitUploadSuccess(data) {
            return $http.post('/importer/uploadSuccess', data)
                .then(submitUploadSuccessComplete)
                .catch(function(message) {
                    console.log('XHR Failed for submitUploadSuccess ' + message);
                    $location.url('/');
                });

            function submitUploadSuccessComplete(data, status, headers, config) {
                return data;
            }
        }
    }
})();