package ru.edu.pr01;

public class SelfCheck {
    private static int failed = 0;

    private static void check(String name, boolean cond) {
        if (!cond) { failed++; System.out.println("FAIL " + name); }
    }

    private static boolean eq(double a, double b) { return Math.abs(a - b) < 0.001; }

    public static void main(String[] args) {
        double vat = OrderCalculator.VAT_PERCENT;
        // допустимые значения: границы включены
        int[] okQty = {1, 10000};
        double[] okPrice = {0.01, 5_000_000}, okDisc = {0, 30};
        for (int q : okQty) check("qty=" + q, OrderCalculator.isValid(q, 100, 0));
        for (double p : okPrice) check("price=" + p, OrderCalculator.isValid(1, p, 0));
        for (double d : okDisc) check("disc=" + d, OrderCalculator.isValid(1, 100, d));
        // недопустимые значения: сразу за границей, отрицательные, NaN
        check("qty=0", !OrderCalculator.isValid(0, 100, 0));
        check("qty=10001", !OrderCalculator.isValid(10001, 100, 0));
        check("price=0", !OrderCalculator.isValid(1, 0, 0));
        check("price>5e6", !OrderCalculator.isValid(1, 5_000_000.01, 0));
        check("price<0", !OrderCalculator.isValid(1, -20, 5));
        check("price NaN", !OrderCalculator.isValid(1, Double.NaN, 5));
        check("disc<0", !OrderCalculator.isValid(1, 100, -0.01));
        check("disc>30", !OrderCalculator.isValid(1, 100, 30.01));
        // расчёты (вариант 4: НДС 25 % после скидки)
        check("base", OrderCalculator.calculateBase(2, 100.0) == 200.0);
        check("discount 10%", eq(OrderCalculator.applyDiscount(200, 10), 180.0));
        check("vat 25%", eq(OrderCalculator.calculateVat(180, vat), 45.0));
        check("total", eq(OrderCalculator.calculateTotal(2, 100.0, 10.0, vat), 225.0));
        check("total sample", eq(OrderCalculator.calculateTotal(12, 2490.50, 5, vat), 35489.63));
        check("VAT_PERCENT == 25", vat == 25.0);

        System.out.println(failed == 0 ? "PASS" : "FAIL  failed checks: " + failed);
    }
}
