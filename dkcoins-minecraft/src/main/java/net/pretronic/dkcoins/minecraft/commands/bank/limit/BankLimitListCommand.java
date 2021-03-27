package net.pretronic.dkcoins.minecraft.commands.bank.limit;

import net.pretronic.dkcoins.api.account.limitation.AccountLimitation;
import net.pretronic.dkcoins.api.account.limitation.LimitationAble;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;

import java.util.Collection;

public class BankLimitListCommand extends ObjectCommand<LimitationAble> {

    public BankLimitListCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("info"));
    }

    @Override
    public void execute(CommandSender commandSender, LimitationAble entity, String[] args) {
        Collection<AccountLimitation> limitations = entity.getLimitations();
        if(limitations.isEmpty()) {
            commandSender.sendMessage(Messages.COMMAND_BANK_INFO_NO_LIMITATION);
        } else {
            commandSender.sendMessage(Messages.COMMAND_BANK_INFO_LIMITATION, VariableSet.create()
                    .addDescribed("limitations", limitations));
        }
    }
}
