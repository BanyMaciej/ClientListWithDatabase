var app = angular.module("myApp", []);
app.controller('submitCtrl', function func($scope, $http) {
    $scope.submitNewClientForm = function () {
        $scope.client = {
            firstName: $scope.firstName,
            age: $scope.age,
            profession: $scope.profession
        };
        $http.post('/clients/addRequest', $scope.client);
    };



    var init = function () {
        console.log("Hello!");
    };
    init();
});

app.controller('clientsCtrl', function ($scope, $http) {
    $scope.getAll = function () {
        $http.get('/clients/getAllRequest').then(
            function (response) {
                angular.element(console.log("Hello!"));
                console.log(response);
                $scope.clients = response.data.clients;
            }, function (response) {
                console.log("error");
            }
        );
    };

    $scope.submitNewNumberForm = function (client, number) {
        var data = {
            id: client.id,
            number: number
        };
        console.log(data);
        $http.post('/clients/addNumber', data).then(
            function (response) {
                console.log(response);
            }, function (response) {
                console.log("error-post");
            }
        );
    };

    $scope.changeState = function (b) {

        return !b;
    }
})
