/**
 * web-events - An event-bus extension for events transmission over websockets
 * Copyright (C) 2016  Adrian Cristian Ionescu - https://github.com/acionescu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.segoia.eventbus.web.websocket.server;

import java.util.List;

import net.segoia.event.conditions.ConditionParam;

public class AuthRequiredData {
    private List<ConditionParam> params;
    private String paramsNamespace;

    public AuthRequiredData(List<ConditionParam> params, String paramsNamespace) {
	super();
	this.params = params;
	this.paramsNamespace = paramsNamespace;
    }

    public AuthRequiredData(List<ConditionParam> params) {
	super();
	this.params = params;
    }

    public AuthRequiredData() {
	super();
	// TODO Auto-generated constructor stub
    }

    public List<ConditionParam> getParams() {
	return params;
    }

    public void setParams(List<ConditionParam> params) {
	this.params = params;
    }

    public String getParamsNamespace() {
	return paramsNamespace;
    }

    public void setParamsNamespace(String paramsNamespace) {
	this.paramsNamespace = paramsNamespace;
    }

}
