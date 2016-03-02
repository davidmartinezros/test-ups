(function (angular, $) {
    'use strict';

    angular
        .module('FileManagerApp')
        .controller('submitController', submitController);

    function submitController($scope, $http, $timeout, $state, store) {

        var loader = $("#loader-div");
        var filePopup = $("#file-popup");

        $scope.filenames = [];
        $scope.isFile = false;
        $scope.fileCount = 0;

        $scope.baseurl = '/importer';

        $scope.add = function (files, events, flow) {
            $scope.fileCount = $scope.fileCount + files.length;
            if ($scope.fileCount > 3 && $scope.fileCount <= 10) {
                $scope.isFile = true;
            } else {
                $scope.isFile = false;
            }
        };

        $scope.upload_1 = function (flow) {
            loader.fadeIn(200);
        };

        $scope.fileSuccess = function (flow, condodata) {
            async.each(flow.files, function (file, callback) {
                $scope.filenames.push({
                    'name': file.uniqueIdentifier + '.jpeg'
                });
                callback();
            }, function (err) {
                if (err) {
                    console.log('error while adding the file name');
                } else {
                    var condoinfo = {
                        user_name: store.get('user_name'),
                        user_id: store.get('user_id'),
                        mobile_no: condodata.mobile_no,
                        bedroom: $scope.bedroomdata.selectedOption.name,
                        condo_name: condodata.condo_name,
                        fileNames: $scope.filenames
                    };

                    $http.post(baseurl + 'condosubmit', condoinfo).success(function (res, req) {
                        if (res.status == 1) {
                            $scope.condosuccessmsg = 'Condo Successfully Added.';
                            $scope.showcondosuccessmsg = true;
                            $timeout(function () {
                                $scope.showcondosuccessmsg = false;
                            }, 3000);
                            document.getElementById("condofrm").reset();
                            $scope.imagefiles = {};
                            loader.hide();
                            $state.go('tab.gallery');
                        } else {
                            $scope.submit_err_msg = "Condo Failed To Insert";
                            $scope.showsubmit_err_msg = true;
                            $timeout(function () {
                                $scope.showsubmit_err_msg = false;
                            }, 3000);
                            loader.hide();
                        }
                    }).error(function (err) {
                        loader.hide();
                        console.log('Connection Problem..');
                    });
                }
            });
        };


        /**
         @function condosubmit
         @returns success message
         @author sameer vedpathak
         @initialDate
         */

        $scope.condosubmit = function (condodata, valid) {
            if (valid) {
                if ($scope.imagefiles.length < 4) {
                    $scope.imagelimitmsg = 'Please Upload 4 Images';
                    $scope.showimagelimitmsg = true;
                    $timeout(function () {
                        $scope.showimagelimitmsg = false;
                    }, 3000);
                } else {
                    loader.fadeIn(200);
                    var condoinfo = {
                        user_name: store.get('user_name'),
                        user_id: store.get('user_id'),
                        mobile_no: condodata.mobile_no,
                        bedroom: $scope.bedroomdata.selectedOption.name,
                        condo_name: condodata.condo_name,
                        attachmentfile: $scope.imagefiles
                    };

                    $http.post(baseurl + 'condosubmit', condoinfo).success(function (res, req) {
                        if (res.status == 1) {
                            $scope.condosuccessmsg = 'Condo Successfully Added.';
                            $scope.showcondosuccessmsg = true;
                            $timeout(function () {
                                $scope.showcondosuccessmsg = false;
                            }, 3000);
                            document.getElementById("condofrm").reset();
                            $scope.imagefiles = {};
                            loader.hide();
                            $state.go('tab.gallery');
                        } else {
                            $scope.submit_err_msg = "Condo Failed To Insert";
                            $scope.showsubmit_err_msg = true;
                            $timeout(function () {
                                $scope.showsubmit_err_msg = false;
                            }, 3000);
                            loader.hide();
                        }
                    }).error(function (err) {
                        loader.hide();
                        console.log('Connection Problem..');
                    });
                }
            }

        };

        $scope.removeimage = function (img, flow) {
            for (var i in flow) {
                if (flow[i] === img) {
                    flow.splice(i, 1);
                    $scope.fileCount--;
                    if ($scope.fileCount > 3 && $scope.fileCount <= 10) {
                        $scope.isFile = true;
                    } else {
                        $scope.isFile = false;
                    }
                }
            }
        };

        $scope.closePopup = function () {
            filePopup.hide();
        };

    }

})(angular, jQuery);