package net.pretronic.dkcoins.minecraft.commands.dkcoins;

import net.pretronic.libraries.command.command.BasicCommand;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import org.mcnative.common.plugin.MinecraftPlugin;
import org.mcnative.common.text.Text;
import org.mcnative.common.text.format.TextColor;

public class DKCoinsInfoCommand extends BasicCommand {

    public DKCoinsInfoCommand(ObjectOwner owner) {
        super(owner, CommandConfiguration.name("info","i","information","version","v"));
    }

    @Override
    public void execute(CommandSender sender, String[] strings) {
        sender.sendMessage(Text.newBuilder()
                .color(TextColor.DARK_GRAY).text("» ")
                .color(TextColor.GOLD).text("DKCoins")
                .color(TextColor.DARK_GRAY).text(" | ")
                .color(TextColor.GRAY).text("v")
                .color(TextColor.RED).text(((MinecraftPlugin)getOwner()).getDescription().getVersion().getName())
                .color(TextColor.GRAY).text(" by ")
                .color(TextColor.RED).text(((MinecraftPlugin)getOwner()).getDescription().getAuthor())
                .build());
    }
}
