var searchConceptApp = angular.module('searchConceptApp', [ 'ngResource' ],function($locationProvider) {
		$locationProvider.hashPrefix('');
});

searchConceptApp.factory('messageService', function($rootScope, $http) {
	var messageService = {};

	messageService.message = '';

	messageService.prepForBroadcast = function(msg) {
		this.message = msg;
		this.broadcastItem();
	};
	messageService.prepForBroadcastToAutoComplete = function(msg){
		this.message = msg;
		this.broadcastItemToAutoComplete();
		}
	messageService.broadcastItem = function() {
		$rootScope.$broadcast('handleBroadcast');
	};
	messageService.broadcastItemToAutoComplete = function() {
		$rootScope.$broadcast('handleBroadcastToAutoComplete');
	};

	return messageService;
});

searchConceptApp.factory('SearchTermServ', function($http, $resource) {
	return {
		AutoComplete : function(request, response) {

			var retArray;
			var ajaxUrl = emr.fragmentActionLink("conceptmanagementapps",
					"chooseConceptByHierarchy", "search");

			$.getJSON(ajaxUrl, {
				id : request.id
			}).success(function(data) {

				response(data);

			}).error(function(xhr, status, err) {
				alert('Concept Search AJAX ' + err);
				console.log('Concept Search AJAX ' + err);
			});
		}
	}
});
searchConceptApp.factory('hierarchyConceptTermServ', function($http, $resource) {
	return {
		BuildHierarchyView : function(request, response) {

			var retArray;
			var ajaxUrl = emr.fragmentActionLink("conceptmanagementapps",
					"chooseConceptByHierarchy", "getAncestors");

			$.getJSON(ajaxUrl, {
				termId : request.termId || 'empty', 
				conceptId : request.conceptId || 'empty', 
				updateBy :  request.updateBy || 'empty'
			}).success(function(data) {

				response(data);

			}).error(function(xhr, status, err) {
				alert('Concept Search AJAX ' + err);
			});
		}
	}
});


function MainCtrl($scope, SearchTermServ,messageService) {
	$scope.selectedConcept = {
			conceptId : 0,
			conceptName : ''
	};
	
	$scope.Wrapper = SearchTermServ;

}
function HierarchyCtrl($scope, messageService,hierarchyConceptTermServ) {

	$scope.hierarchyServiceWrapper = hierarchyConceptTermServ;
	$scope.searchTerm='';
	$scope.selectedRefTerm = {
			refTermtId : 0,
			refTermName : ''
	};
}



