var app = angular.module("myApp", []);
app.controller('submitController', function ($scope, $http) {
    console.log("imie: " + $scope.firstName);
    $scope.submitForm = function () {
        $scope.client = {
            firstName: $scope.firstName,
            age: $scope.age,
            profession: $scope.profession
        };
        $http.post('/clients/addRequest', $scope.client);
    }
});