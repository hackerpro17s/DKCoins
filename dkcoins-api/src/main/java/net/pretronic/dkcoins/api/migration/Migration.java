package net.pretronic.dkcoins.api.migration;

public interface Migration {

    String getName();

    boolean migrate();
}
