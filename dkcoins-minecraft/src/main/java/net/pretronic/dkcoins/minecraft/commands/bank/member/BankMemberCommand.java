package net.pretronic.dkcoins.minecraft.commands.bank.member;

import net.pretronic.libraries.command.NotFindable;
import net.pretronic.libraries.command.command.BasicCommand;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.object.MainObjectCommand;
import net.pretronic.libraries.command.command.object.ObjectCommand;
import net.pretronic.libraries.command.command.object.ObjectNotFindable;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.command.sender.ConsoleCommandSender;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.libraries.utility.map.Pair;
import net.pretronic.dkcoins.api.DKCoins;
import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.access.AccessRight;
import net.pretronic.dkcoins.api.account.member.AccountMember;
import net.pretronic.dkcoins.api.user.DKCoinsUser;
import net.pretronic.dkcoins.minecraft.Messages;
import net.pretronic.dkcoins.minecraft.commands.CommandUtil;

import java.util.Arrays;

public class BankMemberCommand extends ObjectCommand<BankAccount> {

    private final ObjectCommand<BankAccount> listCommand;
    private final ObjectCommand<AccountMember> infoCommand;
    private final ObjectCommand<AccountMember> roleCommand;
    private final ObjectCommand<AccountMember> removeCommand;
    private final ObjectCommand<AccountMember> limitCommand;
    private final ObjectCommand<Pair<BankAccount, String>> addCommand;

    public BankMemberCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("member"));
        this.listCommand = new BankMemberListCommand(owner);
        this.infoCommand = new BankMemberInfoCommand(owner);
        this.roleCommand = new BankMemberRoleCommand(owner);
        this.removeCommand = new BankMemberRemoveCommand(owner);
        this.limitCommand = new BankMemberLimitCommand(owner);
        this.addCommand = new BankMemberAddCommand(owner);
    }

    @Override
    public void execute(CommandSender commandSender, BankAccount account, String[] args) {
        if(CommandUtil.hasAccessAndSendMessage(commandSender, account, AccessRight.MEMBER_MANAGEMENT)) {
            if(account.getType().getName().equalsIgnoreCase("User")) {
                commandSender.sendMessage(Messages.ERROR_ACCOUNT_USER_NOT_POSSIBLE);
                return;
            }
            if(args.length < 1 || args[0].equalsIgnoreCase("list")) {
                this.listCommand.execute(commandSender, account, args);
                return;
            }
            String name = args[0];

            if(args.length < 2) {
                AccountMember member = CommandUtil.parseAccountMember(commandSender, name, account);
                if(member != null) {
                    this.infoCommand.execute(commandSender, member, args);
                }
                return;
            }
            switch (args[1].toLowerCase()) {
                case "help": {
                    commandSender.sendMessage(Messages.COMMAND_BANK_MEMBER_HELP);
                    break;
                }
                case "info": {
                    AccountMember member = CommandUtil.parseAccountMember(commandSender, name, account);
                    if(member != null) {
                        this.infoCommand.execute(commandSender, member, args);
                    }
                    break;
                }
                case "role": {
                    AccountMember member = CommandUtil.parseAccountMember(commandSender, name, account);
                    if(member != null) {
                        this.roleCommand.execute(commandSender, member, Arrays.copyOfRange(args, 2, args.length));
                    }
                    break;
                }
                case "remove": {
                    AccountMember member = CommandUtil.parseAccountMember(commandSender, name, account);
                    if(member != null) {
                        this.removeCommand.execute(commandSender, member, args);
                    }
                    break;
                }
                case "limit": {
                    AccountMember member = CommandUtil.parseAccountMember(commandSender, name, account);
                    if(member != null) {
                        this.limitCommand.execute(commandSender, member, Arrays.copyOfRange(args, 2, args.length));
                    }
                    break;
                }
                case "add": {
                    this.addCommand.execute(commandSender, new Pair<>(account, name), Arrays.copyOfRange(args, 2, args.length));
                    break;
                }
                default: {
                    commandSender.sendMessage(Messages.COMMAND_BANK_MEMBER_HELP);
                }
            }
        }
    }
}
