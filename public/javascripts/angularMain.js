var app = angular.module("myApp", []);
app.controller('submitCtrl', function func($scope, $http, $window) {
    $scope.isNewClientValid = true;
    $scope.isFirstNameValid = true;
    $scope.isAgeValid = true;

    $scope.errNameMsg = "";
    $scope.errAgeMsg = "";

    $scope.submitNewClientForm = function () {
        var data = {
            firstName: $scope.firstName.trim(),
            age: $scope.age,
            profession: $scope.profession
        };
        $http.post('/clients/addNewClientRequest', data).then(
            $window.location.href = "/clients/clientList",
            console.log("error")
        );

    };
});

app.controller('clientsCtrl', function ($scope, $http) {
    $scope.clients = [];
    $scope.searchField = String();
    $scope.isNewNumberValid = true;
    $scope.isNewNumberNotExistValid = true;

    $scope.errNumExistMsg = "";
    $scope.errNumMsg = "";

    $scope.getAll = function () {
        $http.get('/clients/getAllRequest').then(
            function (response) {
                $scope.clients = response.data.clients;
            }, function (response) {
                console.log("error");
            }
        );
    };

    $scope.getPhoneNumbersStyle = function (client) {
        switch( client.phoneNumbers.length ) {
            case 0: return 'none';
            case 1: return 'one';
            case 2: return 'two';
            default: return 'more';
        }
    };

    $scope.submitNewNumberForm = function (client, number) {
        $scope.number = "";
        var data = {
            id: client.id,
            number: parseInt(number)
        };
        $http.post('/clients/addPhoneNumber', data).then(
            function (response) {
                var lClient = $scope.clients.find(function (c) {
                    return angular.equals(c, client);
                });
                lClient.phoneNumbers.push(number);
            }, function (response) {
                console.log("error-post");
                throw new SQLException;
            }
        );
    };

    $scope.submitSearch = function () {
        var data = {
            searchFormula: $scope.searchField
        }
        $http.post('/clients/searchRequest', data).then(
            function (response) {
                $scope.clients = response.data.clients;
            }, function (response) {
                console.log("error");
            }
        )
    }
});

//Client-side validation
app.directive( 'firstNameValid', function () {
    var errors = [
        "Pole 'Imię' nie może być puste!\n",
        "Pole musi zaczynać się z wielkiej litery!\n",
        "Pole musi zawierać tylko litery\n",
        "Pole musi zawierać jedno słowo\n"
    ];
    return {
        restrict: 'A',
        link: function (scope, element, attrs) {
            scope.$watch(attrs.ngModel, function (v) {
                scope.errNameMsg = "";
                var correct = true;
                scope.isFirstNameValid = true;
                if( v === undefined ) {
                    scope.errNameMsg += errors[0];
                    scope.isFirstNameValid = false;
                    return;
                }
                if( !/^[A-ZĄĆĘŻŹŁÓŃ]/.test(v) ) {
                    scope.errNameMsg += errors[1];
                    correct = false;
                }
                if( !/^[a-ząćężźłóń]+$/i.test(v) ) {
                    scope.errNameMsg += errors[2];
                    correct = false;
                }
                if( /^\s+$/.test(v) ) {
                    scope.errNameMsg += errors[3];
                    correct = false;
                }
                scope.isFirstNameValid = correct;
            });
        }
    }
});

app.directive('ageValid', function () {
    var errors = [
        "Pole 'Wiek' nie może być puste!\n",
        "Wiek musi należeć do przedziału 25-80!\n"
    ];
    return {
        restrict: 'A',
        link: function (scope, element, attrs) {
            scope.$watch(attrs.ngModel, function (v) {
                scope.errAgeMsg = "";
                var correct = true;
                scope.isAgeValid = true;
                if( v === undefined ) {
                    scope.errAgeMsg += errors[0];
                    correct = false;
                }
                if( v < 25 || v > 80 ) {
                    scope.errAgeMsg += errors[1];
                    correct = false;
                }
                scope.isAgeValid = correct;
            });
        }
    }
});

app.directive('phoneNumberValid', function () {
    var errors = [
        "Numer nie może zaczynać się od 0!\n",
        "Numer musi posiadać 9 cyfr!\n"
    ];
   return {
       restrict: 'A',
       link: function (scope, element, attrs) {
           scope.$watch(attrs.ngModel, function (v) {
               scope.errNumMsg = "";
               var correct =true;
               scope.isNewNumberValid = true;
               if( /^0/.test(v) ) {
                   scope.errNumMsg += errors[0];
                   correct = false;
               }
               if( !/^\d{9}$/.test(v) ) {
                   scope.errNumMsg += errors[1];
                   correct = false;
               }
               scope.isNewNumberValid = correct;
           });
       }
   }
});

app.directive('phoneNumberExistValid', function ($parse) {
    var errors = [
        "Numer jest już przypisany do klienta!\n",
        "Numer jest przypisany do innego klienta!\n"
    ];
    return {
        restrict: 'A',
        link: function (scope, element, attrs) {
            var localClients = scope.clients;
            var index = $parse(attrs.phoneNumberExistValid)(scope);

            var allNumbers = [];
            for( var k = 0; k < localClients.length; k++ ) {
                allNumbers = allNumbers.concat(localClients[k].phoneNumbers);
            }
            scope.$watch(scope.clients, function (v) {
                for( var k = 0; k < scope.clients.length; k++ ) {
                    allNumbers = allNumbers.concat(scope.clients[k].phoneNumbers);
                }
            });
            scope.$watch(attrs.ngModel, function (v) {
                var correct = true;
                scope.isNewNumberNotExistValid = true;
                scope.errNumExistMsg = "";
                if( localClients[index].phoneNumbers.indexOf(parseInt(v)) !== -1 ) {
                    scope.errNumExistMsg += errors[0];
                    correct = false;
                } else if( allNumbers.indexOf(parseInt(v)) !== -1 ) {
                    scope.errNumExistMsg += errors[1];
                    correct = false;
                }
                scope.isNewNumberNotExistValid = correct;
            });
        }
    }
});

