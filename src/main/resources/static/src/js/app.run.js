(function () {
    'use strict';

    angular
        .module('FileManagerApp')
        .run(appRun);

    appRun.$inject = ['$rootScope', 'SweetAlert'];

    function appRun($rootScope, SweetAlert) {

        $scope.$on('flow::fileSuccess', function (event, $file, $message, $data) {

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
            var flowObj = arguments[1];
            var password = $('#passwordField').val();
            var expiration = $('#expiration').val();

            SweetAlert.swal({
                title: 'File Uploaded!',
                text: 'Your file has been!',
                type: 'success',
                showCancelButton: false,
                confirmButtonColor: '#DD6B55',
                confirmButtonText: 'Ok',
                cancelButtonText: 'No, cancel plx!',
                closeOnConfirm: true,
                closeOnCancel: true
            }, function (isConfirm) {
                if (isConfirm) {
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
                        success: function (response) {
                            console.log("success");
                            location.href = '/uploadComplete?publicId=' + response.response.publicId;
                        },
                        error: function (xhr) {
                            console.log("error sending uploadSuccess");
                        }
                    });
                } else {
                    SweetAlert.swal('Cancelled', 'Your imaginary file is safe :)', 'error');
                }
            });
        });

    }

})();

