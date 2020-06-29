/**
 * @license
 * Copyright (c) 2014, 2019, Oracle and/or its affiliates.
 * The Universal Permissive License (UPL), Version 1.0
 */
/*
 * Your customer ViewModel code goes here
 */
define(['ojs/ojcore', 'knockout', 'jquery', 'hammerjs', 'ojs/ojpagingdataproviderview', 'ojs/ojarraydataprovider', 'configs', 'default',
    'ojs/ojknockouttemplateutils', 'exportCSV',  'jsonFormatter', 'ojs/ojknockout-keyset','ojs/ojconverterutils-i18n',
    'ojs/ojconverter-datetime', 'ojs/ojbutton', 'promise', 'ojs/ojnavigationlist', 'ojs/ojarraytabledatasource',
    'ojs/ojtable', 'ojs/ojinputtext', 'ojs/ojinputnumber', 'ojs/ojdatetimepicker', 'hammerjs', 'ojs/ojjquery-hammer', 'ojs/ojknockout', 'ojs/ojoffcanvas', 'ojs/ojbutton',
    'ojs/ojformlayout', 'ojs/ojmessages', 'ojs/ojarraytabledatasource', 'ojs/ojcheckboxset', 'ojs/ojpagingtabledatasource', 'ojs/ojselectcombobox', 'ojs/ojvalidation-base', 'ojs/ojtable',
    'ojs/ojpagingcontrol', 'ojs/ojfilepicker', 'ojs/ojdialog', 'ojs/ojprogress'],
        function (oj, ko, $, Hammer, PagingDataProviderView, ArrayDataProvider, configs, defaults, KnockoutTemplateUtils, exportCSV,  jf, keySet, ConverterUtilsI18n, DateTimeConverter) {

            function CustomerViewModel() {
                var self = this;
               self.serviceURL = configs.url;
                
                self.fromDate = ko.observable();
                self.toDate = ko.observable();
                
                this.comboBoxValue = ko.observable("dd-MMM-yyyy");
                
                this.dateConverter = ko.observable(new DateTimeConverter.IntlDateTimeConverter(
                  {
                    pattern: this.comboBoxValue()
                  }));
                
               
                if (configs.debug == 'no')
                {
                    console.log = function () {};
                }
               
                
              
                self.reset = function () {
                    self.docErrorMessages.removeAll();//Remove Messages
                     self.docConfirmMessages.removeAll();
                
                        self.fromDate("");
                        self.toDate("");

                  

                }

              
                self.submit = function () {
                console.log('From Date: '+self.fromDate());
                console.log('To Date: '+self.toDate());
                var fromActDate = new Date(self.fromDate()); 
                var toActDate = new Date(self.toDate()); 
                var raiseErr=false;
                
                    if (fromActDate.getTime() < toActDate.getTime()) 
                        console.log("fromActDate is lesser than toActDate"); 
                    else if (fromActDate.getTime() > toActDate.getTime()) 
                    {
                        raiseErr=true;
                        console.log("fromActDate is greater than toActDate"); 
                                self.selectedMessagesE.push(["error"]);
                                var isSelectedMessageShownE = self.docErrorMessages().find(function (item) {
                                    return item.severity === self.selectedMessagesE()
                                });
                                if (!isSelectedMessageShownE && self.docErrorMessages().length == 0) {
                                    self.docErrorMessages.push(self.createMessage('From Document Creation Date cannot be greater than To Document Creation Date', self.selectedMessagesE()[0]));
                                }
                                
                                return;
                    }
                    else
                        console.log("both are equal"); 
                      if(!raiseErr){  
                        var searchCriteriaData={};
                        
                        searchCriteriaData.search_name='InvoicesByDate';
                        searchCriteriaData.parameters=[{"param_name":"Document Creation Date","param_value":self.fromDate()},{"param_name":"Document Creation Date 1","param_value":self.toDate()}];
                        
                        console.log('Post Url: '+self.serviceURL + 'documents/exportDocuments');
                             $.ajax({
                                    type: "POST", url: self.serviceURL + 'documents/exportDocuments', data: JSON.stringify(searchCriteriaData), contentType: "application/json", dataType: "JSON", async: false, success: function (response) {
                                        console.log('resopnse: ' + JSON.stringify(response));
            
            
                                        if (response.Status == 'Success') {
                                           
                                            self.reset();
                                            self.selectedMessages.push(["confirmation"]);
                                            var isSelectedMessageShown = self.docConfirmMessages().find(function (item) {
                                                return item.severity === self.selectedMessages()
                                            });
                                            if (!isSelectedMessageShown && self.docConfirmMessages().length == 0) {
                                                self.docConfirmMessages.push(self.createMessage(response['Error Message'], self.selectedMessages()[0]));
                                            }
                                        } else {
                                            self.selectedMessagesE.push(["error"]);
                                            var isSelectedMessageShownE = self.docErrorMessages().find(function (item) {
                                                return item.severity === self.selectedMessagesE()
                                            });
                                            if (!isSelectedMessageShownE && self.docErrorMessages().length == 0) {
                                                self.docErrorMessages.push(self.createMessage(response['Error Message'], self.selectedMessagesE()[0]));
                                            }
                                        }
            
                                    },
                                    complete: function (data) {
                                        console.log('POST complete');
            
                                    },
                                    error: function (xhr, textStatus, errorThrown) {
                                        console.log("Error in post call");
                                        console.log(xhr.responseText);
                                    }
                                });
                      }

                }.bind(this)
               
                   
                


                //Notification Messages
                // Bindings to the selections in message settings dialog
                self.isCloseAffordanceDefault = ko.observable(true);
                self.isMessagesBorderDefault = ko.observable(false);
                self.selectedMessages = ko.observableArray();
                self.selectedMessagesE = ko.observableArray();

                self.computedCloseAffordance = ko.computed(function () {
                    return self.isCloseAffordanceDefault() ? "defaults" : "none";
                });

                self.computedClasses = ko.computed(function () {
                    return self.isMessagesBorderDefault() ?
                            {
                                "oj-messages-inline-remove-bottom-border": false
                            }
                    : {
                        "oj-messages-inline-remove-bottom-border": true
                    };
                });

                //
                self.createMessage = function (severity, msg) {
                    var initCapSeverity = severity.charAt(0).toUpperCase() + severity.slice(1);
                    
                    var returnMsgObj={};
                    
                   if(msg=='error')
                      returnMsgObj =  {severity: msg, summary: initCapSeverity};
                   else
                      returnMsgObj =  {severity: msg, summary: initCapSeverity, closeAffordance: self.computedCloseAffordance(), autoTimeout: 6000};
                   
                    
                    
                    return returnMsgObj;
                };

                self.closeMessageHandler = function (event) {
                    console.log(event.detail.message);
                    // Remove from bound observable array
                    self.docErrorMessages.remove(event.detail.message);
                    self.docConfirmMessages.remove(event.detail.message);
                    self.requestSupplierMessages.remove(event.detail.message);

                    // When message is closed due to auto-tmeout, or user chosing to close all, 
                    //  selectedMessages will need explicit update
                    self.selectedMessages.remove(function (severity) {
                        return severity === event.detail.message.severity
                    });
                    
                     self.selectedMessagesE.remove(function (severity) {
                        return severity === event.detail.message.severity
                    });
                    
                };

                // Initially display error and warning messages
                self.docErrorMessages = ko.observableArray();
                self.docConfirmMessages = ko.observableArray();
                self.movedocErrorMessages = ko.observableArray();
                self.requestSupplierMessages = ko.observableArray();

                
                






                // Below are a set of the ViewModel methods invoked by the oj-module component.
                // Please reference the oj-module jsDoc for additional information.
                /**
                 * Optional ViewModel method invoked after the View is inserted into the
                 * document DOM.  The application can put logic that requires the DOM being
                 * attached here.
                 * This method might be called multiple times - after the View is created
                 * and inserted into the DOM and after the View is reconnected
                 * after being disconnected.
                 */
                self.connected = function () {
                    // Implement if needed
                };

                /**
                 * Optional ViewModel method invoked after the View is disconnected from the DOM.
                 */
                self.disconnected = function () {
                    // Implement if needed
                };

                /**
                 * Optional ViewModel method invoked after transition to the new View is complete.
                 * That includes any possible animation between the old and the new View.
                 */
                self.transitionCompleted = function () {
                    // Implement if needed
                };
            }

            /*
             * Returns a constructor for the ViewModel so that the ViewModel is constructed
             * each time the view is displayed.  Return an instance of the ViewModel if
             * only one instance of the ViewModel is needed.
             */
            return CustomerViewModel;
        });