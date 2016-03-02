(function (angular, $) {
    'use strict';
    angular
        .module('FileManagerApp')
        .controller('GeneratePwdCtrl', [
            '$scope',
            'dataservice',
            '$cookies',
            'Importer',
            GeneratePwdCtrl
        ]);

    function GeneratePwdCtrl($scope, dataservice, $cookies, Importer) {
        $scope.baseurl = '/importer';

        $scope.upload = null;
        $scope.fileUuid = null;
        $scope.flow = null;

        $scope.flowQuery = function(flowFile, flowChunk) {
            // function will be called for every request
            return {
                password: $scope.password, source: 'flow_query'
            };
        };

        $scope.generatePassword = function () {
            dataservice.generatePassword()
                .then(function (data) {
                    $scope.password = data;
                    $scope.showAlert();
                },
                function (error) {
                    console.error('failure loading pwd', error);
                });
        };

        $scope.showAlert = function () {
            $('#pwd-msg').css('display', '');
        };

        /*
         * Check to see if the user has uploaded a file.
         */
        $scope.isUploaded = function () {
            if ($scope.upload != null) {
                return true;
            } else {
                return false;
            }
        };

        /*
         * Update for progress.
         */
        $scope.fileProgress = function ($flow) {
            console.log("fileProgress");
            console.log($flow);

            $scope.flow = $flow;
        };

        /*
         * For when the file upload completes.
         */
        $scope.fileSuccess = function ($flow, $file, $message) {
            console.log("fileSuccess");
            console.log($flow);

            $scope.flow = $flow;
            $scope.fileUuid = $flow.files[0].uniqueIdentifier;

            Importer.get({id: $scope.fileUuid}, function (result) {
                $scope.upload = result;
            });
        };

        /*
         * Remove the Upload so we can go it again.
         */
        $scope.remove = function () {
            $scope.upload = null;
            $scope.fileUuid = null;

            if ($scope.flow != null) {
                $scope.flow.cancel();
                $scope.flow = null;
            }
        };

        /*
         * Due to difficulties with ng-flow/flowjs sending the right CSRF token
         * in the headers, we will just force it here as a query parameter since
         * this is allowed in Spring Security.
         *
         * NOTE: This could have been done with the query option to the flow-init.
         */
        $scope.uploaderTarget = function () {
            // var rtn = 'importer?_csrf=' + $cookies['CSRF-TOKEN'];
            var rtn = '/importer';
            console.log(rtn);
            return rtn;
        };
    }

})(angular, jQuery);