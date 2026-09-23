package ai.ml.core_concepts.multithreading.threadlocal.RealTimeUsecase;

// Class to hold user-specific information
class UserContext {
    private String sessionId;
    private String username;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "UserContext{sessionId='" + sessionId + "', username='" + username + "'}";
    }
}