package net.pretronic.dkcoins.minecraft.commands.bank.role;

import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.access.AccessRight;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.account.member.AccountMemberRole;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.commands.CommandUtil;
import net.pretronic.dkcoins.minecraft.commands.bank.limit.BankLimitCommand;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.DefinedCompletable;
import net.pretronic.libraries.command.command.object.DefinedNotFindable;
import net.pretronic.libraries.command.command.object.multiple.MultipleMainObjectCommand;
import net.pretronic.libraries.command.command.object.multiple.MultipleObjectCompletable;
import net.pretronic.libraries.command.command.object.multiple.MultipleObjectNotFindable;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;

import java.util.Collection;
import java.util.Collections;

public class BankRoleCommand extends MultipleMainObjectCommand<BankAccount, AccountMemberRole> implements DefinedNotFindable<AccountMemberRole>
        , MultipleObjectNotFindable<BankAccount>, MultipleObjectCompletable<BankAccount> {

    private final BankRoleListCommand listCommand;
    private final BankRoleInfoCommand infoCommand;

    public BankRoleCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("role"));

        this.listCommand = new BankRoleListCommand(owner);
        this.infoCommand = new BankRoleInfoCommand(owner);

        registerCommand(new BankLimitCommand(owner, Messages.COMMAND_BANK_ROLE_LIMIT_HELP));
        registerCommand(infoCommand);
    }

    @Override
    public void commandNotFound(CommandSender commandSender, AccountMemberRole role, String command, String[] args) {
        if(role != null && command == null) {
            infoCommand.execute(commandSender, role, args);
        } else {
            commandSender.sendMessage(Messages.COMMAND_BANK_ROLE_HELP);
        }
    }

    @Override
    public AccountMemberRole getObject(CommandSender commandSender, BankAccount account, String name) {
        if(name.equalsIgnoreCase("list")) return null;
        return account.getRole(name);
    }

    @Override
    public void execute(CommandSender commandSender, BankAccount account, String[] args) {
        if(!CommandUtil.hasAccountAccess(commandSender, account, AccessRight.ROLE_MANAGEMENT)) {
            commandSender.sendMessage(Messages.ERROR_ACCOUNT_MEMBER_NOT_ENOUGH_ACCESS_RIGHTS);
        }
        super.execute(commandSender, account, args);
    }

    @Override
    public void objectNotFound(CommandSender commandSender, BankAccount account, String command, String[] args) {
        if(command.equalsIgnoreCase("list") || command.equalsIgnoreCase("l")) {
            this.listCommand.execute(commandSender, account, args);
        } else {
            commandSender.sendMessage(Messages.ERROR_ACCOUNT_MEMBER_ROLE_NOT_EXISTS, VariableSet.create().add("name", command));
        }
    }

    @Override
    public Collection<String> complete(CommandSender commandSender, BankAccount account, String name) {
        return Iterators.map(account.getRoles()
                ,AccountMemberRole::getName
                ,member1 -> member1.getName().toLowerCase().startsWith(name));
    }
}
