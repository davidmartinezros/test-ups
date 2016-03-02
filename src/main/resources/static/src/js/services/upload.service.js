(function () {
    'use strict';

    angular
        .module('FileManagerApp')
        .factory('Importer', Importer);

    Importer.$inject = ['$resource', 'DateUtils'];

    function Importer($resource, DateUtils) {
        return $resource('importer/:id', {}, {
            'query': {method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    data = angular.fromJson(data);
                    data.uploadedAt = DateUtils.convertLocaleDateFromServer(data.uploadedAt);
                    data.completedAt = DateUtils.convertLocaleDateFromServer(data.completedAt);
                    return data;
                }
            },
            'update': {
                method: 'PUT',
                transformRequest: function (data) {
                    data.uploadedAt = DateUtils.convertLocaleDateToServer(data.uploadedAt);
                    data.completedAt = DateUtils.convertLocaleDateToServer(data.completedAt);
                    return angular.toJson(data);
                }
            },
            'save': {
                method: 'POST',
                transformRequest: function (data) {
                    data.uploadedAt = DateUtils.convertLocaleDateToServer(data.uploadedAt);
                    data.completedAt = DateUtils.convertLocaleDateToServer(data.completedAt);
                    return angular.toJson(data);
                }
            }
        });
    }
})();
