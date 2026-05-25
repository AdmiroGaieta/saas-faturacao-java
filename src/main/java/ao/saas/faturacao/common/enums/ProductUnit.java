package ao.saas.faturacao.common.enums;
public enum ProductUnit {
    UNIT, KG, LITER, METER, M2, M3, HOUR, DAY, MONTH, YEAR;
    public String getLabel() {
        switch(this) {
            case UNIT:  return "Un";   case KG:    return "kg";
            case LITER: return "L";    case METER: return "m";
            case M2:    return "m²";   case M3:    return "m³";
            case HOUR:  return "h";    case DAY:   return "dia";
            case MONTH: return "mês";  case YEAR:  return "ano";
            default:    return "Un";
        }
    }
}
