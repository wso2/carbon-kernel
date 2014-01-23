/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package sample.spellcheck.editor;

import sample.spellcheck.stub.SimplifiedSpellCheckStub;
import sample.spellcheck.stub.SimplifiedSpellCheckStub.DoSpellingSuggestions;
import sample.spellcheck.stub.SimplifiedSpellCheckStub.DoSpellingSuggestionsResponse;

/**
 * class sample.google.spellcheck.FormModel
 * This is the Impementation of the Asynchronous Client
 */
public class FormModel {
    Observer observer;
   
    public FormModel(Observer observer) {
        this.observer = observer;
    }

    public void doAsyncSpellingSuggestion(String word) {
               
        try {
            
            SimplifiedSpellCheckStub stub = new SimplifiedSpellCheckStub();
            
            DoSpellingSuggestions doSpellingSuggestions = new DoSpellingSuggestions();
            doSpellingSuggestions.setPhrase(word);
            
            SimplifiedSpellCheckCallbackHandlerImpl callbackHandlerImpl = new SimplifiedSpellCheckCallbackHandlerImpl(observer, word);
            
            stub.startdoSpellingSuggestions(doSpellingSuggestions, callbackHandlerImpl);
        
        } catch (Exception ex) {
            observer.updateError(ex.getMessage());
        }
    }

    public void doSyncSpellingSuggestion(String word) {
        
        try {
            SimplifiedSpellCheckStub stub = new SimplifiedSpellCheckStub();
            
            DoSpellingSuggestions suggestions = new DoSpellingSuggestions();
            suggestions.setPhrase(word);
            
            DoSpellingSuggestionsResponse response = stub.doSpellingSuggestions(suggestions);
            String suggestion = response.get_return();
            
            if (suggestion == null) {
                observer.update("No suggestions found for " + word);
                
            } else  {
                observer.update(suggestion);
            }
            
        } catch (Exception ex) {
            observer.updateError(ex.getMessage());
        }
    }
    
}
