/**
 * Copyright (c) Codice Foundation
 * <p>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.catalog.plugin.rejection;

import static org.codice.alliance.catalog.core.api.types.Security.CLASSIFICATION;
import static org.codice.alliance.catalog.core.api.types.Security.CLASSIFICATION_SYSTEM;
import static org.codice.alliance.catalog.core.api.types.Security.CODEWORDS;
import static org.codice.alliance.catalog.core.api.types.Security.DISSEMINATION_CONTROLS;
import static org.codice.alliance.catalog.core.api.types.Security.OWNER_PRODUCER;
import static org.codice.alliance.catalog.core.api.types.Security.RELEASABILITY;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.operation.CreateRequest;
import ddf.catalog.operation.DeleteRequest;
import ddf.catalog.operation.DeleteResponse;
import ddf.catalog.operation.QueryRequest;
import ddf.catalog.operation.QueryResponse;
import ddf.catalog.operation.ResourceRequest;
import ddf.catalog.operation.ResourceResponse;
import ddf.catalog.operation.UpdateRequest;
import ddf.catalog.plugin.StopProcessingException;

/**
 * Verify the behavior of {@link UnmarkedMetacardRejectionPlugin}.
 */
@RunWith(MockitoJUnitRunner.class)
public class UnmarkedMetacardRejectionPluginTest {

    private UnmarkedMetacardRejectionPlugin unmarkedMetacardRejectionPlugin;

    @Mock
    private CreateRequest mockCreateRequest;

    @Mock
    private Metacard mockMetacard1;

    @Mock
    private Metacard mockMetacard2;

    @Mock
    private Attribute mockNullAttribute;

    @Mock
    private Attribute mockAttributeWithValue;

    @Before
    public void setup() throws Exception {
        when(mockAttributeWithValue.getValue()).thenReturn("TEST_VALUE");

        unmarkedMetacardRejectionPlugin = new UnmarkedMetacardRejectionPlugin();
    }

    @Test
    public void testNoExceptionWhenMetacardHasMarkings() throws Exception {
        setClassificationAndOwnerProducerExpectation(mockMetacard1,
                mockAttributeWithValue,
                mockAttributeWithValue);

        when(mockCreateRequest.getMetacards()).thenReturn(Arrays.asList(mockMetacard1));

        unmarkedMetacardRejectionPlugin.processPreCreate(mockCreateRequest);
    }

    @Test(expected = StopProcessingException.class)
    public void testThrowsExceptionWhenMetacardMissingClassification() throws Exception {
        setClassificationAndOwnerProducerExpectation(mockMetacard1, null, mockAttributeWithValue);

        when(mockCreateRequest.getMetacards()).thenReturn(Arrays.asList(mockMetacard1));

        unmarkedMetacardRejectionPlugin.processPreCreate(mockCreateRequest);
    }

    @Test(expected = StopProcessingException.class)
    public void testThrowsExceptionWhenMetacardMissingOwnerProducer() throws Exception {
        setClassificationAndOwnerProducerExpectation(mockMetacard1,
                mockAttributeWithValue,
                mockNullAttribute);

        when(mockCreateRequest.getMetacards()).thenReturn(Arrays.asList(mockMetacard1));

        unmarkedMetacardRejectionPlugin.processPreCreate(mockCreateRequest);
    }

    @Test(expected = StopProcessingException.class)
    public void testThrowsExceptionWhenClassificationSystemRequired() throws Exception {
        unmarkedMetacardRejectionPlugin.setClassificationSystemRequired(true);

        processPreCreateForMetacardsWithOnlyBasicMarkings();
    }

    @Test(expected = StopProcessingException.class)
    public void testThrowsExceptionWhenReleasabilityRequired() throws Exception {
        unmarkedMetacardRejectionPlugin.setReleasabilityRequired(true);

        processPreCreateForMetacardsWithOnlyBasicMarkings();
    }

    @Test(expected = StopProcessingException.class)
    public void testThrowsExceptionWhenCodewordsRequired() throws Exception {
        unmarkedMetacardRejectionPlugin.setCodewordsRequired(true);

        processPreCreateForMetacardsWithOnlyBasicMarkings();
    }

