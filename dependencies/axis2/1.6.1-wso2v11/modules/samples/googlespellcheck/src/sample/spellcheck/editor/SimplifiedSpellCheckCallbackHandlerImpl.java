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

import sample.spellcheck.stub.SimplifiedSpellCheckCallbackHandler;
import sample.spellcheck.stub.SimplifiedSpellCheckStub.DoSpellingSuggestionsResponse;

public class SimplifiedSpellCheckCallbackHandlerImpl extends SimplifiedSpellCheckCallbackHandler {

    private Observer observer = null;
    private String phrase = null;
    
    public SimplifiedSpellCheckCallbackHandlerImpl(Observer observer, String phrase) {
        this.observer = observer;
        this.phrase = phrase;
    }
    
    public void receiveResultdoSpellingSuggestions(DoSpellingSuggestionsResponse param1) {
        String suggestion = param1.get_return();
        
        if (suggestion == null) {
            observer.update(phrase);
            observer.updateError(
                    "No suggestions found for " + phrase);
        } else {
            observer.update(suggestion);
        }
    }

    public void receiveErrordoSpellingSuggestions(Exception e) {
        e.printStackTrace();
        observer.updateError(e.getMessage());
    }   
    
    
    
    
}

