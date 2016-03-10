package gov.max.service.file.services;

import org.slf4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class RateLimitManager {

    @Autowired
    private Logger log;

    private static final TemporalUnit TIME_UNIT = ChronoUnit.MILLIS;

    /**
     * Minimum delay between api calls in milliseconds
     */
    @Value("${rate.limit.delay}")
    private long minimumDelay;

    @Value("${rate.limit.postpone.delay}")
    private long postponeDelay;

    private ReentrantLock lock;
    private Map<String, ClientState> clientStateMap;

    @PostConstruct
    private void initialize() {
        lock = new ReentrantLock();
        clientStateMap = new HashMap<>();
    }

    public ClientState updateClientState(String sessionId)  {
        if (sessionId == null || sessionId.isEmpty()) throw new IllegalArgumentException("session must me specified!");

        final LocalDateTime now = LocalDateTime.now();
        ClientState clientState = null;

        lock.lock();
        try {
            if (!clientStateMap.containsKey(sessionId)) {
                clientState = new ClientState(sessionId, now);
                clientStateMap.put(sessionId, clientState);
            } else {
                clientState = clientStateMap.get(sessionId);
                if (!clientState.isPostponed()) {
                    //if minimum delay is not expired
                    if (clientState.getRecentCallTimestamp().isAfter(LocalDateTime.now().minus(minimumDelay, TIME_UNIT))) {
                        LocalDateTime postponeEndTimestamp = now.plus(postponeDelay, TIME_UNIT);
                        clientState.setPostponed(true);
                        clientState.setEndOfPostponePeriod(postponeEndTimestamp);

                        log.debug("client [sid: " + sessionId + "] will be postponed to call api till " + postponeEndTimestamp);
                    }
                    //update the most recent call timestamp
                    clientState.setRecentCallTimestamp(now);
                } else {
                    //check is postpone period is over
                    if (clientState.getEndOfPostponePeriod().isBefore(now)) {
                        clientState.setPostponed(false);
                        clientState.setRecentCallTimestamp(now);
                        log.debug("client [sid: " + sessionId + "] postpone period is over");
                    } else {
                        log.debug("client [sid: " + sessionId + "] is still postponed");
                    }
                }
            }
        } finally {
            lock.unlock();
        }

        return clientState;
    }

    /**
     * Represents client state in order to maintain details of the rate of api calls
     */
    public static class ClientState {
        private String sessionId;
        private LocalDateTime recentCallTimestamp;
        private boolean isPostponed;
        private LocalDateTime endOfPostponePeriod;

        public ClientState(String sessionId, LocalDateTime recentCallTimestamp) {
            this.sessionId = sessionId;
            this.recentCallTimestamp = recentCallTimestamp;
            this.isPostponed = false;
            this.endOfPostponePeriod = null;
        }

        public String getSessionId() {
            return sessionId;
        }

        public LocalDateTime getRecentCallTimestamp() {
            return recentCallTimestamp;
        }

        public void setRecentCallTimestamp(LocalDateTime recentCallTimestamp) {
            this.recentCallTimestamp = recentCallTimestamp;
        }

        public boolean isPostponed() {
            return isPostponed;
        }

        public void setPostponed(boolean postponed) {
            isPostponed = postponed;
        }

        public LocalDateTime getEndOfPostponePeriod() {
            return endOfPostponePeriod;
        }

        public void setEndOfPostponePeriod(LocalDateTime endOfPostponePeriod) {
            this.endOfPostponePeriod = endOfPostponePeriod;
        }

        @Override
        public String toString() {
            return "ClientState[" +
                    "sessionId='" + sessionId + '\'' +
                    ", recentCallTimestamp=" + recentCallTimestamp +
                    ", isPostponed=" + isPostponed +
                    ", endOfPostponePeriod=" + endOfPostponePeriod +
                    ']';
        }
    }
}
