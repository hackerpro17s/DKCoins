package net.pretronic.dkcoins.minecraft.commands.bank.limit;

import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.limitation.AccountLimitation;
import net.pretronic.dkcoins.api.account.limitation.AccountLimitationCalculationType;
import net.pretronic.dkcoins.api.account.limitation.AccountLimitationInterval;
import net.pretronic.dkcoins.api.account.limitation.LimitationAble;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.account.member.RoleAble;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.api.user.DKCoinsUser;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.libraries.command.Completable;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.Textable;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.GeneralUtil;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.Validate;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import org.mcnative.runtime.api.player.MinecraftPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class BankLimitSetCommand extends ObjectCommand<LimitationAble> implements Completable {

    private final Textable helpMessage;

    public BankLimitSetCommand(ObjectOwner owner, Textable helpMessage) {
        super(owner, CommandConfiguration.name("set"));
        Validate.notNull(helpMessage);
        this.helpMessage = helpMessage;
    }

    @Override
    public void execute(CommandSender commandSender, LimitationAble entity, String[] args) {
        if(commandSender instanceof MinecraftPlayer) {
            if(entity instanceof RoleAble) {
                RoleAble target = (RoleAble) entity;
                System.out.println("Debug:"+entity.getAccount().getMembers());
                for (AccountMember member : entity.getAccount().getMembers()) {
                    System.out.println(member.getName() + ":" + member.getRole());
                }
                System.out.println("---");
                AccountMember sender = entity.getAccount().getMember(((MinecraftPlayer)commandSender).getAs(DKCoinsUser.class));
                if(!sender.getRole().isHigher(target.getRole())) {
                    commandSender.sendMessage(Messages.ERROR_ACCOUNT_MEMBER_ROLE_LOWER,
                            VariableSet.create().addDescribed("targetRole", target.getRole()));
                    return;
                }
            }
        }
        if(args.length == 4) {
            String interval0 = args[0];
            AccountLimitationInterval interval = AccountLimitationInterval.parse(args[0]);
            if(interval == null) {
                commandSender.sendMessage(Messages.ERROR_ACCOUNT_LIMITATION_INTERVAL_NOT_VALID, VariableSet.create().add("value", interval0));
                return;
            }
            String amount0 = args[1];
            if(!GeneralUtil.isNumber(amount0)) {
                commandSender.sendMessage(Messages.ERROR_NOT_NUMBER, VariableSet.create().add("value", amount0));
                return;
            }
            double amount = Double.parseDouble(amount0);

            String calculationType0 = args[2];
            AccountLimitationCalculationType calculationType = AccountLimitationCalculationType.parse(calculationType0);
            if(calculationType == null) {
                commandSender.sendMessage(Messages.ERROR_ACCOUNT_LIMITATION_CALCULATION_TYPE_NOT_VALID, VariableSet.create().add("value", calculationType0));
                return;
            }
            String currencyName = args[3];
            Currency currency = DKCoins.getInstance().getCurrencyManager().getCurrency(currencyName);
            if(currency == null) {
                commandSender.sendMessage(Messages.ERROR_CURRENCY_NOT_EXISTS, VariableSet.create().add("name", currencyName));
                return;
            }

            AccountLimitation limitation = entity.addLimitation(currency, calculationType, amount, interval);
            commandSender.sendMessage(Messages.COMMAND_BANK_LIMIT_SET, VariableSet.create()
                    .addDescribed("limitation", limitation));
        } else {
            commandSender.sendMessage(helpMessage);
        }
    }

    @Override
    public Collection<String> complete(CommandSender commandSender, String[] args) {
        if(args.length == 3){
            Collection<String> result = new ArrayList<>();
            for (AccountLimitationCalculationType type : AccountLimitationCalculationType.values()) {
                if(type.name().startsWith(args[2].toUpperCase())){
                    result.add(type.name());
                }
            }
            return result;
        }else if(args.length == 4){
            return Iterators.map(DKCoins.getInstance().getCurrencyManager().getCurrencies()
                    ,Currency::getName
                    ,currency -> currency.getName().toLowerCase().startsWith(args[3].toLowerCase()));
        }
        return Collections.emptyList();
    }
}
