package br.com.yago.emerald.model;

/**
 * Classe utilitária para calcular a experiência de um Pokémon
 * com base em seu nível e grupo de experiência.
 * Fórmulas baseadas em: https://m.bulbapedia.bulbagarden.net/wiki/Experience
 */
public class ExperienceCalculator {

    /**
     * Calcula a experiência total necessária para atingir um determinado nível.
     */
    public static int getTotalExpForLevel(int level, ExperienceGroup group) {
        if (level <= 1) return 0;
        long n = level; // Usar long para evitar overflow nos cálculos intermediários
        long exp = 0;

        switch (group) {
            case ERRATIC:
                if (n <= 50) {
                    exp = (n * n * n * (100 - n)) / 50;
                } else if (n <= 68) {
                    exp = (n * n * n * (150 - n)) / 100;
                } else if (n <= 98) {
                    exp = (n * n * n * ((1911 - 10 * n) / 3)) / 500;
                } else {
                    exp = (n * n * n * (160 - n)) / 100;
                }
                break;
            case FAST:
                exp = (4 * n * n * n) / 5;
                break;
            case MEDIUM_FAST:
                exp = n * n * n;
                break;
            case MEDIUM_SLOW:
                exp = (6 * n * n * n) / 5 - (15 * n * n) + (100 * n) - 140;
                break;
            case SLOW:
                exp = (5 * n * n * n) / 4;
                break;
            case FLUCTUATING:
                 if (n <= 15) {
                    exp = n * n * n * ((((n + 1) / 3) + 24) / 50);
                } else if (n <= 36) {
                    exp = n * n * n * ((n + 14) / 50);
                } else {
                    exp = n * n * n * (((n / 2) + 32) / 50);
                }
                break;
        }
        return (int) exp;
    }

    /**
     * Calcula a experiência necessária para ir do nível atual para o próximo.
     */
    public static int getExpToNextLevel(int currentLevel, ExperienceGroup group) {
        if (currentLevel >= 100) return 0;
        int expForCurrentLevel = getTotalExpForLevel(currentLevel, group);
        int expForNextLevel = getTotalExpForLevel(currentLevel + 1, group);
        return expForNextLevel - expForCurrentLevel;
    }

    /**
     * Calcula o nível de um Pokémon com base em sua experiência total.
     */
    public static int calculateLevelFromExp(int totalExp, ExperienceGroup group) {
        if (totalExp <= 0) return 1;
        // Começa a verificar a partir do nível 2, pois o nível 1 é 0 de exp.
        for (int level = 2; level <= 100; level++) {
            if (totalExp < getTotalExpForLevel(level, group)) {
                return level - 1;
            }
        }
        return 100; // Se a exp for maior ou igual à do nível 100
    }
}