package com.symphony.bdk.core.service.datafeed.impl;

import com.symphony.bdk.core.api.invoker.ApiClient;
import com.symphony.bdk.core.api.invoker.ApiException;
import com.symphony.bdk.core.auth.AuthSession;
import com.symphony.bdk.core.auth.exception.AuthUnauthorizedException;
import com.symphony.bdk.core.config.model.BdkConfig;
import com.symphony.bdk.gen.api.model.AckId;
import com.symphony.bdk.gen.api.model.V4Event;
import com.symphony.bdk.gen.api.model.V5Datafeed;
import com.symphony.bdk.gen.api.model.V5EventList;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A class for implementing the datafeed v1 service.
 */
@Slf4j
public class DatafeedServiceV2 extends AbstractDatafeedService {

    private final AtomicBoolean started = new AtomicBoolean();
    private final AckId ackId;
    private V5Datafeed datafeed;

    public DatafeedServiceV2(ApiClient agentClient, ApiClient podClient, AuthSession authSession, BdkConfig config) {
        super(agentClient, podClient, authSession, config);
        this.ackId = new AckId().ackId("");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() throws ApiException, AuthUnauthorizedException {
        if (this.started.get()) {
            throw new IllegalStateException("The datafeed service is already started");
        }
        try {
            this.datafeed = this.retrieveDatafeed();
            if (this.datafeed == null) {
                this.datafeed = this.createDatafeed();
            }
            log.debug("Start reading datafeed events");
            do {
                this.started.set(true);
                this.readDatafeed();
            } while (this.started.get());
        } catch (Throwable e) {
            if (e instanceof ApiException) {
                throw (ApiException) e;
            } else if (e instanceof AuthUnauthorizedException) {
                throw (AuthUnauthorizedException) e;
            } else {
                e.printStackTrace();
            }
        }
    }

    protected AckId getAckId() {
        return this.ackId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        this.started.set(false);
    }

    private V5Datafeed createDatafeed() throws Throwable {
        log.debug("Start creating datafeed from agent");
        Retry retry = this.getRetryInstance("Create V5Datafeed");
        return retry.executeCheckedSupplier(() -> {
            try {
                return this.datafeedApi.createDatafeed(authSession.getSessionToken(), authSession.getKeyManagerToken());
            } catch (ApiException e) {
                if (e.isUnauthorized()) {
                    log.info("Re-authenticate and try again");
                    authSession.refresh();
                } else {
                    log.error("Error {}: {}", e.getCode(), e.getMessage());
                }
                throw e;
            }
        });
    }

    private V5Datafeed retrieveDatafeed() throws Throwable {
        log.debug("Start retrieving datafeed from agent");
        Retry retry = this.getRetryInstance("Retrieve V5Datafeed");
        List<V5Datafeed> datafeeds = retry.executeCheckedSupplier(() -> {
            try {
                return this.datafeedApi.listDatafeed(authSession.getSessionToken(), authSession.getKeyManagerToken());
            } catch (ApiException e) {
                if (e.isUnauthorized()) {
                    log.info("Re-authenticate and try again");
                    authSession.refresh();
                } else {
                    log.error("Error {}: {}", e.getCode(), e.getMessage());
                }
                throw e;
            }
        });
        if (!datafeeds.isEmpty()) {
            return datafeeds.get(0);
        }
        return null;
    }

    private void readDatafeed() throws Throwable {
        log.debug("Reading datafeed events from datafeed {}", datafeed.getId());
        RetryConfig config = RetryConfig.from(this.retryConfig)
                .retryOnException(e -> {
                    if (e instanceof ApiException && e.getSuppressed().length == 0) {
                        ApiException apiException = (ApiException) e;
                        return apiException.isServerError() || apiException.isUnauthorized() || apiException.isClientError();
                    }
                    return false;
                }).build();
        Retry retry = this.getRetryInstance("Read Datafeed", config);
        retry.executeCheckedSupplier(() -> {
            try {
                V5EventList v5EventList = this.datafeedApi.readDatafeed(
                        datafeed.getId(),
                        authSession.getSessionToken(),
                        authSession.getKeyManagerToken(),
                        ackId);
                this.ackId.setAckId(v5EventList.getAckId());
                List<V4Event> events = v5EventList.getEvents();
                if (events != null && !events.isEmpty()) {
                    this.handleV4EventList(events);
                }
            } catch (ApiException e) {
                if (e.isUnauthorized()) {
                    log.info("Re-authenticate and try again");
                    authSession.refresh();
                } else {
                    log.error("Error {}: {}", e.getCode(), e.getMessage());
                    if (e.isClientError()) {
                        try {
                            log.info("Try to delete the faulty datafeed");
                            this.deleteDatafeed();
                            log.info("Recreate a new datafeed and try again");
                            this.datafeed = this.createDatafeed();
                        } catch (Throwable throwable) {
                            e.addSuppressed(throwable);
                        }
                    }
                }
                throw e;
            }
            return null;
        });
    }

    private void deleteDatafeed() throws Throwable {
        log.debug("Start deleting a faulty datafeed");
        Retry retry = this.getRetryInstance("Delete Datafeed");
        retry.executeCheckedSupplier(() -> {
            try {
                this.datafeedApi.deleteDatafeed(datafeed.getId(), authSession.getSessionToken(), authSession.getKeyManagerToken());
                this.datafeed = null;
            } catch (ApiException e) {
                if (e.isClientError()) {
                    log.debug("The datafeed doesn't exist or is already removed");
                } else {
                    if (e.isUnauthorized()) {
                        log.info("Re-authenticate and try again");
                        authSession.refresh();
                    } else {
                        log.error("Error {}: {}", e.getCode(), e.getMessage());
                    }
                    throw e;
                }
            }
            return null;
        });
    }

}