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
        controller: ['$scope', '$http',
            function($scope, $http) {
                $scope.getTemp = function(city) {}
            }
        ],
        link: function(scope, iElement, iAttrs, ctrl) {
            var term = $(iElement).terminal(function(command, term) {
                if (command !== '') {
                    sock.send(command)
                }
            }, {
                greetings: 'device',
                name: 'js_demo',
                height: 200,
                width: 450,
                prompt: '> '
            });

            sock.onopen = function() {
                term.echo(String("connection established"));
            };

            sock.onmessage = function(e) {
                console.log(e.data)
                term.echo(String("received:" + e.data));
            };

            sock.onclose = function() {
                term.echo(String("connection closed"));
            };

        }
    }
})
