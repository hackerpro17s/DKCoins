package net.pretronic.dkcoins.minecraft.commands.bank.limit;

import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.access.AccessRight;
import net.pretronic.dkcoins.api.account.limitation.LimitationAble;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.commands.CommandUtil;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.DefinedNotFindable;
import net.pretronic.libraries.command.command.object.MainObjectCommand;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.Textable;
import net.pretronic.libraries.utility.Validate;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;

public class BankLimitCommand extends MainObjectCommand<LimitationAble> implements DefinedNotFindable<LimitationAble> {

    protected final Textable helpMessage;

    private final BankLimitListCommand listCommand;

    public BankLimitCommand(ObjectOwner owner, Textable helpMessage) {
        super(owner, CommandConfiguration.name("limit"));
        Validate.notNull(helpMessage);

        this.helpMessage = helpMessage;
        this.listCommand = new BankLimitListCommand(owner);

        registerCommand(new BankLimitSetCommand(owner, helpMessage));
        registerCommand(new BankLimitRemoveCommand(owner, helpMessage));
    }

    @Override
    public LimitationAble getObject(CommandSender commandSender, String name) {
        throw new UnsupportedOperationException("No objects available (Objects should be forwarded)");
    }

    @Override
    public void execute(CommandSender sender, LimitationAble entity, String[] args) {
        if(entity != null && !CommandUtil.hasAccountAccess(sender, entity.getAccount(), AccessRight.LIMIT_MANAGEMENT)) {
            sender.sendMessage(Messages.ERROR_ACCOUNT_MEMBER_NOT_ENOUGH_ACCESS_RIGHTS);
            return;
        }
        super.execute(sender, entity, args);
    }

    @Override
    public void commandNotFound(CommandSender commandSender, LimitationAble entity, String command, String[] args) {
        if(entity != null && command == null) {
            listCommand.execute(commandSender, entity, args);
        } else {
            commandSender.sendMessage(this.helpMessage);
        }
    }
}
