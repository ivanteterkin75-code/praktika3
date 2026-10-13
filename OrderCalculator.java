package ru.edu.pr01;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/** Расчёт заказа: база -> скидка -> НДС -> итог. Вариант 4: НДС = 25 % (одна константа). */
public class OrderCalculator {

    public static final double VAT_PERCENT = 25.0;
    public static final int MIN_QUANTITY = 1;
    public static final int MAX_QUANTITY = 10_000;
    public static final double MAX_UNIT_PRICE = 5_000_000.0;
    public static final double MIN_DISCOUNT = 0.0;
    public static final double MAX_DISCOUNT = 30.0;

    public static boolean isValid(int quantity, double unitPrice, double discountPercent) {
        return validationError(quantity, unitPrice, discountPercent) == null;
    }

    /** Причина ошибки или null. Сравнения с NaN дают false, поэтому условия записаны через отрицание. */
    private static String validationError(int quantity, double unitPrice, double discountPercent) {
        if (quantity < MIN_QUANTITY || quantity > MAX_QUANTITY)
            return "количество должно быть от " + MIN_QUANTITY + " до " + MAX_QUANTITY;
        if (!(unitPrice > 0 && unitPrice <= MAX_UNIT_PRICE))
            return "цена должна быть больше 0 и не более 5 000 000 руб.";
        if (!(discountPercent >= MIN_DISCOUNT && discountPercent <= MAX_DISCOUNT))
            return "скидка должна быть от 0 до 30 %";
        return null;
    }

    public static double calculateBase(int quantity, double unitPrice) {
        return round2((double) quantity * unitPrice);
    }

    /** Скидка - процент от базовой суммы, а не рубли. */
    public static double applyDiscount(double base, double discountPercent) {
        return round2(base - base * discountPercent / 100.0);
    }

    /** НДС начисляется на сумму ПОСЛЕ скидки. */
    public static double calculateVat(double discounted, double vatPercent) {
        return round2(discounted * vatPercent / 100.0);
    }

    public static double calculateTotal(int quantity, double unitPrice, double discountPercent, double vatPercent) {
        double discounted = applyDiscount(calculateBase(quantity, unitPrice), discountPercent);
        return round2(discounted + calculateVat(discounted, vatPercent));
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /** Разбор и расчёт одной записи: печатает результат или причину ошибки. */
    private static void process(String q, String p, String d) {
        int quantity;
        double price, discount;
        try {
            quantity = Integer.parseInt(q.trim());
            price = Double.parseDouble(p.trim().replace(',', '.'));
            discount = Double.parseDouble(d.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: количество - целое число, цена и скидка - числа (получено: \""
                    + q.trim() + "\", \"" + p.trim() + "\", \"" + d.trim() + "\")");
            return;
        }
        String error = validationError(quantity, price, discount);
        if (error != null) {
            System.out.println("Ошибка: " + error);
            return;
        }
        double base = calculateBase(quantity, price);
        double discounted = applyDiscount(base, discount);
        System.out.printf(Locale.US, "Базовая сумма: %.2f руб.%nПосле скидки: %.2f руб.%nНДС (%d%%): %.2f руб.%nИтого к оплате: %.2f руб.%n",
                base, discounted, (int) VAT_PERCENT, calculateVat(discounted, VAT_PERCENT),
                calculateTotal(quantity, price, discount, VAT_PERCENT));
    }

    /** Пакетный режим: CSV с заголовком quantity;unitPrice;discountPercent. */
    private static void processCsv(String path) throws java.io.IOException {
        List<String> lines = Files.readAllLines(Paths.get(path));
        for (int i = 1; i < lines.size(); i++) {
            if (lines.get(i).trim().isEmpty()) continue;
            System.out.println("--- строка " + (i + 1) + ": " + lines.get(i));
            String[] f = lines.get(i).split(";", -1);
            if (f.length == 3) process(f[0], f[1], f[2]);
            else System.out.println("Ошибка: ожидалось 3 поля через ';'");
        }
    }

    public static void main(String[] args) throws java.io.IOException {
        if (args.length > 0) {
            processCsv(args[0]);
            return;
        }
        Scanner in = new Scanner(System.in);
        String[] v = new String[3];
        String[] prompts = {"Количество единиц: ", "Цена за единицу, руб.: ", "Скидка, %: "};
        for (int i = 0; i < 3; i++) {
            System.out.print(prompts[i]);
            v[i] = in.hasNextLine() ? in.nextLine() : "";
        }
        process(v[0], v[1], v[2]);
    }
}
