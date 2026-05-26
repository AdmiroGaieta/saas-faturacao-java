package ao.saas.faturacao.modules.hr.payroll.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Cálculo do IRT (Imposto sobre o Rendimento do Trabalho) — Angola
 * Tabela progressiva conforme Lei n.º 18/14 (alterada)
 *
 * Base tributável = Salário bruto - INSS empregado (3%)
 *
 * Escalões (mensais em AOA):
 *  Até 70.000           → Isento
 *  70.001 – 100.000     → 10%  sobre excedente de 70.000
 *  100.001 – 150.000    → 13%  sobre excedente de 100.000  + 3.000
 *  150.001 – 200.000    → 16%  sobre excedente de 150.000  + 9.500
 *  200.001 – 300.000    → 18%  sobre excedente de 200.000  + 17.500
 *  300.001 – 500.000    → 19%  sobre excedente de 300.000  + 35.500
 *  500.001 – 1.000.000  → 20%  sobre excedente de 500.000  + 73.500
 *  Acima de 1.000.000   → 21%  sobre excedente de 1.000.000 + 173.500
 */
@Component
public class IrtCalculator {

    private static final BigDecimal INSS_EMPLOYEE_RATE = new BigDecimal("0.03"); // 3%
    private static final BigDecimal INSS_EMPLOYER_RATE = new BigDecimal("0.08"); // 8%

    public BigDecimal calcInssEmployee(BigDecimal baseSalary) {
        if (baseSalary == null || baseSalary.compareTo(BigDecimal.ZERO) <= 0)
            return BigDecimal.ZERO;
        return baseSalary.multiply(INSS_EMPLOYEE_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calcInssEmployer(BigDecimal baseSalary) {
        if (baseSalary == null || baseSalary.compareTo(BigDecimal.ZERO) <= 0)
            return BigDecimal.ZERO;
        return baseSalary.multiply(INSS_EMPLOYER_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula o IRT sobre a base tributável.
     * Base tributável = salário bruto − INSS do empregado
     */
    public BigDecimal calcIrt(BigDecimal grossSalary, BigDecimal inssEmployee) {
        if (grossSalary == null) return BigDecimal.ZERO;
        BigDecimal base = grossSalary.subtract(
                inssEmployee != null ? inssEmployee : BigDecimal.ZERO);
        return calcIrtOnBase(base);
    }

    private BigDecimal calcIrtOnBase(BigDecimal base) {
        if (base.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;

        BigDecimal irt;
        BigDecimal b = base;

        if (b.compareTo(bd(70_000)) <= 0) {
            irt = BigDecimal.ZERO;                                          // Isento

        } else if (b.compareTo(bd(100_000)) <= 0) {
            irt = b.subtract(bd(70_000)).multiply(bd("0.10"));              // 10%

        } else if (b.compareTo(bd(150_000)) <= 0) {
            irt = b.subtract(bd(100_000)).multiply(bd("0.13")).add(bd(3_000)); // 13% + 3k

        } else if (b.compareTo(bd(200_000)) <= 0) {
            irt = b.subtract(bd(150_000)).multiply(bd("0.16")).add(bd(9_500)); // 16% + 9.5k

        } else if (b.compareTo(bd(300_000)) <= 0) {
            irt = b.subtract(bd(200_000)).multiply(bd("0.18")).add(bd(17_500)); // 18% + 17.5k

        } else if (b.compareTo(bd(500_000)) <= 0) {
            irt = b.subtract(bd(300_000)).multiply(bd("0.19")).add(bd(35_500)); // 19% + 35.5k

        } else if (b.compareTo(bd(1_000_000)) <= 0) {
            irt = b.subtract(bd(500_000)).multiply(bd("0.20")).add(bd(73_500)); // 20% + 73.5k

        } else {
            irt = b.subtract(bd(1_000_000)).multiply(bd("0.21")).add(bd(173_500)); // 21% + 173.5k
        }

        return irt.setScale(2, RoundingMode.HALF_UP);
    }

    /** Calcula a taxa efectiva de IRT (informativo) */
    public BigDecimal effectiveIrtRate(BigDecimal grossSalary, BigDecimal irt) {
        if (grossSalary == null || grossSalary.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        if (irt == null || irt.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return irt.divide(grossSalary, 4, RoundingMode.HALF_UP)
                  .multiply(bd("100")).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal bd(long value) { return BigDecimal.valueOf(value); }
    private BigDecimal bd(String value) { return new BigDecimal(value); }
}