searchConceptApp.directive('ngHierarchy',function(messageService,hierarchyConceptTermServ) {
	return {
		restrict : 'E',
		replace : true,
		scope : {
			remoteData : '&',
			selectedRefTerm : '=selectedRefTerm'
		},
		template : '<div class="left" ng-show="currentTerm">'
			+ '     <ul id="nestedlist">'

			+ '			<li>Parent(s):<ul>'
			+ '         	<li ng-repeat="term in _parentTermsAndConcepts">'
			+ '         		<div class="chooseByHierarchy">'
			+ '						&nbsp;Name Of Reference Term:&nbsp;<a href="javascript:void(0);" ng-click="selectRefTerm(term);">view&nbsp;&nbsp;</a>'
			+ '						{{term.termName}}&nbsp;&nbsp;'
			+ '						<a href="javascript:void(0);" style="float:right;" ng-click="selectRefTerm(term);">'
			+ '							&nbsp;&nbsp;{{term.termCode}}</a>'
			+ '						</br><label ng-show="term.mappedConcepts">Choose Concept:&nbsp;</label>'
			+ '						<strong><button class="chooseByHierarchyConcept" ng-repeat="parentConcept in term.mappedConcepts" ng-click="selectRefTerm(term);">'
			+ '							{{parentConcept.conceptName}}</button></strong></div></li ng-if="$index!=_parentTermsAndConcepts.length"></ul></br>'

			+ '     						<ul><li>Current Term:<ul><li><div class="currentTerm">'
			+ '									<label  ng-show="currentTerm.termName">&nbsp;Reference Term:&nbsp;</label>'
			+ '									<span>{{currentTerm.termName}}&nbsp;&nbsp;{{currentTerm.termCode}}</span>'
			+ '										<label ng-show="currentTerm.conceptName" >Concept:&nbsp;</label><span>{{currentTerm.conceptName}}</span>'
			+ '								</div></li></ul>'

			+ '									<ul><li>Child(ren):<ul> '
			+ '         							<li ng-repeat="childTerm in _childTermsAndConcepts">'
			+ '											<div class="chooseByHierarchy">&nbsp;Name Of Reference Term:'
			+ '												<a href="javascript:void(0);" ng-click="selectRefTerm(childTerm);">view</a>&nbsp;&nbsp;'
			+ '          									{{childTerm.termName}}&nbsp;&nbsp;<a href="javascript:void(0);" style="float:right;" ng-click="selectRefTerm(childTerm);">'
			+ '												&nbsp;&nbsp;{{childTerm.termCode}}</a>'
			+ '												</br><label ng-show="childTerm.mappedConcepts">Choose Concept:&nbsp;</label>'
			+ '          									<strong> <button class="chooseByHierarchyConcept" ng-click="selectConcept(childTerm, childConcept);" ng-repeat="childConcept in childTerm.mappedConcepts">'
			+ '												{{childConcept.conceptName}}</button></strong>'
			+ '											</div></li></ul></li></ul>'

			+ '     							</li></ul>'   
			+ ' 						</li></ul> '  
			+ ' 				</li>' 
			+ '			</ul>'
			+ '	</div>',
			
			controller : function($scope, $element, $attrs,messageService,hierarchyConceptTermServ) {				

				$scope.$on('handleBroadcast', function() {
					var updateType = 'conceptUpdate';
					var conceptId = '';
					$scope.UpdateHierarchy(messageService.message, updateType, conceptId);
				});

				$scope.selectRefTerm = function(choice) {

					var searchTermName=choice.termName;
					$scope.selectedRefTerm=choice.termName;
					var updateType = 'refTermUpdate';
					var conceptId = '';
					$scope.UpdateHierarchy(choice, updateType, conceptId);


				};
				
				$scope.selectConcept = function(choice, concept) {
					
					var conceptName=concept.conceptName;
					messageService.prepForBroadcastToAutoComplete(conceptName);
					
					var searchTermName=choice.termName;
					$scope.selectedRefTerm=choice.termName;
					$scope.selectedConcept=concept.conceptName;
					var updateType = 'refTermUpdate';
					$scope.UpdateHierarchy(choice, updateType, concept.conceptId);


				};


				$scope.UpdateHierarchy = function(currentTerm, updateBy, conceptId) {

					try {
						$scope.remoteData({
							request : {
								termId : currentTerm.termId,
								conceptId : currentTerm.conceptId || conceptId,
								updateBy :  updateBy
							},
							response : function(data) {

								var childArray = [];
								var parentArray = [];
								var mappedParentTermsArray = [];
								var mappedParentConceptsArray = [];
								var mappedChildConceptsArray = [];
								var mappedChildTermsArray = [];	
								var childTermsAndConceptsArray = [];
								var parentTermsAndConceptsArray = [];

								var parents=angular.fromJson(angular.fromJson(data[0]).parents);
								var children=angular.fromJson(angular.fromJson(data[0]).children);

								for ( var i = 0; i < parents.length; i++) {

									parentArray.push(angular.fromJson(angular.fromJson(data[0])).parents[i]);
								}

								for ( var i = 0; i < children.length; i++) {

									childArray.push(angular.fromJson(angular.fromJson(data[0])).children[i]);
								}

								for ( var i = 0; i < angular.fromJson(parentArray).length; i++) {
									mappedParentTermsArray=angular.fromJson(parentArray[i]).mappedRefTerm;

									var parentConcept = angular.fromJson(parentArray[i]).mappedConcept;

									var mappedConceptsArray = [];
									for(var j = 0; j < parentConcept.length;j++){
										var theConceptId = ''
											var  theConceptName='';

										if(parentConcept[j] !== undefined && parentConcept[j].conceptId !== undefined){
											theConceptId = parentConcept[j].conceptId;
										}
										if(parentConcept[j] !== undefined && parentConcept[j].conceptName !== undefined){
											theConceptName = parentConcept[j].conceptName;
										}
										mappedConceptsArray[j]={conceptId:theConceptId,conceptName:theConceptName};
									}
									parentTermsAndConceptsArray[i]={termCode:mappedParentTermsArray.termCode, termId:mappedParentTermsArray.termId,termName:mappedParentTermsArray.termName,mappedConcepts:mappedConceptsArray};

								}


								for ( var i = 0; i < angular.fromJson(childArray).length; i++) {
									mappedChildTermsArray=angular.fromJson(childArray[i]).mappedRefTerm;

									var childConcept = angular.fromJson(childArray[i]).mappedConcept;

									console.log('childConceptLength  '+childConcept.length);
									var mappedConceptsArray = [];
									for(var j = 0; j < childConcept.length;j++){
										var theConceptId = ''
											var  theConceptName='';

										if(childConcept[j] !== undefined && childConcept[j].conceptId !== undefined){
											theConceptId = childConcept[j].conceptId;
										}
										if(childConcept[j] !== undefined && childConcept[j].conceptName !== undefined){
											theConceptName = childConcept[j].conceptName;
										}

										mappedConceptsArray[j]={conceptId:theConceptId,conceptName:theConceptName};
										console.log(mappedConceptsArray[j]);
									}

									childTermsAndConceptsArray[i]={termCode:mappedChildTermsArray.termCode, termId:mappedChildTermsArray.termId,termName:mappedChildTermsArray.termName,mappedConcepts:mappedConceptsArray};

								}

								$scope.$apply(function () {

									$scope._childTermsAndConcepts = childTermsAndConceptsArray;
									$scope._parentTermsAndConcepts = parentTermsAndConceptsArray;
									$scope.currentTerm = angular.fromJson(angular.fromJson(data[0]).term);

								});
							}
						});
					} catch (ex) {
						console.log(ex.message);
					}
				}

			},
			link : function(scope, iElement, iAttrs, controller) {
				scope._children = [];
				scope._parents = [];
				scope.currentTerm = [];						
			}
	};
});

