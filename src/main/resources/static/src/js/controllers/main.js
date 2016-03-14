(function (window, angular, $) {
    'use strict';
    angular
        .module('FileManagerApp')
        .controller('FileManagerCtrl', [
            '$scope',
            '$translate',
            '$cookies',
            'fileManagerConfig',
            'item',
            'fileNavigator',
            'fileUploader',
            'moment',
            '$timeout',
            FileManagerCtrl
        ]);

        function FileManagerCtrl($scope, $translate, $cookies, fileManagerConfig, Item, fileNavigator, fileUploader, moment, $timeout) {

            $scope.config = fileManagerConfig;
            $scope.reverse = false;
            $scope.predicate = ['model.type', 'model.name'];
            $scope.order = function (predicate) {
                $scope.reverse = ($scope.predicate[1] === predicate) ? !$scope.reverse : false;
                $scope.predicate[1] = predicate;
            };

            // set expires date
            $scope.expires = function () {
                $scope.dt2 = moment().day(7).format("dddd, MMMM Do YYYY");;
            };
            $scope.expires();

            $scope.query = '';
            $scope.temp = new Item();
            $scope.fileNavigator = new fileNavigator();
            $scope.fileUploader = fileUploader;
            $scope.uploadFileList = [];
            $scope.viewTemplate = $cookies.viewTemplate || 'main-table.html';
            $scope.minDate;

            $scope.today = function() {
                $scope.minDate = new Date();
            };
            $scope.today();


            // begin datepicker
            // $scope.today = function(item) {
            //     item.today();
            // };
            //
            // $scope.clear = function (item) {
            //     item.clear();
            // };
            //
            // // Disable weekend selection
            // $scope.disabled = function(item, date, mode) {
            //     item.disabled(date, mode)
            // };
            //
            // $scope.toggleMin = function(item) {
            //     item.toggleMin();
            // };
            //
            // $scope.open = function(item, $event) {
            //     item.open($event);
            // };

            // end datepicker


            $scope.$on("item::refresh", function() {
                $scope.fileNavigator.refresh(); // refresh the list when an item is reactivated
            });

            $scope.setTemplate = function (name) {
                $scope.viewTemplate = $cookies.viewTemplate = name;
            };

            $scope.changeLanguage = function (locale) {
                if (locale) {
                    return $translate.use($cookies.language = locale);
                }
                $translate.use($cookies.language || fileManagerConfig.defaultLang);
            };

            $scope.touch = function (item) {
                item = item instanceof Item ? item : new Item();
                item.revert();
                $scope.temp = item;
            };

            $scope.home = function() {
                window.location = "/";
            };

            $scope.smartClick = function (item) {
                if (item.isFolder()) {
                    return $scope.fileNavigator.folderClick(item);
                }
                if (item.isImage()) {
                    return $scope.openImagePreview(item);
                }
                if (item.isEditable()) {
                    return $scope.openEditItem(item);
                }
            };

            $scope.openImagePreview = function (item) {
                item.inprocess = true;
                $scope.modal('imagepreview')
                    .find('#imagepreview-target')
                    .attr('src', item.getUrl(true))
                    .unbind('load error')
                    .on('load error', function () {
                        item.inprocess = false;
                        $scope.$apply();
                    });
                return $scope.touch(item);
            };

            $scope.openEditItem = function (item) {
                item.getContent();
                $scope.modal('edit');
                return $scope.touch(item);
            };

            $scope.modal = function (id, hide) {
                return $('#' + id).modal(hide ? 'hide' : 'show');
            };

            $scope.isInThisPath = function (path) {
                var currentPath = $scope.fileNavigator.currentPath.join('/');
                return currentPath.indexOf(path) !== -1;
            };

            $scope.edit = function (item) {
                item.edit().then(function () {
                    $scope.modal('edit', true);
                });
            };

            $scope.changePermissions = function (item) {
                item.changePermissions().then(function () {
                    $scope.modal('changepermissions', true);
                });
            };

            $scope.changeExpirationDate = function (item, expirationDate) {
                item.changeItemExpirationDate(expirationDate).then(function () {
                    $scope.fileNavigator.refresh();
                    //console.log('fixed the date setting');
                });
            };

            $scope.copy = function (item) {
                var samePath = item.tempModel.path.join() === item.model.path.join();
                if (samePath && $scope.fileNavigator.fileNameExists(item.tempModel.name)) {
                    item.error = $translate.instant('error_invalid_filename');
                    return false;
                }
                item.copy().then(function () {
                    $scope.fileNavigator.refresh();
                    $scope.modal('copy', true);
                });
            };

            $scope.compress = function (item) {
                item.compress().then(function () {
                    $scope.fileNavigator.refresh();
                    if (!$scope.config.compressAsync) {
                        return $scope.modal('compress', true);
                    }
                    item.asyncSuccess = true;
                }, function () {
                    item.asyncSuccess = false;
                });
            };

            $scope.extract = function (item) {
                item.extract().then(function () {
                    $scope.fileNavigator.refresh();
                    if (!$scope.config.extractAsync) {
                        return $scope.modal('extract', true);
                    }
                    item.asyncSuccess = true;
                }, function () {
                    item.asyncSuccess = false;
                });
            };

            $scope.remove = function (item) {
                item.remove().then(function () {
                    $scope.fileNavigator.refresh();
                    $scope.modal('delete', true);
                });
            };

            $scope.rename = function (item) {
                var samePath = item.tempModel.path.join() === item.model.path.join();
                if (samePath && $scope.fileNavigator.fileNameExists(item.tempModel.name)) {
                    item.error = $translate.instant('error_invalid_filename');
                    return false;
                }
                item.rename().then(function () {
                    $scope.fileNavigator.refresh();
                    $scope.modal('rename', true);
                });
            };

            $scope.createFolder = function (item) {
                var name = item.tempModel.name && item.tempModel.name.trim();
                item.tempModel.type = 'dir';
                item.tempModel.path = $scope.fileNavigator.currentPath;
                if (name && !$scope.fileNavigator.fileNameExists(name)) {
                    item.createFolder().then(function () {
                        $scope.fileNavigator.refresh();
                        $scope.modal('newfolder', true);
                    });
                } else {
                    item.error = $translate.instant('error_invalid_filename');
                    return false;
                }
            };

            $scope.uploadFiles = function () {
                $scope.fileUploader.upload($scope.uploadFileList, $scope.fileNavigator.currentPath).then(function () {
                    $scope.fileNavigator.refresh();
                    $scope.modal('uploadfile', true);
                }, function (data) {
                    var errorMsg = data.result && data.result.error || $translate.instant('error_uploading_files');
                    $scope.temp.error = errorMsg;
                });
            };

            $scope.getQueryParam = function (param) {
                var found;
                window.location.search.substr(1).split('&').forEach(function (item) {
                    if (param === item.split('=')[0]) {
                        found = item.split('=')[1];
                        return false;
                    }
                });
                return found;
            };

            $scope.changeLanguage($scope.getQueryParam('lang'));
            $scope.isWindows = $scope.getQueryParam('server') === 'Windows';
            $scope.fileNavigator.refresh();

            // Open the file upload modal after startup
            //$timeout(function() {
            //    $scope.modal('uploadfile', false);
            //}, 1000);
        }
})(window, angular, jQuery);
