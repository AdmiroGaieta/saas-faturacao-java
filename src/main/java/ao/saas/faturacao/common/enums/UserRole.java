package ao.saas.faturacao.common.enums;

public enum UserRole {
    SUPER_ADMIN, ADMIN, MANAGER, ACCOUNTANT, VIEWER;

    public int getLevel() {
        switch (this) {
            case VIEWER:      return 0;
            case ACCOUNTANT:  return 1;
            case MANAGER:     return 2;
            case ADMIN:       return 3;
            case SUPER_ADMIN: return 4;
            default:          return -1;
        }
    }

    public boolean hasAtLeast(UserRole required) {
        return this.getLevel() >= required.getLevel();
    }
}
