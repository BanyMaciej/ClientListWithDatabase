var app = angular.module("myApp", []);
app.controller('submitCtrl', function func($scope, $http) {
    $scope.submitNewClientForm = function () {
        $scope.client = {
            firstName: $scope.firstName,
            age: $scope.age,
            profession: $scope.profession
        };
        $http.post('/clients/addRequest', $scope.client);
        //TODO handling result
    };
});

app.controller('clientsCtrl', function ($scope, $http) {
    $scope.clients = [];

    $scope.getAll = function () {
        $http.get('/clients/getAllRequest').then(
            function (response) {
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
        $http.post('/clients/addPhoneNumber', data).then(
            function (response) {
                var lClient = $scope.clients.find(function (c) {
                    return angular.equals(c, client);
                });
                lClient.phoneNumbers.push(number);
            }, function (response) {
                console.log("error-post");
            }
        );
    };
});
