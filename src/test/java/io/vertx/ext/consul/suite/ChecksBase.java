/*
 * Copyright (c) 2016 The original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *      The Eclipse Public License is available at
 *      http://www.eclipse.org/legal/epl-v10.html
 *
 *      The Apache License v2.0 is available at
 *      http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.ext.consul.suite;

import io.vertx.core.Handler;
import io.vertx.ext.consul.Check;
import io.vertx.ext.consul.CheckOptions;
import io.vertx.ext.consul.CheckStatus;
import io.vertx.ext.consul.ConsulTestBase;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import io.vertx.ext.consul.vertx.VertxHttpServer;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

import static io.vertx.ext.consul.Utils.*;
import static io.vertx.test.core.TestUtils.randomAlphaString;

/**
 * @author <a href="mailto:ruslan.sennov@gmail.com">Ruslan Sennov</a>
 */
@RunWith(VertxUnitRunner.class)
public abstract class ChecksBase extends ConsulTestBase {
  final static String TEST_HEADER_NAME = "test";
  final static String TEST_HEADER_VALUE = "foo";

  abstract String createCheck(CheckOptions opts);

  abstract void createCheck(TestContext tc, CheckOptions opts, Handler<String> idHandler);

  @Test
  public void ttlCheckLifecycle(TestContext tc) {
    CheckOptions opts = new CheckOptions()
      .setTtl("2s")
      .setName(randomAlphaString(10));
    String checkId = createCheck(opts);

    Check check;

    runAsync(h -> writeClient.warnCheckWithNote(checkId, "warn", h));
    check = getCheckInfo(checkId);
    assertEquals(CheckStatus.WARNING, check.getStatus());
    assertEquals("warn", check.getOutput());

    runAsync(h -> writeClient.failCheckWithNote(checkId, "fail", h));
    check = getCheckInfo(checkId);
    assertEquals(CheckStatus.CRITICAL, check.getStatus());
    assertEquals("fail", check.getOutput());

    runAsync(h -> writeClient.passCheckWithNote(checkId, "pass", h));
    check = getCheckInfo(checkId);
    assertEquals(CheckStatus.PASSING, check.getStatus());
    assertEquals("pass", check.getOutput());

    sleep(vertx, 3000);

    check = getCheckInfo(checkId);
    assertEquals(CheckStatus.CRITICAL, check.getStatus());

    runAsync(h -> writeClient.deregisterCheck(checkId, h));
  }

  @Test
  public void httpCheckLifecycle() {
    VertxHttpServer vertxServer = new VertxHttpServer();
    vertxServer.start();

    HashMap<String, List<String>> headers = new HashMap<>();
    headers.put(TEST_HEADER_NAME, Collections.singletonList(TEST_HEADER_VALUE));

    CheckOptions opts = new CheckOptions()
      .setHttp("http://" + vertxServer.gateway() + ":" + vertxServer.getMappedPort(8080))
      .setInterval("2s")
      .setHeaders(headers)
      .setName("checkName");
    String checkId = createCheck(opts);

    sleep(vertx, 3000);
    Check check = getCheckInfo(checkId);
    assertEquals(CheckStatus.PASSING, check.getStatus());

    vertxServer.updateStatus(CheckStatus.WARNING);
    sleep(vertx, 3000);
    check = getCheckInfo(checkId);
    assertEquals(CheckStatus.WARNING, check.getStatus());

    vertxServer.updateStatus(CheckStatus.CRITICAL);
    sleep(vertx, 3000);
    check = getCheckInfo(checkId);
    assertEquals(CheckStatus.CRITICAL, check.getStatus());

    vertxServer.stop();
    runAsync(h -> writeClient.deregisterCheck(checkId, h));
  }

  @Test
  public void tcpCheckLifecycle() {
    VertxHttpServer reporter = new VertxHttpServer();
    reporter.start();

    CheckOptions opts = new CheckOptions()
      .setTcp(reporter.gateway() + ":" + reporter.getMappedPort(8080))
      .setInterval("2s")
      .setName("checkName");
    String checkId = createCheck(opts);

    sleep(vertx, 3000);
    Check check = getCheckInfo(checkId);
    assertEquals(CheckStatus.PASSING, check.getStatus());

    reporter.close();
    sleep(vertx, 3000);
    check = getCheckInfo(checkId);
    assertEquals(CheckStatus.CRITICAL, check.getStatus());

    runAsync(h -> writeClient.deregisterCheck(checkId, h));
  }

  Check getCheckInfo(String id) {
    List<Check> checks = getAsync(h -> writeClient.localChecks(h));
    return checks.stream()
      .filter(check -> check.getId().equals(id))
      .findFirst()
      .get();
  }

  void getCheckInfo(TestContext tc, String id, Handler<Check> resultHandler) {
    writeClient.localChecks(tc.asyncAssertSuccess(list -> {
      resultHandler.handle(list.stream()
        .filter(check -> check.getId().equals(id))
        .findFirst()
        .orElseThrow(NoSuchElementException::new));
    }));
  }
}
