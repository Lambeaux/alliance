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

import static org.apache.commons.lang3.BooleanUtils.or;
import static org.codice.alliance.catalog.core.api.types.Security.CLASSIFICATION;
import static org.codice.alliance.catalog.core.api.types.Security.CLASSIFICATION_SYSTEM;
import static org.codice.alliance.catalog.core.api.types.Security.CODEWORDS;
import static org.codice.alliance.catalog.core.api.types.Security.DISSEMINATION_CONTROLS;
import static org.codice.alliance.catalog.core.api.types.Security.OWNER_PRODUCER;
import static org.codice.alliance.catalog.core.api.types.Security.RELEASABILITY;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import ddf.catalog.plugin.AccessPlugin;
import ddf.catalog.plugin.StopProcessingException;

/**
 * Metacards ingested or created from an ingested product that <b>do not</b> contain security markings
 * are rejected, where the degree of the expected markings can be configured using this plugin to enable
 * lenient or stricter behavior.
 */
public class UnmarkedMetacardRejectionPlugin implements AccessPlugin {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(UnmarkedMetacardRejectionPlugin.class);

    private static final String REJECT_UNMARKED_ERROR_MESSAGE =
            "Cannot ingest unmarked data. Security attributes required.";

    private boolean classificationSystemRequired;

    private boolean releasabilityRequired;

    private boolean codewordsRequired;

    private boolean disseminationControlsRequired;

    public UnmarkedMetacardRejectionPlugin() {
        this.classificationSystemRequired = false;
        this.releasabilityRequired = false;
        this.codewordsRequired = false;
        this.disseminationControlsRequired = false;
    }

    public boolean isClassificationSystemRequired() {
        return classificationSystemRequired;
    }

    public boolean isReleasabilityRequired() {
        return releasabilityRequired;
    }

    public boolean isCodewordsRequired() {
        return codewordsRequired;
    }

    public boolean isDisseminationControlsRequired() {
        return disseminationControlsRequired;
    }

    public void setClassificationSystemRequired(boolean classificationSystemRequired) {
        this.classificationSystemRequired = classificationSystemRequired;
    }

    public void setReleasabilityRequired(boolean releasabilityRequired) {
        this.releasabilityRequired = releasabilityRequired;
    }

    public void setCodewordsRequired(boolean codewordsRequired) {
        this.codewordsRequired = codewordsRequired;
    }

    public void setDisseminationControlsRequired(boolean disseminationControlsRequired) {
        this.disseminationControlsRequired = disseminationControlsRequired;
    }

    @Override
    public CreateRequest processPreCreate(CreateRequest input) throws StopProcessingException {
        checkMarkings(input.getMetacards());
        return input;
    }

    @Override
    public UpdateRequest processPreUpdate(UpdateRequest input,
            Map<String, Metacard> existingMetacards) throws StopProcessingException {
        return input;
    }

    @Override
    public DeleteRequest processPreDelete(DeleteRequest input) throws StopProcessingException {
        return input;
    }

    @Override
    public DeleteResponse processPostDelete(DeleteResponse input) throws StopProcessingException {
        return input;
    }

    @Override
    public QueryRequest processPreQuery(QueryRequest input) throws StopProcessingException {
        return input;
    }

    @Override
    public QueryResponse processPostQuery(QueryResponse input) throws StopProcessingException {
        return input;
    }

    @Override
    public ResourceRequest processPreResource(ResourceRequest input)
            throws StopProcessingException {
        return input;
    }

    @Override
    public ResourceResponse processPostResource(ResourceResponse input, Metacard metacard)
            throws StopProcessingException {
        return input;
    }

    private void checkMarkings(List<Metacard> metacards) throws StopProcessingException {

        for (Metacard metacard : metacards) {

            Attribute classification = metacard.getAttribute(CLASSIFICATION);
            Attribute ownerProducer = metacard.getAttribute(OWNER_PRODUCER);

            LOGGER.debug("Classification: {}", classification);
            LOGGER.debug("Owner-Producer: {}", ownerProducer);

            if (isAttributeCompletelyNull(classification)
                    || isAttributeCompletelyNull(ownerProducer)) {
                throw new StopProcessingException(REJECT_UNMARKED_ERROR_MESSAGE);
            }

            LOGGER.debug("Minimal security requirements were met for the product");

            Attribute classificationSystem = metacard.getAttribute(CLASSIFICATION_SYSTEM);
            Attribute releasability = metacard.getAttribute(RELEASABILITY);
            Attribute codewords = metacard.getAttribute(CODEWORDS);
            Attribute disseminationControls = metacard.getAttribute(DISSEMINATION_CONTROLS);

            LOGGER.debug("Classification-System: {}", classificationSystem);
            LOGGER.debug("Releasability: {}", releasability);
            LOGGER.debug("Codewords: {}", codewords);
            LOGGER.debug("Dissemination-Controls: {}", disseminationControls);

            // ** Without the explicit array we fail compilation due to ambiguous method **
            if (or(new Boolean[] {
                    classificationSystemRequired && isAttributeCompletelyNull(classificationSystem),
                    releasabilityRequired && isAttributeCompletelyNull(releasability),
                    codewordsRequired && isAttributeCompletelyNull(codewords),
                    disseminationControlsRequired
                            && isAttributeCompletelyNull(disseminationControls)})) {
                throw new StopProcessingException(REJECT_UNMARKED_ERROR_MESSAGE);
            }

            LOGGER.debug("All security requirements were met for the product");
        }
    }

    private boolean isAttributeCompletelyNull(Attribute attribute) {
        return attribute == null || attribute.getValue() == null;
    }
}
