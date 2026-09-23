package ai.ml.core_concepts.multithreading.threadlocal.RealTimeUsecase;
// This example demonstrates how to use ThreadLocal to store a UserContext object, accessible throughout the request processing flow.
import java.util.UUID;

// Manager class to handle the ThreadLocal variable
public class UserSessionManager {
    // ThreadLocal to store UserContext for each thread
    private static final ThreadLocal<UserContext> userSessionInfo = ThreadLocal.withInitial(UserContext::new);

    public static void setUserSessionInfo(String username) {
        UserContext info = userSessionInfo.get();
        info.setSessionId(UUID.randomUUID().toString());
        info.setUsername(username);
    }

    public static UserContext getUserSessionInfo() {
        return userSessionInfo.get();
    }

    // IMPORTANT: Always call remove() to prevent memory leaks in thread pools
    public static void clearUserSessionInfo() {
        userSessionInfo.remove();
    }
}