/**=========================================================
 * Module: sweetalert.js
 =========================================================*/

(function() {
    'use strict';

    angular
        .module('FileManagerApp')
        .controller('SweetAlertController', SweetAlertController);

    SweetAlertController.$inject = ['SweetAlert'];
    function SweetAlertController(SweetAlert) {
        var vm = this;

        activate();

        ////////////////

        function activate() {
          vm.demo3 = function() {
              SweetAlert.swal('Good job!', 'You clicked the button!', 'success');
          };
        }
    }
})();