searchConceptApp.directive('ngAutocomplete',function(messageService) {
	return {
		restrict : 'E',
		replace : true,
		scope : {
			minInputLength : '@minInput',
			remoteData : '&',
			placeholder : '@placeholder',
			restrictCombo : '@restrict',
			selectedConcept : '=selectedConcept'
		},
		template : '<div class="left"><div style="background-color:white;position:top; padding: 0;position: relative;top: 1px;" class="dropdown search" '
			+ +'     ng-class="{open: focused && _choices.length>0}">'
			+ '	   <label>Select Concept</label>'
			+ '    <input type="text" id="selectConcept" ng-model="searchTerm" placeholder="{{placeholder}}" '
			+ '         tabindex="1" accesskey="s" focused="focused" size="50" /> '
			+ '			<div style="background-color:white;position: relative;">'
			+ '     		<ul class="select"> '
			+ '         		<li ng-repeat="choice in _choices">'
			+ '          			<a href="javascript:void(0);" ng-click="selectConcept(choice);">{{choice.conceptName}}</a></li>'
			+ '     		</ul></div>'
			+ '     	<br/><br/>'
			+ '	</div></div>',
			controller : function($scope, $element, $attrs,messageService) {
				
				$scope.$on('handleBroadcastToAutoComplete', function() {

					$scope.searchTerm = messageService.message;		
				});
				
				$scope.selectConcept = function(choice) {
					$scope.selectedConcept = choice;
					$scope.lastSearchTerm = choice.conceptName;
					$scope.searchTerm=choice.conceptName;
					messageService.prepForBroadcast(choice);
				};

				$scope.UpdateSearch = function() {

					if ($scope.searchTerm.length < 3) {
						return;
					}

					if ($scope.canRefresh()) {
						$scope.searching = true;
						$scope.lastSearchTerm = $scope.searchTerm;
						try {
							$scope.remoteData({
								request : {
									id : $scope.searchTerm
								},
								response : function(data) {
									var dataArray = [];
									var iterateSize = data.length;
									if (iterateSize > 25) {
										iterateSize = 25;
									}
									for ( var i = 0; i < iterateSize; i++) {

										dataArray.push(angular.fromJson(data[i]));
									}
									$scope.$apply(function () {
										$scope._choices = dataArray;
										$scope.searching = false;
									});
								}
							});
						} catch (ex) {
							console.log(ex.message);
							$scope.searching = false;
						}
					}
				}
				$scope.$watch('searchTerm', $scope.UpdateSearch);
				$scope.canRefresh = function() {
					return ($scope.searchTerm !== "")
					&& ($scope.searchTerm !== $scope.lastSearchTerm)
					&& ($scope.searching != true);
				};
			},
			link : function(scope, iElement, iAttrs, controller) {

				scope._searchTerm = '';
				scope.searchTerm = '';
				scope._lastSearchTerm = '';
				scope.searching = false;
				scope._choices = [];
				if (iAttrs.restrict == 'true') {
					var searchInput = angular.element(iElement.children()[0])
					searchInput.bind('blur', function() {
						if (scope._choices
								.indexOf(scope.selectedConcept) < 0) {
							scope.selectedConcept = null;
							scope.searchTerm = '';
						}
					});
				}						
			}
	};
});

searchConceptApp.directive('uiIf', [function () {
	return {
		transclude: 'element',
		priority: 1000,
		terminal: true,
		restrict: 'A',
		compile: function (element, attr, transclude) {
			return function (scope, element, attr) {

				var childElement;
				var childScope;

				scope.$watch(attr['uiIf'], function (newValue) {
					if (childElement) {
						childElement.remove();
						childElement = undefined;
					}
					if (childScope) {
						childScope.$destroy();
						childScope = undefined;
					}

					if (newValue) {
						childScope = scope.$new();
						transclude(childScope, function (clone) {
							childElement = clone;
							element.after(clone);
						});
					}
				});
			};
		}
	};
}]);

searchConceptApp.directive("focused", function($timeout) {
	return function(scope, element, attrs) {
		element[0].focus();
		element.bind('focus', function() {
			scope.$apply(attrs.focused + '=true');
		});
		element.bind('blur', function() {
			$timeout(function() {
				scope.$eval(attrs.focused + '=false');
			}, 200);
		});
		scope.$eval(attrs.focused + '=true')
	}
});
HierarchyCtrl.$inject = ['$scope', 'messageService' ,'hierarchyConceptTermServ'];