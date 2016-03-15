(function(window, angular, $) {
    'use strict';
    angular
        .module('FileManagerApp')
        .factory('item', [
            '$rootScope',
            '$http',
            '$q',
            '$translate',
            'fileManagerConfig',
            'chmod',
            'moment',
            'publicId',
            'SweetAlert',
            '$timeout',
            item
        ]);

    function item($rootScope, $http, $q, $translate, fileManagerConfig, Chmod, moment, publicId, SweetAlert, $timeout) {

        var Item = function(model, path) {
            var rawModel = {
                name: model && model.name || '',
                //displayName: model && model.displayName || '',
                shareLink: model && model.shareLink || '',
                createdBy: model && model.createdBy || '',
                session: model && model.session || '',
                publicId: publicId || model && model.publicId || '',
                path: path || [],
                type: model && model.type || 'file',
                size: model && parseInt(model.size || 0),
                date: parseMySQLDate(model && model.date),
                perms: new Chmod(model && model.rights),
                content: model && model.content || '',
                expires: model && model.expires || '',
                recursive: false,
                sizeKb: function() {
                    return Math.round(this.size / 1024, 1);
                },
                fullPath: function() {
                    return ('/' + this.path.join('/') + '/' + this.name).replace(/\/\//, '/');
                }
            };

            this.error = '';
            this.inprocess = false;

            this.model = angular.copy(rawModel);
            this.tempModel = angular.copy(rawModel);

            this.dateOptions = {
                formatYear: 'yy',
                startingDay: 1,
                minDate: new Date()
            };

            this.today = new Date();

            this.formats = ['dd-MMMM-yyyy', 'yyyy/MM/dd', 'dd.MM.yyyy', 'shortDate'];
            this.format = this.formats[0];
            this.opened = false;

            function parseMySQLDate(mysqlDate) {
                if (mysqlDate) {
                    return new Date(mysqlDate);
                } else {
                    return moment().format('MMMM Do YYYY, h:mm:ss a');
                }
                //var d = (mysqlDate || '').toString().split(/[- :]/);
                //return new Date(d[0], d[1] - 1, d[2], d[3], d[4], d[5]);
            }
        };

        Item.prototype.update = function() {
            angular.extend(this.model, angular.copy(this.tempModel));
        };

        Item.prototype.revert = function() {
            angular.extend(this.tempModel, angular.copy(this.model));
            this.error = '';
        };

        Item.prototype.deferredHandler = function(data, deferred, defaultMsg) {
            if (!data || typeof data !== 'object') {
                this.error = 'Bridge response error, please check the docs';
            }
            if (data.result && data.result.error) {
                this.error = data.result.error;
            }
            if (!this.error && data.error) {
                this.error = data.error.message;
            }
            if (!this.error && defaultMsg) {
                this.error = defaultMsg;
            }
            if (this.error) {
                return deferred.reject(data);
            }
            this.update();
            return deferred.resolve(data);
        };

        Item.prototype.createFolder = function() {
            var self = this;
            var deferred = $q.defer();
            var data = {params: {
                mode: 'addfolder',
                path: self.tempModel.path.join('/'),
                name: self.tempModel.name
            }};

            self.inprocess = true;
            self.error = '';
            $http.post(fileManagerConfig.createFolderUrl, data).success(function(data) {
                self.deferredHandler(data, deferred);
            }).error(function(data) {
                self.deferredHandler(data, deferred, $translate.instant('error_creating_folder'));
            })['finally'](function() {
                self.inprocess = false;
            });
        
            return deferred.promise;
        };

        Item.prototype.rename = function() {
            var self = this;
            var deferred = $q.defer();
            var data = {params: {
                mode: 'rename',
                path: self.model.fullPath(),
                newPath: self.tempModel.fullPath()
            }};
            self.inprocess = true;
            self.error = '';
            $http.post(fileManagerConfig.renameUrl, data).success(function(data) {
                self.deferredHandler(data, deferred);
            }).error(function(data) {
                self.deferredHandler(data, deferred, $translate.instant('error_renaming'));
            })['finally'](function() {
                self.inprocess = false;
            });
            return deferred.promise;
        };

        Item.prototype.copy = function() {
            var self = this;
            var deferred = $q.defer();
            var data = {params: {
                mode: 'copy',
                path: self.model.fullPath(),
                newPath: self.tempModel.fullPath()
            }};

            self.inprocess = true;
            self.error = '';
            $http.post(fileManagerConfig.copyUrl, data).success(function(data) {
                self.deferredHandler(data, deferred);
            }).error(function(data) {
                self.deferredHandler(data, deferred, $translate.instant('error_copying'));
            })['finally'](function() {
                self.inprocess = false;
            });
            return deferred.promise;
        };

        Item.prototype.compress = function() {
            var self = this;
            var deferred = $q.defer();
            var data = {params: {
                mode: 'compress',
                path: self.model.fullPath(),
                destination: self.tempModel.fullPath()
            }};

            self.inprocess = true;
            self.error = '';
            $http.post(fileManagerConfig.compressUrl, data).success(function(data) {
                self.deferredHandler(data, deferred);
            }).error(function(data) {
                self.deferredHandler(data, deferred, $translate.instant('error_compressing'));
            })['finally'](function() {
                self.inprocess = false;
            });
            return deferred.promise;
        };

        Item.prototype.extract = function() {
            var self = this;
            var deferred = $q.defer();
            var data = {params: {
                mode: 'extract',
                path: self.model.fullPath(),
                sourceFile: self.model.fullPath(),
                destination: self.tempModel.fullPath()
            }};

            self.inprocess = true;
            self.error = '';
            $http.post(fileManagerConfig.extractUrl, data).success(function(data) {
                self.deferredHandler(data, deferred);
            }).error(function(data) {
                self.deferredHandler(data, deferred, $translate.instant('error_extracting'));
            })['finally'](function() {
                self.inprocess = false;
            });
            return deferred.promise;
        };

        Item.prototype.getUrl = function(preview) {
            var path = this.model.fullPath();
            var data = {
                mode: 'download',
                preview: preview,
                publicId: this.model.publicId,
                path: path
            };
            return path && [fileManagerConfig.downloadFileUrl, $.param(data)].join('?');
        };

        Item.prototype.download = function(preview) {
            if (this.model.type !== 'dir') {
                window.open(this.getUrl(preview), '_blank', '');
            }
        };

        Item.prototype.getContent = function() {
            var self = this;
            var deferred = $q.defer();
            var data = {params: {
                mode: 'editfile',
                path: self.tempModel.fullPath()
            }};

            self.inprocess = true;
            self.error = '';
            $http.post(fileManagerConfig.getContentUrl, data).success(function(data) {
                self.tempModel.content = self.model.content = data.result;
                self.deferredHandler(data, deferred);
            }).error(function(data) {
                self.deferredHandler(data, deferred, $translate.instant('error_getting_content'));
            })['finally'](function() {
                self.inprocess = false;
            });
            return deferred.promise;
        };

        Item.prototype.remove = function() {
            var self = this;

            SweetAlert.swal({
                title: "Are you sure?",
                text: "You will be able to recover this file.",
                type: "warning",
                showCancelButton: true,
                confirmButtonColor: "#DD6B55",
                confirmButtonText: "Yes, delete it!",
                closeOnConfirm: false
            }, function() {

                var deferred = $q.defer();
                var data = {params: {
                    mode: 'delete',
                    path: self.tempModel.fullPath(),
                    publicId: self.tempModel.publicId,
                    session: self.tempModel.session
                }};

                self.inprocess = true;
                self.error = '';
                $http.post(fileManagerConfig.removeUrl, data).success(function(data) {
                    self.deferredHandler(data, deferred);
                    SweetAlert.swal("Deleted!", "Your file has been deleted.", "success");
                    $rootScope.$broadcast("item::refresh");
                }).error(function(data) {
                    self.deferredHandler(data, deferred, $translate.instant('error_deleting'));
                    SweetAlert.swal("Error", "An error occurred. Your file was not deleted :)", "error");
                })['finally'](function() {
                    self.inprocess = false;
                });
                return deferred.promise;
            });
        };

        Item.prototype.reactivate = function() {
            var self = this;
            var deferred = $q.defer();
            var data = {params: {
                mode: 'reactivate',
                content: self.tempModel.content,
                path: self.tempModel.fullPath(),
                publicId: self.tempModel.publicId
            }};

            self.inprocess = true;
            self.error = '';

            $http.post(fileManagerConfig.reactivateUrl, data).success(function(data) {
                $rootScope.$broadcast("item::refresh"); // trigger event that will refresh data on page
                self.deferredHandler(data, deferred);
            }).error(function(data) {
                self.deferredHandler(data, deferred, $translate.instant('error_modifying'));
            })['finally'](function() {
                self.inprocess = false;
            });
            return deferred.promise;
        };

        Item.prototype.edit = function() {
            var self = this;
            var deferred = $q.defer();
            var data = {params: {
                mode: 'savefile',
                content: self.tempModel.content,
                path: self.tempModel.fullPath()
            }};

            self.inprocess = true;
            self.error = '';

            $http.post(fileManagerConfig.editUrl, data).success(function(data) {
                self.deferredHandler(data, deferred);
            }).error(function(data) {
                self.deferredHandler(data, deferred, $translate.instant('error_modifying'));
            })['finally'](function() {
                self.inprocess = false;
            });
            return deferred.promise;
        };

        Item.prototype.changePermissions = function() {
            var self = this;
            var deferred = $q.defer();
            var data = {params: {
                mode: 'changepermissions',
                path: self.tempModel.fullPath(),
                perms: self.tempModel.perms.toOctal(),
                permsCode: self.tempModel.perms.toCode(),
                recursive: self.tempModel.recursive
            }};
            
            self.inprocess = true;
            self.error = '';
            $http.post(fileManagerConfig.permissionsUrl, data).success(function(data) {
                self.deferredHandler(data, deferred);
            }).error(function(data) {
                self.deferredHandler(data, deferred, $translate.instant('error_changing_perms'));
            })['finally'](function() {
                self.inprocess = false;
            });
            return deferred.promise;
        };


        Item.prototype.changeItemExpirationDate = function(expirationDate) {
            var self = this;
            self.model.date = expirationDate;
            var deferred = $q.defer();
            var data = {params: {
                mode: 'changeexpirationdate',
                publicId: self.tempModel.publicId,
                expirationDate: expirationDate
            }};

            self.inprocess = true;
            self.error = '';
            $http.post(fileManagerConfig.expirationDateUrl, data).success(function(data) {
                self.deferredHandler(data, deferred);
            }).error(function(data) {
                self.deferredHandler(data, deferred, $translate.instant('error_changing_date'));
            })['finally'](function() {
                self.inprocess = false;
            });
            return deferred.promise;
        };


        Item.prototype.isFolder = function() {
            return this.model.type === 'dir';
        };

        Item.prototype.isEditable = function() {
            return !this.isFolder() && fileManagerConfig.isEditableFilePattern.test(this.model.name);
        };

        Item.prototype.isImage = function() {
            return fileManagerConfig.isImageFilePattern.test(this.model.name);
        };

        Item.prototype.isCompressible = function() {
            return this.isFolder();
        };

        Item.prototype.isExtractable = function() {
            return !this.isFolder() && fileManagerConfig.isExtractableFilePattern.test(this.model.name);
        };
        
        // datepicker functions

        Item.prototype.clear = function () {
            this.model.date = null;
        };

        // Disable weekend selection
        Item.prototype.disabled = function(date, mode) {
            return ( mode === 'day' && ( date.getDay() === 0 || date.getDay() === 6 ) );
        };

        Item.prototype.toggleMin = function() {
            this.minDate = this.minDate ? null : new Date();
        };
        Item.prototype.toggleMin();

        Item.prototype.open = function($event, item) {
            $event.preventDefault();
            $event.stopPropagation();

            $timeout( function() {
                item.opened = !item.opened;
            }, 50);
        };
        
        // end datepicker functions

        return Item;
    }
})(window, angular, jQuery);
