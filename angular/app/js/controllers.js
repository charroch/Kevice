'use strict';

/* Controllers */

angular.module('myApp.controllers', ['logcat'])
  .controller('MyCtrl1', ['$scope', function($scope) {

  }])
  .controller('MyCtrl2', ['$scope', function($scope) {

  }])
  .controller('DeviceController', ['$scope', '$http', function($scope, $http) {
        $http.get('http://localhost:8081/devices').success(function(devices) {
            $scope.devices = devices;
        });
  }])
  .controller('ModalDemoCtrl', ['$scope', '$modal', '$log', function ($scope, $modal, $log) {

                                                              $scope.items = ['item1', 'item2', 'item3'];

                                                              $scope.open = function (size) {

                                                                var modalInstance = $modal.open({
                                                                  templateUrl: 'myModalContent.html',
                                                                  controller: ModalInstanceCtrl,
                                                                  size: size,
                                                                  resolve: {
                                                                    serial: function () {
                                                                      return 12;
                                                                    }
                                                                  }
                                                                });

                                                                modalInstance.result.then(function (selectedItem) {
                                                                  $scope.selected = selectedItem;
                                                                }, function () {
                                                                  $log.info('Modal dismissed at: ' + new Date());
                                                                });
                                                              };
                                                            }
                                                            ]);

// Please note that $modalInstance represents a modal window (instance) dependency.
// It is not the same as the $modal service used above.

var ModalInstanceCtrl = function ($scope, $modalInstance) {

  $scope.items = ["1", "2"];
  $scope.selected = {
    item: $scope.items[0]
  };

  $scope.ok = function () {
    $modalInstance.close($scope.selected.item);
  };

  $scope.cancel = function () {
    $modalInstance.dismiss('cancel');
  };
};