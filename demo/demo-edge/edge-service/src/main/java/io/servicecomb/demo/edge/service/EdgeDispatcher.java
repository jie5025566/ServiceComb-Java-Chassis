/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.demo.edge.service;

import java.util.Map;

import io.servicecomb.edge.core.AbstractEdgeDispatcher;
import io.servicecomb.edge.core.CompatiblePathVersionMapper;
import io.servicecomb.edge.core.EdgeInvocation;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CookieHandler;

public class EdgeDispatcher extends AbstractEdgeDispatcher {
  private CompatiblePathVersionMapper versionMapper = new CompatiblePathVersionMapper();

  @Override
  public int getOrder() {
    return 10000;
  }

  @Override
  public void init(Router router) {
    String regex = "/api/([^\\\\/]+)/([^\\\\/]+)/(.*)";
    router.routeWithRegex(regex).handler(CookieHandler.create());
    router.routeWithRegex(regex).handler(createBodyHandler());
    router.routeWithRegex(regex).failureHandler(this::onFailure).handler(this::onRequest);
  }

  protected void onRequest(RoutingContext context) {
    Map<String, String> pathParams = context.pathParams();
    String microserviceName = pathParams.get("param0");
    String pathVersion = pathParams.get("param1");
    String path = context.request().path().substring(4);

    EdgeInvocation edgeInvocation = new EdgeInvocation();
    edgeInvocation.setVersionRule(versionMapper.getOrCreate(pathVersion).getVersionRule());

    edgeInvocation.init(microserviceName, context, path, httpServerFilters);
    edgeInvocation.invoke();
  }
}