    @Test(expected = StopProcessingException.class)
    public void testThrowsExceptionWhenDisseminationControlsRequired() throws Exception {
        unmarkedMetacardRejectionPlugin.setDisseminationControlsRequired(true);

        processPreCreateForMetacardsWithOnlyBasicMarkings();
    }

    private void setClassificationAndOwnerProducerExpectation(Metacard metacard,
            Attribute classification, Attribute ownerProducer) {
        when(metacard.getAttribute(CLASSIFICATION)).thenReturn(classification);
        when(metacard.getAttribute(OWNER_PRODUCER)).thenReturn(ownerProducer);
    }

    private void processPreCreateForMetacardsWithOnlyBasicMarkings()
            throws StopProcessingException {
        setClassificationAndOwnerProducerExpectation(mockMetacard1,
                mockAttributeWithValue,
                mockAttributeWithValue);
        setClassificationAndOwnerProducerExpectation(mockMetacard2,
                mockAttributeWithValue,
                mockAttributeWithValue);

        when(mockMetacard1.getAttribute(CLASSIFICATION_SYSTEM)).thenReturn(mockNullAttribute);
        when(mockMetacard1.getAttribute(RELEASABILITY)).thenReturn(null);
        when(mockMetacard1.getAttribute(CODEWORDS)).thenReturn(mockAttributeWithValue);
        when(mockMetacard1.getAttribute(DISSEMINATION_CONTROLS)).thenReturn(mockAttributeWithValue);

        when(mockMetacard2.getAttribute(CLASSIFICATION_SYSTEM)).thenReturn(mockAttributeWithValue);
        when(mockMetacard2.getAttribute(RELEASABILITY)).thenReturn(mockAttributeWithValue);
        when(mockMetacard2.getAttribute(CODEWORDS)).thenReturn(mockNullAttribute);
        when(mockMetacard2.getAttribute(DISSEMINATION_CONTROLS)).thenReturn(null);

        when(mockCreateRequest.getMetacards()).thenReturn(Arrays.asList(mockMetacard1,
                mockMetacard2));

        unmarkedMetacardRejectionPlugin.processPreCreate(mockCreateRequest);
    }

    @Test
    public void testProcessPreUpdate() throws Exception {
        UpdateRequest request = mock(UpdateRequest.class);
        assertThat(request, is(unmarkedMetacardRejectionPlugin.processPreUpdate(request, null)));
    }

    @Test
    public void testProcessPreDelete() throws Exception {
        DeleteRequest request = mock(DeleteRequest.class);
        assertThat(request, is(unmarkedMetacardRejectionPlugin.processPreDelete(request)));
    }

    @Test
    public void testProcessPostDelete() throws Exception {
        DeleteResponse response = mock(DeleteResponse.class);
        assertThat(response, is(unmarkedMetacardRejectionPlugin.processPostDelete(response)));
    }

    @Test
    public void testProcessPreQuery() throws Exception {
        QueryRequest request = mock(QueryRequest.class);
        assertThat(request, is(unmarkedMetacardRejectionPlugin.processPreQuery(request)));
    }

    @Test
    public void testProcessPostQuery() throws Exception {
        QueryResponse response = mock(QueryResponse.class);
        assertThat(response, is(unmarkedMetacardRejectionPlugin.processPostQuery(response)));
    }

    @Test
    public void testProcessPreResource() throws Exception {
        ResourceRequest request = mock(ResourceRequest.class);
        assertThat(request, is(unmarkedMetacardRejectionPlugin.processPreResource(request)));
    }

    @Test
    public void testProcessPostResource() throws Exception {
        ResourceResponse response = mock(ResourceResponse.class);
        assertThat(response,
                is(unmarkedMetacardRejectionPlugin.processPostResource(response, null)));
    }

    @Test
    public void testInspectionOfRequiredFields() throws Exception {
        assertThat(false, is(unmarkedMetacardRejectionPlugin.isClassificationSystemRequired()));
        assertThat(false, is(unmarkedMetacardRejectionPlugin.isReleasabilityRequired()));
        assertThat(false, is(unmarkedMetacardRejectionPlugin.isCodewordsRequired()));
        assertThat(false, is(unmarkedMetacardRejectionPlugin.isDisseminationControlsRequired()));
    }
}
