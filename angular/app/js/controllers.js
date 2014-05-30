'use strict';

/* Controllers */

angular.module('myApp.controllers', [])
    .controller('MyCtrl1', ['$scope',
        function($scope) {

        }
    ])
    .controller('MyCtrl2', ['$scope',
        function($scope) {

        }
    ])
    .controller('DeviceController', ['$scope', '$http',
        function($scope, $http) {
            $http.get('http://localhost:8081/devices').success(function(devices) {
                $scope.devices = devices;
            });
        }
    ])
    .controller('LogCatController', ['$scope', '$modal', '$log',
        function($scope, $modal, $log) {
            $scope.open = function(serial) {
                $scope.serial = serial;

                $modal.open({
                    controller: 'LogController',
                    resolve: {
                        serial: function() {
                            return $scope.serial;
                        }
                    },
                    templateUrl: 'partials/logcat.html'
                })
            }
        }
    ]);

function LogController($modalInstance, serial) {
    console.log("ser " + serial + $modalInstance)
    console.log($modalInstance)
}