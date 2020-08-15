/*
 * (C) Copyright 2020 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 02.08.20, 20:44
 * @web %web%
 *
 * The DKCoins Project is under the Apache License, version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

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
