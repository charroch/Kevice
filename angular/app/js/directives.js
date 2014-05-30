'use strict';

/* Directives */

var sock = new SockJS('http://localhost:8081/echo');

var app = angular.module('myApp.directives', []).
directive('appVersion', ['version',
    function(version) {
        return function(scope, elm, attrs) {
            elm.text(version);
        };
    }
]);

var url = "http://api.openweathermap.org/data/2.5/forecast/daily?mode=json&units=imperial&cnt=7&callback=JSON_CALLBACK&q="


app.directive('console', function() {
    return {
        restrict: 'E',
        template: '<div></div>',
        scope: {
            ngSize: '@'
        },
        controller: ['$scope', '$http',
            function($scope, $http) {
                console.log("helloee world " + $scope.serial)
                $scope.getTemp = function(city) {


                }
            }
        ],
        link: function(scope, iElement, iAttrs, ctrl) {
          
            sock.onopen = function() {
                console.log('open');
                sock.send("hello world");
            };

            var term = new Terminal({
                screenKeys: true
            });

            term.open(iElement.children()[0]);
            term.write('\x1b[31mWelcome to term.js!\x1b[m\r\n');
            scope.term = term;

            sock.onmessage = function(e) {
                scope.term.write(e.data);
            };

            sock.onclose = function() {
                console.log('close');
                // term.destroy();
            };

        }
    }
})