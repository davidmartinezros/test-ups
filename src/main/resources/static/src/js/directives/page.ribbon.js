(function() {
    'use strict';

    angular
        .module('FileManagerApp')
        .directive('pageRibbon', pageRibbon);

    function pageRibbon(ActiveProfiles, $translate, $rootScope) {
        var directive = {
            replace: true,
            restrict: 'AE',
            template: '<div class="github-fork-ribbon right-bottom fixed" title="{{ribbonTitle}}"></div>',
            link: linkFunc
        };

        return directive;

        function linkFunc(scope, element, attrs) {
            ActiveProfiles.get(function(result) {
                if (result.data.ribbonEnv) {
                    //$rootScope.$on('$translateChangeSuccess', function () {
                    scope.ribbonTitle = $translate.instant('ribbon.' + result.data.ribbonEnv);
                    //});
                    element.addClass(result.data.ribbonEnv);
                } else {
                    element.addClass('hidden');
                }
            });
        }
    }
})();