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
package io.vertx.ext.consul;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.JsonGen;
import io.vertx.core.json.JsonObject;

/**
 * Options used to requesting list of nodes
 *
 * @author <a href="mailto:ruslan.sennov@gmail.com">Ruslan Sennov</a>
 */
@DataObject
@JsonGen(publicConverter = false)
public class NodeQueryOptions {

  private String near;
  private BlockingQueryOptions options;

  /**
   * Default constructor
   */
  public NodeQueryOptions() {}

  /**
   * Constructor from JSON
   *
   * @param json the JSON
   */
  public NodeQueryOptions(JsonObject json) {
    NodeQueryOptionsConverter.fromJson(json, this);
  }

  /**
   * Convert to JSON
   *
   * @return the JSON
   */
  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    NodeQueryOptionsConverter.toJson(this, jsonObject);
    return jsonObject;
  }

  /**
   * Get node name for sorting the list in ascending order based on the estimated round trip time from that node.
   *
   * @return the node name
   */
  public String getNear() {
    return near;
  }

  /**
   * Set node name for sorting the list in ascending order based on the estimated round trip time from that node.
   *
   * @param near the node name
   * @return reference to this, for fluency
   */
  public NodeQueryOptions setNear(String near) {
    this.near = near;
    return this;
  }

  /**
   * Get options for blocking query
   *
   * @return the blocking options
   */
  public BlockingQueryOptions getBlockingOptions() {
    return options;
  }

  /**
   * Set options for blocking query
   *
   * @param options the blocking options
   * @return reference to this, for fluency
   */
  public NodeQueryOptions setBlockingOptions(BlockingQueryOptions options) {
    this.options = options;
    return this;
  }
}
