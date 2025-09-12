package br.com.yago.emerald.model;

import com.badlogic.gdx.math.MathUtils;

public class BattleCalculator {

    public static class DamageResult {
        public final int damage;
        public final boolean isCritical;
        public final float effectiveness;

        public DamageResult(int damage, boolean isCritical, float effectiveness) {
            this.damage = damage;
            this.isCritical = isCritical;
            this.effectiveness = effectiveness;
        }
    }

    public static DamageResult calculateDamage(Pokemon attacker, Pokemon defender, Move move) {
        // ... (código existente sem alterações)
        if (move.getCategory() == MoveCategory.STATUS) {
            return new DamageResult(0, false, 1.0f);
        }

        int attackStat;
        int defenseStat;
        if (move.getCategory() == MoveCategory.PHYSICAL) {
            attackStat = attacker.getAttack();
            defenseStat = defender.getDefense();
        } else { // SPECIAL
            attackStat = attacker.getSpecialAttack();
            defenseStat = defender.getSpecialDefense();
        }

        float damage = (((((attacker.getLevel() * 2f / 5f) + 2f) * move.getPower() * attackStat / defenseStat) / 50f) + 2f);

        float stab = 1.0f;
        if (move.getType() == attacker.getType1() || move.getType() == attacker.getType2()) {
            stab = 1.5f;
        }

        float typeEffectiveness = TypeChart.getEffectiveness(move.getType(), defender.getType1(), defender.getType2());

        float randomFactor = MathUtils.random(0.85f, 1.0f);
        
        boolean isCritical = MathUtils.random(1, 16) == 1;
        float critical = isCritical ? 2.0f : 1.0f;

        float modifier = stab * typeEffectiveness * critical * randomFactor;
        damage *= modifier;

        int finalDamage = (int) damage;
        if (finalDamage < 1 && typeEffectiveness > 0) {
            finalDamage = 1;
        }
        
        return new DamageResult(finalDamage, isCritical, typeEffectiveness);
    }

    /**
     * NOVO MÉTODO: Calcula a quantidade de experiência ganha ao derrotar um oponente.
     * Fórmula da Geração III-IV simplificada (a=1, t=1, e=1, p=1, f=1, v=1).
     * @param victor O Pokémon que venceu.
     * @param defeated O Pokémon que foi derrotado.
     * @return A quantidade de pontos de experiência ganhos.
     */
    public static int calculateExperienceGained(Pokemon victor, Pokemon defeated) {
        // a = 1 (batalha selvagem)
        // t = 1 (não implementado)
        // b = baseExperienceYield do derrotado
        // e = 1 (sem Lucky Egg)
        // L = nível do derrotado
        // p, f, v = 1 (não implementado)
        // s = 1 (número de participantes não-desmaiados que não excederam o nível 100)

        // Fórmula: (a * t * b * e * L * p * f * v) / 7s
        double experience = (1 * defeated.getBaseExperienceYield() * defeated.getLevel()) / 7.0;
        return (int) Math.floor(experience);
    }
}