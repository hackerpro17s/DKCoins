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

package net.pretronic.dkcoins.minecraft.commands.user;

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.AccountType;
import net.pretronic.dkcoins.api.account.RankedAccountCredit;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.config.DKCoinsConfig;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.GeneralUtil;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class TopCommand extends ObjectCommand<Currency> {

    public TopCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("top"));
    }

    @Override
    public void execute(CommandSender sender, Currency currency, String[] args) {
        int page = 1;
        if (args.length > 0) {
            String page0 = args[0];
            if (!GeneralUtil.isNaturalNumber(page0)) {
                sender.sendMessage(Messages.ERROR_NOT_NUMBER, VariableSet.create().add("value", page0));
                return;
            }
            page = Integer.parseInt(page0);
        }
        List<RankedAccountCredit> ranks = DKCoins.getInstance().getAccountManager()
                .getTopAccountCredits(currency, new AccountType[0], DKCoinsConfig.TOP_LIMIT_ENTRIES_PER_PAGE, page);
        if (ranks.isEmpty()) {
            sender.sendMessage(Messages.TOP_PAGE_NO_ENTRIES);
            return;
        }

        int start = DKCoinsConfig.TOP_LIMIT_ENTRIES_PER_PAGE * (page - 1) + 1;
        int end = page * DKCoinsConfig.TOP_LIMIT_ENTRIES_PER_PAGE;

        sender.sendMessage(Messages.TOP, VariableSet.create()
                .add("start", start)
                .add("end", end)
                .addDescribed("ranks", ranks)
                .add("page", page));
    }
}
