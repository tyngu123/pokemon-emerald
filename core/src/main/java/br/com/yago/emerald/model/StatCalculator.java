package br.com.yago.emerald.model;

/**
 * Classe utilitária para calcular os atributos de um Pokémon.
 * Fórmulas de Geração III+
 * Assumimos IVs perfeitos (31) e EVs zerados para esta implementação.
 */
public class StatCalculator {

    private static final int DEFAULT_IV = 31;
    private static final int DEFAULT_EV = 0;

    /**
     * Calcula o HP máximo de um Pokémon.
     */
    public static int calculateHP(int base, int level) {
        // Fórmula: floor(((2 * Base + IV + EV/4) * Level) / 100) + Level + 10
        double result = Math.floor(((2 * base + DEFAULT_IV + (double)DEFAULT_EV / 4) * level) / 100.0) + level + 10;
        return (int) result;
    }

    /**
     * Calcula um atributo (Ataque, Defesa, etc.) de um Pokémon.
     */
    public static int calculateStat(int base, int level) {
        // Fórmula: floor(((2 * Base + IV + EV/4) * Level) / 100) + 5
         double result = Math.floor(((2 * base + DEFAULT_IV + (double)DEFAULT_EV / 4) * level) / 100.0) + 5;
        return (int) result;
    }
}