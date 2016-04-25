(function() {
    'use strict';

    angular
        .module('FileManagerApp')
        .service('ActiveProfiles', ActiveProfiles);

    ActiveProfiles.$inject = ['$resource'];

    function ActiveProfiles ($resource) {
        var service = $resource('api/activeProfiles', {}, {
            'get': { method: 'GET', params: {}, isArray: false,
                interceptor: {
                    response: function(response) {
                        // expose response
                        return response;
                    }
                }
            }
        });

        return service;
    }
})();