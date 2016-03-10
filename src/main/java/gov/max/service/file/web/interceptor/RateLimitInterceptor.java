package gov.max.service.file.web.interceptor;

import gov.max.service.file.services.RateLimitManager;

import org.slf4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class RateLimitInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private Logger log;

    private static final int ERROR_CODE_CALL_LIMIT_EXCEEDED = 429;
    private static final int ERROR_CODE_UNKNOWN_ERROR = 400;

    @Value("${api.key.header.name}")
    private String apiKeyHeaderName;

    @Autowired
    private RateLimitManager rateLimitManager;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        boolean result = false;
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (path.contains("download")) {
//        String sessionId = request.getSession().getId();
            String clientRemoveAddr = request.getRemoteAddr();
            RateLimitManager.ClientState clientState = rateLimitManager.updateClientState(clientRemoveAddr);
            if (clientState != null) {
                if (clientState.isPostponed()) {
                    String message = "api calls limit exceeded, you will be postponed till " + clientState.getEndOfPostponePeriod();
                    log.debug(message);
                    response.sendError(ERROR_CODE_CALL_LIMIT_EXCEEDED, message);
                } else {
                    log.debug("client state is ok, sending response");
                    result = true;
                }
            } else {
                log.error("unknown error, client state is null");
                response.sendError(ERROR_CODE_UNKNOWN_ERROR, "unknown error");
            }

        } else {
            result = true;
        }
        return result;
    }
}
