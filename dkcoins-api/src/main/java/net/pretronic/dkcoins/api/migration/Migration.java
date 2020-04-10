package net.pretronic.dkcoins.api.migration;

import net.pretronic.dkcoins.api.currency.Currency;

public interface Migration {

    String getName();

    Result migrate(Currency currency);

    class Result {

        private final boolean success;
        private final int totalMigrateCount;
        private final int dkcoinsAccountMigrateCount;
        private final int mcNativeMigrateCount;
        private final int skipped;
        private final long time;

        public Result(boolean success, int totalMigrateCount, int dkcoinsAccountMigrateCount, int mcNativeMigrateCount, int skipped, long time) {
            this.success = success;
            this.totalMigrateCount = totalMigrateCount;
            this.dkcoinsAccountMigrateCount = dkcoinsAccountMigrateCount;
            this.mcNativeMigrateCount = mcNativeMigrateCount;
            this.skipped = skipped;
            this.time = time;
        }

        public boolean isSuccess() {
            return success;
        }

        public boolean isFailed() {
            return !success;
        }

        public int getTotalMigrateCount() {
            return totalMigrateCount;
        }

        public int getDKCoinsAccountMigrateCount() {
            return dkcoinsAccountMigrateCount;
        }

        public int getMcNativeMigrateCount() {
            return mcNativeMigrateCount;
        }

        public int getSkipped() {
            return skipped;
        }

        public long getTime() {
            return time;
        }
    }
}
