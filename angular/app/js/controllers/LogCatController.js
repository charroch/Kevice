'use strict';

angular.module('logcat').controllers('LogCatController', ['$scope', '$modal', '$log',
    function($scope, $modal, $log) {
        $log.print("hello")
    }
]);