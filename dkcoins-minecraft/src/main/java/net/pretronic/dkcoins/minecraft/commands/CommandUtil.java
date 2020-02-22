/*
 * (C) Copyright 2020 The DKCoins Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 13.02.20, 16:21
 * @website %web%
 *
 * %license%
 */

package net.pretronic.dkcoins.minecraft.commands;

public final class CommandUtil {

    public static String buildReason(String[] args, int start, int end) {
        StringBuilder reasonBuilder = new StringBuilder();
        if(args.length > 3) {
            for (int i = start; i < end; i++) {
                reasonBuilder.append(args[i]);
            }
        } else {
            reasonBuilder = new StringBuilder("none");
        }
        return reasonBuilder.toString();
    }

    public static String buildReason(String[] args, int start) {
        return buildReason(args, start, args.length);
    }
}
