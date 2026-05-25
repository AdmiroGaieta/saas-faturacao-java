package ao.saas.faturacao.common.enums;
public enum InvoiceType {
    INVOICE, INVOICE_RECEIPT, RECEIPT, CREDIT_NOTE, DEBIT_NOTE, PRO_FORMA, QUOTE;
    public String getPrefix() {
        switch(this) {
            case INVOICE:         return "FT";
            case INVOICE_RECEIPT: return "FR";
            case RECEIPT:         return "RC";
            case CREDIT_NOTE:     return "NC";
            case DEBIT_NOTE:      return "ND";
            case PRO_FORMA:       return "PF";
            case QUOTE:           return "OR";
            default:              return "FT";
        }
    }
    public String getLabel() {
        switch(this) {
            case INVOICE:         return "Factura";
            case INVOICE_RECEIPT: return "Factura-Recibo";
            case RECEIPT:         return "Recibo";
            case CREDIT_NOTE:     return "Nota de Crédito";
            case DEBIT_NOTE:      return "Nota de Débito";
            case PRO_FORMA:       return "Factura Pro-Forma";
            case QUOTE:           return "Orçamento";
            default:              return "Documento";
        }
    }
}
