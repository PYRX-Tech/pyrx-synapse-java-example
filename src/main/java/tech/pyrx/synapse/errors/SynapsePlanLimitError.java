package tech.pyrx.synapse.errors;

/**
 * Raised on 403 with code "plan_limit_reached".
 */
public class SynapsePlanLimitError extends SynapseError {

    private final String limitType;
    private final int current;
    private final int maximum;
    private final String plan;

    public SynapsePlanLimitError(String message, int status, String code, String requestId,
                                  String limitType, int current, int maximum, String plan) {
        super(message, status, code, requestId);
        this.limitType = limitType;
        this.current = current;
        this.maximum = maximum;
        this.plan = plan;
    }

    public String getLimitType() { return limitType; }
    public int getCurrent() { return current; }
    public int getMaximum() { return maximum; }
    public String getPlan() { return plan; }
}
