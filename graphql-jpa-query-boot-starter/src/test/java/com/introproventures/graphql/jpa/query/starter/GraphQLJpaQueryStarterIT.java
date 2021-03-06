/*
 * Copyright 2017 IntroPro Ventures, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.introproventures.graphql.jpa.query.starter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.introproventures.graphql.jpa.query.web.GraphQLController.GraphQLQueryRequest;
import graphql.ExecutionResult;
import graphql.GraphQLError;
import lombok.Value;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class GraphQLJpaQueryStarterIT {
    private static final String	WAR_AND_PEACE	= "War and Peace";

    @Autowired
    private TestRestTemplate rest;

    @SpringBootApplication
    static class Application {
    }

    @Test
    public void testGraphql() {
        GraphQLQueryRequest query = new GraphQLQueryRequest("{Books(where:{title:{EQ: \"" + WAR_AND_PEACE + "\"}}){ select {title genre}}}");

        ResponseEntity<Result> entity = rest.postForEntity("/graphql", new HttpEntity<>(query), Result.class);
        Assert.assertEquals(entity.toString(), HttpStatus.OK, entity.getStatusCode());

        Result result = entity.getBody();
        Assert.assertNotNull(result);
        Assert.assertNull(result.getErrors());
        Assert.assertEquals("{Books={select=[{title=War and Peace, genre=NOVEL}]}}", result.getData().toString());
    }

    @Test
    public void testGraphqlArguments() throws JsonParseException, JsonMappingException, IOException {
        GraphQLQueryRequest query = new GraphQLQueryRequest("query BookQuery($title: String!){Books(where:{title:{EQ: $title}}){select{title genre}}}");

        HashMap<String, Object> variables = new HashMap<>();
        variables.put("title", WAR_AND_PEACE);

        query.setVariables(variables);


        ResponseEntity<Result> entity = rest.postForEntity("/graphql", new HttpEntity<>(query), Result.class);
        Assert.assertEquals(entity.toString(), HttpStatus.OK, entity.getStatusCode());

        Result result = entity.getBody();
        Assert.assertNotNull(result);
        Assert.assertNull(result.getErrors());
        Assert.assertEquals("{Books={select=[{title=War and Peace, genre=NOVEL}]}}", result.getData().toString());
    }
}

@Value
class Result implements ExecutionResult {
    Map<String, Object> data;
    List<GraphQLError> errors;
    Map<Object, Object> extensions;

    @Override
    public List<GraphQLError> getErrors() {
        return errors;
    }

    @Override
    public <T> T getData() {
        return null;
    }

    @Override
    public boolean isDataPresent() {
        return data != null;
    }

    @Override
    public Map<Object, Object> getExtensions() {
        return extensions;
    }

    @Override
    public Map<String, Object> toSpecification() {
        return new HashMap<>();
    }
}
