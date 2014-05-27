'use strict';

angular.module('logcat').controllers('LogCatController',
    ['$scope', '$modal', '$log', function ($scope, $modal, $log) {
        $log.print("hello")
    }])
    ;

var LogCatDialogCtrl = function ($scope, $timeout, $dialog){
  $timeout(function(){
    $dialog.dialog({}).open('modalContent.html');
  }, 3000);
}