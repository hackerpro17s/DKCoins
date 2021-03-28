package net.pretronic.dkcoins.common.account.member;

import net.pretronic.dkcoins.api.account.BankAccount;
import net.pretronic.dkcoins.api.account.access.AccessRight;
import net.pretronic.dkcoins.api.account.limitation.AccountLimitation;
import net.pretronic.dkcoins.api.account.limitation.AccountLimitationCalculationType;
import net.pretronic.dkcoins.api.account.limitation.AccountLimitationInterval;
import net.pretronic.dkcoins.api.account.member.AccountMemberRole;
import net.pretronic.dkcoins.api.currency.Currency;
import net.pretronic.dkcoins.common.account.DefaultBankAccount;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.Validate;
import net.pretronic.libraries.utility.annonations.Internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class DefaultAccountMemberRole implements AccountMemberRole {

    public static final String GUEST = "GUEST";
    public static final String USER = "USER";
    public static final String MANAGER = "MANAGER";
    public static final String ADMIN = "ADMIN";
    public static final String OWNER = "OWNER";


    private final DefaultBankAccount account;
    private final int id;
    private final AccountMemberRole parentRole;
    private final Collection<AccountMemberRole> childRoles;
    private final Collection<AccessRight> accessRights;
    private final String name;

    public DefaultAccountMemberRole(DefaultBankAccount account, int id, String name, DefaultAccountMemberRole parentRole, AccessRight... accessRights) {
        this(account, id, name, parentRole, Arrays.asList(accessRights));
    }
    public DefaultAccountMemberRole(DefaultBankAccount account, int id, String name, DefaultAccountMemberRole parentRole, Collection<AccessRight> accessRights) {
        Validate.notNull(account, name, parentRole, accessRights);
        this.account = account;
        this.id = id;
        this.name = name;
        this.parentRole = parentRole;
        this.childRoles = new ArrayList<>();
        this.accessRights = accessRights;

        if(parentRole != null) {
            parentRole.addChildRole(this);
        }
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public AccountMemberRole getParentRole() {
        return this.parentRole;
    }

    @Override
    public Collection<AccountMemberRole> getChildRoles() {
        return this.childRoles;
    }

    @Override
    public Collection<AccessRight> getAccessRights() {
        return this.accessRights;
    }

    @Override
    public boolean canAccess(AccessRight accessRight) {
        Validate.notNull(accessRight);
        for (AccessRight right : getAccessRights()) {
            if(right == accessRight) return true;
        }
        for (AccountMemberRole childRole : getChildRoles()) {
            if(childRole.canAccess(accessRight)) return true;
        }
        return false;
    }

    //Current: OWNER: parent null
    //Target: GUEST
    @Override
    public boolean isHigher(AccountMemberRole role) {
        Validate.notNull(role);
        AccountMemberRole parent = getParentRole();
        while (parent != null) {
            if(parent.equals(role)) return false;
            parent = getParentRole();
        }
        return true;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public BankAccount getAccount() {
        return this.account;
    }

    @Override
    public Collection<AccountLimitation> getLimitations() {
        return Iterators.filter(getAccount().getLimitations(), limitation -> this.equals(limitation.getMemberRole()));
    }

    @Override
    public AccountLimitation getLimitation(Currency comparativeCurrency, AccountLimitationCalculationType calculationType, double amount, AccountLimitationInterval interval) {
        return Iterators.findOne(getLimitations(), limitation -> {
            if(calculationType != limitation.getCalculationType()) return false;
            if(!comparativeCurrency.equals(limitation.getComparativeCurrency())) return false;
            if(amount != limitation.getAmount()) return false;
            if(interval != limitation.getInterval()) return false;
            return true;
        });
    }

    @Override
    public boolean hasLimitation(Currency currency, double amount) {
        return this.account.hasLimitationInternal(this, currency, amount);
    }

    @Override
    public AccountLimitation addLimitation(Currency comparativeCurrency, AccountLimitationCalculationType calculationType, double amount, AccountLimitationInterval interval) {
        return this.account.addLimitationInternal(null, this, comparativeCurrency, calculationType, amount, interval);
    }

    @Override
    public boolean removeLimitation(AccountLimitation limitation) {
        if(!this.equals(limitation.getMemberRole())) return false;
        return getAccount().removeLimitation(limitation);
    }

    @Override
    public AccountMemberRole getRole() {
        return this;
    }

    @Internal
    public void addChildRole(AccountMemberRole role) {
        Validate.notNull(role);
        this.childRoles.add(role);
    }
}
