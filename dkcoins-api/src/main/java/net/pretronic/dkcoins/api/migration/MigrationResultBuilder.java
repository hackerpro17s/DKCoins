package net.pretronic.dkcoins.api.migration;

public class MigrationResultBuilder {

    private boolean success;
    private int totalMigrateCount;
    private int dkcoinsAccountMigrateCount;
    private int mcNativeMigrateCount;
    private int skipped;
    private long time;

    public MigrationResultBuilder setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public MigrationResultBuilder setTotalMigrateCount(int totalMigrateCount) {
        this.totalMigrateCount = totalMigrateCount;
        return this;
    }

    public MigrationResultBuilder setDkcoinsAccountMigrateCount(int dkcoinsAccountMigrateCount) {
        this.dkcoinsAccountMigrateCount = dkcoinsAccountMigrateCount;
        return this;
    }

    public MigrationResultBuilder setMcNativeMigrateCount(int mcNativeMigrateCount) {
        this.mcNativeMigrateCount = mcNativeMigrateCount;
        return this;
    }

    public MigrationResultBuilder setSkipped(int skipped) {
        this.skipped = skipped;
        return this;
    }

    public MigrationResultBuilder setTime(long time) {
        this.time = time;
        return this;
    }

    public MigrationResult build() {
        return new MigrationResult(success, totalMigrateCount, dkcoinsAccountMigrateCount, mcNativeMigrateCount, skipped, time);
    }
}