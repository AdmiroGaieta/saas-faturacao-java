package ao.saas.faturacao.common.enums;
public enum PaymentMethod {
    CASH, BANK_TRANSFER, CHECK, MULTICAIXA, CREDIT_CARD, DEBIT_CARD, OTHER;
    public String getLabel() {
        switch(this) {
            case CASH:          return "Numerário";
            case BANK_TRANSFER: return "Transferência Bancária";
            case CHECK:         return "Cheque";
            case MULTICAIXA:    return "Multicaixa";
            case CREDIT_CARD:   return "Cartão de Crédito";
            case DEBIT_CARD:    return "Cartão de Débito";
            default:            return "Outro";
        }
    }
}
